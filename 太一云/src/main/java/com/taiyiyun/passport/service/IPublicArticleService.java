package com.taiyiyun.passport.service;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.taiyiyun.passport.mqtt.Message;
import com.taiyiyun.passport.po.PublicArticle;
import com.taiyiyun.passport.po.PublicUser;
import com.taiyiyun.passport.po.message.ArticleMessage;

public interface IPublicArticleService {

	public PublicArticle getById(String articleId);

	public PublicArticle getRichById(String articleId);
	
	public int save(PublicArticle entity);

	public List<PublicArticle> getByUserId(String userId);
	
	public Integer deleteArticles(HttpServletRequest request, String userId, PublicArticle article);

	public List<Message<ArticleMessage>>  getGenericMessages(String userId, Long start, Long end);

	public void publish(HttpServletRequest request, PublicArticle article, boolean immigrate);

	public void pushALiMessage(PublicArticle article, PublicUser user);

	public List<PublicArticle> getUnpublishOldArticle(String userId, Long start, Long end, Integer status);

	public int updateOnlineStatus(PublicArticle article);

}
