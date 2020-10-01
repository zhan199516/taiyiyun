package com.taiyiyun.passport.controller.bchain;

import com.alibaba.fastjson.JSON;
import com.taiyiyun.passport.bean.UserDetails;
import com.taiyiyun.passport.consts.Const;
import com.taiyiyun.passport.consts.EnumStatus;
import com.taiyiyun.passport.controller.BaseController;
import com.taiyiyun.passport.language.LangResource;
import com.taiyiyun.passport.language.PackBundle;
import com.taiyiyun.passport.po.BaseResult;
import com.taiyiyun.passport.po.bchain.FoodPointDto;
import com.taiyiyun.passport.po.bchain.FoodProductDto;
import com.taiyiyun.passport.service.bchain.IFoodchainService;
import com.taiyiyun.passport.util.SessionUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/api")
public class FoodchainController extends BaseController {
	
	@Resource
	private IFoodchainService foodchainService;
	
	@ResponseBody
	@RequestMapping(value = "/fchain/getProducts", method = RequestMethod.POST, produces = {Const.PRODUCES_JSON})
	public DeferredResult<String> getProducts(@RequestBody FoodProductDto foodProductDto, HttpServletRequest request, Model model) {
		//根据request 请求获取访问用户本地语言
		PackBundle bundle = LangResource.getResourceBundle(request);
		//api名称
		String apiName = "post + /api/fchain/getProducts";
		//获取当前登录用户信息
		UserDetails userDetails = SessionUtil.getUserDetails(request);
		if(userDetails == null) {
			DeferredResult<String> dr = new DeferredResult<>();
			dr.setResult(toJson(EnumStatus.NINETY_EIGHT.getIndex(), bundle.getString("user.not.login"), apiName, null));
			return dr;
		}
		//设置用户免打扰信息
		try {
			DeferredResult<String> dr = new DeferredResult<>();
			BaseResult<Object> resultData = foodchainService.getFoodProducts(userDetails,foodProductDto);
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
	@RequestMapping(value = "/fchain/getPointRecords", method = RequestMethod.POST, produces = {Const.PRODUCES_JSON})
	public DeferredResult<String> getPointRecords(@RequestBody FoodPointDto foodPointDto, HttpServletRequest request, Model model) {
		//根据request 请求获取访问用户本地语言
		PackBundle bundle = LangResource.getResourceBundle(request);
		//api名称
		String apiName = "post + /api/fchain/getPointRecords";
		//获取当前登录用户信息
		UserDetails userDetails = SessionUtil.getUserDetails(request);
		if(userDetails == null) {
			DeferredResult<String> dr = new DeferredResult<>();
			dr.setResult(toJson(EnumStatus.NINETY_EIGHT.getIndex(), bundle.getString("user.not.login"), apiName, null));
			return dr;
		}
		//设置用户免打扰信息
		try {
			DeferredResult<String> dr = new DeferredResult<>();
			BaseResult<Object> resultData = foodchainService.getPointRecords(userDetails,foodPointDto);
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
	@RequestMapping(value = "/fchain/getPointCount", method = RequestMethod.POST, produces = {Const.PRODUCES_JSON})
	public DeferredResult<String> getPointCount(@RequestBody FoodPointDto foodPointDto, HttpServletRequest request, Model model) {
		//根据request 请求获取访问用户本地语言
		PackBundle bundle = LangResource.getResourceBundle(request);
		//api名称
		String apiName = "post + /api/fchain/getPointCount";
		//获取当前登录用户信息
		UserDetails userDetails = SessionUtil.getUserDetails(request);
		if (userDetails == null) {
			DeferredResult<String> dr = new DeferredResult<>();
			dr.setResult(toJson(EnumStatus.NINETY_EIGHT.getIndex(), bundle.getString("user.not.login"), apiName, null));
			return dr;
		}
		//设置用户免打扰信息
		try {
			DeferredResult<String> dr = new DeferredResult<>();
			BaseResult<Object> resultData = foodchainService.getPointCount(userDetails,foodPointDto);
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
