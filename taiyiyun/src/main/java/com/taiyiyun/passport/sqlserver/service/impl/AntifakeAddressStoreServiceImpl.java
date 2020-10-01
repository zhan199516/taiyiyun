package com.taiyiyun.passport.sqlserver.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.taiyiyun.passport.sqlserver.dao.IAntifakeAddressStoreDao;
import com.taiyiyun.passport.sqlserver.po.AntifakeAddressStore;
import com.taiyiyun.passport.sqlserver.service.IAntifakeAddressStoreService;
import com.taiyiyun.passport.util.StringUtil;

@Service
public class AntifakeAddressStoreServiceImpl implements IAntifakeAddressStoreService {
	
	@Autowired
	private IAntifakeAddressStoreDao dao;

	@Override
	public List<AntifakeAddressStore> getAvailableAddress(String symbol, Integer count) {
		if (StringUtil.isEmpty(symbol) || count == null || count <= 0) {
			return null;
		}
		return dao.getAvailableAddress(symbol, count);
	}
	
	

}
