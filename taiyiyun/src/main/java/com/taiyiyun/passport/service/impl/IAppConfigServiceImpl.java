package com.taiyiyun.passport.service.impl;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.taiyiyun.passport.dao.IAppConfigDao;
import com.taiyiyun.passport.po.AppConfig;
import com.taiyiyun.passport.service.IAppConfigService;

@Service
public class IAppConfigServiceImpl implements IAppConfigService {
	
	@Resource
	private IAppConfigDao dao;
	
	@Override
	public AppConfig getById(String appKey) {
		return dao.getById(appKey);
	}

}
