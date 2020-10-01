package com.taiyiyun.passport.po.message;

import java.io.Serializable;

/**
 * Created by okdos on 2017/6/27.
 * 共享号系统消息
 */
public class LatestTime implements Serializable {
    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    long lastUpdateTime;
}
