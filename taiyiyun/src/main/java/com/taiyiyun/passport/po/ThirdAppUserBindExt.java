package com.taiyiyun.passport.po;

/**
 * Created by okdos on 2017/7/14.
 * 包含key和secret的数据
 */
public class ThirdAppUserBindExt extends ThirdAppUserBind {
    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getAppSecret() {
        return appSecret;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }


    public String getCoinBindUrl() {
        return coinBindUrl;
    }

    public void setCoinBindUrl(String coinBindUrl) {
        this.coinBindUrl = coinBindUrl;
    }

    public String getCoinCallUrl() {
        return coinCallUrl;
    }

    public void setCoinCallUrl(String coinCallUrl) {
        this.coinCallUrl = coinCallUrl;
    }

    private String appKey;
    private String appSecret;
    private String appName;
    private String logoUrl;
    private String coinBindUrl;
    private String coinCallUrl;
}
