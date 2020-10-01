package com.taiyiyun.passport.service;

import com.taiyiyun.passport.po.Account;

public interface IAccountService {
	
	public int save(Account account);
	
	public Account getByUUID(String uuid);

}
