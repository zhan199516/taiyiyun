package com.taiyiyun.passport.controller;

import com.alibaba.fastjson.JSON;
import com.taiyiyun.passport.bean.*;
import com.taiyiyun.passport.consts.Const;
import com.taiyiyun.passport.consts.EnumStatus;
import com.taiyiyun.passport.language.LangResource;
import com.taiyiyun.passport.language.PackBundle;
import com.taiyiyun.passport.mqtt.Message;
import com.taiyiyun.passport.mqtt.Message.ContentType;
import com.taiyiyun.passport.mqtt.Message.MessageType;
import com.taiyiyun.passport.mqtt.Message.SessionType;
import com.taiyiyun.passport.po.BaseResult;
import com.taiyiyun.passport.po.PublicUser;
import com.taiyiyun.passport.po.ThirdApp;
import com.taiyiyun.passport.po.circle.UserContent;
import com.taiyiyun.passport.po.setting.UserConfig;
import com.taiyiyun.passport.po.user.UserDto;
import com.taiyiyun.passport.service.*;
import com.taiyiyun.passport.util.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.*;

@Controller
public class ApiPublicUserController extends BaseController  {

	private static final String TAG = "?platform=sharepassport";
	
	@Resource
	private IPublicUserService publicUserService;
	
	@Resource
	private IPublicUserFollowerService publicUserFollowerService;

	@Resource
	private IPublicUserBlockService publicUserBlockService;

	@Resource
	private IMqttUserService mqttUserService;
	
	@Resource
	private IThirdAppService thirdAppService;

	@Resource
	private IPublicUserConfigService publicUserConfigService;

	public final Logger logger = LoggerFactory.getLogger(getClass());
	
	/**
	 * 检查共享号名称是否可用
	 * @param userName
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/api/share/user/nameCheck", method = RequestMethod.POST, produces = {Const.PRODUCES_JSON})
	public String checkName(String userName, HttpServletRequest request) {
		UserDetails userDetails = SessionUtil.getUserDetails(request);

		PackBundle bundle = LangResource.getResourceBundle(request);
		String apiName = "post + /api/share/user/nameCheck";

		if(null == userDetails) {
			return toJson(1, bundle.getString("user.not.login"), apiName, new ArrayList<Object>());
		}
		
		if(StringUtil.isEmpty(userName)) {
			return toJson(2, bundle.getString("need.param", "userName"), apiName, new ArrayList<Object>());
		}
		
		if(userName.length() > 200) {
			return toJson(3, bundle.getString("user.name.length", "200"), apiName, new ArrayList<Object>());
		}
		
		if (Misc.regexShareName(userName)) {
			return toJson(4, bundle.getString("user.name.content"), apiName, new ArrayList<Object>());
		}
		
		PublicUser psBean = publicUserService.getByUserName(userName);
		if (null != psBean) {
			return toJson(5, bundle.getString("user.name.found"), apiName, new ArrayList<Object>());
		}
		
		return toJson(0, bundle.getString("user.name.available"), apiName, new ArrayList<Object>());
	}
	
	/**
	 * 创建共享号
	 * @param userName
	 * @param description
	 * @param
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/api/share/user/create", method = RequestMethod.POST, produces = {Const.PRODUCES_JSON})
	public String createUser(@RequestParam(value = "userName", required = true) String userName, String description,
			 MultipartFile userLogo, HttpServletRequest request) {
		UserDetails userDetails = SessionUtil.getUserDetails(request);

		PackBundle bundle = LangResource.getResourceBundle(request);
		String apiName = "post + /api/share/user/create";

		if(null == userDetails) {
			return toJson(1, bundle.getString("user.not.login"), apiName, new ArrayList<Object>());
		}
		if (Misc.regexShareName(userName)) {
			return toJson(2, bundle.getString("user.name.content"), apiName, new ArrayList<Object>());
		}

		if (userDetails.getMobile().equals(userName)) {
			return toJson(2, bundle.getString("user.name.mobile.not.allow"), apiName, new ArrayList<Object>());
		}

		PublicUser psBean = publicUserService.getByUserName(userName);
		if (null != psBean) {
			return toJson(3, bundle.getString("user.name.found"), apiName, new ArrayList<Object>());
		}

		PublicUser user = new PublicUser();
		try {
			
			if(!FileUtil.multipartFileIsNull(userLogo)) {
				FileBean logo = FileUtil.saveFile(request, false, "files/images", userLogo, ".PNG|.JPG|.GIF|.JPEG|.BMP");
				user.setAvatarUrl(logo.getRelativePath());
				FileBean ThumbnailFile = FileUtil.getThumbnail(request, false, "files/images/thumbnail", userLogo, ".PNG|.JPG|.GIF|.JPEG|.BMP");
				ImageUtil.zoomImage(logo.getFile(), ThumbnailFile.getFile(), 0.5);
				user.setThumbAvatarUrl(ThumbnailFile.getRelativePath());
			}else {
				user.setAvatarUrl("resources/images/user/user_log_b.png");
				user.setThumbAvatarUrl("resources/images/user/user_logo_s.png");
			}
			user.setBackgroundImgUrl("resources/images/user/user_bg.png");
			
		} catch (Exception e) {
			e.printStackTrace();
			return toJson(4, bundle.getString("failed.execute"), "post + /api/share/user/create", new ArrayList<Object>());
		}

		try {
			user.setVersion(2);
			user.setStatus("1");
			user.setTypeId(0);
			user.setUserName(userName);
			user.setUuid(userDetails.getUuid());
			user.setCreateTime(new Timestamp(new Date().getTime()));
			user.setDescription(description);
			user.setMobile(userDetails.getMobile());
			
			PublicUser unUser = publicUserService.getUnregisteredUserByMobile(userDetails.getMobile());
			
			if(null != unUser) {
				user.setId(unUser.getId());
				publicUserService.update(user);
				
				try {
					UserCache userCache = CacheUtil.getOneDay(Const.APP_USERCACHE + user.getId());
					
					if(null != userCache) {
						userCache.setUuid(user.getUuid());
						userCache.setUserName(user.getUserName());
						userCache.setMobile(user.getMobile());
						CacheUtil.putOneDay(Const.APP_USERCACHE + user.getId(), userCache);
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}else{
				publicUserService.save(user);
			}
			
			List<Message<UserContent>> jsonList = new ArrayList<>();
			Message<UserContent> msg = new Message<UserContent>();
			msg.setContentType(ContentType.CONTENT_CIRCLE_USERINFO_UPDATE.getValue());
			msg.setMessageId(Misc.getMessageId());
			msg.setMessageType(MessageType.MESSAGE_CIRCLE_USERINFO.getValue());
			msg.setVersion(1);
			msg.setUpdateTime(new Date().getTime());
			msg.setSessionType(SessionType.SESSION_CIRCLE.getValue());
			msg.setSessionId(user.getId());
			msg.setFromUserId(user.getId());
			msg.setFromClientId(Misc.getClientId());
			
			UserContent userContent = new UserContent();
			userContent.setUserId(Misc.getNotNullValue(user.getId()));
			userContent.setUserName(user.getUserName());
			userContent.setType(user.getTypeId());
			userContent.setDescription(Misc.getNotNullValue(user.getDescription()));
			
			if(StringUtil.isNotEmpty(user.getAvatarUrl())) {
				userContent.setAvatarUrl(Misc.getServerUri(request, user.getAvatarUrl()));
			}
			if(StringUtil.isNotEmpty(user.getThumbAvatarUrl())) {
				userContent.setThumbAvatarUrl(Misc.getServerUri(request, user.getThumbAvatarUrl()));
			}
			if(StringUtil.isNotEmpty(user.getBackgroundImgUrl())) {
				userContent.setBackgroundImgUrl(Misc.getServerUri(request, user.getBackgroundImgUrl()));
			}
			msg.setContent(userContent);
			
			jsonList.add(msg);
			
			if(StringUtil.isEmpty(userDetails.getUserId())) {
				List<PublicUser> userList = publicUserService.getByUuid(user.getUuid());
				if(null == userList || userList.size() <= 0) {
					throw new Exception(bundle.getString("user.name.not.find"));
				}
				PublicUser publicUser = userList.get(0);
				
				userDetails.setUserId(publicUser.getId());
				userDetails.setUserName(publicUser.getUserName());
				userDetails.setUserKey(publicUser.getUserKey());
				userDetails.setDescription(publicUser.getDescription());
				userDetails.setAvatarUrl(Misc.getServerUri(request, publicUser.getAvatarUrl()));
				userDetails.setBackgroundImgUrl(Misc.getServerUri(request, publicUser.getBackgroundImgUrl()));
				userDetails.setThumbAvatarUrl(Misc.getServerUri(request, publicUser.getThumbAvatarUrl()));
				SessionUtil.addUserDetails(request, userDetails);
			}
			//用户注册成功后，关注通用公共账户
			//同时创建群，让该用户自动进群获取奖励
			try {
				logger.info("开始自动关注、建群、入群，注册用户:" + JSON.toJSONString(user));
				publicUserService.registerPrize(bundle, user.getUuid());
			}
			catch (Exception e){
				e.printStackTrace();
				logger.info("注册关注、建群、入群异常：" + e.getMessage());
			}
			return toJson(0, bundle.getString("successful.user.create"), apiName, jsonList);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			logger.error(e.getLocalizedMessage());
			return toJson(5, bundle.getString("failed.execute"), apiName, new ArrayList<Object>());
		}
		
	}

	/**
	 * 修改共享号信息,不允许将手机号作为共享号名称
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/api/share/user/update",method = RequestMethod.POST, produces = {Const.PRODUCES_JSON})
	public String updateUser(MultipartFile userLogo, String userName, String description,HttpServletRequest request) {
			
		UserDetails userDetails = SessionUtil.getUserDetails(request);

		PackBundle bundle = LangResource.getResourceBundle(request);
		String apiName = "post + /api/share/user/update";

		if(null == userDetails) {
			return toJson(1,  bundle.getString("user.not.login"), apiName, new ArrayList<Object>());
		}
		
		if(StringUtil.isEmpty(userDetails.getUserId())) {
			return toJson(5, bundle.getString("user.not.login"), apiName, new ArrayList<Object>());
		}
		
		PublicUser user = publicUserService.getByStrictId(userDetails.getUuid(), userDetails.getUserId());
		if(null == user) {
			return toJson(2, bundle.getString("user.name.not.find"), apiName, new ArrayList<Object>());
		}
		
		try {
			
			if(!FileUtil.multipartFileIsNull(userLogo)) {
				FileBean logo = FileUtil.saveFile(request, false, "files/images", userLogo, ".PNG|.JPG|.GIF|.JPEG|.BMP");
				user.setAvatarUrl(logo.getRelativePath());
				FileBean ThumbnailFile = FileUtil.getThumbnail(request, false, "files/images/thumbnail", userLogo, ".PNG|.JPG|.GIF|.JPEG|.BMP");
				ImageUtil.zoomImage(logo.getFile(), ThumbnailFile.getFile(), 0.5);
				user.setThumbAvatarUrl(ThumbnailFile.getRelativePath());
			}
			
			if(StringUtil.isNotEmpty(userName)) {
				if (Misc.regexShareName(userName)) {
					return toJson(3, bundle.getString("user.name.content"), apiName, new ArrayList<Object>());
				}
				
				if(userDetails.getMobile().equals(userName)) {
					return toJson(3, bundle.getString("user.name.mobile.not.allow"), apiName, new ArrayList<Object>());
				}

				PublicUser psBean = publicUserService.getByUserName(userName);
				if (null != psBean && !psBean.getId().equals(user.getId())) {
					return toJson(4, bundle.getString("user.name.found"), apiName, new ArrayList<Object>());
				}
				user.setUserName(userName);
			}
			
			if(StringUtil.isNotEmpty(description)) {
				user.setDescription(description);
			}
			
			publicUserService.update(user);
			
			try {
				UserCache userCache = CacheUtil.getOneDay(Const.APP_USERCACHE + user.getId());
				
				if(null != userCache && !userCache.getUserName().equals(user.getUserName())) {
					userCache.setUserName(user.getUserName());
					CacheUtil.putOneDay(Const.APP_USERCACHE + user.getId(), userCache);
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			List<Message<UserContent>> jsonList = new ArrayList<>();
			Message<UserContent> msg = new Message<UserContent>();
			msg.setContentType(ContentType.CONTENT_CIRCLE_USERINFO_UPDATE.getValue());
			msg.setMessageType(MessageType.MESSAGE_CIRCLE_USERINFO.getValue());
			msg.setVersion(1);
			msg.setUpdateTime(new Date().getTime());
			msg.setSessionType(SessionType.SESSION_CIRCLE.getValue());
			msg.setSessionId(user.getId());
			msg.setMessageId(Misc.getMessageId());
			msg.setFromUserId(user.getId());
			msg.setFromClientId(Misc.getClientId());
			
			UserContent userContent = new UserContent();
			userContent.setUserId(Misc.getNotNullValue(user.getId()));
			userContent.setUserName(user.getUserName());
			userContent.setType(user.getTypeId());
			userContent.setDescription(Misc.getNotNullValue(user.getDescription()));
			
			if(StringUtil.isNotEmpty(user.getAvatarUrl())) {
				userContent.setAvatarUrl(Misc.getServerUri(request, user.getAvatarUrl()));
			}
			if(StringUtil.isNotEmpty(user.getThumbAvatarUrl())) {
				userContent.setThumbAvatarUrl(Misc.getServerUri(request, user.getThumbAvatarUrl()));
			}
			if(StringUtil.isNotEmpty(user.getBackgroundImgUrl())) {
				userContent.setBackgroundImgUrl(Misc.getServerUri(request, user.getBackgroundImgUrl()));
			}
			msg.setContent(userContent);
			
			jsonList.add(msg);
			
			//MessagePublisher.getInstance().addPublish(String.format(Message.DOWNLINK_PUBLIC_SHARE_MESSAGE_USERINFO, user.getId()), msg);
			com.taiyiyun.passport.mqtt.v2.MessagePublisher.getInstance().publish(String.format(Message.UPLINK_PUBLIC_SHARE_MESSAGE_USERINFO, user.getId()), msg);
			
			return toJson(0, bundle.getString("successful.modify"), apiName, jsonList);
			
		} catch (Exception e) {
			e.printStackTrace();
			return toJson(4, bundle.getString("failed.execute"), apiName, new ArrayList<Object>());
		}

	}
	
	/**
	 * 删除共享号
	 * @param userId
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/api/share/user/delete", method = RequestMethod.POST, produces = {Const.PRODUCES_JSON})
	public String deleteUser(String userId, HttpServletRequest request) {
		UserDetails userDetails = SessionUtil.getUserDetails(request);

		PackBundle bundle = LangResource.getResourceBundle(request);
		String apiName = "post + /api/share/user/delete";

		if(null == userDetails) {
			return toJson(1, bundle.getString("user.not.login"), apiName, new ArrayList<Object>());
		}
		
		if(StringUtil.isEmpty(userId)) {
			return toJson(0, bundle.getString("need.param", "userId"), apiName, new ArrayList<Object>());
		}

		try {
			
			int count = publicUserService.delete(userDetails.getUuid(),userId);
			if(count == 1) {
				return toJson(0, bundle.getString("successful.delete"), apiName, new ArrayList<Object>());
			}
			
			return toJson(3, bundle.getString("failed.delete.before"), apiName, new ArrayList<Object>());
		} catch (Exception e) {
			e.printStackTrace();
			return toJson(2, bundle.getString("failed.execute"), apiName, new ArrayList<Object>());
		}
		
	}
	
	/**
	 * 获取当前账号的共享号列表
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/api/share/user/getUserList", method = RequestMethod.GET, produces = {Const.PRODUCES_JSON})
	public String getUserList(HttpServletRequest request) {
		UserDetails userDetails = SessionUtil.getUserDetails(request);

		PackBundle bundle = LangResource.getResourceBundle(request);
		String apiName = "get + /api/share/user/getUserList";

		if(null == userDetails) {
			return toJson(1, bundle.getString("user.not.login"), "get + /api/share/user/getUserList", new ArrayList<Object>());
		}
		
		List<Map<String, Object>> jsonList = new ArrayList<>();
		try {
			List<PublicUser> userList = publicUserService.getByUuid(userDetails.getUuid());
			if(null != userList && userList.size() > 0) {
				for(PublicUser user : userList) {
					Map<String,Object> jsonMap = new HashMap<String,Object>();
					
					jsonMap.put("userId", user.getId());
					jsonMap.put("userName", user.getUserName());
					jsonMap.put("description", user.getDescription());
					jsonMap.put("type", user.getTypeId());
					jsonMap.put("useTime", user.getUseTime());
					if(StringUtil.isNotEmpty(user.getAvatarUrl())) {
						jsonMap.put("avatarUrl", Misc.getServerUri(request, user.getAvatarUrl()));
					}
					if(StringUtil.isNotEmpty(user.getThumbAvatarUrl())) {
						jsonMap.put("thumbAvatarUrl", Misc.getServerUri(request, user.getThumbAvatarUrl()));
					}
					if(StringUtil.isNotEmpty(user.getBackgroundImgUrl())) {
						jsonMap.put("backgroundImgUrl", Misc.getServerUri(request, user.getBackgroundImgUrl()));
					}
					jsonList.add(jsonMap);
				}
			}
			
			return toJson(0, bundle.getString("successful.search"), "get + /api/share/user/getUserList", jsonList);
		} catch (Exception e) {
			e.printStackTrace();
			return toJson(2, bundle.getString("failed.execute"), "get + /api/share/user/getUserList", new ArrayList<Object>());
		}
	}
	
	/**
	 * 切换共享号
	 * @param userId
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/api/share/user/switchUser", method = {RequestMethod.POST}, produces = {Const.PRODUCES_JSON})
	public String switchUser(String userId, HttpServletRequest request) {
		UserDetails currentUserDetails = SessionUtil.getUserDetails(request);

		PackBundle bundle = LangResource.getResourceBundle(request);
		String apiName = "post + /api/share/user/switchUser";

		if(null == currentUserDetails) {
			return toJson(1, bundle.getString("user.not.login"), "post + /api/share/user/switchUser", new ArrayList<Object>());
		}
		
		try {
			PublicUser publicUser = publicUserService.getByStrictId(currentUserDetails.getUuid(), userId);
			if(null == publicUser) {
				return toJson(2, bundle.getString("user.name.not.find"), "post + /api/share/user/switchUser", new ArrayList<Object>());
			}
			
			publicUser.setUseTime(new Date());
			publicUserService.update(publicUser);
			
			UserDetails userDetails = new UserDetails();
			userDetails.setUuid(currentUserDetails.getUuid());
			userDetails.setNikeName(currentUserDetails.getNikeName());
			userDetails.setMobile(currentUserDetails.getMobile());
			userDetails.setUserId(publicUser.getId());
			userDetails.setUserName(publicUser.getUserName());
			userDetails.setUserKey(publicUser.getUserKey());
			userDetails.setAvatarUrl(publicUser.getAvatarUrl());
			userDetails.setThumbAvatarUrl(publicUser.getThumbAvatarUrl());
			userDetails.setBackgroundImgUrl(publicUser.getBackgroundImgUrl());
			userDetails.setDescription(publicUser.getDescription());
			SessionUtil.addUserDetails(request, userDetails);
			
			List<Map<String, Object>> jsonList = new ArrayList<>();
			Map<String, Object> jsonMap = new HashMap<>();
			jsonMap.put("userId", publicUser.getId());
			jsonMap.put("userName", publicUser.getUserName());
			jsonMap.put("avatarUrl", Misc.getServerUri(request, publicUser.getAvatarUrl()));
			jsonMap.put("thumbAvatarUrl", Misc.getServerUri(request, publicUser.getThumbAvatarUrl()));
			jsonMap.put("backgroundImgUrl", Misc.getServerUri(request, publicUser.getBackgroundImgUrl()));
			jsonMap.put("description", publicUser.getDescription());
			jsonMap.put("type", publicUser.getTypeId());
			
			Object us = mqttUserService.getByUserId(publicUser.getId());
			jsonMap.put("mqtt", us);
			
			jsonList.add(jsonMap);
			return toJson(0, bundle.getString("successful.operation"), "post + /api/share/user/switchUser", jsonList);
			
		} catch (Exception e) {
			e.printStackTrace();
			return toJson(3, bundle.getString("failed.execute"), "post + /api/share/user/switchUser", new ArrayList<Object>());
		}
		
	}

	/**
	 * 获取当前共享号的信息
	 * @param request 请求
	 * @param os 设备类型
	 * @param version 版本号
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/api/share/user/getCurrentUser", method = {RequestMethod.GET}, produces = {Const.PRODUCES_JSON})
	public String getCurrentUser(HttpServletRequest request, @RequestParam(value="os", required = false) String os, @RequestParam(value="version", required = false) String version) {

		PackBundle bundle = LangResource.getResourceBundle(request);
		String apiName = "get + /api/share/user/getCurrentUser";

		UserDetails currentUserDetails = SessionUtil.getUserDetails(request);
		if(null == currentUserDetails) {
			return toJson(1, bundle.getString("user.not.login"), apiName, new ArrayList<Object>());
		}
		try {
			PublicUser user = null;
			if(StringUtil.isEmpty(currentUserDetails.getUserId())) {
				List<PublicUser> users = publicUserService.getByUuid(currentUserDetails.getUuid());
				if(null != users && users.size() > 0) {
					user = users.get(0);
				}
			}else {
				user = publicUserService.getByUserId(currentUserDetails.getUserId());
				//注释掉原有判断，可以自动创建共享号
//				if(null == user) {
//					return toJson(2, bundle.getString("user.name.not.find"), apiName, new ArrayList<Object>());
//				}
			}
			//如果user为空默认创建共享号
			if (user == null){
				BaseResult<PublicUser> resultData = publicUserService.createDefaultShareAccount(bundle,currentUserDetails,true);
				if (resultData != null && resultData.getStatus() == EnumStatus.ZORO.getIndex()){
					user = resultData.getData();
				}
			}
			if(null != user) {
				//设置登录session
				currentUserDetails.setUserId(user.getId());
				currentUserDetails.setUserName(user.getUserName());
				currentUserDetails.setDescription(user.getDescription());
				currentUserDetails.setAvatarUrl(Misc.getServerUri(request, user.getAvatarUrl()));
				currentUserDetails.setBackgroundImgUrl(Misc.getServerUri(request, user.getBackgroundImgUrl()));
				currentUserDetails.setThumbAvatarUrl(Misc.getServerUri(request, user.getThumbAvatarUrl()));
				SessionUtil.addUserDetails(request, currentUserDetails);
				Map<String,Long> statMap = publicUserService.statUserInfo(currentUserDetails.getUserId());
				if(null == statMap) {
					return toJson(3, bundle.getString("user.name.not.find"), apiName, new ArrayList<Object>());
				}
				//获取用户免打扰状态
				Integer isDisturb = 0;
				UserConfig userConfig = new UserConfig();
				userConfig.setSetupUserId(user.getId());
				userConfig.setUserType(EnumStatus.ZORO.getIndex());
				BaseResult<UserConfig> resultUserConfig = publicUserConfigService.getOneUserConfig(userConfig);
				if (resultUserConfig != null ){
					userConfig = resultUserConfig.getData();
					if (userConfig != null){
						isDisturb = userConfig.getIsDisturb();
					}
				}
				//获取系统全局免打扰状态
				Map<String,Object> jsonMap = new HashMap<>();
				jsonMap.put("userId", user.getId());
				jsonMap.put("isDisturb", isDisturb);
				jsonMap.put("userName", user.getUserName());
				jsonMap.put("description", Misc.getNotNullValue(user.getDescription()));
				jsonMap.put("type", user.getTypeId());
				if(StringUtil.isNotEmpty(user.getAvatarUrl())) {
					jsonMap.put("avatarUrl", Misc.getServerUri(request, user.getAvatarUrl()));
				}
				if(StringUtil.isNotEmpty(user.getThumbAvatarUrl())) {
					jsonMap.put("thumbAvatarUrl", Misc.getServerUri(request, user.getThumbAvatarUrl()));
				}
				if(StringUtil.isNotEmpty(user.getBackgroundImgUrl())) {
					jsonMap.put("backgroundImgUrl", Misc.getServerUri(request, user.getBackgroundImgUrl()));
				}
				Map<String,Object> countMap = new HashMap<>();
				countMap.put("totalReadCount", statMap.get("read"));
				countMap.put("totalCommentCount", statMap.get("replay"));
				countMap.put("totalUpCount", statMap.get("thumbUp"));
				countMap.put("totalDownCount", statMap.get("thumbDown"));
				countMap.put("totalFollowCount", statMap.get("like"));
				countMap.put("totalArticleCount", statMap.get("article"));
				countMap.put("totalShareCount", 0);
				jsonMap.put("count", countMap);
				Object us = mqttUserService.getByUserId(user.getId());
				jsonMap.put("mqtt", us);
				List<Map<String, Object>> list = new ArrayList<>();
				list.add(jsonMap);
				return toJson(0, bundle.getString("successful.search"), apiName, list);
			}
			return toJson(0, bundle.getString("successful.search"), apiName, new ArrayList<Object>());
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			return toJson(2, bundle.getString("failed.execute"), apiName, new ArrayList<Object>());
		}
	}
	
	/**
	 * 修改当前登录号背景
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/api/share/user/uploadBackgroundImg", method = {RequestMethod.POST}, produces = {Const.PRODUCES_JSON})
	public String uploadBackgroundImg(@RequestParam("bgImage") MultipartFile bgImage, HttpServletRequest request) {
		UserDetails userDetails = SessionUtil.getUserDetails(request);

		PackBundle bundle = LangResource.getResourceBundle(request);
		String apiName = "post + /api/share/user/uploadBackgroundImg";

		if(null == userDetails) {
			return toJson(1, bundle.getString("user.not.login"), apiName, new ArrayList<Object>());
		}
		
		if(StringUtil.isEmpty(userDetails.getUserId())) {
			return toJson(6, bundle.getString("user.name.not.find"), apiName, new ArrayList<Object>());
		}
		
		if(FileUtil.multipartFileIsNull(bgImage)) {
			return toJson(2, bundle.getString("need.param", "bgImage"), apiName, new ArrayList<Object>());
		}

		try {
			
			PublicUser user = publicUserService.getByStrictId(userDetails.getUuid(), userDetails.getUserId());
			if(null == user) {
				return toJson(3, bundle.getString("user.name.not.find"), apiName, new ArrayList<Object>());
			}
			
			FileBean backgroundImg = FileUtil.saveFile(request, false, "files/images/background", bgImage, ".PNG|.JPG|.GIF|.JPEG|.BMP");
			
			if(null != backgroundImg) {
				user.setBackgroundImgUrl(backgroundImg.getRelativePath());
				user.setId(userDetails.getUserId());
				publicUserService.update(user);
				
				List<Message<PublicUser>> jsonList = new ArrayList<>();
				Message<PublicUser> msg = new Message<PublicUser>();
				msg.setContentType(ContentType.CONTENT_CIRCLE_USERINFO_UPDATE.getValue());
				msg.setMessageType(MessageType.MESSAGE_CIRCLE_USERINFO.getValue());
				msg.setVersion(1);
				msg.setUpdateTime(new Date().getTime());
				msg.setSessionType(SessionType.SESSION_CIRCLE.getValue());
				msg.setSessionId(user.getId());
				
				if(StringUtil.isNotEmpty(user.getAvatarUrl())) {
					user.setAvatarUrl(Misc.getServerUri(request, user.getAvatarUrl()));
				}
				if(StringUtil.isNotEmpty(user.getThumbAvatarUrl())) {
					user.setThumbAvatarUrl(Misc.getServerUri(request, user.getThumbAvatarUrl()));
				}
				if(StringUtil.isNotEmpty(user.getBackgroundImgUrl())) {
					user.setBackgroundImgUrl(Misc.getServerUri(request, user.getBackgroundImgUrl()));
				}
				msg.setContent(user);
				
				jsonList.add(msg);
				
				return toJson(0, bundle.getString("successful.background"), apiName, jsonList);
			}
			
			return toJson(4, bundle.getString("failed.background"), apiName, new ArrayList<Object>());
			
		} catch (Exception e) {
			e.printStackTrace();
			return toJson(5, bundle.getString("failed.execute"), apiName, new ArrayList<Object>());
		}
		
	}
	
	/**
	 * 取消关注共享号
	 * @param userId
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/api/share/user/unfollow", method = {RequestMethod.POST}, produces = {Const.PRODUCES_JSON})
	public String unfollowPublicUser(String userId, HttpServletRequest request) {
		UserDetails userDetails = SessionUtil.getUserDetails(request);

		PackBundle bundle = LangResource.getResourceBundle(request);
		String apiName = "post + /api/share/user/unfollow";


		if(null == userDetails) {
			return toJson(1, bundle.getString("user.not.login"), apiName, new ArrayList<>());
		}
		
		if(StringUtil.isEmpty(userId)) {
			return toJson(1, bundle.getString("need.param", "userId"), apiName, new ArrayList<>());
		}
		
		if(StringUtil.isEmpty(userDetails.getUserId())) {
			return toJson(5, bundle.getString("user.name.not.find"), apiName, new ArrayList<Object>());
		}
		
		try {
			PublicUser unFollowUser = publicUserService.getByUserId(userId);
			if(null == unFollowUser) {
				return toJson(3, bundle.getString("user.dest.not.find"), apiName, new ArrayList<>());
			}
			
			int count = publicUserFollowerService.unfollowPublicUser(userDetails.getUserId(), userId);
			
			if(count >= 0) {
				List<Message<UserContent>> jsonList = new ArrayList<>();
				Message<UserContent> msg = new Message<UserContent>();
				msg.setContentType(ContentType.CONTENT_CIRCLE_USERINFO_UPDATE.getValue());
				msg.setMessageType(MessageType.MESSAGE_CIRCLE_USERINFO.getValue());
				msg.setVersion(1);
				msg.setUpdateTime(new Date().getTime());
				msg.setSessionType(SessionType.SESSION_CIRCLE.getValue());
				msg.setSessionId(userDetails.getUserId());
				
				UserContent userContent = new UserContent();
				userContent.setUserId(Misc.getNotNullValue(unFollowUser.getId()));
				userContent.setUserName(unFollowUser.getUserName());
				userContent.setType(unFollowUser.getTypeId());
				userContent.setDescription(Misc.getNotNullValue(unFollowUser.getDescription()));
				
				if(StringUtil.isNotEmpty(unFollowUser.getAvatarUrl())) {
					userContent.setAvatarUrl(Misc.getServerUri(request, unFollowUser.getAvatarUrl()));
				}
				if(StringUtil.isNotEmpty(unFollowUser.getThumbAvatarUrl())) {
					userContent.setThumbAvatarUrl(Misc.getServerUri(request, unFollowUser.getThumbAvatarUrl()));
				}
				if(StringUtil.isNotEmpty(unFollowUser.getBackgroundImgUrl())) {
					userContent.setBackgroundImgUrl(Misc.getServerUri(request, unFollowUser.getBackgroundImgUrl()));
				}
				msg.setContent(userContent);
				jsonList.add(msg);
				
				return toJson(0, bundle.getString("successful.unfollow"), apiName, jsonList);
			}
			
			return toJson(0, bundle.getString("failed.unfollow.done"), apiName, new ArrayList<>());
		} catch (Exception e) {
			e.printStackTrace();
			return toJson(2, bundle.getString("failed.execute"), apiName, new ArrayList<>());
		}
	}
	
	/**
	 * 关注共享号
	 * @param userId
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/api/share/user/follow", method = {RequestMethod.POST}, produces = {Const.PRODUCES_JSON})
	public String followPublicUser(String userId, HttpServletRequest request) {
		UserDetails userDetails = SessionUtil.getUserDetails(request);

		PackBundle bundle = LangResource.getResourceBundle(request);
		String apiName = "post + /api/share/user/follow";

		if(null == userDetails) {
			return toJson(1, bundle.getString("user.not.login"), apiName, new ArrayList<Object>());
		}
		
		if(StringUtil.isEmpty(userId)) {
			return toJson(2, bundle.getString("need.param", "userId"), apiName, new ArrayList<Object>());
		}
		
		if(StringUtil.isEmpty(userDetails.getUserId())) {
			return toJson(4, bundle.getString("user.name.not.find"), apiName, new ArrayList<Object>());
		}
		
		try {
			PublicUser followUser = publicUserService.getByUserId(userId);
			if(null == followUser) {
				return toJson(3, bundle.getString("user.dest.not.find"), apiName, new ArrayList<Object>());
			}
			
			int count = publicUserFollowerService.focusPublicUser(userDetails.getUserId(),userId);
			
			List<Message<UserContent>> jsonList = new ArrayList<>();
			Message<UserContent> msg = new Message<UserContent>();
			msg.setContentType(ContentType.CONTENT_CIRCLE_USERINFO_UPDATE.getValue());
			msg.setMessageType(MessageType.MESSAGE_CIRCLE_USERINFO.getValue());
			msg.setVersion(1);
			msg.setUpdateTime(new Date().getTime());
			msg.setSessionType(SessionType.SESSION_CIRCLE.getValue());
			msg.setSessionId(userDetails.getUserId());
			
			UserContent userContent = new UserContent();
			userContent.setUserId(Misc.getNotNullValue(followUser.getId()));
			userContent.setUserName(followUser.getUserName());
			userContent.setType(followUser.getTypeId());
			userContent.setDescription(Misc.getNotNullValue(followUser.getDescription()));
			
			if(StringUtil.isNotEmpty(followUser.getAvatarUrl())) {
				userContent.setAvatarUrl(Misc.getServerUri(request, followUser.getAvatarUrl()));
			}
			if(StringUtil.isNotEmpty(followUser.getThumbAvatarUrl())) {
				userContent.setThumbAvatarUrl(Misc.getServerUri(request, followUser.getThumbAvatarUrl()));
			}
			if(StringUtil.isNotEmpty(followUser.getBackgroundImgUrl())) {
				userContent.setBackgroundImgUrl(Misc.getServerUri(request, followUser.getBackgroundImgUrl()));
			}
			msg.setContent(userContent);
			jsonList.add(msg);
			
			return toJson(0, count == 1 ? bundle.getString("successful.follow") : bundle.getString("failed.follow.done"), apiName, jsonList);
			
		} catch (Exception e) {
			e.printStackTrace();
			return toJson(5, bundle.getString("failed.execute"), apiName, new ArrayList<Object>());
		}
	}
	
	/**
	 * 获取关注列表，只返回关注用户的 userId
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/api/share/user/requestMyFollowingList", method = {RequestMethod.GET}, produces = {Const.PRODUCES_JSON})
	public String getFollowerList(HttpServletRequest request) {
		UserDetails userDetails = SessionUtil.getUserDetails(request);

		PackBundle bundle = LangResource.getResourceBundle(request);

		if(null == userDetails) {
			return toJson(1, bundle.getString("user.not.login"), "get + api/share/user/requestMyFollowingList", new ArrayList<Object>());
		}
		
		if(StringUtil.isEmpty(userDetails.getUserId())) {
			return toJson(3, bundle.getString("user.name.not.find"), "get + api/share/user/requestMyFollowingList", new ArrayList<Object>());
		}
		
		try {
			List<PublicUser> userList = publicUserFollowerService.getFollowerByUserId(userDetails.getUserId());
			List<String> dataList = new ArrayList<>();
			if(null != userList && userList.size() > 0) {
				for (Iterator<PublicUser> it = userList.iterator(); it.hasNext();) {
					PublicUser publicUser = it.next();
					if(null != publicUser) {
						dataList.add(publicUser.getId());
					}
				}
			}
			
			return toJson(0, bundle.getString("successful.search"), "get + api/share/user/requestMyFollowingList", dataList, false);
		} catch (Exception e) {
			e.printStackTrace();
			return toJson(2, bundle.getString("failed.execute"), "get + api/share/user/requestMyFollowingList", new ArrayList<Object>());
		}
	}
	
	/**
	 * 获取关注列表（详细信息）
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/api/share/user/requestMyFollowingListDetail", method = {RequestMethod.GET}, produces = {Const.PRODUCES_JSON})
	public String getFollowerListDetail(HttpServletRequest request) {
		UserDetails userDetails = SessionUtil.getUserDetails(request);

		PackBundle bundle = LangResource.getResourceBundle(request);

		if(null == userDetails) {
			return toJson(1, bundle.getString("user.not.login"), "get + api/share/user/requestMyFollowingListDetail", new ArrayList<Object>());
		}
		
		if (StringUtil.isEmpty(userDetails.getUserId())) {
			return toJson(1, bundle.getString("user.name.not.find"), "get + api/share/user/requestMyFollowingListDetail", new ArrayList<Object>());
		}
		
		try {
			List<PublicUser> userList = publicUserFollowerService.getFollowerByUserId(userDetails.getUserId());
			List<Message<UserContent>> dataList = new ArrayList<>();
			if(null != userList && userList.size() > 0) {
				for (Iterator<PublicUser> it = userList.iterator(); it.hasNext();) {
					PublicUser publicUser = it.next();
					
					if(null != publicUser) {

						Message<UserContent> msg = new Message<UserContent>();
						msg.setContentType(ContentType.CONTENT_CIRCLE_USERINFO_UPDATE.getValue());
						msg.setMessageType(MessageType.MESSAGE_CIRCLE_USERINFO.getValue());
						msg.setVersion(1);
						msg.setUpdateTime(new Date().getTime());
						msg.setSessionType(SessionType.SESSION_CIRCLE.getValue());
						msg.setSessionId(userDetails.getUserId());
						
						UserContent userContent = new UserContent();
						userContent.setUserId(Misc.getNotNullValue(publicUser.getId()));
						userContent.setUserName(publicUser.getUserName());
						userContent.setType(publicUser.getTypeId());
						userContent.setDescription(Misc.getNotNullValue(publicUser.getDescription()));
						
						if(StringUtil.isNotEmpty(publicUser.getAvatarUrl())) {
							userContent.setAvatarUrl(Misc.getServerUri(request, publicUser.getAvatarUrl()));
						}
						if(StringUtil.isNotEmpty(publicUser.getThumbAvatarUrl())) {
							userContent.setThumbAvatarUrl(Misc.getServerUri(request, publicUser.getThumbAvatarUrl()));
						}
						if(StringUtil.isNotEmpty(publicUser.getBackgroundImgUrl())) {
							userContent.setBackgroundImgUrl(Misc.getServerUri(request, publicUser.getBackgroundImgUrl()));
						}
						msg.setContent(userContent);
						
						dataList.add(msg);
					}
				}
			}
			
			return toJson(0, bundle.getString("successful.search"), "get + api/share/user/requestMyFollowingListDetail", dataList, false);
		} catch (Exception e) {
			e.printStackTrace();
			return toJson(2, bundle.getString("failed.execute"), "get + api/share/user/requestMyFollowingListDetail", new ArrayList<Object>());
		}
	}
	
	/**
	 * 获取关注用户信息
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/api/share/user/requestUserInfo", method = {RequestMethod.POST}, produces = {Const.PRODUCES_JSON})
	public String getUserInfo(@RequestBody CustomBean bean, HttpServletRequest request) {
		UserDetails userDetails = SessionUtil.getUserDetails(request);

		PackBundle bundle = LangResource.getResourceBundle(request);

		if(null == userDetails) {
			return toJson(1, bundle.getString("user.not.login"), "get + api/share/user/requestUserInfo", new ArrayList<Object>());
		}
		
		if(null == bean || null == bean.getUserIdList()) {
			return toJson(2, bundle.getString("need.param", ""), "get + api/share/user/requestUserInfo", new ArrayList<Object>());
		}
		
		if(StringUtil.isEmpty(userDetails.getUserId())) {
			return toJson(3, bundle.getString("user.name.not.find"), "get + api/share/user/requestUserInfo", new ArrayList<Object>());
		}
		
		try {
			List<PublicUser> userList = publicUserService.getUserInfoByUserIds(bean.getUserIdList());
			List<Message<UserContent>> dataList = new ArrayList<>();
			if(null != userList && userList.size() > 0) {
				for (Iterator<PublicUser> it = userList.iterator(); it.hasNext();) {
					PublicUser publicUser = it.next();
					
					if(null != publicUser) {
						Message<UserContent> msg = new Message<UserContent>();
						msg.setContentType(ContentType.CONTENT_CIRCLE_USERINFO_UPDATE.getValue());
						msg.setMessageType(MessageType.MESSAGE_CIRCLE_USERINFO.getValue());
						msg.setVersion(1);
						msg.setUpdateTime(new Date().getTime());
						msg.setSessionType(SessionType.SESSION_CIRCLE.getValue());
						msg.setSessionId(userDetails.getUserId());
						
						UserContent userContent = new UserContent();
						userContent.setUserId(Misc.getNotNullValue(publicUser.getId()));
						userContent.setUserName(publicUser.getUserName());
						userContent.setType(publicUser.getTypeId());
						userContent.setDescription(Misc.getNotNullValue(publicUser.getDescription()));
						
						if(StringUtil.isNotEmpty(publicUser.getAvatarUrl())) {
							userContent.setAvatarUrl(Misc.getServerUri(request, publicUser.getAvatarUrl()));
						}
						if(StringUtil.isNotEmpty(publicUser.getThumbAvatarUrl())) {
							userContent.setThumbAvatarUrl(Misc.getServerUri(request, publicUser.getThumbAvatarUrl()));
						}
						if(StringUtil.isNotEmpty(publicUser.getBackgroundImgUrl())) {
							userContent.setBackgroundImgUrl(Misc.getServerUri(request, publicUser.getBackgroundImgUrl()));
						}
						msg.setContent(userContent);
						
						dataList.add(msg);
					}
				}
			}
			
			return toJson(0, bundle.getString("successful.search"), "get + api/share/user/requestUserInfo", dataList);
		} catch (Exception e) {
			e.printStackTrace();
			return toJson(4, bundle.getString("failed.execute"), "get + api/share/user/requestUserInfo", new ArrayList<Object>());
		}
	}

	/**
	 * 拉黑共享号
	 * @param userId
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/api/share/user/block", method = {RequestMethod.POST}, produces = {Const.PRODUCES_JSON})
	public String blockPublicUser(String userId, HttpServletRequest request) {
		String apiName = "POST+/api/share/user/block";

		PackBundle bundle = LangResource.getResourceBundle(request);


		UserDetails userDetails = SessionUtil.getUserDetails(request);
		if(null == userDetails) {
			return toJson(1, bundle.getString("user.not.login"), apiName, new ArrayList<Object>());
		}

		if(StringUtil.isEmpty(userId)) {
			return toJson(2, bundle.getString("need.param", "userId"), apiName, new ArrayList<Object>());
		}

		if(StringUtil.isEmpty(userDetails.getUserId())) {
			return toJson(4, bundle.getString("user.name.not.find"), apiName, new ArrayList<Object>());
		}

		try {
			PublicUser blockUser = publicUserService.getByUserId(userId);
			if(null == blockUser) {
				return toJson(3, bundle.getString("user.dest.not.find"), apiName, new ArrayList<Object>());
			}

			int count = publicUserBlockService.blockUser(userDetails.getUserId(),userId);

			//可连续拉黑和取消拉黑
			if(count >= 0) {
				List<Message<UserContent>> jsonList = new ArrayList<>();
				Message<UserContent> msg = new Message<UserContent>();
				msg.setContentType(ContentType.CONTENT_CIRCLE_USERINFO_UPDATE.getValue());
				msg.setMessageType(MessageType.MESSAGE_CIRCLE_USERINFO.getValue());
				msg.setVersion(1);
				msg.setUpdateTime(new Date().getTime());
				msg.setSessionType(SessionType.SESSION_CIRCLE.getValue());
				msg.setSessionId(userDetails.getUserId());

				UserContent userContent = new UserContent();
				userContent.setUserId(Misc.getNotNullValue(blockUser.getId()));
				userContent.setUserName(blockUser.getUserName());
				userContent.setType(blockUser.getTypeId());
				userContent.setDescription(Misc.getNotNullValue(blockUser.getDescription()));

				if(StringUtil.isNotEmpty(blockUser.getAvatarUrl())) {
					userContent.setAvatarUrl(Misc.getServerUri(request, blockUser.getAvatarUrl()));
				}
				if(StringUtil.isNotEmpty(blockUser.getThumbAvatarUrl())) {
					userContent.setThumbAvatarUrl(Misc.getServerUri(request, blockUser.getThumbAvatarUrl()));
				}
				if(StringUtil.isNotEmpty(blockUser.getBackgroundImgUrl())) {
					userContent.setBackgroundImgUrl(Misc.getServerUri(request, blockUser.getBackgroundImgUrl()));
				}
				msg.setContent(userContent);
				jsonList.add(msg);

				return toJson(0, bundle.getString("successful.operation"), apiName, jsonList);
			}

			return toJson(0, bundle.getString("failed.black.done"), apiName, new ArrayList<Object>());
		} catch (Exception e) {
			e.printStackTrace();
			return toJson(5, bundle.getString("failed.execute"), apiName, new ArrayList<Object>());
		}
	}

	/**
	 * 取消拉黑共享号
	 * @param userId
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/api/share/user/unblock", method = {RequestMethod.POST}, produces = {Const.PRODUCES_JSON})
	public String unblockPublicUser(String userId, HttpServletRequest request) {
		String apiName = "POST+/api/share/user/unblock";

		PackBundle bundle = LangResource.getResourceBundle(request);

		UserDetails userDetails = SessionUtil.getUserDetails(request);
		if(null == userDetails) {
			return toJson(1, bundle.getString("user.not.login"), apiName, new ArrayList<>());
		}

		if(StringUtil.isEmpty(userId)) {
			return toJson(1, bundle.getString("need.param", "userId"), apiName, new ArrayList<>());
		}

		if(StringUtil.isEmpty(userDetails.getUserId())) {
			return toJson(5, bundle.getString("user.name.not.find"), apiName, new ArrayList<Object>());
		}

		try {
			PublicUser unFollowUser = publicUserService.getByUserId(userId);
			if(null == unFollowUser) {
				return toJson(3, bundle.getString(""), apiName, new ArrayList<>());
			}

			int count = publicUserBlockService.unblockUser(userDetails.getUserId(), userId);

			//可连续拉黑和取消拉黑
			if(count >= 0) {
				List<Message<UserContent>> jsonList = new ArrayList<>();
				Message<UserContent> msg = new Message<UserContent>();
				msg.setContentType(ContentType.CONTENT_CIRCLE_USERINFO_UPDATE.getValue());
				msg.setMessageType(MessageType.MESSAGE_CIRCLE_USERINFO.getValue());
				msg.setVersion(1);
				msg.setUpdateTime(new Date().getTime());
				msg.setSessionType(SessionType.SESSION_CIRCLE.getValue());
				msg.setSessionId(userDetails.getUserId());

				UserContent userContent = new UserContent();
				userContent.setUserId(Misc.getNotNullValue(unFollowUser.getId()));
				userContent.setUserName(unFollowUser.getUserName());
				userContent.setType(unFollowUser.getTypeId());
				userContent.setDescription(Misc.getNotNullValue(unFollowUser.getDescription()));

				if(StringUtil.isNotEmpty(unFollowUser.getAvatarUrl())) {
					userContent.setAvatarUrl(Misc.getServerUri(request, unFollowUser.getAvatarUrl()));
				}
				if(StringUtil.isNotEmpty(unFollowUser.getThumbAvatarUrl())) {
					userContent.setThumbAvatarUrl(Misc.getServerUri(request, unFollowUser.getThumbAvatarUrl()));
				}
				if(StringUtil.isNotEmpty(unFollowUser.getBackgroundImgUrl())) {
					userContent.setBackgroundImgUrl(Misc.getServerUri(request, unFollowUser.getBackgroundImgUrl()));
				}
				msg.setContent(userContent);
				jsonList.add(msg);

				return toJson(0, bundle.getString("successful.operation"), apiName, jsonList);
			}

			return toJson(4, bundle.getString("failed.operation"), apiName, new ArrayList<>());
		} catch (Exception e) {
			e.printStackTrace();
			return toJson(2, bundle.getString("failed.execute"), apiName, new ArrayList<>());
		}
	}


	/**
	 * 获取拉黑列表，只返回拉黑用户的 userId
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/api/share/user/requestMyBlockingList", method = {RequestMethod.GET}, produces = {Const.PRODUCES_JSON})
	public String getBLockList(HttpServletRequest request) {
		String apiName = "get + api/share/user/requestMyBlockingList";

		PackBundle bundle = LangResource.getResourceBundle(request);

		UserDetails userDetails = SessionUtil.getUserDetails(request);
		if(null == userDetails) {
			return toJson(1, bundle.getString("user.not.login"), apiName, new ArrayList<Object>());
		}

		if(StringUtil.isEmpty(userDetails.getUserId())) {
			return toJson(3, bundle.getString("need.param", "userId"), apiName, new ArrayList<Object>());
		}

		try {
			List<PublicUser> userList = publicUserBlockService.getBlockByUserId(userDetails.getUserId());
			List<String> dataList = new ArrayList<>();
			if(null != userList && userList.size() > 0) {
				for (Iterator<PublicUser> it = userList.iterator(); it.hasNext();) {
					PublicUser publicUser = it.next();
					if(null != publicUser) {
						dataList.add(publicUser.getId());
					}
				}
			}

			return toJson(0, bundle.getString("successful.search"), apiName, dataList, false);
		} catch (Exception e) {
			e.printStackTrace();
			return toJson(2, bundle.getString("failed.execute"), apiName, new ArrayList<Object>());
		}
	}

	/**
	 * 获取拉黑列表（详细信息）
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/api/share/user/requestMyBlockingListDetail", method = {RequestMethod.GET}, produces = {Const.PRODUCES_JSON})
	public String getBlockListDetail(HttpServletRequest request) {
		UserDetails userDetails = SessionUtil.getUserDetails(request);

		PackBundle bundle = LangResource.getResourceBundle(request);

		if(null == userDetails) {
			return toJson(1, bundle.getString("user.not.login"), "get + api/share/user/requestMyFollowingListDetail", new ArrayList<Object>());
		}

		if (StringUtil.isEmpty(userDetails.getUserId())) {
			return toJson(1, bundle.getString("user.name.not.find"), "get + api/share/user/requestMyFollowingListDetail", new ArrayList<Object>());
		}

		try {
			List<PublicUser> userList = publicUserBlockService.getBlockByUserId(userDetails.getUserId());
			List<Message<UserContent>> dataList = new ArrayList<>();
			if(null != userList && userList.size() > 0) {
				for (Iterator<PublicUser> it = userList.iterator(); it.hasNext();) {
					PublicUser publicUser = it.next();

					if(null != publicUser) {

						Message<UserContent> msg = new Message<UserContent>();
						msg.setContentType(ContentType.CONTENT_CIRCLE_USERINFO_UPDATE.getValue());
						msg.setMessageType(MessageType.MESSAGE_CIRCLE_USERINFO.getValue());
						msg.setVersion(1);
						msg.setUpdateTime(new Date().getTime());
						msg.setSessionType(SessionType.SESSION_CIRCLE.getValue());
						msg.setSessionId(userDetails.getUserId());

						UserContent userContent = new UserContent();
						userContent.setUserId(Misc.getNotNullValue(publicUser.getId()));
						userContent.setUserName(publicUser.getUserName());
						userContent.setType(publicUser.getTypeId());
						userContent.setDescription(Misc.getNotNullValue(publicUser.getDescription()));

						if(StringUtil.isNotEmpty(publicUser.getAvatarUrl())) {
							userContent.setAvatarUrl(Misc.getServerUri(request, publicUser.getAvatarUrl()));
						}
						if(StringUtil.isNotEmpty(publicUser.getThumbAvatarUrl())) {
							userContent.setThumbAvatarUrl(Misc.getServerUri(request, publicUser.getThumbAvatarUrl()));
						}
						if(StringUtil.isNotEmpty(publicUser.getBackgroundImgUrl())) {
							userContent.setBackgroundImgUrl(Misc.getServerUri(request, publicUser.getBackgroundImgUrl()));
						}
						msg.setContent(userContent);

						dataList.add(msg);
					}
				}
			}

			return toJson(0, bundle.getString("successful.search"), "get + api/share/user/requestMyFollowingListDetail", dataList, false);
		} catch (Exception e) {
			e.printStackTrace();
			return toJson(2, bundle.getString("failed.execute"), "get + api/share/user/requestMyFollowingListDetail", new ArrayList<Object>());
		}
	}


	/**
	 * 获取当前共享号的应用列表
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/api/share/user/getCurrentUserAppList", method = {RequestMethod.GET}, produces = {Const.PRODUCES_JSON})
	public String getCurrentUserAppList(HttpServletRequest request) {

		PackBundle bundle = LangResource.getResourceBundle(request);

		UserDetails userDetails = SessionUtil.getUserDetails(request);
		if(null == userDetails) {
			return toJson(1, bundle.getString("user.not.login"), "get + api/share/user/getCurrentUserAppList", new ArrayList<Object>());
		}
		
		if (StringUtil.isEmpty(userDetails.getUserId())) {
			return toJson(1, bundle.getString("user.name.not.find"), "get + api/share/user/getCurrentUserAppList", new ArrayList<Object>());
		}
		
		try {
			logger.info("bundle=" + JSON.toJSONString(bundle));
			return toJson(0, bundle.getString("successful.search"), "get + api/share/user/getCurrentUserAppList", thirdAppService.getByUserId(userDetails.getUserId()));
		} catch (Exception e) {
			e.printStackTrace();
			return toJson(2, bundle.getString("failed.execute"), "get + api/share/user/getCurrentUserAppList", new ArrayList<Object>());
		}
	}

	/**
	 * 获取当前共享号的应用列表
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/api/share/user/getCurrentUserAppListNew", method = {RequestMethod.GET}, produces = {Const.PRODUCES_JSON})
	public String getCurrentUserAppListNew(HttpServletRequest request) {

		PackBundle bundle = LangResource.getResourceBundle(request);

		UserDetails userDetails = SessionUtil.getUserDetails(request);
		if(null == userDetails) {
			return toJson(1, bundle.getString("user.not.login"), "get + api/share/user/getCurrentUserAppList", new ArrayList<Object>());
		}

		if (StringUtil.isEmpty(userDetails.getUserId())) {
			return toJson(1, bundle.getString("user.name.not.find"), "get + api/share/user/getCurrentUserAppList", new ArrayList<Object>());
		}

		try {
			logger.info("bundle=" + JSON.toJSONString(bundle));
			List<ThirdApp> thirdAppList = thirdAppService.getByUserId(userDetails.getUserId());
			if(thirdAppList != null && !thirdAppList.isEmpty()) {
				for(int i = 0; i < thirdAppList.size(); i++) {
					ThirdApp thirdApp = thirdAppList.get(i);
					String appUrl = thirdApp.getAppUrl();
					if(StringUtils.isNotEmpty(appUrl) && StringUtils.contains(appUrl, TAG)) {
						thirdApp.setAppUrl(appUrl.substring(0, appUrl.lastIndexOf(TAG)));
					}
				}
			}
			return toJson(0, bundle.getString("successful.search"), "get + api/share/user/getCurrentUserAppList", thirdAppList);
		} catch (Exception e) {
			e.printStackTrace();
			return toJson(2, bundle.getString("failed.execute"), "get + api/share/user/getCurrentUserAppList", new ArrayList<>());
		}
	}
	
	@ResponseBody
	@RequestMapping(value = "/api/circle/CircleStat", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	public String getCircleStat(@RequestParam("circle_id") String userId, HttpServletRequest request) {
		Map<String, Object> jsonMap = new HashMap<String, Object>();

		PackBundle bundle = LangResource.getResourceBundle(request);
		
		try {
			PublicUser user = publicUserService.getByUserId(userId);
			Map<String,Long> statMap = publicUserService.statUserInfo(userId);
			if(null == user || null == statMap) {
				jsonMap.put("ErrorCode", 1);
				jsonMap.put("Status", false);
				jsonMap.put("ErrorMsg", bundle.getString("user.name.not.find"));
				return JSON.toJSONString(jsonMap);
			}
			
			jsonMap.put("ErrorCode", 0);
			jsonMap.put("Status", true);
			jsonMap.put("ErrorMsg", "");
			jsonMap.put("PublicCircleID", user.getId());
			jsonMap.put("PublicCircleName", user.getUserName());
			jsonMap.put("PublicCircleDescription", user.getDescription());
			jsonMap.put("TotalReadCount", statMap.get("read"));
			jsonMap.put("TotalCommentCount", statMap.get("replay"));
			jsonMap.put("TotalUpCount", statMap.get("thumbUp"));
			jsonMap.put("TotalDownCount", statMap.get("thumbDown"));
			jsonMap.put("TotalShareCount", statMap.get("shareCount"));
			jsonMap.put("TotalFollowCount", statMap.get("like"));
			jsonMap.put("TotalArticleCount", statMap.get("article"));
			jsonMap.put("uuid", user.getUuid());
			jsonMap.put("Version", user.getVersion());
			if(StringUtil.isNotEmpty(user.getAvatarUrl())) {
				jsonMap.put("PublicCircleImg", Misc.getServerUri(request, user.getAvatarUrl()));
			}
			
			if(StringUtil.isNotEmpty(user.getThumbAvatarUrl())) {
				jsonMap.put("ThumbAvatarUrl", Misc.getServerUri(request, user.getThumbAvatarUrl()));
			}
			jsonMap.put("Description", Misc.getNotNullValue(user.getDescription()));
			
			jsonMap.put("BackgroundImgUrl", "");
			if(StringUtil.isNotEmpty(user.getBackgroundImgUrl())) {
				jsonMap.put("BackgroundImgUrl", Misc.getServerUri(request, user.getBackgroundImgUrl()));
			}
			
			jsonMap.put("ErrorCode", 0);
			jsonMap.put("Status", "0");
			jsonMap.put("ErrorMsg", "");
			jsonMap.put("PublicCircleId", user.getId());
			
			return JSON.toJSONString(jsonMap);
		} catch (Exception e) {
			e.printStackTrace();
			jsonMap.put("ErrorCode", 1);
			jsonMap.put("Status", false);
			jsonMap.put("ErrorMsg", bundle.getString("failed.execute"));
			return JSON.toJSONString(jsonMap);
		}
		
	}
	
	
	@ResponseBody
	@RequestMapping(value = "/api/share/user/search", method = {RequestMethod.GET}, produces = {Const.PRODUCES_JSON})
	public String searchUser(String key, Integer start, String tag, HttpServletRequest request) {
		UserDetails userDetails = SessionUtil.getUserDetails(request);

		PackBundle bundle = LangResource.getResourceBundle(request);

		if(null == userDetails) {
			return toJson(1, bundle.getString("user.not.login"), "get + /api/share/user/search", new ArrayList<>(), false);
		}
		
		if(StringUtil.isEmpty(key)) {
			return toJson(3, bundle.getString("need.search.param"), "get + /api/share/user/search", new ArrayList<>(), false);
		}

		//对key值做去空处理
		key = key.trim();
		
		if(start == null) {
			start = 0;
		}
		
		UserBodyInfo<UserContent> cache = null;
		
		if(StringUtil.isEmpty(tag)) {
			tag = Const.USER_SEARCH_TAG + new Date().getTime();
			CacheUtil.evict(Const.USER_SEARCH_KEY + userDetails.getUuid());
			
		}else {
			
			cache = CacheUtil.getHalfHour(Const.USER_SEARCH_KEY + userDetails.getUuid());
			if(null == cache || !tag.equals(cache.getTag())) {
				cache = null;
			}
		}
		
		try {
			if(null == cache) {
				cache = new UserBodyInfo<UserContent>();
				List<UserContent> contents = new ArrayList<>();
				List<PublicUser> dataList = publicUserService.searchForUserList(key);
				if(null != dataList && dataList.size() > 0) {
					for(int i = 0; i < dataList.size(); i++){
						PublicUser user = dataList.get(i);
						if(null != user) {
							UserContent userContent = new UserContent();
							userContent.setUserId(user.getId());
							userContent.setUserName(user.getUserName());
							userContent.setIndex(i + 1);
							userContent.setAvatarUrl(Misc.getServerUri(request, user.getAvatarUrl()));
							userContent.setThumbAvatarUrl(Misc.getServerUri(request, user.getThumbAvatarUrl()));
							userContent.setDescription(user.getDescription());
							userContent.setThumbAvatarUrl(Misc.getServerUri(request, user.getBackgroundImgUrl()));
							userContent.setType(user.getTypeId());
							contents.add(userContent);
						}
					}
				}
				
				cache.setStart(start);
				cache.setTag(tag);
				cache.setList(contents);
				CacheUtil.putHalfHour(Const.USER_SEARCH_KEY + userDetails.getUuid(), cache);
			}else {
				cache.setStart(start);
				cache.setTag(tag);
			}
			
			Map<String, Object> extData = new HashMap<>();
			extData.put("hasMore", cache.getHasMore());
			extData.put("tag", cache.getTag());
			return toJson(0, bundle.getString("successful.search"), "get + /api/share/user/search", cache.getDataList(), extData);
		} catch (Exception e) {
			e.printStackTrace();
			return toJson(4, bundle.getString("failed.execute"), "get + /api/share/user/search", new ArrayList<>(), false);
		}
		
	}
	
	@ResponseBody
	@RequestMapping(value = "/api/share/user/searchMobile", method = {RequestMethod.GET}, produces = {Const.PRODUCES_JSON})
	public String searchUserByMobile(String key, Integer start, String tag, HttpServletRequest request) {

		PackBundle bundle = LangResource.getResourceBundle(request);

		String apiName = "get + /api/share/user/mobile/search";
		UserDetails userDetails = SessionUtil.getUserDetails(request);
		if(null == userDetails) {
			return toJson(1, bundle.getString("user.not.login"), apiName, new ArrayList<>(), false);
		}
		
		if(StringUtil.isEmpty(key)) {
			return toJson(3, bundle.getString("need.search.param"), apiName, new ArrayList<>(), false);
		}

		//去掉key中的空格
		key = key.trim();
		
		if(!NumberUtil.isMobile(key)) {
			return toJson(0, bundle.getString("successful.search"), apiName, new ArrayList<>(), false);
		}
		
		if(start == null) {
			start = 0;
		}
		
		UserBodyInfo<UserContent> cache = null;
		
		if(StringUtil.isEmpty(tag)) {
			tag = Const.USER_SEARCH_TAG + new Date().getTime();
			CacheUtil.evict(Const.USER_SEARCH_KEY + userDetails.getUuid());
			
		}else {
			
			cache = CacheUtil.getHalfHour(Const.USER_SEARCH_KEY + userDetails.getUuid());
			if(null == cache || !tag.equals(cache.getTag())) {
				cache = null;
			}
		}
		
		try {
			if(null == cache) {
				cache = new UserBodyInfo<UserContent>();
				List<UserContent> contents = new ArrayList<>();
				List<PublicUser> dataList = publicUserService.searchForUserListByMobile(key);
				if(null == dataList || dataList.size() <= 0) {
					
					PublicUser user = new PublicUser();
					
					user.setUserName(key + "（" + bundle.getString("user.not.register") + "）");
					user.setVersion(2);
					user.setStatus("1");
					user.setTypeId(0);
					user.setAvatarUrl("resources/images/user/user_log_b.png");
					user.setThumbAvatarUrl("resources/images/user/user_logo_s.png");
					user.setBackgroundImgUrl("resources/images/user/user_bg.png");
					user.setCreateTime(new Timestamp(new Date().getTime()));
					user.setMobile(key);
					publicUserService.save(user);
					
					dataList = new ArrayList<>();
					dataList.add(user);
				}
				
				for(int i = 0; i < dataList.size(); i++){
					PublicUser user = dataList.get(i);
					if(null != user) {
						UserContent userContent = new UserContent();
						userContent.setUserId(user.getId());
						userContent.setUserName(user.getUserName());
						userContent.setIndex(i + 1);
						userContent.setAvatarUrl(Misc.getServerUri(request, user.getAvatarUrl()));
						userContent.setThumbAvatarUrl(Misc.getServerUri(request, user.getThumbAvatarUrl()));
						userContent.setDescription(user.getDescription());
						userContent.setThumbAvatarUrl(Misc.getServerUri(request, user.getBackgroundImgUrl()));
						userContent.setType(user.getTypeId());
						userContent.setUuid(Misc.getNotNullValue(user.getUuid()));
						contents.add(userContent);
					}
				}
				
				cache.setStart(start);
				cache.setTag(tag);
				cache.setList(contents);
				CacheUtil.putHalfHour(Const.USER_SEARCH_KEY + userDetails.getUuid(), cache);
			}else {
				cache.setStart(start);
				cache.setTag(tag);
			}
			
			Map<String, Object> extData = new HashMap<>();
			extData.put("hasMore", cache.getHasMore());
			extData.put("tag", cache.getTag());
			cache.setPageSize(20);
			return toJson(0, bundle.getString("successful.search"), apiName, cache.getDataList(), extData);
		} catch (Exception e) {
			e.printStackTrace();
			return toJson(4, bundle.getString("failed.execute"), apiName, new ArrayList<>(), false);
		}
		
	}
	
	/**
	 * 如果查询关键字是手机号按照手机号规则查询，如果不是按照共享号名称查询
	 */
	@ResponseBody
	@RequestMapping(value = "/api/share/user/searchUserName", method = {RequestMethod.GET}, produces = {Const.PRODUCES_JSON})
	public String searchUserName(String key, Integer start, String tag, HttpServletRequest request) {

		PackBundle bundle = LangResource.getResourceBundle(request);

		String apiName = "get + /api/share/user/mobile/searchUserName";
		UserDetails userDetails = SessionUtil.getUserDetails(request);
		if(null == userDetails) {
			return toJson(1, bundle.getString("user.not.login"), apiName, new ArrayList<>(), false);
		}
		
		if(StringUtil.isEmpty(key)) {
			return toJson(3, bundle.getString("need.search.param"), apiName, new ArrayList<>(), false);
		}
		
		if(start == null) {
			start = 0;
		}
		
		UserBodyInfo<UserContent> cache = null;
		
		if(StringUtil.isEmpty(tag)) {
			tag = Const.USER_SEARCH_TAG + new Date().getTime();
			CacheUtil.evict(Const.USER_SEARCH_KEY + userDetails.getUuid());
			
		}else {
			
			cache = CacheUtil.getHalfHour(Const.USER_SEARCH_KEY + userDetails.getUuid());
			if(null == cache || !tag.equals(cache.getTag())) {
				cache = null;
			}
		}
		
		try {
			if(null == cache) {
				cache = new UserBodyInfo<UserContent>();
				List<UserContent> contents = new ArrayList<>();
				
				List<PublicUser> dataList = new ArrayList<>();
				String mobile = userDetails.getMobile();
				GlobleMobile globleMobile = new GlobleMobile(mobile);
				PhoneUtil phoneUtil = PhoneUtil.getInstance();
				if(phoneUtil.checkPhoneNumberByCountryCode(key, globleMobile.getMobilePrefix())) {
					dataList = publicUserService.searchForUserListByMobile(key);
					if(null == dataList || dataList.size() <= 0) {
						PublicUser user = new PublicUser();
						user.setUserName(key);
						user.setVersion(2);
						user.setStatus("1");
						user.setTypeId(0);
						user.setAvatarUrl("resources/images/user/user_log_b.png");
						user.setThumbAvatarUrl("resources/images/user/user_logo_s.png");
						user.setBackgroundImgUrl("resources/images/user/user_bg.png");
						user.setCreateTime(new Timestamp(new Date().getTime()));
						user.setMobile(key);
						user.setMobilePrefix(globleMobile.getMobilePrefix());
						publicUserService.save(user);
						user.setUserName(key + "（"+ bundle.getString("user.not.register")+ "）");
						dataList = new ArrayList<>();
						dataList.add(user);

					}
				}else {
					dataList = publicUserService.searchForUserList(key);
				}
				
				for(int i = 0; i < dataList.size(); i++){
					PublicUser user = dataList.get(i);
					if(null != user) {
						UserContent userContent = new UserContent();
						userContent.setUserId(user.getId());
						userContent.setUserName(user.getUserName());
						userContent.setIndex(i + 1);
						userContent.setAvatarUrl(Misc.getServerUri(request, user.getAvatarUrl()));
						userContent.setThumbAvatarUrl(Misc.getServerUri(request, user.getThumbAvatarUrl()));
						userContent.setDescription(user.getDescription());
						userContent.setThumbAvatarUrl(Misc.getServerUri(request, user.getBackgroundImgUrl()));
						userContent.setType(user.getTypeId());
						userContent.setUuid(Misc.getNotNullValue(user.getUuid()));
						contents.add(userContent);
					}
				}
				cache.setStart(start);
				cache.setTag(tag);
				cache.setList(contents);
				CacheUtil.putHalfHour(Const.USER_SEARCH_KEY + userDetails.getUuid(), cache);
			}else {
				cache.setStart(start);
				cache.setTag(tag);
			}
			
			Map<String, Object> extData = new HashMap<>();
			extData.put("hasMore", cache.getHasMore());
			extData.put("tag", cache.getTag());
			cache.setPageSize(20);
			return toJson(0, bundle.getString("successful.search"), apiName, cache.getDataList(), extData);
		} catch (Exception e) {
			e.printStackTrace();
			return toJson(4, bundle.getString("failed.execute"), apiName, new ArrayList<>(), false);
		}
		
	}
	
	/*===========================================旧接口==================================================*/
	
	@ResponseBody
	@RequestMapping(value = "/MessagePush/api/createPublicCircle", produces = {Const.PRODUCES})
	public String addPublicUser(@RequestParam(value = "PublicCircleName", required = true) String userName, @RequestParam(value = "Description") String description,
			@RequestParam(value = "uuid") String uuid, MultipartFile PublicCirclePic, HttpServletRequest request) {

		PackBundle bundle = LangResource.getResourceBundle(request);

		UserDetails userDetails = SessionUtil.getUserDetails(request);
		if(null == userDetails) {
			return toJson("1", bundle.getString("user.not.login"), null);
		}
		
		if (Misc.regexShareName(userName)) {
			return toJson("2", bundle.getString("user.name.content"), null);
		}

		PublicUser psBean = publicUserService.getByUserName(userName);
		if (null != psBean) {
			return toJson("3", bundle.getString("user.name.found"), null);
		}
		
		PublicUser user = new PublicUser();
		try {
			
			if(!FileUtil.multipartFileIsNull(PublicCirclePic)) {
				FileBean logo = FileUtil.saveFile(request, false, "files/images", PublicCirclePic, ".PNG|.JPG|.GIF|.JPEG|.BMP");
				user.setAvatarUrl(logo.getRelativePath());
				FileBean ThumbnailFile = FileUtil.getThumbnail(request, false, "files/images/thumbnail", PublicCirclePic, ".PNG|.JPG|.GIF|.JPEG|.BMP");
				ImageUtil.zoomImage(logo.getFile(), ThumbnailFile.getFile(), 0.5);
				user.setThumbAvatarUrl(ThumbnailFile.getRelativePath());
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			return toJson("4", bundle.getString("failed.execute"), null);
		}

		try {
			user.setVersion(2);
			user.setStatus("1");
			user.setTypeId(0);
			user.setUserName(userName);
			user.setUuid(userDetails.getUuid());
			user.setCreateTime(new Timestamp(new Date().getTime()));
			user.setDescription(description);
			publicUserService.save(user);
			
			List<Map<String, Object>> jsonList = new ArrayList<Map<String, Object>>();
			
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("PublicCircleId", user.getId());
			data.put("PublicCircleName", userName);
			data.put("Description", description);
			if(StringUtil.isNotEmpty(user.getAvatarUrl())) {
				data.put("PublicCirclePic", (Misc.getServerUri(request) + user.getAvatarUrl()).replace("//", "/"));
			}
			
			if(StringUtil.isNotEmpty(user.getThumbAvatarUrl())) {
				data.put("ThumbAvatarUrl", (Misc.getServerUri(request) + user.getThumbAvatarUrl()).replace("//", "/"));
			}
			data.put("uuid", userDetails.getUuid());
			data.put("Version", user.getVersion());
			data.put("PublicCircleAdminid", "-1");
			jsonList.add(data);
			return toJson("0", bundle.getString("successful.operation"), jsonList);
		} catch (Exception e) {
			e.printStackTrace();
			return toJson("4", bundle.getString("failed.execute") + e.getMessage(), null);
		}
		
	}
	
	@ResponseBody
	@RequestMapping(value = "/MessagePush/api/updatePublicCircle", produces = {Const.PRODUCES})
	public String updatePublicUser(@RequestParam(value = "uuid") String uuid, @RequestParam(value = "PublicCirclePic") MultipartFile logoFile, @RequestParam(value = "PublicCircleId", required = true) String userId,
			HttpServletRequest request) {

		PackBundle bundle = LangResource.getResourceBundle(request);
		
		if (StringUtil.isEmpty(userId)) {
			return toJson("2", bundle.getString("need.param", "userId"), null);
		}
		
		PublicUser user = publicUserService.getByStrictId(uuid, userId);
		if(null == user) {
			return toJson("3", bundle.getString("user.name.not.find"), null);
		}
		
		try {
			
			if(!FileUtil.multipartFileIsNull(logoFile)) {
				FileBean logo = FileUtil.saveFile(request, false, "files/images", logoFile, ".PNG|.JPG|.GIF|.JPEG|.BMP");
				user.setAvatarUrl(logo.getRelativePath());
				FileBean ThumbnailFile = FileUtil.getThumbnail(request, false, "files/images/thumbnail", logoFile, ".PNG|.JPG|.GIF|.JPEG|.BMP");
				ImageUtil.zoomImage(logo.getFile(), ThumbnailFile.getFile(), 0.5);
				user.setThumbAvatarUrl(ThumbnailFile.getRelativePath());
				
				publicUserService.update(user);
			}
			
			Map<String, Object> jsonMap = new HashMap<String, Object>();
			jsonMap.put("Status", "0");
			jsonMap.put("errcode", "");
			jsonMap.put("datacode", user.getId());
			return toJson("0", bundle.getString("successful.modify"), jsonMap);
			
		} catch (Exception e) {
			e.printStackTrace();
			return toJson("4", bundle.getString("failed.execute"), null);
		}

	}
	
	@ResponseBody
	@RequestMapping(value = "/MessagePush/api/getPublicCircleList", produces = {Const.PRODUCES})
	public String getPublicShareNums(@RequestParam("uuid") String uuid, @RequestParam String sign,HttpServletRequest request) {

		PackBundle bundle = LangResource.getResourceBundle(request);

		List<Map<String,Object>> jsonList = new ArrayList<Map<String,Object>>();
		if(StringUtil.isEmpty(uuid)) {
			return toJson("1", bundle.getString("need.param", "uuid"), jsonList);
		}
		try {
			List<PublicUser> userList = publicUserService.getByUuid(uuid);
			if(null != userList && userList.size() > 0) {
				for(PublicUser user : userList) {
					Map<String,Object> jsonMap = new HashMap<String,Object>();
					
					jsonMap.put("PublicCircleId", user.getId());
					jsonMap.put("PublicCircleName", user.getUserName());
					jsonMap.put("Description", user.getDescription());
					jsonMap.put("uuid", user.getUuid());
					jsonMap.put("Version", user.getVersion());
					if(StringUtil.isNotEmpty(user.getAvatarUrl())) {
						jsonMap.put("PublicCirclePic", Misc.getServerUri(request, user.getAvatarUrl()));
					}
					
					if(StringUtil.isNotEmpty(user.getThumbAvatarUrl())) {
						jsonMap.put("ThumbAvatarUrl", Misc.getServerUri(request, user.getThumbAvatarUrl()));
					}
					jsonMap.put("Description", user.getDescription());
					
					if(StringUtil.isNotEmpty(user.getBackgroundImgUrl())) {
						jsonMap.put("backgroundImgUrl", Misc.getServerUri(request, user.getBackgroundImgUrl()));
					}
					jsonList.add(jsonMap);
				}
			}
			
			return toJson("0", bundle.getString("successful.search"), jsonList);
		} catch (Exception e) {
			e.printStackTrace();
			return toJson("2", bundle.getString("failed.execute"), jsonList);
		}
	}


	@ResponseBody
	@RequestMapping(value = "/api/user/getAllInfos", method = RequestMethod.POST, produces = {Const.PRODUCES_JSON})
	public DeferredResult<String> getAllInfos(@RequestBody UserDto userDto, HttpServletRequest request) {
		//根据request 请求获取访问用户本地语言
		PackBundle bundle = LangResource.getResourceBundle(request);
		//api名称
		String apiName = "post + /api/user/getAllInfos";
		//获取当前登录用户信息
		UserDetails userDetails = SessionUtil.getUserDetails(request);
		if(userDetails == null) {
			DeferredResult<String> dr = new DeferredResult<>();
			dr.setResult(toJson(EnumStatus.NINETY_EIGHT.getIndex(), bundle.getString("user.not.login"), apiName, null));
			return dr;
		}
		try {
			//当前登录用户id
			String userId = userDetails.getUserId();
			userDto.setCurrentUserId(userId);
			DeferredResult<String> dr = new DeferredResult<>();
			BaseResult<Map<String,Object>> resultData = publicUserService.getUserInfos(userDto,EnumStatus.ZORO.getIndex());
//			resultData.setApiName(apiName);
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
	@RequestMapping(value = "/api/user/getGroupInfos", method = RequestMethod.POST, produces = {Const.PRODUCES_JSON})
	public DeferredResult<String> getGroupInfos(@RequestBody UserDto userDto, HttpServletRequest request) {
		//根据request 请求获取访问用户本地语言
		PackBundle bundle = LangResource.getResourceBundle(request);
		//api名称
		String apiName = "post + /api/user/getGroupInfos";
		//获取当前登录用户信息
		UserDetails userDetails = SessionUtil.getUserDetails(request);
		if(userDetails == null) {
			DeferredResult<String> dr = new DeferredResult<>();
			dr.setResult(toJson(EnumStatus.NINETY_EIGHT.getIndex(), bundle.getString("user.not.login"), apiName, null));
			return dr;
		}
		try {
			//当前登录用户id
			String userId = userDetails.getUserId();
			userDto.setCurrentUserId(userId);
			DeferredResult<String> dr = new DeferredResult<>();
			BaseResult<Map<String,Object>> resultData = publicUserService.getUserInfos(userDto,EnumStatus.ONE.getIndex());
//			resultData.setApiName(apiName);
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
	@RequestMapping(value = "/api/user/getUserInfos", method = RequestMethod.POST, produces = {Const.PRODUCES_JSON})
	public DeferredResult<String> getUserInfos(@RequestBody UserDto userDto, HttpServletRequest request) {
		//根据request 请求获取访问用户本地语言
		PackBundle bundle = LangResource.getResourceBundle(request);
		//api名称
		String apiName = "post + /api/user/getUserInfos";
		//获取当前登录用户信息
		UserDetails userDetails = SessionUtil.getUserDetails(request);
		if(userDetails == null) {
			DeferredResult<String> dr = new DeferredResult<>();
			dr.setResult(toJson(EnumStatus.NINETY_EIGHT.getIndex(), bundle.getString("user.not.login"), apiName, null));
			return dr;
		}
		try {
			//当前登录用户id
			String userId = userDetails.getUserId();
			userDto.setCurrentUserId(userId);
			DeferredResult<String> dr = new DeferredResult<>();
			BaseResult<Map<String,Object>> resultData = publicUserService.getUserInfos(userDto,EnumStatus.TWO.getIndex());
//			resultData.setApiName(apiName);
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
	@RequestMapping(value = "/api/user/getGroupUsers", method = RequestMethod.POST, produces = {Const.PRODUCES_JSON})
	public DeferredResult<String> getGroupUsers(@RequestBody UserDto userDto, HttpServletRequest request) {
		//根据request 请求获取访问用户本地语言
		PackBundle bundle = LangResource.getResourceBundle(request);
		//api名称
		String apiName = "post + /api/user/getGroupUsers";
		//获取当前登录用户信息
		UserDetails userDetails = SessionUtil.getUserDetails(request);
		if(userDetails == null) {
			DeferredResult<String> dr = new DeferredResult<>();
			dr.setResult(toJson(EnumStatus.NINETY_EIGHT.getIndex(), bundle.getString("user.not.login"), apiName, null));
			return dr;
		}
		//群id不能为空
		if(StringUtil.isEmpty(userDto.getGroupId())) {
			DeferredResult<String> dr = new DeferredResult<>();
			dr.setResult(toJson(EnumStatus.ONE.getIndex(), bundle.getString("need.param", "groupId"), apiName,null));
			return dr;
		}
		//用户ids不能为空
		if(userDto.getUserIds() ==  null || userDto.getUserIds().length == 0) {
			DeferredResult<String> dr = new DeferredResult<>();
			dr.setResult(toJson(EnumStatus.ONE.getIndex(), bundle.getString("need.param", "userIds"), apiName,null));
			return dr;
		}
		try {
			//当前登录用户id
			String userId = userDetails.getUserId();
			userDto.setCurrentUserId(userId);
			DeferredResult<String> dr = new DeferredResult<>();
			BaseResult<Map<String,Object>> resultData = publicUserService.getGroupUsers(userDto);
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
