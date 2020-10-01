package com.taiyiyun.passport.po;

/**
 * Created by nina on 2018/1/26.
 */
public class PublicHeartBeat {
    private String userId;//用户ID
    private Long updateTime;//过期时间

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }
}
