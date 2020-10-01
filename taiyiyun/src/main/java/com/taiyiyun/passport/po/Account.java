package com.taiyiyun.passport.po;

import java.io.Serializable;

public class Account implements Serializable {

	private static final long serialVersionUID = 240244847681126897L;

	private String uuid;

	private String recommendUserId;

	private String address;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getRecommendUserId() {
		return recommendUserId;
	}

	public void setRecommendUserId(String recommendUserId) {
		this.recommendUserId = recommendUserId;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

}
