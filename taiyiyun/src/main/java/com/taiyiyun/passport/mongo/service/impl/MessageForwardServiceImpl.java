package com.taiyiyun.passport.mongo.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.taiyiyun.passport.aliyun.push.*;
import com.taiyiyun.passport.bean.UserCache;
import com.taiyiyun.passport.consts.Config;
import com.taiyiyun.passport.consts.Const;
import com.taiyiyun.passport.dao.group.IGroupDao;
import com.taiyiyun.passport.dao.group.IGroupMemberDao;
import com.taiyiyun.passport.init.SpringContext;
import com.taiyiyun.passport.mongo.dao.IMessageLogDao;
import com.taiyiyun.passport.mongo.po.MessageLog;
import com.taiyiyun.passport.mongo.service.IMessageForwardService;
import com.taiyiyun.passport.mqtt.IMessageObserver;
import com.taiyiyun.passport.mqtt.Message;
import com.taiyiyun.passport.mqtt.Message.ContentType;
import com.taiyiyun.passport.mqtt.Message.MessageType;
import com.taiyiyun.passport.mqtt.Message.SessionType;
import com.taiyiyun.passport.mqtt.MessagePublisher;
import com.taiyiyun.passport.po.PublicUser;
import com.taiyiyun.passport.po.group.Group;
import com.taiyiyun.passport.service.IPublicUserBlockService;
import com.taiyiyun.passport.service.IPublicUserService;
import com.taiyiyun.passport.service.IRedisService;
import com.taiyiyun.passport.util.CacheUtil;
import com.taiyiyun.passport.util.NumberUtil;
import com.taiyiyun.passport.util.SMSHelper;
import com.taiyiyun.passport.util.StringUtil;
import org.apache.commons.lang.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service("messageForwardServiceImpl1")
public class MessageForwardServiceImpl implements IMessageForwardService, IMessageObserver {

	public final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Resource
	private IMessageLogDao messageLogDao;
	
	@Resource
	private IRedisService redisService;
	
	@Resource
	private IPublicUserBlockService userBlockService;

	private static final Integer REVOKE_TIMEOUT = Config.getInt(Const.CONFIG_MESSAGE_REVOKE_TIMEOUT, 120000);
	
	@Override
	public String acceptTopic() {
		return "^uplink/message/.*";
	}

	@Override
	public boolean acceptNoneContentType() {
		return true;
	}
	
	@Override
	public Set<ContentType> acceptContentType() {
		return ACCEPT_ALL;
	}

	@Override
	public void receiveMessage(String topic, Message<JSONObject> message, int mqMessageId, int mqQos) {
		MessageType messageType = Message.getMessageType(message.getMessageType());
		if (null != message && null != message.getMessageType() && null != messageType) {
			System.out.println("接收到信息=================");
			message.setUpdateTime(System.currentTimeMillis());  // 更新消息更新时间
			switch (messageType) {
			case MESSAGE_IM_SYSTEM:
			case MESSAGE_IM_GENERIC:
				forwardMessage(topic, message);
				break;
			case MESSAGE_IM_REVOKE:
				revokeMessage(topic, message);
			default:
				break;
			}
		}
	}
	
	private void forwardMessage(String topic, Message<?> message) {
		SessionType sessionType = Message.getSessionType(message.getSessionType());
		if (null != sessionType) {
			switch (sessionType) {
			case SESSION_P2P:
				forwardP2PMessage(topic, message);
				pushAliMessage(topic, message);
				break;
			case SESSION_GROUP:
				forwardGroupMessage(topic, message);
				pushAliGroupMessage(topic, message);
			default:
				break;
			}
		}
	}
	
	/**
	 * 转发P2P消息
	 * @param topic
	 * @param message
	 */
	private void forwardP2PMessage(String topic, Message<?> message) {
		System.out.println("下发单聊信息=================");
		String forwardFromTopic = Message.DOWNLINK_MESSAGE + new String(topic.substring(topic.lastIndexOf("/") + 1));
		String forwardToTopic = Message.DOWNLINK_MESSAGE + message.getSessionId();
		MessagePublisher.getInstance().addPublish(forwardFromTopic, message);

		PublicUser user = userBlockService.getMyBlock(message.getSessionId(), message.getFromUserId());
		if(null == user) {
			MessagePublisher.getInstance().addPublish(forwardToTopic, message);
		}
	}


	/**
	 * 转发群组消息
	 * @param topic
	 * @param message
	 */
	private void forwardGroupMessage(String topic, Message<?> message) {
		System.out.println("下发群聊信息=================");
		String groupId = message.getSessionId();
		String fromUserId = message.getFromUserId();
		List<String> userIds = cacheUserIds(groupId);
		if(userIds != null && userIds.size() > 0 && (userIds.contains(fromUserId) || message.getContentType() == ContentType.CONTENT_IM_SYSTEM_GROUP_MEMBER_LEAVE.getValue())) {
			if(message.getContentType() != ContentType.CONTENT_IM_SYSTEM_GROUP_MEMBER_LEAVE.getValue()) {
				//先给第一个人发
				String fromUserTopic = Message.DOWNLINK_MESSAGE + fromUserId;
				MessagePublisher.getInstance().addFirstGroupMessagePublish(fromUserTopic, message);
			}
			for(String userId : userIds) {
				if(!StringUtils.equalsIgnoreCase(fromUserId, userId)) {
					String toUserTopic = Message.DOWNLINK_MESSAGE + userId;
					MessagePublisher.getInstance().addPublish(toUserTopic, message);
				}
			}
		}
	}

	private List<String> cacheUserIds(String groupId) {
		IGroupMemberDao gmDao = SpringContext.getBean(IGroupMemberDao.class);
		String userIdsStr = redisService.get(Const.GROUP_USERIDS + groupId);
		if(StringUtils.isEmpty(userIdsStr)) {
			List<String> userIds = gmDao.selectGroupMemberUserIdByGroupId(groupId);
			String userIdJson = JSONObject.toJSONString(userIds);
			redisService.evict(Const.GROUP_USERIDS + groupId);
			redisService.put(Const.GROUP_USERIDS + groupId, userIdJson, 3600);
			return userIds;
		} else {
			List<String> userIds = (List<String>)JSONObject.parse(userIdsStr);
			return userIds;
		}
	}

	private void revokeMessage(String topic, Message<?> message) {
		String messageLogId = Message.bulidUniquedId(topic, message);
		MessageLog messageLog = messageLogDao.findOne(messageLogId);
		if (null != messageLog) {
			Long messageTimeout = System.currentTimeMillis() - message.getPublishTime();
			if (messageTimeout <= REVOKE_TIMEOUT) {
				updateMessageLogRevoked(messageLogId);
				forwardMessage(topic, message);
			} else {
				logger.error("消息：" + message.toString() + "， 撤回失败。已超时：" + messageTimeout + "毫秒");
			}
		} else {
			logger.error("消息：" + message.toString() + "， 撤回失败。消息不存在。");
		}
	}

	private void updateMessageLogRevoked(String id) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("isRevoked", new Boolean(true));
		params.put("updateTime", System.currentTimeMillis());
		messageLogDao.update(id, params);
	}

	private void pushAliGroupMessage(String topic, Message<?> message) {
		System.out.println("-------------------阿里云推送群红包消息------------------");
		if(ContentType.CONTENT_IM_GENERIC_RD_REDPAY.getValue() == message.getContentType()) {
			String id = Message.bulidUniquedId(topic, message);
			if (getLock(id)) {
				PushMessage pushMessage = new PushMessage();
				pushMessage.setDeviceType(PushDeviceType.ALL);
				pushMessage.setPushType(PushType.NOTICE);
				pushMessage.setTitle(makeTitle(message.getSessionId(), message.getFromUserId()));
				pushMessage.setiOSSubtitle("");
				pushMessage.setSummary(getMessageBody(message));
				pushMessage.setTarget(PushTarget.ACCOUNT);
				List<String> userIds = cacheUserIds(message.getSessionId());
				if(userIds != null && !userIds.isEmpty()) {
					pushMessage.setTargetValue(StringUtils.join(userIds, ","));
				}
				pushMessage.setBody(getMessageBody(message));
				pushMessage.addExtParameter("fromUserId", message.getFromUserId());
				pushMessage.addExtParameter("sessionId", message.getSessionId());
				pushMessage.addExtParameter("messageType", String.valueOf(message.getMessageType()));
				pushMessage.addExtParameter("_NOTIFICATION_BAR_STYLE_", 1);

				Push.getInstance().addMessage(pushMessage);

				SMSHelper.sendMessageInfo(message);
			}
		}
	}

	private String makeTitle(String groupId, String userId) {
		Group group = getGroupFromCache(groupId);
		String title = "";
		if(group != null) {
			title = group.getGroupName();
		} else {
			title = getUserNameFromCache(userId);
		}
		if(title.getBytes().length > 20) {
			title = title.substring(0, 6) + "..";
		}
		return title;
	}

	private void pushAliMessage(String topic, Message<?> message) {
		String id = Message.bulidUniquedId(topic, message);
		if (getLock(id)) {
			PushMessage pushMessage = new PushMessage();

			pushMessage.setDeviceType(PushDeviceType.ALL);
			pushMessage.setPushType(PushType.NOTICE);
			pushMessage.setTitle(getUserNameFromCache(message.getFromUserId()));
			pushMessage.setiOSSubtitle("");
			pushMessage.setSummary(getMessageBody(message));
			pushMessage.setTarget(PushTarget.ACCOUNT);
			pushMessage.setTargetValue(message.getSessionId());
			pushMessage.setBody(getMessageBody(message));

			pushMessage.addExtParameter("fromUserId", message.getFromUserId());
			pushMessage.addExtParameter("sessionId", message.getSessionId());
			pushMessage.addExtParameter("messageType", String.valueOf(message.getMessageType()));
			pushMessage.addExtParameter("_NOTIFICATION_BAR_STYLE_", 1);

			Push.getInstance().addMessage(pushMessage);

			SMSHelper.sendMessageInfo(message);
		}
	}
	
	public boolean getLock(String id) {
		return redisService.setNX("lock.message:" + id, id);
	}

	private Group getGroupFromCache(String groupId) {
		Group group = CacheUtil.getHalfHour(Const.GROUP_ALIYUN_TUISONG + groupId);
		if(group == null) {
			IGroupDao groupDao = SpringContext.getBean(IGroupDao.class);
			group = groupDao.selectByPrimarykey(groupId);
			CacheUtil.evict(Const.GROUP_ALIYUN_TUISONG + groupId);
			CacheUtil.putHalfHour(Const.GROUP_ALIYUN_TUISONG + groupId, group);
		}
		return group;
	}
	
	private String getUserNameFromCache(String userId) {
		String DEFAULT_VALUE = "共享护照";
		try {
			
			UserCache userCache = CacheUtil.getOneDay(Const.APP_USERCACHE + userId);
			if(null == userCache) {
				IPublicUserService userService = SpringContext.getBean(IPublicUserService.class);
				
				PublicUser user = userService.getByUserId(userId);
				if(null != user) {
					userCache = new UserCache();
					userCache.setUserId(user.getId());
					userCache.setUserName(user.getUserName());
					userCache.setUuid(user.getUuid());
					userCache.setMobile(user.getMobile());
				}
			}
			
			if(null != userCache) {
				CacheUtil.putOneDay(Const.APP_USERCACHE + userId, userCache);
				
				if (StringUtil.isNotEmpty(userCache.getUserName()) && userCache.getUserName().length() > 20) {
					return userCache.getUserName().substring(0, 17) + "...";
				}
				
				return userCache.getUserName();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return DEFAULT_VALUE;
	}
	
	private String getMessageContent(Message<?> message) {
		JSONObject content = JSONObject.parseObject(JSONObject.toJSONString(message.getContent()));
		
		if(Message.ContentType.CONTENT_IM_GENERIC_TRANSFER_APPLY.getValue() == message.getContentType()) {
			return getUserNameFromCache(message.getFromUserId()) + "向你转账" + NumberUtil.formatAmount(content.getString("amount")) + content.getString("coinName");
		}
		
		if(Message.ContentType.CONTENT_IM_GENERIC_TRANSFER_ACCEPT.getValue() == message.getContentType()) {
			return getUserNameFromCache(message.getFromUserId()) + "已收取" + NumberUtil.formatAmount(content.getString("amount")) + content.getString("coinName");
		}
		
		if(Message.ContentType.CONTENT_IM_GENERIC_IMAGE.getValue() == message.getContentType()) {
			return getUserNameFromCache(message.getFromUserId()) + "给你发了一张图片";
		}
		
		if(Message.ContentType.CONTENT_IM_GENERIC_AUDIO.getValue() == message.getContentType()) {
			return getUserNameFromCache(message.getFromUserId()) + "给你发一段语音";
		}
		
		if(Message.ContentType.CONTENT_IM_GENERIC_FILE.getValue() == message.getContentType()) {
			return getUserNameFromCache(message.getFromUserId()) + "给你发了文件";
		}
		
		return getUserNameFromCache(message.getFromUserId()) + ": " + content.getString("text");
	}
	
	private String getMessageBody(Message<?> message) {
		JSONObject content = JSONObject.parseObject(JSONObject.toJSONString(message.getContent()));
		
		if(Message.ContentType.CONTENT_IM_GENERIC_TRANSFER_APPLY.getValue() == message.getContentType()) {
			return "向你转账" + NumberUtil.formatAmount(content.getString("amount")) + content.getString("coinName");
		}
		
		if(Message.ContentType.CONTENT_IM_GENERIC_TRANSFER_ACCEPT.getValue() == message.getContentType()) {
			return "已收取" + NumberUtil.formatAmount(content.getString("amount")) + content.getString("coinName");
		}
		
		if(Message.ContentType.CONTENT_IM_GENERIC_IMAGE.getValue() == message.getContentType()) {
			return "给你发了一张图片";
		}
		
		if(Message.ContentType.CONTENT_IM_GENERIC_AUDIO.getValue() == message.getContentType()) {
			return "给你发一段语音";
		}
		
		if(Message.ContentType.CONTENT_IM_GENERIC_FILE.getValue() == message.getContentType()) {
			return "给你发了文件";
		}

		if(ContentType.CONTENT_IM_GENERIC_RD_REDPAY.getValue() == message.getContentType()) {
			String contentMessage = "发送了一个群红包";
			String userName = getUserNameFromCache(message.getFromUserId());
			String text = content.getString("text");
			if(StringUtils.isNotEmpty(userName) && StringUtils.isNotEmpty(text)) {
				contentMessage = userName + ":[红包]" + text;
			}
			return contentMessage;
		}
		return content.getString("text");
	}

}
