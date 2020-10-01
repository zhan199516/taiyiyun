package com.taiyiyun.passport.mqtt.v2;

import com.alibaba.fastjson.JSON;
import com.taiyiyun.passport.consts.Config;
import com.taiyiyun.passport.consts.Const;
import com.taiyiyun.passport.mqtt.Message;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.Arrays;

/**
 * Created by nina on 2018/3/16.
 */
public final class MessagePublisher {

    private int qos = 0; // 服务质量，默认只分发一次

    private MqttClient client; // MQTT 客户端

    private MqttConnectOptions options; // 连接配置

    private Message<?> message; // 发送的信息

    public void publish(String topic, Message<?> message) throws Exception{
        if(null == client) {
            init();
        }
        if(!client.isConnected()) {
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

    public static MessagePublisher getInstance() {
        return MessagePublisherFactory.messagePublisher;
    }

    private static class MessagePublisherFactory {
        public static MessagePublisher messagePublisher = new MessagePublisher();
    }


    public int getQos() {
        return qos;
    }

    public void setQos(int qos) {
        this.qos = qos;
    }

    public MqttClient getClient() {
        return client;
    }

    public void setClient(MqttClient client) {
        this.client = client;
    }

    public MqttConnectOptions getOptions() {
        return options;
    }

    public void setOptions(MqttConnectOptions options) {
        this.options = options;
    }

    public Message<?> getMessage() {
        return message;
    }

    public void setMessage(Message<?> message) {
        this.message = message;
    }
}
