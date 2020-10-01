package com.taiyiyun.passport.mqtt.v2;

import com.taiyiyun.passport.mqtt.Message;

/**
 * Created by nina on 2018/3/19.
 */
public interface IMessageForwardService {
    void receiveMessage(String topic, Message<?> message, int mqMessageId, int mqQos);
}
