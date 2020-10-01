package com.taiyiyun.passport.bean;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;

public class UserDetails implements Serializable {

	private static final long serialVersionUID = -4484462964183217771L;

	@JSONField(name = "userId")
	private String userId;
	
	@JSONField(name = "userName")
	private String userName;
	
	@JSONField(name = "uuid")
	private String uuid;
	
	@JSONField(name = "mobile")
	private String mobile;
	
	@JSONField(name = "nikeName")
	private String nikeName;

	@JSONField(name = "clientId")
	private String clientId;

	@JSONField(name = "userKey")
	private String userKey;
	
	@JSONField(name = "avatarUrl")
	private String avatarUrl;
	
	@JSONField(name = "thumbAvatarUrl")
	private String thumbAvatarUrl;
	
	@JSONField(name = "backgroundImgUrl")
	private String backgroundImgUrl;
	
	@JSONField(name = "description")
	private String description;

	public UserDetails() {
	}


	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getNikeName() {
		return nikeName;
	}

	public void setNikeName(String nikeName) {
		this.nikeName = nikeName;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getUserKey() {
		return userKey;
	}

	public void setUserKey(String userKey) {
		this.userKey = userKey;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}

	public String getThumbAvatarUrl() {
		return thumbAvatarUrl;
	}

	public void setThumbAvatarUrl(String thumbAvatarUrl) {
		this.thumbAvatarUrl = thumbAvatarUrl;
	}

	public String getBackgroundImgUrl() {
		return backgroundImgUrl;
	}

	public void setBackgroundImgUrl(String backgroundImgUrl) {
		this.backgroundImgUrl = backgroundImgUrl;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
}
