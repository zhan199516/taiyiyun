package com.taiyiyun.passport.dao;

import java.util.List;

import com.taiyiyun.passport.po.PublicArticle;
import com.taiyiyun.passport.po.immigrate.OldArticle;

public interface IOldArticleDao {
	
	public List<OldArticle> getByUUID(Long id);
	
	public int updateArticle(PublicArticle article);

}
