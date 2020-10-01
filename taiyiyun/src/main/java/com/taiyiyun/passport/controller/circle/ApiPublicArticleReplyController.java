package com.taiyiyun.passport.controller.circle;

import com.alibaba.fastjson.JSON;
import com.taiyiyun.passport.po.PublicArticleReply;
import com.taiyiyun.passport.po.PublicArticleReplyDel;
import com.taiyiyun.passport.service.IPublicArticleReplyService;
import com.taiyiyun.passport.util.StringUtil;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by okdos on 2017/6/29.
 */
@Controller
@RequestMapping("/api/circle")
public class ApiPublicArticleReplyController {

    @Resource
    IPublicArticleReplyService service;

    @ResponseBody
    @RequestMapping(value = "/article/reply", method={RequestMethod.GET}, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public List<PublicArticleReply> getRepliesByArticleId(String articleId){
        List<PublicArticleReply> list = service.getArticleReplies(articleId);

        for(PublicArticleReply reply : list){
            String pic = reply.getUserPicture();
            if(!StringUtil.isEmpty(pic)){
                pic = "../" + pic;
            }
            reply.setUserPicture(pic);
        }
        return list;
    }


    @ResponseBody
    @RequestMapping(value="/article/reply", method={RequestMethod.POST}, produces= {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String postArticleReply(@RequestBody PublicArticleReply reply){
        return JSON.toJSONString(service.insertReply(reply));
    }


    @ResponseBody
    @RequestMapping(value="/article/replyChild", method={RequestMethod.POST}, produces= {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String postArticleReplyChild(@RequestBody PublicArticleReply reply){
        if(reply.getUserId() == null || reply.getUserId() == ""){
            return "{\"code\": 1, \"error\": \"条件不正确\"}";
        } else {
            PublicArticleReply rp = service.insertReplyChild(reply);
            return JSON.toJSONString(rp);
        }
    }

    @ResponseBody
    @RequestMapping(value="/article/replyDelete", method={RequestMethod.POST}, produces= {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String postArticleReplyDelete(@RequestBody PublicArticleReplyDel del){
        Integer i = service.deleteReply(del);
        return i.toString();
    }



}
