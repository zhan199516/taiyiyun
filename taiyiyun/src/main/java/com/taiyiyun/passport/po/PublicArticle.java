package com.taiyiyun.passport.po;

import java.io.Serializable;
import java.util.Date;

public class PublicArticle implements Serializable {
	
	private static final long serialVersionUID = -8626161574134424242L;

	private String articleId;
	private String userId;
	private String title;
	private Date publishTime;
	private Date updateTime;
	private String chainAddress;
	private Date chainTime;
	private int contentType;
	private Boolean isOriginal;
	private String registerNo;
	private String thumbImg;
	private String content;
	private String summary;
	private String articleHash;
	private String forwardFrom;
	private String userName;
	private String userAvatarUrl;
	private int onlineStatus;
	private String clientId;
	private Integer topLevel;

	public Integer getTopLevel() {
		return topLevel;
	}

	public void setTopLevel(Integer topLevel) {
		this.topLevel = topLevel;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getArticleId() {
		return articleId;
	}

	public void setArticleId(String articleId) {
		this.articleId = articleId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Date getPublishTime() {
		return publishTime;
	}

	public void setPublishTime(Date publishTime) {
		this.publishTime = publishTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public String getChainAddress() {
		return chainAddress;
	}

	public void setChainAddress(String chainAddress) {
		this.chainAddress = chainAddress;
	}

	public Date getChainTime() {
		return chainTime;
	}

	public void setChainTime(Date chainTime) {
		this.chainTime = chainTime;
	}

	public int getContentType() {
		return contentType;
	}

	public void setContentType(int contentType) {
		this.contentType = contentType;
	}

	public Boolean getIsOriginal() {
		return isOriginal;
	}

	public void setIsOriginal(Boolean isOriginal) {
		this.isOriginal = isOriginal;
	}

	public String getThumbImg() {
		return thumbImg;
	}

	public void setThumbImg(String thumbImg) {
		this.thumbImg = thumbImg;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}


	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserAvatarUrl() {
		return userAvatarUrl;
	}

	public void setUserAvatarUrl(String userAvatarUrl) {
		this.userAvatarUrl = userAvatarUrl;
	}

	public int getOnlineStatus() {
		return onlineStatus;
	}

	public void setOnlineStatus(int onlineStatus) {
		this.onlineStatus = onlineStatus;
	}

	public String getArticleHash() {
		return articleHash;
	}

	public void setArticleHash(String articleHash) {
		this.articleHash = articleHash;
	}

	public String getRegisterNo() {
		return registerNo;
	}

	public void setRegisterNo(String registerNo) {
		this.registerNo = registerNo;
	}

	public String getForwardFrom() {
		return forwardFrom;
	}

	public void setForwardFrom(String forwardFrom) {
		this.forwardFrom = forwardFrom;
	}
	
}
