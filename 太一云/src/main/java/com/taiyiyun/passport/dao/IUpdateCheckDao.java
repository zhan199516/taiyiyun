package com.taiyiyun.passport.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.taiyiyun.passport.po.UpdateCheck;

public interface IUpdateCheckDao {
	
	public List<UpdateCheck> getCurrentUpdateCheck(@Param("deviceType") Integer deviceType, @Param("version") String version);

	UpdateCheck getData(@Param("deviceType") Integer deviceType, @Param("version") String version);

}
