package com.taiyiyun.passport.sqlserver.po;

import java.io.Serializable;
import java.util.Date;

public class AntifakeAddressStore implements Serializable {

	private static final long serialVersionUID = 1L;

	private String address;

	private String symbol;

	private Boolean status;

	private String secretkey;

	private Boolean transferStatus;

	private Date creationTime;

	public String getAddress() {
		return address;
	}

	public String getSymbol() {
		return symbol;
	}

	public Boolean getStatus() {
		return status;
	}

	public String getSecretkey() {
		return secretkey;
	}

	public Boolean getTransferStatus() {
		return transferStatus;
	}

	public Date getCreationTime() {
		return creationTime;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public void setStatus(Boolean status) {
		this.status = status;
	}

	public void setSecretkey(String secretkey) {
		this.secretkey = secretkey;
	}

	public void setTransferStatus(Boolean transferStatus) {
		this.transferStatus = transferStatus;
	}

	public void setCreationTime(Date creationTime) {
		this.creationTime = creationTime;
	}

}
