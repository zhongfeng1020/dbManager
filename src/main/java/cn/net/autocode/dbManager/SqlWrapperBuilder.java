package cn.net.autocode.dbManager;

import com.alibaba.fastjson2.JSONArray;
import org.springframework.lang.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class SqlWrapperBuilder {
	
	private String where = "";
	
	private String orderBy;

	private String groupBy;
	
	private String selectColumn;
	
	private String updateColumn;
	
	private final String tableName;
	
	private List<Object> updateParams = new ArrayList<Object>();
	
	private List<Object> params = new ArrayList<Object>();

	
	public SqlWrapperBuilder(String tableName) {
		this.tableName = tableName;
	}
	
	public SqlWrapperBuilder selectColumn(String selectColumn) {
		this.selectColumn = selectColumn;
		return this;
	}
	
	public SqlWrapperBuilder updateColumn(String updateColumn,@Nullable Object... value) {
		if(this.updateColumn==null||this.updateColumn.equals("")) {
			this.updateColumn = updateColumn;
		}else {
			this.updateColumn = this.updateColumn+","+updateColumn;
		}
		if(value!=null&&value.length>0) {
			for(Object v:value) {
				if(v==null){
					this.updateParams.add(v);
				}else if(v instanceof JSONArray){
					this.updateParams.add(((JSONArray) v).toJSONString());
				}else if(v.getClass().isArray()){
					this.updateParams.add(Arrays.toString((Object[]) v));
				}else if(v instanceof Collection){
					this.updateParams.add(JSONArray.copyOf((Collection) v).toJSONString());
				}else{
					this.updateParams.add(v);
				}
			}
		}else{
			this.updateParams.add(null);
		}
		return this;
	}
	
	public SqlWrapperBuilder eq(String column,Object value) {
		if(value!=null&&!value.equals("")) {
			this.where = this.where + " and "+column+" = ?";
			params.add(value);
		}
		
		return this;
	}

	
	
	public SqlWrapperBuilder eqNull(String column,Object value) {
		if(value==null||value.equals("")) {
			this.where = this.where + " and "+column+" is null";
		}else {
			this.where = this.where + " and "+column+" = ?";
			params.add(value);
		}
		return this;
	}
	

	
	public SqlWrapperBuilder ne(String column,Object value) {
		if(value!=null&&!value.equals("")) {
			this.where = this.where + " and "+column+" <> ?";
			params.add(value);
		}
		return this;
	}

	public SqlWrapperBuilder neNull(String column,Object value) {		
		if(value==null||value.equals("")) {
			this.where = this.where + " and "+column+" is not null";
		}else {
			this.where = this.where + " and "+column+" <> ?";
			params.add(value);
		}
		
		return this;
	}
	
	public SqlWrapperBuilder like(String column,Object value) {
		if(value!=null&&!value.equals("")) {
			this.where = this.where + " and "+column+" like ?";
			params.add("%"+value+"%");
		}
		return this;
	}
	
	public SqlWrapperBuilder notLike(String column,Object value) {
		if(value!=null&&!value.equals("")) {
			this.where = this.where + " and "+column+" not like ?";
			params.add("%"+value+"%");
		}
		return this;
	}
	
	public SqlWrapperBuilder gt(String column,Object value) {
		if(value!=null&&!value.equals("")) {
			this.where = this.where + " and "+column+" > ?";
			params.add(value);
		}
		
		return this;
	}
	
	public SqlWrapperBuilder ge(String column,Object value) {
		if(value!=null&&!value.equals("")) {
			this.where = this.where + " and "+column+" >= ?";
			params.add(value);
		}
		
		return this;
	}
	
	
	public SqlWrapperBuilder lt(String column,Object value) {
		if(value!=null&&!value.equals("")) {
			this.where = this.where + " and "+column+" < ?";
			params.add(value);
		}
		
		return this;
	}
	
	public SqlWrapperBuilder le(String column,Object value) {
		if(value!=null&&!value.equals("")) {
			this.where = this.where + " and "+column+" <= ?";
			params.add(value);
		}
		
		return this;
	}
	
	public SqlWrapperBuilder in(String column,List<?> value) {

		if(value!=null && !value.isEmpty()) {
			this.where = this.where + " and "+column+" in (?) ";
			params.add(value);
		}
		return this;
	}
	
	public SqlWrapperBuilder notIn(String column,List<?> value) {
		if(value!=null && !value.isEmpty()) {
			this.where = this.where + " and "+column+" not in (?) ";
			params.add(value);
		}
		return this;
	}
	
	public SqlWrapperBuilder where(String whereStr,@Nullable Object... value) {
		if(whereStr==null||whereStr.trim().equals("")){
			return this;
		}
		this.where = this.where + " and "+whereStr;
		if(value!=null) {
			for(Object v:value) {
				if(v!=null&&!v.equals("")) {
					params.add(v);
				}
			}
		}
		return this;
	}
	
	public SqlWrapperBuilder orderBy(String orderBy) {
		this.orderBy = orderBy;
		return this;
	}

	public SqlWrapperBuilder groupBy(String groupBy){
		this.groupBy = groupBy;
		return this;
	}
	
	
	public SqlWrapper sqlWrapper() {
		SqlWrapper sw = new SqlWrapper();
		String sql = "";
		String where = "";
		if(this.selectColumn==null||this.selectColumn.trim().equals("")) {
			sql =  "select * from "+this.tableName;
		}else {
			sql = "select "+this.selectColumn.trim() + " from "+this.tableName;
		}
		
		if(this.where!=null&&!this.where.equals("")) {
			where = this.where.substring(4);
			sql = sql+" where "+ where;
		}

		if(this.groupBy!=null&&!this.groupBy.trim().equals("")){
			sql = sql+" group by "+this.groupBy;
		}

		if(this.orderBy!=null&&!this.orderBy.trim().equals("")) {
			sql = sql+" order by "+this.orderBy;
		}
		sw.setSql(sql);
		sw.setParams(params);
		return sw;
	}
	
	public SqlWrapper countSqlWrapper() {
		SqlWrapper sw = new SqlWrapper();
		String sql = "";
		if(this.groupBy!=null&&!this.groupBy.trim().equals("")){
			sql = " select "+this.groupBy +" from "+this.tableName;
			sql = "select count(*) from ("+sql;
		}else{
			sql = "select count(*) from "+this.tableName;
		}

		String where = "";

		if(this.where!=null&&!this.where.equals("")) {
			where = this.where.substring(4);
			sql = sql+" where "+ where;
		}

		if(this.groupBy!=null&&!this.groupBy.trim().equals("")){
			sql = sql+" group by "+this.groupBy+") ttzz";
		}

		sw.setSql(sql);
		sw.setParams(params);
		return sw;
	}
	
	public SqlWrapper updateSqlWrapper() {
		SqlWrapper sw = new SqlWrapper();
		String sql =  "update "+this.tableName+" set "+this.updateColumn;
		String where = "";
		
		if(this.where!=null&&!this.where.equals("")) {
			where = this.where.substring(4);
			sql = sql+" where "+ where;
		}
		
		sw.setSql(sql);
		sw.setParams(updateParams);
		sw.getParams().addAll(params);
		return sw;
	}

	public SqlWrapper deleteSqlWrapper() {
		SqlWrapper sw = new SqlWrapper();
		String sql =  "delete from  "+this.tableName;
		String where = "";

		if(this.where!=null&&!this.where.equals("")) {
			where = this.where.substring(4);
			sql = sql+" where "+ where;
		}

		sw.setSql(sql);
		sw.setParams(params);
		return sw;
	}
	
	
}
