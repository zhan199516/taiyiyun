package com.taiyiyun.passport.sqlserver.service;

import java.util.List;

import com.taiyiyun.passport.sqlserver.po.Entity;

public interface IEntityService {

	public Entity getByEntityId(String entityId);

	public List<Entity> getByEntityUnqueIdAndTypeId(String entityUnqueId, int typeId);

}
