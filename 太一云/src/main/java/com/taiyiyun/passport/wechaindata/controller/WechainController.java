package com.taiyiyun.passport.wechaindata.controller;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.taiyiyun.passport.controller.BaseController;
import com.taiyiyun.passport.sqlserver.po.StatisticalData;
import com.taiyiyun.passport.wechaindata.service.IWechainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by nina on 2017/12/26.
 */
@Controller
public class WechainController extends BaseController{
    @Autowired
    private IWechainService wechainService;

    @ResponseBody
    @RequestMapping(value="/api/statisticaldata", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String statistical(String getData) {
        StatisticalData statisticalData = wechainService.queryStatisticalData();
        String dataStr = JSONObject.toJSONString(statisticalData, SerializerFeature.WriteMapNullValue);

        return getData + "(" + dataStr + ")";
    }

}
