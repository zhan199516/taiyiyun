package com.taiyiyun.passport.sqlserver.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.taiyiyun.passport.sqlserver.dao.IUserEntityDao;
import com.taiyiyun.passport.sqlserver.po.UserEntity;
import com.taiyiyun.passport.sqlserver.service.IUserEntityService;

@Service
public class UserEntityServiceImpl implements IUserEntityService {
	
	@Autowired
	private IUserEntityDao dao;
	
	
	@Override
	public List<UserEntity> getByUUIDAndTypeId(String uuid, int typeId) {
		Map<String, Object> params  = new HashMap<String, Object>();
		params.put("uuid", uuid);
		params.put("typeId", typeId);
		
		return dao.selectUserEntityByUUIDAndTypeId(params);
	}

}
