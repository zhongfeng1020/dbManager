package cn.net.autocode.dbManager.handler;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;


/**
 * 
 * &#064;Description:  将数据库查询结构转化为bean对象
 * @author 陈忠峰
 * @version 1.00.00
 *
 */
public class BeanHandler{
	
	public static <T> T handle(SqlRowSet rs,Class<T> clazz) throws SQLException  {
		
		/*
		 * 1、获取所有的属性字段
		 */
		PropertyDescriptor[] props = BeanProcessor.propertyDescriptors(clazz);
		/*
		 * 2、获取sql查询语句的列
		 */
		SqlRowSetMetaData rsmd = rs.getMetaData();
		
		/*
		 * 3、建立列名与字段名的映射
		 */
		int[] columnToProperty = BeanProcessor.mapColumnsToProperties(rsmd, props);
		
		try {
			if(rs.next()){
				return BeanProcessor.createBean(rs, clazz, props, columnToProperty);
			}else{
				return null;
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	public static <T> List<T> handleList(SqlRowSet rs,Class<T> clazz) throws SQLException {
		/*
		 * 1、获取所有的属性字段
		 */
		PropertyDescriptor[] props = BeanProcessor.propertyDescriptors(clazz);
		/*
		 * 2、获取sql查询语句的列
		 */
		SqlRowSetMetaData rsmd = rs.getMetaData();
		
		/*
		 * 3、建立列名与字段名的映射
		 */
		int[] columnToProperty = BeanProcessor.mapColumnsToProperties(rsmd, props);
		
		List<T> list = new ArrayList<>();
		while(rs.next()){
			T bean = BeanProcessor.createBean(rs, clazz, props, columnToProperty);
			list.add(bean);
		}
		return list;
	}
	
	
	/**
	 * 获取insert sql语句
	 * @param columns 数据库表列集合
	 * @param tableName 数据表表名
	 * @return insert sql语句
	 */
	public static String insertSql(Object[] columns,String tableName) {
		int cols =  columns.length;
		StringBuilder columnsStr = new StringBuilder("(");
		StringBuilder valuesStr = new StringBuilder("(");
		
		for (int col = 0; col <cols; col++) {
			if(col>0){
				columnsStr.append(",");
				valuesStr.append(",");
			}
			columnsStr.append(columns[col]);
			valuesStr.append("?");
		}
		columnsStr.append(")");
		valuesStr.append(")");
		StringBuilder insertSql = new StringBuilder("insert into ");
		insertSql.append(tableName);
		insertSql.append(columnsStr);
		insertSql.append(" values ");
		insertSql.append(valuesStr);
		return insertSql.toString();
	}
	
	/**
	 * 获取updateSql语句
	 * @param columns 数据库表列集合
	 * @param tableName 数据库表表名
	 * @return update sql语句
	 */
	public static String updateSql(Object[] columns,String tableName,List<String> primaryKey) {
		int cols =  columns.length;
		
		StringBuilder setStr = new StringBuilder();
		StringBuilder whereStr = new StringBuilder();
		for(int i=(cols-primaryKey.size());i<cols;i++) {
			if(i==(cols-primaryKey.size())) {
				whereStr.append(" where "+columns[i]+" = ?");
			}else {
				whereStr.append(" and "+columns[i]+" = ?");
			}
		}
		for (int col = 0; col <cols-primaryKey.size(); col++) {
			if(col>0){
				setStr.append(","+columns[col]+" = ?");
			}else{
				setStr.append(columns[col]+" = ?");
			}		
		}
		
		StringBuilder updateSql = new StringBuilder("update ");
		updateSql.append(tableName);
		updateSql.append(" set ");
		updateSql.append(setStr);
		updateSql.append(whereStr);
		return updateSql.toString();
	}
	
	public static <T> Object[][] columnValue(String[] columns,T bean,List<String> primaryKey) throws SQLException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		/*
		 * 1、获取所有的属性字段
		 */
		PropertyDescriptor[] props = BeanProcessor.propertyDescriptors(bean.getClass());

		Map<String, Integer> propertyMap = new HashMap<String, Integer>();
		for (int i = 0; i < props.length; i++) {
			propertyMap.put(props[i].getName().toLowerCase(), i);
		}
		
		List<String> columnList = new ArrayList<String>();
		List<Object> valueList = new ArrayList<Object>();
		
		List<String> idColumn = new ArrayList<String>();
		List<Object> idValue = new ArrayList<Object>();
		
		for (int col = 0; col < columns.length; col++) {
			String columnName = columns[col].toLowerCase();
			String propertyName = columnName.replaceAll("_", "").toLowerCase();
			if(propertyMap.containsKey(propertyName)){
				int i = propertyMap.get(propertyName);
				Object value = props[i].getReadMethod().invoke(bean);
				if(value != null && !primaryKey.contains(columnName)){
					columnList.add(columnName);
					valueList.add(value);
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
