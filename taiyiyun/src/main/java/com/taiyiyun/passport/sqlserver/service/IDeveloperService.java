package com.taiyiyun.passport.sqlserver.service;

import com.taiyiyun.passport.sqlserver.po.Developer;

public interface IDeveloperService {

	public Developer getByAppKey(String appKey);

}
