package com.taiyiyun.passport.po.redpacket;

import com.taiyiyun.passport.transfer.Ask.AskParam;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public class RedpacketHandout implements Serializable {

	private static final long serialVersionUID = -2210181436770810929L;

	/*红包发送唯一限制token*/
	private String repeatToken;

	/*红包id*/
	private String redpacketId;
	/**
	 * 发红包用户UUID
	 */
	private String fromUuid;
	/**
	 * 发红包用户Id
	 */
	private String userId;
	/**
	 * 平台id
	 */
	private String platformId;

	/**
	 * 平台名称
	 */
	private String platformName;
	/**
	 * 币id
	 */
	private String coinId;

	/*每个币价值人民币*/
	private BigDecimal coinprice;

	/*此币种允许转账的最小值*/
	private BigDecimal quota;

	/*手续费*/
	private BigDecimal handCharge;

	/**
	 * 手续费费率
	 */
	private BigDecimal chargeRate;

	/**
	 * 红包类型 1：群组手气包（随机） 2：群组平均包 3：个人红包
	 */
	private Short type;
	/**
	 * 发送红包目标用户类型，0：个人  1：群组
	 */
	private Short sessionType;
	/**
	 * 目标用户sessionid
	 */
	private String sessionId;

	/**
	 * 每个红包金额
	 */
	private BigDecimal eachAmount;

	/**
	 * 红包总数量
	 */
	private BigDecimal totalAmount;

	/**
	 * 红包总数量
	 */
	private Integer redpacketCount;

	/**
	 * 红包寄语
	 */
	private String text;

	/*当前页*/
	private Integer page;

	/*每页记录数*/
	private Integer rows;
	/*记录时间戳*/
	private Long timestamp;

	/*批量操作请求参数对象*/
	private List<AskParam> askParams;

	public RedpacketHandout() {

	}

	public String getPlatformName() {
		return platformName;
	}

	public void setPlatformName(String platformName) {
		this.platformName = platformName;
	}

	public BigDecimal getChargeRate() {
		return chargeRate;
	}

	public void setChargeRate(BigDecimal chargeRate) {
		this.chargeRate = chargeRate;
	}

	public String getRedpacketId() {
		return redpacketId;
	}

	public void setRedpacketId(String redpacketId) {
		this.redpacketId = redpacketId;
	}

	public Integer getPage() {
		return page;
	}

	public void setPage(Integer page) {
		this.page = page;
	}

	public Integer getRows() {
		return rows;
	}

	public void setRows(Integer rows) {
		this.rows = rows;
	}

	public BigDecimal getCoinprice() {
		return coinprice;
	}

	public void setCoinprice(BigDecimal coinprice) {
		this.coinprice = coinprice;
	}

	public BigDecimal getQuota() {
		return quota;
	}

	public void setQuota(BigDecimal quota) {
		this.quota = quota;
	}

	public String getFromUuid() {
		return fromUuid;
	}

	public void setFromUuid(String fromUuid) {
		this.fromUuid = fromUuid;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
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

	public Short getType() {
		return type;
	}

	public void setType(Short type) {
		this.type = type;
	}

	public void setCoinId(String coinId) {
		this.coinId = coinId;
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

	public BigDecimal getEachAmount() {
		return eachAmount;
	}

	public void setEachAmount(BigDecimal eachAmount) {
		this.eachAmount = eachAmount;
	}

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}

	public Integer getRedpacketCount() {
		return redpacketCount;
	}

	public void setRedpacketCount(Integer redpacketCount) {
		this.redpacketCount = redpacketCount;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public List<AskParam> getAskParams() {
		return askParams;
	}

	public void setAskParams(List<AskParam> askParams) {
		this.askParams = askParams;
	}

	public BigDecimal getHandCharge() {
		return handCharge;
	}

	public void setHandCharge(BigDecimal handCharge) {
		this.handCharge = handCharge;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}


	public String getRepeatToken() {
		return repeatToken;
	}

	public void setRepeatToken(String repeatToken) {
		this.repeatToken = repeatToken;
	}
}
