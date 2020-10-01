package com.taiyiyun.passport.mqtt;

import com.taiyiyun.passport.consts.Config;
import com.taiyiyun.passport.consts.Const;
import com.taiyiyun.passport.init.SpringContext;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class MessageSubscriber {
	
	private MessageSubscriber() {
		try {
			observers = SpringContext.getBeans(IMessageObserver.class);
		} catch (Exception e) {
			logger.error("获取所有观察者异常。", e);
		}
		if (null == observers) {
			observers = new HashMap<>();
		}
	}

	public final Logger logger = LoggerFactory.getLogger(getClass());
	
	private String topic = "#"; // 订阅所有Topic
	
	private int qos = 2; // 服务质量，默认只分发一次
	
	private MqttClient client; // MQTT 客户端
	
	private Map<String, IMessageObserver> observers = new HashMap<>(); // 消息观察者
	
	private MqttConnectOptions options; // 连接配置
	
	private ScheduledExecutorService executorService; // 定时任务
	
	private IMqttToken token;
	
	public static MessageSubscriber getInstance() {
		return MessageSubscriberFactory.MESSAGE_SUBSCRIBER;
	}
	
	public void subscribe() throws Exception {
		init();
//		client.connect(options);
//		token = client.subscribeWithResponse(topic, qos);
		
		startReconnect();
	}
	
	public void unSubscribe() {
		try {
			executorService.shutdownNow();
		} catch (Exception e) {
			logger.error("自动重连任务关闭异常。", e);
		}
		try {
			client.disconnect();
		} catch (MqttException e) {
			logger.error("取消订阅，客户端断开异常。[borker]: " + client.getCurrentServerURI() + ", [client-id]: " + client.getClientId(), e);
		}
		
		try {
			client.close();
		} catch (MqttException e) {
			logger.error("取消订阅，客户端关闭异常。[borker]: " + client.getCurrentServerURI() + ", [client-id]: " + client.getClientId(), e);
		}
	}
	

	/**
	 * 设置消息观察者
	 *
	 */
	public void setObservers(Map<String, IMessageObserver> observers) {
		this.observers = observers;
	}
	
	private void init() throws MqttException {
		qos = Config.getInt(Const.CONFIG_MQTT_QOS, qos);
		
		client = new MqttClient(Config.get(Const.CONFIG_MQTT_BORKER), Config.get(Const.CONFIG_MQTT_SUBSCRIBER_CLIENT_ID), new MemoryPersistence());
		client.setCallback(new MessageSubscriberCallback(this));

		options = new MqttConnectOptions();
		options.setCleanSession(false);
		options.setUserName(Config.get(Const.CONFIG_MQTT_USERNAME));
		options.setPassword(Config.get(Const.CONFIG_MQTT_PASSWORD).toCharArray());
		
		options.setConnectionTimeout(Config.getInt(Const.CONFIG_MQTT_CONNECTION_TIMEOUT, 10));
		options.setKeepAliveInterval(Config.getInt(Const.CONFIG_MQTT_HEARTBEAT_TIME, 30));

		executorService = Executors.newSingleThreadScheduledExecutor();
	}
	
	private void startReconnect() {
		//10秒钟检查一次
		executorService.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				reconnect();
			}
		}, 1, 10, TimeUnit.SECONDS);
		
	}

	private volatile boolean doConnecting = false;
	
	protected void reconnect() {
		if (!client.isConnected() && !doConnecting) {
			try {
				doConnecting = true;
				logger.info("开始重新连接mqtt服务器 [client-id]: " + client.getClientId());
				client.connect(options);
				doConnecting = false;
				token = client.subscribeWithResponse(topic, qos);
			} catch (Exception e) {
				doConnecting = false;
				logger.error("客户端断开连接，重连异常。[borker]: " + client.getCurrentServerURI() + ", [client-id]: " + client.getClientId(), e);
			}
		}
	}
	
	protected Map<String, IMessageObserver> getObservers() {
		return observers;
	}
	
	protected MqttClient getMqttClient() {
		return client;
	}
	
	public void setTopic(String topic) {
		this.topic = topic;
	}

	protected String getTopic() {
		return topic;
	}
	
	protected int getQos() {
		return qos;
	}
	
	protected IMqttToken getToken() {
		return token;
	}
	
	private static class MessageSubscriberFactory {
		private static final MessageSubscriber MESSAGE_SUBSCRIBER = new MessageSubscriber();
	}
}
