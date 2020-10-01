package com.taiyiyun.passport.po;

import java.sql.Timestamp;

/**
 * Created by okdos on 2017/8/7.
 */
public class PublicUserBlock {

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getBlockId() {
        return blockId;
    }

    public void setBlockId(String blockId) {
        this.blockId = blockId;
    }

    public Timestamp getBlockTime() {
        return blockTime;
    }

    public void setBlockTime(Timestamp blockTime) {
        this.blockTime = blockTime;
    }

    long id;
    String userId;
    String blockId;
    Timestamp blockTime;
}
