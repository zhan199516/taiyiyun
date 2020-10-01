package com.taiyiyun.passport.dao;

import com.taiyiyun.passport.po.ThirdApp;
import com.taiyiyun.passport.po.ThirdAppExt;

import java.util.List;

public interface IThirdAppDao {
	public List<ThirdAppExt> getAll();

	public List<ThirdApp> getByUserId(String userId);

	public ThirdAppExt getBindByAppId(String appId);

	public ThirdAppExt getBindByAppKey(String appKey);

	public ThirdAppExt getOneByAppId(String appId);
}
