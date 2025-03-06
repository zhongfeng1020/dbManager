package cn.net.autocode.dbManager.handler;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 
 * &#064;Description:  Sql处理脚本
 * @author 陈忠峰
 * @version 1.00.00
 *
 */
public class SqlHandler{
	
	public static Object[][] preSqlValue(String sql,Object[] values){
		
		StringBuilder sqlBuffer = new StringBuilder(sql);

		int currentIndex = sqlBuffer.indexOf("?");
		
		List<Object> valueList = new ArrayList<>();

        for (Object value : values) {
            int valueLength = 1;
            if (value == null) {
                valueList.add(null);
            } else if (value.getClass().isArray()) {
                valueLength = Array.getLength(value);
                for (int j = 0; j < valueLength; j++) {
                    valueList.add(Array.get(value, j));
                }
            } else if (value instanceof Collection) {
                valueLength = ((Collection<?>) value).size();
                valueList.addAll((Collection<?>) value);
            } else {
                valueList.add(value);
            }

            String placeholder = "";
            for (int j = 0; j < valueLength - 1; j++) {
                placeholder = placeholder + "?,";
            }

            sqlBuffer.replace(currentIndex, currentIndex, placeholder);
            currentIndex = sqlBuffer.indexOf("?", currentIndex + valueLength * 2 - 1);
        }
		
		Object[][] rv = new Object[2][];
		rv[0] = new Object[]{sqlBuffer.toString()};
		rv[1] = valueList.toArray();
		return rv;
	}
	
}
