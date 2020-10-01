package com.taiyiyun.passport.mongo.service;

import com.alibaba.fastjson.JSONObject;
import com.taiyiyun.passport.mongo.po.UplinkMessageLog;
import com.taiyiyun.passport.mqtt.Message;

/**
 * Created by nina on 2018/3/16.
 */
public interface IUplinkMessageLogService {

    void save(UplinkMessageLog uplinkMessageLog);
    void receiveUplinkMessageLog(String topic, Message<JSONObject> message, int mqMessageId, int mqQos);
}
