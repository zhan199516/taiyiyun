package com.taiyiyun.passport.bean;

import java.io.Serializable;

public class TradePasswordBean implements Serializable {

	private static final long serialVersionUID = 6290023797850925L;

	private String transId;

	private String password;

	private String old;

	public String getTransId() {
		return transId;
	}

	public void setTransId(String transId) {
		this.transId = transId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getOld() {
		return old;
	}

	public void setOld(String old) {
		this.old = old;
	}

}
