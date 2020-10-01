package com.taiyiyun.passport.controller.circle;

import java.util.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.taiyiyun.passport.language.LangResource;
import com.taiyiyun.passport.language.PackBundle;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.alibaba.fastjson.JSON;
import com.taiyiyun.passport.bean.UserDetails;
import com.taiyiyun.passport.po.PublicArticle;
import com.taiyiyun.passport.po.PublicArticleForward;
import com.taiyiyun.passport.po.PublicUser;
import com.taiyiyun.passport.po.circle.Article;
import com.taiyiyun.passport.po.circle.BodyInfo;
import com.taiyiyun.passport.service.ICircleMsgService;
import com.taiyiyun.passport.service.IPublicArticleForwardService;
import com.taiyiyun.passport.service.IPublicArticleService;
import com.taiyiyun.passport.service.IPublicUserFollowerService;
import com.taiyiyun.passport.service.IPublicUserService;
import com.taiyiyun.passport.util.CacheUtil;
import com.taiyiyun.passport.util.SessionUtil;
import com.taiyiyun.passport.util.StringUtil;

/**
 * Created by okdos on 2017/7/4.
 */
@Controller
@RequestMapping("/api/circle")
@SessionAttributes(names={ApiArticleController.HOT_NEW_NAME, ApiArticleController.QUERY_NAME})
public class ApiArticleController {

    static final String HOT_NEW_NAME = "hot_new_cache";
    static final String QUERY_NAME = "query_cache";

    @Resource
    private ICircleMsgService service;
    
    @Resource
    private IPublicUserService userService;
    
    @Resource
    private IPublicArticleService articleService;
    
    @Resource
    private IPublicArticleForwardService articleForwardService;
    
    @Resource
    private IPublicUserFollowerService userFollowerService;


    private BodyInfo<List<Article>> subCache(int start, BodyInfo<List<Article>> cache){
        BodyInfo<List<Article>> bodyInfo = new BodyInfo<>();
        bodyInfo.setStatus("0");
        bodyInfo.setTag(cache.getTag());

        if(cache.getMessages().size() > start + 10){
            bodyInfo.setHasMore(true);
            bodyInfo.setMessages(cache.getMessages().subList(start, start+10));
        } else {
            bodyInfo.setHasMore(false);
            if(cache.getMessages().size() > start){
                bodyInfo.setMessages(cache.getMessages().subList(start, cache.getMessages().size()));
            } else {
                bodyInfo.setMessages(new ArrayList<Article>());
            }
        }
        return bodyInfo;
    }





    @ResponseBody
    @RequestMapping(value = "/HotNew", method= RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String GetHotNew(Integer start, String tag, Model modelMap, HttpServletRequest request, HttpSession session){

        if(start == null){
            start = 0;
        }

        boolean nullThenQuery = false;
        BodyInfo<List<Article>> cache = null;
        try{
            if(StringUtil.isEmpty(tag)){
                tag = "HOT" + (new Date()).getTime();
                nullThenQuery = true;
            } else {
                if(modelMap.asMap().containsKey(ApiArticleController.HOT_NEW_NAME)){
                    cache = (BodyInfo<List<Article>>)modelMap.asMap().get(ApiArticleController.HOT_NEW_NAME);
                }
                if(cache == null || !tag.equals(cache.getTag())){
                    nullThenQuery = true;
                }
            }

            if(nullThenQuery){
                cache = new BodyInfo<>();
                cache.setTag(tag);
                List<Article> list = service.getHotNew(request, null);

//                List<Article> newList = new ArrayList<>();
//                for(Article article: list){
//                    if(article.getArticleId().equals("23c5c18f938511e7bea000163e068d24")){
//                        newList.add(article);
//                        break;
//                    }
//                }
//                for(Article article: list){
//                    if(!article.getArticleId().equals("23c5c18f938511e7bea000163e068d24")){
//                        newList.add(article);
//                    }
//                }
//                list = newList;

                cache.setMessages(list);

                for(int i = 0; i < list.size(); i++){
                    list.get(i).setIndex(i+1);
                }

                modelMap.addAttribute(ApiArticleController.HOT_NEW_NAME, cache);
            }

            BodyInfo<List<Article>> bodyInfo = subCache(start, cache);


            return JSON.toJSONString(bodyInfo);
        }
        catch(Exception ex){
            BodyInfo<Object> error = new BodyInfo<>();
            error.setStatus("1");
            error.setErrorMsg(ex.getMessage());
            return JSON.toJSONString(error);
        }
    }


    @ResponseBody
    @RequestMapping(value = "/Query", method= RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String GetQuery(String key, Integer start, String tag, HttpServletRequest request){

        PackBundle bundle = LangResource.getResourceBundle(request);

    	UserDetails userDetails = SessionUtil.getUserDetails(request);
    	if(null == userDetails) {
    		 BodyInfo<Object> error = new BodyInfo<>();
             error.setStatus("1");
             error.setErrorMsg(bundle.getString("user.not.login"));
             return JSON.toJSONString(error);
    	}
    	
    	if (StringUtil.isEmpty(userDetails.getUserId())) {
    		BodyInfo<Object> error = new BodyInfo<>();
            error.setStatus("2");
            error.setErrorMsg(bundle.getString("user.not.find"));
            return JSON.toJSONString(error);
		}
    	
        if(StringUtil.isEmpty(key) && StringUtil.isEmpty(tag)){
            BodyInfo<Object> error = new BodyInfo<>();
            error.setStatus("3");
            error.setErrorMsg(bundle.getString("need.param", "key"));
            return JSON.toJSONString(error);
        }
        
        if(start == null){
            start = 0;
        }
        
        BodyInfo<List<Article>> cache = null;
        
        if (StringUtil.isEmpty(tag)) {
            tag = "query:" + key;
            CacheUtil.evict(ApiArticleController.QUERY_NAME + userDetails.getUserId());
        }else {
        	cache = CacheUtil.getHalfHour(ApiArticleController.QUERY_NAME + userDetails.getUserId());
			if(null == cache || !tag.equals(cache.getTag())) {
				cache = null;
			}
        }

        try{
            if(cache == null){
                cache = new BodyInfo<>();
                cache.setTag(tag);
                cache.setStatus("0");
                List<Article> list = service.getHotNew(request, key);
                cache.setMessages(list);
                for(int i = 0; i < list.size(); i++){
                    list.get(i).setIndex(i+1);
                }
                CacheUtil.putHalfHour(ApiArticleController.QUERY_NAME + userDetails.getUserId(), cache);
            }

            return JSON.toJSONString(subCache(start, cache));

        } catch(Exception ex){
            BodyInfo<Object> error = new BodyInfo<>();
            error.setStatus("1");
            error.setErrorMsg(ex.getMessage());
            return JSON.toJSONString(error);
        }

    }
    

    @ResponseBody
    @RequestMapping(value = "/ForwardArticle", method= RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String forwardArticle(String circle_id, String article_id, Integer from, Integer to, HttpServletRequest request){

        PackBundle bundle = LangResource.getResourceBundle(request);

        String apiName = "GET /ForwardArticle";

    	UserDetails userDetails = SessionUtil.getUserDetails(request);
    	if(null == userDetails) {
    		 BodyInfo<Object> error = new BodyInfo<>();
             error.setStatus("1");
             error.setErrorMsg(bundle.getString("user.not.login"));
             return JSON.toJSONString(error);
    	}
    	
    	if (StringUtil.isEmpty(userDetails.getUserId())) {
    		BodyInfo<Object> error = new BodyInfo<>();
            error.setStatus("2");
            error.setErrorMsg(bundle.getString("user.not.find"));
            return JSON.toJSONString(error);
		}
    	
        if(StringUtil.isEmpty(circle_id) || StringUtil.isEmpty(article_id) || from == null || to == null){
            BodyInfo<Object> error = new BodyInfo<>();
            error.setStatus("3");
            error.setErrorMsg(bundle.getString("need.param", "circle_id/article_id"));
            return JSON.toJSONString(error);
        }
        
        if(from.intValue() != 1 && from.intValue() != 2 && from.intValue() != 3) {
        	BodyInfo<Object> error = new BodyInfo<>();
            error.setStatus("4");
            error.setErrorMsg(bundle.getString("article.forward.dest.unsupported"));
            return JSON.toJSONString(error);
        }
        
        if(to.intValue() != 1 && to.intValue() != 2 && to.intValue() != 3) {
        	BodyInfo<Object> error = new BodyInfo<>();
            error.setStatus("5");
            error.setErrorMsg(bundle.getString("article.forward.source.unsupported"));
            return JSON.toJSONString(error);
        }
        
        try{
        	
        	PublicArticle article =  articleService.getById(article_id);
        	if(null == article || article.getOnlineStatus() == 4) {
        		BodyInfo<Object> error = new BodyInfo<>();
                error.setStatus("6");
                error.setErrorMsg(bundle.getString("article.forward.not.found"));
                return JSON.toJSONString(error);
        	}
        	
        	PublicUser user = userService.getByUserId(circle_id);
        	if(null == user) {
        		BodyInfo<Object> error = new BodyInfo<>();
                error.setStatus("7");
                error.setErrorMsg(bundle.getString("article.operator.not.found"));
                return JSON.toJSONString(error);
        	}
        	
        	PublicArticleForward entity = new PublicArticleForward();
        	entity.setArticleId(article_id);
        	entity.setCreateTime(new Date());
        	entity.setOperatorId(circle_id);
        	entity.setFromType(from);
        	entity.setToType(to);
        	articleForwardService.save(entity);
        	
        	BodyInfo<Object> success = new BodyInfo<>();
        	success.setStatus("0");
            return JSON.toJSONString(success);

        } catch(Exception ex){
        	ex.printStackTrace();
            BodyInfo<Object> error = new BodyInfo<>();
            error.setStatus("8");
            error.setErrorMsg(bundle.getString("failed.execute"));
            return JSON.toJSONString(error);
        }

    }

    @ResponseBody
    @RequestMapping(value = "/Article/Info", method= RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String getArticleInfo(String article_id, HttpServletRequest request) {
    	UserDetails userDetails = SessionUtil.getUserDetails(request);

        PackBundle bundle = LangResource.getResourceBundle(request);
    	String apiName = "GET /Article/Info";

    	if(null == userDetails) {
    		 BodyInfo<Object> error = new BodyInfo<>();
             error.setStatus("1");
             error.setErrorMsg(bundle.getString("user.not.login"));
             return JSON.toJSONString(error);
    	}
    	try {
    		PublicArticle article = articleService.getById(article_id);
    		if(null != article) {
    			PublicUser user = userService.getByUserId(article.getUserId());
    			if(null != user) {
    				PublicUser follower = userFollowerService.getMyFollower(userDetails.getUserId(),user.getId());
    				Map<String, Object> json = new HashMap<>();
    				json.put("Status", "0");
    				json.put("ErrorMsg", "");
    				json.put("PublicCircleID", user.getId());
    				json.put("ContentID", user.getId());
    				json.put("ViewerFocused", follower == null ? 0 : 1);
    				json.put("TypeId", user.getTypeId());
    				return JSON.toJSONString(json);
    			}
    		}
    		
			BodyInfo<Object> error = new BodyInfo<>();
            error.setStatus("3");
            error.setErrorMsg(bundle.getString("failed.execute"));
            return JSON.toJSONString(error);
		} catch (Exception e) {
			e.printStackTrace();
			BodyInfo<Object> error = new BodyInfo<>();
            error.setStatus("2");
            error.setErrorMsg(bundle.getString("failed.execute"));
            return JSON.toJSONString(error);
		}
    }
}
