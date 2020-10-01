package com.taiyiyun.passport.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.taiyiyun.passport.language.LangResource;
import com.taiyiyun.passport.language.PackBundle;
import com.taiyiyun.passport.service.IPasswordLockService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import com.taiyiyun.passport.bean.TradePasswordBean;
import com.taiyiyun.passport.bean.UserDetails;
import com.taiyiyun.passport.consts.Config;
import com.taiyiyun.passport.consts.Const;
import com.taiyiyun.passport.po.TradePassword;
import com.taiyiyun.passport.service.IAppConfigService;
import com.taiyiyun.passport.service.IRedisService;
import com.taiyiyun.passport.service.ITradePasswordService;
import com.taiyiyun.passport.util.MD5Util;
import com.taiyiyun.passport.util.Misc;
import com.taiyiyun.passport.util.SessionUtil;
import com.taiyiyun.passport.util.StringUtil;

import java.util.ResourceBundle;

@Controller
public class ApiTradePasswordController extends BaseController {
	
	@Resource
	private ITradePasswordService tradePasswordService;
	
	@Resource
	private IAppConfigService appConfigService;
	
	@Resource
	private IRedisService redisService;

	@Resource
	private IPasswordLockService passwordLockService;

	private final String PASSWORDERROR = "tradepass.error";

	/**
	 * 设置交易密码
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/api/money/password/set", method = {RequestMethod.POST}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public String setPassword(@RequestBody TradePasswordBean bean, HttpServletRequest request) {

		PackBundle bundle = LangResource.getResourceBundle(request);

		UserDetails userDetails = SessionUtil.getUserDetails(request);
		if(null == userDetails) {
			return toJson(1, bundle.getString("user.not.login"), "post + /api/money/password/set", "");
		}
		
		/*if(null == bean || StringUtil.isEmpty(bean.getTransId()) || StringUtil.isEmpty(bean.getPassword())) {
			return toJson(7, bundle.getString("need.param", ""), "post + /api/money/password/set", "");
		}*/

		if(!Misc.regexMoneyPwd(bean.getPassword())) {
			return toJson(2, bundle.getString("password.grammar.fault"), "post + /api/money/password/set", "");
		}
		
		/*String  transactionId = redisService.get(Const.TRANS_ID + userDetails.getMobile());
		redisService.evict(Const.TRANS_ID + userDetails.getMobile());
		
		if(StringUtil.isEmpty(transactionId)) {
			return toJson(3, bundle.getString("failed.operation.timeout"), "post + /api/money/password/set", "");
		}
		
		if (!transactionId.equals(bean.getTransId())) {
			return toJson(6, bundle.getString("need.param.error", "transId"), "post + /api/money/password/set", "");
		}*/
		
		try {
			
			TradePassword tp = tradePasswordService.getByUUID(userDetails.getUuid());
			if(null != tp) {
				tp.setPwd(MD5Util.MD5Encode(bean.getPassword(), false));
				int count = tradePasswordService.update(tp);
				if(1 == count) {
					passwordLockService.releaseLock(PASSWORDERROR, userDetails.getUuid());
				}
				return toJson(0, bundle.getString("successful.password.set"), "post + /api/money/password/set", "");
			}
			
			TradePassword tradePwd = new TradePassword();
			tradePwd.setPwd(MD5Util.MD5Encode(bean.getPassword(), false));
			tradePwd.setUuid(userDetails.getUuid());
			tradePasswordService.save(tradePwd);
			
			return toJson(0, bundle.getString("successful.password.set"), "post + /api/money/password/set", "");
		} catch (Exception e) {
			e.printStackTrace();
			return toJson(5, bundle.getString("failed.execute"), "post + /api/money/password/set", "");
		}
	}
	
	/**
	 * 修改交易密码
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/api/money/password/modify", method = {RequestMethod.POST}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public String modifyPassword(@RequestBody TradePasswordBean bean, HttpServletRequest request) {

		PackBundle bundle = LangResource.getResourceBundle(request);

		UserDetails userDetails = SessionUtil.getUserDetails(request);
		if(null == userDetails) {
			return toJson(1, bundle.getString("user.not.login"), "post + /api/money/password/modify", "");
		}
		
		if(null == bean || StringUtil.isEmpty(bean.getOld()) || StringUtil.isEmpty(bean.getPassword())) {
			return toJson(7, bundle.getString("need.param", ""), "post + /api/money/password/modify", "");
		}
		
		if(!Misc.regexMoneyPwd(bean.getPassword())) {
			return toJson(2, bundle.getString("password.grammar.fault"), "post + /api/money/password/modify", "");
		}
		
		try {

			if(passwordLockService.checkLock(PASSWORDERROR, userDetails.getUserId())) {
				return toJson(8, bundle.getString("password.lock"), "post + /api/money/password/modify", "");
			}
			
			TradePassword tradePwd = tradePasswordService.getByUUID(userDetails.getUuid());
			if(null == tradePwd) {
				return toJson(4, bundle.getString("password.not.found"), "post + /api/money/password/modify", "");
			}
			
			if(!MD5Util.MD5Encode(bean.getOld(), false).equals(tradePwd.getPwd())) {
				passwordLockService.refreshError(PASSWORDERROR, userDetails.getUuid());
				return toJson(3, bundle.getString("password.old.fault"), "post + /api/money/password/modify", "");
			}
			
			tradePwd.setPwd(MD5Util.MD5Encode(bean.getPassword(), false));
			
			int count = tradePasswordService.update(tradePwd);
			if(1 == count) {
				passwordLockService.releaseLock(PASSWORDERROR, userDetails.getUuid());
				return toJson(0, bundle.getString("successful.password.set"), "post + /api/money/password/modify", "");
			}
			
			return toJson(5, bundle.getString("failed.password.set"), "post + /api/money/password/modify", "");
		} catch (Exception e) {
			e.printStackTrace();
			return toJson(6, bundle.getString("failed.execute"), "post + /api/money/password/modify", "");
		}
	}
	
	/**
	 * 验证交易密码
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/api/money/password/check", method = {RequestMethod.POST}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public String checkPassword(@RequestBody TradePasswordBean bean, HttpServletRequest request) {

		PackBundle bundle = LangResource.getResourceBundle(request);

		String apiName = "post + /api/money/password/check";
		
		UserDetails userDetails = SessionUtil.getUserDetails(request);
		if(null == userDetails) {
			return toJson(1, bundle.getString("user.not.login"), apiName, "");
		}
		
		if(null == bean) {
			return toJson(5, bundle.getString("need.param", ""), apiName, "");
		}
		
		try {
			
			if(passwordLockService.checkLock(PASSWORDERROR, userDetails.getUuid())) {
				return toJson(6, bundle.getString("password.lock"), apiName, "");
			}
			
			if(!Misc.regexMoneyPwd(bean.getPassword())) {
				return toJson(2, bundle.getString("password.grammar.fault"), apiName, "");
			}
			
			TradePassword tradePwd = tradePasswordService.getByUUID(userDetails.getUuid());
			if(null == tradePwd) {
				return toJson(2, bundle.getString("password.not.found"), apiName, "");
			}
			
			if(!MD5Util.MD5Encode(bean.getPassword(), false).equals(tradePwd.getPwd())) {
				passwordLockService.refreshError(PASSWORDERROR, userDetails.getUuid());
				return toJson(3, bundle.getString("need.password.error"), apiName, "");
			}
			
			passwordLockService.releaseLock(PASSWORDERROR, userDetails.getUuid());
			
			return toJson(0, bundle.getString("successful.verify"), apiName, "");
		} catch (Exception e) {
			e.printStackTrace();
			return toJson(4, bundle.getString("failed.execute"), apiName, "");
		}
	}
	
	/**
	 * 检查是否设置了交易密码
	 * @param password
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/api/money/password/check", method = {RequestMethod.GET}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public String checkPwdExists(HttpServletRequest request) {

		PackBundle bundle = LangResource.getResourceBundle(request);

		UserDetails userDetails = SessionUtil.getUserDetails(request);
		if(null == userDetails) {
			return toJson(1, bundle.getString("user.not.login"), "get + /api/money/password/check", "");
		}
		
		try {
			
			TradePassword tradePwd = tradePasswordService.getByUUID(userDetails.getUuid());
			if(null == tradePwd) {
				return toJson(2, bundle.getString("password.not.found"), "get + /api/money/password/check", "");
			}
			
			return toJson(0, bundle.getString("failed.password.set"), "get + /api/money/password/check", "");
		} catch (Exception e) {
			e.printStackTrace();
			return toJson(3, bundle.getString("failed.execute"), "get + /api/money/password/check", "");
		}
	}


}
