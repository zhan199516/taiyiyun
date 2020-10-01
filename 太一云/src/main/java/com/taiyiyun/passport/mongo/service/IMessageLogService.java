package com.taiyiyun.passport.mongo.service;

import com.taiyiyun.passport.mongo.po.IMessageLog;
import com.taiyiyun.passport.mongo.po.MessageLog;
import com.taiyiyun.passport.mqtt.Message.MessageType;

import java.util.List;

public interface IMessageLogService {
	
	void save(MessageLog messageLog);

	<T extends IMessageLog> List<T> findByTopicAndPublishTimeAndMessageType(String[] topics, Long startPublishTime, Long endPublishTime, MessageType[] messageTypes, Class<T> clazz);

	MessageLog getLastMessageByTopic(String... topics);

	MessageLog getLastMessageByTopicAndPublishTime(String[] topics, Long startPublishTime);

	MessageLog getLastShareMsgByTopic(String... topics);

	MessageLog getLastShareMsgByTopicAndPublishTime(String[] topics, Long startPublishTime);


	MessageLog getLastMsg(List<String> sessionIds, List<Integer> sessionTypes, List<Integer> messageTypes);

	MessageLog getArticleMessage(String articleId);

}
