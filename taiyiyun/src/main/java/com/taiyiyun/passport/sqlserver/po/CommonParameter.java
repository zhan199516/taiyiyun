package com.taiyiyun.passport.sqlserver.po;

/**
 * Created by nina on 2017/10/25.
 */
public class CommonParameter {
    /*应用公钥*/
    private String appKey;
    /*请求签名*/
    private String sign;

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }
}
