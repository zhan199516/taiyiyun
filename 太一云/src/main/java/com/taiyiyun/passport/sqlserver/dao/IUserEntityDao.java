package com.taiyiyun.passport.sqlserver.dao;

import com.taiyiyun.passport.sqlserver.po.UserEntity;

import java.util.List;
import java.util.Map;

/**
 * Created by nina on 2017/10/26.
 */
public interface IUserEntityDao {

    void deleteUserEntityByEntityIdAndUUID(Map<String, String> params);

    void insertSelective(UserEntity userEntity);

    void disableUserEntityStatusZero(String uuid);

    List<UserEntity> selectUserEntityByUUIDAndTypeId(Map<String, Object> params);
}
