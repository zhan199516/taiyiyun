package com.taiyiyun.passport.service;

import java.util.List;

import com.taiyiyun.passport.po.ThirdApp;

public interface IThirdAppService {

	public List<ThirdApp> getByUserId(String userId);

}
