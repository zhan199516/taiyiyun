package com.taiyiyun.passport.sms;

import com.taiyiyun.passport.consts.Config;
import com.taiyiyun.passport.consts.Const;
import com.taiyiyun.passport.exception.DefinedError;
import com.taiyiyun.passport.util.HttpClientUtil;
import com.taiyiyun.passport.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class SmsClient {
	
	private static final  String URL = Config.get(Const.SMS_URL);
	
	private static final  String USERNAME = Config.get(Const.SMS_USERNAME);
	
	private static final  String PASSWORD = Config.get(Const.SMS_PASSWORD);

	public static final Logger logger = LoggerFactory.getLogger(SmsClient.class);
	
	private SmsClient(){
		
	}
	
	public static String singleSendSMS(String mobile, String msg) {
		try {
			checkConfig();
		} catch (Exception e) {
			logger.error("创蓝短信发送异常。", e);
			return null;
		}
		
		Map<String, Object> params = new HashMap<>();
		params.put("account", USERNAME);
		params.put("password", PASSWORD);
		params.put("phone", mobile);
		params.put("msg", msg);
		
		try {
			return HttpClientUtil.doJsonPost(URL, params);
		} catch (DefinedError e) {
			e.printStackTrace();
			logger.error("创蓝短信发送异常。", e);
		}
		return null;
	}
	
	private static void checkConfig() throws Exception{
		String message = "创蓝短信配置错误，配置项：";
		
		if (StringUtil.isEmpty(URL)) {
			throw new Exception(message + Const.SMS_URL);
		}
		
		if (StringUtil.isEmpty(USERNAME)) {
			throw new Exception(message + Const.SMS_USERNAME);
		}
		
		if (StringUtil.isEmpty(PASSWORD)) {
			throw new Exception(message + Const.SMS_USERNAME);
		}
		
	}

}
