package com.taiyiyun.passport.sms;

/**
 * Created by nina on 2017/9/26.
 */
public class ShortMessage {

    private int type;//0-国内；1-国际
    private String mobile;
    private String text;

    public ShortMessage() {}

    public ShortMessage(int type, String mobile, String text) {
        this.type = type;
        this.mobile = mobile;
        this.text = text;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
