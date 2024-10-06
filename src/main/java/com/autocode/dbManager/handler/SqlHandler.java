package com.autocode.dbManager.handler;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 
 * @Description: 
 * @author 陈忠峰
 * @version 1.00.00
 * @history:
 *
 */
public class SqlHandler{
	
	@SuppressWarnings("unchecked")
	public static Object[][] preSqlValue(String sql,Object[] values){
		
		StringBuilder sqlBuffer = new StringBuilder(sql);

		int currentIndex = sqlBuffer.indexOf("?");
		
		List<Object> valueList = new ArrayList<Object>();
		
		for(int i=0;i<values.length;i++){
			int valueLength = 1;
			if(values[i]==null){
				valueList.add(values[i]);
			}else if(values[i].getClass().isArray()){
				valueLength = Array.getLength(values[i]);
				for(int j=0;j<valueLength;j++){
					valueList.add(Array.get(values[i], j));
				}
			}else if(values[i] instanceof Collection){
				valueLength = ((Collection<?>)values[i]).size();
				valueList.addAll((Collection<? extends Object>)values[i]);
			}else{
				valueList.add(values[i]);
			}
			
			String placeholder = "";
			for(int j=0;j<valueLength-1;j++){
				placeholder = placeholder+"?,";
			}
			
			sqlBuffer.replace(currentIndex, currentIndex,placeholder);
			currentIndex = sqlBuffer.indexOf("?", currentIndex+valueLength*2-1);
		}
		
		Object[][] rv = new Object[2][];
		rv[0] = new Object[]{sqlBuffer.toString()};
		rv[1] = valueList.toArray();
		return rv;
	}
	
}
