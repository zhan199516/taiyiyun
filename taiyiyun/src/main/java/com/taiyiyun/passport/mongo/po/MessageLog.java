package com.taiyiyun.passport.mongo.po;

import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.mongodb.DBObject;
import com.taiyiyun.passport.mqtt.Message;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "message_log", language = "zh-CN")
public class MessageLog implements IMessageLog{

	@Id
	private String id; // UUID

	private String topic; // Topic

	private Integer mqMessageId; // MQTT消息ID

	private Integer mqQos; // MQTT服务质量

	private Long logTime; // 日志时间，时间戳
	
	private Boolean isRevoked; // 是否撤销


	@JSONField(serialize = false)
	private static final long serialVersionUID = 7566900274488105270L;

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
	private Map<String, Object>  content; // 消息内容

	private Boolean immigrate; //只有迁移的时候使用



	public String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public void ContentSetNull() {
		this.setContent(null);
		this.setContentType(null);
	}

	@Override
	public void ContentDeleteTransaction() {
		Map map = this.getContent();
		if(map != null){
			map.remove("transaction");
		}
	}

	public String getTopic() {
		return topic;
	}

	@Override
	public void setTopic(String topic) {
		this.topic = topic;
	}

	public Long getLogTime() {
		return logTime;
	}

	@Override
	public void setLogTime(Long logTime) {
		this.logTime = logTime;
	}

	public Integer getMqMessageId() {
		return mqMessageId;
	}

	@Override
	public void setMqMessageId(Integer mqMessageId) {
		this.mqMessageId = mqMessageId;
	}

	public Integer getMqQos() {
		return mqQos;
	}

	@Override
	public void setMqQos(Integer mqQos) {
		this.mqQos = mqQos;
	}

	public Boolean getIsRevoked() {
		return isRevoked;
	}

	public void setIsRevoked(Boolean isRevoked) {
		this.isRevoked = isRevoked;
	}




	public Boolean getImmigrate() {
		return immigrate;
	}

	public void setImmigrate(Boolean immigrate) {
		this.immigrate = immigrate;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public Integer getMessageType() {
		return messageType;
	}

	public void setMessageType(Integer messageType) {
		this.messageType = messageType;
	}

	public String getFromUserId() {
		return fromUserId;
	}

	public void setFromUserId(String fromUserId) {
		this.fromUserId = fromUserId;
	}

	public String getFromClientId() {
		return fromClientId;
	}

	public void setFromClientId(String fromClientId) {
		this.fromClientId = fromClientId;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public Integer getSessionType() {
		return sessionType;
	}

	public void setSessionType(Integer sessionType) {
		this.sessionType = sessionType;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public Long getPublishTime() {
		return publishTime;
	}

	public void setPublishTime(Long publishTime) {
		this.publishTime = publishTime;
	}

	public Long getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Long updateTime) {
		this.updateTime = updateTime;
	}

	public Integer getContentType() {
		return contentType;
	}

	public void setContentType(Integer contentType) {
		this.contentType = contentType;
	}

	public Map<String, Object> getContent() {
		return content;
	}

	public void setContent(Map<String, Object>  content) {
		this.content = content;
	}


}
