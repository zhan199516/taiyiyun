package com.taiyiyun.passport.po;

/**
 * Created by okdos on 2017/6/24.
 */
public class BaseResult<T> {
    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getInternalError() {
        return internalError;
    }

    public void setInternalError(String internalError) {
        this.internalError = internalError;
    }

    private Integer status;
    private String error;
    private String internalError;
    private String apiName;
    private T data;
}
