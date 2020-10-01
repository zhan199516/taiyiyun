package com.taiyiyun.passport.transfer.Ask;

/**
 * Created by okdos on 2017/7/17.
 *  通用性的请求对象
 */
public class AskBody<T> {
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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

    public String getRelateId() {
        return relateId;
    }

    public void setRelateId(String relateId) {
        this.relateId = relateId;
    }

    private String appKey;
    private String appSecret;
    private String userKey;
    private String userSecretKey;
    private String code; //获取的code
    private String token; //获取的token
    private String coinBindUrl;
    private String coinCallUrl;
    private String relateId; //相关的id
}
