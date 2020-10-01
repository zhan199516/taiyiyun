package com.taiyiyun.passport.service;

import com.taiyiyun.passport.po.AppVersion;

public interface IAppVersionService {
	
	public AppVersion getCurrentAppVersion(Integer deviceType, String version);

}
