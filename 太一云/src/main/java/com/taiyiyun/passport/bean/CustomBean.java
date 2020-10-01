package com.taiyiyun.passport.bean;

import java.io.Serializable;
import java.util.List;

public class CustomBean implements Serializable {

	private static final long serialVersionUID = -4944272637270835343L;

	private String name;
	private String value;
	private List<String> userIdList;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public List<String> getUserIdList() {
		return userIdList;
	}

	public void setUserIdList(List<String> userIdList) {
		this.userIdList = userIdList;
	}

}
