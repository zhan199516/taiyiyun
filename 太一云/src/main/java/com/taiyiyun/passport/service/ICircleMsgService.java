package com.taiyiyun.passport.service;

import com.taiyiyun.passport.po.circle.Article;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by okdos on 2017/6/28.
 */
public interface ICircleMsgService {

    List<Article> getCircleMsgByMap(HashMap<String, Object> map, HttpServletRequest request, boolean onlyCert);

    List<Article> getHotNew(HttpServletRequest request, String key);

	List<Article> getArticleCertificateByMap(HashMap<String, Object> map, HttpServletRequest request, boolean b);

	public Map<String, Object> getBlockchainStat();
}
