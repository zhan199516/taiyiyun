package com.taiyiyun.passport.service;

import java.util.HashMap;

/**
 * Created by okdos on 2017/7/1.
 */
public interface IArticleViewService {

    HashMap<String, Object> readArticle(String articleId, String viewId);

    String findFirstImageUrl(String content);

}
