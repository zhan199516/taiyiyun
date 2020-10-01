package com.taiyiyun.passport.bean;

public class TransferFailedException extends RuntimeException {

	private static final long serialVersionUID = 7733796711675663361L;
	
	public TransferFailedException() {
		
	}
	
	public TransferFailedException(String msg) {
		super(msg);
	}

}
