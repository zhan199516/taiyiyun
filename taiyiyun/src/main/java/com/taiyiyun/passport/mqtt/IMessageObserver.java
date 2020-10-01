package com.taiyiyun.passport.mqtt;

import com.alibaba.fastjson.JSONObject;
import com.taiyiyun.passport.mqtt.Message.ContentType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 消息观察者接口定义
 * @author LiuQing
 */
public interface IMessageObserver {
	
	/**
	 * 是否接受无内容类型的消息
	 * @return
	 */
	public boolean acceptNoneContentType();
	
	/**
	 * 接受的Topic正则表达式
	 * @return
	 */
	public String acceptTopic();
	
	/**
	 * 接收的内容类型
	 * @return
	 */
	public Set<ContentType> acceptContentType();
	
	/**
	 * 接收消息方法
	 * @param topic Topic 
	 * @param message
	 * @param mqMessageId
	 * @param mqQos
	 * @return
	 */
	public void receiveMessage(String topic, Message<JSONObject> message, int mqMessageId, int mqQos);
	
	static final Set<ContentType> ACCEPT_ALL = new HashSet<>(Arrays.asList(Message.ContentType.CONTENT_CIRCLE_GENERIC_ONE_IMAGE, 
			Message.ContentType.CONTENT_CIRCLE_GENERIC_TEXT, Message.ContentType.CONTENT_CIRCLE_GENERIC_THREE_IMAGE,
			Message.ContentType.CONTENT_CIRCLE_GENERIC_TITLE_TEXT, Message.ContentType.CONTENT_CIRCLE_SYSTEM,
			Message.ContentType.CONTENT_CIRCLE_USERINFO_UPDATE, Message.ContentType.CONTENT_IM_GENERIC_AUDIO,
			Message.ContentType.CONTENT_IM_GENERIC_FILE, Message.ContentType.CONTENT_IM_GENERIC_IMAGE,
			Message.ContentType.CONTENT_IM_GENERIC_LN, Message.ContentType.CONTENT_IM_GENERIC_TEXT,
			Message.ContentType.CONTENT_IM_GENERIC_TRANSFER_APPLY, ContentType.CONTENT_IM_GENERIC_TRANSFER_ACCEPT,
			Message.ContentType.CONTENT_IM_SYSTEM_ADD_FRIEND_RESPONSE,
			Message.ContentType.CONTENT_IM_SYSTEM_ADD_FRIEND_RESQUEST,
			ContentType.CONTENT_IM_SYSTEM_GROUP_CHANGE_OWNER, ContentType.CONTENT_IM_SYSTEM_GROUP_CREATE_GROUP, ContentType.CONTENT_IM_SYSTEM_GROUP_JOIN_RESPONSE,
			ContentType.CONTENT_IM_SYSTEM_GROUP_JOIN_RESQUEST, ContentType.CONTENT_IM_SYSTEM_GROUP_MEMBER_LEAVE, ContentType.CONTENT_IM_SYSTEM_GROUP_UPDATE_GROUPINFO,
			ContentType.CONTENT_IM_SYSTEM_GROUP_UPDATE_GROUPINFO, ContentType.CONTENT_IM_GENERIC_RD_REDPAY, ContentType.CONTENT_IM_GENERIC_RD_REDGRABED, ContentType.CONTENT_IM_GENERIC_RD_REDCOMPLATED));

}
