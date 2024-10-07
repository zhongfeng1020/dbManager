
package com.autocode.dbManager.handler;

/**
 * @author 陈忠峰
 *
 */
public class PreparedStatementHandler {
	
	public static String classToTable(String className) {
		String tableName = className.replaceAll("([a-z])([A-Z])", "$1_$2");
		return tableName.toLowerCase();
	}
	
	public static String columnToProperty(String columnName){
		String column = columnName.toLowerCase();
        String[] name = column.split("_");
        String propertyName = name[0];
        for (int i = 1; i < name.length; i++) {
        	propertyName = propertyName + name[i].substring(0, 1).toUpperCase() + name[i].substring(1, name[i].length());
        }
        return propertyName;
	}

	public static String propertyToColumn(String filedName){
		String columnName = filedName.replaceAll("([a-z])([A-Z])", "$1_$2");
		return columnName.toLowerCase();
	}

}
