package com.taiyiyun.passport.mongo.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.DBObject;
import com.taiyiyun.passport.mongo.dao.IUplinkMessageLogDao;
import com.taiyiyun.passport.mongo.po.UplinkMessageLog;
import com.taiyiyun.passport.mongo.service.IUplinkMessageLogService;
import com.taiyiyun.passport.mqtt.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by nina on 2018/3/16.
 */
@Service
public class UplinkMessageLogServiceImpl implements IUplinkMessageLogService{

    @Resource
    private IUplinkMessageLogDao uplinkMessageLogDao;

    public final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void save(UplinkMessageLog uplinkMessageLog) {
        uplinkMessageLogDao.insert(uplinkMessageLog);
    }

    @Override
    public void receiveUplinkMessageLog(String topic, Message<JSONObject> message, int mqMessageId, int mqQos) {
        Message.MessageType messageType = Message.getMessageType(message.getMessageType());
        if (null != message.getMessageType() && null != messageType && null == message.getImmigrate()) {
            if(message.getUpdateTime() == null) {
                message.setUpdateTime(System.currentTimeMillis());  // 更新消息更新时间
            }
        }

        UplinkMessageLog log = new UplinkMessageLog();
        JSONObject jsonObject = message.getContent();
        if(jsonObject == null) {
            jsonObject = new JSONObject();
        }
        String strContent = jsonObject.toJSONString();
        DBObject obj = (DBObject) com.mongodb.util.JSON.parse(strContent);
        log.setContent(obj.toMap());
        log.setContent(obj.toMap());
        log.setContentType(message.getContentType());
        log.setFromClientId(message.getFromClientId());
        log.setFromUserId(message.getFromUserId());
        log.setMessageId(message.getMessageId());
        log.setMessageType(message.getMessageType());
        log.setPublishTime(message.getPublishTime());
        log.setSessionId(message.getSessionId());
        log.setSessionType(message.getSessionType());
        log.setUpdateTime(message.getUpdateTime());
        log.setVersion(message.getVersion());

        log.setIsRevoked(false);
        log.setTopic(topic);
        log.setId(Message.bulidUniquedId(topic, message));
        log.setLogTime(System.currentTimeMillis());
        log.setMqMessageId(mqMessageId);
        log.setMqQos(mqQos);
        if(null == findOne(log.getId())) {
            save(log);
        }
    }

    public UplinkMessageLog findOne(String id) {
        return uplinkMessageLogDao.findOne(id);
    }

    private void revokeMessage(String topic, Message<?> message) {
        String uplinkMessageLogId = Message.bulidUniquedId(topic, message);
        UplinkMessageLog uplinkMessageLog = uplinkMessageLogDao.findOne(uplinkMessageLogId);
        if(null != uplinkMessageLog) {
            updateUplinkMessageLogRevoked(uplinkMessageLogId, message.getMessageType());
        } else {
            logger.error("消息：" + message.toString() + "， 撤回失败。文章不存在。");
        }
    }

    private void updateUplinkMessageLogRevoked(String id, Integer messageType) {
        Map<String, Object> params = new HashMap<>();
        params.put("isRevoked", new Boolean(true));
        params.put("updateTime", System.currentTimeMillis());
        params.put("messageType", messageType);
        uplinkMessageLogDao.update(id, params);
    }

}
