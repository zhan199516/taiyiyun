package com.taiyiyun.passport.transfer.po;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by okdos on 2017/7/14.
 */
public class BindEntity {
    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public String getUserSecretKey() {
        return userSecretKey;
    }

    public void setUserSecretKey(String userSecretKey) {
        this.userSecretKey = userSecretKey;
    }

    public String getUniqueKey() {
        return uniqueKey;
    }

    public void setUniqueKey(String uniqueKey) {
        this.uniqueKey = uniqueKey;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public Long getBindTime() {
        return bindTime;
    }

    public void setBindTime(Long bindTime) {
        this.bindTime = bindTime;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getUniqueAddress() {
        return uniqueAddress;
    }

    public void setUniqueAddress(String uniqueAddress) {
        this.uniqueAddress = uniqueAddress;
    }

    @JsonProperty("user_key")
    @JSONField(name="user_key")
    private String userKey;
    @JsonProperty("user_secret_key")
    @JSONField(name="user_secret_key")
    private String userSecretKey;
    @JsonProperty("unique_key")
    @JSONField(name="unique_key")
    private String uniqueKey;
    @JsonProperty("phone")
    @JSONField(name="phone")
    private String uniqueAddress;
    @JsonProperty("app_key")
    @JSONField(name="app_key")
    private String appKey;
    @JsonProperty("bindTime")
    @JSONField(name="bindTime")
    private Long bindTime;
    private String uuid;
    private String sign;
}
