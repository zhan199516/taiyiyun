package com.taiyiyun.passport.mongo.dao.impl;

import com.taiyiyun.passport.mongo.dao.IUplinkMessageLogDao;
import com.taiyiyun.passport.mongo.po.UplinkMessageLog;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Map;

/**
 * Created by nina on 2018/3/16.
 */
@Repository
public class UplinkMessageLogDaoImpl implements IUplinkMessageLogDao {

    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public void insert(UplinkMessageLog uplinkMessageLog) {
        mongoTemplate.insert(uplinkMessageLog);
    }

    @Override
    public UplinkMessageLog findOne(String id) {
        Query query = new Query(new Criteria("_id").is(id));
        return mongoTemplate.findOne(query, UplinkMessageLog.class);
    }

    @Override
    public void update(String id, Map<String, Object> params) {
        Query query = new Query(new Criteria("_id").is(id));
        mongoTemplate.updateMulti(query, getUpdate(params), UplinkMessageLog.class);
    }

    protected Update getUpdate(Map<String, Object> params) {
        Update update = new Update();
        for(Map.Entry<String, Object> entry : params.entrySet()) {
            update.set(entry.getKey(), entry.getValue());
        }
        return update;
    }
}
