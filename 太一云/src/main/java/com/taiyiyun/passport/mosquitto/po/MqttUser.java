package com.taiyiyun.passport.mosquitto.po;

import com.taiyiyun.passport.consts.Config;

/**
 * Created by okdos on 2017/7/3.
 */
public class MqttUser {

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPw() {
        return pw;
    }

    public void setPw(String pw) {
        this.pw = pw;
    }

    public String getSup() {
        return sup;
    }

    public void setSup(String sup) {
        this.sup = sup;
    }


    private String username;
    private String pw;
    private String sup;
}
