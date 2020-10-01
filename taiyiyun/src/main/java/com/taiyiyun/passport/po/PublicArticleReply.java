package com.taiyiyun.passport.po;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by okdos on 2017/6/29.
 */
public class PublicArticleReply {

    public long getReplyId() {
        return replyId;
    }

    public void setReplyId(long replyId) {
        this.replyId = replyId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
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

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDeleteOperator() {
        return deleteOperator;
    }

    public void setDeleteOperator(String deleteOperator) {
        this.deleteOperator = deleteOperator;
    }

    public String getDeleteStatus() {
        return deleteStatus;
    }

    public void setDeleteStatus(String deleteStatus) {
        this.deleteStatus = deleteStatus;
    }

    public String getDeleteTime() {
        return deleteTime;
    }

    public void setDeleteTime(String deleteTime) {
        this.deleteTime = deleteTime;
    }


    public List<PublicArticleReply> getReplies() {
        return replies;
    }

    public void setReplies(List<PublicArticleReply> replies) {
        this.replies = replies;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPicture() {
        return userPicture;
    }

    public void setUserPicture(String userPicture) {
        this.userPicture = userPicture;
    }

    private long replyId;
    private Long parentId;
    private String articleId;
    private String userId;
    private Timestamp createTime;
    private String comment;
    private String deleteOperator;
    private String deleteStatus;
    private String deleteTime;

    private String userName;
    private String userPicture;

    private List<PublicArticleReply> replies = new ArrayList<>();
}
