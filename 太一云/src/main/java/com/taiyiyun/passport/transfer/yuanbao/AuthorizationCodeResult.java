package com.taiyiyun.passport.transfer.yuanbao;

import java.io.Serializable;

import com.alibaba.fastjson.annotation.JSONField;

public final class AuthorizationCodeResult implements Serializable {

	@JSONField(serialize = false)
	private static final long serialVersionUID = -5588015224162481554L;

	@JSONField(name = "AuthorizationCode")
	private String authorizationCode;

	public String getAuthorizationCode() {
		return authorizationCode;
	}

	public void setAuthorizationCode(String authorizationCode) {
		this.authorizationCode = authorizationCode;
	}

}
