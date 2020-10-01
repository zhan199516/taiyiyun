package com.taiyiyun.passport.po;


/**
 * Created by okdos on 2017/7/8.
 */
public class MqttUserPWD
{
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMqttPwd() {
        return mqttPwd;
    }

    public void setMqttPwd(String mqttPwd) {
        this.mqttPwd = mqttPwd;
    }

    public String getMqttPwdEncode() {
        return mqttPwdEncode;
    }

    public void setMqttPwdEncode(String mqttPwdEncode) {
        this.mqttPwdEncode = mqttPwdEncode;
    }

    public Long getMqttPwdExpireTime() {
        return mqttPwdExpireTime;
    }

    public void setMqttPwdExpireTime(Long mqttPwdExpireTime) {
        this.mqttPwdExpireTime = mqttPwdExpireTime;
    }

    private String userId;
    private String mqttPwd;
    private String mqttPwdEncode;
    private Long mqttPwdExpireTime;
}
