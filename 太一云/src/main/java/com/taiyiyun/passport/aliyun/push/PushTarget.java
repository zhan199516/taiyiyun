package com.taiyiyun.passport.aliyun.push;

public enum PushTarget {

	DEVICE("DEVICE", "设备"),
	
	ACCOUNT("ACCOUNT", "账户"),
	
	TAG("TAG", "自定义标签"),
	
	ALL("ALL", "全部");

	private String value;
	
	private String displayName;

	PushTarget(String value, String displayName) {
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
