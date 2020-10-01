package com.taiyiyun.passport.transfer.Answer;

/**
 * Created by okdos on 2017/7/17.
 */
public class ErrorCodeResult {

    public Integer getErrorCode() {
        if(errorCode == null){
            return 0;
        } else {
            return errorCode;
        }
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    private Integer errorCode;

    public boolean isSuccess(){
        return this.errorCode == null;
    }
}
