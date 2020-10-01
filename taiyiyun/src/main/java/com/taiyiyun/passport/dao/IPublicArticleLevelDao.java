package com.taiyiyun.passport.dao;

import com.taiyiyun.passport.po.PublicArticleStatistic;
import com.taiyiyun.passport.po.PublicArticleLevel;
import org.apache.ibatis.annotations.Param;

public interface IPublicArticleLevelDao {

	public PublicArticleLevel getByTowId(@Param("userId") String userId, @Param("articleId") String articleId);

	public int insert(PublicArticleLevel entity);

	public int update(PublicArticleLevel entity);
}
