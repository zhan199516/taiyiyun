package com.taiyiyun.passport.po;

public class WeiConfigRes {
    public String appId;
    public Long timestamp;
    public String nonceStr;
    public String signature;
    public String reqestUrl;


    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getNonceStr() {
        return nonceStr;
    }

    public void setNonceStr(String nonceStr) {
        this.nonceStr = nonceStr;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getReqestUrl() {
        return reqestUrl;
    }

    public void setReqestUrl(String reqestUrl) {
        this.reqestUrl = reqestUrl;
    }
}
