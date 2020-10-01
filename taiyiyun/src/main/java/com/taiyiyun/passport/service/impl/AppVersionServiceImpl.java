package com.taiyiyun.passport.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.taiyiyun.passport.dao.IAppVersionDao;
import com.taiyiyun.passport.po.AppVersion;
import com.taiyiyun.passport.service.IAppVersionService;
import com.taiyiyun.passport.util.StringUtil;

@Service
public class AppVersionServiceImpl implements IAppVersionService {
	
	@Resource
	private IAppVersionDao dao;

	@Override
	public AppVersion getCurrentAppVersion(Integer deviceType, String version) {
		if(null == deviceType || StringUtil.isEmpty(version)) {
			return null;
		}
		List<AppVersion> appVersions = dao.getCurrentAppVersion(deviceType, version);
		if( null != appVersions && appVersions.size() > 0) {
			return appVersions.get(0);
		}
		return null;
	}
	
	

}
