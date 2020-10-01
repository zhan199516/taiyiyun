package com.taiyiyun.passport.dao;

import org.apache.ibatis.annotations.Param;

import com.taiyiyun.passport.po.ThirdArticlePublish;

public interface IThirdArticlePublishDao {
	
	public ThirdArticlePublish getByAppKey(@Param("appKey") String appKey);

}
