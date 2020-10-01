package com.taiyiyun.passport.mongo.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.DBObject;
import com.taiyiyun.passport.mongo.dao.IMessageLogDao;
import com.taiyiyun.passport.mongo.po.IMessageLog;
import com.taiyiyun.passport.mongo.po.MessageLog;
import com.taiyiyun.passport.mongo.service.IMessageLogService;
import com.taiyiyun.passport.mqtt.IMessageObserver;
import com.taiyiyun.passport.mqtt.Message;
import com.taiyiyun.passport.mqtt.Message.ContentType;
import com.taiyiyun.passport.mqtt.Message.MessageType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service("messageLogServiceImpl1")
public class MessageLogServiceImpl implements IMessageLogService, IMessageObserver {
	
	@Resource
	private IMessageLogDao messageLogDao;

	public final Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void save(MessageLog messageLog) {
		messageLogDao.insert(messageLog);
	}
	
	public MessageLog findOne(String id) {
		return messageLogDao.findOne(id);
	}
	
	@Override
	public Set<ContentType> acceptContentType() {
		return ACCEPT_ALL;
	}



	@Override
	public void receiveMessage(String topic, Message<JSONObject> message, int mqMessageId, int mqQos) {

		MessageType messageType = Message.getMessageType(message.getMessageType());
		if (null != message.getMessageType() && null != messageType && null == message.getImmigrate()) {
			if(message.getUpdateTime() == null) {
				message.setUpdateTime(System.currentTimeMillis());  // 更新消息更新时间
			}
			if(messageType == MessageType.MESSAGE_CIRCLE_REVOKE){
				revokeMessage(topic, message);
				return;
			}
		}


		MessageLog log = new MessageLog();

		JSONObject jsonObject = message.getContent();
		if(jsonObject == null) {
			jsonObject = new JSONObject();
		}
		String strContent = jsonObject.toJSONString();
		DBObject obj = (DBObject) com.mongodb.util.JSON.parse(strContent);
		log.setContent(obj.toMap());
		log.setContentType(message.getContentType());
		log.setFromClientId(message.getFromClientId());
		log.setFromUserId(message.getFromUserId());
		log.setMessageId(message.getMessageId());
		log.setMessageType(message.getMessageType());
		log.setPublishTime(message.getPublishTime());
		log.setSessionId(message.getSessionId());
		log.setSessionType(message.getSessionType());
		log.setUpdateTime(message.getUpdateTime());
		log.setVersion(message.getVersion());

		log.setIsRevoked(false);
		log.setTopic(topic);
		log.setId(Message.bulidUniquedId(topic, message));
		log.setLogTime(System.currentTimeMillis());
		log.setMqMessageId(mqMessageId);
		log.setMqQos(mqQos);

		if (null == findOne(log.getId())) {
			save(log);
		}
	}

	@Override
	public String acceptTopic() {
		return ".*";
	}

	@Override
	public boolean acceptNoneContentType() {
		return true;
	}

	private <T extends IMessageLog> T fixMessage(T m){
		if (m == null){
			return null;
		}

		m.setMqMessageId(null);
		m.setMqQos(null);
		m.setLogTime(null);
		m.setTopic(null);
		m.setId(null);

		//属于撤销的文章
		MessageType messageType = Message.getMessageType(m.getMessageType());
		if (null != m.getMessageType() && null != messageType) {
			if(messageType == MessageType.MESSAGE_CIRCLE_REVOKE){
				m.ContentSetNull();
			}
		}

		//非转账消息去掉transaction节点
		if(m.getMessageType() < 210){
			m.ContentDeleteTransaction();
		}
		return m;
	}

	@Override
	public <T extends IMessageLog> List<T> findByTopicAndPublishTimeAndMessageType(String[] topics, Long startTime, Long endPublishTime, MessageType[] messageTypes, Class<T> clazz) {
		List<T> messages = messageLogDao.findByTopicAndPublishTimeAndMessageType(topics, startTime, endPublishTime, messageTypes, clazz);
		if (null != messages && messages.size() > 0) {
			for (T m : messages) {
				fixMessage(m);
			}
		}
		return messages;
	}

	@Override
	public MessageLog getLastMessageByTopic(String... topics) {
		MessageLog messageLog = fixMessage(messageLogDao.getLastMessageByTopic(topics));
		return messageLog;
	}

	@Override
	public MessageLog getLastMessageByTopicAndPublishTime(String[] topics, Long startPublishTime) {
		return fixMessage(messageLogDao.getLastMessageByTopicAndPublishTime(topics, startPublishTime));
	}

	@Override
	public MessageLog getLastShareMsgByTopic(String... topics) {
		return fixMessage(messageLogDao.getLastShareMsgByTopic(topics));
	}

	@Override
	public MessageLog getLastShareMsgByTopicAndPublishTime(String[] topics, Long startPublishTime) {
		return fixMessage(messageLogDao.getLastShareMsgByTopicAndPublishTime(topics, startPublishTime));
	}

	@Override
	public MessageLog getLastMsg(List<String> sessionIds, List<Integer> sessionTypes, List<Integer> messageTypes){
		MessageLog messageLog = fixMessage(messageLogDao.getLastMsg(sessionIds, sessionTypes, messageTypes));
		return messageLog;
	}


	//撤回文章 todo 需要加上数据库的修改
	private void revokeMessage(String topic, Message<?> message) {
		String messageLogId = Message.bulidUniquedId(topic, message);
		MessageLog messageLog = messageLogDao.findOne(messageLogId);
		if (null != messageLog) {
			updateMessageLogRevoked(messageLogId, message.getMessageType());

		} else {
			logger.error("消息：" + message.toString() + "， 撤回失败。文章不存在。");
		}
	}

	//更新时间
	private void updateMessageLogRevoked(String id, Integer messageType) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("isRevoked", new Boolean(true));
		params.put("updateTime", System.currentTimeMillis());
		params.put("messageType", messageType);
		messageLogDao.update(id, params);
	}

	@Override
	public MessageLog getArticleMessage(String articleId){
		return messageLogDao.getArticleMessage(articleId);
	}
}
