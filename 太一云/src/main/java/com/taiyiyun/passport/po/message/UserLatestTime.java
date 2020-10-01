package com.taiyiyun.passport.po.message;

import java.sql.Timestamp;

/**
 * Created by okdos on 2017/6/27.
 */
public class UserLatestTime {
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Timestamp getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Timestamp lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    String userId;
    Timestamp lastUpdateTime;
}
