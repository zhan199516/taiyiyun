package com.taiyiyun.passport.service;

import com.taiyiyun.passport.po.PublicArticleStatistic;

/**
 * Created by okdos on 2017/7/3.
 */
public interface IPublicArticleStatisticService {

    PublicArticleStatistic getByArticleId(String articleId);

    PublicArticleStatistic getByUserId(String articleId);

}
