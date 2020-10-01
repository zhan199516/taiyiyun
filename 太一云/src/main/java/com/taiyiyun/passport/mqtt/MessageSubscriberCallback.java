package com.taiyiyun.passport.mqtt;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.taiyiyun.passport.init.SpringContext;
import com.taiyiyun.passport.mongo.service.impl.MessageForwardServiceImpl;
import com.taiyiyun.passport.mongo.service.impl.MessageHeartBeatServiceImpl;
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

public class MessageSubscriberCallback implements MqttCallback {
	
	private MessageSubscriber messageSubscriber;
	
	public MessageSubscriberCallback(MessageSubscriber messageSubscriber) {
		this.messageSubscriber = messageSubscriber;
	}

	public final Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void connectionLost(Throwable cause) {
		//logger.info("客户端：" + messageSubscriber.getMqttClient().getClientId() + "等待schedule重连。", cause);
		logger.error("客户端：" + messageSubscriber.getMqttClient().getClientId() + "连接丢失，尝试重连。", cause);
		messageSubscriber.reconnect();
	}

	private static Type messageType = new TypeRef<Message<JSONObject>>(){}.getType();

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		String playload = new String(message.getPayload(), "UTF-8");
		logger.info("接收到消息：\n\n" +  "主题：" + topic + "\n内容：" + playload + "\n");
		List<MessageContentType> messageContentTypes = null;
		try {
			Type type = new TypeRef<ArrayList<MessageContentType>>(){}.getType();
			messageContentTypes = JSON.parseObject(playload, type);
		} catch (Exception e) {
			logger.error("不符合标准格式的消息数据，不处理。消息：" + playload);
			return;
		}

		if (null == messageContentTypes || messageContentTypes.size() == 0) {
			logger.error("消息：" + playload + "转换JSON为Java对象失败。");
			return;
		}

		JSONArray jsonArray = JSON.parseArray(playload);
		for (int i = 0; i < messageContentTypes.size(); ++i) {
			Message.ContentType contentType = Message.getContentType(messageContentTypes.get(i).getContentType());
			if(contentType.getValue()== 1101) {
				Message<JSONObject> msg = null;
				try {
					msg = JSON.parseObject(jsonArray.get(i).toString(), messageType);
				} catch (Exception e) {
					logger.error("Can not cast to " + messageType + ", data = " + jsonArray.get(i).toString(), e);
				}
				if(msg != null) {
					boolean isMatchTopic = Pattern.matches("^uplink/message/.*", topic);
					if(isMatchTopic) {
						MessageHeartBeatServiceImpl hbService = SpringContext.getBean(MessageHeartBeatServiceImpl.class);
						hbService.receiveMessage(msg);
					}
				}
			} else {
				MessageForwardServiceImpl observer = SpringContext.getBean(MessageForwardServiceImpl.class);
				boolean isMatchTopic = Pattern.matches(observer.acceptTopic(), topic);
				if (isMatchTopic) {
					if (null != observer.acceptContentType() && null != contentType && observer.acceptContentType().contains(contentType)) {
						Message<JSONObject> msg = null;
						try {
							msg = JSON.parseObject(jsonArray.get(i).toString(), messageType);
						} catch (Exception e) {
							logger.error("Can not cast to " + messageType + ", data = " + jsonArray.get(i).toString(), e);
						}
						if (null != msg) {
							observer.receiveMessage(topic, msg, message.getId(), message.getQos());
						}

					} else if (null == contentType && observer.acceptNoneContentType()) {
						Message<JSONObject> msg = null;
						try {
							msg = JSON.parseObject(jsonArray.get(i).toString(), messageType);
						} catch (Exception e) {
							logger.error("Can not cast to " + Message.class + ", data = " + jsonArray.get(i).toString(), e);
						}
						if (null != msg) {
							observer.receiveMessage(topic, msg, message.getId(), message.getQos());
						}
					}
				}
			}
		}

	}


	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		
	}

}
