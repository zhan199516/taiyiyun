package com.taiyiyun.passport.mqtt.v2;

import com.alibaba.fastjson.JSONObject;
import com.taiyiyun.passport.dao.IPublicUplinkmessageLogFailDao;
import com.taiyiyun.passport.init.SpringContext;
import com.taiyiyun.passport.mongo.service.impl.UplinkMessageLogServiceImpl;
import com.taiyiyun.passport.mqtt.Message;
import com.taiyiyun.passport.po.PublicUplinkmessageLogFail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Created by nina on 2018/3/16.
 */
public final class UplinkMessageHandler {

    public static final Logger logger = LoggerFactory.getLogger(UplinkMessageHandler.class);

    public boolean handleUplinkMessage(String payload, String topic, Message<JSONObject> msg, int mqMessageId, int mqQos) {
        System.out.println("处理监听到的消息====================\r\n");
        System.out.println("主题：" + topic + "\r\n");
        System.out.println("消息体：\r\n" + msg.toString() + "\r\n");
        boolean flag = false;
        try {
            if(msg != null) {
                //更新updateTime时间
                msg.setUpdateTime(System.currentTimeMillis());
                //记录uplink日志
                UplinkMessageLogServiceImpl uplinkMessageLogService = SpringContext.getBean(UplinkMessageLogServiceImpl.class);
                uplinkMessageLogService.receiveUplinkMessageLog(topic, msg, mqMessageId, mqQos);
                //发起downLink日志任务
                DownLinkLogJob downLinkLogJob = new DownLinkLogJob(payload, topic, msg, mqMessageId, mqQos);
                DownLinkJobManager.getInstance().addDownLinkLogJob(downLinkLogJob);
                //发起downLink转发任务
                DownLinkForwardJob downLinkForwardJob = new DownLinkForwardJob(payload, topic, msg, mqMessageId, mqQos);
                DownLinkJobManager.getInstance().addDownLinkForwardJob(downLinkForwardJob);
                //向发消息者返回消息（要排除系统发的）
                MessageForwardServiceImpl forwardService = SpringContext.getBean(MessageForwardServiceImpl.class);
                forwardService.forwardMessageForSender(topic, msg, mqMessageId, mqQos);
                flag = true;
            }
        } catch (Exception e) {
            PublicUplinkmessageLogFail fail = new PublicUplinkmessageLogFail();
            try {
                Date now = new Date();
                //记录失败的uplink消息
                IPublicUplinkmessageLogFailDao uplinkmessageLogFailDao = SpringContext.getBean(IPublicUplinkmessageLogFailDao.class);
                fail.setCreateTime(now);
                fail.setErrMsg(e.getMessage());
                fail.setMessage(JSONObject.toJSONString(msg));
                fail.setMessageId(msg.getMessageId());
                fail.setTopic(topic);
                fail.setStatus(0);
                fail.setUpdateTime(now);
                uplinkmessageLogFailDao.insertSelective(fail);
            } catch (Exception ex) {
                logger.error("记录监听到的uplink失败消息异常：" + ex.getMessage() + "\\r\\消息内容：" + JSONObject.toJSONString(fail));
            }
        }
        return flag;
    }

    public static UplinkMessageHandler getInstance() {
        return UplinkMessageHandlerFactory.UPLINK_MESSAGE_HANDLER;
    }

    private static class UplinkMessageHandlerFactory {
        static UplinkMessageHandler UPLINK_MESSAGE_HANDLER = new UplinkMessageHandler();
    }

}
