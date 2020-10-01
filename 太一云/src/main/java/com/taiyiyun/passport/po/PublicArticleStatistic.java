package com.taiyiyun.passport.po;

import java.util.Date;

/**
 * Created by okdos on 2017/6/29.
 */
public class PublicArticleStatistic {
	private String userId;
	private String articleId;
	private Integer readCount = 0;
	private Integer upCount = 0;
	private Integer downCount = 0;
	private Integer forwardCount = 0;
	private Integer replyCount = 0;
	private Date createTime;

	public String getArticleId() {
		return articleId;
	}

	public void setArticleId(String articleId) {
		this.articleId = articleId;
	}

	public Integer getReadCount() {
        return readCount;
    }

    public void setReadCount(Integer readCount) {
        this.readCount = readCount;
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

    public Integer getForwardCount() {
        return forwardCount;
    }

    public void setForwardCount(Integer forwardCount) {
        this.forwardCount = forwardCount;
    }

    public Integer getReplyCount() {
        return replyCount;
    }

    public void setReplyCount(Integer replyCount) {
        this.replyCount = replyCount;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

}
