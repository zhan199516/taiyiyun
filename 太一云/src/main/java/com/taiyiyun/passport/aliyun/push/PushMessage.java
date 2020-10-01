package com.taiyiyun.passport.aliyun.push;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;

public class PushMessage implements Serializable {

	@JSONField(serialize = false)
	private static final long serialVersionUID = 8407081984747979516L;

	@JSONField(name = "target")
	private PushTarget target; // 推送目标类型

	@JSONField(name = "targetValue")
	private String targetValue; // 推送目标值

	@JSONField(name = "deviceType")
	private PushDeviceType deviceType; // 设备类型

	@JSONField(name = "pushType")
	private PushType pushType; // 推送类型

	@JSONField(name = "title")
	private String title; // 标题

	private String iOSSubtitle;

	private String body;

	@JSONField(name = "summary")
	private String summary; // 摘要

	@JSONField(name = "content")
	private String content; // 内容

	@JSONField(name = "extParameters")
	private Map<String, Object> extParameters; // 扩展参数

	public PushTarget getTarget() {
		return target;
	}

	public void setTarget(PushTarget target) {
		this.target = target;
	}

	public String getTargetValue() {
		return targetValue;
	}

	public void setTargetValue(String targetValue) {
		this.targetValue = targetValue;
	}

	public PushDeviceType getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(PushDeviceType deviceType) {
		this.deviceType = deviceType;
	}

	public PushType getPushType() {
		return pushType;
	}

	public void setPushType(PushType pushType) {
		this.pushType = pushType;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Map<String, Object> getExtParameters() {
		return extParameters;
	}

	public void setExtParameters(Map<String, Object> extParameters) {
		this.extParameters = extParameters;
	}

	public void addExtParameter(String key, Object value) {
		if (null == extParameters) {
			extParameters = new HashMap<String, Object>();
		}
		extParameters.put(key, value);
	}

	@JSONField(serialize = false)
	public String getStringExtParameters() {
		return (null == extParameters) ? null : JSON.toJSONString(extParameters);
	}

	public String getiOSSubtitle() {
		return iOSSubtitle;
	}

	public void setiOSSubtitle(String iOSSubtitle) {
		this.iOSSubtitle = iOSSubtitle;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public static final PushMessage getDefault() {
		PushMessage message = new PushMessage();
		message.setDeviceType(PushDeviceType.ALL);
		message.setPushType(PushType.NOTICE);
		message.setTarget(PushTarget.ACCOUNT);
		return message;
	}

	public String toString() {
		return JSON.toJSONString(this);
	}
}
