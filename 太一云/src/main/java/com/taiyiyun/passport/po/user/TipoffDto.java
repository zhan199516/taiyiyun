package com.taiyiyun.passport.po.user;

import java.io.Serializable;

public class TipoffDto implements Serializable {

	private static final long serialVersionUID = 3227718212292650155L;

	private String userId;
	private String tipoffId;
	private Integer tipoffType;
	private Integer illegalType;
	private String tipoffContent;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getTipoffId() {
		return tipoffId;
	}

	public void setTipoffId(String tipoffId) {
		this.tipoffId = tipoffId;
	}

	public Integer getTipoffType() {
		return tipoffType;
	}

	public void setTipoffType(Integer tipoffType) {
		this.tipoffType = tipoffType;
	}

	public Integer getIllegalType() {
		return illegalType;
	}

	public void setIllegalType(Integer illegalType) {
		this.illegalType = illegalType;
	}

	public String getTipoffContent() {
		return tipoffContent;
	}

	public void setTipoffContent(String tipoffContent) {
		this.tipoffContent = tipoffContent;
	}

}
