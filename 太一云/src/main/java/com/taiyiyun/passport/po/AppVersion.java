package com.taiyiyun.passport.po;

import java.io.Serializable;

public class AppVersion implements Serializable {

	private static final long serialVersionUID = 1299253412700675619L;

	private Long versionId;

	private String version;

	private Integer appType;

	private Integer status;

	private Boolean forcedUpdate;

	private Long onlineTime;

	private Long downtime;

	private String operatorUserId;

	private String operatorUserName;

	private String description;

	public Long getVersionId() {
		return versionId;
	}

	public void setVersionId(Long versionId) {
		this.versionId = versionId;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Integer getAppType() {
		return appType;
	}

	public void setAppType(Integer appType) {
		this.appType = appType;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Boolean getForcedUpdate() {
		return forcedUpdate;
	}

	public void setForcedUpdate(Boolean forcedUpdate) {
		this.forcedUpdate = forcedUpdate;
	}

	public Long getOnlineTime() {
		return onlineTime;
	}

	public void setOnlineTime(Long onlineTime) {
		this.onlineTime = onlineTime;
	}

	public Long getDowntime() {
		return downtime;
	}

	public void setDowntime(Long downtime) {
		this.downtime = downtime;
	}

	public String getOperatorUserId() {
		return operatorUserId;
	}

	public void setOperatorUserId(String operatorUserId) {
		this.operatorUserId = operatorUserId;
	}

	public String getOperatorUserName() {
		return operatorUserName;
	}

	public void setOperatorUserName(String operatorUserName) {
		this.operatorUserName = operatorUserName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
