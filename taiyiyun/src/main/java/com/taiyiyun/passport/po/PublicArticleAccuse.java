package com.taiyiyun.passport.po;

import java.io.Serializable;
import java.util.Date;

public class PublicArticleAccuse implements Serializable {

	private static final long serialVersionUID = 3227718212292650155L;

	private Long id;

	private String userId;

	private String targetArticleId;

	private Integer accuseTypeId;

	private String accuseDescription;

	private Date accuseTime;

	private String feedbackCode;

	private String feedbackDescription;

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

	public String getTargetArticleId() {
		return targetArticleId;
	}

	public void setTargetArticleId(String targetArticleId) {
		this.targetArticleId = targetArticleId;
	}

	public Integer getAccuseTypeId() {
		return accuseTypeId;
	}

	public void setAccuseTypeId(Integer accuseTypeId) {
		this.accuseTypeId = accuseTypeId;
	}

	public String getAccuseDescription() {
		return accuseDescription;
	}

	public void setAccuseDescription(String accuseDescription) {
		this.accuseDescription = accuseDescription;
	}

	public Date getAccuseTime() {
		return accuseTime;
	}

	public void setAccuseTime(Date accuseTime) {
		this.accuseTime = accuseTime;
	}

	public String getFeedbackCode() {
		return feedbackCode;
	}

	public void setFeedbackCode(String feedbackCode) {
		this.feedbackCode = feedbackCode;
	}

	public String getFeedbackDescription() {
		return feedbackDescription;
	}

	public void setFeedbackDescription(String feedbackDescription) {
		this.feedbackDescription = feedbackDescription;
	}

	public Date getFeedbackTime() {
		return feedbackTime;
	}

	public void setFeedbackTime(Date feedbackTime) {
		this.feedbackTime = feedbackTime;
	}

}
