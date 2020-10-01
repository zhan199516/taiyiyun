package com.taiyiyun.passport.po.circle;

import java.io.Serializable;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by okdos on 2017/6/28.
 */
public class Article implements Serializable{

	private static final long serialVersionUID = 8436862410359932977L;

	@JSONField(name = "ArticleId")
	private String articleId;

	@JSONField(name = "PublicCircleID")
	private String publicCircleID;

	@JSONField(name = "PublicCircleName")
	private String publicCircleName;

	@JSONField(name = "PublicCircleImg")
	private String publicCircleImg;

	@JSONField(name = "PushTitle")
	private String pushTitle;

	@JSONField(name = "Signature")
	private String signature;

	@JSONField(name = "SendMQTime")
	private Long sendMQTime;

	@JSONField(name = "Type")
	private Integer type = 1;

	@JSONField(name = "Version")
	private int version = 1;

	@JSONField(name = "status")
	private Integer status;

	@JSONField(name = "Content")
	private ArticleContent content;

	@JSONField(name = "Index")
	private Integer index;

	@JSONField(name = "IsOriginal")
	private Boolean isOriginal;

	@JSONField(name = "ReadCount")
	private Integer readCount = 0;

	@JSONField(name = "CommentCount")
	private Integer commentCount = 0;

	@JSONField(name = "UpCount")
	private Integer upCount = 0;

	@JSONField(name = "DownCount")
	private Integer downCount = 0;

	@JSONField(name = "ShareCount")
	private Integer shareCount = 0;
	
	@JSONField(name = "ArticleHash")
	private String articleHash;

	public Boolean getIsOriginal() {
		return isOriginal;
	}

	public void setIsOriginal(Boolean isOriginal) {
		this.isOriginal = isOriginal;
	}

	public Integer getReadCount() {
		return readCount;
	}

	public void setReadCount(Integer readCount) {
		this.readCount = readCount;
	}

	public Integer getCommentCount() {
		return commentCount;
	}

	public void setCommentCount(Integer commentCount) {
		this.commentCount = commentCount;
	}

	public Integer getUpCount() {
		return upCount;
	}

	public void setUpCount(Integer upCount) {
		this.upCount = upCount;
	}

	public Integer getDownCount() {
		return downCount;
	}

	public void setDownCount(Integer downCount) {
		this.downCount = downCount;
	}

	public Integer getShareCount() {
		return shareCount;
	}

	public void setShareCount(Integer shareCount) {
		this.shareCount = shareCount;
	}

	public String getPublicCircleImg() {
		return publicCircleImg;
	}

	public void setPublicCircleImg(String publicCircleImg) {
		this.publicCircleImg = publicCircleImg;
	}

	public String getPublicCircleID() {
		return publicCircleID;
	}

	public void setPublicCircleID(String publicCircleID) {
		this.publicCircleID = publicCircleID;
	}

	public String getPublicCircleName() {
		return publicCircleName;
	}

	public void setPublicCircleName(String publicCircleName) {
		this.publicCircleName = publicCircleName;
	}

	public String getPushTitle() {
		return pushTitle;
	}

	public void setPushTitle(String pushTitle) {
		this.pushTitle = pushTitle;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public Long getSendMQTime() {
		return sendMQTime;
	}

	public void setSendMQTime(Long sendMQTime) {
		this.sendMQTime = sendMQTime;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getIndex() {
		return index;
	}

	public void setIndex(Integer index) {
		this.index = index;
	}

	public String getArticleId() {
		return articleId;
	}

	public void setArticleId(String articleId) {
		this.articleId = articleId;
	}

	public ArticleContent getContent() {
		return content;
	}

	public void setContent(ArticleContent content) {
		this.content = content;
	}

	public String getArticleHash() {
		return articleHash;
	}

	public void setArticleHash(String articleHash) {
		this.articleHash = articleHash;
	}

}
