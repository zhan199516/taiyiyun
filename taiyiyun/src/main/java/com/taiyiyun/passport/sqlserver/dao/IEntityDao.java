package com.taiyiyun.passport.sqlserver.dao;

import com.taiyiyun.passport.sqlserver.po.Entity;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

/**
 * Created by nina on 2017/10/25.
 */
public interface IEntityDao {

    List<Entity> selectEntitiesByIDCardAndUUID(Map<String, String> params);

    List<Entity> selectEntitysByUUID(String uuid);

    void deleteEntityByEntityId(String entityId);

    void insertSelective(Entity entity);

    void disableEntityStatusZero(String uuid);

    Entity selectAuthedEntityByUUID(String UUID);

    int selectEntityHasAuthed(String UUID);

	Entity getById(String entityId);

	List<Entity> getByEntityUnqueIdAndTypeId(@Param("entityUnqueId") String entityUnqueId, @Param("typeId") int typeId);

}
