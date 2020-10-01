package com.taiyiyun.passport.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.taiyiyun.passport.po.BaseResult;
import com.taiyiyun.passport.po.WeConfigReq;
import com.taiyiyun.passport.po.WeiConfigRes;
import com.taiyiyun.passport.service.IWechatService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by okdos on 2017/7/31.
 */
@Controller
public class WechatController {


    @Resource
    private IWechatService weichatService;

    @ResponseBody
    @RequestMapping(value="/api/circle/Wechat", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE}, method = {RequestMethod.POST})
    public String postForConfig(@RequestBody WeConfigReq req, HttpServletRequest request){

        BaseResult<WeiConfigRes> baseResult = new BaseResult<>();

        try{
            WeiConfigRes res = weichatService.singature(req.getUrl());
            baseResult.setStatus(0);
            baseResult.setData(res);
        } catch(Exception ex){
            baseResult.setStatus(1);
            baseResult.setError(ex.getMessage());
        }

        return JSON.toJSONString(baseResult);

    }

}
