package com.taiyiyun.passport.mongo.dao;

import com.taiyiyun.passport.mongo.po.UplinkMessageLog;

import java.util.Map;

/**
 * Created by nina on 2018/3/16.
 */
public interface IUplinkMessageLogDao {

    void insert(UplinkMessageLog uplinkMessageLog);

    UplinkMessageLog findOne(String id);

    void update(String id, Map<String, Object> params);

}
