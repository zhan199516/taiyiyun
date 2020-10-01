package com.taiyiyun.passport.transfer;

import java.io.Serializable;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * 用户信息
 * @author LiuQing
 */
public class UserInfo implements Serializable {

	@JSONField(serialize = false)
	private static final long serialVersionUID = -8438059371849401990L;

	@JSONField(name = "email")
	private String email; // 邮箱

	@JSONField(name = "mobile")
	private String mobile; // 手机

	@JSONField(name = "username")
	private String username; // 用户名

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

}
