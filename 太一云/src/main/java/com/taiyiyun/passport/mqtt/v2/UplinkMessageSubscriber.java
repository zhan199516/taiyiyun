package com.taiyiyun.passport.mqtt.v2;

import com.taiyiyun.passport.consts.Config;
import com.taiyiyun.passport.consts.Const;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by nina on 2018/3/14.
 */
public final class UplinkMessageSubscriber {

    public static void main(String[] args) {
        String topic = "^uplink/.*";
        String a = "uplink/message/467af24e07e511e8925800163e063c40";
        boolean b = a.matches(topic);
        System.out.println(b);
    }

    private UplinkMessageSubscriber() {}

    public final Logger logger = LoggerFactory.getLogger(getClass());

    //private String topic = "^uplink/message/.*"; //订阅所有上行消息Topic
    private String topic = "uplink/#"; //订阅所有上行消息Topic

    private int qos = 0; //服务质量

    private MqttClient client; // MQTT 客户端

    private MqttConnectOptions options; // 连接配置

    private ScheduledExecutorService executorService; // 定时任务

    private IMqttToken token;

    public static UplinkMessageSubscriber getInstance() {
        return UplinkMessageSubscriberFactory.UPLINK_MESSAGE_SUBSCRIBER;
    }

    public void subscribe() throws Exception {
        init();
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

    private void init() throws MqttException {
        qos = Config.getInt(Const.CONFIG_MQTT_QOS, qos);
        client = new MqttClient(Config.get(Const.CONFIG_MQTT_BORKER), Config.get(Const.CONFIG_MQTT_SUBSCRIBER_CLIENT_ID), new MemoryPersistence());
        client.setCallback(new UplinkMessageSubscriberCallback(this));

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

    private static class UplinkMessageSubscriberFactory {
        private static final UplinkMessageSubscriber UPLINK_MESSAGE_SUBSCRIBER = new UplinkMessageSubscriber();
    }

}
