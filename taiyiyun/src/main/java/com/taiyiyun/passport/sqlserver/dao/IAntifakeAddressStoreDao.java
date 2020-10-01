package com.taiyiyun.passport.sqlserver.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.taiyiyun.passport.sqlserver.po.AntifakeAddressStore;

public interface IAntifakeAddressStoreDao {

	public List<AntifakeAddressStore> getAvailableAddress(@Param("symbol") String symbol, @Param("count") Integer count);
	
	public int changeStatusUsed(String address);

}
