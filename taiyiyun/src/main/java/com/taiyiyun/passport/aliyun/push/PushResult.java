package com.taiyiyun.passport.aliyun.push;

import java.io.Serializable;

import com.alibaba.fastjson.JSON;

/**
 * 推送结果
 * @author LiuQing
 */
public class PushResult implements Serializable {
	
	private static final long serialVersionUID = -3829217226784011664L;

	private boolean success;

	private String data;

	private Throwable cause;

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public Throwable getCause() {
		return cause;
	}

	public void setCause(Throwable cause) {
		this.cause = cause;
	}
	
	public String toString() {
		return JSON.toJSONString(this);
	}
}
