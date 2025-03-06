package cn.net.autocode.dbManager.handler;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;

/**
 * 
 * &#064;Description:  将查询结果转化为Map对象
 * @author 陈忠峰
 * @version 1.00.00
 *
 */
public class MapHandler{
	

	public static Map<String,Object> handle(SqlRowSet rs) throws SQLException  {
		/*
		 * 1、获取sql查询语句的列
		 */
		SqlRowSetMetaData rsmd = rs.getMetaData();
		
		/*
		 * 2、将列名转化为map对应的key值
		 */
		String[][] mes = mapColumnsToKey(rsmd);
		String[] keys = mes[0];
		String[] types = mes[1];
		
		if(rs.next()){
			return createMap(rs,keys,types);
		}else{
			return new HashMap<String,Object>();
		}
	}
	
	public static List<Map<String,Object>> handleList(SqlRowSet rs) throws SQLException {
		/*
		 * 1、获取sql查询语句的列
		 */
		SqlRowSetMetaData rsmd = rs.getMetaData();
		
		/*
		 * 2、将列名转化为map对应的key值
		 */
		String[][] mes = mapColumnsToKey(rsmd);
		String[] keys = mes[0];
		String[] types = mes[1];
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		while(rs.next()){
			list.add(createMap(rs,keys,types));
		}
		return list;
	}
	
	private static String[][] mapColumnsToKey(SqlRowSetMetaData rsmd) throws SQLException {
		
		int cols = rsmd.getColumnCount();
		String[] keys = new String[cols + 1];
		String[] types = new String[cols+1];
		for (int col = 1; col <= cols; col++) {
			String columnName = rsmd.getColumnLabel(col);
			if (null == columnName || 0 == columnName.length()) {
				columnName = rsmd.getColumnName(col);
			}
			String key = PreparedStatementHandler.columnToProperty(columnName);
			keys[col]=key;
			
			String type = rsmd.getColumnTypeName(col);
			types[col] = type;
		}
		String[][] mes = new String[2][];
		mes[0] = keys;
		mes[1] = types;
		return mes;
	}

	private static Map<String,Object> createMap(SqlRowSet rs,String[] keys,String[] types) throws SQLException {

		Map<String, Object> map = new HashMap<String,Object>();
		
		
		for (int i = 1; i < keys.length; i++) {
			Object value = rs.getObject(i);
			map.put(keys[i], value);
		}
		
		return map;
	}
}
