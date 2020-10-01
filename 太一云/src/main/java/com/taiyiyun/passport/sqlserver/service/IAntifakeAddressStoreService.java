package com.taiyiyun.passport.sqlserver.service;

import java.util.List;

import com.taiyiyun.passport.sqlserver.po.AntifakeAddressStore;

public interface IAntifakeAddressStoreService {
	
	public List<AntifakeAddressStore> getAvailableAddress(String symbol, Integer count);

}
