package com.taiyiyun.passport.po.asset;

//import java.math.BigDecimal;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * 转账消息内容定义
 * 
 * @author LiuQing
 */
public class TransferAccountsMessageContent {

	@JSONField(serialize = false)
	private static final long serialVersionUID = -2473367167420166098L;

	@JSONField(name = "tradeNo")
	private String tradeNo; // 交易编号

	@JSONField(name = "fromUserId")
	private String fromUserId; // 发起用户ID

	@JSONField(name = "toUserId")
	private String toUserId; // 接收用户ID

	@JSONField(name = "platformId")
	private String platformId; //平台id

	@JSONField(name = "platformName")
	private String platformName; //平台name

	@JSONField(name = "coinId")
	private String coinId; // 币种ID

	@JSONField(name = "coinName")
	private String coinName; // 币种名称

	@JSONField(name = "amount")
	private Double amount; // 金额

	@JSONField(name = "fee")
	private Double fee; // 手续费

	@JSONField(name = "text")
	private String text; // 说明

	@JSONField(name = "createTime")
	private Long createTime; // 创建时间

	@JSONField(name = "expireTime")
	private Long expireTime; // 过期时间

	@JSONField(name = "acceptTime")
	private Long acceptTime; // 接收时间

	public Long getAcceptTime() {
		return acceptTime;
	}

	public void setAcceptTime(Long acceptTime) {
		this.acceptTime = acceptTime;
	}

	public String getTradeNo() {
		return tradeNo;
	}

	public void setTradeNo(String tradeNo) {
		this.tradeNo = tradeNo;
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

	public static long getSerialVersionUID() {
		return serialVersionUID;
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

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public Double getFee() {
		return fee;
	}

	public void setFee(Double fee) {
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

	public Long getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(Long expireTime) {
		this.expireTime = expireTime;
	}

}
