package com.taiyiyun.passport.aliyun.push;

public enum PushType {
	
	MESSAGE("MESSAGE", "消息"),
	
	NOTICE("NOTICE", "通知");
	
	private String value;

	private String displayName;

	PushType(String value, String displayName) {
		this.value = value;
		this.displayName = displayName;
	}

	public String getValue() {
		return value;
	}

	public String getDisplayName() {
		return displayName;
	}
	
}
