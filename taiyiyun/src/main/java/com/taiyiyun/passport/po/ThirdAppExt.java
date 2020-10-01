package com.taiyiyun.passport.po;

/**
 * Created by okdos on 2017/7/14.
 * 第三方绑定处理
 */
public class ThirdAppExt extends ThirdApp {

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

    public String getCoinCallUrl() {
        return coinCallUrl;
    }

    public void setCoinCallUrl(String coinCallUrl) {
        this.coinCallUrl = coinCallUrl;
    }

    private String appKey;
    private String appSecret;
    private String coinCallUrl;
}
