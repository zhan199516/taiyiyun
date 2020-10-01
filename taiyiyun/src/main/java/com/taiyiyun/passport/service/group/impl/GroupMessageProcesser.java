package com.taiyiyun.passport.service.group.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.taiyiyun.passport.consts.Config;
import com.taiyiyun.passport.mqtt.Message;
import com.taiyiyun.passport.mqtt.MessagePublisher;
import com.taiyiyun.passport.po.PublicUser;
import com.taiyiyun.passport.po.group.AddGroupUserParam;
import com.taiyiyun.passport.po.group.Group;
import com.taiyiyun.passport.po.group.GroupMember;
import com.taiyiyun.passport.util.Misc;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 群消息处理，处理所有的群相关消息。
 * <p>
 * 包含以下群消息的处理：
 * <ol>
 * <li>邀请入群，请求审批；
 * <li>审批通过，通知群内成员；
 * <li>退群通知消息；
 * <li>更改群信息；
 * <li>更改个人信息；
 * <li>变更群主；
 * <li>建群通知消息；
 * </ol>
 */
public class GroupMessageProcesser {

	/**
	 * <p>
	 * 使用{@link Message.ContentType#CONTENT_IM_SYSTEM_GROUP_CREATE_GROUP}消息类型进行消息的推送
	 * <p>
	 * 注意：只提交至推送队列，群主及被邀请成员全部推送
	 * 
	 * @param group
	 *            群信息
	 * @param publicUserList
	 *            被邀请用户信息
	 * 
	 * @see MessagePublisher#addPublish(String, Message)
	 * 
	 */
	public static Message<JSONObject> createGroupMessagePublish(Group group, List<PublicUser> publicUserList) {
		List<String> topics = buildCreateGroupTopics(group, publicUserList);
		Message<JSONObject> createGroupMsg = buildCreateGroupMessage(group, publicUserList);
		addMessage2PublishQueue(topics, createGroupMsg);
		return createGroupMsg;
	}

	/**
	 * <p>
	 * 使用{@link Message.ContentType#CONTENT_IM_SYSTEM_GROUP_JOIN_RESQUEST}消息类型进行消息的推送
	 * <p>
	 * 注意：只提交至推送队列，仅推送给群主
	 * 
	 * @param gmInviter
	 *            邀请用户信息
	 * @param addGroupUserParam
	 *            邀请信息
	 * @param group
	 *            群信息
	 * @param publicUserList
	 *            被邀请用户信息
	 * 
	 * @see MessagePublisher#addPublish(String, Message)
	 * 
	 */
	public static Message<JSONObject> groupUserJoinRequestMessagePublish(GroupMember gmInviter, AddGroupUserParam addGroupUserParam,
			Group group, List<PublicUser> publicUserList) {
		//String topic = new StringBuilder(Message.DOWNLINK_SYSTEM).append(group.getOwnerId()).toString();
		String topic = new StringBuilder(Message.UPLINK_SYSTEM).append(group.getOwnerId()).toString();
		Message<JSONObject> message = buildGroupUserJoinRequestMessage(gmInviter, addGroupUserParam, group,
				publicUserList);
		//MessagePublisher.getInstance().addPublish(topic, message);
		try {
			com.taiyiyun.passport.mqtt.v2.MessagePublisher.getInstance().publish(topic, message);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return message;
	}

	/**
	 * <p>
	 * 使用{@link Message.ContentType#CONTENT_IM_SYSTEM_GROUP_JOIN_RESPONSE}消息类型进行消息的推送
	 * <p>
	 * 注意：只提交至推送队列，群主、被邀请成员及现有群成员全部推送
	 * 
	 * @param joinType
	 *            入群类型
	 * @param inviterId
	 *            邀请者用户ID
	 * @param inviterName
	 *            邀请者用户昵称
	 * @param iviterReason
	 *            邀请原因
	 * @param group
	 *            群信息
	 * @param publicUserList
	 *            审批通过的用户信息
	 * @param groupCurrentUserIdList
	 *            群当前成员信息
	 * 
	 * @see MessagePublisher#addPublish(String, Message)
	 * 
	 */
	public static Message<JSONObject> groupUserJoinResponseMessagePublish(int joinType, String inviterId, String inviterName, String iviterReason,
			Group group, List<PublicUser> publicUserList, List<String> groupCurrentUserIdList) {
		List<String> topics = buildGroupUserJoinResponseMessageTopics(group, publicUserList, groupCurrentUserIdList);
		Message<JSONObject> message = buildGroupUserJoinResponseMessage(joinType, inviterId, inviterName, iviterReason, group,
				publicUserList, groupCurrentUserIdList);
		addMessage2PublishQueue(topics, message);
		return message;
	}
	
	/**
	 * <p>
	 * 推送退群通知消息，使用消息类型：{@link Message.ContentType#CONTENT_IM_SYSTEM_GROUP_MEMBER_LEAVE}
	 * <p>
	 * 推送对象：所有群成员
	 * 
	 * @param group
	 *            群信息
	 * @param leaveGroupUserIdList
	 *            退群用户ID
	 * @param groupUserIdList
	 *            现有群成员ID
	 * 
	 * @see MessagePublisher#addPublish(String, Message)
	 */
	public static Message<JSONObject> leaveGroupMessagePublish(boolean isKick, Group group,
			List<String> leaveGroupUserIdList, List<String> groupUserIdList) {
		//groupUserIdList.addAll(leaveGroupUserIdList);
		List<String> topics = buildLeavaGroupMessageTopics(groupUserIdList);
		Message<JSONObject> message = buildLeaveGroupMessage(isKick, group, leaveGroupUserIdList);
		addMessage2PublishQueue(topics, message);
		return message;
	}

	/**
	 * <p>
	 * 推送变更群主消息，使用消息类型：{@link Message.ContentType#CONTENT_IM_SYSTEM_GROUP_CHANGE_OWNER}
	 * <p>
	 * 推送对象：所有群成员
	 * 
	 * @param group
	 *            群信息
	 * @param userId
	 *            新群主用户ID
	 * @param groupUserIdList
	 *            所有群成员
	 * @return 
	 * 
	 * @see MessagePublisher#addPublish(String, Message)
	 */
//	public static Message<JSONObject> changeGroupOwnerMessagePublish(Group group, String userId, List<String> groupUserIdList) {
//		List<String> topics = buildChangeGroupOwnerMessageTopics(groupUserIdList);
//		Message<JSONObject> message = buildChangeGroupOwnerMessage(group, userId);
//		addMessage2PublishQueue(topics, message);
//		return message;
//	}

	/**
	 * <p>
	 * 推送更改群信息消息，使用消息类型：{@link Message.ContentType#CONTENT_IM_SYSTEM_GROUP_UPDATE_GROUPINFO}
	 * <p>
	 * 推送对象：所有群成员
	 * 
	 * @param group
	 *            群信息
	 * @param updateUserId
	 *            更新群信息用户ID
	 * @param groupUserIdList
	 *            所有群成员用户ID
	 */
	public static void updateGroupInfoMessagePublish(Group group, String updateUserId, List<String> groupUserIdList) {
		List<String> topics = buildUpdateGroupInfoMessageTopics(groupUserIdList);
		Message<JSONObject> message = buildUpdateGroupInfoMessage(group, updateUserId, groupUserIdList.size());
		addMessage2PublishQueue(topics, message);
	}

	/**
	 * <p>
	 * 推送更改个人信息消息，使用消息类型：{@link Message.ContentType#CONTENT_IM_SYSTEM_GROUP_UPDATE_MEMINFO}
	 * 
	 * @param groupMember
	 *            更改的用户信息
	 * @param avatarUrl
	 *            用户头像
	 * @param groupUserIdList
	 *            所有群成员用户ID
	 */
	public static void updateMemberInfoMessagePublish(GroupMember groupMember, String avatarUrl,
			List<String> groupUserIdList) {
		List<String> topics = buildUpdateMemberInfoMessageTopics(groupUserIdList);
		Message<JSONObject> message = buildUpdateMemberInfoMessage(groupMember, avatarUrl);
		addMessage2PublishQueue(topics, message);
	}

	/**
	 * <p>
	 * 推送更换群主消息，使用消息类型：{@link Message.ContentType#CONTENT_IM_SYSTEM_GROUP_CHANGE_OWNER}
	 *
	 * @param groupId
	 * 				群id
	 * @param oldOwnerId
	 * 				旧群主用户id
	 * @param newOwnerId
	 * 				新群主用户id
	 * @param groupUserIdList
	 * 				群成员用户id集合
	 */
	public static Message<JSONObject> changeGroupOwnerMessagePublish(String groupId, String oldOwnerId, String newOwnerId, List<String> groupUserIdList) {
		List<String> topics = buildChangeGroupOwnerTopics(groupUserIdList);
		Message<JSONObject> message = buildChangeGroupOwnerMessage(groupId, oldOwnerId, newOwnerId);
		addMessage2PublishQueue(topics, message);
		return message;
	}

	/**
	 * <p>
	 * 构建建群消息Topic，被邀请成员的Topic，不包含群主
	 * <p>
	 * Example:
	 * <p>
	 * {@link List} => ["downlink/message/aaa", "downlink/message/bbb", ...]
	 * 
	 * @param group
	 *            群信息
	 * @param publicUserList
	 *            被邀请用户信息
	 * 
	 * @return
	 */
	private static List<String> buildCreateGroupTopics(Group group, List<PublicUser> publicUserList) {
		List<String> topics = new ArrayList<>(publicUserList.size());
		for (PublicUser user : publicUserList) {
			//topics.add(new StringBuilder(Message.DOWNLINK_SYSTEM).append(user.getId()).toString());
			topics.add(new StringBuilder(Message.UPLINK_SYSTEM).append(user.getId()).toString());
		}
		//topics.remove(new StringBuilder(Message.DOWNLINK_SYSTEM).append(group.getOwnerId()).toString());
		topics.remove(new StringBuilder(Message.UPLINK_SYSTEM).append(group.getOwnerId()).toString());
		return topics;
	}

	/**
	 * <p>
	 * 构建群消息内容实体
	 * 
	 * @param group
	 *            群信息
	 * @param publicUserList
	 *            被邀请用户信息
	 * 
	 * @see Message
	 * 
	 * @return
	 */
	private static Message<JSONObject> buildCreateGroupMessage(Group group, List<PublicUser> publicUserList) {
		Message<JSONObject> createGroupMsg = new Message<>();
		createGroupMsg.setVersion(Integer.valueOf(Message.VERSION_1));
		createGroupMsg.setMessageType(Message.MessageType.MESSAGE_GROUP_SYSTEM.getValue());
		createGroupMsg.setFromUserId(group.getOwnerId());
		createGroupMsg.setFromClientId(Config.get("mqtt.clientId"));
		createGroupMsg.setMessageId(new StringBuilder("CrateGroup-").append(group.getGroupId()).toString());
		createGroupMsg.setSessionType(Message.SessionType.SESSION_GROUP.getValue());
		createGroupMsg.setSessionId(group.getGroupId());

		long groupCrateTime = (null == group.getCreateTime()) ? System.currentTimeMillis()
				: group.getCreateTime().getTime();
		createGroupMsg.setPublishTime(groupCrateTime);
		createGroupMsg.setUpdateTime(groupCrateTime);
		createGroupMsg.setContentType(Message.ContentType.CONTENT_IM_SYSTEM_GROUP_CREATE_GROUP.getValue());

		JSONObject msgContent = new JSONObject();
		msgContent.put("groupId", group.getGroupId());
		msgContent.put("groupName", group.getGroupName());
		msgContent.put("groupAvatarUrl", Misc.getServerUri(null, group.getGroupHeader()));
		msgContent.put("ownerId", group.getOwnerId());
		msgContent.put("memberAmount", publicUserList.size());
		JSONArray contentMemberArray = new JSONArray(publicUserList.size());
		for (PublicUser user : publicUserList) {
			JSONObject memberObj = new JSONObject();
			memberObj.put("invitee", user.getId());
			memberObj.put("inviteeName", user.getUserName());
			memberObj.put("inviteeAvatarUrl", Misc.getServerUri(null, user.getAvatarUrl()));
			contentMemberArray.add(memberObj);
		}
		msgContent.put("members", contentMemberArray);

		createGroupMsg.setContent(msgContent);

		return createGroupMsg;
	}

	/**
	 * <p>
	 * 构建用户入群审批消息
	 * 
	 * @param gmInviter
	 *            邀请用户信息
	 * @param addGroupUserParam
	 *            邀请信息
	 * @param group
	 *            群信息
	 * @param publicUserList
	 *            被邀请用户信息
	 * 
	 * @see Message
	 * 
	 * @return
	 */
	private static Message<JSONObject> buildGroupUserJoinRequestMessage(GroupMember gmInviter,
			AddGroupUserParam addGroupUserParam, Group group, List<PublicUser> publicUserList) {
		Message<JSONObject> groupUserJoinRequestMsg = new Message<>();
		groupUserJoinRequestMsg.setVersion(Integer.valueOf(Message.VERSION_1));
		groupUserJoinRequestMsg.setMessageType(Message.MessageType.MESSAGE_GROUP_SYSTEM.getValue());
		if (null != gmInviter) {
			groupUserJoinRequestMsg.setFromUserId(gmInviter.getUserId());
		} else {
			groupUserJoinRequestMsg.setFromUserId(publicUserList.get(0).getId());
		}
		groupUserJoinRequestMsg.setFromClientId(Config.get("mqtt.clientId"));
		groupUserJoinRequestMsg.setMessageId(new StringBuilder("Group-Join-Request-").append(group.getGroupId())
				.append("-").append(System.currentTimeMillis()).toString());
		groupUserJoinRequestMsg.setSessionType(Message.SessionType.SESSION_GROUP.getValue());
		groupUserJoinRequestMsg.setSessionId(group.getGroupId());

		long msgTime = System.currentTimeMillis();
		groupUserJoinRequestMsg.setPublishTime(msgTime);
		groupUserJoinRequestMsg.setUpdateTime(msgTime);
		groupUserJoinRequestMsg.setContentType(Message.ContentType.CONTENT_IM_SYSTEM_GROUP_JOIN_RESQUEST.getValue());

		JSONObject msgContent = new JSONObject();
		if (null != addGroupUserParam.getInviteReason()) {
			msgContent.put("text", addGroupUserParam.getInviteReason());
		}
		if (null != gmInviter) {
			msgContent.put("inviter", gmInviter.getUserId());
			msgContent.put("inviterName", gmInviter.getNikeName());
		}
		msgContent.put("joinType", addGroupUserParam.getJoinType());
		JSONArray joinMemsArray = new JSONArray(publicUserList.size());
		for (PublicUser user : publicUserList) {
			JSONObject memberObj = new JSONObject();
			memberObj.put("invitee", user.getId());
			memberObj.put("inviteeName", user.getUserName());
			memberObj.put("avatarUrl", Misc.getServerUri(null,user.getAvatarUrl()));
			joinMemsArray.add(memberObj);
		}
		msgContent.put("joinMems", joinMemsArray);

		groupUserJoinRequestMsg.setContent(msgContent);

		return groupUserJoinRequestMsg;
	}

	/**
	 * <p>
	 * 构建入群通知消息TOPIC
	 * 
	 * @param gmInviter
	 *            邀请用户信息
	 * @param addGroupUserParam
	 *            邀请信息
	 * @param group
	 *            群信息
	 * @param publicUserList
	 *            被邀请用户信息
	 * @param groupCurrentUserIdList
	 *            现有群成员
	 * 
	 * @return
	 */
	private static List<String> buildGroupUserJoinResponseMessageTopics(Group group, List<PublicUser> publicUserList,
			List<String> groupCurrentUserIdList) {
		List<String> topics = new ArrayList<>(publicUserList.size() + groupCurrentUserIdList.size());
//		for (PublicUser user : publicUserList) {
//			topics.add(new StringBuilder(Message.DOWNLINK_SYSTEM).append(user.getId()).toString());
//		}
		for (String userId : groupCurrentUserIdList) {
			//topics.add(new StringBuilder(Message.DOWNLINK_SYSTEM).append(userId).toString());
			topics.add(new StringBuilder(Message.UPLINK_SYSTEM).append(userId).toString());
		}
		return topics;
	}

	/**
	 * <p>
	 * 构建入群通知消息
	 * 
	 * @param joinType
	 *            入群类型
	 * @param inviterId
	 *            邀请者用户ID
	 * @param inviterName
	 *            邀请者用户昵称
	 * @param iviterReason
	 *            邀请原因
	 * @param group
	 *            群信息
	 * @param publicUserList
	 *            审批通过的用户信息
	 * @param groupCurrentUserIdList
	 *            群当前成员信息
	 * @return
	 * 
	 * @see Message
	 */
	private static Message<JSONObject> buildGroupUserJoinResponseMessage(int joinType, String inviterId,
			String inviterName, String iviterReason, Group group, List<PublicUser> publicUserList,
			List<String> groupCurrentUserIdList) {
		Message<JSONObject> groupUserJoinResponseMsg = new Message<>();
		groupUserJoinResponseMsg.setVersion(Integer.valueOf(Message.VERSION_1));
		groupUserJoinResponseMsg.setMessageType(Message.MessageType.MESSAGE_GROUP_SYSTEM.getValue());
		if (null != inviterId) {
			groupUserJoinResponseMsg.setFromUserId(inviterId);
		} else {
			groupUserJoinResponseMsg.setFromUserId(group.getOwnerId());
		}
		groupUserJoinResponseMsg.setFromClientId(Config.get("mqtt.clientId"));
		groupUserJoinResponseMsg.setMessageId(new StringBuilder("Group-Join-Response-").append(group.getGroupId())
				.append("-").append(System.currentTimeMillis()).toString());
		groupUserJoinResponseMsg.setSessionType(Message.SessionType.SESSION_GROUP.getValue());
		groupUserJoinResponseMsg.setSessionId(group.getGroupId());

		long msgTime = System.currentTimeMillis();
		groupUserJoinResponseMsg.setPublishTime(msgTime);
		groupUserJoinResponseMsg.setUpdateTime(msgTime);
		groupUserJoinResponseMsg.setContentType(Message.ContentType.CONTENT_IM_SYSTEM_GROUP_JOIN_RESPONSE.getValue());

		JSONObject msgContent = new JSONObject();
		msgContent.put("joinType", joinType);
		msgContent.put("groupId", group.getGroupId());
		msgContent.put("groupName", group.getGroupName());
		msgContent.put("groupAvatarUrl", Misc.getServerUri(null, group.getGroupHeader()));
		msgContent.put("ownerId", group.getOwnerId());
		msgContent.put("groupType", group.getGroupType());
		msgContent.put("inviteType", group.getInviteType());
		msgContent.put("alterGroupName", group.getModifyRight());
		msgContent.put("inviter", inviterId);
		msgContent.put("inviterName", inviterName);
		msgContent.put("needAuth", group.getNeedAuth());
		JSONArray joinMemsArray = new JSONArray(publicUserList.size());
		for (PublicUser user : publicUserList) {
			JSONObject memberObj = new JSONObject();
			memberObj.put("invitee", user.getId());
			memberObj.put("inviteeName", user.getUserName());
			memberObj.put("avatarUrl", Misc.getServerUri(null,user.getAvatarUrl()));
			joinMemsArray.add(memberObj);
		}
		msgContent.put("joinMems", joinMemsArray);

		groupUserJoinResponseMsg.setContent(msgContent);

		return groupUserJoinResponseMsg;
	}
	
	/**
	 * 添加消息到发送队列
	 * 
	 * @param topics
	 *            消息TOPICS
	 * @param message
	 *            具体的消息
	 * 
	 * @see MessagePublisher#addPublish(String, Message)
	 * 
	 */
	private static void addMessage2PublishQueue(List<String> topics, Message<?> message) {
		//MessagePublisher publisher = MessagePublisher.getInstance();
		com.taiyiyun.passport.mqtt.v2.MessagePublisher publisher = com.taiyiyun.passport.mqtt.v2.MessagePublisher.getInstance();
		try {
			for (String topic : topics) {
				publisher.publish(topic, message);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * <p>
	 * 构建退群消息TOPIC
	 * 
	 * @param userIdList
	 *            群全部成员用户ID
	 * @return
	 */
	private static List<String> buildLeavaGroupMessageTopics(List<String> userIdList) {
		List<String> topics = new ArrayList<>(userIdList.size());
		for (String userId : userIdList) {
			//topics.add(new StringBuilder(Message.DOWNLINK_SYSTEM).append(userId).toString());
			topics.add(new StringBuilder(Message.UPLINK_SYSTEM).append(userId).toString());

		}
		return topics;
	}

	/**
	 * <p>
	 * 构建退群消息
	 * 
	 * @param group
	 *            群信息
	 * @param leaveGroupUserIdList
	 *            退群用户ID
	 * @return
	 */
	private static Message<JSONObject> buildLeaveGroupMessage(boolean isKick, Group group, List<String> leaveGroupUserIdList) {
		Message<JSONObject> leaveGroupMsg = new Message<>();
		leaveGroupMsg.setVersion(Integer.valueOf(Message.VERSION_1));
		leaveGroupMsg.setMessageType(Message.MessageType.MESSAGE_GROUP_SYSTEM.getValue());
		if (isKick) {
			leaveGroupMsg.setFromUserId(group.getOwnerId());
		} else {
			leaveGroupMsg.setFromUserId(leaveGroupUserIdList.get(0));
		}
		leaveGroupMsg.setFromClientId(Config.get("mqtt.clientId"));
		leaveGroupMsg.setMessageId(new StringBuilder("Group-Member-Leave-").append(group.getGroupId()).append("-")
				.append(System.currentTimeMillis()).toString());
		leaveGroupMsg.setSessionType(Message.SessionType.SESSION_GROUP.getValue());
		leaveGroupMsg.setSessionId(group.getGroupId());

		long msgTime = System.currentTimeMillis();
		leaveGroupMsg.setPublishTime(msgTime);
		leaveGroupMsg.setUpdateTime(msgTime);
		leaveGroupMsg.setContentType(Message.ContentType.CONTENT_IM_SYSTEM_GROUP_MEMBER_LEAVE.getValue());

		JSONObject msgContent = new JSONObject();

		JSONArray joinMemsArray = new JSONArray();
		for (String userId : leaveGroupUserIdList) {
			joinMemsArray.add(userId);
		}
		msgContent.put("userIds", joinMemsArray);

		leaveGroupMsg.setContent(msgContent);

		return leaveGroupMsg;
	}

	/**
	 * <p>
	 * 构建变更群主消息TOPIC
	 * 
	 * @param userIdList
	 *            群全部成员用户ID
	 * 
	 * @return
	 * 
	 * @see #buildLeavaGroupMessageTopics(List)
	 * 
	 */
//	private static List<String> buildChangeGroupOwnerMessageTopics(List<String> userIdList) {
//		return buildLeavaGroupMessageTopics(userIdList);
//	}

	/**
	 * <p>
	 * 构建版更群主消息
	 * 
	 * @param group
	 *            群信息
	 * @param userId
	 *            新群主用户ID
	 * @return
	 */
//	private static Message<JSONObject> buildChangeGroupOwnerMessage(Group group, String userId) {
//		Message<JSONObject> changeGroupOwnerMsg = new Message<>();
//		changeGroupOwnerMsg.setVersion(Integer.valueOf(Message.VERSION_1));
//		changeGroupOwnerMsg.setMessageType(Message.MessageType.MESSAGE_GROUP_SYSTEM.getValue());
//		changeGroupOwnerMsg.setFromUserId(userId);
//		changeGroupOwnerMsg.setFromClientId(Config.get("mqtt.clientId"));
//		changeGroupOwnerMsg.setMessageId(new StringBuilder("Group-Change-Owner-").append(group.getGroupId()).append("-")
//				.append(System.currentTimeMillis()).toString());
//		changeGroupOwnerMsg.setSessionType(Message.SessionType.SESSION_GROUP.getValue());
//		changeGroupOwnerMsg.setSessionId(group.getGroupId());
//
//		long msgTime = System.currentTimeMillis();
//		changeGroupOwnerMsg.setPublishTime(msgTime);
//		changeGroupOwnerMsg.setUpdateTime(msgTime);
//		changeGroupOwnerMsg.setContentType(Message.ContentType.CONTENT_IM_SYSTEM_GROUP_CHANGE_OWNER.getValue());
//
//		JSONObject msgContent = new JSONObject();
//
//		msgContent.put("groupId", group.getGroupId());
//		msgContent.put("userId", userId);
//
//		changeGroupOwnerMsg.setContent(msgContent);
//
//		return changeGroupOwnerMsg;
//	}

	/**
	 * <p>
	 * 构建更改群信息消息TOPIC
	 * 
	 * @param userIdList
	 *            群全部成员用户ID
	 * 
	 * @return
	 * 
	 * @see #buildLeavaGroupMessageTopics(List)
	 * 
	 */
	private static List<String> buildUpdateGroupInfoMessageTopics(List<String> userIdList) {
		return buildLeavaGroupMessageTopics(userIdList);
	}

	/**
	 * <p>
	 * 构建更改群信息消息
	 * 
	 * @param group
	 *            群信息
	 * @param updateUserId
	 *            更新群信息的用户ID
	 * 
	 * @return
	 */
	private static Message<JSONObject> buildUpdateGroupInfoMessage(Group group, String updateUserId, int memberAmount) {
		Message<JSONObject> updateGroupInfoMsg = new Message<>();
		updateGroupInfoMsg.setVersion(Integer.valueOf(Message.VERSION_1));
		updateGroupInfoMsg.setMessageType(Message.MessageType.MESSAGE_GROUP_SYSTEM.getValue());
		updateGroupInfoMsg.setFromUserId(updateUserId);
		updateGroupInfoMsg.setFromClientId(Config.get("mqtt.clientId"));
		updateGroupInfoMsg.setMessageId(new StringBuilder("Group-Info-Update-").append(group.getGroupId()).append("-")
				.append(System.currentTimeMillis()).toString());
		updateGroupInfoMsg.setSessionType(Message.SessionType.SESSION_GROUP.getValue());
		updateGroupInfoMsg.setSessionId(group.getGroupId());

		long msgTime = System.currentTimeMillis();
		updateGroupInfoMsg.setPublishTime(msgTime);
		updateGroupInfoMsg.setUpdateTime(msgTime);
		updateGroupInfoMsg.setContentType(Message.ContentType.CONTENT_IM_SYSTEM_GROUP_UPDATE_GROUPINFO.getValue());

		JSONObject msgContent = new JSONObject();

		msgContent.put("groupId", group.getGroupId());
		msgContent.put("groupName", group.getGroupName());
		msgContent.put("groupHeader", Misc.getServerUri(null, group.getGroupHeader()));
		msgContent.put("ownerId", group.getOwnerId());
		msgContent.put("memberAmount", memberAmount);
		msgContent.put("inviteType", group.getInviteType());
		msgContent.put("modifyRight", group.getModifyRight());
		msgContent.put("groupType", group.getGroupType());
		msgContent.put("needAuth", group.getNeedAuth());
		updateGroupInfoMsg.setContent(msgContent);

		return updateGroupInfoMsg;
	}

	/**
	 * <p>
	 * 构建更改个人信息消息TOPIC
	 * 
	 * @param userIdList
	 *            群全部成员用户ID
	 * 
	 * @return
	 * 
	 * @see #buildLeavaGroupMessageTopics(List)
	 * 
	 */
	private static List<String> buildUpdateMemberInfoMessageTopics(List<String> groupUserIdList) {
		return buildLeavaGroupMessageTopics(groupUserIdList);
	}

	/**
	 * <p>
	 * 构建更改个人信息消息
	 * 
	 * @param groupMember
	 *            个人信息
	 * @param avatarUrl
	 *            用户头像
	 * @return
	 */
	private static Message<JSONObject> buildUpdateMemberInfoMessage(GroupMember groupMember, String avatarUrl) {
		Message<JSONObject> updateMemberInfoMsg = new Message<>();
		updateMemberInfoMsg.setVersion(Integer.valueOf(Message.VERSION_1));
		updateMemberInfoMsg.setMessageType(Message.MessageType.MESSAGE_GROUP_SYSTEM.getValue());
		updateMemberInfoMsg.setFromUserId(groupMember.getUserId());
		updateMemberInfoMsg.setFromClientId(Config.get("mqtt.clientId"));
		updateMemberInfoMsg.setMessageId(new StringBuilder("Group-Member-Update-").append(groupMember.getGroupId())
				.append("-").append(System.currentTimeMillis()).toString());
		updateMemberInfoMsg.setSessionType(Message.SessionType.SESSION_GROUP.getValue());
		updateMemberInfoMsg.setSessionId(groupMember.getGroupId());

		long msgTime = System.currentTimeMillis();
		updateMemberInfoMsg.setPublishTime(msgTime);
		updateMemberInfoMsg.setUpdateTime(msgTime);
		updateMemberInfoMsg.setContentType(Message.ContentType.CONTENT_IM_SYSTEM_GROUP_UPDATE_MEMINFO.getValue());

		JSONObject msgContent = new JSONObject();

		msgContent.put("memId", groupMember.getUserId());
		msgContent.put("nikeName", groupMember.getNikeName());
		msgContent.put("avatarUrl", Misc.getServerUri(null, avatarUrl));
		msgContent.put("nikeNameAltered", groupMember.getNikeNameAltered());

		updateMemberInfoMsg.setContent(msgContent);

		return updateMemberInfoMsg;
	}

	/**
	 * <p>
	 * 构建更换群主消息TOPIC
	 *
	 * @param groupUserIdList
	 * 					群成员全部id
	 * @return
	 *
	 * @see #buildLeavaGroupMessageTopics(List)
	 */
	private static List<String> buildChangeGroupOwnerTopics(List<String> groupUserIdList) {
		return buildLeavaGroupMessageTopics(groupUserIdList);
	}

	/**
	 * <p>
	 * 构建更换群主消息
	 *
	 * @param groupId
	 * 				群id
	 * @param oldOwnerId
	 * 				旧群主用户id
	 * @param newOwnerId
	 * 				新群主用户id
	 * @return
	 */
	private static Message<JSONObject> buildChangeGroupOwnerMessage(String groupId, String oldOwnerId, String newOwnerId) {
		Message<JSONObject> changeGroupOwnerMsg = new Message<>();
		changeGroupOwnerMsg.setVersion(Integer.valueOf(Message.VERSION_1));
		changeGroupOwnerMsg.setMessageType(Message.MessageType.MESSAGE_GROUP_SYSTEM.getValue());
		changeGroupOwnerMsg.setFromUserId(oldOwnerId);
		changeGroupOwnerMsg.setFromClientId(Config.get("mqtt.clientId"));
		changeGroupOwnerMsg.setMessageId(new StringBuilder("Group-Change-Owner-").append(groupId).append("-").append(System.currentTimeMillis()).toString());
		changeGroupOwnerMsg.setSessionType(Message.SessionType.SESSION_GROUP.getValue());
		changeGroupOwnerMsg.setSessionId(groupId);
		long msgTime = System.currentTimeMillis();
		changeGroupOwnerMsg.setPublishTime(msgTime);
		changeGroupOwnerMsg.setUpdateTime(msgTime);
		changeGroupOwnerMsg.setContentType(Message.ContentType.CONTENT_IM_SYSTEM_GROUP_CHANGE_OWNER.getValue());
		JSONObject msgContent = new JSONObject();
		msgContent.put("groupId", groupId);
		msgContent.put("userId", newOwnerId);
		changeGroupOwnerMsg.setContent(msgContent);
		return changeGroupOwnerMsg;
	}

}
