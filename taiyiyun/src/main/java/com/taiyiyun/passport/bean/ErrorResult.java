package com.taiyiyun.passport.bean;

import java.util.HashMap;
import java.util.Map;

public class ErrorResult {
	
	/**
	 * 系统错误编码
	 */
	public static final String CODE_SYSERR = "-1000";
	
	/**
	 * 错误编码明细信息
	 */
	public static final Map<String, String> ERROR_MAP = new HashMap<String, String>();
	{
		ERROR_MAP.put(CODE_SYSERR, "系统错误");
	}
	
	private String code;
	
	private String message;
	
	private String detail;
	
	public ErrorResult() {
		
	}
	
	public ErrorResult(String code) {
		this.code = code;
		this.message = ERROR_MAP.get(code);
	}
	
	public ErrorResult(String code, String message, String detail) {
		this.code = code;
		this.message = message;
		this.detail = detail;
	}

	public String getCode() {
		return code;
	}

	public ErrorResult setCode(String code) {
		this.code = code;
		return this;
	}

	public String getMessage() {
		return message;
	}

	public ErrorResult setMessage(String message) {
		this.message = message;
		return this;
	}

	public String getDetail() {
		return detail;
	}

	public ErrorResult setDetail(String detail) {
		this.detail = detail;
		return this;
	}

}