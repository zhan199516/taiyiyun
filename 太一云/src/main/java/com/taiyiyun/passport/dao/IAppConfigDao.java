package com.taiyiyun.passport.dao;

import com.taiyiyun.passport.po.AppConfig;

public interface IAppConfigDao {
	
	public AppConfig getById(String appKey);

}
