package com.taiyiyun.passport.aliyun.push;

public enum AndroidOpenType {

	APP("APPLICATION", "打开应用"),
	
	ACTIVITY("ACTIVITY", "打开应用Activity"),
	
	URL("URL", "打开URL"),
	
	NONE("NONE", "无跳转逻辑");

	private String value;

	private String displayName;

	AndroidOpenType(String value, String displayName) {
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
