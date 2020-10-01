package com.taiyiyun.passport.controller.circle;

import com.alibaba.fastjson.JSON;
import com.taiyiyun.passport.service.IPublicArticleStatisticService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * Created by okdos on 2017/7/3.
 */
@Controller
@RequestMapping("/api/circle")
public class ApiPublicArticleStatisticController {

    @Resource
    IPublicArticleStatisticService service;

    @ResponseBody
    @RequestMapping(value = "/article/levelAmount", method={RequestMethod.GET}, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String levelAmount(String articleId, String userId){
        if(articleId != null){
            return JSON.toJSONString(service.getByArticleId(articleId));
        }
        else if(userId != null){
            return JSON.toJSONString(service.getByUserId(userId));
        } else {
            return "";
        }
    }

}
