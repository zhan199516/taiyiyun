package com.taiyiyun.passport.controller.circle;

import com.alibaba.fastjson.JSON;
import com.taiyiyun.passport.controller.BaseController;
import com.taiyiyun.passport.po.PublicUserFollower;
import com.taiyiyun.passport.po.PublicUserLike;
import com.taiyiyun.passport.service.IPublicUserFollowerService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * Created by okdos on 2017/6/29.
 */
@Controller
@RequestMapping("/api/circle")
public class ApiPublicUserLikeController extends BaseController {

    @Resource
    IPublicUserFollowerService service;

    @ResponseBody
    @RequestMapping(value = "/user/like", method={RequestMethod.GET}, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String getUserLike(String userId, String likeId){

        PublicUserFollower follower = service.getRecord(userId, likeId);
        if(follower == null){
            return null;
        } else {
            PublicUserLike like = new PublicUserLike();
            like.setLikeId(follower.getFollowerId());
            like.setUserId(follower.getUserId());
            like.setId(follower.getId());

            return JSON.toJSONString(like);
        }


    }

    @ResponseBody
    @RequestMapping(value = "/user/likeAdd", method={RequestMethod.POST}, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String addUserLike(@RequestBody PublicUserLike like){
        try{
            service.focusPublicUser(like.getUserId(), like.getLikeId());

            return this.toJson(0, null, "POST+likeAdd", null);
        }
        catch(Exception ex){
            return this.toJson(1, ex.toString(), "POSt+likeAdd", null);
        }
    }

    @ResponseBody
    @RequestMapping(value = "/user/likeRemove", method={RequestMethod.POST}, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String removeUserLike(@RequestBody PublicUserLike like){
        try{
            service.unfollowPublicUser(like.getUserId(), like.getLikeId());
            return this.toJson(0, null, "POST+likeAdd", null);
        }
        catch(Exception ex){
            return this.toJson(1, ex.toString(), "POSt+likeAdd", null);
        }

    }

}
