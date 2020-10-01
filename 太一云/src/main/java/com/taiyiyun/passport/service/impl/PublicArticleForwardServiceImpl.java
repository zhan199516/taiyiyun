package com.taiyiyun.passport.service.impl;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.taiyiyun.passport.dao.IPublicArticleForwardDao;
import com.taiyiyun.passport.po.PublicArticleForward;
import com.taiyiyun.passport.service.IPublicArticleForwardService;

@Service
public class PublicArticleForwardServiceImpl implements IPublicArticleForwardService {
	
	@Resource
	private IPublicArticleForwardDao dao;

	@Override
	public int save(PublicArticleForward entity) {
		
		return dao.save(entity);
	}
	
	

}
