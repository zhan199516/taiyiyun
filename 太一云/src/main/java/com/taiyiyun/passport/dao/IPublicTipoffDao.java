package com.taiyiyun.passport.dao;

import com.taiyiyun.passport.po.PublicTipoff;

public interface IPublicTipoffDao {
	
	public int insert(PublicTipoff publicTipoff);

	public PublicTipoff getOneByUserAndTipoffId(PublicTipoff publicTipoff);

}
