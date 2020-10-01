package com.taiyiyun.passport.po.redpacket;

import java.io.Serializable;
import java.math.BigDecimal;

public class RedpacketDetailDto implements Serializable {

	private static final long serialVersionUID = -2210181436770810929L;
	/*红包id*/
	private String redpacketId;
	/*红包id*/
	private Short redpacketType;
	/*用户id*/
	private String userId;
	/*用户名*/
	private String userName;
	/*用户备注名*/
	private String nikeName;
	/*用户头像*/
	private String avatarUrl;
	/*抢到金额*/
	private BigDecimal amount;
	/*是否手气最佳*/
	private Short isBest;
	/*抢红包时间*/
	private String receiveTime;

	public RedpacketDetailDto() {

	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getNikeName() {
		return nikeName;
	}

	public void setNikeName(String nikeName) {
		this.nikeName = nikeName;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public Short getIsBest() {
		return isBest;
	}

	public void setIsBest(Short isBest) {
		this.isBest = isBest;
	}

	public String getRedpacketId() {
		return redpacketId;
	}

	public void setRedpacketId(String redpacketId) {
		this.redpacketId = redpacketId;
	}

	public Short getRedpacketType() {
		return redpacketType;
	}

	public void setRedpacketType(Short redpacketType) {
		this.redpacketType = redpacketType;
	}

	public String getReceiveTime() {
		return receiveTime;
	}

	public void setReceiveTime(String receiveTime) {
		this.receiveTime = receiveTime;
	}
}
