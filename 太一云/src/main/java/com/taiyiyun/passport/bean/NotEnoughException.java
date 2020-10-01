package com.taiyiyun.passport.bean;

public class NotEnoughException extends RuntimeException {

	private static final long serialVersionUID = 2012306254891907705L;
	
	public NotEnoughException() {
		
	}
	
	public NotEnoughException(String msg) {
		super(msg);
	}
	

}
