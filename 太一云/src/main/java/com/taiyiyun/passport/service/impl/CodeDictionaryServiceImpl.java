package com.taiyiyun.passport.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.taiyiyun.passport.dao.ICodeDictionaryDao;
import com.taiyiyun.passport.po.CodeDictionary;
import com.taiyiyun.passport.service.ICodeDictionaryService;
import com.taiyiyun.passport.util.StringUtil;

@Service
public class CodeDictionaryServiceImpl implements ICodeDictionaryService {
	
	@Resource
	private ICodeDictionaryDao dao;
	
	@Override
	public List<CodeDictionary> getByBusiness(String business) {
		if (StringUtil.isEmpty(business)) {
			return null;
		}
		
		return dao.getByBusiness(business);
	}
	
	@Override
	public CodeDictionary getById(Integer id) {
		if (id == null || id < 0) {
			return null;
		}
		
		return dao.getById(id);
	}

}
