package com.taiyiyun.passport.po;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;

public class UpdateCheck implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;

	private String title;

	private String text;
	
	@JSONField(serialize = false)
	private String version;

	/*qq客服群号*/
	private String qqGroup;

	@JSONField(serialize = false)
	private Integer appType;

	private String url;

	private Integer updateStatus;
	
	private String rightButton;
	
	private String leftButton;
	
	@JSONField(serialize = false)
	private Integer type;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
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

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Integer getUpdateStatus() {
		return updateStatus;
	}

	public void setUpdateStatus(Integer updateStatus) {
		this.updateStatus = updateStatus;
	}

	public String getRightButton() {
		return rightButton;
	}

	public void setRightButton(String rightButton) {
		this.rightButton = rightButton;
	}

	public String getLeftButton() {
		return leftButton;
	}

	public void setLeftButton(String leftButton) {
		this.leftButton = leftButton;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getQqGroup() {
		return qqGroup;
	}

	public void setQqGroup(String qqGroup) {
		this.qqGroup = qqGroup;
	}
}
