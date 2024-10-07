package com.autocode.dbManager.handler;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.*;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;


/**
 * 
 * &#064;Description:  将数据库的查询结果转化为JSON对象
 * @author 陈忠峰
 * @version 1.00.00
 * 
 */
public class JsonHandler{

	public static JSONObject handle(SqlRowSet rs) throws SQLException, JSONException {
		/*
		 * 1、获取sql查询语句的列
		 */
		SqlRowSetMetaData rsmd = rs.getMetaData();
		
		/*
		 * 2、将列名转化为map对应的key值
		 */
		String[] keys = mapColumnsToKey(rsmd);
		if(rs.next()){
			return createJSON(rs,keys);
		}else{
			return new JSONObject();
		}
	}

	public static JSONObject handle2(SqlRowSet rs,String defaultValue) throws SQLException, JSONException {
		/*
		 * 1、获取sql查询语句的列
		 */
		SqlRowSetMetaData rsmd = rs.getMetaData();
		
		/*
		 * 2、将列名转化为map对应的key值
		 */
		String[] keys = mapColumnsToKey(rsmd);
		if(rs.next()){
			return createJSON2(rs,keys,defaultValue);
		}else{
			return null;
		}
	}
	
	public static JSONArray handleList(SqlRowSet rs) throws SQLException, JSONException {
		/*
		 * 1、获取sql查询语句的列
		 */
		SqlRowSetMetaData rsmd = rs.getMetaData();
		
		/*
		 * 2、将列名转化为map对应的key值
		 */
		String[] keys = mapColumnsToKey(rsmd);
		
		JSONArray array = new JSONArray();
		
		while(rs.next()){
			array.add(createJSON(rs,keys));
		}
		return array;
	}
	
	public static JSONArray handleList2(SqlRowSet rs,String defaultValue) throws SQLException, JSONException {
		/*
		 * 1、获取sql查询语句的列
		 */
		SqlRowSetMetaData rsmd = rs.getMetaData();
		
		/*
		 * 2、将列名转化为map对应的key值
		 */
		String[] keys = mapColumnsToKey(rsmd);
		
		JSONArray array = new JSONArray();
		
		while(rs.next()){
			array.add(createJSON2(rs,keys,defaultValue));
		}
		return array;
	}
	
	private static String[] mapColumnsToKey(SqlRowSetMetaData rsmd){
		
		int cols = rsmd.getColumnCount();
		String[] keys = new String[cols + 1];

		for (int col = 1; col <= cols; col++) {
			String columnName = rsmd.getColumnLabel(col);
			if (null == columnName || 0 == columnName.length()) {
				columnName = rsmd.getColumnName(col);
			}
			String key = PreparedStatementHandler.columnToProperty(columnName);
			keys[col]=key;
		}
		return keys;
	}
	
	private static JSONObject createJSON(SqlRowSet rs,	String[] keys) throws JSONException, SQLException {
		JSONObject json = new JSONObject();

		for (int i = 1; i < keys.length; i++) {
			Object value = rs.getObject(i);
			if(value==null){
				value = "";
			}
			json.put(keys[i], value);
		}
		
		return json;
	}
	
	private static JSONObject createJSON2(SqlRowSet rs,	String[] keys,String defaultValue) throws JSONException, SQLException {

		JSONObject json = new JSONObject();

		for (int i = 1; i < keys.length; i++) {
			Object value = rs.getObject(i);
			if(value==null||value.toString().equals("")){
				value = defaultValue;
			}
			json.put(keys[i], value);
		}
		
		return json;
	}
	
	
	public static <T> Object[][] columnValue(String[] columns,JSONObject json,List<String> primaryKey) throws SQLException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		/*
		 * 1、获取所有的属性字段
		 */
		List<String> columnList = new ArrayList<String>();
		List<Object> valueList = new ArrayList<Object>();
		
		List<String> idColumn = new ArrayList<String>();
		List<Object> idValue = new ArrayList<Object>();
		
		//将json数据处理
		JSONObject nj = new JSONObject();
		
		Set<String> keys = json.keySet();
		for(String key:keys) {
			nj.put(key.toLowerCase(), json.get(key));
		}
		
		for (int col = 0; col < columns.length; col++) {
			String columnName = columns[col].toLowerCase();
			String propertyName = columnName.replaceAll("_", "").toLowerCase();
			
			if(nj.containsKey(propertyName)){
				Object value = nj.get(propertyName);
				if(!primaryKey.contains(columnName)){
					columnList.add(columnName);
					if("".equals(value)) {
						valueList.add(null);
					}else {
						if(value instanceof ArrayList<?>){
							valueList.add(value.toString());
						}else{
							valueList.add(value);
						}
					}
				}else if(primaryKey.contains(columnName)){
					idColumn.add(columnName);
					if("".equals(value)) {
						idValue.add(null);
					}else {
						if(value instanceof ArrayList<?>){
							idValue.add(value.toString());
						}else{
							idValue.add(value);
						}
					}
				}
			}
		}
		
		columnList.addAll(idColumn);
		valueList.addAll(idValue);
		
		Object[][] cv = new Object[2][];
		cv[0] = columnList.toArray();
		cv[1] = valueList.toArray();
		return cv;
	}


	public static <T> Map<String,Object> batchInsertValue(String[] columns,JSONArray jsonArray) throws SQLException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		/*
		 * 1、获取所有的属性字段
		 */
		List<String> columnList = new ArrayList<String>();
		List<Object[]> valueList = new ArrayList<Object[]>();


		//
		JSONObject json = jsonArray.getJSONObject(0);
		//将json数据处理
		Set<String> keys = json.keySet();
		Map<String,String> keyMap = new HashMap<String,String>();
		for(String key:keys) {
			keyMap.put(key.toLowerCase(), key);
		}

		int len = jsonArray.size();
		int collen = columns.length;
		for(int i=0;i<len;i++){
			JSONObject nj = jsonArray.getJSONObject(i);
			Object[] valArray = new Object[collen];
			valueList.add(valArray);
			if(i==0){
				for (int col = 0; col < collen; col++) {
					String columnName = columns[col].toLowerCase();
					columnList.add(columnName);
					String propertyName = columnName.replaceAll("_", "").toLowerCase();
					Object value = nj.get(keyMap.get(propertyName));
					if("".equals(value)) {
						valArray[col] = null;
					}else {
						valArray[col] = value;
					}
				}
			}else{
				for (int col = 0; col < collen; col++) {
					String columnName = columns[col].toLowerCase();
					String propertyName = columnName.replaceAll("_", "").toLowerCase();
					Object value = nj.get(keyMap.get(propertyName));
					if("".equals(value)) {
						valArray[col] = null;
					}else {
						valArray[col] = value;
					}
				}
			}
		}

		Map<String,Object> cv = new HashMap<String,Object>();
		cv.put("columnArray",columnList.toArray());
		cv.put("valueList",valueList);
		return cv;
	}


	public static <T> Object[][] columnNotNullValue(String[] columns,JSONObject json,List<String> primaryKey) throws SQLException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		/*
		 * 1、获取所有的属性字段
		 */
		List<String> columnList = new ArrayList<String>();
		List<Object> valueList = new ArrayList<Object>();
		
		List<String> idColumn = new ArrayList<String>();
		List<Object> idValue = new ArrayList<Object>();
		
		//将json数据处理
		JSONObject nj = new JSONObject();
		
		Set<String> keys = json.keySet();
		for(String key:keys) {
			nj.put(key.toLowerCase(), json.get(key));
		}
		
		for (int col = 0; col < columns.length; col++) {
			String columnName = columns[col].toLowerCase();
			String propertyName = columnName.replaceAll("_", "").toLowerCase();
			
			if(nj.containsKey(propertyName)){
				Object value = nj.get(propertyName);
				if(!primaryKey.contains(columnName)){
					columnList.add(columnName);
					if(value!=null&&"".equals(value)) {
						valueList.add(null);
					}else {
						valueList.add(value);
					}
				}else if(primaryKey.contains(columnName)){
					idColumn.add(columnName);
					idValue.add(value);
				}
			}
		}
		
		columnList.addAll(idColumn);
		valueList.addAll(idValue);
		
		Object[][] cv = new Object[2][];
		cv[0] = columnList.toArray();
		cv[1] = valueList.toArray();
		return cv;
	}
	
}
