package com.taiyiyun.passport.controller.asset;

import com.alibaba.fastjson.JSONObject;
import com.taiyiyun.passport.bean.UserDetails;
import com.taiyiyun.passport.controller.BaseController;
import com.taiyiyun.passport.exception.DefinedError;
import com.taiyiyun.passport.language.LangResource;
import com.taiyiyun.passport.language.PackBundle;
import com.taiyiyun.passport.po.asset.CoinPlatform;
import com.taiyiyun.passport.po.asset.Fee;
import com.taiyiyun.passport.service.IThirdAppUserBindService;
import com.taiyiyun.passport.service.transfer.AssetCache;
import com.taiyiyun.passport.transfer.Answer.CoinInfo;
import com.taiyiyun.passport.transfer.yuanbao.IYuanBaoTransferAccounts;
import com.taiyiyun.passport.util.SessionUtil;
import com.taiyiyun.passport.util.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by okdos on 2017/7/7.
 */

@Controller
@RequestMapping("/api/money")
public class ApiAsset extends BaseController {

    public final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    IThirdAppUserBindService thirdAppUserBindService;

    @Resource
    IYuanBaoTransferAccounts accounts;


//    public static class Person{
//        BigDecimal a;
//        BigDecimal b;
//
//        public BigDecimal getA() {
//            return a;
//        }
//
//        public void setA(BigDecimal a) {
//            this.a = a;
//        }
//
//        public BigDecimal getB() {
//            return b;
//        }
//
//        public void setB(BigDecimal b) {
//            this.b = b;
//        }
//    }

    /**
     *  获取绑定信息 完成
     * @param request 用于获取session的参数
     * @return JSON序列化的字符串
     */
    @ResponseBody
    @RequestMapping(value="/asset/bind", method={RequestMethod.GET}, produces={MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String getBindAsset(HttpServletRequest request){

        //多语言版本的例子
        PackBundle bundle = LangResource.getResourceBundle(request);

        String apiName = "get + api/money/bind";
        UserDetails userDetails = SessionUtil.getUserDetails(request);
        if(null == userDetails) {
            return toJson(1, bundle.getString("user.not.login"), apiName, new ArrayList<Object>());
        }

        try{
            List<CoinPlatform> list =  thirdAppUserBindService.getCoinPlatform(bundle, userDetails.getUuid());
            return toJson(0, "", apiName, list);
        } catch(Exception ex){
            return toJson(1, bundle.getString("failed.execute"), apiName, null, ex.getMessage());
        }
    }


    /**
     * 获取手续费
     * @return
     */
    @ResponseBody
    @RequestMapping(value="/askfee", method={RequestMethod.POST}, produces={MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String getFee(@RequestBody String input ,HttpServletRequest request ){

        PackBundle bundle = LangResource.getResourceBundle(request);

        String apiName = "post + api/money/askfee";
        UserDetails userDetails = SessionUtil.getUserDetails(request);
        if(null == userDetails) {
            return toJson(1, bundle.getString("user.not.login"), apiName, new ArrayList<Object>());
        }

        try{
            logger.info("api/money/askfee request param：" + input);
            JSONObject body = JSONObject.parseObject(input);
            String uuid = userDetails.getUuid();
            String platformId = body.getString("platformId");
            String coinId = body.getString("coinId");
            BigDecimal amount = body.getBigDecimal("amount");
            AssetCache assetCache = AssetCache.getInstance();
            CoinInfo coin = assetCache.getCoin(platformId, coinId);
            if(coin == null) {
                assetCache.refresh();
                coin = assetCache.getCoin(platformId, coinId);
            }
//            if(amount.compareTo(coin.getQuota().multiply(new BigDecimal(100))) < 0) {
//                //errorMsg = "转账金额不能小于" + tempQuota.multiply(new BigDecimal(100)).toString();
//                String errorMsg = bundle.getString("transfer.postmoney.check_amountvalid", coin.getQuota().multiply(new BigDecimal(100)).toString());
//                return toJson(1, errorMsg, apiName, null);
//            }
            if(amount.compareTo(coin.getQuota()) < 0) {
                //errorMsg = "转账金额不能小于" + tempQuota.multiply(new BigDecimal(100)).toString();
                String errorMsg = bundle.getString("transfer.postmoney.check_amountvalid", coin.getQuota().toString());
                return toJson(1, errorMsg, apiName, null);
            }

            if(StringUtil.isEmptyOrBlank(platformId) || StringUtil.isEmptyOrBlank(coinId) || null == amount ){
                return toJson(2, bundle.getString("need.param.error"), apiName, null);
            }

            Fee fee = thirdAppUserBindService.getFee(bundle, uuid, platformId, coinId, amount);

            if(fee != null){
                return toJson(0, "", apiName, fee);
            } else {
                return toJson(2, bundle.getString("third.trade.noFind"), apiName, null);
            }

        }
        catch(DefinedError.ThirdErrorException ex){
            return toJson(2, bundle.getString("third.trade.error.again"), apiName, null, ex.getMessage());
        }
        catch(DefinedError.ThirdUserKeyInvalid ex){
            return toJson(2, bundle.getString("third.trade.bind.expired"), apiName, null, ex.getMessage());
        }
        catch(Exception ex){
            return toJson(2, bundle.getString("failed.execute"), apiName, null, ex.getMessage());
        }
    }

}



