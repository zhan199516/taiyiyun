package com.taiyiyun.passport.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.sf.json.JSONObject;
import com.taiyiyun.passport.consts.Config;
import com.taiyiyun.passport.init.SpringContext;
import com.taiyiyun.passport.sqlserver.po.YunsignInterface;
import com.taiyiyun.passport.sqlserver.service.IYunsignInterfaceService;
import com.taiyiyun.passport.yunsign.IdentityCard;

public final class YunSignHelper {
	private YunSignHelper() {
		
	}
	
	public static final String sign(Map<String, String> dataMap, String appId, String appKey) {
		if (dataMap == null) {
			return null;
		}
		
		String serializeParam = serialize(sortMap(dataMap), appId, appKey);
		
		String sign = null;
		try {
			sign = MD5Util.MD5Encode(serializeParam, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sign;
	}
	
	private static List<Entry<String, String>> sortMap(Map<String, String> dataMap) {
		if (null == dataMap || dataMap.size() == 0) {
			return null;
		}
		
		List<Entry<String, String>> entries = new ArrayList<Entry<String, String>>(dataMap.entrySet());
		Collections.sort(entries, new Comparator<Entry<String, String>>() {
			@Override
			public int compare(Entry<String, String> o1, Entry<String, String> o2) {

				return o1.getKey().toString().compareTo(o2.getKey().toString());
			}
		});

		return entries;
	}
	
	private static String serialize(List<Entry<String, String>> dataList, String appId, String appkey) {
		if (dataList == null || dataList.size() == 0) {
			return null;
		}

		StringBuffer bf = new StringBuffer(appId);
		for (int i = 0; i < dataList.size(); i++) {
			Entry<String, String> entry = dataList.get(i);
			if (StringUtil.isNotEmpty(entry.getKey()) && StringUtil.isNotEmpty(entry.getValue())) {
				bf.append("&").append(entry.getValue());
			}
			
		}
		bf.append("&").append(appkey);
		
		return bf.toString();
	}
	
	
	public static final String currentTimespan() {
		Date now = new Date();
		return String.valueOf(now.getTime());
	}
	
	public static final String getAppId(String national) {
		String appId = Config.get("yunsign.national.appId");
		if ("1".equals(national)) {
			appId = Config.get("yunsign.appId");
		}
		
		return appId;
	}
	
	public static final String getAppkey(String national) {
		String appKey = Config.get("yunsign.national.appKey");
		if ("1".equals(national)) {
			appKey = Config.get("yunsign.appKey");
		}
		
		return appKey;
	}
	
	public static final AuthResponse realNameAuth(String uuid, String userName, String identityNumber, String facePotoImgData, String national) {
		AuthResponse response = new AuthResponse();
		String timespan = YunSignHelper.currentTimespan();
		String appId = YunSignHelper.getAppId(national);
		String appKey = YunSignHelper.getAppkey(national);
		IYunsignInterfaceService yunsignInterfaceService = SpringContext.getBean(IYunsignInterfaceService.class);
		
		Map<String, String> dataMap = new HashMap<String, String>();
		dataMap.put("userName", userName);
		dataMap.put("identityNumber", identityNumber);
		dataMap.put("time", timespan);
		String sign = YunSignHelper.sign(dataMap, appId, appKey);
		
		IdentityCard identityCard = new IdentityCard();
		String rs = identityCard.twoItemsAuthen(appId, appKey, userName, identityNumber, facePotoImgData, timespan, sign);
		
		System.err.println(rs);
		YunsignInterface bean = new YunsignInterface();
		bean.setUuid(uuid);
		bean.setInterfaceType(1);
		bean.setCreationTime(new Date());
		bean.setStatus(1);
		
		JSONObject json = JSONObject.fromObject(rs);
		if (!json.getString("code").equals("000")) {
			if ("900Q".equals(json.getString("code"))) {
				response.setMessage("非活体检测，请重新采集");
			} else if ("900R".equals(json.getString("code"))) {
				response.setMessage("系统错误，请稍后再试");
			} else {
				response.setMessage(json.getString("code") + json.getString("desc"));
			}
			response.setSuccess(false);
			
			bean.setCode(json.getString("code"));
			bean.setrDesc(json.getString("desc"));
			bean.setStatus(0);
		} else {
			response.setSuccess(true);
			response.setMessage("认证通过");
			
			bean.setStatus(1);
		}
		
		yunsignInterfaceService.save(bean);
		
		response.setCode(json.getString("code"));
		response.setDesc(json.getString("desc"));
		return response;
	}
	
	public static class AuthResponse {

		private boolean success;

		private String message;
		
		private String code;
		
		private String desc;

		public boolean isSuccess() {
			return success;
		}

		public String getMessage() {
			return message;
		}

		public void setSuccess(boolean success) {
			this.success = success;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public String getCode() {
			return code;
		}

		public String getDesc() {
			return desc;
		}

		public void setCode(String code) {
			this.code = code;
		}

		public void setDesc(String desc) {
			this.desc = desc;
		}

	}
}
