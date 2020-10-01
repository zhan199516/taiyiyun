package com.taiyiyun.passport.bean;

import java.io.Serializable;

public class UserCache implements Serializable{

	private static final long serialVersionUID = 5870009651266789102L;
	
	private String userId;
	
	private String uuid;
	
	private String userName;
	
	private String mobile;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
}
