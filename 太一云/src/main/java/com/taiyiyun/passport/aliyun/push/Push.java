package com.taiyiyun.passport.aliyun.push;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.http.ProtocolType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.push.model.v20160801.PushRequest;
import com.aliyuncs.push.model.v20160801.PushResponse;
import com.aliyuncs.utils.ParameterHelper;
import com.taiyiyun.passport.consts.Config;
import com.taiyiyun.passport.consts.Const;
import com.taiyiyun.passport.util.Misc;
import com.taiyiyun.passport.util.StringUtil;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class Push {
	
	private static final ConcurrentLinkedQueue<PushMessage> PUSH_QUEUE = new ConcurrentLinkedQueue<>();

	private static Push instance = new Push();
	
	private Push() {
		new Thread(new Poll()).start();
	}
	
	public void addMessage(PushMessage message) {
		PUSH_QUEUE.add(message); // 添加到发送队列
	}
	
	public static Push getInstance() {
		return instance;
	}

	public List<PushResult> push(PushMessage message, String deviceIds) {
		List<PushResult> rsList = new ArrayList<PushResult>();
		PushResult pushResult = new PushResult();
		try {
			checkConfig();
			List<Long> appkeyList = Misc.parseLongList(APPKEY);
			for(Iterator<Long> it = appkeyList.iterator(); it.hasNext();) {
				Long appKey = it.next();
				PushRequest pushRequest = new PushRequest();
				pushRequest.setProtocol(ProtocolType.HTTPS); //推送内容需要保护，请使用HTTPS协议
				pushRequest.setMethod(MethodType.POST); //推送内容较长，请使用POST请求
				// 推送目标
				pushRequest.setAppKey(appKey);
				pushRequest.setTarget(message.getTarget().getValue()); //推送目标: DEVICE:按设备推送 ALIAS : 按别名推送 ACCOUNT:按帐号推送  TAG:按标签推送; ALL: 广播推送
				pushRequest.setTargetValue(deviceIds); //根据Target来设定，如Target=DEVICE, 则对应的值为 设备id1,设备id2. 多个值使用逗号分隔.(帐号与设备有一次最多100个的限制)
				pushRequest.setDeviceType(message.getDeviceType().getValue()); // 设备类型 ANDROID iOS ALL.
				pushRequest.setPushType(message.getPushType().getValue()); // 消息类型 MESSAGE NOTICE
				// 推送配置
				pushRequest.setTitle(message.getTitle()); // 消息的标题
				pushRequest.setBody(message.getBody()); // 消息的内容

				// 推送配置: iOS
				pushRequest.setIOSBadge(1); // iOS应用图标右上角角标
				pushRequest.setIOSMusic(DEFAULT_MUSIC); // iOS通知声音
				pushRequest.setIOSSubtitle("");//iOS10通知副标题的内容
				pushRequest.setIOSNotificationCategory("iOS10 Notification Category");//指定iOS10通知Category
				pushRequest.setIOSMutableContent(true);//是否允许扩展iOS通知内容
				pushRequest.setIOSApnsEnv(APNS_ENV);//iOS的通知是通过APNs中心来发送的，需要填写对应的环境信息。"DEV" : 表示开发环境 "PRODUCT" : 表示生产环境
				pushRequest.setIOSRemind(true); // 消息推送时设备不在线（既与移动推送的服务端的长连接通道不通），则这条推送会做为通知，通过苹果的APNs通道送达一次。注意：离线消息转通知仅适用于生产环境
				pushRequest.setIOSRemindBody("iOSRemindBody");//iOS消息转通知时使用的iOS通知内容，仅当iOSApnsEnv=PRODUCT && iOSRemind为true时有效
				pushRequest.setIOSExtParameters(message.getStringExtParameters()); //通知的扩展属性(注意 : 该参数要以json map的格式传入,否则会解析出错)

				// 推送配置: Android
				pushRequest.setAndroidNotifyType(AndroidNotifyType.SOUND.getValue());//通知的提醒方式 "VIBRATE" : 震动 "SOUND" : 声音 "BOTH" : 声音和震动 NONE : 静音
				pushRequest.setAndroidNotificationBarType(1);//通知栏自定义样式0-100
				pushRequest.setAndroidNotificationBarPriority(1);//通知栏自定义样式0-100
				pushRequest.setAndroidOpenType(AndroidOpenType.APP.getValue()); //点击通知后动作 "APPLICATION" : 打开应用 "ACTIVITY" : 打开AndroidActivity "URL" : 打开URL "NONE" : 无跳转
				pushRequest.setAndroidOpenUrl(""); //Android收到推送后打开对应的url,仅当AndroidOpenType="URL"有效
				pushRequest.setAndroidActivity(""); // 设定通知打开的activity，仅当AndroidOpenType="Activity"有效
				pushRequest.setAndroidMusic(DEFAULT_MUSIC); // Android通知音乐
				pushRequest.setAndroidRemind(true);
				pushRequest.setAndroidPopupActivity("com.taiyiyun.sharepassport.alipush.PopupPushActivity");//设置该参数后启动辅助弹窗功能, 此处指定通知点击后跳转的Activity（辅助弹窗的前提条件：1. 集成第三方辅助通道；2. StoreOffline参数设为true）
				String popupTitle = message.getTitle();
				if(popupTitle.length() > 16) {
					popupTitle = popupTitle.substring(0,13) + "...";
				}
				pushRequest.setAndroidPopupTitle(popupTitle);
				pushRequest.setAndroidPopupBody(message.getBody());
				pushRequest.setAndroidExtParameters(message.getStringExtParameters()); //设定通知的扩展属性。(注意 : 该参数要以 json map 的格式传入,否则会解析出错)
				// 推送控制
				Date pushDate = new Date(System.currentTimeMillis()) ; // 30秒之间的时间点, 也可以设置成你指定固定时间
				String pushTime = ParameterHelper.getISO8601Time(pushDate);
				pushRequest.setPushTime(pushTime); // 延后推送。可选，如果不设置表示立即推送
				final String expireTime = ParameterHelper.getISO8601Time(new Date(System.currentTimeMillis() + EXPIRE_TIME));
				pushRequest.setExpireTime(expireTime); // 消息过期时间
				pushRequest.setStoreOffline(true); // 离线消息是否保存,若保存, 在推送时候，用户即使不在线，下一次上线则会收到
				PushResponse pushResponse = ClientFactory.ACS_CLIENT.getAcsResponse(pushRequest);
				Map<String, String> resultMap = new HashMap<String, String>();
				resultMap.put("requestId", pushResponse.getRequestId());
				//resultMap.put("responseId", pushResponse.getResponseId());
				resultMap.put("message", JSONObject.toJSONString(message));
				pushResult.setData(JSON.toJSONString(resultMap));
				pushResult.setSuccess(true);
				//LOGGER.info("阿里云推送消息成功，推送结果：" + pushResult.toString());
				rsList.add(pushResult);
			}
			return rsList;
		} catch (Exception e) {
			logger.error("阿里云推送消息异常。", e);
			pushResult.setSuccess(false);
			pushResult.setCause(e);
			rsList.add(pushResult);
			return rsList;
		}
	}

	/**
	 * 阿里云消息推送
	 * //@param message 待推送的消息
	 * @return
	 */
	/*
	public List<PushResult> push(PushMessage message, String deviceIds) {
		
		//LOGGER.info("阿里云推送消息：" + message.toString());
		
		List<PushResult> rsList = new ArrayList<PushResult>();
		PushResult pushResult = new PushResult();

		try {
			checkConfig();
		} catch (Exception e) {
			logger.error("阿里云推送消息异常。", e);
			pushResult.setSuccess(false);
			pushResult.setCause(e);
			rsList.add(pushResult);
			return rsList;
		}
		
		List<Long> appkeyList = Misc.parseLongList(APPKEY);
		
		try {
			for (Iterator<Long> it = appkeyList.iterator(); it.hasNext();) {
				Long appKey = it.next();
				
				PushRequest pushRequest = new PushRequest();
		        pushRequest.setProtocol(ProtocolType.HTTPS); //推送内容需要保护，请使用HTTPS协议
		        pushRequest.setMethod(MethodType.POST); //推送内容较长，请使用POST请求
		        pushRequest.setXiaomiActivity("com.taiyiyun.sharepassport.alipush.PopupPushActivity");
		        pushRequest.setStoreOffline(true);

		        pushRequest.setAppKey(appKey); // 推送目标
		        pushRequest.setTarget(message.getTarget().getValue());
//		        pushRequest.setTargetValue(message.getTargetValue());
		        pushRequest.setTargetValue(deviceIds);
		        pushRequest.setDeviceType(message.getDeviceType().getValue());
		        
		        pushRequest.setType(message.getPushType().getValue());
		        pushRequest.setTitle(message.getTitle());
		        pushRequest.setSummary(message.getSummary());
		        pushRequest.setBody(message.getBody());
		        
		        pushRequest.setApnsEnv(APNS_ENV);
		        pushRequest.setRemind(true);
		        
		        pushRequest.setiOSBadge("1");
		        pushRequest.setiOSSubtitle(message.getTitle());
		        pushRequest.setiOSMusic(DEFAULT_MUSIC);
		        pushRequest.setiOSNotificationCategory("iOS10 Notification Category");
		        pushRequest.setiOSMutableContent(true);
		        pushRequest.setiOSExtParameters(message.getStringExtParameters());
		        
		        pushRequest.setAndroidMusic(DEFAULT_MUSIC);
		        pushRequest.setAndroidOpenType(AndroidOpenType.APP.getValue());
		        //pushRequest.setAndroidActivity("com.taiyiyun.passport.ui.activity.NavigationActivity");
		        pushRequest.setAndroidExtParameters(message.getStringExtParameters());
		        
		        pushRequest.setRemind(true); // 当APP不在线时候，是否通过通知提醒
		        pushRequest.setStoreOffline(STORE_OFFLINE); // 离线消息是否保存,若保存, 在推送时候，用户即使不在线，下一次上线则会收到
		        
		        final String expireTime = ParameterHelper.getISO8601Time(new Date(System.currentTimeMillis() + EXPIRE_TIME));
		        pushRequest.setExpireTime(expireTime); // 消息过期时间

				PushResponse pushResponse = ClientFactory.ACS_CLIENT.getAcsResponse(pushRequest);
			        
		        Map<String, String> resultMap = new HashMap<String, String>();
		        resultMap.put("requestId", pushResponse.getRequestId());
		        resultMap.put("responseId", pushResponse.getResponseId());
		        resultMap.put("message", pushResponse.getMessage());
		        pushResult.setData(JSON.toJSONString(resultMap));
		        pushResult.setSuccess(true);
		        //LOGGER.info("阿里云推送消息成功，推送结果：" + pushResult.toString());
		        rsList.add(pushResult);
			}
			
			return rsList;
		} catch (Exception e) {
			logger.error("阿里云推送消息异常。", e);
        	pushResult.setCause(e);
        	pushResult.setSuccess(false);
        	rsList.add(pushResult);
        	return rsList;
		}
	}
	*/
	private static void checkConfig() throws Exception {
		
		String message = "阿里云推送配置错误，配置项：";
		if (StringUtil.isEmpty(APPKEY)) {
			throw new Exception(message + Const.CONFIG_ALIYUN_PUSH_APP_KEY);
		}
		
		if (StringUtil.isEmpty(REGION)) {
			throw new Exception(message + Const.CONFIG_ALIYUN_PUSH_REGION);
		}
		
		if (StringUtil.isEmpty(ACCESS_KEY_ID)) {
			throw new Exception(message + Const.CONFIG_ALIYUN_PUSH_ACCESS_KEY_ID);
		}
		
		if (StringUtil.isEmpty(ACCESS_KEY_SECRET)) {
			throw new Exception(message + Const.CONFIG_ALIYUN_PUSH_ACCESS_KEY_SECRET);
		}
		
	}

	public final Logger logger = LoggerFactory.getLogger(getClass());
	
	private static final String APPKEY = Config.get(Const.CONFIG_ALIYUN_PUSH_APP_KEY);
	
	private static final Long EXPIRE_TIME = Config.getLong(Const.CONFIG_ALIYUN_PUSH_MESSAGE_EXPIRE_TIME, 72 * 3600 * 1000);
	
	private static final String REGION = Config.get(Const.CONFIG_ALIYUN_PUSH_REGION);
	
	private static final String ACCESS_KEY_ID = Config.get(Const.CONFIG_ALIYUN_PUSH_ACCESS_KEY_ID);
	
	private static final String ACCESS_KEY_SECRET = Config.get(Const.CONFIG_ALIYUN_PUSH_ACCESS_KEY_SECRET);
	
	private static final String APNS_ENV = Config.get(Const.CONFIG_ALIYUN_PUSH_APNS_ENV);
	
	private static final Boolean STORE_OFFLINE = Config.get(Const.CONFIG_ALIYUN_PUSH_STORE_OFFLINE, false);
	
	private static final String DEFAULT_MUSIC = "default";
	
	public static final class ClientFactory {
		private static final IAcsClient ACS_CLIENT = new DefaultAcsClient(DefaultProfile.getProfile(REGION, ACCESS_KEY_ID, ACCESS_KEY_SECRET));
	}
	
	static class Poll implements Runnable{

		@Override
		public void run() {
			try{
				while(true){
					PushMessage message = PUSH_QUEUE.poll();
					if(null == message){
						Thread.sleep(100);
					} else {
						try{
							String targetValue = message.getTargetValue();
							if(StringUtil.isEmpty(targetValue)){
								return;
							}

							String[] list = targetValue.split(",");
							int times = (list.length + 99) / 100;
							for(int i = 0; i < times; i++){
								Object[] inner = ArrayUtils.subarray(list, i * 100, i*100+100);
								String innerString = StringUtils.join(inner, ",");
								//logger.info("阿里云推送" + inner.length + "个用户");
								Push.getInstance().push(message, innerString);
							}

						} catch(Exception e){
							e.printStackTrace();
						}

					}
				}
			} catch(InterruptedException e){
				//线程中断则退出
				e.printStackTrace();
			}
		}
	}
	
}
