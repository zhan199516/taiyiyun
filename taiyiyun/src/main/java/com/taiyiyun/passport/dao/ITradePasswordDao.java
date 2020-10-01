package com.taiyiyun.passport.dao;

import com.taiyiyun.passport.po.TradePassword;

public interface ITradePasswordDao {
	
	public int save(TradePassword tradePwd);
	
	public int update(TradePassword tradePwd);
	
	public TradePassword getByUUID(String uuid);

	public int deleteById(String uuid); 

}
