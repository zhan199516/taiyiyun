package com.taiyiyun.passport.po.redpacket;

public class ReturnResult<T> {

	private Integer status;

	private T data;
	
	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	private String error;
	

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

}
