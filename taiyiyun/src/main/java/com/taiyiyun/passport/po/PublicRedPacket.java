package com.taiyiyun.passport.po;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 红包表实体
 * @author wang85li
 *
 */
public class PublicRedPacket implements java.io.Serializable {

	private static final long serialVersionUID = 2795261185592343368L;
	
	/** 红包ID  主键 */
	private String packId;
	/** 发红包者用户ID */
	private String userId;
	/** 发红包者uuid */
	private String fromUuid;
	/** session类型（0-个人，1-群） */
	private Short sessionType;
	/**sessionId（范围类型为0时存储个人id，范围类型为1时存储群id）*/
	private String sessionId;
	/**资产平台ID*/
	private String platformId;
	/**资产平台名称*/
	private String platformName;
	/**币种ID*/
	private String coinId;
	/*交易id*/
	private Long tradeId;
	/**币种名称*/
	private String coinName;
	/**红包类型*/
	private Short packType;
	/**发币金额（数量）*/
	private BigDecimal amount;
	/**兑换费率*/
	private BigDecimal exchangeRate;
	/**红包现金价值*/
	private  BigDecimal cashAmount;
	/**红包现金总价值*/
	private  BigDecimal totalCashAmount;
	/**红包个数*/
	private Integer packCount;
	/**红包总个数*/
	private Integer totalCount;
	/**已抢红包个数*/
	private Integer grabedCount;
	/**红包状态*/
	private Integer packStatus;
	/***手续费*/
	private BigDecimal fee;
	/**留言*/
	private String remark;
	/*过期清算执行状态*/
	private Short clearStatus;
	/*过期清算执行时间*/
	private Date clearTime;
	/**创建时间*/
	private Date createTime;
	/**过期时间*/
	private Long expireTime;
	/*当前页数*/
	private Integer start;
    /*每页记录数*/
	private Integer offset;
	/*记录时间戳*/
	private Long recordTimestamp;
	/*红包发送唯一限制token*/
	private String repeatToken;

	public Integer getGrabedCount() {
		return grabedCount;
	}

	public String getPlatformName() {
		return platformName;
	}

	public void setPlatformName(String platformName) {
		this.platformName = platformName;
	}

	public void setGrabedCount(Integer grabedCount) {
		this.grabedCount = grabedCount;
	}

	public String getFromUuid() {
		return fromUuid;
	}

	public void setFromUuid(String fromUuid) {
		this.fromUuid = fromUuid;
	}

	public Long getTradeId() {
		return tradeId;
	}

	public void setTradeId(Long tradeId) {
		this.tradeId = tradeId;
	}

	public String getPackId() {
		return packId;
	}
	public void setPackId(String packId) {
		this.packId = packId;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public short getSessionType() {
		return sessionType;
	}
	public void setSessionType(Short sessionType) {
		this.sessionType = sessionType;
	}
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	public String getPlatformId() {
		return platformId;
	}
	public void setPlatformId(String platformId) {
		this.platformId = platformId;
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

	public Short getPackType() {
		return packType;
	}
	public void setPackType(Short packType) {
		this.packType = packType;
	}
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	public BigDecimal getExchangeRate() {
		return exchangeRate;
	}
	public void setExchangeRate(BigDecimal exchangeRate) {
		this.exchangeRate = exchangeRate;
	}
	public BigDecimal getCashAmount() {
		return cashAmount;
	}
	public void setCashAmount(BigDecimal cashAmount) {
		this.cashAmount = cashAmount;
	}
	public int getPackCount() {
		return packCount;
	}
	public void setPackCount(Integer packCount) {
		this.packCount = packCount;
	}
	public Integer getPackStatus() {
		return packStatus;
	}

	public void setPackStatus(Integer packStatus) {
		this.packStatus = packStatus;
	}

	public BigDecimal getFee() {
		return fee;
	}
	public void setFee(BigDecimal fee) {
		this.fee = fee;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}

	public Short getClearStatus() {
		return clearStatus;
	}

	public void setClearStatus(Short clearStatus) {
		this.clearStatus = clearStatus;
	}

	public Date getClearTime() {
		return clearTime;
	}

	public void setClearTime(Date clearTime) {
		this.clearTime = clearTime;
	}

	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public Long getExpireTime() {
		return expireTime;
	}
	public void setExpireTime(Long expireTime) {
		this.expireTime = expireTime;
	}
	public Integer getStart() {
		return start;
	}

	public void setStart(Integer start) {
		this.start = start;
	}

	public Integer getOffset() {
		return offset;
	}

	public void setOffset(Integer offset) {
		this.offset = offset;
	}

	public Integer getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(Integer totalCount) {
		this.totalCount = totalCount;
	}

	public BigDecimal getTotalCashAmount() {
		return totalCashAmount;
	}

	public void setTotalCashAmount(BigDecimal totalCashAmount) {
		this.totalCashAmount = totalCashAmount;
	}

	public Long getRecordTimestamp() {
		return recordTimestamp;
	}

	public void setRecordTimestamp(Long recordTimestamp) {
		this.recordTimestamp = recordTimestamp;
	}

	public String getRepeatToken() {
		return repeatToken;
	}

	public void setRepeatToken(String repeatToken) {
		this.repeatToken = repeatToken;
	}
}
