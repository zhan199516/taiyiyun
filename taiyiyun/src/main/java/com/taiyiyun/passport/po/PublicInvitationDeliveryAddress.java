package com.taiyiyun.passport.po;

/***
 * 邀请一定数量用户后，设置的配送地址
 * @author wang85li
 *
 */
public class PublicInvitationDeliveryAddress implements java.io.Serializable {

	private static final long serialVersionUID = -7845450072453605058L;
	
	/**地址ID*/
	private String addressId;
	/**邀请人ID*/
	private String invitationId;
	/**发邀请人用户ID*/
	private String invitationUserId;
	/**手机号码*/
	private String mobile;
	/**用户名称*/
	private String userName;
	/**用户配送地址*/
	private String userAddress;
	/**配送状态 0 未配送1 配送成功2 配送中*/
	private Integer deliveryStatus;
	private java.util.Date createTime;
	private java.util.Date upateTime;
	
	public String getAddressId() {
		return addressId;
	}
	public void setAddressId(String addressId) {
		this.addressId = addressId;
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
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getUserAddress() {
		return userAddress;
	}
	public void setUserAddress(String userAddress) {
		this.userAddress = userAddress;
	}
	public Integer getDeliveryStatus() {
		return deliveryStatus;
	}
	public void setDeliveryStatus(Integer deliveryStatus) {
		this.deliveryStatus = deliveryStatus;
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
