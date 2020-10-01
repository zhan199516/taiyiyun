package com.taiyiyun.passport.service;

import java.util.List;

import com.taiyiyun.passport.po.CodeDictionary;

public interface ICodeDictionaryService {

	public List<CodeDictionary> getByBusiness(String business);

	public  CodeDictionary getById(Integer id);

}
