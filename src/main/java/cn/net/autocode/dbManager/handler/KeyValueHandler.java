package cn.net.autocode.dbManager.handler;

import java.sql.SQLException;
import java.util.Map;

import com.alibaba.fastjson2.JSONObject;
import org.springframework.jdbc.support.rowset.SqlRowSet;


/**
 * 
 * &#064;Description:  将包含value、text列的查询结果集转化为Map对象，value为对应Map的key值，text对应Map的value值
 * @author 陈忠峰
 * @version 1.00.00
 *
 */
public class KeyValueHandler{
	
	public static void handle(SqlRowSet rs,Map<String,Object> map) throws SQLException{
		while(rs.next()){
			String key = rs.getString("value");
			Object value = rs.getObject("text");
			map.put(key, value);
		}
	}
	
	public static void handle2(SqlRowSet rs, JSONObject json) throws SQLException{
		while(rs.next()){
			String key = rs.getString("value");
			Object value = rs.getObject("text");
			json.put(key, value);
		}
	}
}
