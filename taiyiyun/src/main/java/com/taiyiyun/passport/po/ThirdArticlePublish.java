package com.taiyiyun.passport.po;

import java.io.Serializable;

public class ThirdArticlePublish implements Serializable {

	private static final long serialVersionUID = -7714285105805906353L;

	private String appId;

	private String appKey;

	private String appSecret;

	private String remark;

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getAppKey() {
		return appKey;
	}

	public void setAppKey(String appKey) {
		this.appKey = appKey;
	}

	public String getAppSecret() {
		return appSecret;
	}

	public void setAppSecret(String appSecret) {
		this.appSecret = appSecret;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

}
