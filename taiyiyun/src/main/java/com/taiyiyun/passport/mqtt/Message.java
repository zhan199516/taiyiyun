package com.taiyiyun.passport.mqtt;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * 消息基础结构定义
 * 
 * @author LiuQing
 */
public class Message<T> implements IContentTypeInterface {
	
	@JSONField(serialize = false)
	private static final long serialVersionUID = 7566900274488105270L;

	@JSONField(name = "version")
	private Integer version; // 消息版本号

	@JSONField(name = "messageType")
	private Integer messageType; // 消息类型

	@JSONField(name = "fromUserId")
	private String fromUserId; // 用户标识，全网唯一

	private String toUserId; // 标识消息发送给群里的指定用户

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
	private T content; // 消息内容

	private Boolean immigrate; //只有迁移的时候使用

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

	public T getContent() {
		return content;
	}

	public void setContent(T content) {
		this.content = content;
	}

	public String getToUserId() {
		return toUserId;
	}

	public void setToUserId(String toUserId) {
		this.toUserId = toUserId;
	}

	/**
	 * 消息类型
	 * @author LiuQing
	 */
	public static enum MessageType {
		/**
		 * IM系统消息
		 */
		MESSAGE_IM_SYSTEM(1, "IM 系统消息"), 
		
		/**
		 * IM 普通消息
		 */
		MESSAGE_IM_GENERIC(2, "IM 普通消息"), 
		
		/**
		 * IM 撤回消息
		 */
		MESSAGE_IM_REVOKE(3, "IM 撤回消息"),
		
		/**
		 * 共享号系统消息
		 */
		MESSAGE_CIRCLE_SYSTEM(4, "共享号系统消息"), 
		
		/**
		 * 共享号普通消息
		 */
		MESSAGE_CIRCLE_GENERIC(5, "共享号普通消息"), 
		
		/**
		 * 共享号撤回消息
		 */
		MESSAGE_CIRCLE_REVOKE(6, "共享号撤回消息"),
		
		/**
		 * 共享号用户信息消息
		 */
		MESSAGE_CIRCLE_USERINFO(7, "共享号用户信息消息"),

		/**
		 * 登录消息
		 */
		MESSAGE_CIRCLE_LOGIN(8, "登录消息"),
		
		
		MESSAGE_GROUP_SYSTEM(9, "群聊相关通知消息"),

		/**
		 * 心跳消息
		 */
		MESSAGE_HEART_BEAT(11, "心跳消息");

		Integer value;

		String description;

		MessageType(Integer value, String description) {
			this.value = value;
			this.description = description;
		}

		public int getValue() {
			return value;
		}

		public void setValue(Integer value) {
			this.value = value;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

	}
	
	/**
	 * 会话类型
	 * @author LiuQing
	 */
	public static enum SessionType {
		
		SESSION_P2P_CIRCLE(0, "P2P 会话和共享号文章（仅在本地同时查询 P2P 消息和共享号消息时使用）"),
		SESSION_P2P(1, "P2P 会话"), 
		SESSION_GROUP(2, "群聊会话"), 
		SESSION_CIRCLE(3, "共享号文章会话");
		
		Integer value;

		String description;

		SessionType(Integer value, String description) {
			this.value = value;
			this.description = description;
		}

		public int getValue() {
			return value;
		}

		public void setValue(Integer value) {
			this.value = value;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}
	}
	
	/**
	 * 内容类型
	 * @author LiuQing
	 */
	public static enum ContentType {
		
		CONTENT_IM_SYSTEM_ADD_FRIEND_RESQUEST(101, "添加好友请求"),
		CONTENT_IM_SYSTEM_ADD_FRIEND_RESPONSE(102, "添加好友响应"),
		CONTENT_IM_GENERIC_TEXT(201, "普通 IM 对话消息"),
		CONTENT_IM_GENERIC_IMAGE(202, "IM 图片消息"),
		CONTENT_IM_GENERIC_AUDIO(203, "IM 语音消息"),
		CONTENT_IM_GENERIC_FILE(204, "IM 文件消息"),
		CONTENT_IM_GENERIC_TRANSFER_APPLY(211, "IM 转账通知（待补充）"),
		CONTENT_IM_GENERIC_TRANSFER_ACCEPT(212, "IM 转账接收（待补充）"),
		CONTENT_IM_GENERIC_RD_REDPAY(214, "IM 红包通知消息"),
		CONTENT_IM_GENERIC_RD_REDGRABED(215, "IM 红包被接收通知消息"),
		CONTENT_IM_GENERIC_RD_REDCOMPLATED(216, "IM 红包金额到账通知消息"),
		CONTENT_IM_GENERIC_LN(221, "IM 闪电网络消息（待补充）"),
		CONTENT_CIRCLE_SYSTEM(401, "共享号系统消息"),
		CONTENT_CIRCLE_GENERIC_TITLE_TEXT(501, "共享号消息（包含标题和内容）"),
		CONTENT_CIRCLE_GENERIC_TEXT(502, "共享号消息（只包含内容）"),
		CONTENT_CIRCLE_GENERIC_ONE_IMAGE(503, "共享号消息（包含标题和 1 张缩略图）"),
		CONTENT_CIRCLE_GENERIC_THREE_IMAGE(504, "共享号消息（包含标题和 3 张缩略图）"),
		CONTENT_CIRCLE_USERINFO_UPDATE(701, "共享号资料更新消息"),
		CONTENT_CIRCLE_LOGIN(801, "登录信息更新"),

		CONTENT_IM_SYSTEM_GROUP_JOIN_RESQUEST(901, "邀请请求审批"),
		CONTENT_IM_SYSTEM_GROUP_JOIN_RESPONSE(902, "入群通知"),
		CONTENT_IM_SYSTEM_GROUP_MEMBER_LEAVE(903, "退群通知"),
		CONTENT_IM_SYSTEM_GROUP_UPDATE_GROUPINFO(904, "变更群信息"),
		CONTENT_IM_SYSTEM_GROUP_UPDATE_MEMINFO(905, "变更成员信息"),
		CONTENT_IM_SYSTEM_GROUP_CHANGE_OWNER(906, "群主变更"),
		CONTENT_IM_SYSTEM_GROUP_CREATE_GROUP(907, "建群通知消息"),

		CONTENT_HEART_BEAT(1101, "心跳消息");



		Integer value;

		String description;

		ContentType(Integer value, String description) {
			this.value = value;
			this.description = description;
		}

		public int getValue() {
			return value;
		}

		public void setValue(Integer value) {
			this.value = value;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}
	}
	
	public static final String VERSION_1 = "1";
	
	public static final String DOWNLINK_MESSAGE = "downlink/message/";
	
	public static final String DOWNLINK_SYSTEM = "downlink/system/";

	public static final String UPLINK_SYSTEM = "uplink/system/";
	
	public static final String UPLINK_MESSAGE = "uplink/message/";

	public static final String UPLINK_PUBLIC_SHARE_MESSAGE = "uplink/public/share_message/";
	
	public static final String DOWNLINK_PUBLIC_SHARE_MESSAGE = "downlink/public/share_message/";

	public static final String UPLINK_PUBLIC_SHARE_MESSAGE_USERINFO = "uplink/public/share_message/%s/userinfo";
	
	public static final String DOWNLINK_PUBLIC_SHARE_MESSAGE_USERINFO = "downlink/public/share_message/%s/userinfo";


	
	/**
	 * 根据类型值获取对应的消息类型
	 * @param value 消息类型值
	 * @return
	 */
	public static MessageType getMessageType(Integer value) {
		if (null != value) {
			MessageType[] messageTypes = MessageType.values();
			for(MessageType mt : messageTypes) {
				if (mt.value.intValue() == value.intValue()) {
					return mt;
				}
			}
		}
		return null;
	}
	
	/**
	 * 根据类型值获取对应的会话类型
	 * @param value 会话类型值
	 * @return
	 */
	public static SessionType getSessionType(Integer value) {
		if (null != value) {
			SessionType[] sessionTypes = SessionType.values();
			for(SessionType st : sessionTypes) {
				if (st.value.intValue() == value.intValue()) {
					return st;
				}
			}
		}
		return null;
	}
	
	/**
	 * 根据类型值获取对应的内容类型
	 * @param value 内容类型值
	 * @return
	 */
	public static ContentType getContentType(Integer value) {
		if (null != value) {
			ContentType[] contentTypes = ContentType.values();
			for(ContentType ct : contentTypes) {
				if (ct.value.intValue() == value.intValue()) {
					return ct;
				}
			}
		}
		return null;
	}

	
	public static final String bulidUniquedId(String topic, Message<?> message) {
		return DigestUtils.sha256Hex(topic + "-" + message.getFromClientId() + "-" + message.getFromUserId() + "-" + message.getMessageId());
	}
	
	public String toString() {
		return JSON.toJSONString(this);
	}
}
