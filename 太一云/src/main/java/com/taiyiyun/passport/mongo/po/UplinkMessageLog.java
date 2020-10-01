package com.taiyiyun.passport.mongo.po;

import com.alibaba.fastjson.annotation.JSONField;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

/**
 * Created by nina on 2018/3/16.
 */
@Document(collection = "uplink_message_log", language = "zh-CN")
public class UplinkMessageLog implements IMessageLog {

    @Id
    private String id; //UUID

    private String topic; //Topic

    private Integer mqMessageId; //MQTT消息ID

    private Integer mqQos; //MQTT服务质量

    private Long logTime; //日志时间，时间戳

    private Boolean isRevoked; // 是否撤销

    @JSONField(serialize = false)
    private static final long serialVersionUID = -1L;

    @JSONField(name = "version")
    private Integer version; // 消息版本号

    @JSONField(name = "messageType")
    private Integer messageType; // 消息类型

    @JSONField(name = "fromUserId")
    private String fromUserId; // 用户标识，全网唯一

    @JSONField(name = "fromClientId")
    private String fromClientId; // 客户端标识，全网唯一

    // 发送方本地生成的一个唯一标识
    // 对于接收方而言，MID = FromUserId + FromClientId + MessageId 是唯一的
    // 如果发送方清除了本地的 LastMessageId，需要重新生成 ClientId
    @JSONField(name = "messageId")
    private String messageId;

    @JSONField(name = "sessionType")
    private Integer sessionType; // 会话类型; 0: 单聊; 1: 群聊; 2: 共享号发文

    // SessionType 为 0 时，SessionId 表示 user_id
    // SessionType 为 1 时，SessionId 表示 group_id
    // SessionType 为 2 时，SessionId 表示 circle_id(本质上是 FromUserId)
    @JSONField(name = "sessionId")
    private String sessionId;

    @JSONField(name = "publishTime")
    private Long publishTime; // 消息创建时间

    @JSONField(name = "updateTime")
    private Long updateTime; // 消息最后更新时间

    @JSONField(name = "contentType")
    private Integer contentType; // 内容类型

    @JSONField(name = "content")
    private Map<String, Object> content; // 消息内容

    private Boolean immigrate; //只有迁移的时候使用



    @Override
    public void setMqMessageId(Integer mqMessageId) {
        this.mqMessageId = mqMessageId;
    }

    @Override
    public void setMqQos(Integer mqQos) {
        this.mqQos = mqQos;
    }

    @Override
    public void setLogTime(Long logTime) {
        this.logTime = logTime;
    }

    @Override
    public void setTopic(String topic) {
        this.topic = topic;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public Integer getMessageType() {
        return messageType;
    }

    @Override
    public void ContentSetNull() {
        this.setContent(null);
        this.setContentType(null);
    }

    @Override
    public void ContentDeleteTransaction() {
        Map map = this.getContent();
        if(map != null) {
            map.remove("transaction");
        }
    }

    public String getId() {
        return id;
    }

    public String getTopic() {
        return topic;
    }

    public Integer getMqMessageId() {
        return mqMessageId;
    }

    public Integer getMqQos() {
        return mqQos;
    }

    public Long getLogTime() {
        return logTime;
    }

    public Boolean getRevoked() {
        return isRevoked;
    }

    public Integer getVersion() {
        return version;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public String getFromClientId() {
        return fromClientId;
    }

    public String getMessageId() {
        return messageId;
    }

    public Integer getSessionType() {
        return sessionType;
    }

    public String getSessionId() {
        return sessionId;
    }

    public Long getPublishTime() {
        return publishTime;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public Integer getContentType() {
        return contentType;
    }

    public Map<String, Object> getContent() {
        return content;
    }

    public Boolean getImmigrate() {
        return immigrate;
    }

    public void setIsRevoked(Boolean isRevoked) {
        isRevoked = isRevoked;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public void setMessageType(Integer messageType) {
        this.messageType = messageType;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public void setFromClientId(String fromClientId) {
        this.fromClientId = fromClientId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public void setSessionType(Integer sessionType) {
        this.sessionType = sessionType;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setPublishTime(Long publishTime) {
        this.publishTime = publishTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public void setContentType(Integer contentType) {
        this.contentType = contentType;
    }

    public void setContent(Map<String, Object> content) {
        this.content = content;
    }

    public void setImmigrate(Boolean immigrate) {
        this.immigrate = immigrate;
    }
}
