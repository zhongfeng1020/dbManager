package cn.net.autocode.dbManager;

import java.util.ArrayList;
import java.util.List;

public class SqlWrapper {

	private String sql;
	
	private List<Object> params = new ArrayList<Object>();
	
	private List<String> namedParams = new ArrayList<String>();

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public List<Object> getParams() {
		return params;
	}

	public void setParams(List<Object> params) {
		this.params = params;
	}

	public List<String> getNamedParams() {
		return namedParams;
	}

	public void setNamedParams(List<String> namedParams) {
		this.namedParams = namedParams;
	}
}
