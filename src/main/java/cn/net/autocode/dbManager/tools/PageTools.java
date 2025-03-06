package cn.net.autocode.dbManager.tools;

import com.alibaba.fastjson2.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class PageTools {
	
	/**
	 * 计算总页数及当前页
	 */
	static JSONObject step1(JSONObject json) {
		int total = json.getIntValue("total");
		int pageSize = json.getIntValue("pageSize");
		if(pageSize==0) {
			pageSize = 20;
			json.put("pageSize", pageSize);
		}
		int pageCount =  (total-1)/pageSize+1;
		json.put("pageCount", pageCount);

		//处理当前页
		int currentPage = json.getIntValue("currentPage");
		if(currentPage==0) {
			currentPage = 1;
		}else if(currentPage>pageCount) {
			currentPage = pageCount;
		}
		json.put("currentPage", currentPage);
		
		return json;
	}
	
	/**
	 * 计算要查询的开始结束内容
	 */
	static JSONObject step2(JSONObject json) {
		int pageSize = json.getIntValue("pageSize");
		int currentPage = json.getIntValue("currentPage");
		int total = json.getIntValue("total");
		int starNum = (currentPage-1)*pageSize;
		int endNum = currentPage*pageSize > total?total:currentPage*pageSize;
		json.put("starNum", starNum);
		json.put("endNum", endNum);
		return json;
	}
	
	/**
	 * 计算要显示的分页列表
	 */
	static JSONObject step3(JSONObject json) {
		int currentPage = json.getIntValue("currentPage");
		int pageCount = json.getIntValue("pageCount");
		List<Integer> pageList = new ArrayList<Integer>();
		
		if(pageCount<=6) {
			for(int i=1;i<=pageCount;i++) {
				pageList.add(i);
			}
		}else if(currentPage<=3) {
			for(int i=1;i<=6;i++) {
				pageList.add(i);
			}
			
		}else if(pageCount-currentPage>3) {
			for(int i=currentPage-3;i<=currentPage+3;i++) {
				pageList.add(i);
			}
		}else if(pageCount-currentPage<=3) {
			for(int i=pageCount-6;i<=pageCount;i++) {
				pageList.add(i);
			}
		}
		json.put("pageList", pageList);
		return json;
	}
	
	public static JSONObject compute(JSONObject json) {
		PageTools.step1(json);
		PageTools.step2(json);
		PageTools.step3(json);
		return json;
	}
	
}
