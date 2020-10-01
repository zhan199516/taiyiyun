package com.taiyiyun.passport.transfer.Answer;

/**
 * Created by okdos on 2017/7/17.
 * 获取token的返回信息
 */
public class TokenGetResult extends  ErrorCodeResult{

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public String getUser_key() {
        return user_key;
    }

    public void setUser_key(String user_key) {
        this.user_key = user_key;
    }

    private String access_token;
    private String user_key;

}
