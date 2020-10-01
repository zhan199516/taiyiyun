package com.taiyiyun.passport.mqtt.v2;

import com.alibaba.fastjson.JSONObject;
import com.taiyiyun.passport.consts.Config;
import com.taiyiyun.passport.consts.Const;
import com.taiyiyun.passport.dao.group.IGroupMemberDao;
import com.taiyiyun.passport.init.SpringContext;
import com.taiyiyun.passport.mongo.dao.IMessageLogDao;
import com.taiyiyun.passport.mongo.po.MessageLog;
import com.taiyiyun.passport.mqtt.Message;
import com.taiyiyun.passport.po.PublicUser;
import com.taiyiyun.passport.service.IPublicUserBlockService;
import com.taiyiyun.passport.service.IRedisService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by nina on 2018/3/19.
 */
@Service("messageForwardServiceImpl2")
public class MessageForwardServiceImpl implements IMessageForwardService {

    public final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private IMessageLogDao messageLogDao;
    @Resource
    private IPublicUserBlockService userBlockService;
    @Resource
    private IRedisService redisService;

    private static final Integer REVOKE_TIMEOUT = Config.getInt(Const.CONFIG_MESSAGE_REVOKE_TIMEOUT, 120000);

    @Override
    public void receiveMessage(String topic, Message<?> message, int mqMessageId, int mqQos) {
        Message.MessageType messageType = Message.getMessageType(message.getMessageType());
        if(null != message && null != message.getMessageType() && null != messageType) {
            switch (messageType) {
                case MESSAGE_IM_SYSTEM:
                case MESSAGE_IM_GENERIC:
                    //转发聊天消息
                    forwardIMMessage(topic, message);
                    break;
                case MESSAGE_IM_REVOKE:
                    //转发撤销聊天消息
                    revokeMessage(topic, message);
                    break;
                case MESSAGE_CIRCLE_SYSTEM:
                    break;
                case MESSAGE_CIRCLE_GENERIC:
                    //转发文章消息
                    forwardCircleIMMessage(topic, message);
                    break;
                case MESSAGE_CIRCLE_REVOKE:
                    //共享号撤回文章消息
                    forwardMessageCircleRevoke(topic, message);
                    break;
                case MESSAGE_CIRCLE_USERINFO:
                    //共享号用户信息消息
                    forwardMessageCircleUserinfo(topic, message);
                    break;
                case MESSAGE_CIRCLE_LOGIN:
                    //登录消息
                    forwardMessageCircleLogin(topic, message);
                    break;
                case MESSAGE_GROUP_SYSTEM:
                    //转发群系统消息
                    forwardGroupSystemMessage(topic, message);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 下发登录消息
     * @param topic
     * @param message
     */
    private void forwardMessageCircleLogin(String topic, Message<?> message) {
        Message.SessionType sessionType = Message.getSessionType(message.getSessionType());
        if(null != sessionType) {
            try {
                switch (sessionType) {
                    case SESSION_P2P:
                        String newTopic = topic.replace("uplink", "downlink");
                        MessagePublisher.getInstance().publish(newTopic, message);
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                logger.error("下发登录消息异常：" + e.getMessage());
            }
        }
    }

    /**
     * 下发共享号用户信息消息
     * @param topic
     * @param message
     */
    private void forwardMessageCircleUserinfo(String topic, Message<?> message) {
        Message.SessionType sessionType = Message.getSessionType(message.getSessionType());
        if(null != sessionType) {
            try {
                switch (sessionType) {
                    case SESSION_CIRCLE:
                        String newTopic = topic.replace("uplink", "downlink");
                        MessagePublisher.getInstance().publish(newTopic, message);
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                logger.error("下发共享号用户信息消息异常：" + e.getMessage());
            }
        }
    }

    /**
     * 下发文章撤回消息
     * @param topic
     * @param message
     */
    private void forwardMessageCircleRevoke (String topic, Message<?> message) {
        Message.SessionType sessionType = Message.getSessionType(message.getSessionType());
        if(null != sessionType) {
            try {
                switch (sessionType) {
                    case SESSION_CIRCLE:
                        String newTopic = topic.replace("uplink", "downlink");
                        MessagePublisher.getInstance().publish(newTopic, message);
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                logger.error("下发文章撤回消息异常：" + e.getMessage());
            }
        }
    }


    /**
     * 向消息接收方下发群系统消息
     * @param topic
     * @param message
     */
    private void forwardGroupSystemMessage(String topic, Message<?> message) {
        Message.SessionType sessionType = Message.getSessionType(message.getSessionType());
        if(null != sessionType) {
            try {
                switch (sessionType) {
                    case SESSION_GROUP:
                        String newTopic = topic.replace("uplink", "downlink");
                        MessagePublisher.getInstance().publish(newTopic, message);
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                logger.error("下发群系统消息异常：" + e.getMessage());
            }
        }
    }

    /**
     * 向消息接收方下发共享号发文消息
     * @param topic
     * @param message
     */
    private void forwardCircleIMMessage(String topic, Message<?> message) {
        Message.SessionType sessionType = Message.getSessionType(message.getSessionType());
        if(null != sessionType) {
            try {
                switch (sessionType) {
                    case SESSION_CIRCLE:
                        String newTopic = topic.replace("uplink", "downlink");
                        MessagePublisher.getInstance().publish(newTopic, message);
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                logger.error("下发文章消息异常：" + e.getMessage());
            }
        }
    }

    /**
     * 向消息接收方下发IM聊天消息
     * @param topic
     * @param message
     */
    private void forwardIMMessage(String topic, Message<?> message) {
        Message.SessionType sessionType = Message.getSessionType(message.getSessionType());
        if(null != sessionType) {
            try {
                switch (sessionType) {
                    case SESSION_P2P:
                        //下发单聊消息
                        forwardP2PMessage(topic, message);
                        break;
                    case SESSION_GROUP:
                        //下发群聊消息
                        forwardGroupIMMessage(topic, message);
                        break;
                    case SESSION_CIRCLE:
                        //下发共享号消息
                        break;
                    case SESSION_P2P_CIRCLE:
                        //未用到
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                logger.error("转发IM聊天消息异常：" + e.getMessage());
            }
        }
    }

    /**
     * 向消息接收者下发单聊消息
     * @param topic
     * @param message
     */
    private void forwardP2PMessage(String topic, Message<?> message) {
        String forwardToTopic = Message.DOWNLINK_MESSAGE + message.getSessionId();
        PublicUser user = userBlockService.getMyBlock(message.getSessionId(), message.getFromUserId());
        if(null == user) {
            try {
                MessagePublisher.getInstance().publish(forwardToTopic, message);
            } catch (Exception e) {
                logger.error("向消息接收者下发单聊消息异常：" + e.getMessage());
            }
        }
    }

    /**
     * 向群成员下发群聊消息
     * @param topic
     * @param message
     */
    private void forwardGroupIMMessage(String topic, Message<?> message) {
        System.out.println("下发群聊消息***************************");
        String groupId = message.getSessionId();
        String fromUserId = message.getFromUserId();
        String toUserId = message.getToUserId();
        try {
            if(StringUtils.isEmpty(toUserId)) {
                List<String> userIds = cacheUserIds(groupId);
                if(userIds != null && userIds.size() > 0 && (userIds.contains(fromUserId) || message.getContentType() == Message.ContentType.CONTENT_IM_SYSTEM_GROUP_MEMBER_LEAVE.getValue())) {
                        for(String userId : userIds) {
                            if(!StringUtils.equalsIgnoreCase(fromUserId, userId)) {
                                String toUserTopic = Message.DOWNLINK_MESSAGE + userId;
                                MessagePublisher.getInstance().publish(toUserTopic, message);
                            }
                        }
                }
            } else {
                String toUserTopic = Message.DOWNLINK_MESSAGE + toUserId;
                MessagePublisher.getInstance().publish(toUserTopic, message);
            }
        } catch (Exception e) {
            logger.error("向群成员下发群聊消息异常：" + e.getMessage());
        }
    }

    private void revokeMessage(String topic, Message<?> message) {
        String messageLogId = Message.bulidUniquedId(topic, message);
        MessageLog messageLog = messageLogDao.findOne(messageLogId);
        if(null != messageLog) {
            Long messageTimeout = System.currentTimeMillis() - message.getPublishTime();
            if (messageTimeout <= REVOKE_TIMEOUT) {
                updateMessageLogRevoked(messageLogId);
                forwardIMMessage(topic, message);
            } else {
                logger.error("消息：" + message.toString() + "， 撤回失败。已超时：" + messageTimeout + "毫秒");
            }
        } else {
            logger.error("消息：" + message.toString() + ", 撤回失败。消息不存在。");
        }
    }

    private void updateMessageLogRevoked(String id) {
        Map<String, Object> params = new HashMap<>();
        params.put("isRevoked", new Boolean(true));
        params.put("updateTime", System.currentTimeMillis());
        messageLogDao.update(id, params);
    }


    /**
     * 向消息发送者下发聊天成功消息
     * @param topic
     * @param message
     * @param mqMessageId
     * @param mqQos
     */
    public void forwardMessageForSender(String topic, Message<JSONObject> message, int mqMessageId, int mqQos) {
        Message.MessageType messageType = Message.getMessageType(message.getMessageType());
        if(null != message && null != message.getMessageType() && null != messageType) {
            switch (messageType) {
                case MESSAGE_IM_SYSTEM:
                case MESSAGE_IM_GENERIC:
                    forwardImMessageForSender(topic, message);
                    break;
                case MESSAGE_IM_REVOKE:
                    break;
                case MESSAGE_CIRCLE_GENERIC:
                    break;
                case MESSAGE_GROUP_SYSTEM:
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 向消息发送者下发IM聊天消息
     * @param topic
     * @param message
     */
    private void forwardImMessageForSender(String topic, Message<?> message) {
        Message.SessionType sessionType = Message.getSessionType(message.getSessionType());
        if( null != sessionType) {
            try {
                switch (sessionType) {
                    case SESSION_P2P:
                        System.out.println("下发单聊消息=========\r\n");
                        String forwardFromTopic = Message.DOWNLINK_MESSAGE + new String(topic.substring(topic.lastIndexOf("/") + 1));
                        MessagePublisher.getInstance().publish(forwardFromTopic, message);
                        break;
                    case SESSION_GROUP:
                        if(message.getContentType() != Message.ContentType.CONTENT_IM_SYSTEM_GROUP_MEMBER_LEAVE.getValue()) {
                            System.out.println("下发群聊消息==========\r\n");
                            String toUserId = message.getToUserId();
                            if(StringUtils.isEmpty(toUserId)) {
                                String fromUserTopic = Message.DOWNLINK_MESSAGE + message.getFromUserId();
                                MessagePublisher.getInstance().publish(fromUserTopic, message);
                            }
                        }
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                logger.error("向发送者转发消息异常：" + e.getMessage());
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
}
