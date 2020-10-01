package com.taiyiyun.passport.mqtt.v2;

import com.alibaba.fastjson.JSONObject;
import com.taiyiyun.passport.mqtt.Message;

/**
 * Created by nina on 2018/3/21.
 */
public interface IMessageLogService {
    void receiveMessage(String topic, Message<JSONObject> message, int mqMessageId, int mqQos);
}
