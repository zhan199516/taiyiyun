package com.taiyiyun.passport.mongo.po;

/**
 * Created by okdos on 2017/7/13.
 */
public interface IMessageLog {

    //Integer getMqMessageId();
    void setMqMessageId(Integer mqMessageId);

    //Integer getMqQos();
    void setMqQos(Integer mqQos);

    //Long getLogTime();
    void setLogTime(Long logTime);

    //String getTopic();
    void setTopic(String topic);

    //String getId();
    void setId(String id);

    Integer getMessageType();

    void ContentSetNull();

    void ContentDeleteTransaction();
}
