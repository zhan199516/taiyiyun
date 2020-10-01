package com.taiyiyun.passport.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.taiyiyun.passport.po.AppVersion;

public interface IAppVersionDao {
	
	public List<AppVersion> getCurrentAppVersion(@Param("deviceType") Integer deviceType, @Param("version") String version);

}
