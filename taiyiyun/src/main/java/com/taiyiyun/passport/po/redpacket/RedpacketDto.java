package com.taiyiyun.passport.po.redpacket;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public class RedpacketDto<T> implements Serializable {

	private static final long serialVersionUID = -2210181436770810929L;

	/*红包id*/
	private String redpacketId;
	/*发红包用户id*/
	private String userId;
	/*发红包用户姓名*/
	private String userName;
	/*发红包备注名*/
	private String nikeName;
	/*发红包用户头像*/
	private String avatarUrl;
	/*平台id*/
	private String platformId;
	/*币种id*/
	private String coinId;
	/*币种名称*/
	private String coinName;
	/*附加文字*/
	private String text;
	/*红包类型*/
	private Short redpacketType;
	/*红包状态*/
	private Short redpacketStatus;
	/*红包币总金额*/
	private BigDecimal totalAmount;
	/*红包现金价值总金额*/
	private BigDecimal totalCashAmount;
	/*红包剩余币金额*/
	private BigDecimal grabAmount;
	/*红包总个数*/
	private Integer totalCount;
	/*已抢红包个数*/
	private Integer grabedCount;
	/*红包抢光时间，单位：秒*/
	private String grabAllTimes;
	/*当前页*/
	private Integer currentPage;
	/*是否抢到*/
	private Short isGrab;
	/*当前用户抢红包信息*/
	private T grabedUser;
	/*红包明细列表*/
	private List<T> pageList;


	public RedpacketDto() {

	}

	public Short getRedpacketType() {
		return redpacketType;
	}

	public void setRedpacketType(Short redpacketType) {
		this.redpacketType = redpacketType;
	}

	public Short getRedpacketStatus() {
		return redpacketStatus;
	}

	public void setRedpacketStatus(Short redpacketStatus) {
		this.redpacketStatus = redpacketStatus;
	}

	public Integer getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(Integer currentPage) {
		this.currentPage = currentPage;
	}

	public BigDecimal getTotalCashAmount() {
		return totalCashAmount;
	}

	public void setTotalCashAmount(BigDecimal totalCashAmount) {
		this.totalCashAmount = totalCashAmount;
	}

	public String getRedpacketId() {
		return redpacketId;
	}

	public void setRedpacketId(String redpacketId) {
		this.redpacketId = redpacketId;
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

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}

	public BigDecimal getGrabAmount() {
		return grabAmount;
	}

	public void setGrabAmount(BigDecimal grabAmount) {
		this.grabAmount = grabAmount;
	}

	public Integer getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(Integer totalCount) {
		this.totalCount = totalCount;
	}

	public Integer getGrabedCount() {
		return grabedCount;
	}

	public void setGrabedCount(Integer grabedCount) {
		this.grabedCount = grabedCount;
	}

	public String getGrabAllTimes() {
		return grabAllTimes;
	}

	public void setGrabAllTimes(String grabAllTimes) {
		this.grabAllTimes = grabAllTimes;
	}

	public Short getIsGrab() {
		return isGrab;
	}

	public void setIsGrab(Short isGrab) {
		this.isGrab = isGrab;
	}

	public T getGrabedUser() {
		return grabedUser;
	}

	public void setGrabedUser(T grabedUser) {
		this.grabedUser = grabedUser;
	}

	public List<T> getPageList() {
		return pageList;
	}

	public void setPageList(List<T> pageList) {
		this.pageList = pageList;
	}
}
