package com.taiyiyun.passport.mqtt.v2;

import com.taiyiyun.passport.mqtt.Message;

/**
 * Created by nina on 2018/3/15.
 */
public class DownLinkForwardJob implements DownLinkJob{

    String topic;
    String payload;
    Message<?> message;
    private int mqMessageId;
    private int mqQos;

    public DownLinkForwardJob(String payload, String topic, Message<?> message, int mqMessageId, int mqQos) {
        this.topic = topic;
        this.payload = payload;
        this.message = message;
        this.mqMessageId = mqMessageId;
        this.mqQos = mqQos;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public Message<?> getMessage() {
        return message;
    }

    public void setMessage(Message<?> message) {
        this.message = message;
    }

    public int getMqMessageId() {
        return mqMessageId;
    }

    public void setMqMessageId(int mqMessageId) {
        this.mqMessageId = mqMessageId;
    }

    public int getMqQos() {
        return mqQos;
    }

    public void setMqQos(int mqQos) {
        this.mqQos = mqQos;
    }
}
