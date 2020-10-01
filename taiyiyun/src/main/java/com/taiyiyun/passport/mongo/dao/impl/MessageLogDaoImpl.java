package com.taiyiyun.passport.mongo.dao.impl;

import com.alibaba.fastjson.JSON;
import com.taiyiyun.passport.mongo.dao.IMessageLogDao;
import com.taiyiyun.passport.mongo.po.IMessageLog;
import com.taiyiyun.passport.mongo.po.MessageLog;
import com.taiyiyun.passport.mqtt.Message.MessageType;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


@Repository
public class MessageLogDaoImpl implements IMessageLogDao {

	@Resource
	private MongoTemplate mongoTemplate;
	
	@Override
	public void insert(MessageLog messageLog) {
		//处理bigDecimal保存为字符串的问题
		mongoTemplate.insert(messageLog);
	}

	@Override
	public MessageLog findOne(String id) {
		Query query = new Query(new Criteria("_id").is(id));
		return mongoTemplate.findOne(query, MessageLog.class);
	}

	@Override
	public void update(String id, Map<String, Object> params) {
		Query query = new Query(new Criteria("_id").is(id));
		mongoTemplate.updateMulti(query, getUpdate(params), MessageLog.class);
	}
	
	protected Update getUpdate(Map<String, Object> params) {
		Update update = new Update();
		Iterator<String> it = params.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			update.set(key, params.get(key));
		}
		return update;
	}

	@Override
	public <T extends IMessageLog> List<T> findByTopicAndPublishTimeAndMessageType(String[] topics, Long startPublishTime, Long endPublishTime, MessageType[] messageTypes, Class<T> clazz) {
		Query query = new Query();
		Criteria criteria = new Criteria("topic").in(Arrays.asList(topics));
		if(startPublishTime != null || endPublishTime != null){
//			criteria = criteria.and("publishTime"); //不使用publishTime
			criteria = criteria.and("updateTime");

			if(startPublishTime != null){
				criteria = criteria.gt(startPublishTime);
			}

			if(endPublishTime != null){
				criteria = criteria.lte(endPublishTime);
			}
		}

		query.addCriteria(criteria);
		if (null != messageTypes) {
			Object[] objs = new Object[messageTypes.length];
			for (int i = 0; i < messageTypes.length; ++i) {
				if (null != messageTypes[i]) {
					objs[i] = messageTypes[i].getValue();
				}
			}
			query.addCriteria(new Criteria("messageType").in(objs));
		}
		query.addCriteria(new Criteria("isRevoked").is(false));
		query.with(new Sort(Direction.ASC, "updateTime")).limit(400);
		return mongoTemplate.find(query, clazz);
	}

	@Override
	public MessageLog getLastMessageByTopic(String... topics) {
		Query query = new Query();
		query.addCriteria(new Criteria("topic").in(Arrays.asList(topics))
				.and("messageType").in(MessageType.MESSAGE_IM_GENERIC.getValue(), MessageType.MESSAGE_IM_REVOKE.getValue(), MessageType.MESSAGE_GROUP_SYSTEM.getValue()))
				.with(new Sort(Direction.DESC, "updateTime")).limit(1);
		MessageLog messageLog = mongoTemplate.findOne(query, MessageLog.class);
		return messageLog;
	}

	@Override
	public MessageLog getLastMessageByTopicAndPublishTime(String[] topics, Long startPublishTime) {
		Query query = new Query();
		Criteria criteria = new Criteria("topic").in(Arrays.asList(topics));
		criteria = criteria.and("messageType");
		criteria = criteria.in(MessageType.MESSAGE_IM_GENERIC.getValue(), MessageType.MESSAGE_IM_REVOKE.getValue(), MessageType.MESSAGE_GROUP_SYSTEM.getValue());
		criteria = criteria.and("updateTime");
		if(startPublishTime != null){
			criteria = criteria.gt(startPublishTime);
		}
		query.addCriteria(criteria);
		query.with(new Sort(Direction.DESC, "updateTime")).limit(1);
		MessageLog messageLog = mongoTemplate.findOne(query, MessageLog.class);
		return messageLog;
	}

	@Override
	public MessageLog getLastShareMsgByTopic(String... topics) {
		Query query = new Query();
		query.addCriteria(new Criteria("topic").in(Arrays.asList(topics))
				.and("messageType").in(
						MessageType.MESSAGE_CIRCLE_REVOKE.getValue(),
						MessageType.MESSAGE_CIRCLE_GENERIC.getValue(),
						MessageType.MESSAGE_CIRCLE_USERINFO.getValue()
				))
				.with(new Sort(Direction.DESC, "updateTime")).limit(1);
		return mongoTemplate.findOne(query, MessageLog.class);
	}

	@Override
	public MessageLog getLastShareMsgByTopicAndPublishTime(String[] topics, Long startPublishTime) {
		Query query = new Query();
		Criteria criteria = new Criteria("topic").in(Arrays.asList(topics));
		criteria = criteria.and("messageType");
		criteria = criteria.in(MessageType.MESSAGE_CIRCLE_REVOKE.getValue(),
				MessageType.MESSAGE_CIRCLE_GENERIC.getValue(),
				MessageType.MESSAGE_CIRCLE_USERINFO.getValue());
		criteria = criteria.and("updateTime");
		if(startPublishTime != null){
			criteria = criteria.gt(startPublishTime);
		}
		query.addCriteria(criteria);
		query.with(new Sort(Direction.DESC, "updateTime")).limit(1);
		MessageLog messageLog = mongoTemplate.findOne(query, MessageLog.class);
		return messageLog;
	}


	public MessageLog getLastMsg(List<String> sessionIds, List<Integer> sessionTypes, List<Integer> messageTypes){
		long start = System.currentTimeMillis();
		Query query = new Query();


		if(sessionIds != null){
			Criteria criteria = new Criteria("sessionId").in(sessionIds.toArray());
			query.addCriteria(criteria);
		}

		if(sessionTypes != null){
			Criteria criteria = new Criteria("sessionType").in(sessionTypes.toArray());
			query.addCriteria(criteria);
		}

		if(sessionTypes != null){
			Criteria criteria = new Criteria("messageType").in(messageTypes.toArray());
			query.addCriteria(criteria);
		}

		query.with(new Sort(Direction.DESC, "updateTime")).limit(1);
		MessageLog messageLog = mongoTemplate.findOne(query, MessageLog.class);
		return messageLog;

	}

	public MessageLog getArticleMessage(String articleId){
		Query query = new Query();
		Criteria criteria = new Criteria("content.articleId").is(articleId);
		query.addCriteria(criteria);

		//Criteria criteria = Criteria.where("content.articleId").is(articleId);
//		query.addCriteria(criteria);
		//query.addCriteria(where("content.articleId").is(articleId));

//		BasicQuery query1 = new BasicQuery("{ \"_id\" : \"" + "358faddc032a89b1d89baf81f8d28e781570cfa8521563fc5898ff8ebaaddfd4" + "\" }");
//		MessageLog messageLog1 = mongoTemplate.findOne(query1, MessageLog.class);
//		System.out.println("-----------------a:" + JSON.toJSONString(messageLog1));
//
//		BasicQuery query2 = new BasicQuery("{ \"content.articleId\" : \"" + articleId + "\" }");


		MessageLog messageLog = mongoTemplate.findOne(query, MessageLog.class);

		System.out.println("-----------------b:" + JSON.toJSONString(messageLog));

		return messageLog;

	}

	@Override
	public MessageLog getOneMessage(String messageId, String userId, String clientId) {
		Query query = new Query();
		query.addCriteria(new Criteria("messageId").in(messageId)
				.and("fromUserId").in(userId)
				.and("fromClientId").in(clientId))
				.with(new Sort(Direction.DESC, "updateTime")).limit(1);
		MessageLog messageLog = mongoTemplate.findOne(query, MessageLog.class);
		return messageLog;
	}


}
