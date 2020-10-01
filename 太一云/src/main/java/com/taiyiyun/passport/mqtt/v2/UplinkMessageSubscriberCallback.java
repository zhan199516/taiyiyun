package com.taiyiyun.passport.mqtt.v2;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.taiyiyun.passport.init.SpringContext;
import com.taiyiyun.passport.mongo.service.impl.MessageHeartBeatServiceImpl;
import com.taiyiyun.passport.mqtt.Message;
import com.taiyiyun.passport.mqtt.MessageContentType;
import com.taiyiyun.passport.util.TypeRef;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by nina on 2018/3/14.
 */
public class UplinkMessageSubscriberCallback implements MqttCallback{

    private UplinkMessageSubscriber uplinkMessageSubscriber;

    public UplinkMessageSubscriberCallback(UplinkMessageSubscriber uplinkMessageSubscriber) {
        this.uplinkMessageSubscriber = uplinkMessageSubscriber;
    }

    public final Logger logger = LoggerFactory.getLogger(getClass());


    @Override
    public void connectionLost(Throwable cause) {
        logger.error("客户端：" + uplinkMessageSubscriber.getMqttClient().getClientId() + "连接丢失，尝试重连。", cause);
        uplinkMessageSubscriber.reconnect();
    }

    private static Type messageType = new TypeRef<Message<JSONObject>>(){}.getType();

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String payLoad = new String(message.getPayload(), "UTF-8");
        logger.info("接收到消息：\r\n" +  "主题：" + topic + "\n内容：" + payLoad + "\n");
        List<MessageContentType> messageContentTypes = null;
        try {
            Type type = new TypeRef<ArrayList<MessageContentType>>(){}.getType();
            messageContentTypes = JSON.parseObject(payLoad, type);
        } catch (Exception e) {
            logger.error("不符合标准格式的消息数据，不处理。消息：" + payLoad);
            return;
        }
        if (null == messageContentTypes || messageContentTypes.size() == 0) {
            logger.error("消息：" + payLoad + "转换JSON为Java对象失败。");
            return;
        }
        JSONArray jsonArray = JSON.parseArray(payLoad);
        for(int i = 0; i < messageContentTypes.size(); ++i) {
            Message.ContentType contentType = Message.getContentType(messageContentTypes.get(i).getContentType());
            Message<JSONObject> msg = null;
            try {
                msg = JSON.parseObject(jsonArray.get(i).toString(), messageType);
            } catch (Exception e) {
                logger.error("Can not cast to " + messageType + ", data = " + jsonArray.get(i).toString(), e);
            }
            if(contentType != null && contentType.getValue()== 1101) {//处理心跳消息
                if(msg != null) {
                    MessageHeartBeatServiceImpl hbService = SpringContext.getBean(MessageHeartBeatServiceImpl.class);
                    hbService.receiveMessage(msg);
                }
            } else {
                boolean isMatchTopic = Pattern.matches("^uplink/.*", topic);
                if(isMatchTopic) {
                    UplinkMessageHandler.getInstance().handleUplinkMessage(payLoad, topic, msg, message.getId(), message.getQos());
                }
            }
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }

}
