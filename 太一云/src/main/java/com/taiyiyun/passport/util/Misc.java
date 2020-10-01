package com.taiyiyun.passport.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.codec.digest.DigestUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.taiyiyun.passport.consts.Config;
import com.taiyiyun.passport.init.SpringContext;
import com.taiyiyun.passport.service.IRedisService;

public class Misc {
	
	private static final char[] CODES = "0123456789abcdefghjkmnopqrstuvwxyzABCDEFGHJKMNPQRSTUVWXYZ".toCharArray();
	private static final String[] rgRules = { "太一", "元宝", "广+链", "yuanbao", "taiyi", "太一", "yuanbao", "ybcoin", "ybc", "元宝", "lvhe", "律和", "allcoin", "中国+链", "深+链", "粤+链", "前海+链", "前海+联盟", "蒙+链",
			"浙+链", "京+链", "沪+链", "赣+链", "上+链", "北+链", "广+链", "津+链", "医养链", "大数据公益基金", "china+chain", "游戏点", "GameCredits", "MobileGO", "护照", "共享城市", "共享点", "共享号", "共享积分",
			"区块链金融", "STS", "STH", "ZGBQL", "BQYS", "数通社", "币圈月神", "币说", "聊币", "币趣", "币有料", "币知道", "中国", "国家" };
	private static String clientId;
	
	public static String randomCode(int len) {
		return random(CODES, len);
	}
	
	private static String random(char[] chars, int len) {
		Random random = new Random();
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < len; ++i) {
			buf.append(chars[random.nextInt(chars.length)]);
		}
		return buf.toString();
	}
	
	public static String getUUID() {
		UUID uuid = UUID.randomUUID();
		return String.valueOf(uuid.toString()).replace("-", "");
	}
	
	public static String renderSize(long size) {
		if (size <= 0) {
			return "0B";
		}
		String units[] = { "B", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB" };
		int index = (int) Math.floor(Math.log(size) / Math.log(1024));
		double renderSize = size / Math.pow(1024, index);
		return formatDouble(renderSize, 2) + units[index];
	}
	
	public static String formatDouble(Object obj, int scale) {
		return String.format("%." + scale + "f", obj);
	}
	
	public static boolean regexShareName(String shareName) {
		String regex = "";
		for (int i = 0; i < rgRules.length; i++) {
			String rgRule = rgRules[i];
			try {
				rgRule = new String(rgRule.getBytes("UTF-8"), "UTF-8");
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			String[] cc = rgRule.split("\\+");
			if (1 == cc.length) {
				regex = regex + "|" + rgRule;
			} else if (2 == cc.length) {
				regex = regex + "|" + ".*" + cc[0] + ".*" + cc[1] + ".*";
			}
		}
		regex = regex.substring(1);

		Matcher m = Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(shareName);
		while (m.find()) {
			return true;
		}
		return false;

	}
	
	public static String getServerUri(HttpServletRequest request) {
		String path = request.getContextPath();
		return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + path + "/";
	}
	
	public static String getServerUri(HttpServletRequest request, String relativePath) {
		if(StringUtil.isEmpty(relativePath)) {
			return null;
		}
		
		//String path = request.getContextPath();
		//return request.getScheme() + "://" + request.getServerName() + ":" + (request.getServerPort() + path + "/" + relativePath).replace("//", "/");
		if(relativePath.contains("://")){
			return relativePath;
		} else {
			return Config.get("server.link") + ("/" + relativePath).replace("//", "/");
		}


	}

	public static String getClientId() {
		if(StringUtil.isEmpty(clientId)) {
			synchronized (Misc.class) {
				if(StringUtil.isEmpty(clientId)) {
					clientId = Config.get("mqtt.clientId") + DateUtil.getFileDateTime();
				}
			}
		}
		return clientId;
	}
	
	public static String getMessageId() {
		 
		return DigestUtils.sha256Hex(Misc.getUUID() + DateUtil.getFileDateTime());
	}
	
	public static boolean regexMoneyPwd(String pwd) {
		if(StringUtil.isEmpty(pwd)) {
			return false;
		}
		
		return pwd.matches("^\\d{6}$");
	}
	
	public static String getNotNullValue(String value) {
		if(null == value) {
			return "";
		}
		return value;
	}

	public static Integer parseInt(String value) {
		if(StringUtil.isEmpty(value)) {
			return null;
		}
		
		try {
			return Integer.parseInt(value);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static int getArticleType(String thumbImg) {
		int type = 0;
		if(StringUtil.isEmpty(thumbImg)) {
			return type;
		}

		try{
			JSONArray jsonArray = JSONArray.parseArray(thumbImg);

			if(jsonArray.size() <= 2 && jsonArray.size() >= 1) {
				type = 1;
			}else if(jsonArray.size() >= 3) {
				type = 2;
			}
		} catch (Exception ex){
			ex.printStackTrace();
		}
		
		return type;
	}

	public static List<String> parseJsonArrayToList(String thumbImg, HttpServletRequest request) {
		List<String> list = new ArrayList<>();
		if(StringUtil.isEmpty(thumbImg)) {
			return list;
		}
		try{
			List<String> dataList = JSONObject.parseArray(thumbImg, String.class);
			for (Iterator<String> it = dataList.iterator(); it.hasNext();) {
				String url = it.next();
				list.add(Misc.getServerUri(request, url));
			}
		} catch (Exception ex){
			ex.printStackTrace();
		}

		
		return list;
		
	}
	
	public static String getBCIPC() throws Exception{
		final String NUM_KEY = "arcicle.register.number";
		try {
			SimpleDateFormat ft = new SimpleDateFormat("yyyyMMdd");
			String dataNo = ft.format(new Date());
			
			IRedisService redis = SpringContext.getBean(IRedisService.class);
			
			Long no = redis.getIncreNumber(NUM_KEY + dataNo, 24 * 3600);
			
			if(null != no) {
				String serialNo = "";
				int length = no.toString().length();
				
				switch (length) {
				case 1:
					serialNo = "00000" + no;
					break;
				case 2:
					serialNo = "0000" + no;
					break;
				case 3:
					serialNo = "000" + no;
					break;
				case 4:
					serialNo = "00" + no;
					break;
				case 5:
					serialNo = "0" + no;
					break;
				default:
					serialNo = "" + no;
					break;
				}
				
				return "BCIPC-" + dataNo + serialNo;
			}
			
			throw new Exception("文章证书编号生成错误");
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("服务器异常");
		}
	}
	
	public static final List<Long> parseLongList(String value) {
		List<Long> rsList = new ArrayList<Long>();
		if (StringUtil.isEmpty(value)) {
			return rsList;
		}
		
		String[] strs = value.split(",");
		for (String str : strs) {
			rsList.add(Long.parseLong(str));
		}
		
		return rsList;
	}

}
