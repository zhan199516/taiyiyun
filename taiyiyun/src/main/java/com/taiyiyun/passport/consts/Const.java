package com.taiyiyun.passport.consts;

public final class Const {
	
	/**
	 * 避免IE下载JSON文件和中文乱码
	 */
	public static final String PRODUCES = "text/html;charset=UTF-8";
	
	public static final String PRODUCES_JSON = "text/json;charset=UTF-8";
	
	public static final String SESSION_SHARE_NUM = "share-number-details";

	public static final String SESSION_LOGIN = "share-number-login-info";
	
	// MQTT服务相关配置
	public static final String CONFIG_MQTT_BORKER = "mqtt.borker";
	
	public static final String CONFIG_MQTT_SUBSCRIBER_CLIENT_ID = "mqtt.subscriberClientId";
	
	public static final String CONFIG_MQTT_PUBLISHER_CLIENT_ID = "mqtt.publisherClientId";
	
	public static final String CONFIG_MQTT_QOS = "mqtt.qos";
	
	public static final String CONFIG_MQTT_USERNAME = "mqtt.username";
	
	public static final String CONFIG_MQTT_PASSWORD = "mqtt.password";

	public static final String CONFIG_MQTT_CONNECTION_TIMEOUT = "mqtt.connectionTimeout";

	public static final String CONFIG_MQTT_HEARTBEAT_TIME = "mqtt.heartbeatTime";

	// 消息撤回超时时间
	public static final String CONFIG_MESSAGE_REVOKE_TIMEOUT = "message.revokeTimeout";
	
	// 阿里云推送服务相关配置
	public static final String CONFIG_ALIYUN_PUSH_REGION = "aliyun.push.region";

	public static final String CONFIG_ALIYUN_PUSH_ACCESS_KEY_ID = "aliyun.push.accessKeyId";

	public static final String CONFIG_ALIYUN_PUSH_ACCESS_KEY_SECRET = "aliyun.push.accessKeySecret";

	public static final String CONFIG_ALIYUN_PUSH_APP_KEY = "aliyun.push.appKey";

	public static final String CONFIG_ALIYUN_PUSH_APNS_ENV = "aliyun.push.apnsEnv";
	
	public static final String CONFIG_ALIYUN_PUSH_MESSAGE_EXPIRE_TIME = "aliyun.push.messageExpireTime";
	
	public static final String CONFIG_ALIYUN_PUSH_STORE_OFFLINE = "aliyun.push.storeOffline";
	
	
	public static final int ARTICLE_THUMBNAIL_WIDTH = 300;
	
	public static final int ARTICLE_THUMBNAIL_HEIGHT = 225;

	public static final String IMG_VALIDATE_CODE = "image.validate.code";
	
	public static final String TRANS_ID = "transId";
	
	public static final String USER_SEARCH_KEY = "user_search_key_cache";
	
	public static final String USER_SEARCH_TAG = "user_search_tag";
	
	public static final String YUNPIAN_MOBILE = "mobile";
	
	public static final String YUNPIAN_TEXT = "text";
	
	public static final String APP_USERCACHE = "app.user.cache";
	
	public static final String SMS_URL = "sms.url";
	
	public static final String SMS_USERNAME = "sms.username";
	
	public static final String SMS_PASSWORD = "sms.password";

	public static final String SMS_YP_APIKEY = "sms.yp.apikey";

	public static final String GROUP_SEARCH_KEY = "group_search_key_cache";

	public static final String GROUP_SEARCH_TAG = "group_search_tag";
	
	public static final String REPACKET_INFO = "redpacketInfo";

	public static final String GROUP_USERIDS = "group_userids";

	public static final String GROUP_ALIYUN_TUISONG = "group_aliyun_tuisong";

	public static final String GROUP_SETDISTURB_USERIDS = "group_setdisturb_userids";

	public static final String GROUP_GROUPNAME_IM = "group_groupname_im";
	
}
