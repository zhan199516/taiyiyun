package com.taiyiyun.passport.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.taiyiyun.passport.bean.UserCache;
import com.taiyiyun.passport.bean.UserDetails;
import com.taiyiyun.passport.consts.Config;
import com.taiyiyun.passport.consts.Const;
import com.taiyiyun.passport.consts.EnumStatus;
import com.taiyiyun.passport.dao.IPublicInvitationConfDao;
import com.taiyiyun.passport.dao.IPublicUserDao;
import com.taiyiyun.passport.language.LangResource;
import com.taiyiyun.passport.language.PackBundle;
import com.taiyiyun.passport.language.ThirdTranslate;
import com.taiyiyun.passport.mqtt.Message;
import com.taiyiyun.passport.mqtt.Message.ContentType;
import com.taiyiyun.passport.mqtt.Message.MessageType;
import com.taiyiyun.passport.mqtt.Message.SessionType;
import com.taiyiyun.passport.po.*;
import com.taiyiyun.passport.po.circle.UserContent;
import com.taiyiyun.passport.service.IPublicInvitationService;
import com.taiyiyun.passport.service.IPublicUserService;
import com.taiyiyun.passport.service.ISharePointService;
import com.taiyiyun.passport.service.queue.Consumer;
import com.taiyiyun.passport.service.session.LoginInfo;
import com.taiyiyun.passport.sqlserver.dao.IEntityDao;
import com.taiyiyun.passport.sqlserver.po.Entity;
import com.taiyiyun.passport.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@CrossOrigin(origins = "*", maxAge = 7600)
@Controller
@RequestMapping("/api/invitation")
public class ApiInvitationController extends BaseController {
	public final Logger logger = LoggerFactory.getLogger(getClass());
	
	static final Consumer consumer = new Consumer();
	static final String appKey = "1A051FEAA0A0451E8D2112AF2A24716C";
	static final int min =Runtime.getRuntime().availableProcessors()*10;
	static final int max =Runtime.getRuntime().availableProcessors()*40;
	static ExecutorService executorService=new ThreadPoolExecutor(min, max, 2L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());  

	@Resource
	private IPublicInvitationService publicInvitationService;
	@Resource
	private IPublicUserService publicUserService;
	@Resource
	private ISharePointService sharePointService;
	@Resource
	private IEntityDao entityDao;
	@Resource
	private IPublicUserDao publicUserDao;
	@Resource
	private IPublicInvitationConfDao publicInvitationConfDao;
	
	
	@ResponseBody
	@RequestMapping(value = "/jump",method = {RequestMethod.GET}, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	public Object test5(HttpServletRequest request,@RequestParam("type") String type,@RequestParam("userId") String userId) {
		//根据request 请求获取访问用户本地语言
        PackBundle bundle = LangResource.getResourceBundle(request);
		if (StringUtil.isEmpty(type)) {
			return  toJson(EnumStatus.NINETY_NINE.getIndex(), bundle.getString("system.error"), "", null);
		}
		if (null == userId || "".equals(userId)) {
			logger.info("getUserDetails  user not login");
			return toJson(EnumStatus.NINETY_EIGHT.getIndex(), bundle.getString("user.not.login"), "", null);
		}
	    Map<String, Object> retMap = publicInvitationService.getInvitationConf(null);
	    if (null != retMap && retMap.containsKey("invitationId")) {
	    	if ("1".equals(type)) {
				Map<String, Object> model = new java.util.HashMap<>();
				try {
					model.put("InvitationId", java.net.URLEncoder.encode(retMap.get("invitationId").toString(),"UTF-8").replaceAll("%", AESUtil.KEY));
					String	invitationUserId = java.net.URLEncoder.encode(AESUtil.encryptStr(retMap.get("invitationId").toString(), userId),"UTF-8");
					model.put("InvitationUserId",invitationUserId.replaceAll("%", AESUtil.KEY));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String jumpPath ="/invitations/"+model.get("InvitationUserId")+"/"+model.get("InvitationId")+".c";	
				return new ModelAndView("redirect:"+jumpPath+"");
	    	} else {
	    		Map<String, Object> model = new java.util.HashMap<>();
	    		PublicUser sendUser = publicUserDao.getByUserId(userId);
	    		if (null == sendUser) {
	    			return toJson(EnumStatus.NINETY_EIGHT.getIndex(), bundle.getString("user.not.login"), "", null);
	    		}
	    		Entity entity = entityDao.selectAuthedEntityByUUID(sendUser.getUuid());
	    		if (null != entity &&  1 == entity.getStatus()) {
	    			model.put("Authentication", 1);
	    		} else {
	    			model.put("Authentication", 0);
	    		}
	    		Integer result = publicInvitationService.getInvitationUserCount(retMap.get("invitationId").toString(),userId);
				model.put("IUserCount", (null == result) ? 0 :result.intValue());
				try {
					model.put("InvitationId",java.net.URLEncoder.encode(retMap.get("invitationId").toString(),"UTF-8").replaceAll("%", AESUtil.KEY));
					String invitationUserId = java.net.URLEncoder.encode(AESUtil.encryptStr(retMap.get("invitationId").toString(), userId),"UTF-8");
					model.put("InvitationUserId", invitationUserId.replaceAll("%", AESUtil.KEY));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String jumpPath ="/invitations/"+model.get("Authentication")+"/"+model.get("IUserCount")+"/"+model.get("InvitationUserId")+"/"+model.get("InvitationId")+".c";	
				return new ModelAndView("redirect:"+jumpPath+"");
	    	}
	    }
	    return toJson(EnumStatus.NINETY_NINE.getIndex(), bundle.getString("system.error"), "", null);
	}

	  
	  
	@ResponseBody
	@RequestMapping(value = "/SMSVerifyCode", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE })
	public String addDeliveryAddress(HttpServletRequest request,
			@RequestParam("Mobile") String phone) {

        PackBundle bundle = LangResource.getResourceBundle(request);
		PhoneUtil pu = PhoneUtil.getInstance();
        String[] phones = phone.split("-");
        boolean flag = true;
        if (phones.length == 1) {//默认是中文
            flag = pu.checkPhoneNumberByCountryCode(phone, "86");
        } else if (phones.length == 2) {//直接合法性验证
            flag = pu.checkPhoneNumberByCountryCode(phones[1], phones[0]);
        }
        if (!flag) {
            return toJson(false, bundle.getString("mobile.grammar.fault"), null);
        }
      
		Map<String, String> map_post = new TreeMap<>();
		map_post.put("Mobile", phone);
		map_post.put("Appkey", appKey);
		// 签名算法
		String mSign = MyUtils.mSignatureAlgorithm(map_post);

		Map<String, String> params = new HashMap<String, String>();
		params.put("Appkey", appKey);
		params.put("Mobile", phone);
		params.put("Sign", mSign);
		 System.out.println("phone................"+phone);
        System.out.println("mSign................"+mSign);
        try {
            String rst = HttpClientUtil.doPost(Config.get("remote.url") + "/Api/SMSVerifyCode", params);
            return ThirdTranslate.getInstance().translateThirdReturn(bundle, rst);
        } catch (Exception ex) {
            return getErrorResponse(ex);
        }
	}
			
	 private String getErrorResponse(Exception ex) {
	        HashMap<String, Object> rst = new HashMap<>();
	        rst.put("success", false);
	        rst.put("error", ex.toString());
	        rst.put("data", null);
	        rst.put("errorCode", 2);
	        return JSON.toJSONString(rst);
	    }
	 
	@ResponseBody
	@RequestMapping(value = "/addDeliveryAddress", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE })
	public String addDeliveryAddress(HttpServletRequest request,
			@RequestParam("InvitationId") String invitationId,
			@RequestParam("InvitationUserId") String invitationUserId,
			@RequestParam("Mobile") String mobile,
			@RequestParam("UserAddress") String userAddress,
			@RequestParam("UserName") String userName) {
		  //根据request 请求获取访问用户本地语言
		 PackBundle bundle = LangResource.getResourceBundle(request);	
		 
		
        if (StringUtil.isEmpty(invitationId)) {
			return toJson(false, bundle.getString("need.invitationId"), null);
		}
        if (StringUtil.isEmpty(invitationUserId)) {
			return toJson(false, bundle.getString("need.invitationUserId"), null);
		}
        if (StringUtil.isEmpty(mobile)) {
			return toJson(false, bundle.getString("need.mobile"), null);
		}
        if (StringUtil.isEmpty(userAddress)) {
			return toJson(false, bundle.getString("need.userAddress"), null);
		}
        if (StringUtil.isEmpty(userName)) {
			return toJson(false, bundle.getString("need.userName"), null);
		}
        try {
			invitationUserId = AESUtil.dncryptStr(java.net.URLDecoder.decode(invitationUserId.replaceAll(AESUtil.KEY,"%"),"UTF-8"));
		} catch (Exception ex) {
		     return toJson(false, bundle.getString("system.error"), null);
		}
        
        PublicUser sendUser = publicUserDao.getByUserId(invitationUserId);
		if (null == sendUser) {
			 return toJson(false, bundle.getString("system.error"), null);
		}
		try {
			invitationId = java.net.URLDecoder.decode(invitationId.replaceAll(AESUtil.KEY, "%"),"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		java.util.List<PublicInvitationConf> invitationConfList = publicInvitationConfDao.listByInvitationId(invitationId);
		if (null ==  invitationConfList || invitationConfList.size() <= 0) {
			 return toJson(false, bundle.getString("system.error"), null);
		}
		
		boolean result = publicInvitationService.isPublicInvitationDeliveryAddress(invitationId, invitationUserId, mobile);
		if (result) {
			 return toJson(false, bundle.getString("invitation.delivery.address"), null);
		}
		
        PublicInvitationDeliveryAddress publicInvitationDeliveryAddress = new PublicInvitationDeliveryAddress ();
        publicInvitationDeliveryAddress.setCreateTime(new java.util.Date());
        publicInvitationDeliveryAddress.setDeliveryStatus(0);
        publicInvitationDeliveryAddress.setInvitationId(invitationId);
        publicInvitationDeliveryAddress.setInvitationUserId(invitationUserId);
        publicInvitationDeliveryAddress.setMobile(mobile);
        publicInvitationDeliveryAddress.setUpateTime(new java.util.Date());
        publicInvitationDeliveryAddress.setUserAddress(userAddress);
        publicInvitationDeliveryAddress.setUserName(userName);
        boolean ret = publicInvitationService.insert(publicInvitationDeliveryAddress);
        if (ret) {
        	return toJson(true, bundle.getString("need.success"), null);
        }
        return toJson(false, bundle.getString("system.error"), null);
	}

	
	@ResponseBody
	@RequestMapping(value = "/getInvitationUserCount", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE })
	public String getInvitationUserCount(HttpServletRequest request,
			@RequestParam("InvitationId") String invitationId,
			@RequestParam("InvitationUserId") String invitationUserId) {
		PackBundle bundle = LangResource.getResourceBundle(request);
		if (StringUtil.isEmpty(invitationId)) {
			return toJson(false, bundle.getString("need.invitationId"), null);
		}
		if (StringUtil.isEmpty(invitationUserId)) {
			return toJson(false, bundle.getString("need.invitationUserId"), null);
		}
		Integer ret = publicInvitationService.getInvitationUserCount(invitationId,invitationUserId);
		if (null != ret) {
			return toJson(false,null, ret);
		}
		return toJson(false,null, 0);
	}
	
	/***
     * 邀请用户注册
     * @param request
     * @param phone
     * @param password
     * @param verifyCode
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/inviteUsersRegister", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public DeferredResult<String> inviteUsersRegister (HttpServletRequest request, 
    		@RequestParam("Mobile") String phone,
            @RequestParam("Password") String password,
            @RequestParam("VerifyCode") String verifyCode,
            @RequestParam("InvitationId") String invitationId,
            @RequestParam("InvitationUserId") String invitationUserId) {
    	DeferredResult<String> dr = new DeferredResult<>();
        PackBundle bundle = LangResource.getResourceBundle(request);
        String ip = RequestUtil.getIpAddr(request);
        String deviceId = ip;

        if (StringUtil.isEmpty(phone)) {
        	dr.setResult(toJson(false, bundle.getString("need.mobile"), null));
            return dr;
        }
        PhoneUtil pu = PhoneUtil.getInstance();
        String[] phones = phone.split("-");
        boolean flag = true;
        if (phones.length == 1) {//默认是中文
            flag = pu.checkPhoneNumberByCountryCode(phone, "86");
        } else if (phones.length == 2) {//直接合法性验证
            flag = pu.checkPhoneNumberByCountryCode(phones[1], phones[0]);
        }
        if (!flag) {
        	dr.setResult(toJson(false, bundle.getString("mobile.grammar.fault"), null));
            return dr;
        }

        if (StringUtil.isEmpty(password)) {
        	dr.setResult(toJson(false, bundle.getString("need.password"), null));
            return dr;
        }

        if (StringUtil.isEmpty(appKey)) {
        	dr.setResult(toJson(false, bundle.getString("need.param", "appKey"), null));
            return dr;
        }

        if (StringUtil.isEmpty(verifyCode)) {
        	dr.setResult(toJson(false, bundle.getString("need.sms"), null));
            return dr;
        }
        
        if (StringUtil.isEmpty(invitationId)) {
        	dr.setResult(toJson(false, bundle.getString("need.invitationId"), null));
            return dr;
        }
		try {
			invitationId = java.net.URLDecoder.decode(invitationId.replaceAll(AESUtil.KEY,"%"),"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		java.util.List<PublicInvitationConf> invitationConfList = publicInvitationConfDao.listByInvitationId(invitationId);
		if (null ==  invitationConfList || invitationConfList.size() <= 0) {
			dr.setResult(toJson(false, bundle.getString("system.error"), null));
			return dr;
		}
    
        if (StringUtil.isEmpty(invitationUserId)) {
        	dr.setResult(toJson(false, bundle.getString("need.invitationUserId"), null));
            return dr;
        }
        
    	try {
			invitationUserId = AESUtil.dncryptStr(java.net.URLDecoder.decode(invitationUserId.replaceAll(AESUtil.KEY,"%"),"UTF-8"));
		} catch (Exception ex) {
			dr.setResult(toJson(false, bundle.getString("system.error"), null));
		    return dr;
		}
    	
        PublicUser sendUser = publicUserDao.getByUserId(invitationUserId);
		if (null == sendUser) {
			dr.setResult(toJson(false, bundle.getString("system.error"), null));
		    return dr;
		}
        String encrptPwd = null;
        try {
			 encrptPwd = MyUtils.Encrypt(password, MyUtils.AESKEY);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
     // 保存在 Map
        Map<String, String> map_post = new TreeMap<>();
        map_post.put("Mobile", phone);
        map_post.put("Password", encrptPwd.trim());
        map_post.put("VerifyCode", verifyCode);
        map_post.put("Appkey", appKey);
        // 签名算法
        String mSign = MyUtils.mSignatureAlgorithm(map_post);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("Appkey", appKey);
        params.put("Mobile", phone);
        params.put("password", encrptPwd);
        params.put("sign", mSign);
        params.put("VerifyCode", verifyCode);

        try {
            String responseText = HttpClientUtil.doJsonPost(Config.get("remote.url") + "/Api/CustomerRegist", params);
            logger.info(".................sign......"+mSign);
            logger.info(".................responseText......"+responseText);
            JSONObject json = (JSONObject) JSONObject.parse(responseText);
            if (StringUtil.isNotEmpty(json.get("Message"))) {
            	dr.setResult(toJson(false, bundle.getString("need.http.null"), null));
                return dr;
            }
            if (json.get("success").equals(false)) {
            	dr.setResult(toJson(false, json.getString("error"), null));
                return dr;
            }

            JSONObject data = (JSONObject) json.get("data");
            if (json.get("success").equals(true) && StringUtil.isNotEmpty(data)) {
                if (data.containsKey("myOutPutUser")) {

                    JSONObject userData = (JSONObject) data.get("myOutPutUser");
                    if (null != data && userData.containsKey("UUID") && StringUtil.isNotEmpty(userData.getString("UUID"))) {
                        SharePoint bean = new SharePoint();
                        bean.setBalance(BigDecimal.ZERO);
                        bean.setUuid(userData.getString("UUID"));
                        sharePointService.save(bean);

                        UserDetails userDetails = new UserDetails();
                        userDetails.setUuid(userData.getString("UUID"));
                        userDetails.setNikeName(userData.getString("NikeName"));
                        userDetails.setMobile(userData.getString("Mobile"));
                        SessionUtil.addUserDetails(request, userDetails);

                        LoginInfo loginInfo = new LoginInfo();
                        loginInfo.setSessionId(request.getSession().getId());
                        loginInfo.setIp(ip);
                        loginInfo.setLoginTime(new Date().getTime());
                        loginInfo.setMobile(phone);
                        loginInfo.setAppKey(appKey);
                        loginInfo.setDeviceId(deviceId);

                        SessionUtil.setLoginInfo(request, loginInfo);
                        PublicInvitationUserRegister publicInvitationUserRegister=new PublicInvitationUserRegister();
                		publicInvitationUserRegister.setCreateTime(new java.util.Date());
                		publicInvitationUserRegister.setInvitationId(invitationId);
                		publicInvitationUserRegister.setInvitationUserId(invitationUserId);
                		publicInvitationUserRegister.setMobile(phone);
                		publicInvitationUserRegister.setUpateTime(new java.util.Date());
                        return createUser(request,bundle,userDetails,publicInvitationUserRegister);
                    }
                }
            }
            dr.setResult(toJson(false, json.getString("error"), null));
            return dr;
        } catch (Exception ex) {
        	dr.setResult(toJson(false, ex.getMessage(), null));
            return dr;
        }
    }
    
    
	public  DeferredResult<String> createUser(HttpServletRequest request,PackBundle bundle,UserDetails userDetails,PublicInvitationUserRegister userRegister) {
		
		final DeferredResult<String> deferred = new DeferredResult<>();
		String apiName = "post + /api/share/user/create";
		PublicUser user = new PublicUser();
		try {
			user.setAvatarUrl("resources/images/user/user_log_b.png");
			user.setThumbAvatarUrl("resources/images/user/user_logo_s.png");
			user.setBackgroundImgUrl("resources/images/user/user_bg.png");
		} catch (Exception e) {
			e.printStackTrace();
			deferred.setResult(toJson(4, bundle.getString("failed.execute"), 
					"post + /api/share/user/create",
					new ArrayList<Object>()));
			return deferred;
		}

		try {
			user.setVersion(2);
			user.setStatus("1");
			user.setTypeId(0);
			user.setUserName("U".concat(RandomUtil.getFixLenthString(11)));
			user.setUuid(userDetails.getUuid());
			user.setCreateTime(new Timestamp(new Date().getTime()));
			user.setDescription(userDetails.getDescription());
			user.setMobile(userDetails.getMobile());

			PublicUser unUser = publicUserService.getUnregisteredUserByMobile(userDetails.getMobile());

			if (null != unUser) {
				user.setId(unUser.getId());
				publicUserService.update(user);
				try {
					UserCache userCache = CacheUtil.getOneDay(Const.APP_USERCACHE + user.getId());

					if (null != userCache) {
						userCache.setUuid(user.getUuid());
						userCache.setUserName(user.getUserName());
						userCache.setMobile(user.getMobile());
						CacheUtil.putOneDay(Const.APP_USERCACHE + user.getId(), userCache);
					}

				} catch (Exception e) {
					e.printStackTrace();
				}

			} else {
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
	
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			logger.error(e.getLocalizedMessage());
		}
		
		executorService.submit(new Runnable() {
			@Override
			public void run(){
				//用户注册成功后，关注通用公共账户
				//同时创建群，让该用户自动进群获取奖励
				try {
					logger.info("开始自动关注、建群、入群，注册用户UUID:" + JSON.toJSONString(user));
					publicUserService.registerPrize(bundle, user.getUuid());
				}
				catch (Exception e){
					logger.info("注册关注、建群、入群异常：" + e.getMessage());
					deferred.setResult(toJson(5, bundle.getString("failed.execute"), apiName, e.getMessage()));
				}
				
				try {
					userRegister.setUserId(user.getId());
					boolean ret = publicInvitationService.insert(userRegister);
					if (ret) {
						logger.info("插入用户要求注册表 success" + ret);
						deferred.setResult(toJson(0, bundle.getString("need.success"), apiName, ret));
					}
				} catch (Exception ex) {
					logger.info("插入用户要求注册表异常" + ex.getMessage());
					deferred.setResult(toJson(5, bundle.getString("failed.execute"), apiName, ex.getMessage()));
				}
			}
		});
		return deferred;
	}
	
	
	@ResponseBody
	@RequestMapping(value = "/getDisabledStatus", method = { RequestMethod.GET }, produces = {
			MediaType.APPLICATION_JSON_UTF8_VALUE })
	public String getDisabledStatus(HttpServletRequest request) {
		  //根据request 请求获取访问用户本地语言  Misc.getServerUri(request)
        PackBundle bundle = LangResource.getResourceBundle(request);
      //获取当前登录用户信息
        UserDetails userDetails = SessionUtil.getUserDetails(request);
        if (null == userDetails) {
        	logger.info("getUserDetails  user not login");
        	return toJson(EnumStatus.NINETY_EIGHT.getIndex(), bundle.getString("user.not.login"), "", null);
        }
        String apiName = "get + /api/invitation/getDisabledStatus";
        Map<String, Object> retMap = publicInvitationService.getInvitationConf(null);
        if (null != retMap) {
        	return toJson(EnumStatus.ZORO.getIndex(), null, apiName, retMap);
        }
        //logger.info("-------------------------" + ret);
		return toJson(EnumStatus.NINETY_NINE.getIndex(), bundle.getString("system.error"), apiName, null);
	}

}
