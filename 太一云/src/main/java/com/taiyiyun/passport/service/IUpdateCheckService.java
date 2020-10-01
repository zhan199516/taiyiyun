package com.taiyiyun.passport.service;

import com.taiyiyun.passport.po.UpdateCheck;

public interface IUpdateCheckService {
	
	public UpdateCheck getCurrentUpdateCheck(Integer deviceType, String version);

}
