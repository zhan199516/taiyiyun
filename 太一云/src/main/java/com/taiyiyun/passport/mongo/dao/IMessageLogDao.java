package com.taiyiyun.passport.mongo.dao;

import com.taiyiyun.passport.mongo.po.IMessageLog;
import com.taiyiyun.passport.mongo.po.MessageLog;
import com.taiyiyun.passport.mqtt.Message.MessageType;

import java.util.List;
import java.util.Map;

public interface IMessageLogDao {
	
	void insert(MessageLog messageLog);

	MessageLog findOne(String id);

	void update(String id, Map<String, Object> params);
	
	<T extends IMessageLog> List<T> findByTopicAndPublishTimeAndMessageType(String[] topics, Long startPublishTime, Long endPublishTime, MessageType[] messageTypes, Class<T> clazz);

	/*只查会话消息*/
	MessageLog getLastMessageByTopic(String... topics);

	MessageLog getLastMessageByTopicAndPublishTime(String[] topics, Long startPublishTime);

	MessageLog getLastShareMsgByTopic(String... topics);

	MessageLog getLastShareMsgByTopicAndPublishTime(String[] topics, Long startPublishTime);

	MessageLog getLastMsg(List<String> sessionIds, List<Integer> sessionTypes, List<Integer> messageTypes);

	MessageLog getArticleMessage(String articleId);

	MessageLog getOneMessage(String messageId,String userId,String clientId);
}
