package com.taiyiyun.passport.po;

import java.sql.Timestamp;

/**
 * Created by okdos on 2017/6/29.
 */
public class PublicArticleLevel {


    public long getLevelId() {
        return levelId;
    }

    public void setLevelId(long levelId) {
        this.levelId = levelId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getLikeLevel() {
        return likeLevel;
    }

    public void setLikeLevel(int likeLevel) {
        this.likeLevel = likeLevel;
    }

    public String getArticleId() {
        return articleId;
    }

    public void setArticleId(String articleId) {
        this.articleId = articleId;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    private long levelId;
    private String userId;
    private int likeLevel;
    private String articleId;
    private Timestamp createTime;
}
