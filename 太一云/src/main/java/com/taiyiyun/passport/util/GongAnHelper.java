package com.taiyiyun.passport.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.alibaba.fastjson.JSONObject;
import com.taiyiyun.passport.consts.Config;
import com.taiyiyun.passport.language.PackBundle;

public final class GongAnHelper {
	
	private static Log log = LogFactory.getLog(GongAnHelper.class);

	private GongAnHelper() {

	}
	
	public static final AuthResponse realNameAuth(PackBundle bundle, String userName, String idNumber, String validDateStart, String validDateEnd, String imgDataBase64) {
		AuthResponse response = new AuthResponse();
		
		try {
			Map<String, String> params = new HashMap<>();
	        params.put("imgDataBase64", imgDataBase64);
	        params.put("idNumber", idNumber);
	        params.put("userName", userName);
	        params.put("validDateStart", validDateStart);
	        params.put("validDateEnd", validDateEnd);
	       
	        String isTwo = Config.get("gongan.auth.proxy.interface.istwo");
	        String api = "/api/realNameAuthFourInfo";
	        if (StringUtils.equalsIgnoreCase("0", isTwo)) {
	            api = "/api/realNameAuthTwoInfo";
	        }
	        String url = Config.get("gongan.auth.proxy.url") + api;
	        String rst = HttpClientUtil.doPost(url, params, 5000);
	        JSONObject res = JSONObject.parseObject(rst);
	        int status = res.getIntValue("status");
	        String errorMsg = res.getString("errorMsg");
	        StringBuffer sb = new StringBuffer();
	        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
	        sb.append("身份信息为：姓名：" + userName + "身份证号：" + idNumber + "的用户,验证时间："+DateUtil.toString(new Date(), "yyyy-MM-dd HH:mm:ss")+"，公安一所实名认证结果：");
	        sb.append(errorMsg);
	        log.info(sb.toString());
	       
	        if (status == 0) {//认证成功！
	        	response.setStatus(0);
	        	response.setSuccess(true);
	        	response.setMessage("认证成功！");
	            log.error("公安一所实名认证成功：【用户身份证号】=" + idNumber);
	        } else {
	            if(status == 1) {
	            	response.setSuccess(false);
	            	response.setStatus(status);
	            	response.setMessage(bundle.getString("not.same.person"));//errCode == 1代表 不是本人！
	            } else {
	            	response.setSuccess(false);
	            	response.setStatus(status);
	            	response.setMessage(bundle.getString("authenticate.failed"));//errCode == 2代表 验证不通过
	            }
	        }
		} catch (Exception e) {
			log.error("公安一所实名认证失败：【用户身份证号】=" + idNumber + "异常信息：" + e.getMessage());
			response.setSuccess(false);
			response.setStatus(2);
			response.setMessage(bundle.getString("authenticate.failed"));
		}
		return response;
	}

	public static class AuthResponse {
		private boolean success;

		private String message;

		private int status;

		public boolean isSuccess() {
			return success;
		}

		public String getMessage() {
			return message;
		}

		public int getStatus() {
			return status;
		}

		public void setSuccess(boolean success) {
			this.success = success;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public void setStatus(int status) {
			this.status = status;
		}

	}

}
