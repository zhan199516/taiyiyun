package com.taiyiyun.passport.po;

/**
 * Created by okdos on 2017/9/20.
 */
public class PublicUserTop {
    private String userId;
    private Integer topType;
    private Integer topLevel;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getTopType() {
        return topType;
    }

    public void setTopType(Integer topType) {
        this.topType = topType;
    }

    public Integer getTopLevel() {
        return topLevel;
    }

    public void setTopLevel(Integer topLevel) {
        this.topLevel = topLevel;
    }
}
