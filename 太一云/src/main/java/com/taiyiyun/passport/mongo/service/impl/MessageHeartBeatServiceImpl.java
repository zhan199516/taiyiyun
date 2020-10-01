package com.taiyiyun.passport.mongo.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.taiyiyun.passport.dao.IPublicHeartBeatDao;
import com.taiyiyun.passport.mongo.service.IMessageHeartBeatService;
import com.taiyiyun.passport.mqtt.Message;
import com.taiyiyun.passport.po.PublicHeartBeat;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by nina on 2018/1/26.
 */
@Service
public class MessageHeartBeatServiceImpl implements IMessageHeartBeatService {

    @Resource
    private IPublicHeartBeatDao hbDao;

    public void receiveMessage(Message<JSONObject> message) {
        Message.MessageType messageType = Message.getMessageType(message.getMessageType());
        if (null != message && null != message.getMessageType() && null != messageType) {
            System.out.println("接收到心跳消息信息=================");
            PublicHeartBeat heartBeat = new PublicHeartBeat();
            heartBeat.setUserId(message.getFromUserId());
            heartBeat.setUpdateTime(System.currentTimeMillis());
            hbDao.save(heartBeat);
        }
    }

}
