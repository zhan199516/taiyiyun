package com.taiyiyun.passport.sqlserver.service;

import java.util.List;

import com.taiyiyun.passport.sqlserver.po.UserEntity;

public interface IUserEntityService {

	public List<UserEntity> getByUUIDAndTypeId(String uuid, int typeId);

}
