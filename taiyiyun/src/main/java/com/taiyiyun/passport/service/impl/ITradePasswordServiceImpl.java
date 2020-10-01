package com.taiyiyun.passport.service.impl;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.taiyiyun.passport.dao.ITradePasswordDao;
import com.taiyiyun.passport.po.TradePassword;
import com.taiyiyun.passport.service.ITradePasswordService;

@Service
public class ITradePasswordServiceImpl implements ITradePasswordService {
	
	@Resource
	private ITradePasswordDao dao;

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void save(TradePassword tradePwd) {
		dao.deleteById(tradePwd.getUuid());
		dao.save(tradePwd);
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public int update(TradePassword tradePwd) {
		return dao.update(tradePwd);
	}

	@Override
	public TradePassword getByUUID(String uuid) {
		
		return dao.getByUUID(uuid);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public int deleteById(String uuid) {
		
		return dao.deleteById(uuid);
	}

}
