package com.taiyiyun.passport.aliyun.push;

public enum PushDeviceType {

	IOS("IOS", "IOS设备"),

	ANDROID("ANDROID", "Android设备"),

	ALL("ALL", "全部设备");

	private String value;

	private String displayName;

	PushDeviceType(String value, String displayName) {
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
