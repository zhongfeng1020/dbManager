/**
 * 执行数据库操作
 */
package com.autocode.dbManager;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.util.List;
import java.util.Map;


/**
 * @author 陈忠峰
 *
 */
public interface Dao {
	/**
	 * 查询单个值
	 * @param sql  查询列中只能有一个字段，且查询结果集小于等于1
	 * @param clazz 返回的值类型，只支持java基本类型
	 * @param args  查询参数，当参数值为集合类型时，where条件中的对比方式使用 in (?)，系统根据集合的数量自动转换？的数量
	 * @return 查询结果，没有查询道返回null
	 * @param <T> 返回值类型
	 */
	<T> T queryValue(String sql,Class<T> clazz,Object... args);

	/**
	 * 查询单列值集合
	 * @param sql 查询列中只能有一个字段，
	 * @param clazz 返回的值类型，只支持java基本类型
	 * @param args 查询参数，当参数值为集合类型时，where条件中的对比方式使用 in (?)，系统根据集合的数量自动转换？的数量
	 * @return 查询结果，没有查询道list集合为空
	 * @param <T> 返回值类型
	 */
	<T> List<T> queryListValue(String sql,Class<T> clazz,Object... args);

	/**
	 * 将多行查询结果转化为键值对，查询的列名必须含value text列
	 * @param sql 查询sql
	 * @param args 查询参数，当参数值为集合类型时，where条件中的对比方式使用 in (?)，系统根据集合的数量自动转换？的数量
	 * @return 返回map类型，key对于value值，value对应text值
	 */
	Map<String,Object> queryMapVT(String sql, Object... args);

	/**
	 * 将多行查询结果转化为键值对，查询的列名必须含value text列
	 * @param sql 查询sql，查询结果集小于等于1
	 * @param args 查询参数，当参数值为集合类型时，where条件中的对比方式使用 in (?)，系统根据集合的数量自动转换？的数量
	 * @return 返回 JSONObject类型的键值对
	 */
	JSONObject queryJsonVT(String sql, Object... args);


	/**
	 * 获取分页信息
	 * currentPage：当前页，第几页
	 * pageSize：每页记录数
	 * total：总记录数
	 * pageCount：总页数
	 * @param page 分页参数，包含每页记录数，当前第几页，如果不传默认第一页，每页20条记录，该方法将更新page的属性信息
	 * @param sql 查询总记录数的sql
	 * @param args 查询参数，当参数值为集合类型时，where条件中的对比方式使用 in (?)，系统根据集合的数量自动转换？的数量
	 */
	void pageInfo(JSONObject page,String sql,Object...args);
	

	/**
	 * 查询单条记录
	 * @param sql 查询sql，查询结果集小于等于1
	 * @param args  查询参数，当参数值为集合类型时，where条件中的对比方式使用 in (?)，系统根据集合的数量自动转换？的数量
	 * @return map类型查询结果
	 */
	Map<String,Object> queryMap(String sql,Object... args);
	
	
	/**
	 * 查询多条记录
	 * @param sql 查询sql
	 * @param args  查询参数，当参数值为集合类型时，where条件中的对比方式使用 in (?)，系统根据集合的数量自动转换？的数量
	 * @return 查询结果
	 */
	List<Map<String,Object>> queryListMap(String sql, Object... args);


	/**
	 * 分页查询，先通过pageInfo方法获取page的值，然后再进行分页查询
	 * @param sql 查询sql
	 * @param page 分页信息
	 * @param args 查询参数，当参数值为集合类型时，where条件中的对比方式使用 in (?)，系统根据集合的数量自动转换？的数量
	 * @return 分页结果
	 */
	List<Map<String,Object>> queryListMapByPage(String sql,JSONObject page,Object... args);

	/**
	 * 查询单条记录，返回jsonobject类型
	 * @param sql 查询sql，查询结果集小于等于1
	 * @param args 查询参数，当参数值为集合类型时，where条件中的对比方式使用 in (?)，系统根据集合的数量自动转换？的数量
	 * @return 返回JSONObjec对象，如果查询不到，返回的对象为空
	 */
	JSONObject queryJSON(String sql, Object... args);
	

	/**
	 * 查询多条记录，返回JSONArray类型
	 * @param sql 查询sql，查询结果集小于等于1
	 * @param args 查询参数，当参数值为集合类型时，where条件中的对比方式使用 in (?)，系统根据集合的数量自动转换？的数量
	 * @return 返回JSONObjec对象，如果查询不到，返回的对象为空
	 */
	JSONArray queryJSONArray(String sql, Object... args);


	/**
	 * 分页查询，先通过pageInfo方法获取page的值，然后再进行分页查询
	 * @param sql 查询sql
	 * @param page 分页信息
	 * @param args 查询参数，当参数值为集合类型时，where条件中的对比方式使用 in (?)，系统根据集合的数量自动转换？的数量
	 * @return 分页结果
	 */
	JSONArray queryJSONArrayByPage(String sql,JSONObject page,Object... args);
	

	/**
	 * 执行insert操作，，json的key值与table的column转换成驼峰样式后的内容进行对照，如果一致则为对应的列值
	 * @param json table记录对应的json对象
	 * @param tableName table名称
	 * @return 成功返回1，否则为0
	 */
	int createJSON(JSONObject json,String tableName);

	/**
	 * 暂时不用
	 * @param jsonArray table对应的数据
	 * @param tableName table名称
	 * @return 返回执行结果
	 */
	int[] createJSONArray(JSONArray jsonArray,String tableName);

	/**
	 * 执行update操作，json的key值与table的column转换成驼峰样式后的内容进行对照，如果一致则为对应的列值
	 * @param json json table记录对应的json对象
	 * @param tableName table名称
	 * @param primaryKey 根据时的条件列，如果参数为空则默认根据id进行更新
	 * @return 返回执行结果
	 */
	int updateJSON(JSONObject json,String tableName,String... primaryKey);

	/**
	 * 唯一性性校验
	 * @param tableName table名称
	 * @param data table对应的数据
	 * @return 返回校验结果
	 */
	public int uniqueCheck(String tableName,JSONObject data);

	/**
	 * 执行原生sql的insert、update、delete操作
	 * @param sql 原生sql
	 * @param params 查询参数，当参数值为集合类型时，where条件中的对比方式使用 in (?)，系统根据集合的数量自动转换？的数量
	 * @return 返回执行结果
	 */
	int executeUpdate(String sql,Object... params);


	/**
	 * 批量执行执行原生sql的insert、update、delete操作
	 * @param sql 执行sql
	 * @param params 查询参数，当参数值为集合类型时，where条件中的对比方式使用 in (?)，系统根据集合的数量自动转换？的数量
	 * @return 返回执行机构
	 */
	int[] batchUpdate(String sql,List<Object[]> params);


	/**
	 * 将文件保存到db_file表中
	 * 1、db_file中只存储文件信息，实际存储表为db_file_storage
	 * 2、根据文件的MD5摘要，在db_file_storage表中做了去重处理
	 * @param bytes 文件信息
	 * @param fileName 文件名称
	 * @param fileType 文件类型
	 * @return db_file的id值
	 */
	String saveFile(byte[] bytes, String fileName, String fileType);

	/**
	 * 更新文件内容
	 * @param bytes 文件信息
	 * @param fileName 文件名称
	 * @param fileType 文件类型
	 * @param fileId 文件id
	 * @return 1：代表成功，0：代表失败
	 */
	int updateFile(byte[] bytes,String fileName, String fileType,String fileId);

	/**
	 * 读取文件
	 * @param fileId 文件ID
	 * @return 文件内容
	 */
	byte[] readFile(String fileId);


	/**
	 * 删除文件
	 * @param fileId
	 * @return
	 */
	public int delFile(String fileId);

	/**
	 * 获取最大序号值
	 * @param sql 查询sql
	 * @param args 参数信息
	 * @return 返回int类型的最大序号信息
	 */
	int maxSort(String sql,Object... args);
	

	/**
	 * 查询表的信息，暂时不用
	 * @param tableName table名称
	 * @return 返回table信息
	 */
	JSONObject queryTableInfo(String tableName);

	/**
	 * 查询表的字段信息，暂时不用
	 * @param tableName table名称
	 * @return 返回table的列信息
	 */
	Map<String,String> queryColumnMap(String tableName);

	/**
	 * 检查表是否存在，暂时不用
	 * @param tableName table名称
	 * @return 返回结果
	 */
	boolean checkTable(String tableName);

	/**
	 * 获取数据的所有表，暂时不用
	 * @return 返回table信息
	 */
	JSONArray getTables();

	/**
	 * 暂时不用
	 * @return 数据库类型
	 */
	String getDbType();

	/**
	 * 暂时不用
	 * @param sql 执行sql
	 */
	void execute(String sql);

	void updateBlob(byte[] bytes,String sql,String id);

	byte[] readBlob(String sql,String id);
}
