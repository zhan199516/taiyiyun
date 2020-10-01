package com.taiyiyun.passport.service.impl;

import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import com.taiyiyun.passport.dao.IAccountDao;
import com.taiyiyun.passport.po.Account;
import com.taiyiyun.passport.service.IAccountService;

import java.io.Serializable;

@Service
public class AccountServiceImpl implements IAccountService {
	
	@Resource
	private IAccountDao dao;

	@Override
	public int save(Account account) {
		
		return dao.save(account);
	}

	@Override
	public Account getByUUID(String uuid) {
		
		return dao.getByUUID(uuid);
	}
	
	

}
