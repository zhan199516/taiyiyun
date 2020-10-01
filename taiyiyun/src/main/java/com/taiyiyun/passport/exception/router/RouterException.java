package com.taiyiyun.passport.exception.router;

import com.taiyiyun.passport.exception.PassportException;

import java.util.List;

/**
 * Created by nina on 2018/1/29.
 */
public class RouterException extends PassportException{

    public RouterException(String code, String message, List<Object> arguments) {
        super(code, message, arguments);
    }
}
