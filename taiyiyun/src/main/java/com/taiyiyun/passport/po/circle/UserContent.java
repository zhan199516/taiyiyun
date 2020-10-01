package com.taiyiyun.passport.po.circle;

import java.io.Serializable;

public class UserContent implements Serializable {

	private static final long serialVersionUID = 3422169461289187278L;

	private String userId = "";

	private String userName = "";

	private String avatarUrl = "";

	private String thumbAvatarUrl = "";

	private String backgroundImgUrl = "";

	private String description = "";
	
	private String uuid;

	private Integer type = -1;
	
	private Integer index;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
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

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Integer getIndex() {
		return index;
	}

	public void setIndex(Integer index) {
		this.index = index;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

}
