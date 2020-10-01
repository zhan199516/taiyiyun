package com.taiyiyun.passport.po;


import java.sql.Timestamp;

/**
 * Created by okdos on 2017/7/14.
 */
public class ThirdAppUserBind {

    private long id;
    private String uuid;
    private String appId;
    private String uniqueKey;
    private String uniqueAddress;
    private Long bindTime;
    private String userKey;
    private String userSecretKey;
    private String accessToken;
    private Timestamp accessTokenExpireTime;
    private Timestamp bindExpireTime;
    private Integer bindStatus;
    private String thirdpartAppkey;

    public boolean isBinded(){
        return bindStatus != null && bindStatus == 1;
    }

    public String getUniqueAddress() {
        return uniqueAddress;
    }

    public void setUniqueAddress(String uniqueAddress) {
        this.uniqueAddress = uniqueAddress;
    }

    public String getThirdpartAppkey() {
        return thirdpartAppkey;
    }

    public void setThirdpartAppkey(String thirdpartAppkey) {
        this.thirdpartAppkey = thirdpartAppkey;
    }

    public Integer getBindStatus() {
        return bindStatus;
    }

    public void setBindStatus(Integer bindStatus) {
        this.bindStatus = bindStatus;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getUniqueKey() {
        return uniqueKey;
    }

    public void setUniqueKey(String uniqueKey) {
        this.uniqueKey = uniqueKey;
    }

    public Long getBindTime() {
        return bindTime;
    }

    public void setBindTime(Long bindTime) {
        this.bindTime = bindTime;
    }

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

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Timestamp getAccessTokenExpireTime() {
        return accessTokenExpireTime;
    }

    public void setAccessTokenExpireTime(Timestamp accessTokenExpireTime) {
        this.accessTokenExpireTime = accessTokenExpireTime;
    }

    public Timestamp getBindExpireTime() {
        return bindExpireTime;
    }

    public void setBindExpireTime(Timestamp bindExpireTime) {
        this.bindExpireTime = bindExpireTime;
    }

}
