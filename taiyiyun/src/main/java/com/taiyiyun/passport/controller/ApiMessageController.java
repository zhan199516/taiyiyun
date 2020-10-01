package com.taiyiyun.passport.controller;

import com.alibaba.fastjson.JSON;
import com.taiyiyun.passport.bean.UserDetails;
import com.taiyiyun.passport.consts.EnumStatus;
import com.taiyiyun.passport.language.LangResource;
import com.taiyiyun.passport.language.PackBundle;
import com.taiyiyun.passport.po.BaseResult;
import com.taiyiyun.passport.service.IMqttUserService;
import com.taiyiyun.passport.util.SessionUtil;
import com.taiyiyun.passport.util.StringUtil;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Controller
public class ApiMessageController extends BaseController {
	
	@Resource
	private IMqttUserService mqttUserService;
	
	@ResponseBody
	@RequestMapping(value = "/api/message/repeat", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	public DeferredResult<String> setDisturb(@RequestParam("messageId") String messageId,
											 @RequestParam("userId") String userId,
											 @RequestParam("clientId") String clientId,
											 HttpServletRequest request) {
		//根据request 请求获取访问用户本地语言
		PackBundle bundle = LangResource.getResourceBundle(request);
		//api名称
		String apiName = "post + /api/message/repeat";
		//获取当前登录用户信息
		UserDetails userDetails = SessionUtil.getUserDetails(request);
		if(userDetails == null) {
			DeferredResult<String> dr = new DeferredResult<>();
			dr.setResult(toJson(EnumStatus.NINETY_EIGHT.getIndex(), bundle.getString("user.not.login"), apiName, null));
			return dr;
		}
		//消息id不能为空
		if(StringUtil.isEmpty(messageId)) {
			DeferredResult<String> dr = new DeferredResult<>();
			dr.setResult(toJson(EnumStatus.ONE.getIndex(), bundle.getString("need.param", "messageId"), apiName, null));
			return dr;
		}
		//设备id不能为空
		if(StringUtil.isEmpty(clientId)) {
			DeferredResult<String> dr = new DeferredResult<>();
			dr.setResult(toJson(EnumStatus.ONE.getIndex(), bundle.getString("need.param", "clientId"), apiName, null));
			return dr;
		}
		try {
			//如果userId为空，直接获取登录用户的userId
			if (StringUtil.isEmpty(userId)){
				userId = userDetails.getUserId();
			}
			DeferredResult<String> dr = new DeferredResult<>();
			BaseResult<Integer> resultData = mqttUserService.repeatMessage(bundle,messageId,userId,clientId);
			String rjson = JSON.toJSONString(resultData);
			dr.setResult(rjson);
			return dr;
		}
		catch (Exception e) {
			e.printStackTrace();
			DeferredResult<String> dr = new DeferredResult<>();
			dr.setResult(toJson(EnumStatus.NINETY_NINE.getIndex(), bundle.getString("system.error"), apiName, null));
			return dr;
		}
	}
}
