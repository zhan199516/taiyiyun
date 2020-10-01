package com.taiyiyun.passport.bean;

import java.io.Serializable;

public class LoginUser implements Serializable {
	private static final long serialVersionUID = 3480570209061757776L;
	
	private String CreationTime;
	private String Address;
	private String Mobile;
	private int Status;
	private String NikeName;
	private String HeadPicture;
	private String UUID;

	public String getCreationTime() {
		return CreationTime;
	}

	public void setCreationTime(String creationTime) {
		CreationTime = creationTime;
	}

	public String getAddress() {
		return Address;
	}

	public void setAddress(String address) {
		Address = address;
	}

	public String getMobile() {
		return Mobile;
	}

	public void setMobile(String mobile) {
		Mobile = mobile;
	}

	public int getStatus() {
		return Status;
	}

	public void setStatus(int status) {
		Status = status;
	}

	public String getNikeName() {
		return NikeName;
	}

	public void setNikeName(String nikeName) {
		NikeName = nikeName;
	}

	public String getHeadPicture() {
		return HeadPicture;
	}

	public void setHeadPicture(String headPicture) {
		HeadPicture = headPicture;
	}

	public String getUUID() {
		return UUID;
	}

	public void setUUID(String uUID) {
		UUID = uUID;
	}

}
