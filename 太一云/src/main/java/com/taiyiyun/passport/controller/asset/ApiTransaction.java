package com.taiyiyun.passport.controller.asset;

import com.taiyiyun.passport.bean.UserDetails;
import com.taiyiyun.passport.controller.BaseController;
import com.taiyiyun.passport.language.LangResource;
import com.taiyiyun.passport.language.PackBundle;
import com.taiyiyun.passport.po.PublicUser;
import com.taiyiyun.passport.po.asset.Trade;
import com.taiyiyun.passport.po.asset.TransferAccept;
import com.taiyiyun.passport.po.asset.TransferAnswer;
import com.taiyiyun.passport.service.IPublicUserService;
import com.taiyiyun.passport.service.ITransferService;
import com.taiyiyun.passport.util.SessionUtil;
import com.taiyiyun.passport.util.StringUtil;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

/**
 * Created by okdos on 2017/7/7.
 */
@Controller
@RequestMapping("/api/money/transaction")
public class ApiTransaction extends BaseController {

    @Resource
    ITransferService transferService;

    @Resource
    IPublicUserService userService;

    /**
     * 发起转账
     * @param apply
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value="/postMoney", method={RequestMethod.POST}, produces={MediaType.APPLICATION_JSON_UTF8_VALUE})
    public DeferredResult<String> postMoney(@RequestBody TransferAnswer apply, HttpServletRequest request){

        PackBundle bundle = LangResource.getResourceBundle(request);

        UserDetails userDetails = SessionUtil.getUserDetails(request);
        if(null == userDetails) {
            DeferredResult<String> dr = new DeferredResult<>();
            dr.setResult(toJson(1, bundle.getString("user.not.login"), "", null));
            return dr;
        }

        PublicUser user = userService.getByUserId(apply.getToUserId());

        if(user == null){
            DeferredResult<String> dr = new DeferredResult<>();
            dr.setResult(toJson(2, bundle.getString("third.trade.dest.null"), "", null));
            return dr;
        }

        apply.setToUuid(user.getUuid());
        apply.setFromUserId(userDetails.getUserId());
        apply.setFromUuid(userDetails.getUuid());
        apply.setCreateTime((new Date()).getTime());


        if(apply.getExpireTime() == 0){
            apply.setExpireTime((new Date()).getTime() + 23*60*60*1000);
        }

        return transferService.postMoney(bundle, apply);
    }

    /**
     * 确认收账
     * @param body
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value="/acceptMoney", method={RequestMethod.POST}, produces={MediaType.APPLICATION_JSON_UTF8_VALUE})
    public DeferredResult<String> acceptMoney(@RequestBody TransferAccept body, HttpServletRequest request){
        body.setToUuid(null);

        PackBundle bundle = LangResource.getResourceBundle(request);

        UserDetails userDetails = SessionUtil.getUserDetails(request);
        if(null == userDetails) {
            DeferredResult<String> dr = new DeferredResult<>();
            dr.setResult(toJson(1, bundle.getString("user.not.login"), "", null));
            return dr;
        }

        if(StringUtil.isEmptyOrBlank(userDetails.getUuid())){
            DeferredResult<String> dr = new DeferredResult<>();
            dr.setResult(toJson(2, bundle.getString("user.not.bind.uuid"), "", null));
            return dr;
        }


        if(body.getTradeNo() == null){
            DeferredResult<String> dr = new DeferredResult<>();
            dr.setResult(toJson(2, bundle.getString("need.param.error","tradeNo"), "", null));
            return dr;
        }


        body.setToUuid(userDetails.getUuid());
        body.setToUserId(userDetails.getUserId());
        body.setAcceptTime(new Date().getTime());

        return transferService.acceptMoney(bundle, body);
    }


    @ResponseBody
    @RequestMapping(value="/tradeStatus", method={RequestMethod.GET}, produces={MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String getTradeStatus(@RequestParam(value = "tradeNo") String tradeNo, HttpServletRequest request){

        PackBundle bundle = LangResource.getResourceBundle(request);

        String apiName = "get+/api/money/transaction/tradeStatus";

        UserDetails userDetails = SessionUtil.getUserDetails(request);
        if(null == userDetails) {
            return toJson(1, bundle.getString("user.not.login"), apiName, null);
        }

        try{
            Trade trade = transferService.getTradeByTradeNo(tradeNo, userDetails.getUuid(), userDetails.getUserId());
            if(trade == null){
                return toJson(2, bundle.getString("third.trade.null.dest.or.source"), apiName, null);
            }

            return toJson(0, null, apiName, trade);
        } catch(Exception ex) {
            return toJson(2, bundle.getString("failed.execute"), apiName, null, ex.getMessage());
        }
    }





    @ResponseBody
    @RequestMapping(value="/tradeHistory", method={RequestMethod.GET}, produces={MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String getTradeHistory(Long start, Long end, Integer ioType, String platformId, String coinId, Integer line, HttpServletRequest request){
        String apiName = "get+/api/money/transaction/tradeHistory";

        PackBundle bundle = LangResource.getResourceBundle(request);

        UserDetails userDetails = SessionUtil.getUserDetails(request);
        if(null == userDetails) {
            return toJson(1, bundle.getString("user.not.login"), apiName, null);
        }

        if(ioType == null){
            ioType = 3;
        }

        try{
            List<Trade> tradeList = transferService.getTradeHistory(bundle,start, end, ioType, platformId, coinId, line, userDetails.getUserId(), userDetails.getUuid());
            return toJson(0, null, apiName, tradeList);

        } catch(Exception ex){
            return toJson(2, bundle.getString("failed.execute"), apiName, null, ex.getMessage());
        }


    }


}