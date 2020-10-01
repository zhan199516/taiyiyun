package com.taiyiyun.passport.sqlserver.po;

import java.util.Date;

/**
 * Created by nina on 2017/10/25.
 */
public class Developer {
    /*应用标识*/
    private String appKey;
    /*应用名称*/
    private String appName;
    /*用户密钥*/
    private String appSecret;
    /*状态，0：正常，1：禁用*/
    private int status;
    /*创建时间*/
    private Date creationTime;
    /*用户ID*/
    private String uuid;
    /*应用编号*/
    private Integer appId;
    /*上线时间*/
    private Date onlineTime;
    /*下线时间*/
    private Date downTime;

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppSecret() {
        return appSecret;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Integer getAppId() {
        return appId;
    }

    public void setAppId(Integer appId) {
        this.appId = appId;
    }

    public Date getOnlineTime() {
        return onlineTime;
    }

    public void setOnlineTime(Date onlineTime) {
        this.onlineTime = onlineTime;
    }

    public Date getDownTime() {
        return downTime;
    }

    public void setDownTime(Date downTime) {
        this.downTime = downTime;
    }
}
