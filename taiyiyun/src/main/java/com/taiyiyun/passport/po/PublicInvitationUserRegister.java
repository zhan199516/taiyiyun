package com.taiyiyun.passport.po;

/**
 * 邀请用户注册实体
 * @author wang85li
 *
 */
public class PublicInvitationUserRegister implements java.io.Serializable {

	private static final long serialVersionUID = -2328601076679284291L;
	/** 注册ID 主键 */
	private String registerId;
	/** 邀请ID */
	private String invitationId;
	/** 邀请用户ID */
	private String invitationUserId;
	/** 注册用户ID */
	private String userId;
	/** 注册手机号 */
	private String mobile;
	private java.util.Date createTime;
	private java.util.Date upateTime;
	
	public String getRegisterId() {
		return registerId;
	}
	public void setRegisterId(String registerId) {
		this.registerId = registerId;
	}
	public String getInvitationId() {
		return invitationId;
	}
	public void setInvitationId(String invitationId) {
		this.invitationId = invitationId;
	}
	public String getInvitationUserId() {
		return invitationUserId;
	}
	public void setInvitationUserId(String invitationUserId) {
		this.invitationUserId = invitationUserId;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public java.util.Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(java.util.Date createTime) {
		this.createTime = createTime;
	}
	public java.util.Date getUpateTime() {
		return upateTime;
	}
	public void setUpateTime(java.util.Date upateTime) {
		this.upateTime = upateTime;
	}

}
