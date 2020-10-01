package com.taiyiyun.passport.po;

/**
 * Created by okdos on 2017/6/30.
 */
public class PublicArticleReplyDel {
    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

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

    private String operator;
    private long replyId;
    private Long parentId;
}
