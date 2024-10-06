package com.autocode.dbManager.handler;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;

/**
 * @author 陈忠峰
 * @version 1.00.00
 * 
 */
public class BeanProcessor {

	private static final int PROPERTY_NOT_FOUND = -1;

	private static final Map<Class<?>, Object> primitiveDefaults = new HashMap<>();

	static {
		primitiveDefaults.put(Integer.TYPE, 0);
		primitiveDefaults.put(Short.TYPE, 0);
		primitiveDefaults.put(Byte.TYPE, 0);
		primitiveDefaults.put(Float.TYPE, 0f);
		primitiveDefaults.put(Double.TYPE, 0d);
		primitiveDefaults.put(Long.TYPE, 0L);
		primitiveDefaults.put(Boolean.TYPE, Boolean.FALSE);
		primitiveDefaults.put(Character.TYPE, 0);
	}
	
	/**
	 * 1、获取类的所有属性字段
	 */
	public static PropertyDescriptor[] propertyDescriptors(Class<?> clazz){
		BeanInfo beanInfo;
		try {
			beanInfo = Introspector.getBeanInfo(clazz);

		} catch (IntrospectionException e) {
			throw new RuntimeException("Bean introspection failed: "
					+ e.getMessage());
		}
		return beanInfo.getPropertyDescriptors();
	}
	
	/**
	 * 3、获取列与字段的对应关系
	 */
	public static int[] mapColumnsToProperties(SqlRowSetMetaData rsmd,
			PropertyDescriptor[] props) {

		int cols = rsmd.getColumnCount();
		int[] columnToProperty = new int[cols + 1];
		Arrays.fill(columnToProperty, PROPERTY_NOT_FOUND);

		Map<String, Integer> propertyMap = new HashMap<>();
		for (int i = 0; i < props.length; i++) {
			propertyMap.put(props[i].getName().toLowerCase(), i);
		}

		for (int col = 1; col <= cols; col++) {
			String columnName = rsmd.getColumnLabel(col);
			if (0 == columnName.length()) {
				columnName = rsmd.getColumnName(col);
			}
			String propertyName = columnName.replaceAll("_", "").toLowerCase();
			if (propertyMap.containsKey(propertyName)) {
				columnToProperty[col] = propertyMap.get(propertyName);
			}
		}

		return columnToProperty;
	}
	
	public static Object processColumn(SqlRowSet rs, int index, Class<?> propType) {

		if (!propType.isPrimitive() && rs.getObject(index) == null) {
			return null;
		}
		
		if (propType.equals(Integer.TYPE) || propType.equals(Integer.class)) {
			return rs.getInt(index);
		} else if(propType.equals(Short.TYPE) || propType.equals(Short.class)){
			return rs.getShort(index);
		} else if(propType.equals(Long.TYPE) || propType.equals(Long.class)){
			return rs.getLong(index);
		} else if (propType.equals(Float.TYPE) || propType.equals(Float.class)) {
			return rs.getFloat(index);
		} else if (propType.equals(Double.TYPE) || propType.equals(Double.class)) {
			return rs.getDouble(index);
		} else if (propType.equals(BigDecimal.class)) {
			return rs.getBigDecimal(index);
		} else if (propType.equals(Boolean.TYPE) || propType.equals(Boolean.class)) {
			return rs.getBoolean(index);
		} else if (propType.equals(String.class)) {
			return rs.getString(index);
		}  else if (propType.equals(Byte.TYPE) || propType.equals(Byte.class)) {
			return rs.getByte(index);
		} else if (propType.equals(Timestamp.class)) {
			return rs.getTimestamp(index);
		} else if (propType.equals(Date.class)) {
			return new Date(rs.getDate(index).getTime());
		} else {
			return rs.getObject(index);
		}
	}
	
	public static <T> T createBean(SqlRowSet rs, Class<T> type,
			PropertyDescriptor[] props, int[] columnToProperty)
			throws SQLException {

		T bean = newInstance(type);

		for (int i = 1; i < columnToProperty.length; i++) {
			if (columnToProperty[i] == PROPERTY_NOT_FOUND) {
				continue;
			}

			PropertyDescriptor prop = props[columnToProperty[i]];
			Class<?> propType = prop.getPropertyType();
			
			/*
			 * 根据列的值
			 */
			Object value = processColumn(rs, i, propType);

			if (value == null && propType.isPrimitive()) {
				value = primitiveDefaults.get(propType);
			}
			callSetter(bean, prop, value);
		}

		return bean;
	}

	/* 调用Setter方法 */
	private static void callSetter(Object target, PropertyDescriptor prop, Object value)
			throws SQLException {

		Method setter = prop.getWriteMethod();

		if (setter == null) {
			return;
		}

		Class<?>[] params = setter.getParameterTypes();
		try {
			if (value instanceof java.util.Date) {
				final String dataType = params[0].getName();
				switch (dataType) {
					case "java.sql.Date":
						value = new java.sql.Date(
								((Date) value).getTime());
						break;
					case "java.sql.Time":
						value = new java.sql.Time(
								((Date) value).getTime());
						break;
					case "java.sql.Timestamp":
						value = new Timestamp(
								((Date) value).getTime());
						break;
				}
			}
			
			if (isCompatibleType(value, params[0])) {
				setter.invoke(target, value);
			} else {
				throw new SQLException("Cannot set " + prop.getName()
						+ ": incompatible types, cannot convert "
						+ value.getClass().getName() + " to "
						+ params[0].getName());
			}

		} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
			throw new SQLException("Cannot set " + prop.getName() + ": "
					+ e.getMessage());

		}
	}

	private static boolean isCompatibleType(Object value, Class<?> type) {
		if (value == null || type.isInstance(value)) {
			return true;
		} else if (type.equals(Integer.TYPE) && value instanceof Integer) {
			return true;

		} else if (type.equals(Long.TYPE) && value instanceof Long) {
			return true;

		} else if (type.equals(Double.TYPE) && value instanceof Double) {
			return true;

		} else if (type.equals(Float.TYPE) && value instanceof Float) {
			return true;

		} else if (type.equals(Short.TYPE) && value instanceof Short) {
			return true;

		} else if (type.equals(Byte.TYPE) && value instanceof Byte) {
			return true;

		} else if (type.equals(Character.TYPE)
				&& value instanceof Character) {
			return true;

		} else if (type.equals(Boolean.TYPE) && value instanceof Boolean) {
			return true;
		}else{
			return false;
		}

	}

	private static <T>  T newInstance(Class<T> c) throws SQLException {
		try {
			return c.getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e1) {
			// TODO Auto-generated catch block
			throw new SQLException("Cannot create " + c.getName() + ": "
					+ e1.getMessage());
		}
	}

}
