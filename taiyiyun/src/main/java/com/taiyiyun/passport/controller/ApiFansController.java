package com.taiyiyun.passport.controller;

import com.alibaba.fastjson.JSON;
import com.taiyiyun.passport.bean.UserDetails;
import com.taiyiyun.passport.consts.Const;
import com.taiyiyun.passport.consts.EnumStatus;
import com.taiyiyun.passport.language.LangResource;
import com.taiyiyun.passport.language.PackBundle;
import com.taiyiyun.passport.po.BaseResult;
import com.taiyiyun.passport.po.follower.UserFollower;
import com.taiyiyun.passport.service.IPublicUserFollowerService;
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
import java.util.List;

@Controller
public class ApiFansController extends BaseController {
	
	@Resource
	private IPublicUserFollowerService publicUserFollowerService;
	
	@ResponseBody
	@RequestMapping(value = "/api/user/getFans", method = RequestMethod.POST, produces = {Const.PRODUCES_JSON})
	public DeferredResult<String> getFans(@RequestBody UserFollower userFollower, HttpServletRequest request) {
		//根据request 请求获取访问用户本地语言
		PackBundle bundle = LangResource.getResourceBundle(request);
		//api名称
		String apiName = "get + /api/user/getFans";
		//获取当前登录用户信息
		UserDetails userDetails = SessionUtil.getUserDetails(request);
		if(userDetails == null) {
			DeferredResult<String> dr = new DeferredResult<>();
			dr.setResult(toJson(EnumStatus.NINETY_EIGHT.getIndex(), bundle.getString("user.not.login"), apiName, null));
			return dr;
		}
		//如果入参没有用户id，系统默认获取登录用户的id
		String userId = userFollower.getUserId();
		if (StringUtil.isEmpty(userId)) {
			userId = userDetails.getUserId();
		}
		//调用查询粉丝信息业务
		try {
			DeferredResult<String> dr = new DeferredResult<>();
			userFollower.setUserId(userId);
			BaseResult<List<UserFollower>> resultData = publicUserFollowerService.listFansPage(userFollower);
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
	@RequestMapping(value = "/api/user/getFollowers", method = RequestMethod.POST, produces = {Const.PRODUCES_JSON})
	public DeferredResult<String> getFollowers(@RequestBody UserFollower userFollower, HttpServletRequest request) {
		//根据request 请求获取访问用户本地语言
		PackBundle bundle = LangResource.getResourceBundle(request);
		//api名称
		String apiName = "get + /api/user/getFollowers";
		//获取当前登录用户信息
		UserDetails userDetails = SessionUtil.getUserDetails(request);
		if(userDetails == null) {
			DeferredResult<String> dr = new DeferredResult<>();
			dr.setResult(toJson(EnumStatus.NINETY_EIGHT.getIndex(), bundle.getString("user.not.login"), apiName, null));
			return dr;
		}
		//如果入参没有用户id，系统默认获取登录用户的id
		String userId = userFollower.getUserId();
		if (StringUtil.isEmpty(userId)) {
			userId = userDetails.getUserId();
		}
		//调用查询我关注的用户信息业务
		try {
			DeferredResult<String> dr = new DeferredResult<>();
			userFollower.setFollowerId(userId);
			BaseResult<List<UserFollower>> resultData = publicUserFollowerService.listFollowerPage(userFollower);
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
