package com.taiyiyun.passport.dao;

import java.util.HashMap;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.taiyiyun.passport.bean.CustomBean;
import com.taiyiyun.passport.po.PublicArticle;

public interface IPublicArticleDao {
	
	public PublicArticle getById(String articleId);

	public PublicArticle getRichById(String articleId);

	void saveBefore(PublicArticle entity);

	public int save(PublicArticle entity);
	
	public List<PublicArticle> getByUserId(String userId);
	
	public Integer deleteArticles(@Param("userId") String userId, @Param("articleIds") List<String> articleIds);

	List<PublicArticle> getMessagesByMap(HashMap<String, Object> map);

	List<PublicArticle> getHotMessage(@Param("key") String key);

	public int updateOnlineStatus(PublicArticle article);

	public List<CustomBean> getArticleStatus();

}
