package com.taiyiyun.passport.exception;

import java.util.List;

/**
 * Created by nina on 2017/12/20.
 */
public class PassportException extends RuntimeException {
    private String code;
    private String message;
    private List<Object> arguments;

    public PassportException(String code, String message, List<Object> arguments) {
        super(message);
        this.code = code;
        this.message = message;
        this.arguments = arguments;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<Object> getArguments() {
        return arguments;
    }

}
