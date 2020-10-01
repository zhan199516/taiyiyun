package com.taiyiyun.passport.controller;

import com.alibaba.fastjson.JSON;
import com.taiyiyun.passport.bean.UserDetails;
import com.taiyiyun.passport.commons.ResultData;
import com.taiyiyun.passport.consts.EnumStatus;
import com.taiyiyun.passport.consts.EnumType;
import com.taiyiyun.passport.language.LangResource;
import com.taiyiyun.passport.language.PackBundle;
import com.taiyiyun.passport.po.BaseResult;
import com.taiyiyun.passport.po.redpacket.RedPacketFormBean;
import com.taiyiyun.passport.po.redpacket.RedpacketDto;
import com.taiyiyun.passport.po.redpacket.RedpacketHandout;
import com.taiyiyun.passport.service.IPublicRedPacketService;
import com.taiyiyun.passport.util.SessionUtil;
import com.taiyiyun.passport.util.StringUtil;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;


/**
 * 红包相关控制器
 */
@Controller
@RequestMapping("/api/redpacket")
public class ApiRedpacketController extends BaseController {

    @Resource
	private IPublicRedPacketService publicRedPacketService;


    @ResponseBody
    @RequestMapping(value = "/handoutCommon",method = {RequestMethod.POST}, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public DeferredResult<String> handoutCommon(@RequestBody RedpacketHandout redpacketHandout, HttpServletRequest request){
        //获取当前登录用户信息
        UserDetails userDetails = SessionUtil.getUserDetails(request);
        //根据request 请求获取访问用户本地语言
        PackBundle bundle = LangResource.getResourceBundle(request);
        //api名称
        String apiName = "post + /api/redpacket/handoutCommon";
        //****************************************************************
        //参看其他控制器参数验证实现，并没有采用bean valid的方式，直接逐一显示验证
        //****************************************************************
        if(userDetails == null) {
            DeferredResult<String> dr = new DeferredResult<>();
            dr.setResult(toJson(EnumStatus.NINETY_EIGHT.getIndex(), bundle.getString("user.not.login"), apiName, null));
            return dr;
        }
        //平台ID不能为空
        if(StringUtil.isEmpty(redpacketHandout.getPlatformId())) {
            DeferredResult<String> dr = new DeferredResult<>();
            dr.setResult(toJson(EnumStatus.ONE.getIndex(), bundle.getString("need.param", "platformId"), apiName,null));
            return dr;
        }
        //币ID不能为空
        if(StringUtil.isEmpty(redpacketHandout.getCoinId())) {
            DeferredResult<String> dr = new DeferredResult<>();
            dr.setResult(toJson(EnumStatus.ONE.getIndex(), bundle.getString("need.param", "coinId"), apiName, null));
            return dr;
        }
        //目标用户类型不能为空
        if(StringUtil.isEmpty(redpacketHandout.getSessionType())) {
            DeferredResult<String> dr = new DeferredResult<>();
            dr.setResult(toJson(EnumStatus.ONE.getIndex(), bundle.getString("need.param", "sessionType"), apiName, null));
            return dr;

        }
        //目标用户ID不能为空
        if(StringUtil.isEmpty(redpacketHandout.getSessionId())) {
            DeferredResult<String> dr = new DeferredResult<>();
            dr.setResult(toJson(EnumStatus.ONE.getIndex(), bundle.getString("need.param", "sessionId"), apiName, null));
            return dr;
        }
        //红包金额不能为空
        if(StringUtil.isEmpty(redpacketHandout.getEachAmount())) {
            DeferredResult<String> dr = new DeferredResult<>();
            dr.setResult(toJson(EnumStatus.ONE.getIndex(), bundle.getString("need.param", "eachAmount"), apiName, null));
            return dr;
        }
        //红包数量不能为空
        if(StringUtil.isEmpty(redpacketHandout.getRedpacketCount())) {
            DeferredResult<String> dr = new DeferredResult<>();
            dr.setResult(toJson(EnumStatus.ONE.getIndex(), bundle.getString("need.param", "redpacketCount"), apiName, null));
            return dr;
        }
        ////红包寄语不能为空
        if(StringUtil.isEmpty(redpacketHandout.getText())) {
            DeferredResult<String> dr = new DeferredResult<>();
            dr.setResult(toJson(EnumStatus.ONE.getIndex(), bundle.getString("need.param", "text"), apiName, null));
            return dr;
        }
        //获取登录用户userId
        String userId = userDetails.getUserId();
        redpacketHandout.setUserId(userId);
        //设置红包类型
        Short sessionType = redpacketHandout.getSessionType();
        if (sessionType == EnumStatus.ONE.getIndex()){
            redpacketHandout.setType((short) EnumType.T3.getIndex());
        }
        else{
            redpacketHandout.setType((short) EnumType.T2.getIndex());
        }
        //调用红包发送业务
        try {
            return publicRedPacketService.handoutRedpacketTask(bundle,redpacketHandout);
        }
        catch (Exception e) {
            e.printStackTrace();
            DeferredResult<String> dr = new DeferredResult<>();
            dr.setResult(toJson(EnumStatus.NINETY_NINE.getIndex(), bundle.getString("system.error"), apiName, null));
            return dr;
        }
    }

    @ResponseBody
    @RequestMapping(value = "/handoutRandom",method = {RequestMethod.POST}, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public DeferredResult<String> handoutRandom(@RequestBody RedpacketHandout redpacketHandout, HttpServletRequest request){
        //获取当前登录用户信息
        UserDetails userDetails = SessionUtil.getUserDetails(request);
        //根据request 请求获取访问用户本地语言
        PackBundle bundle = LangResource.getResourceBundle(request);
        //api名称
        String apiName = "post + /api/redpacket/handoutCommon";
        //****************************************************************
        //参看其他控制器参数验证实现，并没有采用bean valid的方式，直接逐一显示验证
        //****************************************************************
        if(userDetails == null) {
            DeferredResult<String> dr = new DeferredResult<>();
            dr.setResult(toJson(EnumStatus.NINETY_EIGHT.getIndex(), bundle.getString("user.not.login"), apiName, null));
            return dr;
        }
        //平台ID不能为空
        if(StringUtil.isEmpty(redpacketHandout.getPlatformId())) {
            DeferredResult<String> dr = new DeferredResult<>();
            dr.setResult(toJson(EnumStatus.ONE.getIndex(), bundle.getString("need.param", "platformId"), apiName,null));
            return dr;
        }
        //币ID不能为空
        if(StringUtil.isEmpty(redpacketHandout.getCoinId())) {
            DeferredResult<String> dr = new DeferredResult<>();
            dr.setResult(toJson(EnumStatus.ONE.getIndex(), bundle.getString("need.param", "coinId"), apiName, null));
            return dr;
        }
        //目标用户类型不能为空
        if(StringUtil.isEmpty(redpacketHandout.getSessionType())) {
            DeferredResult<String> dr = new DeferredResult<>();
            dr.setResult(toJson(EnumStatus.ONE.getIndex(), bundle.getString("need.param", "sessionType"), apiName, null));
            return dr;
        }
        //sessionType=1,不能发个人红包，提示参数无效
        Short sessionType = redpacketHandout.getSessionType();
        if(sessionType == 1) {
            DeferredResult<String> dr = new DeferredResult<>();
            dr.setResult(toJson(EnumStatus.ONE.getIndex(), bundle.getString("need.param.invalid", "sessionType"), apiName, null));
            return dr;

        }
        //目标用户ID不能为空
        if(StringUtil.isEmpty(redpacketHandout.getSessionId())) {
            DeferredResult<String> dr = new DeferredResult<>();
            dr.setResult(toJson(EnumStatus.ONE.getIndex(), bundle.getString("need.param", "sessionId"), apiName, null));
            return dr;
        }
        //红包金额不能为空
        if(StringUtil.isEmpty(redpacketHandout.getTotalAmount())) {
            DeferredResult<String> dr = new DeferredResult<>();
            dr.setResult(toJson(EnumStatus.ONE.getIndex(), bundle.getString("need.param", "totalAmount"), apiName, null));
            return dr;
        }
        //红包数量不能为空
        if(StringUtil.isEmpty(redpacketHandout.getRedpacketCount())) {
            DeferredResult<String> dr = new DeferredResult<>();
            dr.setResult(toJson(EnumStatus.ONE.getIndex(), bundle.getString("need.param", "redpacketCount"), apiName, null));
            return dr;
        }
        //红包寄语不能为空
        if(StringUtil.isEmpty(redpacketHandout.getText())) {
            DeferredResult<String> dr = new DeferredResult<>();
            dr.setResult(toJson(EnumStatus.ONE.getIndex(), bundle.getString("need.param", "text"), apiName, null));
            return dr;
        }
        //获取登录用户userId
        String userId = userDetails.getUserId();
        redpacketHandout.setUserId(userId);
        //设置红包类型
        redpacketHandout.setType((short) EnumType.T1.getIndex());
        //调用红包发送业务
        try {
            return publicRedPacketService.handoutRedpacketTask(bundle,redpacketHandout);
        }
        catch (Exception e) {
            e.printStackTrace();
            DeferredResult<String> dr = new DeferredResult<>();
            dr.setResult(toJson(EnumStatus.NINETY_NINE.getIndex(), bundle.getString("system.error"), apiName, null));
            return dr;
        }
    }


    @ResponseBody
	@RequestMapping(value = "/grab", method = { RequestMethod.POST }, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE })
	public DeferredResult<String> grab(@RequestBody RedPacketFormBean redPacketFormBean, HttpServletRequest request) {
    	System.out.println("@RequestBody RedPacketFormBean redPacketFormBean, redPacketFormBean.getRedpacketId()...............2222...............");
		// 绑定的语言
		PackBundle packBundle = LangResource.getResourceBundle(request);
		UserDetails userDetails = SessionUtil.getUserDetails(request);
		String apiName = "GET+requestgrab";
		if (userDetails == null) {
			DeferredResult<String> dr = new DeferredResult<>();
			dr.setResult(toJson(EnumStatus.NINETY_EIGHT.getIndex(), packBundle.getString("user.not.login"), apiName, null));
			return dr;
		}
		if (StringUtil.isEmpty(redPacketFormBean.getRedpacketId())) {
			DeferredResult<String> dr = new DeferredResult<>();
			dr.setResult(toJson(EnumStatus.ONE.getIndex(), packBundle.getString("need.param", "redpacketId"), apiName,null));
			return dr;
		}
		return publicRedPacketService.grab(packBundle, redPacketFormBean.getRedpacketId() , userDetails.getUserId());
	}

	@ResponseBody
	@RequestMapping(value = "/status", method = { RequestMethod.POST }, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE })
	public DeferredResult<String> status(@RequestBody RedPacketFormBean redPacketFormBean, HttpServletRequest request) {
		System.out.println("@RequestBody RedPacketFormBean redPacketFormBean, redPacketFormBean.getRedpacketId()....."+redPacketFormBean.getRedpacketId());
//		// 绑定的语言
		PackBundle packBundle = LangResource.getResourceBundle(request);
		UserDetails userDetails = SessionUtil.getUserDetails(request);
		String apiName = "GET+requeststatus";
		if (userDetails == null) {
			DeferredResult<String> dr = new DeferredResult<>();
			dr.setResult(toJson(EnumStatus.NINETY_EIGHT.getIndex(), packBundle.getString("user.not.login"), apiName, null));
			return dr;
		}
		if (StringUtil.isEmpty(redPacketFormBean.getRedpacketId())) {
			DeferredResult<String> dr = new DeferredResult<>();
			dr.setResult(toJson(EnumStatus.ONE.getIndex(), packBundle.getString("need.param", "redpacketId"), apiName,null));
			return dr;
		}
		return publicRedPacketService.getStatus(packBundle,redPacketFormBean.getRedpacketId(), userDetails.getUserId());
	}

    @ResponseBody
    @RequestMapping(value = "/detail",method = {RequestMethod.POST}, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public DeferredResult<String> detail(@RequestBody RedpacketHandout redpacketHandout, HttpServletRequest request){
        //根据request 请求获取访问用户本地语言
        PackBundle bundle = LangResource.getResourceBundle(request);
        //api名称
        String apiName = "post + /api/redpacket/detail";
        //获取当前登录用户信息
        UserDetails userDetails = SessionUtil.getUserDetails(request);
        if(userDetails == null) {
            DeferredResult<String> dr = new DeferredResult<>();
            dr.setResult(toJson(EnumStatus.NINETY_EIGHT.getIndex(), bundle.getString("user.not.login"), apiName, null));
            return dr;
        }
        //红包ID不能为空
        String redpacketId = redpacketHandout.getRedpacketId();
        if(StringUtil.isEmpty(redpacketId)) {
            DeferredResult<String> dr = new DeferredResult<>();
            dr.setResult(toJson(EnumStatus.ONE.getIndex(), bundle.getString("need.param", "redpacketId"), apiName,null));
            return dr;
        }
        Integer page = redpacketHandout.getPage();
        Integer rows = redpacketHandout.getRows();
        String userId = userDetails.getUserId();
        //设置记录时间戳，如果未传该值默认为当前时间纳秒
        Long timestamp = redpacketHandout.getTimestamp();
        if (timestamp == null || timestamp.longValue() == 0){
            timestamp = null;
        }
        try {
            ResultData<RedpacketDto<Map<String,Object>>> resultData = publicRedPacketService.detail(bundle,userId,redpacketId,page,rows,timestamp);
            DeferredResult<String> deferred = new DeferredResult<>();
            BaseResult<Object> rst = new BaseResult<>();
            rst.setStatus(resultData.getStatus());
            rst.setError(resultData.getMessage());
            rst.setData(resultData.getData());
            deferred.setResult(JSON.toJSONString(rst));
            return deferred;
        }
        catch (Exception e) {
            e.printStackTrace();
            DeferredResult<String> dr = new DeferredResult<>();
            dr.setResult(toJson(EnumStatus.NINETY_NINE.getIndex(), bundle.getString("system.error"), apiName, null));
            return dr;
        }
    }

    @ResponseBody
    @RequestMapping(value = "/handoutDetail",method = {RequestMethod.POST}, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public DeferredResult<String> handoutDetail(@RequestBody RedpacketHandout redpacketHandout,HttpServletRequest request){
        //根据request 请求获取访问用户本地语言
        PackBundle bundle = LangResource.getResourceBundle(request);
        //api名称
        String apiName = "post + /api/redpacket/handoutDetail";
        //获取当前登录用户信息
        UserDetails userDetails = SessionUtil.getUserDetails(request);
        if(userDetails == null) {
            DeferredResult<String> dr = new DeferredResult<>();
            dr.setResult(toJson(EnumStatus.NINETY_EIGHT.getIndex(), bundle.getString("user.not.login"), apiName, null));
            return dr;
        }
        Integer page = redpacketHandout.getPage();
        Integer rows = redpacketHandout.getRows();
        String userId = userDetails.getUserId();
        //设置记录时间戳，如果未传该值默认为当前时间纳秒
        Long timestamp = redpacketHandout.getTimestamp();
        if (timestamp == null || timestamp.longValue() == 0){
            timestamp = null;
        }
        try{
            ResultData<RedpacketDto<Map<String,Object>>> resultData = publicRedPacketService.handoutDetail(bundle,userId,page,rows,timestamp);
            DeferredResult<String> deferred = new DeferredResult<>();
            BaseResult<Object> rst = new BaseResult<>();
            rst.setStatus(resultData.getStatus());
            rst.setError(resultData.getMessage());
            rst.setData(resultData.getData());
            deferred.setResult(JSON.toJSONString(rst));
            return deferred;
        }
        catch (Exception e) {
            e.printStackTrace();
            DeferredResult<String> dr = new DeferredResult<>();
            dr.setResult(toJson(EnumStatus.NINETY_NINE.getIndex(), bundle.getString("system.error"), apiName, null));
            return dr;
        }
    }

    @ResponseBody
    @RequestMapping(value = "/receiveDetail",method = {RequestMethod.POST}, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public DeferredResult<String> receiveDetail(@RequestBody RedpacketHandout redpacketHandout,HttpServletRequest request){
        //根据request 请求获取访问用户本地语言
        PackBundle bundle = LangResource.getResourceBundle(request);
        //api名称
        String apiName = "post + /api/redpacket/receiveDetail";
        //获取当前登录用户信息
        UserDetails userDetails = SessionUtil.getUserDetails(request);
        if(userDetails == null) {
            DeferredResult<String> dr = new DeferredResult<>();
            dr.setResult(toJson(EnumStatus.NINETY_EIGHT.getIndex(), bundle.getString("user.not.login"), apiName, null));
            return dr;
        }
        Integer page = redpacketHandout.getPage();
        Integer rows = redpacketHandout.getRows();
        String userId = userDetails.getUserId();
        //设置记录时间戳，如果未传该值默认为当前时间纳秒
        Long timestamp = redpacketHandout.getTimestamp();
        if (timestamp == null || timestamp.longValue() == 0){
            timestamp = null;
        }
        try{
            ResultData<RedpacketDto<Map<String,Object>>> resultData = publicRedPacketService.receiveDetail(bundle,userId,page,rows,timestamp);
            DeferredResult<String> deferred = new DeferredResult<>();
            BaseResult<Object> rst = new BaseResult<>();
            rst.setStatus(resultData.getStatus());
            rst.setError(resultData.getMessage());
            rst.setData(resultData.getData());
            deferred.setResult(JSON.toJSONString(rst));
            return deferred;
        }
        catch (Exception e) {
            e.printStackTrace();
            DeferredResult<String> dr = new DeferredResult<>();
            dr.setResult(toJson(EnumStatus.NINETY_NINE.getIndex(), bundle.getString("system.error"), apiName, null));
            return dr;
        }
    }
}
