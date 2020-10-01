package com.taiyiyun.passport.service;

import org.apache.ibatis.annotations.Param;

import com.taiyiyun.passport.po.ThirdArticlePublish;

public interface IThirdArticlePublishService {
	
	public ThirdArticlePublish getByAppKey(@Param("appKey") String appKey);

}
