package com.taiyiyun.passport.yunpian;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.taiyiyun.passport.consts.Config;
import com.taiyiyun.passport.consts.Const;
import com.taiyiyun.passport.exception.DefinedError;
import com.taiyiyun.passport.init.SpringContext;
import com.taiyiyun.passport.po.PublicUser;
import com.taiyiyun.passport.service.IPublicUserService;
import com.taiyiyun.passport.util.StringUtil;
import com.yunpian.sdk.YunpianClient;

public final class YunPianSms {
	
	private static String TEMPLATE = "{fromUser}给您发送了{amount}个{coinName}，请下载共享护照进行接收。taiyiyun.com/app";
	
	private YunPianSms() {
		
	}
	
	public static void singleSend(String mobile, String text) throws Exception{
		if(StringUtil.isEmpty(mobile) || StringUtil.isEmpty(text)) {
			throw new NullPointerException("yunpan: mobile is null or text is null");
		}
		
		YunpianClient client = new YunpianClient(Config.get("yunpian.apiKey")).init();
		
		Map<String, String> params = new HashMap<String, String>();
		params.put(Const.YUNPIAN_MOBILE, mobile);
		params.put(Const.YUNPIAN_TEXT, text);
		client.sms().single_send(params);
	}
	
	public static void send(List<String> mobiles, String text) {
		if(null == mobiles || mobiles.size() <= 0 || StringUtil.isEmpty(text)) {
			throw new NullPointerException("yunpan: mobiles is null or text is null");
		}
		
		YunpianClient client = new YunpianClient(Config.get("yunpian.apiKey")).init();
		
		Map<String, String> params = new HashMap<String, String>();
		params.put(Const.YUNPIAN_TEXT, text);
		
		Double rs = Math.ceil(mobiles.size() / 1000);
		
		if(rs.intValue() > 1) {
			for(int i = 0; i < rs.intValue(); i++) {
				String mbs = getMobiles(mobiles, 0 * 1000);
				if(StringUtil.isNotEmpty(mbs)) {
					params.put(Const.YUNPIAN_MOBILE, mbs);
					client.sms().batch_send(params);
				}
			}
		}else {
			String mbs = getMobiles(mobiles, 0);
			if(StringUtil.isNotEmpty(mbs)) {
				params.put(Const.YUNPIAN_MOBILE, mbs);
				client.sms().batch_send(params);
			}
		}
		
	}
	
	private static String getMobiles(List<String> mobiles, int start) {
		List<String> _temp = mobiles.subList(start, start + 1000);
		
		StringBuffer bf = new StringBuffer();
		int _size = _temp.size();
		for(int i = 0; i < _size; i++) {
			bf.append(_temp.get(i));
			
			if((i + 1) < _size) {
				bf.append(",");
			}
		}
		return bf.toString();
	}

	public static void sendPostMoneySms(String toUserId, BigDecimal amount, String coinName) throws DefinedError{
		
		try {
			IPublicUserService userService = SpringContext.getBean(IPublicUserService.class);
			
			PublicUser user = userService.getByUserId(toUserId);
			
			if(null == user) {
				throw new DefinedError.ParameterException("收款方系统查询不到", null);
			}
			
			YunPianSms.singleSend(user.getMobile(), TEMPLATE.replace("{fromUser}", user.getUserName()).replace("{amount}", amount.toString()).replace("{coinName}", coinName));
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new DefinedError.OtherException("程序异常", e.getMessage());
		}
		
	}

	
}
