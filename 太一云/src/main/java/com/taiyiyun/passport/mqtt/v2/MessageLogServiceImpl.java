package com.taiyiyun.passport.mqtt.v2;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.DBObject;
import com.taiyiyun.passport.aliyun.push.*;
import com.taiyiyun.passport.bean.UserCache;
import com.taiyiyun.passport.consts.Const;
import com.taiyiyun.passport.dao.IPublicArticleDao;
import com.taiyiyun.passport.dao.IPublicDownlinkmessageLogjobFailDao;
import com.taiyiyun.passport.dao.IPublicUserConfigDao;
import com.taiyiyun.passport.dao.IPublicUserDao;
import com.taiyiyun.passport.dao.group.IGroupDao;
import com.taiyiyun.passport.dao.group.IGroupMemberDao;
import com.taiyiyun.passport.init.SpringContext;
import com.taiyiyun.passport.mongo.dao.IMessageLogDao;
import com.taiyiyun.passport.mongo.po.MessageLog;
import com.taiyiyun.passport.mqtt.Message;
import com.taiyiyun.passport.po.PublicArticle;
import com.taiyiyun.passport.po.PublicDownlinkmessageLogjobFail;
import com.taiyiyun.passport.po.PublicUser;
import com.taiyiyun.passport.po.PublicUserConfig;
import com.taiyiyun.passport.po.group.Group;
import com.taiyiyun.passport.service.IPublicArticleService;
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
import java.util.*;

/**
 * Created by nina on 2018/3/21.
 */
@Service("messageLogServiceImpl2")
public class MessageLogServiceImpl implements IMessageLogService {

    @Resource
    private IMessageLogDao messageLogDao;
    @Resource
    private IPublicUserBlockService userBlockService;
    @Resource
    private IRedisService redisService;
    @Resource
    private IPublicArticleDao articleDao;
    @Resource
    private IPublicUserDao userDao;
    @Resource
    private IPublicDownlinkmessageLogjobFailDao logjobFailDao;
    @Resource
    private IPublicArticleService publicArticleService;
    @Resource
    private IGroupMemberDao groupMemberDao;
    @Resource
    private IPublicUserConfigDao userConfigDao;

    public final Logger logger = LoggerFactory.getLogger(getClass());


    @Override
    public void receiveMessage(String topic, Message<JSONObject> message, int mqMessageId, int mqQos) {
        Message.MessageType messageType = Message.getMessageType(message.getMessageType());
        if(messageType == null) return;
        switch (messageType) {
            case MESSAGE_IM_SYSTEM:
            case MESSAGE_IM_GENERIC:
                //记录聊天消息日志
                logIMMessage(topic, message, mqMessageId, mqQos);
                break;
            case MESSAGE_IM_REVOKE:
                //记录撤销聊天消息日志
                break;
            case MESSAGE_CIRCLE_SYSTEM:
                break;
            case MESSAGE_CIRCLE_GENERIC:
                //记录文章消息日志
                logCircleGenericMessage(topic, message, mqMessageId, mqQos);
                break;
            case MESSAGE_CIRCLE_REVOKE:
                //记录共享号撤回文章消息日志
                logCircleRevokeMessage(topic, message, mqMessageId, mqQos);
                break;
            case MESSAGE_CIRCLE_USERINFO:
                //记录共享号用户信息消息日志
                logCircleUserinfoMessage(topic, message, mqMessageId, mqQos);
                break;
            case MESSAGE_CIRCLE_LOGIN:
                //记录共享号登录消息日志
                logCircleLoginMessage(topic, message, mqMessageId, mqQos);
                break;
            case MESSAGE_GROUP_SYSTEM:
                //记录群系统消息日志
                logGroupSystemMessage(topic, message, mqMessageId, mqQos);
                break;
            default:
                break;
        }
    }

    /**
     * 记录群聊系统消息日志
     * @param topic
     * @param message
     * @param mqMessageId
     * @param mqQos
     */
    private void logGroupSystemMessage(String topic, Message<JSONObject> message, int mqMessageId, int mqQos) {
        Message.SessionType sessionType = Message.getSessionType(message.getSessionType());
        if(null != sessionType) {
            switch (sessionType) {
                case SESSION_GROUP:
                    logGroupSystemMessageDetail(topic, message, mqMessageId,mqQos);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 记录群聊系统消息日志详情
     * @param topic
     * @param message
     * @param mqMessageId
     * @param mqQos
     */
    private void logGroupSystemMessageDetail(String topic, Message<JSONObject> message, int mqMessageId, int mqQos) {
        String newTopic = topic.replace("uplink", "downlink");
        try {
            save(newTopic, message, mqMessageId, mqQos);
        } catch (Exception e) {
            saveLogJobFail(newTopic, message, e.getMessage());
        }
    }

    /**
     * 记录共享号登录消息日志
     * @param topic
     * @param message
     * @param mqMessageId
     * @param mqQos
     */
    private void logCircleLoginMessage(String topic, Message<JSONObject> message, int mqMessageId, int mqQos) {
        Message.SessionType sessionType = Message.getSessionType(message.getSessionType());
        if(null != sessionType) {
            switch (sessionType) {
                case SESSION_P2P:
                    logCircleLoginMessageDetail(topic, message, mqMessageId,mqQos);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 记录共享号登录消息日志详情
     * @param topic
     * @param message
     * @param mqMessageId
     * @param mqQos
     */
    private void logCircleLoginMessageDetail(String topic, Message<JSONObject> message, int mqMessageId, int mqQos) {
        String newTopic = topic.replace("uplink", "downlink");
        try {
            save(newTopic, message, mqMessageId, mqQos);
        } catch (Exception e) {
            saveLogJobFail(newTopic, message, e.getMessage());
        }
    }

    /**
     * 记录共享号用户信息消息日志
     * @param topic
     * @param message
     * @param mqMessageId
     * @param mqQos
     */
    private void logCircleUserinfoMessage(String topic, Message<JSONObject> message, int mqMessageId, int mqQos) {
        Message.SessionType sessionType = Message.getSessionType(message.getSessionType());
        if(null != sessionType) {
            switch (sessionType) {
                case SESSION_CIRCLE:
                    logCircleUserinfoMessageDetail(topic, message, mqMessageId,mqQos);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 记录共享号用户信息消息日志详情
     * @param topic
     * @param message
     * @param mqMessageId
     * @param mqQos
     */
    private void logCircleUserinfoMessageDetail(String topic, Message<JSONObject> message, int mqMessageId, int mqQos) {
        String newTopic = topic.replace("uplink", "downlink");
        try {
            save(newTopic, message, mqMessageId, mqQos);
        } catch (Exception e) {
            saveLogJobFail(newTopic, message, e.getMessage());
        }
    }

    /**
     * 记录共享号文章撤销日志
     * @param topic
     * @param message
     * @param mqMessageId
     * @param mqQos
     */
    private void logCircleRevokeMessage(String topic, Message<JSONObject> message, int mqMessageId, int mqQos) {
        Message.SessionType sessionType = Message.getSessionType(message.getSessionType());
        if(null != sessionType) {
            switch (sessionType) {
                case SESSION_CIRCLE:
                    logCircleRevokeMessageDetail(topic, message, mqMessageId,mqQos);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 记录共享号文章撤销日志详情
     * @param topic
     * @param message
     * @param mqMessageId
     * @param mqQos
     */
    private void logCircleRevokeMessageDetail(String topic, Message<JSONObject> message, int mqMessageId, int mqQos) {
        String newTopic = topic.replace("uplink", "downlink");
        try {
            save(newTopic, message, mqMessageId, mqQos);
            revokeMessage(newTopic, message);
        } catch (Exception e) {
            saveLogJobFail(newTopic, message, e.getMessage());
        }
    }

    /**
     * 记录文章消息日志
     * @param topic
     * @param message
     * @param mqMessageId
     * @param mqQos
     */
    private void logCircleGenericMessage(String topic, Message<JSONObject> message, int mqMessageId, int mqQos) {
        Message.SessionType sessionType = Message.getSessionType(message.getSessionType());
        if(null != sessionType) {
            switch (sessionType) {
                case SESSION_CIRCLE:
                    logCircleGenericMessageDetail(topic, message, mqMessageId, mqQos);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 记录文章消息日志详情
     * @param topic
     * @param message
     * @param mqMessageId
     * @param mqQos
     */
    private void logCircleGenericMessageDetail(String topic, Message<JSONObject> message, int mqMessageId, int mqQos) {
        boolean flag = false;
        String newTopic = topic.replace("uplink", "downlink");
        try {
            save(newTopic, message, mqMessageId, mqQos);
            flag = true;
        } catch (Exception e) {
            saveLogJobFail(newTopic, message, e.getMessage());
        }
        if(flag) {
            String userId = message.getFromUserId();
            String articleId = message.getMessageId();
            PublicUser user = userDao.getByUserId(userId);
            PublicArticle article = articleDao.getById(articleId);
            publicArticleService.pushALiMessage(article, user);
        }
    }
    /**
     * 记录消息接收方IM聊天消息
     * @param topic
     * @param message
     * @param mqMessageId
     * @param mqQos
     */
    private void logIMMessage(String topic, Message<JSONObject> message, int mqMessageId, int mqQos) {
        Message.SessionType sessionType = Message.getSessionType(message.getSessionType());
        if(null != sessionType) {
            try {
                switch (sessionType) {
                    case SESSION_P2P:
                        //记录单聊消息日志
                        logP2PMessage(topic, message, mqMessageId, mqQos);
                        break;
                    case SESSION_GROUP:
                        //记录群聊消息日志
                        logGroupMessage(topic, message, mqMessageId, mqQos);
                        break;
                    case SESSION_CIRCLE:
                        //记录共享号消息(不在这里处理)
                        break;
                    case SESSION_P2P_CIRCLE:
                        //未用到
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                logger.error("记录IM聊天日志消息异常：" + e.getMessage());
            }
        }
    }

    /**
     * 记录群聊消息日志
     * @param topic
     * @param message
     * @param mqMessageId
     * @param mqQos
     */
    private void logGroupMessage(String topic, Message<JSONObject> message, int mqMessageId, int mqQos) {
        boolean flag = false;
        String groupId = message.getSessionId();
        String fromUserId = message.getFromUserId();
        String toUserId = message.getToUserId();
        List<String> userIds = cacheUserIds(groupId);
        List<String> userIdsTmp = copy(userIds);
        if(StringUtils.isNotEmpty(toUserId)) {
            //handleToUser(fromUserId, message, mqMessageId, mqQos);
            handleToUser(toUserId, message, mqMessageId, mqQos);
            pushAliMessage(topic, message, toUserId);
            return;
        }
        try {
            if(userIds != null && userIds.size() > 0 && (userIds.contains(fromUserId) || message.getContentType() == Message.ContentType.CONTENT_IM_SYSTEM_GROUP_MEMBER_LEAVE.getValue())) {
                if(message.getContentType() != Message.ContentType.CONTENT_IM_SYSTEM_GROUP_MEMBER_LEAVE.getValue()) {
                    String fromUserTopic = Message.DOWNLINK_MESSAGE + fromUserId;
                    save(fromUserTopic, message, mqMessageId, mqQos);
                    userIdsTmp.remove(fromUserId);
                }
                for(String userId : userIds) {
                    if(!StringUtils.equalsIgnoreCase(fromUserId, userId)) {
                        String toUserTopic = Message.DOWNLINK_MESSAGE + userId;
                        save(toUserTopic, message, mqMessageId, mqQos);
                        userIdsTmp.remove(userId);
                    }
                }
            }
            flag = true;
        } catch (Exception e) {
            for(String userId : userIdsTmp) {
                String toUserTopic = Message.DOWNLINK_MESSAGE + userId;
                saveLogJobFail(toUserTopic, message, e.getMessage());
            }
        }
        if(flag) {//下发群聊阿里云推送消息
            if(userIds != null && !userIds.isEmpty()) {
                List<String> tempUserIds = new ArrayList<>();
                List<String> userIdList = cacheSetDisturbUserIdsForGroup(message.getSessionId());
                for(String userId : userIds) {
                    boolean b = hasSetGlobalDisturb(userId);
                    if(b) {
                        continue;
                    }
                    if(userIdList.contains(userId)) {
                        continue;
                    }
                    if(StringUtils.equalsIgnoreCase(userId, message.getFromUserId())) {
                        continue;
                    }
                    tempUserIds.add(userId);
                }
                String targetValue = StringUtils.join(tempUserIds, ",");
                pushAliMessage(topic, message, targetValue);
            }
        }
    }


    /**
     * 是否设置了全局免打扰
     * @param userId
     * @return
     */
    private boolean hasSetGlobalDisturb(String userId) {
        PublicUserConfig config = new PublicUserConfig();
        config.setSetupUserId(userId);
        config.setUserType(0);
        config.setIsDisturb(1);
        List<PublicUserConfig> list = userConfigDao.list(config);
        int size = list.size();
        if(list == null || size == 0) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 针对某个用户的消息免打扰
     * @param setupUserId 设置用户ID
     * @param toUserId 针对哪个用户免打扰的用户ID
     * @return
     */
    private boolean hasSetDisturbForUser(String setupUserId, String toUserId) {
        PublicUserConfig config = new PublicUserConfig();
        config.setSetupUserId(setupUserId);
        config.setTargetId(toUserId);
        config.setIsDisturb(1);
        List<PublicUserConfig> list = userConfigDao.list(config);
        int size = list.size();
        if(list == null || size == 0) {
            return false;
        }
        return true;
    }

    /**
     * 查询群中设置了消息免打扰的用户ID列表
     * @param groupId 群ID
     * @return
     */
    private List<String> querySetDisturbUserIdsForGroup(String groupId) {
        return groupMemberDao.selectSetDisturbUserIds(groupId);
    }


    /**
     * 处理群聊中单独给某个用户发消息的日志记录
     * @param userId
     * @param message
     * @param mqMessageId
     * @param mqQos
     */
    private void handleToUser(String userId, Message<JSONObject> message, int mqMessageId, int mqQos) {
        try {
            String topic = Message.DOWNLINK_MESSAGE + userId;
            save(topic, message, mqMessageId, mqQos);
        } catch (Exception e) {
            String topic = Message.DOWNLINK_MESSAGE + userId;
            saveLogJobFail(topic, message, e.getMessage());
        }
    }

    private List<String> copy(List<String> srcList) {
        List<String> destList = new ArrayList<>();
        if(srcList != null && !srcList.isEmpty()) {
            for(String str : srcList) {
                destList.add(str);
            }
        }
        return destList;
    }

    /**
     * 记录单聊消息的日志
     * @param topic
     * @param message
     */
    private void logP2PMessage(String topic, Message<JSONObject> message, int mqMessageId, int mqQos) {
        boolean flagFrom = false;
        boolean flagTo = false;
        String logFromTopic = Message.DOWNLINK_MESSAGE + message.getFromUserId();
        try {
            save(logFromTopic, message, mqMessageId, mqQos);
            flagFrom = true;
        } catch (Exception e) {
            saveLogJobFail(logFromTopic, message, e.getMessage());
        }
        String logToTopic = Message.DOWNLINK_MESSAGE + message.getSessionId();
        try {
            PublicUser user = userBlockService.getMyBlock(message.getSessionId(), message.getFromUserId());
            if(null == user) {
                save(logToTopic, message, mqMessageId, mqQos);
            }
            flagTo = true;
        } catch (Exception e) {
            saveLogJobFail(logToTopic, message, e.getMessage());
        }
        if(flagFrom && flagTo) {
            boolean bGlobal = hasSetGlobalDisturb(message.getSessionId());
            if(!bGlobal) {
                boolean b = hasSetDisturbForUser(message.getSessionId(), message.getFromUserId());
                if(!b) {
                    pushAliMessage(topic, message, message.getSessionId());
                }
            }
        }
    }

    private void pushAliMessage(String topic, Message<?> message, String targetValue) {
        String id = Message.bulidUniquedId(topic, message);
        if (getLock(id)) {
            PushMessage pushMessage = new PushMessage();

            pushMessage.setDeviceType(PushDeviceType.ALL);
            pushMessage.setPushType(PushType.NOTICE);
            Integer sessionType = message.getSessionType();
            if(sessionType != null && sessionType.intValue() == 2) {
                pushMessage.setTitle(getGroupNameFromCache(message.getSessionId()));
            } else {
                pushMessage.setTitle(getUserNameFromCache(message.getFromUserId(), true));
            }
            pushMessage.setiOSSubtitle("");
            pushMessage.setSummary(getMessageBody(message));
            pushMessage.setTarget(PushTarget.ACCOUNT);
            pushMessage.setTargetValue(targetValue);
            pushMessage.setBody(getMessageBody(message));

            pushMessage.addExtParameter("fromUserId", message.getFromUserId());
            pushMessage.addExtParameter("sessionId", message.getSessionId());
            pushMessage.addExtParameter("messageType", String.valueOf(message.getMessageType()));
            pushMessage.addExtParameter("_NOTIFICATION_BAR_STYLE_", 1);

            Push.getInstance().addMessage(pushMessage);

           //SMSHelper.sendMessageInfo(message);
        }
    }

    private String getGroupNameFromCache(String groupId) {
        String groupName = redisService.get(Const.GROUP_GROUPNAME_IM + groupId);
        if(StringUtils.isEmpty(groupName)) {
            IGroupDao groupDao = SpringContext.getBean(IGroupDao.class);
            Group group = groupDao.selectByPrimarykey(groupId);
            groupName = group.getGroupName();
            redisService.evict(Const.GROUP_GROUPNAME_IM + groupId);
            redisService.put(Const.GROUP_GROUPNAME_IM + groupId, groupName, 3600);
        }
        if (StringUtil.isNotEmpty(groupName) && groupName.length() > 20) {
            return groupName.substring(0, 17) + "...";
        }
        return groupName;
    }

    private String getUserNameFromCache(String userId, boolean isSub) {
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
                if(isSub) {
                    if (StringUtil.isNotEmpty(userCache.getUserName()) && userCache.getUserName().length() > 20) {
                        return userCache.getUserName().substring(0, 17) + "...";
                    }
                }
                return userCache.getUserName();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return DEFAULT_VALUE;
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

        if(Message.ContentType.CONTENT_IM_GENERIC_RD_REDPAY.getValue() == message.getContentType()) {
            String contentMessage = "发送了一个群红包";
            String userName = getUserNameFromCache(message.getFromUserId(), true);
            String text = content.getString("text");
            if(StringUtils.isNotEmpty(userName) && StringUtils.isNotEmpty(text)) {
                contentMessage = userName + ":[红包]" + text;
            }
            return contentMessage;
        }
        if(Message.ContentType.CONTENT_IM_GENERIC_TEXT.getValue() == message.getContentType()) {
            Message.SessionType sessionType = Message.getSessionType(message.getSessionType());
            switch (sessionType) {
                case SESSION_P2P :
                    break;
                case SESSION_GROUP:
                    String contentMessage = "发送了一条群消息";
                    String userName = getUserNameFromCache(message.getFromUserId(), false);
                    String text = content.getString("text");
                    if(StringUtils.isNotEmpty(userName) && StringUtils.isNotEmpty(text)) {
                        contentMessage = userName + ":" + text;
                    }
                    return contentMessage;
                default:
                    break;
            }
            return content.getString("text");
        }
        return content.getString("text");
    }

    public boolean getLock(String id) {
        return redisService.setNX("lock.message:" + id, id);
    }

    private void saveLogJobFail(String topic, Message<JSONObject> message, String errMsg) {
        Date now = new Date();
        PublicDownlinkmessageLogjobFail logjobFail = new PublicDownlinkmessageLogjobFail();
        logjobFail.setCreateTime(now);
        logjobFail.setErrMsg(errMsg);
        logjobFail.setMessage(JSONObject.toJSONString(message));
        logjobFail.setMessageId(message.getMessageId());
        logjobFail.setStatus(0);
        logjobFail.setTopic(topic);
        logjobFail.setUpdateTime(now);
        logjobFailDao.insertSelective(logjobFail);
    }


    public void save(String topic, Message<JSONObject> message, int mqMessageId, int mqQos) {
        Message.MessageType messageType = Message.getMessageType(message.getMessageType());
        if (null != message.getMessageType() && null != messageType && null == message.getImmigrate()) {
            if(message.getUpdateTime() == null) {
                message.setUpdateTime(System.currentTimeMillis());  // 更新消息更新时间
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
        MessageLog messageLog = messageLogDao.findOne(log.getId());
        if(null == messageLog) {
            messageLogDao.insert(log);
        }
    }

    //撤回文章
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

    private List<String> cacheSetDisturbUserIdsForGroup(String groupId) {
        IGroupMemberDao gmDao = SpringContext.getBean(IGroupMemberDao.class);
        String userIdsStr = redisService.get(Const.GROUP_SETDISTURB_USERIDS + groupId);
        if(StringUtils.isEmpty(userIdsStr)) {
            List<String> userIds = gmDao.selectSetDisturbUserIds(groupId);
            if(userIds != null && !userIds.isEmpty()) {
                String userIdJson = JSONObject.toJSONString(userIds);
                redisService.evict(Const.GROUP_SETDISTURB_USERIDS + groupId);
                redisService.put(Const.GROUP_SETDISTURB_USERIDS + groupId, userIdJson, 3600);
            }
            return userIds;
        } else {
            List<String> userIds = (List<String>)JSONObject.parse(userIdsStr);
            return userIds;
        }
    }
}
