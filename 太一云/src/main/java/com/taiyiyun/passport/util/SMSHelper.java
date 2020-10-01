package com.taiyiyun.passport.util;

import com.taiyiyun.passport.bean.UserCache;
import com.taiyiyun.passport.consts.Const;
import com.taiyiyun.passport.init.SpringContext;
import com.taiyiyun.passport.mqtt.Message;
import com.taiyiyun.passport.po.PublicUser;
import com.taiyiyun.passport.po.asset.TransferAnswer;
import com.taiyiyun.passport.service.IPublicUserService;
import com.taiyiyun.passport.sms.ModelType;
import com.taiyiyun.passport.sms.SmsClient;
import com.taiyiyun.passport.sms.SmsYPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SMSHelper {
	
	private static String POSTMONEY_TEMPLATE = "{fromUser} 给您发送了 {amount}{coinName}，请用您的手机号注册“共享护照”收取，http://wechain.im";
	
	private static String MESSAGE_TEMPLATE = "{fromUser} 给您发送了一条消息，请用您的手机号注册登录共享护照收取。下载地址 http://wechain.im 。";

	private static final Logger logger = LoggerFactory.getLogger(ReflectUtil.class);
	
	private SMSHelper(){
		
	}
	
	public static void sendTransMoneyInfo(TransferAnswer apply){
		try {
			
			if(null == apply || StringUtil.isEmpty(apply.getToUserId()) || StringUtil.isEmpty(apply.getToUserId()) || null == apply.getAmount()) {
				logger.error("发送转账短信失败，参数为空");
				return;
			}
			
			UserCache userCacheFrom = CacheUtil.getOneDay(Const.APP_USERCACHE + apply.getFromUserId());
			if(null == userCacheFrom) {
				IPublicUserService userService = SpringContext.getBean(IPublicUserService.class);
				
				PublicUser fromUser = userService.getByUserId(apply.getFromUserId());
				if(null != fromUser) {
					userCacheFrom = new UserCache();
					userCacheFrom.setUserId(fromUser.getId());
					userCacheFrom.setUserName(fromUser.getUserName());
					userCacheFrom.setUuid(fromUser.getUuid());
					userCacheFrom.setMobile(fromUser.getMobile());
				}
			}
			
			if(null == userCacheFrom){
				logger.error("发送转账短信失败，系统中不存在转账发送人");
				return;
			}
			
			CacheUtil.putOneDay(Const.APP_USERCACHE + apply.getFromUserId(), userCacheFrom);
			
			UserCache userCacheTo = CacheUtil.getOneDay(Const.APP_USERCACHE + apply.getToUserId());
			if(null == userCacheTo) {
				IPublicUserService userService = SpringContext.getBean(IPublicUserService.class);
				
				PublicUser toUser = userService.getByUserId(apply.getToUserId());
				if(null != toUser) {
					userCacheTo = new UserCache();
					userCacheTo.setUserId(toUser.getId());
					userCacheTo.setUserName(toUser.getUserName());
					userCacheTo.setUuid(toUser.getUuid());
					userCacheTo.setMobile(toUser.getMobile());
				}
			}
			
			if(null == userCacheTo) {
				logger.error("发送转账短信失败，系统中不存在转账接收人");
				return;
			}
			
			CacheUtil.putOneDay(Const.APP_USERCACHE + apply.getToUserId(), userCacheTo);
			
			if(null != userCacheTo && StringUtil.isEmpty(userCacheTo.getUuid())) {
				int isChina = SmsYPClient.isChina(userCacheTo.getMobile());
				if(isChina == 0) {
					//SmsYPClient.singleSendSMS(userCacheTo.getMobile(), ModelType.SMS_YP_SIGNNAME_SENDCOIN_CN, userCacheFrom.getUserName(), apply.getAmount(), apply.getCoinName());
					//因为云片服务器不支持带推广的模板，所以推广的短信发送还是用创蓝
					SmsClient.singleSendSMS(userCacheTo.getMobile(), POSTMONEY_TEMPLATE.replace("{fromUser}", userCacheFrom.getUserName()).replace("{amount}", apply.getAmount().toString()).replace("{coinName}", apply.getCoinName()));
				} else {
					SmsYPClient.singleSendSMS(userCacheTo.getMobile(), ModelType.SMS_YP_SIGNNAME_SENDCOIN_EN, userCacheFrom.getUserName(), apply.getAmount(), apply.getCoinName());
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}


	public static void sendMessageInfo(Message<?> message){
		try {
			
			if(null == message || StringUtil.isEmpty(message.getFromUserId()) || StringUtil.isEmpty(message.getSessionId())) {
				logger.error("发送聊天消息短信失败，参数为空");
				return;
			}
			
			String toUserId = message.getSessionId();
			
			UserCache userCacheFrom = CacheUtil.getOneDay(Const.APP_USERCACHE + message.getFromUserId());
			if(null == userCacheFrom) {
				IPublicUserService userService = SpringContext.getBean(IPublicUserService.class);
				
				PublicUser fromUser = userService.getByUserId(message.getFromUserId());
				if(null != fromUser) {
					userCacheFrom = new UserCache();
					userCacheFrom.setUserId(fromUser.getId());
					userCacheFrom.setUserName(fromUser.getUserName());
					userCacheFrom.setUuid(fromUser.getUuid());
					userCacheFrom.setMobile(fromUser.getMobile());
				}
			}
			
			if(null == userCacheFrom){
				logger.error("发送聊天消息短信失败，系统中不存在消息发送人");
				return;
			}
			
			CacheUtil.putOneDay(Const.APP_USERCACHE + message.getFromUserId(), userCacheFrom);
			
			UserCache userCacheTo = CacheUtil.getOneDay(Const.APP_USERCACHE + toUserId);
			if(null == userCacheTo) {
				IPublicUserService userService = SpringContext.getBean(IPublicUserService.class);
				
				PublicUser toUser = userService.getByUserId(toUserId);
				if(null != toUser) {
					userCacheTo = new UserCache();
					userCacheTo.setUserId(toUser.getId());
					userCacheTo.setUserName(toUser.getUserName());
					userCacheTo.setUuid(toUser.getUuid());
					userCacheTo.setMobile(toUser.getMobile());
				}
			}
			
			if(null == userCacheTo) {
				logger.error("发送聊天消息短信失败，系统中不存在消息接收人");
				return;
			}
			
			CacheUtil.putOneDay(Const.APP_USERCACHE + toUserId, userCacheTo);
			
			if(null != userCacheTo && StringUtil.isEmpty(userCacheTo.getUuid())) {
				int isChina = SmsYPClient.isChina(userCacheTo.getMobile());
				if(isChina == 0) {
					//SmsYPClient.singleSendSMS(userCacheTo.getMobile(), ModelType.SMS_YP_SIGNNAME_NOTICE_CN, userCacheFrom.getUserName());
					//因为云片服务器不支持带推广的短信，所以此处还是用创蓝短信平台
					SmsClient.singleSendSMS(userCacheTo.getMobile(), MESSAGE_TEMPLATE.replace("{fromUser}", userCacheFrom.getUserName()));
				} else {
					SmsYPClient.singleSendSMS(userCacheTo.getMobile(), ModelType.SMS_YP_SIGNNAME_NOTICE_EN, userCacheFrom.getUserName());
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public static boolean sendVerifyCode(String mobile, String code) {
		if(StringUtil.isEmpty(mobile) || StringUtil.isEmpty(code)) {
			logger.error("发送聊天消息短信失败，参数为空");
			return false;
		}
		
		int isChina = SmsYPClient.isChina(mobile);
		if(isChina == 0) {
			SmsYPClient.singleSendSMS(mobile, ModelType.SMS_YP_SIGNNAME_CHECKCODE_CN, code);
		} else {
			SmsYPClient.singleSendSMS(mobile, ModelType.SMS_YP_SIGNNAME_CHECKCODE_EN, code);
		}
		
		return true;
	}
	

}
