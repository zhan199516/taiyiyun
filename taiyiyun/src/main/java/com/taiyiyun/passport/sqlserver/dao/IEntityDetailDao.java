package com.taiyiyun.passport.sqlserver.dao;

import com.taiyiyun.passport.sqlserver.po.EntityDetail;

import java.util.Map;

/**
 * Created by nina on 2017/10/26.
 */
public interface IEntityDetailDao {
    void deleteEndityDetailByEntityId(String entityId);

    void insertSelective(EntityDetail entityDetail);

    EntityDetail selectEntityDetailHasValidDateByMobile(Map<String, String> params);
}
