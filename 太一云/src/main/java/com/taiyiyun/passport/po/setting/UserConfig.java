package com.taiyiyun.passport.po.setting;

import java.io.Serializable;

public class UserConfig implements Serializable {

	private static final long serialVersionUID = -1175035620811415244L;
	private String setupUserId;
	private String targetId;
	private Integer isDisturb;
	private Integer isTop;
	private Integer userType;

	public String getSetupUserId() {
		return setupUserId;
	}

	public void setSetupUserId(String setupUserId) {
		this.setupUserId = setupUserId;
	}

	public String getTargetId() {
		return targetId;
	}

	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}

	public Integer getIsDisturb() {
		return isDisturb;
	}

	public void setIsDisturb(Integer isDisturb) {
		this.isDisturb = isDisturb;
	}

	public Integer getUserType() {
		return userType;
	}

	public void setUserType(Integer userType) {
		this.userType = userType;
	}

	public Integer getIsTop() {
		return isTop;
	}

	public void setIsTop(Integer isTop) {
		this.isTop = isTop;
	}
}
