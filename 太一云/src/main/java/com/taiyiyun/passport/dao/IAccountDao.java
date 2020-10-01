package com.taiyiyun.passport.dao;

import com.taiyiyun.passport.po.Account;

public interface IAccountDao {
	
	public int save(Account account);
	
	public Account getByUUID(String uuid);

}
