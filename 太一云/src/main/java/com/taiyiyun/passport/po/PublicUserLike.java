package com.taiyiyun.passport.po;

import java.sql.Timestamp;

/**
 * Created by okdos on 2017/6/29.
 */
public class PublicUserLike {

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

    public String getLikeId() {
        return likeId;
    }

    public void setLikeId(String likeId) {
        this.likeId = likeId;
    }

    public Timestamp getFocusTime() {
        return focusTime;
    }

    public void setFocusTime(Timestamp focusTime) {
        this.focusTime = focusTime;
    }

    long id;
    String userId;
    String likeId;
    Timestamp focusTime;
}
