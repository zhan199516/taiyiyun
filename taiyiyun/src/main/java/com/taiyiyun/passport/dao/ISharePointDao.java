package com.taiyiyun.passport.dao;

import java.math.BigDecimal;

import org.apache.ibatis.annotations.Param;

import com.taiyiyun.passport.po.SharePoint;

public interface ISharePointDao {
	
	public int save(SharePoint bean);
	
	public int deductSharePoint(@Param("uuid") String uuid, @Param("balance") BigDecimal balance);
	
	public int addSharePoint(@Param("uuid") String uuid, @Param("balance") BigDecimal balance);
	
	public SharePoint getSharePointByUUID(String uuid);

}
