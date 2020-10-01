package com.taiyiyun.passport.controller;

import com.taiyiyun.passport.bean.UserDetails;
import com.taiyiyun.passport.consts.Config;
import com.taiyiyun.passport.dao.IPublicUserFollowerDao;
import com.taiyiyun.passport.dao.group.IGroupMemberDao;
import com.taiyiyun.passport.language.LangResource;
import com.taiyiyun.passport.language.PackBundle;
import com.taiyiyun.passport.mongo.po.MessageLog;
import com.taiyiyun.passport.mongo.service.IMessageLogService;
import com.taiyiyun.passport.mqtt.Message;
import com.taiyiyun.passport.mqtt.Message.MessageType;
import com.taiyiyun.passport.po.PublicUser;
import com.taiyiyun.passport.po.PublicUserFollower;
import com.taiyiyun.passport.po.group.GroupMember;
import com.taiyiyun.passport.po.message.LatestTime;
import com.taiyiyun.passport.po.message.UserListEntity;
import com.taiyiyun.passport.service.IPublicArticleService;
import com.taiyiyun.passport.service.IPublicUserService;
import com.taiyiyun.passport.util.SessionUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;

/**
 * Created by okdos on 2017/6/26.
 */


@Controller
@RequestMapping("/api/share/message")
public class ApiOfflineController extends BaseController {

    @Resource
    IPublicArticleService ser;

    @Resource
    IPublicUserService userSer;
    
    @Resource
	private IMessageLogService messageLogService;

    @Resource
    private IPublicUserFollowerDao userFollowerDao;
    @Resource
    private IGroupMemberDao gmDao;

    @ResponseBody
    @RequestMapping(value = "/requestShareGenericMessages",method = {RequestMethod.GET}, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String RequestShareGenericMessages(String userId, Long start, Long end, HttpServletRequest request){

        //绑定的语言
        ResourceBundle resourceBundle = (ResourceBundle)request.getAttribute("lang.name");

        String apiName = "GET+requestShareGenericMessages";



        String[] topics = {Message.DOWNLINK_PUBLIC_SHARE_MESSAGE + userId};
        if(start == null || start == 0){
            start = null;

            long focusTime = 0;
            UserDetails userDetails = SessionUtil.getUserDetails(request);
            if(userDetails != null){
                PublicUserFollower userLike = userFollowerDao.getRecord(userDetails.getUserId(), userId);
                if(userLike != null){
                    if(userLike.getFocusTime() != null){
                        focusTime = userLike.getFocusTime().getTime();
                    }
                }
            }

            long beforeTime = new Date().getTime() - Config.getInt("message.offline.before", 604800)*1000;
            if(focusTime > beforeTime){
                beforeTime = focusTime;
            }

            start = beforeTime;
        }
        if(end != null && end == 0){
            end = null;
        }

        MessageType[] types = new MessageType[]{
                MessageType.MESSAGE_CIRCLE_SYSTEM,
                MessageType.MESSAGE_CIRCLE_GENERIC,
                MessageType.MESSAGE_CIRCLE_REVOKE/*,
                MessageType.MESSAGE_CIRCLE_USERINFO*/
        };

        List<MessageLog> messages = messageLogService.findByTopicAndPublishTimeAndMessageType(topics, start, end, types, MessageLog.class);
        
//        MessageLog shareMessage = messageLogService.getLastShareMsgByTopic(String.format(Message.DOWNLINK_PUBLIC_SHARE_MESSAGE_USERINFO, userId));
        List<String> sessions = new ArrayList<>();
        sessions.add(userId);

        List<Integer> sessionTypes = new ArrayList<>();
        sessionTypes.add(Message.SessionType.SESSION_CIRCLE.getValue());

        List<Integer> messageTypes = new ArrayList<>();
        messageTypes.add(MessageType.MESSAGE_CIRCLE_USERINFO.getValue());

        MessageLog shareMessage = messageLogService.getLastMsg(sessions, sessionTypes ,messageTypes);

        if(null != shareMessage && shareMessage.getUpdateTime() != null && shareMessage.getUpdateTime() >= (start == null ? 0: start)) {
            messages.add(0 ,shareMessage);
        }

        Map<String, Object> extData = new HashMap<String, Object>();
        extData.put("hasMore", (null != messages && messages.size() >= 20));
        return toJson(0, "", apiName, messages, extData);
    }

    private Message<LatestTime> newMsg(){
        Message<LatestTime> dt = new Message<>();
        dt.setVersion(1);
        dt.setMessageType(4);
        dt.setSessionType(3);
        dt.setContentType(401);
        return dt;
    }

    //获取系统消息
    //包括 IM 系统消息（messageType = 1，contentType = 103）
    //包含共享号系统消息（messageType = 4，contentType = 401）
    //包含 创建群的系统消息（messageType = 9, contentType = 901,902,903,904,905,906,907） 注：不包含离群的通知消息和聊天消息
    @ResponseBody
    @RequestMapping(value = "/requestAllSystemMessages",method = {RequestMethod.POST}, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String RequestShareSystemMessages(@RequestBody UserListEntity query, HttpServletRequest request) throws IOException {
        try{
            List<Message<LatestTime>> messages = new ArrayList<>();
            for(int i = 0; i < query.getUserIdList().size(); i++){
    		    String userId = query.getUserIdList().get(i);
                String topicMessage = "downlink/public/share_message/" + userId;
                String topicUserInfo = "downlink/public/share_message/"+userId+"/userinfo";
                long focusTime = 0;
                UserDetails userDetails = SessionUtil.getUserDetails(request);
                if(userDetails != null){
                    PublicUserFollower userLike = userFollowerDao.getRecord(userDetails.getUserId(), userId);
                    if(userLike != null){
                        if(userLike.getFocusTime() != null){
                            focusTime = userLike.getFocusTime().getTime();
                        }
                    }
                }
                long beforeTime = new Date().getTime() - Config.getInt("message.offline.before", 604800)*1000;
                if(focusTime > beforeTime){
                    beforeTime = focusTime;
                }
                MessageLog msgLog = messageLogService.getLastShareMsgByTopicAndPublishTime(new String[]{topicMessage, topicUserInfo}, beforeTime);

                if(null != msgLog) {
                    Long time = (null == msgLog) ? 0L : msgLog.getUpdateTime();
                    Message<LatestTime> msg = new Message<>();
                    msg.setVersion(Integer.valueOf(Message.VERSION_1));
                    msg.setMessageType(4);
                    msg.setSessionType(3);  //公众号消息一定是3
                    msg.setContentType(401);
                    msg.setSessionId(userId);
                    LatestTime lt = new LatestTime();
                    lt.setLastUpdateTime(time);
                    msg.setContent(lt);
                    messages.add(msg);
                }
            }
            // IM消息最新时间
            UserDetails userDetails = SessionUtil.getUserDetails(request);
            if(null != userDetails) {
                Long start = 0L;
                String userId = userDetails.getUserId(); // 从Session中取得userId
                PublicUser user = userSer.getByUserId(userId);
                start = new Date().getTime() - 24 * 60 * 60 * 1000;
                if(user.getLastMsgPullTime() != null && user.getLastMsgPullTime() < start){
                    start = user.getLastMsgPullTime() - 60 * 1000;
                }
                MessageLog msgLog = messageLogService.getLastMessageByTopicAndPublishTime(new String[]{Message.DOWNLINK_MESSAGE + userId, Message.DOWNLINK_SYSTEM + userId}, start);
                if(null != msgLog) {
                    Long time = (null == msgLog) ? 0L : msgLog.getUpdateTime();
                    Message<LatestTime> msg = new Message<>();
                    msg.setVersion(Integer.valueOf(Message.VERSION_1));
                    msg.setMessageType(1);
                    msg.setSessionType(msgLog.getSessionType());
                    msg.setContentType(103);
                    msg.setSessionId(userId);
                    LatestTime lt = new LatestTime();
                    lt.setLastUpdateTime(time);
                    msg.setContent(lt);
                    messages.add(msg);
                }
            }
            return this.toJson(0, "", "POST+requestShareSystemMessages", messages);

        } catch(Exception ex){
        	ex.printStackTrace();
            return this.toJson(3, ex.toString(), "POST+requestShareSystemMessages", null);
        }

    }
    
    @ResponseBody
	@RequestMapping(value = "requestUserMessages", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	public String requestUserMessages(HttpServletRequest request, Long start, Long end) {

        PackBundle bundle = LangResource.getResourceBundle(request);

		String apiName = "GET+requestUserMessages";
		
		UserDetails userDetails = SessionUtil.getUserDetails(request);
		if(null == userDetails) {
			return toJson(1, bundle.getString("user.not.login"), apiName, null);
		}
		String userId = userDetails.getUserId(); // 从Session中取得userId
        boolean flag = true;
        List<MessageLog> messages = null;
        List<MessageLog> messageLogList = new ArrayList<>();
        int messagesSize = 0;
        List<String> groupIds = gmDao.selectGroupIds(userId);
        //获取群用户信息，用于比较joinTime
        Map<String, GroupMember> gmMap = new HashMap<>();
        int groupIdSize = groupIds.size();
        for(int i = 0; i < groupIdSize ; i++) {
            String groupId = groupIds.get(i);
            Map<String, String> params = new HashMap<>();
            params.put("userId", userId);
            params.put("groupId", groupId);
            GroupMember groupMember = gmDao.selectGroupMemberByGroupIdAndUserId(params);
            gmMap.put(groupId, groupMember);
        }
        PublicUser user = userSer.getByUserId(userId);
        while(flag) {
            if (null == start || 0 == start) {
                if(user.getLastMsgPullTime() == null){
                    start = null;
                } else {
                    //start = 0L; 最近一天的消息，如果查询中的最后消息小于最近一天消息，则使用个查询中的最后消息
                    start = new Date().getTime() - 24 * 60 * 60 * 1000;
                    if(user.getLastMsgPullTime() < start){
                        start = user.getLastMsgPullTime() - 60 * 1000;
                    }
                }
            }
            if (null == end || 0 == end) {
                end = System.currentTimeMillis();
            }
            String[] topics = {Message.DOWNLINK_MESSAGE + userId, Message.DOWNLINK_SYSTEM + userId};
            messages = messageLogService.findByTopicAndPublishTimeAndMessageType(topics, start, end, new MessageType[]{MessageType.MESSAGE_IM_GENERIC, MessageType.MESSAGE_IM_REVOKE, MessageType.MESSAGE_GROUP_SYSTEM}, MessageLog.class);
            // logger.info(apiName + "[userId: " + userId + ", start: " + start + ", end: " + end + "] -> " + JSON.toJSONString(messages));
            messagesSize = messages.size();
            if(messagesSize > 0){
                //去除离群用户的群通知消息
                for(int i = 0 ; i < messagesSize; i++) {
                    MessageLog messageLog = messages.get(i);
                    if(messageLog.getSessionType() == 2) {//群聊会话需要验证是否已经离群
                        for(String groupId : groupIds) {
                            if(StringUtils.equals(messageLog.getSessionId(), groupId)) {
                                if(messageLog.getPublishTime() >= gmMap.get(groupId).getJoinTime().getTime()) {
                                    messageLogList.add(messageLog);
                                }
                            }
                        }
                    } else {
                        messageLogList.add(messageLog);
                    }
                }
                MessageLog messageLog = messages.get(messagesSize -1);
                start = messageLog.getUpdateTime();
                if(!messageLogList.isEmpty()) {
                    userSer.updateMsgPullTime(userId, messageLog.getUpdateTime());
                    flag = false;
                }
            } else {
                flag = false;
            }
        }
		Map<String, Object> extData = new HashMap<>();
		extData.put("hasMore", (null != messages && messagesSize >= 400));
		return toJson(0, "", apiName, messageLogList, extData);
	}
}
