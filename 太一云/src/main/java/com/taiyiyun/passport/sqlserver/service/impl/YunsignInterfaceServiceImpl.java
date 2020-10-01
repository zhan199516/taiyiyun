package com.taiyiyun.passport.sqlserver.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.taiyiyun.passport.sqlserver.dao.IYunsignInterfaceDao;
import com.taiyiyun.passport.sqlserver.po.YunsignInterface;
import com.taiyiyun.passport.sqlserver.service.IYunsignInterfaceService;

@Service
public class YunsignInterfaceServiceImpl implements IYunsignInterfaceService {
	
	@Autowired
	private IYunsignInterfaceDao dao;

	@Override
	public int save(YunsignInterface bean) {
		return dao.save(bean);
	}

}
