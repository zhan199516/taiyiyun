package com.taiyiyun.passport.aliyun.push;

/**
 * Created by nina on 2018/4/8.
 */
public enum AndroidNotifyType {

    VIBRATE("VIBRATE", "振动"),//默认值
    SOUND("SOUND", "声音"),
    BOTH("BOTH", "声音和振动"),
    NONE("NONE", "静音");



    private String value;
    private String description;

    AndroidNotifyType(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
}
