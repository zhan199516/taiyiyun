package com.taiyiyun.passport.sqlserver.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.taiyiyun.passport.sqlserver.dao.IDeveloperDao;
import com.taiyiyun.passport.sqlserver.po.Developer;
import com.taiyiyun.passport.sqlserver.service.IDeveloperService;
import com.taiyiyun.passport.util.StringUtil;

@Service
public class DeveloperServiceImpl implements IDeveloperService{
	
	@Autowired
	private IDeveloperDao dao;

	@Override
	public Developer getByAppKey(String appKey) {
		if (StringUtil.isEmpty(appKey)) {
			return null;
		}
		return dao.selectDeveloperByAppKey(appKey);
	}

}
