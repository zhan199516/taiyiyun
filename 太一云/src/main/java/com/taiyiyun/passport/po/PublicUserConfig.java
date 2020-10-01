package com.taiyiyun.passport.po;

import java.io.Serializable;
import java.util.Date;

public class PublicUserConfig implements Serializable {

	private static final long serialVersionUID = -1175035620811415244L;

	private Long id;
	private String setupUserId;
	private String targetId;
	private Integer isDisturb;
	private Integer isTop;
	private Integer userType;
	private Date modifyTime;
	private Date createTime;

	private PublicUser user;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

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

	public Date getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
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

	public PublicUser getUser() {
		return user;
	}

	public void setUser(PublicUser user) {
		this.user = user;
	}
}
