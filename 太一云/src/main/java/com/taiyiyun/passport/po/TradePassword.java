package com.taiyiyun.passport.po;

import java.io.Serializable;

public class TradePassword implements Serializable {

	private static final long serialVersionUID = -8767738199692801664L;

	private String id;
	private String uuid;
	private String pwd;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getPwd() {
		return pwd;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

}
