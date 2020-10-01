package com.taiyiyun.passport.bean;

import com.alibaba.fastjson.annotation.JSONField;
import com.taiyiyun.passport.consts.Config;

/**
 * Created by okdos on 2017/7/8.
 */
public class MqttClientSetting {
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




    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public int getKeepalive() {
        return keepalive;
    }

    public int getClean() {
        return clean;
    }

    public int getAuth() {
        return auth;
    }

    public int getTls() {
        return tls;
    }

    public void setTls(int tls) {
        this.tls = tls;
    }

    public int getSubQos() {
        return subQos;
    }

    public void setSubQos(int subQos) {
        this.subQos = subQos;
    }

    public int getPubQos() {
        return pubQos;
    }

    public void setPubQos(int pubQos) {
        this.pubQos = pubQos;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setKeepalive(int keepalive) {
        this.keepalive = keepalive;
    }

    public void setClean(int clean) {
        this.clean = clean;
    }

    public void setAuth(int auth) {
        this.auth = auth;
    }

    public int getHeartbeat() {
        return heartbeat;
    }

    public void setHeartbeat(int heartbeat) {
        this.heartbeat = heartbeat;
    }

    private String username;
	@JSONField(name = "password")
	private String pw;
	private String sup;

    private String host = Config.get("mosquitto.user.host");
    private int port = Config.getInt("mosquitto.user.port", 1884);
    private int keepalive = Config.getInt("mosquitto.user.keepalive", 60);
    private int clean = Config.getInt("mosquitto.user.clean", 1);
    private int auth = Config.getInt("mosquitto.user.auth", 1);
    private int tls = Config.getInt("mosquitto.user.tls", 0);
    private int subQos = Config.getInt("mosquitto.user.subQos", 1);
    private int pubQos = Config.getInt("mosquitto.user.pubQos", 0);
    private int heartbeat = Config.getInt("mqtt.heartbeat", 300);
}
