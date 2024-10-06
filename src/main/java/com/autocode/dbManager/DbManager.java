
package com.autocode.dbManager;


import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.Digester;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.autocode.dbManager.handler.*;
import com.autocode.dbManager.tools.PageTools;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.AbstractLobCreatingPreparedStatementCallback;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;
import org.springframework.util.FileCopyUtils;
import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

/**
 * 
 * 访问数据库的工具支持oracle mysql sqlserver
 * 
 * @author 陈忠峰
 */
public class DbManager implements Dao {
	private final JdbcTemplate jdbcTemplate;

	private String DRIVER_NAME = null;

	private String SCHEMA;


	public DbManager(DataSource ds) {
		jdbcTemplate = new JdbcTemplate(ds);
	}


	/**
	 * 查询单个值
	 * @return 返回单个值
	 */
	@SuppressWarnings("unchecked")
	public <T> T queryValue(String sql, Class<T> clazz, Object... args) {
		if (args == null) {
			args = new Object[] {};
		}
		Object[][] rv = SqlHandler.preSqlValue(sql, args);
		String preSql = (String) rv[0][0];
		List<T> list = jdbcTemplate.queryForList(preSql, clazz, rv[1]);
		Object value = null;
		if (list.size() > 1) {
			throw new RuntimeException(preSql + "返回的记录数>1");
		} else if (list.size() == 1) {
			value = list.get(0);
		}
		if (value == null) {
			if (clazz.equals(Integer.TYPE) || clazz.equals(Integer.class)) {
				value = 0;
			} else if (clazz.equals(Short.TYPE) || clazz.equals(Short.class)) {
				value = 0;
			} else if (clazz.equals(Long.TYPE) || clazz.equals(Long.class)) {
				value = 0L;
			} else if (clazz.equals(Float.TYPE) || clazz.equals(Float.class)) {
				value = 0.0f;
			} else if (clazz.equals(Double.TYPE) || clazz.equals(Double.class)) {
				value = 0.0;
			} else if (clazz.equals(Boolean.TYPE) || clazz.equals(Boolean.class)) {
				value = false;
			} else if (clazz.equals(Byte.TYPE) || clazz.equals(Byte.class)) {
				value = 0;
			}
		}
		return (T) value;
	}

	@Override
	public <T> List<T> queryListValue(String sql, Class<T> clazz, Object... args) {
		// TODO Auto-generated method stub
		if (args == null) {
			args = new Object[] {};
		}

		Object[][] rv = SqlHandler.preSqlValue(sql, args);
		String preSql = (String) rv[0][0];
		return jdbcTemplate.queryForList(preSql, clazz, rv[1]);
	}

	@Override
	public void pageInfo(JSONObject page, String sql, Object... args) {
		// TODO Auto-generated method stub
		Object[][] rv = SqlHandler.preSqlValue(sql, args);
		String preSql = (String) rv[0][0];
		Integer totalNum = jdbcTemplate.queryForObject(preSql, Integer.class, rv[1]);
		page.put("total", totalNum);
		PageTools.compute(page);
	}

	/**
	 * 查询单条记录
	 */
	public Map<String, Object> queryMap(String sql, Object... args) {
		if (args == null) {
			args = new Object[] {};
		}

		Object[][] rv = SqlHandler.preSqlValue(sql, args);
		String preSql = (String) rv[0][0];

		SqlRowSet rs = jdbcTemplate.queryForRowSet(preSql, rv[1]);
		Map<String,Object> map;

		if (rs.getRow() > 1) {
			throw new RuntimeException(sql + "返回的记录数>1");
		}

		try {
			map = MapHandler.handle(rs);
		} catch (JSONException | SQLException e) {
			throw new RuntimeException(e.getMessage());
		}

		return map;
	}

	/**
	 * 查询多条记录
	 */
	public List<Map<String, Object>> queryListMap(String sql, Object... args) {
		if (args == null) {
			args = new Object[] {};
		}

		Object[][] rv = SqlHandler.preSqlValue(sql, args);
		String preSql = (String) rv[0][0];
		Object[] params = rv[1];
		SqlRowSet rs = jdbcTemplate.queryForRowSet(preSql, params);
		List<Map<String,Object>> array;
		try {
			array = MapHandler.handleList(rs);
		} catch (JSONException | SQLException e) {
			throw new RuntimeException(e.getMessage());
		}

		return array;
	}

	@Override
	public List<Map<String, Object>> queryListMapByPage(String sql, JSONObject page, Object... args) {
		// TODO Auto-generated method stub
		return this.queryListMap(this.pageSql(sql, page), args);
	}


	@Override
	public Map<String, Object> queryMapVT(String sql, Object... args) {
		// TODO Auto-generated method stub
		if (args == null) {
			args = new Object[] {};
		}
		Object[][] rv = SqlHandler.preSqlValue(sql, args);
		String preSql = (String) rv[0][0];
		SqlRowSet rs = jdbcTemplate.queryForRowSet(preSql, rv[1]);

		Map<String, Object> map = new LinkedHashMap<>();
		try {
			KeyValueHandler.handle(rs, map);
		} catch (SQLException e) {
			throw new RuntimeException(e.getMessage());
		}

		return map;
	}
	@Override
	public JSONObject queryJsonVT(String sql, Object... args) {
		// TODO Auto-generated method stub
		if (args == null) {
			args = new Object[] {};
		}
		Object[][] rv = SqlHandler.preSqlValue(sql, args);
		String preSql = (String) rv[0][0];
		SqlRowSet rs = jdbcTemplate.queryForRowSet(preSql, rv[1]);

		JSONObject json = new JSONObject();
		try {
			KeyValueHandler.handle2(rs, json);
		} catch (SQLException e) {
			throw new RuntimeException(e.getMessage());
		}
		return json;
	}

	@Override
	public JSONObject queryJSON(String sql, Object... args) {
		// TODO Auto-generated method stub
		if (args == null) {
			args = new Object[] {};
		}
		Object[][] rv = SqlHandler.preSqlValue(sql, args);
		String preSql = (String) rv[0][0];
		Object[] params = rv[1];
		SqlRowSet rs = jdbcTemplate.queryForRowSet(preSql, params);

		JSONObject json;
		rs.last();
		if (rs.getRow() > 1) {
			throw new RuntimeException(sql + "返回的记录数>1");
		}else{
			rs.beforeFirst();
		}

		try {
			json = JsonHandler.handle(rs);
		} catch (JSONException | SQLException e) {
			throw new RuntimeException(e.getMessage());
		}

		return json;
	}

	@Override
	public JSONArray queryJSONArray(String sql, Object... args) {
		// TODO Auto-generated method stub
		if (args == null) {
			args = new Object[] {};
		}

		Object[][] rv = SqlHandler.preSqlValue(sql, args);
		String preSql = (String) rv[0][0];
		Object[] params = rv[1];
		
		SqlRowSet rs = jdbcTemplate.queryForRowSet(preSql, params);
		JSONArray array;
		try {
			array = JsonHandler.handleList(rs);
		} catch (JSONException | SQLException e) {
			throw new RuntimeException(e.getMessage());
		}
		return array;
	}

	
	@Override
	public JSONArray queryJSONArrayByPage(String sql, JSONObject page, Object... args) {
		// TODO Auto-generated method stub
		String newSql = this.pageSql(sql, page);
		return this.queryJSONArray(newSql, args);
	}

	/**
	 * 查询单条记录
	 */
	public <T> T queryObject(String sql, Class<T> clazz, Object... args) {

		if (args == null) {
			args = new Object[] {};
		}

		Object[][] rv = SqlHandler.preSqlValue(sql, args);
		String preSql = (String) rv[0][0];
		Object[] params = rv[1];
		SqlRowSet rs = jdbcTemplate.queryForRowSet(preSql, params);
		T bean;
		rs.last();
		if (rs.getRow() > 1) {
			throw new RuntimeException(sql + "返回的记录数>1");
		}else{
			rs.beforeFirst();
		}

		try {
			bean = BeanHandler.handle(rs, clazz);
		} catch (SQLException e) {
			throw new RuntimeException(e.getMessage());
		}

		return bean;
	}

	/**
	 * 查询多条记录
	 */
	public <T> List<T> queryListObject(String sql, Class<T> clazz, Object... args) {
		if (args == null) {
			args = new Object[] {};
		}

		Object[][] rv = SqlHandler.preSqlValue(sql, args);
		String preSql = (String) rv[0][0];
		Object[] params = rv[1];
		SqlRowSet rs = jdbcTemplate.queryForRowSet(preSql, params);
		List<T> list;
		try {
			list = BeanHandler.handleList(rs, clazz);
		} catch (SQLException e) {
			throw new RuntimeException(e.getMessage());
		}

		return list;
	}


	@Override
	public int createJSON(JSONObject json, String tableName) {
		// TODO Auto-generated method stub

		// 1、获取表名及列名
		String[] columns = this.obtainColumns(tableName);

		// 2、获取insert sql
		Object[][] cv;
		try {
			cv = JsonHandler.columnValue(columns, json, Collections.singletonList("id"));
		} catch (Exception e1) {
			e1.printStackTrace();
			throw new RuntimeException(e1.getMessage());
		}

		String insertSql = BeanHandler.insertSql(cv[0], tableName);

		// 3、插入操作
		return jdbcTemplate.update(insertSql, cv[1]);
	}

	@Override
	public int[] createJSONArray(JSONArray jsonArray, String tableName) {
		// 1、获取表名及列名
		String[] columns = this.obtainColumns(tableName);


		// 2、获取insert sql
		Map<String,Object> cv;
		try {
			cv = JsonHandler.batchInsertValue(columns,jsonArray);
		} catch (Exception e1) {
			e1.printStackTrace();
			throw new RuntimeException(e1.getMessage());
		}

		Object[] cv0= (Object[]) cv.get("columnArray");
		String insertSql = BeanHandler.insertSql(cv0, tableName);
		List<Object[]> cv1 = (List<Object[]>) cv.get("valueList");

		// 3、插入操作
		return jdbcTemplate.batchUpdate(insertSql,cv1);

	}

	@Override
	public int updateJSON(JSONObject json, String tableName, String... primaryKey) {
		// TODO Auto-generated method stub
		if (primaryKey == null || primaryKey.length == 0) {
			primaryKey = new String[] { "id" };
		}
		String[] columns = this.obtainColumns(tableName);
		Object[][] cv;
		List<String> idArray = new ArrayList<>();
		for (String pk : primaryKey) {
			idArray.add(pk.toLowerCase());
		}

		try {
			cv = JsonHandler.columnValue(columns, json, idArray);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}

		// 2、获取update sql
		String updateSql = BeanHandler.updateSql(cv[0], tableName, idArray);

		// 3、更新操作
		return jdbcTemplate.update(updateSql, cv[1]);
	}


	@Override
	public int uniqueCheck(String tableName,JSONObject data) {
		//1、获取列名
		String[] columns = this.obtainColumns(tableName);

		// 2、将json数据处理
		JSONObject nj = new JSONObject();
		Set<String> keys = data.keySet();
		for (String key : keys) {
			if (data.get(key) != null && !"".equals(data.getString(key).trim())) {
				nj.put(key.replace("_","").toUpperCase(), data.get(key));
			}
		}

		SqlWrapperBuilder swb = new SqlWrapperBuilder(tableName);
		for(String column:columns){
			String proptyName = column.replace("_","").toUpperCase();
			if(nj.containsKey(proptyName)){
				if("ID".equals(column)){
					swb.ne(column,nj.get(proptyName));
				}else{
					swb.eq(column,nj.get(proptyName));
				}
			}
		}

		SqlWrapper sw = swb.countSqlWrapper();
		return this.queryValue(sw.getSql(),Integer.class,sw.getParams().toArray());
	}

	/**
	 * insert update delete操作
	 *
	 */
	public int executeUpdate(String sql, Object... params) {
		if (params == null) {
			params = new Object[] {};
		}
		Object[][] sv = SqlHandler.preSqlValue(sql, params);
		String dealSql = (String) sv[0][0];
		return jdbcTemplate.update(dealSql, sv[1]);
	}

	@Override
	public int[] batchUpdate(String sql, List<Object[]> params) {
		return jdbcTemplate.batchUpdate(sql,params);
	}

	@Override
	public int maxSort(String sql, Object... args) {
		// TODO Auto-generated method stub
		List<Integer> list = jdbcTemplate.queryForList(sql, Integer.class, args);
		if (list.size() == 0) {
			return 0;
		} else {
			if (list.get(0) == null) {
				return 0;
			} else {
				return list.get(0) + 1;
			}

		}
	}

	/**
	 * 获取表对应的列
	 *
	 */
	protected String[] obtainColumns(String tableName) {
		String selectSql = "select * FROM " + tableName + " where 1=0";
		SqlRowSet rs = jdbcTemplate.queryForRowSet(selectSql);
		String[] columns;
		SqlRowSetMetaData rsmd = rs.getMetaData();
		int cols = rsmd.getColumnCount();
		columns = new String[cols];
		for (int col = 1; col <= cols; col++) {
			columns[col - 1] = rsmd.getColumnName(col).toUpperCase();
		}
		return columns;
	}


	public String saveFile(byte[] bytes, String fileName, String fileType) {
		//计算文件摘要
		Digester md5 = new Digester(DigestAlgorithm.MD5);
		String digester = md5.digestHex(bytes);

		//文件存储
		String storageId;
		List<String> storageIds = jdbcTemplate.queryForList("select file_image_id from db_file_storage where file_type=? and digester=?", String.class,fileType,digester);
		if(!storageIds.isEmpty()){
			storageId =storageIds.get(0);
		}else{
			String sql = "insert into db_file_storage(FILE_IMAGE_ID,FILE_NAME,digester,file_type,FILE_IMAGE) values(?,?,?,?,?) ";
			final LobHandler lobHandler = new DefaultLobHandler();
			final Map<String, String> imageId = new HashMap<>();
			jdbcTemplate.execute(sql, new AbstractLobCreatingPreparedStatementCallback(lobHandler) {
				@Override
				protected void setValues(PreparedStatement ps, LobCreator lobCreator)
						throws SQLException, DataAccessException {
					// TODO Auto-generated method stub
					String id = IdWorker.uuid();
					imageId.put("id", id);
					ps.setString(1, id);
					ps.setString(2, fileName);
					ps.setString(3, digester);
					ps.setString(4, fileType);
					lobCreator.setBlobAsBytes(ps, 5, bytes);
				}
			});
			if (imageId.isEmpty()) {
				throw new RuntimeException("文件存储失败！");
			} else {
				storageId =  imageId.get("id");
			}
		}

		//存储附件信息
		String fileId = IdWorker.ulid();
		Date curDate = new Date();

		BigDecimal primitiveSize = new BigDecimal(bytes.length);
		BigDecimal divisor = new BigDecimal(1024);
		BigDecimal fileSize = primitiveSize.divide(divisor, 2, RoundingMode.HALF_UP);

		jdbcTemplate.update("insert into db_file(id,file_name,file_type,upload_time,create_time,storage_id,file_size) values(?,?,?,?,?,?)", fileId,fileName,fileType,curDate,curDate,storageId,fileSize);
		return fileId;
	}

	@Override
	public int updateFile(byte[] bytes, String fileName, String fileType, String fileId) {
		String oldStorageId = jdbcTemplate.queryForObject("select storage_id from db_file where id=?", String.class, fileId);
		JSONObject fj = new JSONObject();

		Digester md5 = new Digester(DigestAlgorithm.MD5);
		String digester = md5.digestHex(bytes);

		//存储文件
		String storageId = null;
		List<String> storageIds = jdbcTemplate.queryForList("select file_image_id from db_file_storage where file_type=? and digester=?", String.class,fileType,digester);
		if(!storageIds.isEmpty()){
			storageId = storageIds.get(0);
		}else{
			String sql = "insert into db_file_storage(FILE_IMAGE_ID,FILE_NAME,digester,file_type,FILE_IMAGE) values(?,?,?,?,?) ";
			final LobHandler lobHandler = new DefaultLobHandler();
			final Map<String, String> imageId = new HashMap<>();
			jdbcTemplate.execute(sql, new AbstractLobCreatingPreparedStatementCallback(lobHandler) {
				@Override
				protected void setValues(PreparedStatement ps, LobCreator lobCreator)
						throws SQLException, DataAccessException {
					// TODO Auto-generated method stub
					String id = IdWorker.uuid();
					imageId.put("id", id);
					ps.setString(1, id);
					ps.setString(2, fileName);
					ps.setString(3, digester);
					ps.setString(4, fileType);
					lobCreator.setBlobAsBytes(ps, 5, bytes);
				}
			});
			if (imageId.isEmpty()) {
				throw new RuntimeException("文件存储失败！");
			} else {
				storageId =  imageId.get("id");
			}
		}

		//存储附件信息
		BigDecimal primitiveSize = new BigDecimal(bytes.length);
		BigDecimal divisor = new BigDecimal(1024);
		BigDecimal fileSize = primitiveSize.divide(divisor, 2, RoundingMode.HALF_UP);
		int updateNum = jdbcTemplate.update("update db_file set upload_time=?,file_name=?,file_type=?,file_size=?,storage_id=? where id=?",new Date(),fileName,fileType,fileSize,storageId,fileId);

		if(StrUtil.isNotBlank(oldStorageId)){
			int num = jdbcTemplate.queryForObject("select count(*) from db_file where storage_id=?", Integer.class, oldStorageId);
			if(num==0){
				jdbcTemplate.update("delete from db_file_storage where file_image_id=?",oldStorageId);
			}
		}
		return updateNum;
	}


	public byte[] readFile(String fileId) {
		// TODO Auto-generated method stub
		String storageId = jdbcTemplate.queryForObject("select storage_id from db_file where id=?", String.class,fileId);
		String sql = "select FILE_IMAGE from db_file_storage where FILE_IMAGE_ID = ? ";
		return this.readBlob(sql,storageId);
	}

	@Override
	public int delFile(String fileId) {
		//String storageId = dao.queryValue("select storage_id from db_file where id=?", String.class, fileId);
		String storageId = jdbcTemplate.queryForObject("select storage_id from db_file where id=?",String.class,fileId);
		int delNum = jdbcTemplate.update("delete from db_file where id=?", fileId);

		if(StrUtil.isNotBlank(storageId)){
			int num = jdbcTemplate.queryForObject("select count(*) from db_file where storage_id=?", Integer.class, storageId);
			if(num==0){
				jdbcTemplate.update("delete from db_file_storage where file_image_id=?",storageId);
			}
		}

		return delNum;
	}


	private boolean isSQLSERVER() {
		return this.getDbType().equals("SQL_SERVER");
	}

	private boolean isMYSQL() {
		return this.getDbType().equals("MYSQL");
	}

	private boolean isORACLE() {
		return this.getDbType().equals("ORACLE");
	}

	private String pageSql(String sql, JSONObject page) {

		int currentPage = page.getIntValue("currentPage");
		if (currentPage == 0) {
			currentPage = 1;
		}

		int pageSize = page.getIntValue("pageSize");
		if (pageSize == 0) {
			pageSize = 20;
		}

		int startRows = (currentPage - 1) * pageSize;
		int endRows = currentPage * pageSize;

		String newSql = sql;
		if (this.isSQLSERVER()) {
			newSql = sql + " offset " + startRows + " rows fetch next " + pageSize + " rows only";
		} else if (this.isMYSQL()) {
			newSql = sql + " limit " + startRows + "," + pageSize;
		} else if (this.isORACLE()) {
			newSql = "select * from ( select czftt.*,ROWNUM as rowno from ( " + sql + " ) czftt where ROWNUM<="
					+ endRows + ") czftt1 where czftt1.rowno>" + startRows;
		}else{
			newSql = sql + " limit " + startRows + "," + pageSize;
		}

		return newSql;
	}

	@Override
	public JSONObject queryTableInfo(String tableName) {
		// TODO Auto-generated method stub
		JSONObject info= new JSONObject();
		Map<String,Object> columnCache = null;
		if (this.isSQLSERVER()) {
			//获取object_id
			String sql0 = "select so.id object_id,CAST(so.name as nvarchar(255)) as TABLE_NAME, cast(sep.value as nvarchar(500)) as REMARKS " +
					" from sysobjects so left JOIN sys.extended_properties sep on sep.major_id = so.id and sep.minor_id = 0 where (xtype = 'U' or xtype = 'v') " +
					" and  so.name=?";
			info = this.queryJSON(sql0,tableName);
			Integer objectId = info.getIntValue("objectId");

			//获取列的对照
			String sql = "select cast(b.object_id as nvarchar(255))+'@'+ cast(b.column_id as nvarchar(255)) value,a.name+'@'+b.name text from sys.tables a,sys.columns b " +
					" where a.object_id=b.object_id and a.object_id=? order by b.object_id,b.column_id";
			columnCache = this.queryMapVT(sql,objectId);

			//--获取列的信息、默认值、备注
			String sql1 = "select a.name name,a.column_id sort_no,b.name type,a.max_length length,a.precision,a.scale,a.is_nullable as isnullable,a.default_object_id,d.definition as defaultvalue,cast(e.value as nvarchar(500)) remarks " +
					"from  sys.columns a inner join sys.types b on a.user_type_id=b.user_type_id " +
					"left join sys.default_constraints  d on a.default_object_id=d.object_id " +
					"left join sys.extended_properties e on a.object_id=e.major_id and a.column_id=e.minor_id  " +
					"where a.object_id=? order by sort_no";
			JSONArray columns = this.queryJSONArray(sql1,objectId);
			for(int i=0;i<columns.size();i++){
				JSONObject column = columns.getJSONObject(i);
				if(column.getBooleanValue("isnullable")){
					column.put("isnullable","Y");
				}else{
					column.put("isnullable","N");
				}

				if(column.getString("type").equals("nvarchar")){
					column.put("length",column.getIntValue("length")/2);
				}
			}
			info.put("columns",columns);
			//获取主键信息 primary_key
			String sql2="select c.column_id " +
					" from sys.key_constraints a,SYS.indexes b,SYS.index_columns c  " +
					"where a.parent_object_id = b.object_id and a.name=b.name and a.type='PK' and b.object_id=c.object_id and b.index_id=c.index_id " +
					"and a.parent_object_id=?";
			List<Integer> pks = this.queryListValue(sql2,Integer.class,objectId);
			int len=columns.size();
			for(int i=0;i<len;i++){
				JSONObject jsonObject = columns.getJSONObject(i);
				if(pks.contains(jsonObject.getIntValue("sortNo"))){
					jsonObject.put("primaryKey","Y");
				}else{
					jsonObject.put("primaryKey","N");
				}
			}

			//获取子表信息
			String sql3="select a.parent_object_id fk_table_id,parent_column_id fk_column_id,referenced_column_id pk_column_id,a.constraint_column_id sort_no," +
					"(case b.delete_referential_action when 1 then 'Y' ELSE 'N' END) delete_rule " +
					"from sys.foreign_key_columns a,sys.foreign_keys b where a.constraint_object_id=b.object_id and a.referenced_object_id=? " +
					"order by a.constraint_column_id";
			JSONArray exportedKeys = this.queryJSONArray(sql3,objectId);

			int len3=exportedKeys.size();
			for(int i=0;i<len3;i++){
				JSONObject json = exportedKeys.getJSONObject(i);
				String fkTableId = json.getString("fkTableId");
				String fkColumnId = json.getString("fkColumnId");
				if(!columnCache.containsKey(fkTableId+"@"+fkColumnId)){
					Map<String,Object> vt = this.queryMapVT(sql,fkTableId);
					columnCache.putAll(vt);
				}
				String[] fks = columnCache.get(fkTableId+"@"+fkColumnId).toString().split("@");
				json.put("fkTable",fks[0]);
				json.put("fkField",fks[1]);
				json.put("isSystem","Y");
				String pkColumnId = json.getString("pkColumnId");
				json.put("pkField",columnCache.get(objectId+"@"+pkColumnId).toString().split("@")[1]);
			}
			info.put("exportedKeys",exportedKeys);

			//获取索引信息
			String sql4 = "select a.name,(case a.is_unique when 1 then 'Y' else 'N' end) non_unique,b.column_id " +
					"FROM SYS.indexes a,SYS.index_columns b WHERE a.object_id=b.object_id and a.index_id=b.index_id and a.OBJECT_ID=? " +
					"order by a.index_id,b.key_ordinal";
			JSONArray indexInfos = this.queryJSONArray(sql4,objectId);
			int len4=indexInfos.size();
			for(int i=0;i<len4;i++){
				JSONObject json = indexInfos.getJSONObject(i);
				String pkColumnId = json.getString("columnId");
				json.put("fieldName",columnCache.get(objectId+"@"+pkColumnId).toString().split("@")[1]);
			}
			info.put("indexInfos",indexInfos);

		} else if (this.isMYSQL()) {
			String sql0 = "select TABLE_NAME,TABLE_COMMENT as remarks " +
					"from INFORMATION_SCHEMA.Tables where table_schema=? and table_name=?";
			info = this.queryJSON(sql0,this.SCHEMA,tableName);

			//--获取列的信息、默认值、备注、主键信息
			String sql1 = "select column_name name,ordinal_position sort_no,data_type type," +
					" ifnull(character_maximum_length,0)+ifnull(NUMERIC_PRECISION,0)+ifnull(datetime_precision,0) length," +
					" ifnull(numeric_scale,0) scale,case is_nullable when 'YES' then 'Y' else 'N' end isnullable,column_default defaultvalue,column_comment remarks," +
					" case column_key when 'PRI' then 'Y' else 'N' END primary_Key " +
					" from information_schema.columns where table_schema=? and table_name=?";
			JSONArray columns = this.queryJSONArray(sql1,this.SCHEMA,tableName);
			info.put("columns",columns);
			//获取子表信息
			String sql3="select table_name fk_table,column_name fk_Field,'Y' is_system,referenced_column_name pk_Field,ordinal_position sort_no,'Y' delete_rule \n" +
					"from information_schema.KEY_COLUMN_USAGE where table_schema=? AND referenced_table_name=? " +
					"order by sort_no";
			JSONArray exportedKeys = this.queryJSONArray(sql3,this.SCHEMA,tableName);
			info.put("exportedKeys",exportedKeys);

			//获取索引信息
			String sql4 = "select index_name name,(case non_unique when 0 then 'Y' else 'N' end) non_unique,column_name field_name,seq_in_index " +
					"from information_schema.statistics where table_schema=? and table_name=? and index_name<>'PRIMARY' order by name,seq_in_index";
			JSONArray indexInfos = this.queryJSONArray(sql4,this.SCHEMA,tableName);
			info.put("indexInfos",indexInfos);

		} else if (this.isORACLE()) {
			
		}else{
			String sql0 = "select TABLE_NAME,TABLE_COMMENT as remarks " +
					"from INFORMATION_SCHEMA.Tables where table_schema=? and table_name=?";
			info = this.queryJSON(sql0,this.SCHEMA,tableName);

			//--获取列的信息、默认值、备注、主键信息
			String sql1 = "select column_name name,ordinal_position sort_no,data_type type," +
					" ifnull(character_maximum_length,0)+ifnull(NUMERIC_PRECISION,0)+ifnull(datetime_precision,0) length," +
					" ifnull(numeric_scale,0) scale,case is_nullable when 'YES' then 'Y' else 'N' end isnullable,column_default defaultvalue,column_comment remarks," +
					" case column_key when 'PRI' then 'Y' else 'N' END primary_Key " +
					" from information_schema.columns where table_schema=? and table_name=?";
			JSONArray columns = this.queryJSONArray(sql1,this.SCHEMA,tableName);
			info.put("columns",columns);
			//获取子表信息
			String sql3="select table_name fk_table,column_name fk_Field,'Y' is_system,referenced_column_name pk_Field,ordinal_position sort_no,'Y' delete_rule \n" +
					"from information_schema.KEY_COLUMN_USAGE where table_schema=? AND referenced_table_name=? " +
					"order by sort_no";
			JSONArray exportedKeys = this.queryJSONArray(sql3,this.SCHEMA,tableName);
			info.put("exportedKeys",exportedKeys);

			//获取索引信息
			String sql4 = "select index_name name,(case non_unique when 0 then 'Y' else 'N' end) non_unique,column_name field_name,seq_in_index " +
					"from information_schema.statistics where table_schema=? and table_name=? and index_name<>'PRIMARY' order by name,seq_in_index";
			JSONArray indexInfos = this.queryJSONArray(sql4,this.SCHEMA,tableName);
			info.put("indexInfos",indexInfos);
		}
		return info;
	}

	@Override
	public Map<String, String> queryColumnMap(String tableName) {
		Map<String,String> columnMap = new HashMap<>();
		String selectSql = "select * FROM " + tableName + " where 1=0";
		SqlRowSet rs = jdbcTemplate.queryForRowSet(selectSql);
		SqlRowSetMetaData rsmd = rs.getMetaData();
		int cols = rsmd.getColumnCount();
		for (int col = 1; col <= cols; col++) {
			String columnName = rsmd.getColumnName(col).toLowerCase();
			String filedName = PreparedStatementHandler.columnToProperty(rsmd.getColumnName(col).toLowerCase());
			columnMap.put(filedName,columnName);
		}
		return columnMap;
	}

	@Override
	public boolean checkTable(String tableName) {
		if(this.isMYSQL()){
			String sql = "select TABLE_NAME from information_schema.TABLES where TABLE_NAME = ? AND TABLE_SCHEMA=?";
			List<Map<String, Object>> list = jdbcTemplate.queryForList(sql,tableName,this.SCHEMA);
			return list.size()>0;
		}else if(this.isORACLE()){
			String sql = "select table_name from user_tables where table_name=?";
			List<Map<String, Object>> list = jdbcTemplate.queryForList(sql,tableName.toUpperCase());
			return list.size()>0;
		}else if(this.isSQLSERVER()){
			String sql = "select name from sysobjects where (xtype = 'U' or xtype = 'v') and (name =? or name=?)";
			List<Map<String, Object>> list = jdbcTemplate.queryForList(sql,tableName.toUpperCase(),tableName.toLowerCase());
			return list.size()>0;
		}else{
			String sql = "select TABLE_NAME from information_schema.TABLES where TABLE_NAME = ? AND TABLE_SCHEMA=?";
			List<Map<String, Object>> list = jdbcTemplate.queryForList(sql,tableName,this.SCHEMA);
			return list.size()>0;
		}
	}

	@Override
	public JSONArray getTables() {
		if(this.isMYSQL()){
			String sql = "select TABLE_NAME,TABLE_COMMENT as remarks " +
					"from INFORMATION_SCHEMA.Tables where table_schema=?";
			return this.queryJSONArray(sql,this.SCHEMA);

		}else if(this.isORACLE()){
			String sql = "select table_name from user_tables where table_name=?";

			return null;
		}else if(this.isSQLSERVER()){
			String sql = "select CAST(so.name as nvarchar(255)) as TABLE_NAME, cast(sep.value as nvarchar(500)) as REMARKS " +
					" from sys.tables so left JOIN sys.extended_properties sep on sep.major_id = so.object_id and sep.minor_id = 0";
			return this.queryJSONArray(sql);
		}else{
			String sql = "select TABLE_NAME,TABLE_COMMENT as remarks " +
					"from INFORMATION_SCHEMA.Tables where table_schema=?";
			return this.queryJSONArray(sql,this.SCHEMA);
		}
	}

	@Override
	public String getDbType() {
		if (this.DRIVER_NAME == null) {
			try {
				this.DRIVER_NAME = this.jdbcTemplate.getDataSource().getConnection().getMetaData().getDriverName();
				this.SCHEMA = this.jdbcTemplate.getDataSource().getConnection().getSchema();
				if(this.SCHEMA==null){
					this.SCHEMA = this.jdbcTemplate.getDataSource().getConnection().getCatalog();
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				throw new RuntimeException(e.getMessage());
			}
		}

		if(this.DRIVER_NAME.toUpperCase().contains("MYSQL")){
			return "MYSQL";
		}

		if(this.DRIVER_NAME.toUpperCase().contains("SQL SERVER")){
			return "SQL_SERVER";
		}
		if(this.DRIVER_NAME.toUpperCase().contains("ORACLE")){
			return "ORACLE";
		}
		return "";
	}

	@Override
	public void execute(String sql) {
		jdbcTemplate.execute(sql);
	}

	@Override
	public void updateBlob(byte[] bytes, String sql, String id) {
		// TODO Auto-generated method stub

		final LobHandler lobHandler = new DefaultLobHandler();
		final Map<String, String> imageId = new HashMap<>();
		jdbcTemplate.execute(sql, new AbstractLobCreatingPreparedStatementCallback(lobHandler) {
			@Override
			protected void setValues(PreparedStatement ps, LobCreator lobCreator)
					throws SQLException, DataAccessException {
				// TODO Auto-generated method stub
				lobCreator.setBlobAsBytes(ps, 1, bytes);
				ps.setString(2, id);
			}
		});
	}

	@Override
	public byte[] readBlob(String sql, String id) {
		// TODO Auto-generated method stub
		List<byte[]> query = jdbcTemplate.query(sql, (rs, rowNum) -> {
            LobHandler lobHandler = new DefaultLobHandler(); // reusable object
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                FileCopyUtils.copy(lobHandler.getBlobAsBinaryStream(rs, 1), out);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return out.toByteArray();
        },id);
		return query.get(0);
	}

}
