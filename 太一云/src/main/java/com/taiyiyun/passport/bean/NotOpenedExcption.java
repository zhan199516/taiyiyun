package com.taiyiyun.passport.bean;

public class NotOpenedExcption  extends RuntimeException{

	private static final long serialVersionUID = -983235151894151366L;
	
	public NotOpenedExcption() {
		
	}
	
	public NotOpenedExcption(String msg) {
		super(msg);
	}

}
