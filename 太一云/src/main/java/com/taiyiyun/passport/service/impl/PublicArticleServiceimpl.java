package com.taiyiyun.passport.service.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.taiyiyun.passport.dao.IPublicUserConfigDao;
import com.taiyiyun.passport.dao.IPublicUserDao;
import com.taiyiyun.passport.language.LangResource;
import com.taiyiyun.passport.language.PackBundle;
import com.taiyiyun.passport.mongo.po.MessageLog;
import com.taiyiyun.passport.mongo.service.IMessageLogService;
import com.taiyiyun.passport.po.PublicUserConfig;
import com.taiyiyun.passport.po.PublicUserTop;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.taiyiyun.passport.aliyun.push.Push;
import com.taiyiyun.passport.aliyun.push.PushDeviceType;
import com.taiyiyun.passport.aliyun.push.PushMessage;
import com.taiyiyun.passport.aliyun.push.PushTarget;
import com.taiyiyun.passport.aliyun.push.PushType;
import com.taiyiyun.passport.consts.Config;
import com.taiyiyun.passport.dao.IPublicArticleDao;
import com.taiyiyun.passport.dao.IPublicUserFollowerDao;
import com.taiyiyun.passport.mqtt.Message;
import com.taiyiyun.passport.mqtt.Message.MessageType;
import com.taiyiyun.passport.mqtt.Message.SessionType;
import com.taiyiyun.passport.mqtt.MessagePublisher;
import com.taiyiyun.passport.po.PublicArticle;
import com.taiyiyun.passport.po.PublicUser;
import com.taiyiyun.passport.po.circle.ArticleMessageContent;
import com.taiyiyun.passport.po.message.ArticleMessage;
import com.taiyiyun.passport.service.IPublicArticleService;
import com.taiyiyun.passport.util.Misc;
import com.taiyiyun.passport.util.StringUtil;

@Service
public class PublicArticleServiceimpl implements IPublicArticleService {

	@Resource
	private IPublicUserDao userDao;

	@Resource
	private IPublicArticleDao dao;
	
	@Resource
	private IPublicUserFollowerDao userFollowerDao;

	@Resource
	private IMessageLogService messageLogService;
	@Resource
	private IPublicUserConfigDao  userConfigDao;

	private Message<ArticleMessage> map(PublicArticle article){
		Message<ArticleMessage> baseResult = new Message<>();

		baseResult.setVersion(1);
		baseResult.setMessageType(5);

		baseResult.setFromUserId(Config.get("mqtt.clientId"));
		baseResult.setFromClientId(Config.get("mqtt.clientId"));
		baseResult.setMessageId(article.getArticleId());

		baseResult.setSessionType(3);
		baseResult.setSessionId(baseResult.getFromUserId());

		baseResult.setPublishTime(article.getPublishTime().getTime());
		baseResult.setUpdateTime(article.getUpdateTime().getTime());


//		CONTENT_CIRCLE_GENERIC_TITLE_TEXT       = 501,      // 共享号消息（包含标题和内容）
//		CONTENT_CIRCLE_GENERIC_TEXT             = 502,      // 共享号消息（只包含内容）
//		CONTENT_CIRCLE_GENERIC_ONE_IMAGE        = 503,      // 共享号消息（包含标题和 1 张缩略图）
//		CONTENT_CIRCLE_GENERIC_THREE_IMAGE      = 504,      // 共享号消息（包含标题和 3 张缩略图）
		//baseResult.setContent();

		ArticleMessage message = new ArticleMessage();
		baseResult.setContent(message);

		message.setArticleId(article.getArticleId());
		message.setTitle(article.getTitle());
		message.setText(article.getContent());

		baseResult.setContentType(502);
		if(StringUtil.isEmptyOrBlank(message.getTitle())){
			baseResult.setContentType(501);
		}

		String files = article.getThumbImg();
		try{
			List<String> list = JSON.parseArray(files, String.class);
			message.setAttachFileUrls(list);

			if(list.size() >= 3){
				baseResult.setContentType(504);
			} else if(list.size() >= 1){
				baseResult.setContentType(503);
			}
		} catch (Exception ex){
		}

		return baseResult;
	}



	@Override
	public PublicArticle getById(String articleId) {
		
		return dao.getById(articleId);
	}

	@Override
	public PublicArticle getRichById(String articleId) {
		return dao.getRichById(articleId);
	}

	@Override
	public int save(PublicArticle entity) {
		if(StringUtil.isEmpty(entity.getClientId())){
			entity.setClientId(Misc.getClientId());
		}


		PublicUserTop userTop = userDao.getUserTop(entity.getUserId());
		if(userTop != null){
			if(entity.getTopLevel() == null){
				if(userTop.getTopType() != null && userTop.getTopLevel() != null
						&& userTop.getTopType() == 1 && userTop.getTopLevel() > 0){
					entity.setTopLevel(userTop.getTopLevel());
				}
			} else
			if(userTop.getTopType() != null && userTop.getTopLevel() != null
					&& userTop.getTopLevel() > 0 && entity.getTopLevel() > 0){
				if(userTop.getTopLevel() < entity.getTopLevel()){
					entity.setTopLevel(userTop.getTopLevel());
				}
			}
		} else {
			entity.setTopLevel(null);
		}


		//如果新文章大于0，则作者的旧文章toplevel设置为0
		if(entity.getTopLevel() != null && entity.getTopLevel() > 0){
			dao.saveBefore(entity);
		}
		return dao.save(entity);
	}

	@Override
	public List<PublicArticle> getByUserId(String userId) {
		return dao.getByUserId(userId);
	}

	@Override
//	@Transactional(propagation = Propagation.REQUIRED)
	public Integer deleteArticles(HttpServletRequest request, String userId, PublicArticle article) {
		int count = 0;
		if(null == article || StringUtil.isEmpty(article.getArticleId())) {
			return count;
		}
		
		List<String> articleIds = Arrays.asList(article.getArticleId());
		try {
			revokeMessage(request, article);
			count = dao.deleteArticles(userId, articleIds);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
		return count;
	}

	@Override
	public List<Message<ArticleMessage>> getGenericMessages(String userId, Long start, Long end) {
		Timestamp dtStart = null;
		if(start != null && start != 0){
			dtStart = new Timestamp(start);
		}

		Timestamp dtEnd = null;
		if(end != null && end != 0){
			dtEnd = new Timestamp(end);
		}

		HashMap<String, Object> map = new HashMap<>();


		map.put("start", dtStart);
		map.put("end", dtEnd);
		map.put("userId", userId);
		map.put("onlineStatus", 2);

		List<PublicArticle> list =  dao.getMessagesByMap(map);

		List<Message<ArticleMessage>> newList = new ArrayList<>();

		for(int i = 0; i < list.size(); i++){
			PublicArticle article = list.get(i);

			newList.add(map(article));
		}

		return newList;
	}

	@Override
	public List<PublicArticle> getUnpublishOldArticle(String userId, Long start, Long end, Integer status){
		HashMap<String, Object> map = new HashMap<>();

		map.put("userId", userId);
		map.put("start", start);
		map.put("end", end);
		map.put("onlineStatus", status);

		List<PublicArticle> list =  dao.getMessagesByMap(map);

		return list;
	}

	@Override
	public int updateOnlineStatus(PublicArticle article) {
		return dao.updateOnlineStatus(article);
	}

	@Override
	public void publish(HttpServletRequest request, PublicArticle article, boolean immigrate){

		try {
			Message<ArticleMessageContent> message = new Message<>();
//			message.setFromClientId(Misc.getClientId());
			message.setFromClientId(article.getClientId());
			message.setFromUserId(article.getUserId());
			message.setMessageType(MessageType.MESSAGE_CIRCLE_GENERIC.getValue());
			message.setSessionType(SessionType.SESSION_CIRCLE.getValue());
			message.setSessionId(article.getUserId());
			message.setMessageId(article.getArticleId());

			if(immigrate){
				message.setPublishTime(article.getPublishTime().getTime());
				message.setUpdateTime(article.getUpdateTime().getTime());
				message.setImmigrate(true);
			} else {
				message.setPublishTime(new Date().getTime());
				message.setUpdateTime(article.getUpdateTime().getTime());
			}

			message.setVersion(1);
			message.setContentType(article.getContentType());
			
			ArticleMessageContent articleContent = new ArticleMessageContent();
			articleContent.setArticleId(article.getArticleId());
			articleContent.setOriginal(article.getIsOriginal());
			articleContent.setTitle(article.getTitle());
			articleContent.setText(article.getSummary());
			
			if(StringUtil.isNotEmpty(article.getThumbImg())) {
				JSONArray jsons = JSONArray.parseArray(article.getThumbImg());
				List<String> thumbUrls = new ArrayList<>();
				for(Object path : jsons) {
					thumbUrls.add(Misc.getServerUri(request, String.valueOf(path)));
				}
				articleContent.setAttachFileUrls(thumbUrls);
			}
			
			message.setContent(articleContent);
			
			//MessagePublisher.getInstance().addPublish(Message.DOWNLINK_PUBLIC_SHARE_MESSAGE + article.getUserId(), message);
			com.taiyiyun.passport.mqtt.v2.MessagePublisher.getInstance().publish(Message.UPLINK_PUBLIC_SHARE_MESSAGE + article.getUserId(), message);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private void revokeMessage(HttpServletRequest request, PublicArticle article) {
		try {
			Message<ArticleMessageContent> message = new Message<>();
//			message.setFromClientId(Misc.getClientId());

			if(StringUtil.isEmpty(article.getClientId())){
				MessageLog log = messageLogService.getArticleMessage(article.getArticleId());
				if(log == null){
					return;
				}
				message.setFromClientId(log.getFromClientId());
			} else {
				message.setFromClientId(article.getClientId());
			}

			message.setFromUserId(article.getUserId());
			message.setMessageType(MessageType.MESSAGE_CIRCLE_REVOKE.getValue());
			message.setSessionType(SessionType.SESSION_CIRCLE.getValue());
			message.setSessionId(article.getUserId());
			message.setMessageId(article.getArticleId());
			message.setPublishTime(new Date().getTime());
			message.setUpdateTime(article.getUpdateTime().getTime());
			message.setVersion(1);
			
			//MessagePublisher.getInstance().addPublish(Message.DOWNLINK_PUBLIC_SHARE_MESSAGE + article.getUserId(), message);
			com.taiyiyun.passport.mqtt.v2.MessagePublisher.getInstance().publish(Message.UPLINK_PUBLIC_SHARE_MESSAGE + article.getUserId(), message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void pushALiMessage(PublicArticle article, PublicUser user){
		if(null == article || StringUtil.isEmpty(article.getUserId())) {
			return;
		}
		
		try {
			
			String userName = user.getUserName();
			if(userName.length() >= 20){
				userName = userName.substring(0, 17) + "...";
			}
			
			List<PublicUser> followers = userFollowerDao.getFocusMeUsers(article.getUserId());
			if(null != followers && followers.size() > 0) {
				PushMessage message = new PushMessage();
				
				message.setDeviceType(PushDeviceType.ALL);
				message.setPushType(PushType.NOTICE);
				message.setTitle(userName);
				message.setSummary(article.getTitle());
				message.setContent(article.getTitle());
				message.setBody(article.getTitle());
				message.setTarget(PushTarget.ACCOUNT);
				message.setTargetValue(getPushAccounts(followers, article.getUserId()));
				
				message.addExtParameter("fromUserId", article.getUserId());
				message.addExtParameter("sessionId", article.getUserId());
				message.addExtParameter("messageType", String.valueOf(MessageType.MESSAGE_CIRCLE_GENERIC.getValue()));
				message.addExtParameter("_NOTIFICATION_BAR_STYLE_", 1);
				
				Push.getInstance().addMessage(message);
			}
		} catch (Exception e) {
			e.printStackTrace();
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

	private String getPushAccounts(List<PublicUser> followers, String authorId) {
		if(null == followers || followers.size() == 0) {
			return null;
		}
		
		int lg = followers.size();
		StringBuffer bf = new StringBuffer();
		for (int i = 0; i < lg; i++) {
			PublicUser user = followers.get(i);
			boolean b = hasSetGlobalDisturb(user.getId());
			if(b) continue;//设置了全局免打扰的用户直接跳过
			boolean b1 = hasSetDisturbForUser(user.getId(), authorId);
			if(b1) continue;//设置了针对该文章做的消息免打扰
			bf.append(user.getId());
			
			if((i + 1) < lg) {
				bf.append(",");
			}
		}
		
		return bf.toString(); 
	}

}

