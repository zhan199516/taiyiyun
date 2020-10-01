package com.taiyiyun.passport.commons;

/**
 * Created by zhangjun on 2018/1/10.
 */
public class ResultData<T> {
    /*业务状态*/
    private Integer status;
    /*业务信息*/
    private String message;
    /*业务结果数*/
    private T data;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
