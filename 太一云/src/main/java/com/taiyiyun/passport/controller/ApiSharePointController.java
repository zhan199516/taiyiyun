package com.taiyiyun.passport.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.taiyiyun.passport.language.LangResource;
import com.taiyiyun.passport.language.PackBundle;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.taiyiyun.passport.bean.UserDetails;
import com.taiyiyun.passport.po.SharePoint;
import com.taiyiyun.passport.service.ISharePointService;
import com.taiyiyun.passport.util.SessionUtil;
import com.taiyiyun.passport.util.StringUtil;

@Controller
public class ApiSharePointController extends BaseController {
	
	@Resource
	private ISharePointService sharePointService;
	
	@RequestMapping(value = "/api/sharePoint/getBalance", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public String getBalance(HttpServletRequest request) {

		PackBundle bundle = LangResource.getResourceBundle(request);

		UserDetails userDetails = SessionUtil.getUserDetails(request);
		if(null == userDetails) {
			return toJson(1, bundle.getString("user.not.login"), "post + /api/sharePoint/getBalance", null);
		}
		
		SharePoint sharePoint = sharePointService.getSharePoint(userDetails.getUuid());
		
		if(null == sharePoint) {
			return toJson(2, bundle.getString("user.name.not.find"), "post + /api/sharePoint/getBalance", null);
		}
		
		return toJson(0, bundle.getString("successful.search"), "post + /api/sharePoint/getBalance", sharePoint);
	}
	
	
	@RequestMapping(value = "/api/sharePoint/transfer", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public String transfersharePoint(String fromUUID, String toUUID, String sharePoint, HttpServletRequest request) {

		PackBundle bundle = LangResource.getResourceBundle(request);

		if(StringUtil.isEmpty(fromUUID) || StringUtil.isEmpty(toUUID) || StringUtil.isEmpty(sharePoint)) {
			return toJson(2, bundle.getString("need.param", ""), "post + /api/sharePoint/transfersharePoint", null);
		}
		
		
		return null;
	}
}
