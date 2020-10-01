package com.taiyiyun.passport.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.taiyiyun.passport.po.CodeDictionary;

public interface ICodeDictionaryDao {
	
	public List<CodeDictionary> getByBusiness(@Param("business") String business);
	
	public CodeDictionary getById(@Param("id") Integer id);

}
