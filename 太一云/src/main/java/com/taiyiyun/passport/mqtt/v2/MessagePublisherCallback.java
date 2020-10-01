package com.taiyiyun.passport.mqtt.v2;


import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessagePublisherCallback implements MqttCallback {

	public final Logger logger = LoggerFactory.getLogger(getClass());

	private com.taiyiyun.passport.mqtt.v2.MessagePublisher messagePublisher;

	public MessagePublisherCallback(MessagePublisher messagePublisher) {
		this.messagePublisher = messagePublisher;
	}
	
	@Override
	public void connectionLost(Throwable cause) {
		logger.error("连接丢失：" + messagePublisher.getClient().getClientId(), cause);
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		logger.error("发布客户端，不关注任何Topic。收到Topic: " + topic + " 的消息： " + new String(message.getPayload(), "UTF-8") + ", messageId: " + message.getId() + ", qos: " + message.getQos());
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		try {
			if (null != token.getMessage()) {
				logger.debug("发布信息完成，发布的消息：" + new String(token.getMessage().getPayload(), "UTF-8") + ", messageId: " + token.getMessage().getId() + ", qos: " + token.getMessage().getQos());
			}
		} catch (Exception e) {
			logger.error("发布客户端发布消息完成回调异常。", e);
		}
	}

}
