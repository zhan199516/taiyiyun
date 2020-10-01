package com.taiyiyun.passport.controller;

import com.alibaba.fastjson.JSON;
import com.taiyiyun.passport.bean.UserDetails;
import com.taiyiyun.passport.consts.Const;
import com.taiyiyun.passport.consts.EnumStatus;
import com.taiyiyun.passport.language.LangResource;
import com.taiyiyun.passport.language.PackBundle;
import com.taiyiyun.passport.po.BaseResult;
import com.taiyiyun.passport.po.setting.UserConfig;
import com.taiyiyun.passport.service.IPublicUserConfigService;
import com.taiyiyun.passport.util.SessionUtil;
import com.taiyiyun.passport.util.StringUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller
public class ApiUserConfigController extends BaseController {
	
	@Resource
	private IPublicUserConfigService publicUserConfigService;
	
	@ResponseBody
	@RequestMapping(value = "/api/user/setDisturb", method = RequestMethod.POST, produces = {Const.PRODUCES_JSON})
	public DeferredResult<String> setDisturb(@RequestBody UserConfig userConfig, HttpServletRequest request) {
		//根据request 请求获取访问用户本地语言
		PackBundle bundle = LangResource.getResourceBundle(request);
		//api名称
		String apiName = "post + /api/user/setDisturb";
		//获取当前登录用户信息
		UserDetails userDetails = SessionUtil.getUserDetails(request);
		if(userDetails == null) {
			DeferredResult<String> dr = new DeferredResult<>();
			dr.setResult(toJson(EnumStatus.NINETY_EIGHT.getIndex(), bundle.getString("user.not.login"), apiName, null));
			return dr;
		}
		//设置目标用户ID不能为空
		if(userConfig.getUserType() == null) {
			DeferredResult<String> dr = new DeferredResult<>();
			dr.setResult(toJson(EnumStatus.ONE.getIndex(), bundle.getString("need.param", "userType"), apiName,null));
			return dr;
		}
		Integer userType = userConfig.getUserType();
		if (userType.intValue() != EnumStatus.ZORO.getIndex()) {
			//设置目标用户ID不能为空
			if (StringUtil.isEmpty(userConfig.getTargetId())) {
				DeferredResult<String> dr = new DeferredResult<>();
				dr.setResult(toJson(EnumStatus.ONE.getIndex(), bundle.getString("need.param", "targetId"), apiName, null));
				return dr;
			}
		}
		//免打扰状态不能为空
		if(StringUtil.isEmpty(userConfig.getIsDisturb())) {
			DeferredResult<String> dr = new DeferredResult<>();
			dr.setResult(toJson(EnumStatus.ONE.getIndex(), bundle.getString("need.param", "isDisturb"), apiName, null));
			return dr;
		}
		//获取登录用户userId
		String userId = userDetails.getUserId();
		//设置用户免打扰信息
		try {
			//设置当前用户id
			userConfig.setSetupUserId(userId);
			DeferredResult<String> dr = new DeferredResult<>();
			BaseResult<Integer> resultData = publicUserConfigService.configDisturbStatus(bundle,userConfig);
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

	@ResponseBody
	@RequestMapping(value = "/api/user/setTop", method = RequestMethod.POST, produces = {Const.PRODUCES_JSON})
	public DeferredResult<String> setTop(@RequestBody UserConfig userConfig, HttpServletRequest request) {
		//根据request 请求获取访问用户本地语言
		PackBundle bundle = LangResource.getResourceBundle(request);
		//api名称
		String apiName = "post + /api/user/setTop";
		//获取当前登录用户信息
		UserDetails userDetails = SessionUtil.getUserDetails(request);
		if(userDetails == null) {
			DeferredResult<String> dr = new DeferredResult<>();
			dr.setResult(toJson(EnumStatus.NINETY_EIGHT.getIndex(), bundle.getString("user.not.login"), apiName, null));
			return dr;
		}
		//设置目标用户ID不能为空
		if(StringUtil.isEmpty(userConfig.getTargetId())) {
			DeferredResult<String> dr = new DeferredResult<>();
			dr.setResult(toJson(EnumStatus.ONE.getIndex(), bundle.getString("need.param", "targetId"), apiName,null));
			return dr;
		}
		//免打扰状态不能为空
		if(StringUtil.isEmpty(userConfig.getIsTop())) {
			DeferredResult<String> dr = new DeferredResult<>();
			dr.setResult(toJson(EnumStatus.ONE.getIndex(), bundle.getString("need.param", "isDisturb"), apiName, null));
			return dr;
		}
		//获取登录用户userId
		String userId = userDetails.getUserId();
		//设置用户免打扰信息
		try {
			//设置当前用户id
			userConfig.setSetupUserId(userId);
			DeferredResult<String> dr = new DeferredResult<>();
			BaseResult<Integer> resultData = publicUserConfigService.configTopStatus(bundle,userConfig);
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

	@ResponseBody
	@RequestMapping(value = "/api/user/getConfigInfos", method = RequestMethod.POST, produces = {Const.PRODUCES_JSON})
	public DeferredResult<String> getConfigInfos(@RequestBody UserConfig userConfig, HttpServletRequest request) {
		//根据request 请求获取访问用户本地语言
		PackBundle bundle = LangResource.getResourceBundle(request);
		//api名称
		String apiName = "post + /api/user/getConfigInfos";
		//获取当前登录用户信息
		UserDetails userDetails = SessionUtil.getUserDetails(request);
		if(userDetails == null) {
			DeferredResult<String> dr = new DeferredResult<>();
			dr.setResult(toJson(EnumStatus.NINETY_EIGHT.getIndex(), bundle.getString("user.not.login"), apiName, null));
			return dr;
		}
		//如果入参没有用户id，系统默认获取登录用户的id
		String userId = userConfig.getTargetId();
		if (StringUtil.isEmpty(userId)) {
			userId = userDetails.getUserId();
		}
		//设置用户免打扰信息
		try {
			//设置当前用户id
			userConfig.setSetupUserId(userId);
			DeferredResult<String> dr = new DeferredResult<>();
			BaseResult<Map<String,Object>>  resultData = publicUserConfigService.getUserConfigInfos(userConfig);
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
