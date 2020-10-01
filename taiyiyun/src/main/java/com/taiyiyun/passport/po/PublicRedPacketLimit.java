package com.taiyiyun.passport.po;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 红包限额实体
 *
 */
public class PublicRedPacketLimit implements java.io.Serializable {

	/*主键id*/
	private Long id;
	/*发送人Id*/
	private String userId;
	/*平台Id*/
	private String platformId;
	/*发币数量*/
	private BigDecimal coinCount;
	/*发币现金限额*/
	private BigDecimal cashAmount;
	/*有效状态*/
	private Integer limitStatus;
	/*签名*/
	private String dataSign;
	/*备注*/
	private String remark;
	/*修改时间*/
	private Date modifyTime;
	/*创建时间*/
	private Date createTime;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public BigDecimal getCoinCount() {
		return coinCount;
	}

	public void setCoinCount(BigDecimal coinCount) {
		this.coinCount = coinCount;
	}

	public BigDecimal getCashAmount() {
		return cashAmount;
	}

	public void setCashAmount(BigDecimal cashAmount) {
		this.cashAmount = cashAmount;
	}

	public Integer getLimitStatus() {
		return limitStatus;
	}

	public void setLimitStatus(Integer limitStatus) {
		this.limitStatus = limitStatus;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public Date getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getPlatformId() {
		return platformId;
	}

	public void setPlatformId(String platformId) {
		this.platformId = platformId;
	}

	public String getDataSign() {
		return dataSign;
	}

	public void setDataSign(String dataSign) {
		this.dataSign = dataSign;
	}
}
