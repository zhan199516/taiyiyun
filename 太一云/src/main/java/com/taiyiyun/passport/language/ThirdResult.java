package com.taiyiyun.passport.language;

/**
 * Created by okdos on 2017/9/6.
 */
public class ThirdResult {
    private boolean success;
    private String error;
    private int errorCode;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }
}
