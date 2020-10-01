package com.taiyiyun.passport.service.impl;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.taiyiyun.passport.dao.IThirdArticlePublishDao;
import com.taiyiyun.passport.po.ThirdArticlePublish;
import com.taiyiyun.passport.service.IThirdArticlePublishService;

@Service
public class ThirdArticlePublishServiceImpl implements IThirdArticlePublishService {
	@Resource
	private IThirdArticlePublishDao dao;

	@Override
	public ThirdArticlePublish getByAppKey(String appKey) {
		
		return dao.getByAppKey(appKey);
	}

}
