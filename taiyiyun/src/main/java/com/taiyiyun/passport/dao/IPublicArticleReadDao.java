package com.taiyiyun.passport.dao;

import com.taiyiyun.passport.po.PublicArticleRead;

public interface IPublicArticleReadDao {
	
	public int updateReadCount(PublicArticleRead articleRead);

}
