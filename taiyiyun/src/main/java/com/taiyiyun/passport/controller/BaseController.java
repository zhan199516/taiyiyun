package com.taiyiyun.passport.controller;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class BaseController {
	public final Logger logger = LoggerFactory.getLogger(getClass());

	protected final String toJson(int status, String error, String apiName, Object value) {
		String internalError = null;
		return toJson(status, error, apiName, value, internalError);
	}

	protected final String toJson(int status, String error, String apiName, Object value, String internalError) {
		Map<String, Object> json = new HashMap<String, Object>();
		json.put("status", status);
		json.put("error", error);
		json.put("apiName", apiName);
		json.put("data", value);
		json.put("internalError", internalError);
		String rjson = JSON.toJSONString(json);
		logger.info("controller info ==>" + rjson);
		return rjson;
	}
	
	protected final String toJson(String status, String error, Object value) {
		Map<String, Object> json = new HashMap<String, Object>();
		json.put("Status", status);
		json.put("errcode", error);
		json.put("Data", value);
		String rjson = JSON.toJSONString(json);
		logger.info("controller info ==>" + rjson);
		return rjson;
	}
	
	protected final String toJson(boolean success,int errorCode, String error, Object value) {
		Map<String, Object> json = new HashMap<String, Object>();
		json.put("success", success);
		json.put("errorCode", errorCode);
		json.put("error", error);
		json.put("data", value);
		String rjson = JSON.toJSONString(json);
		logger.info("controller info ==>" + rjson);
		return rjson;
	}
	
	protected final String toJson(boolean success,int errorCode, Object value) {
		
		return toJson(success, errorCode, null , value);
	}
	
	protected final String toJson(boolean success,String error, Object value) {
		
		Map<String, Object> json = new HashMap<String, Object>();
		json.put("success", success);
		json.put("error", error);
		json.put("data", value);
		String rjson = JSON.toJSONString(json);
		logger.info("controller info ==>" + rjson);
		return rjson;
	}
	
	protected final String toJson(String status, Object value) {
		
		Map<String, Object> json = new HashMap<String, Object>();
		json.put("Status", status);
		json.put("Data", value);
		String rjson = JSON.toJSONString(json);
		logger.info("controller info ==>" + rjson);
		return rjson;
	}
	
	protected final String toJson(int status, String error, String apiName, Object value, boolean hasMore) {
		Map<String, Object> json = new HashMap<String, Object>();
		json.put("status", status);
		json.put("error", error);
		json.put("apiName", apiName);
		json.put("data", value);
		json.put("hasMore", hasMore);
		String rjson = JSON.toJSONString(json);
		logger.info("controller info ==>" + rjson);
		return rjson;
	}
	
	protected final String toJson(int status, String error, String apiName, Object value, Map<String, Object> extData) {
		Map<String, Object> json = new HashMap<String, Object>();
		json.put("status", status);
		json.put("error", error);
		json.put("apiName", apiName);
		json.put("data", value);
		json.putAll(extData);
		String rjson = JSON.toJSONString(json);
		logger.info("controller info ==>" + rjson);
		return rjson;
	}
}
