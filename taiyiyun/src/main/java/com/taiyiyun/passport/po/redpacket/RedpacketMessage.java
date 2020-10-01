package com.taiyiyun.passport.po.redpacket;

import java.io.Serializable;
import java.math.BigDecimal;

public class RedpacketMessage implements Serializable {

	private static final long serialVersionUID = -2210181436770810929L;

	/*红包id*/
	private String redPacketId;
	/*红包个数*/
	private Integer redpacketCount;
	/*红包类型*/
	private Short redpacketType;
	/*发消息用户id*/
	private String fromUserId;
	/*收消息用户id*/
	private String toUserId;
	/*平台id*/
	private String platformId;
	/*平台名称*/
	private String platformName;
	/*用户名称，如果是群组用户，为昵称，如果是个人用户为姓名*/
	private String name;
	/*群成员头像地址*/
	private String avatarUrl;
	/*币Id*/
	private String coinId;
	/*币名称*/
	private String coinName;
	/*发红包金额*/
	private BigDecimal amount;
	/*发红包手续费*/
	private BigDecimal fee;
	/*红包寄语*/
	private String text;
	/*红包创建*/
	private Long createTime;
	/*红包失效时间*/
	private Long expireTime;
	/*消息接收时间*/
	private Long acceptTime;
	/*红包状态*/
	private Integer status;


	public String getRedPacketId() {
		return redPacketId;
	}

	public void setRedPacketId(String redPacketId) {
		this.redPacketId = redPacketId;
	}

	public Integer getRedpacketCount() {
		return redpacketCount;
	}

	public void setRedpacketCount(Integer redpacketCount) {
		this.redpacketCount = redpacketCount;
	}

	public Short getRedpacketType() {
		return redpacketType;
	}

	public void setRedpacketType(Short redpacketType) {
		this.redpacketType = redpacketType;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getFromUserId() {
		return fromUserId;
	}

	public void setFromUserId(String fromUserId) {
		this.fromUserId = fromUserId;
	}

	public String getToUserId() {
		return toUserId;
	}

	public void setToUserId(String toUserId) {
		this.toUserId = toUserId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}

	public String getPlatformId() {
		return platformId;
	}

	public void setPlatformId(String platformId) {
		this.platformId = platformId;
	}

	public String getPlatformName() {
		return platformName;
	}

	public void setPlatformName(String platformName) {
		this.platformName = platformName;
	}

	public String getCoinId() {
		return coinId;
	}

	public void setCoinId(String coinId) {
		this.coinId = coinId;
	}

	public String getCoinName() {
		return coinName;
	}

	public void setCoinName(String coinName) {
		this.coinName = coinName;
	}

	public BigDecimal getFee() {
		return fee;
	}

	public void setFee(BigDecimal fee) {
		this.fee = fee;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Long createTime) {
		this.createTime = createTime;
	}

	public Long getAcceptTime() {
		return acceptTime;
	}

	public void setAcceptTime(Long acceptTime) {
		this.acceptTime = acceptTime;
	}

	public Long getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(Long expireTime) {
		this.expireTime = expireTime;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}
}
