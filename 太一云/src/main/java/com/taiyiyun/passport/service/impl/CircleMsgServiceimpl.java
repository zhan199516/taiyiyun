package com.taiyiyun.passport.service.impl;

import java.util.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.taiyiyun.passport.exception.DefinedError;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.taiyiyun.passport.bean.CustomBean;
import com.taiyiyun.passport.consts.Config;
import com.taiyiyun.passport.dao.IPublicArticleDao;
import com.taiyiyun.passport.dao.IPublicArticleStatisticDao;
import com.taiyiyun.passport.dao.IPublicUserDao;
import com.taiyiyun.passport.po.PublicArticle;
import com.taiyiyun.passport.po.PublicArticleStatistic;
import com.taiyiyun.passport.po.PublicUser;
import com.taiyiyun.passport.po.circle.Article;
import com.taiyiyun.passport.po.circle.ArticleContent;
import com.taiyiyun.passport.service.ICircleMsgService;
import com.taiyiyun.passport.util.HttpClientUtil;
import com.taiyiyun.passport.util.Misc;
import com.taiyiyun.passport.util.StringUtil;

/**
 * Created by okdos on 2017/6/28.
 */

@Service
public class CircleMsgServiceimpl implements ICircleMsgService {

    @Resource
    IPublicArticleDao dao;
    
    @Resource
    private IPublicUserDao userDao;
    
    @Resource
    private IPublicArticleStatisticDao articleStatisticDao;


    private Article map(PublicArticle source, HttpServletRequest request, boolean onlyCert){
        Article article = new Article();
        ArticleContent content = new ArticleContent();
        
    	
    	PublicUser user = userDao.getByUserId(source.getUserId());
    	if(null == user || null == article) {
    		return null;
    	}
    	List<PublicArticleStatistic> statusList = articleStatisticDao.getByTowId(source.getArticleId(), null);
    	
    	if(null != statusList && statusList.size() == 1) {
    		PublicArticleStatistic status = statusList.get(0);
    		article.setDownCount(0);
    		article.setUpCount(status.getUpCount());
    		article.setCommentCount(status.getReplyCount());
    		article.setReadCount(status.getReadCount());
    		article.setShareCount(status.getForwardCount());
    	}
    	
        content.setText(Misc.getNotNullValue(source.getSummary()));
        content.setTitle(Misc.getNotNullValue(source.getTitle()));
        content.setImage("");
        content.setImageType("");
//        content.setUrl(); //url 原始的不处理
        content.setImageUrl("");
        
        if(!onlyCert){
            try{
                String urls = source.getThumbImg();
                if(urls != null && !urls.isEmpty()){
                    List<String> ls =  JSON.parseArray(urls, String.class);
                    if(ls.size() > 0){
                        content.setImageUrl(Misc.getServerUri(request, ls.get(0)));
                    }
                }
            } catch(Exception ex){
            }
        }

        article.setArticleId(source.getArticleId());
        article.setPublicCircleID(source.getUserId());
        article.setPushTitle(source.getTitle());
        article.setSendMQTime(source.getUpdateTime().getTime());
        article.setPublicCircleName(source.getUserName());
        article.setStatus(source.getOnlineStatus());
        article.setType(1);
        article.setVersion(1);
        article.setContent(content);
        article.setIsOriginal(source.getIsOriginal());
        article.setPublicCircleImg(Misc.getServerUri(request, user.getAvatarUrl()));
        
        return article;
    }


    @Override
    public List<Article> getCircleMsgByMap(HashMap<String, Object> map, HttpServletRequest request, boolean onlyCert) {
        List<PublicArticle> plist = dao.getMessagesByMap(map);
        List<Article> alist = new ArrayList<>();
        for (PublicArticle aPlist : plist) {
        	Article article = map(aPlist, request, onlyCert);
        	//正常article显示出来
        	if(null != article) {
        		if(null != article.getContent()){
        			ArticleContent content = article.getContent();

        			String thumbImg = aPlist.getThumbImg();
        			if(!StringUtil.isEmpty(thumbImg) && !thumbImg.startsWith("[")){
        				thumbImg = JSON.toJSONString(Arrays.asList(thumbImg));
					}

					content.setType(Misc.getArticleType(thumbImg));

					content.setImageUrls(Misc.parseJsonArrayToList(thumbImg, request));

					content.setImage(null);
					content.setImageType(null);
					content.setImageUrl(null);
				}
				if(article.getDownCount() <= 5) {//超过5个踩就不显示出来
					alist.add(article);
				}
        	}
        }
        return alist;
    }

    @Override
    public List<Article> getHotNew(HttpServletRequest request, String key) {
        List<PublicArticle> plist = dao.getHotMessage(key);
        List<Article> alist =  new ArrayList<>();
        
        for (PublicArticle aPlist : plist) {
        	Article article = map(aPlist, request, false);
        	if(null != article) {
        		if(StringUtil.isNotEmpty(article.getPublicCircleImg())) {
        			article.setPublicCircleImg(article.getPublicCircleImg());
        		}else {
        			article.setPublicCircleImg("");
        		}
        		alist.add(article);
        	}
        }
        
        return alist;
    }
    
    @Override
    public List<Article> getArticleCertificateByMap(HashMap<String, Object> map, HttpServletRequest request, boolean onlyCert) {
        List<PublicArticle> plist = dao.getMessagesByMap(map);
        List<Article> alist = new ArrayList<>();
        for (PublicArticle aPlist : plist) {
        	Article article = map(aPlist, request, onlyCert);
        	if(null != article) {
        		article.setCommentCount(null);
        		article.setDownCount(null);
        		article.setReadCount(null);
        		article.setShareCount(null);
        		article.setUpCount(null);
        		article.setType(null);
        		article.setIsOriginal(null);
        		article.setPushTitle(null);
        		article.setStatus(null);
        		article.setArticleHash(Misc.getNotNullValue(aPlist.getArticleHash()));
        		
        		article.getContent().setType(null);
				String thumbImg = aPlist.getThumbImg();
				if(!StringUtil.isEmpty(thumbImg) && !thumbImg.startsWith("[")){
					thumbImg = JSON.toJSONString(Arrays.asList(thumbImg));
				}
				article.getContent().setImageUrls(Misc.parseJsonArrayToList(thumbImg, request));

        		article.getContent().setImage(null);
        		article.getContent().setImageType(null);
        		article.getContent().setImageUrl(null);
        		alist.add(article);
        	}
        }
        return alist;
    }


	@Override
	public Map<String, Object> getBlockchainStat() {
		Map<String, Object> dataMap = new HashMap<>();
		dataMap.put("allCount", 0);
		dataMap.put("PersionCount", 0);
		dataMap.put("TodayCount", 0);
		dataMap.put("TodayPersionCount", 0);
		dataMap.put("height", 0);
		dataMap.put("txs", 0);
		dataMap.put("users", 0);
		dataMap.put("TotalArticleCount", 0);
		dataMap.put("TotalAuthorCount", 0);
		List<CustomBean> datas = dao.getArticleStatus();
		if(null != datas) {
			for(CustomBean bean : datas) {
				if(null != bean) {
					if("totalArticle".equals(bean.getName())) {
						dataMap.put("TotalArticleCount", Misc.parseInt(bean.getValue()));
					}
					if("totalAuthor".equals(bean.getName())) {
						dataMap.put("TotalAuthorCount", Misc.parseInt(bean.getValue()));
					}
				}
			}
		}
		String blockUrl = Config.get("block.chain.statistics.url");
		if(StringUtil.isNotEmpty(blockUrl)) {
			try{
				String responseText = HttpClientUtil.doGet(blockUrl, null);
				if(StringUtil.isNotEmpty(responseText)) {
					JSONObject dataJson = JSONObject.parseObject(responseText);
					if(StringUtil.isEmpty(dataJson.get("Message"))) {
						dataMap.put("allCount", dataJson.getInteger("allCount"));
						dataMap.put("PersionCount", dataJson.getInteger("PersionCount"));
						dataMap.put("TodayCount", dataJson.getInteger("TodayCount"));
						dataMap.put("TodayPersionCount", dataJson.getInteger("TodayPersionCount"));
						dataMap.put("height", dataJson.getInteger("height"));
						dataMap.put("txs", dataJson.getInteger("txs"));
						dataMap.put("users", dataJson.getInteger("users"));
					}
				}
			} catch (DefinedError ex){
				ex.printStackTrace();
			}
		}
		
		return dataMap;
	}
}
