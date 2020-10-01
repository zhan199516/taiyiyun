package com.taiyiyun.passport.po;

import java.io.Serializable;

import com.alibaba.fastjson.annotation.JSONField;

public class ThirdApp implements Serializable {

	private static final long serialVersionUID = -6737625371785432517L;

	@JSONField(name = "appId")
	private String appId;
	@JSONField(name = "appName")
	private String appName;
	@JSONField(name = "logoUrl")
	private String logoUrl;
	@JSONField(name = "appUrl")
	private String appUrl;
	@JSONField(name = "appDescription")
	private String description;
	//第三方appKey
	private String thirdpartAppkey;

	public String getThirdpartAppkey() {
		return thirdpartAppkey;
	}

	public void setThirdpartAppkey(String thirdpartAppkey) {
		this.thirdpartAppkey = thirdpartAppkey;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getLogoUrl() {
		return logoUrl;
	}

	public void setLogoUrl(String logoUrl) {
		this.logoUrl = logoUrl;
	}

	public String getAppUrl() {
		return appUrl;
	}

	public void setAppUrl(String appUrl) {
		this.appUrl = appUrl;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
