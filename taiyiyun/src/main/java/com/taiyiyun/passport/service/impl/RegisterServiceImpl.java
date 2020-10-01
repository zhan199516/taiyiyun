package com.taiyiyun.passport.service.impl;

import java.util.Map;

import javax.annotation.Resource;

import com.taiyiyun.passport.dao.ILoginAppDao;
import com.taiyiyun.passport.exception.DefinedError;
import com.taiyiyun.passport.language.PackBundle;
import com.taiyiyun.passport.po.LoginApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.taiyiyun.passport.consts.Config;
import com.taiyiyun.passport.dao.IAccountDao;
import com.taiyiyun.passport.dao.IPublicUserDao;
import com.taiyiyun.passport.po.Account;
import com.taiyiyun.passport.po.PublicUser;
import com.taiyiyun.passport.service.IRegisterService;
import com.taiyiyun.passport.util.HttpClientUtil;
import com.taiyiyun.passport.util.MD5Signature;
import com.taiyiyun.passport.util.StringUtil;

@Service
public class RegisterServiceImpl implements IRegisterService {
	
	@Resource
	private IAccountDao accountDao;
	
	@Resource
	private IPublicUserDao publicUserDao;

	@Resource
	private ILoginAppDao loginAppDao;

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public String saveRegisterInfo(PackBundle packBundle, Map<String, String> params) {
		
		if(null == params || !params.containsKey("recommendId")) {
			String returnMsg = "参数不能为空";
			if(packBundle != null) {
				returnMsg = packBundle.getString("need.param","");
			}
			//return "参数为空";
			return returnMsg;
		}
		
		String recommendId = params.remove("recommendId");
		
		if(StringUtil.isNotEmpty(recommendId)) {
			PublicUser recUser = publicUserDao.getByUserId(recommendId);
			if(null == recUser) {
				String returnMsg = "推荐人共享号不存在";
				if(packBundle != null) {
					returnMsg = packBundle.getString("register.saveregisterinfo.recommendersharenonull");
				}
				//return "推荐人共享号不存在";
				return returnMsg;
			}
		}

		String appKey = params.get("AppKey");
		LoginApp loginApp = loginAppDao.getItem(appKey);
		if(loginApp == null){
			String returnMsg = "AppKey不能为空";
			if(packBundle != null) {
				returnMsg = packBundle.getString("need.appkey");
			}
			//return "AppKey无效";
			return returnMsg;
		}
		
		String sign = MD5Signature.signMd5(params, loginApp.getAppSecret());
		params.put("sign", sign);

		JSONObject json = null;
		try{
			String responseText = HttpClientUtil.doPost(Config.get("remote.url") + "/Api/CustomerRegist", params);
			System.out.println(responseText);

			json = (JSONObject) JSONObject.parse(responseText);
			if(StringUtil.isNotEmpty(json.get("Message"))) {
				//return "找不到与请求匹配的 HTTP 资源";
				String returnMsg = "找不到与请求匹配的 HTTP 资源";
				if(packBundle != null) {
					returnMsg = packBundle.getString("need.http.null");
				}
				return returnMsg;
			}
		} catch(DefinedError ex){
			//return "找不到与请求匹配的 HTTP 资源";
			String returnMsg = "找不到与请求匹配的 HTTP 资源";
			if(packBundle != null) {
				returnMsg = packBundle.getString("need.http.null");
			}
			return returnMsg;
		}


		if(json.get("success").equals(false)) {
			return json.getString("error");
		}
		
		JSONObject data = (JSONObject) json.get("data");
		if(json.get("success").equals(true) && StringUtil.isNotEmpty(data)) {
			if(data.containsKey("IsRegistered") && data.getBooleanValue("IsRegistered")) {
				String returnMsg = "手机号已注册";
				if(packBundle != null) {
					returnMsg = packBundle.getString("need.mobile.register");
				}
				//return "手机号已注册";
				return returnMsg;
			}
			
			if(data.containsKey("myOutPutUser") && null != data.getJSONObject("myOutPutUser") && StringUtil.isNotEmpty(recommendId)) {
				JSONObject bean = data.getJSONObject("myOutPutUser");
				Account account = new Account();
				account.setUuid(bean.getString("UUID"));
				account.setRecommendUserId(recommendId);
				account.setAddress(bean.getString("Address"));
				accountDao.save(account);
			}
			return "true";
		}
		String returnMsg = "注册失败";
		if(packBundle != null) {
			returnMsg = packBundle.getString("failed.register");
		}
		//return "注册失败";
		return returnMsg;
	}

}
