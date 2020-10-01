package com.taiyiyun.passport.transfer.po;

/**
 * Created by okdos on 2017/7/14.
 */
public class CallbackStatus {
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    private int status;
    private String error;
}
