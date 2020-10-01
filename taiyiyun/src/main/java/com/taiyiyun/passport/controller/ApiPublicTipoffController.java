package com.taiyiyun.passport.controller;

import com.alibaba.fastjson.JSON;
import com.taiyiyun.passport.bean.UserDetails;
import com.taiyiyun.passport.consts.Const;
import com.taiyiyun.passport.consts.EnumStatus;
import com.taiyiyun.passport.language.LangResource;
import com.taiyiyun.passport.language.PackBundle;
import com.taiyiyun.passport.po.BaseResult;
import com.taiyiyun.passport.po.CodeDictionary;
import com.taiyiyun.passport.po.user.TipoffDto;
import com.taiyiyun.passport.service.ICodeDictionaryService;
import com.taiyiyun.passport.service.IPublicTipoffService;
import com.taiyiyun.passport.util.SessionUtil;
import com.taiyiyun.passport.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ApiPublicTipoffController extends BaseController {

	public final Logger logger = LoggerFactory.getLogger(getClass());

	@Resource
	private IPublicTipoffService publicTipoffService;

	@Resource
	private ICodeDictionaryService codeDictionaryService;

	@ResponseBody
	@RequestMapping(value = "/api/user/getTipoffType", method = {RequestMethod.GET}, produces = {Const.PRODUCES_JSON})
	public String getAccuseArticles(@RequestParam(value = "businessType", required = true) String businessType, HttpServletRequest request) {
		PackBundle bundle = LangResource.getResourceBundle(request);
		String apiName = "get + /api/user/getTipoffType";
		try {
			//业务参数类型不能为空
			if(StringUtil.isEmpty(businessType)){
				return toJson(EnumStatus.ONE.getIndex(), bundle.getString("need.param", "businessType"), apiName, null);
			}
			List<CodeDictionary> dataList = codeDictionaryService.getByBusiness(businessType + "_" + bundle.getLanguage());
			List<Map<String, Object>> jsonList = new ArrayList<>();
			if (null != dataList && dataList.size() > 0) {
				for (CodeDictionary cd : dataList) {
					Map<String, Object> json = new HashMap<>();
					json.put("id", cd.getId());
					json.put("code", cd.getCode());
					json.put("text", cd.getCaption());
					jsonList.add(json);
				}
			}
			return toJson(EnumStatus.ZORO.getIndex(), bundle.getString("successful.search"), apiName, jsonList);
		} catch (Exception e) {
			e.printStackTrace();
			return toJson(EnumStatus.NINETY_NINE.getIndex(), bundle.getString("failed.execute"), apiName, new ArrayList<>());
		}
	}

	/**
	 * 文章举报
	 */
	@ResponseBody
	@RequestMapping(value = "/api/user/tipoff", method = {RequestMethod.POST}, produces = {Const.PRODUCES_JSON})
	public DeferredResult<String> accuseArticles(@RequestBody TipoffDto tipoffDto, HttpServletRequest request) {
		PackBundle bundle = LangResource.getResourceBundle(request);
		String apiName = "post + /api/user/tipoff";
		UserDetails userDetails = SessionUtil.getUserDetails(request);
		if(null == userDetails) {
			DeferredResult<String> dr = new DeferredResult<>();
			dr.setResult(toJson(EnumStatus.NINETY_EIGHT.getIndex(), bundle.getString("user.not.login"), apiName,null));
			return dr;
		}
		String userId = userDetails.getUserId();
		if(StringUtil.isEmpty(userId)) {
			DeferredResult<String> dr = new DeferredResult<>();
			dr.setResult(toJson(EnumStatus.NINETY_EIGHT.getIndex(), bundle.getString("user.not.login"), apiName,null));
			return dr;
		}
		String tipoffId = tipoffDto.getTipoffId();
		Integer tipoffType = tipoffDto.getTipoffType();
		Integer illegalType = tipoffDto.getIllegalType();
		String tipoffContent = tipoffDto.getTipoffContent();
		if(StringUtil.isEmpty(tipoffId)) {
			DeferredResult<String> dr = new DeferredResult<>();
			dr.setResult(toJson(EnumStatus.TWO.getIndex(), bundle.getString("need.param", "tipoffId"), apiName,null));
			return dr;
		}
		if(StringUtil.isEmpty(tipoffType)) {
			DeferredResult<String> dr = new DeferredResult<>();
			dr.setResult(toJson(EnumStatus.TWO.getIndex(), bundle.getString("need.param", "tipoffType"), apiName,null));
			return dr;
		}
		if(StringUtil.isEmpty(illegalType)) {
			DeferredResult<String> dr = new DeferredResult<>();
			dr.setResult(toJson(EnumStatus.TWO.getIndex(), bundle.getString("need.param", "illegalType"), apiName,null));
			return dr;
		}
		if(StringUtil.isEmpty(tipoffContent)) {
			DeferredResult<String> dr = new DeferredResult<>();
			dr.setResult(toJson(EnumStatus.TWO.getIndex(), bundle.getString("need.param", "tipoffContent"), apiName,null));
			return dr;
		}
		try{
			//设置登录人userId
			tipoffDto.setUserId(userId);
			DeferredResult<String> dr = new DeferredResult<>();
			BaseResult<Integer> resultData = publicTipoffService.addTipoff(bundle,tipoffDto);
			String rjson = JSON.toJSONString(resultData);
			dr.setResult(rjson);
			return dr;
		} catch (Exception e) {
			e.printStackTrace();
			DeferredResult<String> dr = new DeferredResult<>();
			dr.setResult(toJson(EnumStatus.NINETY_NINE.getIndex(), bundle.getString("system.error"), apiName, null));
			return dr;
		}
	}
}
