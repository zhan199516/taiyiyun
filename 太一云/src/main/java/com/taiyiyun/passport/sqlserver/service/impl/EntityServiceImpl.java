package com.taiyiyun.passport.sqlserver.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.taiyiyun.passport.sqlserver.dao.IEntityDao;
import com.taiyiyun.passport.sqlserver.po.Entity;
import com.taiyiyun.passport.sqlserver.service.IEntityService;
import com.taiyiyun.passport.util.StringUtil;

@Service
public class EntityServiceImpl implements IEntityService {
	
	@Autowired
	private IEntityDao dao;
	
	
	@Override
	public Entity getByEntityId(String entityId) {
		return dao.getById(entityId);
	}


	@Override
	public List<Entity> getByEntityUnqueIdAndTypeId(String entityUnqueId, int typeId) {
		if (StringUtil.isEmpty(entityUnqueId) || typeId < 0) {
			return null;
		}
		return dao.getByEntityUnqueIdAndTypeId(entityUnqueId, typeId);
	}

}
