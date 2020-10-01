package com.taiyiyun.passport.service;

import com.taiyiyun.passport.po.PublicArticleStatistic;
import com.taiyiyun.passport.po.PublicArticleLevel;


/**
 * Created by okdos on 2017/6/29.
 */
public interface IPublicArticleLevelService {

   PublicArticleLevel getByTowId(String userId, String articleId);

   void doUp(String userId, String articleId) throws Exception;

   void doDown(String userId, String articleId) throws Exception;

   void doCancel(String userId, String articleId) throws Exception;
}
