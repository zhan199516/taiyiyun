package com.taiyiyun.passport.po.message;

/**
 * Created by okdos on 2017/6/27.
 */
public class BaseMessage<T> {
	public long getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public int getMessageType() {
		return messageType;
	}

	public void setMessageType(int messageType) {
		this.messageType = messageType;
	}

	public int getSessionType() {
		return sessionType;
	}

	public void setSessionType(int sessionType) {
		this.sessionType = sessionType;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public int getContentType() {
		return contentType;
	}

	public void setContentType(int contentType) {
		this.contentType = contentType;
	}

	public T getContent() {
		return content;
	}

	public void setContent(T content) {
		this.content = content;
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

	public long getPublishTime() {
		return publishTime;
	}

	public void setPublishTime(long publishTime) {
		this.publishTime = publishTime;
	}

	private int version;
	private int messageType;
	private String fromUserId;
	private String fromClientId;
	private String messageId;

	private int sessionType;
	private String sessionId;
	private long publishTime;
	private long updateTime;
	private int contentType;
	private T content;
}
