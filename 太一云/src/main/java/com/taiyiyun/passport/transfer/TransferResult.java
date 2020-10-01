package com.taiyiyun.passport.transfer;

import java.io.Serializable;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * 转账API返回结果
 * @author LiuQing
 *
 * @param <T> -详细的结果数据
 */
public class TransferResult<T extends Serializable> implements Serializable {

	@JSONField(serialize = false)
	private static final long serialVersionUID = -8472305868253411590L;
	
	@JSONField(name = "platform")
	private String platform; // 平台

	@JSONField(name = "success")
	private boolean success; // 是否成功

	@JSONField(name = "errorMessage")
	private String errorMessage; // 错误信息

	@JSONField(name = "data")
	private T data; // 数据

	@JSONField(serialize = false)
	private Throwable cause; // 异常原因

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	public Throwable getCause() {
		return cause;
	}

	public void setCause(Throwable cause) {
		this.cause = cause;
	}

}
