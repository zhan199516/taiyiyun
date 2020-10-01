package com.taiyiyun.passport.controller.circle;

import com.alibaba.fastjson.JSON;
import com.taiyiyun.passport.po.PublicArticleLevel;
import com.taiyiyun.passport.service.IPublicArticleLevelService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

/**
 * Created by okdos on 2017/6/29.
 */
@Controller
@RequestMapping("/api/circle")
public class ApiPublicArticleLevelCotroller {
    @Resource
    IPublicArticleLevelService service;

    @ResponseBody
    @RequestMapping(value = "/article/level", method={RequestMethod.GET}, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String getLevel(String userId, String articleId){
        return JSON.toJSONString(service.getByTowId(userId, articleId));
    }

    @ResponseBody
    @RequestMapping(value = "/article/level", method={RequestMethod.POST}, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String postLevel(@RequestBody PublicArticleLevel level, HttpServletRequest request){

        HashMap<String, Object> rst = new HashMap<>();

        try{

            if(level.getLikeLevel() == 1){
                service.doUp(level.getUserId(), level.getArticleId());
            }
            else if(level.getLikeLevel() == 2){
                service.doDown(level.getUserId(), level.getArticleId());
            }
            else {
                service.doCancel(level.getUserId(), level.getArticleId());
            }

            rst.put("status", 0);
            rst.put("message", "");
        }
        catch(Exception ex){
            rst.put("status", 1);
            rst.put("message", ex);
        }

        return JSON.toJSONString(rst);
    }

}
