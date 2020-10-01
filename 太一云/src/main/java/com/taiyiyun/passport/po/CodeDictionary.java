package com.taiyiyun.passport.po;

import java.io.Serializable;

public class CodeDictionary implements Serializable {

	private static final long serialVersionUID = 6480020877054704496L;
	
	private Integer id;
	
	private String business;
	
	private String code;
	
	private String caption;
	
	private Integer sortId;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getBusiness() {
		return business;
	}

	public void setBusiness(String business) {
		this.business = business;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public Integer getSortId() {
		return sortId;
	}

	public void setSortId(Integer sortId) {
		this.sortId = sortId;
	}
}
