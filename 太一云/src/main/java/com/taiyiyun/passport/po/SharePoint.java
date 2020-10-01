package com.taiyiyun.passport.po;

import java.io.Serializable;
import java.math.BigDecimal;

public class SharePoint implements Serializable {

	private static final long serialVersionUID = -5985292591406337642L;

	private String id;
	private String uuid;
	private BigDecimal balance;

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

	public BigDecimal getBalance() {
		return balance;
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}

}
