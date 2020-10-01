package com.taiyiyun.passport.service.impl;

import java.text.SimpleDateFormat;
import java.util.HashMap;

import javax.annotation.Resource;

import com.taiyiyun.passport.po.*;
import com.taiyiyun.passport.service.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import com.taiyiyun.passport.dao.IPublicArticleReadDao;
import com.taiyiyun.passport.util.Misc;
import com.taiyiyun.passport.util.StringUtil;

import org.apache.commons.lang.StringEscapeUtils;

/**
 * Created by okdos on 2017/7/1.
 */
@Service
public class ArticleViewServiceImpl implements IArticleViewService {

    @Resource
    IPublicArticleService articleService;

    @Resource
    IPublicUserService userService;

    @Resource
    IPublicUserFollowerService publicUserFollowerService;
    
    @Resource
    IPublicArticleReadDao articleReadDao;


    /**
     * 去掉content中的无用宽高设置
     * @param content
     * @return
     */
    private String contentFix(String content){

        content = StringEscapeUtils.unescapeHtml(content);

        Document doc = Jsoup.parseBodyFragment(content);

        Elements images = doc.select("img");

        Element node = null;
        while(true){
            node = images.first();
            if(node != null){
                node.addClass("news-img");
                node.removeAttr("width");
                node.removeAttr("height");
            } else {
                break;
            }
            images = images.next();
        }



        content = doc.outerHtml();

        return content.replace("http://creditid.taiyiyun.com:9090", "");
    }

    @Override
    public String findFirstImageUrl(String content){
        content = StringEscapeUtils.unescapeHtml(content);

        Document doc = Jsoup.parseBodyFragment(content);

        Elements images = doc.select("img");
        Element node = images.first();
        if(node == null){
            return null;
        } else {
            return node.attr("src");
        }
    }

    @Override
    public HashMap<String, Object> readArticle(String articleId, String viewId) {

        HashMap<String, Object> model = new HashMap<>();

        PublicArticle article = articleService.getRichById(articleId);

        if(article != null) {
        	
        	PublicArticleRead articleRead = new PublicArticleRead();
        	articleRead.setArticleId(article.getArticleId());
        	
        	articleReadDao.updateReadCount(articleRead);
        	
            PublicUser user = StringUtil.isEmpty(viewId)? null : userService.getByUserId(viewId);

            PublicUserLike like = null;
            if(user != null){
//                if(user.getId().equals(article.getUserId())){
//                } else {
                    PublicUserFollower follower = publicUserFollowerService.getRecord(viewId, article.getUserId());
                    if(follower != null) {
                        like = new PublicUserLike();
                        like.setUserId(follower.getUserId());
                        like.setLikeId(follower.getFollowerId());
                    }
//                }
            }

            model.put("mainHtml", contentFix(article.getContent()));
            model.put("data_articleId", article.getArticleId());
            model.put("data_title", article.getTitle());
            model.put("data_abstract", article.getSummary());
            model.put("data_url", "../" + article.getUserAvatarUrl());
            model.put("data_owner", article.getUserId());
            model.put("data_original", article.getIsOriginal());
            model.put("user_name", article.getUserName());
            model.put("update_time", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(article.getUpdateTime()));

            if(article.getUserAvatarUrl() == null){
                model.put("user_head", "../resources/images/head.png");
            }
            else {
                model.put("user_head", "../" + article.getUserAvatarUrl());
            }

            if(like != null){
                model.put("data_focused", 1); //是否关注了,已关注
//            } else if(user== null || user.getId().equals(article.getUserId())){
            } else if(user== null){
                model.put("data_focused", 2); //不需要关注
            } else {
                model.put("data_focused", 0); //是否关注了
            }

            if(user != null){
                model.put("data_vid", user.getId());
                model.put("data_vname", user.getUserName());
                model.put("data_vpic", "../" + user.getThumbAvatarUrl());
            }

            model.put("data_from", Misc.getNotNullValue(article.getForwardFrom()));
        }


        return model;
    }
}
