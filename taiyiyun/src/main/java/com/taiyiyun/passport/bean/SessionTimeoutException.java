package com.taiyiyun.passport.bean;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "登陆超时")
public class SessionTimeoutException extends RuntimeException {

	private static final long serialVersionUID = 4542586114733352502L;

}
