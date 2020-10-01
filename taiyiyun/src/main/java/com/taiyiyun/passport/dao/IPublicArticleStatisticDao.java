package com.taiyiyun.passport.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.taiyiyun.passport.po.PublicArticleStatistic;

/**
 * Created by okdos on 2017/7/3.
 */
public interface IPublicArticleStatisticDao {
    public List<PublicArticleStatistic> getByTowId(@Param("articleId") String articleId, @Param("userId") String userId);

    public void updateStatistic(@Param("articleId") String articleId, @Param("userId") String userId);

    public void insertStatistic(@Param("articleId") String articleId, @Param("userId") String userId);

    public Long getArticleStatistic(@Param("userId") String userId);
}
