package cn.net.autocode.dbManager.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;


public abstract class NamedParameterUtils {

	
	private static final String[] START_SKIP = new String[] {"'", "\"", "--", "/*"};

	
	private static final String[] STOP_SKIP = new String[] {"'", "\"", "\n", "*/"};

	
	private static final String PARAMETER_SEPARATORS = "\"':&,;()|=+-*%/\\<>^";

	
	private static final boolean[] separatorIndex = new boolean[128];

	static {
		for (char c : PARAMETER_SEPARATORS.toCharArray()) {
			separatorIndex[c] = true;
		}
	}


	/**
	 * SQL解析
	 * @param sql
	 * @return
	 */
	public static ParsedSql parseSqlStatement(final String sql) {
		Assert.notNull(sql, "SQL must not be null");
		Set<String> namedParameters = new HashSet<>();
		String sqlToUse = sql;
		List<ParameterHolder> parameterList = new ArrayList<>();

		char[] statement = sql.toCharArray();
		int namedParameterCount = 0;
		int unnamedParameterCount = 0;
		int totalParameterCount = 0;

		int escapes = 0;
		int i = 0;
		while (i < statement.length) {
			int skipToPosition = i;
			while (i < statement.length) {
				skipToPosition = skipCommentsAndQuotes(statement, i);
				if (i == skipToPosition) {
					break;
				}
				else {
					i = skipToPosition;
				}
			}
			if (i >= statement.length) {
				break;
			}
			char c = statement[i];
			if (c == ':' || c == '&') {
				int j = i + 1;
				if (c == ':' && j < statement.length && statement[j] == ':') {
					// Postgres-style "::" casting operator should be skipped
					i = i + 2;
					continue;
				}
				String parameter = null;
				if (c == ':' && j < statement.length && statement[j] == '{') {
					// :{x} style parameter
					while (statement[j] != '}') {
						j++;
						if (j >= statement.length) {
							throw new InvalidDataAccessApiUsageException("Non-terminated named parameter declaration " +
									"at position " + i + " in statement: " + sql);
						}
						if (statement[j] == ':' || statement[j] == '{') {
							throw new InvalidDataAccessApiUsageException("Parameter name contains invalid character '" +
									statement[j] + "' at position " + i + " in statement: " + sql);
						}
					}
					if (j - i > 2) {
						parameter = sql.substring(i + 2, j);
						namedParameterCount = addNewNamedParameter(namedParameters, namedParameterCount, parameter);
						totalParameterCount = addNamedParameter(
								parameterList, totalParameterCount, escapes, i, j + 1, parameter);
					}
					j++;
				}
				else {
					while (j < statement.length && !isParameterSeparator(statement[j])) {
						j++;
					}
					if (j - i > 1) {
						parameter = sql.substring(i + 1, j);
						namedParameterCount = addNewNamedParameter(namedParameters, namedParameterCount, parameter);
						totalParameterCount = addNamedParameter(
								parameterList, totalParameterCount, escapes, i, j, parameter);
					}
				}
				i = j - 1;
			}
			else {
				if (c == '\\') {
					int j = i + 1;
					if (j < statement.length && statement[j] == ':') {
						// escaped ":" should be skipped
						sqlToUse = sqlToUse.substring(0, i - escapes) + sqlToUse.substring(i - escapes + 1);
						escapes++;
						i = i + 2;
						continue;
					}
				}
				if (c == '?') {
					int j = i + 1;
					if (j < statement.length && (statement[j] == '?' || statement[j] == '|' || statement[j] == '&')) {
						// Postgres-style "??", "?|", "?&" operator should be skipped
						i = i + 2;
						continue;
					}
					unnamedParameterCount++;
					totalParameterCount++;
				}
			}
			i++;
		}
		ParsedSql parsedSql = new ParsedSql(sqlToUse);
		for (ParameterHolder ph : parameterList) {
			parsedSql.addNamedParameter(ph.getParameterName(), ph.getStartIndex(), ph.getEndIndex());
		}
		parsedSql.setNamedParameterCount(namedParameterCount);
		parsedSql.setUnnamedParameterCount(unnamedParameterCount);
		parsedSql.setTotalParameterCount(totalParameterCount);
		return parsedSql;
	}

	
	private static int addNamedParameter(
			List<ParameterHolder> parameterList, int totalParameterCount, int escapes, int i, int j, String parameter) {

		parameterList.add(new ParameterHolder(parameter, i - escapes, j - escapes));
		totalParameterCount++;
		return totalParameterCount;
	}

	private static int addNewNamedParameter(Set<String> namedParameters, int namedParameterCount, String parameter) {
		if (!namedParameters.contains(parameter)) {
			namedParameters.add(parameter);
			namedParameterCount++;
		}
		return namedParameterCount;
	}

	private static int skipCommentsAndQuotes(char[] statement, int position) {
		for (int i = 0; i < START_SKIP.length; i++) {
			if (statement[position] == START_SKIP[i].charAt(0)) {
				boolean match = true;
				for (int j = 1; j < START_SKIP[i].length(); j++) {
					if (statement[position + j] != START_SKIP[i].charAt(j)) {
						match = false;
						break;
					}
				}
				if (match) {
					int offset = START_SKIP[i].length();
					for (int m = position + offset; m < statement.length; m++) {
						if (statement[m] == STOP_SKIP[i].charAt(0)) {
							boolean endMatch = true;
							int endPos = m;
							for (int n = 1; n < STOP_SKIP[i].length(); n++) {
								if (m + n >= statement.length) {
									// last comment not closed properly
									return statement.length;
								}
								if (statement[m + n] != STOP_SKIP[i].charAt(n)) {
									endMatch = false;
									break;
								}
								endPos = m + n;
							}
							if (endMatch) {
								// found character sequence ending comment or quote
								return endPos + 1;
							}
						}
					}
					// character sequence ending comment or quote not found
					return statement.length;
				}
			}
		}
		return position;
	}

	private static String NotNamedParmeters(String actSql) {
		actSql = actSql.trim();
		List<String> operator = Arrays.asList(" or "," and ");
		if(!actSql.contains("@CZF@")) {
			return actSql;
		}
		
		boolean flagend = false;
		if(actSql.endsWith("@CZF@")) {
			flagend = true;
		}
		
		
		String[] splitSql = actSql.split("@CZF@");
		int len = splitSql.length;
		for(int i=0;i<len;i++) {
			String sql = splitSql[i];
			if(i>0&&(splitSql[i-1].endsWith("(")||splitSql[i-1].endsWith(" where "))) {
				sql = sql.replaceAll("^[ ]+", "");
				if(sql.startsWith("and ")) {
					sql = sql.substring(4);
				}else if(sql.startsWith("or ")) {
					sql = sql.substring(3);
				}
			}
			
			
			
			if(!flagend&&i==(len-1)) {
				splitSql[i] = sql;
			}else {
				int last = -1;
				for(String oper:operator) {
					int nlast = sql.lastIndexOf(oper);
					if(nlast>last) {
						last = nlast;
					}
				}
				
				if(last==-1) {
					int nlast = sql.lastIndexOf(" where ");
					if(nlast>-1) {
						last = nlast+7;
					}
				}
				if(sql.indexOf('(', last)>-1) {
					String sql2 = splitSql[i+1].replaceAll("^[ ]+", "");
					if(sql2.startsWith(")")) {
						splitSql[i] = sql.substring(0, last);
						splitSql[i+1] = sql2.substring(1);
					}else {
						splitSql[i]  = sql.substring(0,sql.lastIndexOf('(')+1);
					}
				}else {
					splitSql[i] = sql.substring(0, last);
				}
			}
		}
		
		StringBuilder actBuilder = new StringBuilder(); 
		for(String itemSql:splitSql) {
			actBuilder.append(itemSql);
		}
		return actBuilder.toString();
	}
	
	
	private static void substituteNamedParameters(ParsedSql parsedSql, @Nullable Map<String,Object> paramSource) {
		
		if(parsedSql.getUnnamedParameterCount()>0&&parsedSql.getNamedParameterCount()>0) {
			throw new RuntimeException("sql语句中不能同时存在占位符与命名参数！");
		}
		
		String originalSql = parsedSql.getOriginalSql();
		List<String> paramNames = parsedSql.getParameterNames();
		if (paramNames==null||paramNames.isEmpty()) {
			parsedSql.setActualSql(originalSql);
			return;
		}
		
		List<Object> parameterList = new ArrayList<Object>();
		StringBuilder actualSql = new StringBuilder(originalSql.length());
		
		int lastIndex = 0;
		for (int i = 0; i < paramNames.size(); i++) {
			String paramName = paramNames.get(i);
			int[] indexes = parsedSql.getParameterIndexes(i);
			int startIndex = indexes[0];
			int endIndex = indexes[1];
			actualSql.append(originalSql, lastIndex, startIndex);
			if (paramSource != null && paramSource.containsKey(paramName)&&paramSource.get(paramName)!=null) {
				Object value = paramSource.get(paramName);
				if(value instanceof String) {
					if(value.toString().trim().isEmpty()) {
						actualSql.append("@CZF@");
					}else {
						actualSql.append('?');
						parameterList.add(value);
					}
				}else {
					actualSql.append('?');
					parameterList.add(value);
				}
				
			}else {
				actualSql.append("@CZF@");
			}
			
			lastIndex = endIndex;
		}
		
		actualSql.append(originalSql, lastIndex, originalSql.length());
		
		
		/**
		 * 设置实际sql
		 */
		parsedSql.setActualSql(NotNamedParmeters(actualSql.toString()));
		parsedSql.setParameterValues(parameterList.toArray());
		
	}

	

	private static boolean isParameterSeparator(char c) {
		return (c < 128 && separatorIndex[c]) || Character.isWhitespace(c);
	}

	
	public static ParsedSql parseSqlNamedParameters(String sql, Map<String,Object> paramSource) {
		ParsedSql parsedSql = parseSqlStatement(sql);
		substituteNamedParameters(parsedSql, paramSource);
		return parsedSql;
	}

	
	private static class ParameterHolder {

		private final String parameterName;

		private final int startIndex;

		private final int endIndex;

		public ParameterHolder(String parameterName, int startIndex, int endIndex) {
			this.parameterName = parameterName;
			this.startIndex = startIndex;
			this.endIndex = endIndex;
		}

		public String getParameterName() {
			return this.parameterName;
		}

		public int getStartIndex() {
			return this.startIndex;
		}

		public int getEndIndex() {
			return this.endIndex;
		}
	}
	
}
