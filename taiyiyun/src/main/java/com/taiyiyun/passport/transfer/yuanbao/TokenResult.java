package com.taiyiyun.passport.transfer.yuanbao;

import java.io.Serializable;

import com.alibaba.fastjson.annotation.JSONField;

public final class TokenResult implements Serializable {

	@JSONField(serialize = false)
	private static final long serialVersionUID = 2439314246504999569L;

	@JSONField(name = "user_key")
	private String userKey;

	@JSONField(name = "access_token")
	private String accessToken;

	public String getUserKey() {
		return userKey;
	}

	public void setUserKey(String userKey) {
		this.userKey = userKey;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

}
