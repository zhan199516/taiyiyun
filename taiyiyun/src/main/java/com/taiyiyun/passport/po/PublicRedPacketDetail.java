package com.taiyiyun.passport.po;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 红包明细实体
 * 
 * @author wang85li
 *
 */
public class PublicRedPacketDetail implements java.io.Serializable {

	private static final long serialVersionUID = 6203825716161472134L;
	/*接口批量操作时匹配key值*/
	private String mapkey;
	/** 红包明细ID 主键 */
	private String detailId;
	/*转账交易id*/
	private Long tradeId;
	/** 红包ID 外键 */
	private String packId;
	/** 发红包userID */
	private String fromUserId;
	/*发红包uuid */
	private String fromUuid;
	/** 领取人ID */
	private String toUserId;
	/** 领取人uuid */
	private String toUuid;
	/** 币种ID */
	private String coinId;
	/** session类型（0-个人，1-群） */
	private Short sessionType;
	/**sessionId（范围类型为0时存储个人id，范围类型为1时存储群id）*/
	private String sessionId;
	/**抢到的金额*/
	/** 抢到的金额 */
	private BigDecimal amount;
	/** 兑换费率 */
	private BigDecimal exchangeRate;
	/** 现金价值 */
	private BigDecimal cashAmount;
	/** 手续费 */
	private BigDecimal fee;
	/*是否手气最佳*/
	private Short isBest;
	/*红包类型*/
	private Short packType;
	/** 领取时间 */
	private Date receiveTime;
	/** 转账状态 **/
	private Short transferStatus;
	/** 转账完成时间 */
	private Date transferTime;
	/** 创建时间 */
	private Date createTime;
	/** 冻结交易ID */
	private Long freezeTradeId;
	/**过期时间*/
	private Long expireTime;
	/**解除冻结时间 */
	private Date unfreezeTime;
	/** 解除冻结状态*/
	private Short unfreezeStatus;
	/** 结算交易ID */
	private Long clearTradeId;
	/*当前页数*/
	private Integer start;
	/*每页记录数*/
	private Integer offset;
	/*已抢币总金额*/
	private BigDecimal grabedAmount;
	/*已抢总金额*/
	private BigDecimal grabedCashAmount;
	/*服务费总金额*/
	private BigDecimal feeAmount;
	/*已抢总数量*/
	private Integer grabedCount;
	/*用户名*/
	private String userName;
	/*用户备注名*/
	private String nikeName;
	/*用户头像*/
	private String avatarUrl;
	/*平台id*/
	private String platformId;
	/*记录时间戳*/
	private Long recordTimestamp;
	/**绑定状态*/
	private Integer bindStatus;
	/*备注说明*/
	private String text;

	public String getCoinId() {
		return coinId;
	}

	public void setCoinId(String coinId) {
		this.coinId = coinId;
	}

	public Date getUnfreezeTime() {
		return unfreezeTime;
	}

	public void setUnfreezeTime(Date unfreezeTime) {
		this.unfreezeTime = unfreezeTime;
	}

	public Short getUnfreezeStatus() {
		return unfreezeStatus;
	}

	public void setUnfreezeStatus(Short unfreezeStatus) {
		this.unfreezeStatus = unfreezeStatus;
	}

	public BigDecimal getGrabedCashAmount() {
		return grabedCashAmount;
	}

	public String getMapkey() {
		return mapkey;
	}

	public void setMapkey(String mapkey) {
		this.mapkey = mapkey;
	}

	public void setGrabedCashAmount(BigDecimal grabedCashAmount) {
		this.grabedCashAmount = grabedCashAmount;
	}

	public Short getPackType() {
		return packType;
	}

	public void setPackType(Short packType) {
		this.packType = packType;
	}

	public String getDetailId() {
		return detailId;
	}

	public void setDetailId(String detailId) {
		this.detailId = detailId;
	}

	public String getPackId() {
		return packId;
	}

	public void setPackId(String packId) {
		this.packId = packId;
	}

	public Long getTradeId() {
		return tradeId;
	}

	public void setTradeId(Long tradeId) {
		this.tradeId = tradeId;
	}

	public String getFromUserId() {
		return fromUserId;
	}

	public void setFromUserId(String fromUserId) {
		this.fromUserId = fromUserId;
	}

	public String getFromUuid() {
		return fromUuid;
	}

	public void setFromUuid(String fromUuid) {
		this.fromUuid = fromUuid;
	}

	public String getToUserId() {
		return toUserId;
	}

	public void setToUserId(String toUserId) {
		this.toUserId = toUserId;
	}

	public Short getSessionType() {
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

	public BigDecimal getFee() {
		return fee;
	}

	public void setFee(BigDecimal fee) {
		this.fee = fee;
	}

	public Short getIsBest() {
		return isBest;
	}

	public void setIsBest(Short isBest) {
		this.isBest = isBest;
	}

	public Date getReceiveTime() {
		return receiveTime;
	}

	public void setReceiveTime(Date receiveTime) {
		this.receiveTime = receiveTime;
	}

	public Short getTransferStatus() {
		return transferStatus;
	}

	public void setTransferStatus(Short transferStatus) {
		this.transferStatus = transferStatus;
	}

	public Date getTransferTime() {
		return transferTime;
	}

	public void setTransferTime(Date transferTime) {
		this.transferTime = transferTime;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Long getFreezeTradeId() {
		return freezeTradeId;
	}

	public void setFreezeTradeId(Long freezeTradeId) {
		this.freezeTradeId = freezeTradeId;
	}

	public Long getClearTradeId() {
		return clearTradeId;
	}

	public void setClearTradeId(Long clearTradeId) {
		this.clearTradeId = clearTradeId;
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

	public BigDecimal getGrabedAmount() {
		return grabedAmount;
	}

	public void setGrabedAmount(BigDecimal grabedAmount) {
		this.grabedAmount = grabedAmount;
	}

	public Integer getGrabedCount() {
		return grabedCount;
	}

	public void setGrabedCount(Integer grabedCount) {
		this.grabedCount = grabedCount;
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

	public BigDecimal getFeeAmount() {
		return feeAmount;
	}

	public void setFeeAmount(BigDecimal feeAmount) {
		this.feeAmount = feeAmount;
	}

	public Long getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(Long expireTime) {
		this.expireTime = expireTime;
	}

	public String getPlatformId() {
		return platformId;
	}

	public void setPlatformId(String platformId) {
		this.platformId = platformId;
	}

	public Long getRecordTimestamp() {
		return recordTimestamp;
	}

	public void setRecordTimestamp(Long recordTimestamp) {
		this.recordTimestamp = recordTimestamp;
	}

	public Integer getBindStatus() {
		return bindStatus;
	}

	public void setBindStatus(Integer bindStatus) {
		this.bindStatus = bindStatus;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getToUuid() {
		return toUuid;
	}

	public void setToUuid(String toUuid) {
		this.toUuid = toUuid;
	}
}
