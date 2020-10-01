package com.taiyiyun.passport.exception.group;

import com.taiyiyun.passport.exception.PassportException;

import java.util.List;

/**
 * Created by nina on 2017/12/20.
 */
public class GroupException extends PassportException{

    public GroupException(String code, String message, List<Object> arguments) {
        super(code, message, arguments);
    }
}
