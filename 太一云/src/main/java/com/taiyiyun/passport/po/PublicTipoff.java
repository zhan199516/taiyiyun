package com.taiyiyun.passport.po;

import java.io.Serializable;
import java.util.Date;

public class PublicTipoff implements Serializable {

	private static final long serialVersionUID = 3227718212292650155L;

	private Long id;
	private String userId;
	private String tipoffId;
	private Integer tipoffType;
	private Integer illegalType;
	private String tipoffContent;
	private Date tipoffTime;
	private String feedbackCode;
	private String feedbackContent;
	private Date feedbackTime;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

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

	public Date getTipoffTime() {
		return tipoffTime;
	}

	public void setTipoffTime(Date tipoffTime) {
		this.tipoffTime = tipoffTime;
	}

	public String getFeedbackCode() {
		return feedbackCode;
	}

	public void setFeedbackCode(String feedbackCode) {
		this.feedbackCode = feedbackCode;
	}

	public String getFeedbackContent() {
		return feedbackContent;
	}

	public void setFeedbackContent(String feedbackContent) {
		this.feedbackContent = feedbackContent;
	}

	public Date getFeedbackTime() {
		return feedbackTime;
	}

	public void setFeedbackTime(Date feedbackTime) {
		this.feedbackTime = feedbackTime;
	}
}
