package com.taiyiyun.passport.transfer.Answer;

/**
 * Created by okdos on 2017/7/17.
 * 获取code的返回信息
 */
public class AuthorizeResult extends ErrorCodeResult {


    public String getAuthorizationCode() {
        return authorizationCode;
    }

    public void setAuthorizationCode(String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }


    private String authorizationCode;

}
