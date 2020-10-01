package com.taiyiyun.passport.service.impl;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.taiyiyun.passport.dao.IPublicArticleAccuseDao;
import com.taiyiyun.passport.po.PublicArticleAccuse;
import com.taiyiyun.passport.service.IPublicArticleAccuseService;

@Service
public class PublicArticleAccuseServiceImpl implements IPublicArticleAccuseService {
	
	@Resource
	private IPublicArticleAccuseDao dao;
	
	@Override
	public int accuseArticle(PublicArticleAccuse articleAccuse) {
		return dao.save(articleAccuse);
	}

}
