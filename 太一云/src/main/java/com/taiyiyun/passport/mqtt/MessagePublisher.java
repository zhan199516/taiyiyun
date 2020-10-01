package com.taiyiyun.passport.mqtt;

import com.alibaba.fastjson.JSON;
import com.taiyiyun.passport.consts.Config;
import com.taiyiyun.passport.consts.Const;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class MessagePublisher {

	private MessagePublisher() {
		//启动线程，进行数据发送
		ExecutorService es = Executors.newFixedThreadPool(2);
		es.execute(new Poll());
		es.execute(new PollGroupFirst());
	}
	
	private static final ConcurrentLinkedQueue<PublishMessage> PUBLISH_QUEUE = new ConcurrentLinkedQueue<>();

	private static final ConcurrentLinkedQueue<PublishMessage> PUBLISH_QUEUE_FIRST_GROUP_MESSAGE = new ConcurrentLinkedQueue<>();

	private int qos = 2; // 服务质量，默认只分发一次

	private MqttClient client; // MQTT 客户端

	private MqttConnectOptions options; // 连接配置
	
	private Message<?> message; // 发送的信息
	
	private boolean isPublishing = false; // 是否正在发送

	public static MessagePublisher getInstance() {
		return MessagePublisherFactory.MESSAGE_PUBLISHER;
	}

	// todo 存在并发下线程不安全问题，需要修改
	public void addPublish(String topic, Message<?> message) {
		PUBLISH_QUEUE.add(new PublishMessage(topic, message)); // 添加到发送队列
	}

	public void addFirstGroupMessagePublish(String topic, Message<?> message) {
		PUBLISH_QUEUE_FIRST_GROUP_MESSAGE.add(new PublishMessage(topic, message));
	}


	private void publish(String topic, Message<?> message) throws Exception {
		if (null == client) {
			init();
		}
		
		if (!client.isConnected()) {
			client.connect(options);
		}
		
		MqttTopic mqttTopic = client.getTopic(topic);

		MqttMessage mqttMessage = new MqttMessage();
		mqttMessage.setQos(qos);
		mqttMessage.setRetained(false);

		String msg = JSON.toJSONString(Arrays.asList(message));
		mqttMessage.setPayload(msg.getBytes("UTF-8"));
		MqttDeliveryToken token = mqttTopic.publish(mqttMessage);
		token.waitForCompletion();
	}
	
	public void disconnect() {
		if (null != client) {
			if (client.isConnected()) {
				try {
					client.disconnect();
				} catch (Exception e) {
					
				}
			}
			try {
				client.close();
			} catch (Exception e) {
				
			}
		}
	}

	private void init() throws Exception {
		qos = Config.getInt(Const.CONFIG_MQTT_QOS, qos);

		client = new MqttClient(Config.get(Const.CONFIG_MQTT_BORKER), Config.get(Const.CONFIG_MQTT_PUBLISHER_CLIENT_ID),new MemoryPersistence());
		client.setCallback(new MessagePublisherCallback(this));

		options = new MqttConnectOptions();
		options.setCleanSession(false);
		options.setUserName(Config.get(Const.CONFIG_MQTT_USERNAME));
		options.setPassword(Config.get(Const.CONFIG_MQTT_PASSWORD) .toCharArray());

		options.setConnectionTimeout(Config.getInt(Const.CONFIG_MQTT_CONNECTION_TIMEOUT, 10));
		options.setKeepAliveInterval(Config.getInt(Const.CONFIG_MQTT_HEARTBEAT_TIME, 30));
	}

	public int getQos() {
		return qos;
	}

	public MqttClient getClient() {
		return client;
	}

	public Message<?> getMessage() {
		return message;
	}

	public void setMessage(Message<?> message) {
		this.message = message;
	}

	private static class MessagePublisherFactory {
		static final MessagePublisher MESSAGE_PUBLISHER = new MessagePublisher();
	}

	static class Poll implements Runnable{
		@Override
		public void run() {
			try{
				while(true){
					PublishMessage message = PUBLISH_QUEUE.poll();
					if(null == message){
						Thread.sleep(100);
					} else {
						try{
							getInstance().publish(message.topic, message.message);
						} catch(Exception e){
							e.printStackTrace();
						}

					}
				}
			} catch(InterruptedException e){
				//线程中断则退出
				e.printStackTrace();
			}
		}
	}

	static class PollGroupFirst implements Runnable {
		@Override
		public void run() {
			try{
				while(true){
					PublishMessage message = PUBLISH_QUEUE_FIRST_GROUP_MESSAGE.poll();
					if(null == message){
						Thread.sleep(100);
					} else {
						try{
							getInstance().publish(message.topic, message.message);
						} catch(Exception e){
							e.printStackTrace();
						}
					}
				}
			} catch(InterruptedException e){
				//线程中断则退出
				e.printStackTrace();
			}
		}
	}
}

class PublishMessage {
	
	String topic;
	
	Message<?> message;
	
	PublishMessage(String topic, Message<?> message) {
		this.topic = topic;
		this.message = message;
	}
}
