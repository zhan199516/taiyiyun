package com.taiyiyun.passport.service;

import com.taiyiyun.passport.po.TradePassword;

public interface ITradePasswordService {

	public void save(TradePassword tradePwd);

	public TradePassword getByUUID(String uuid);

	public int deleteById(String uuid);

	int update(TradePassword tradePwd);

}
