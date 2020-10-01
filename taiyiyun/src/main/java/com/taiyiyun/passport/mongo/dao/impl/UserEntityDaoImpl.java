package com.taiyiyun.passport.mongo.dao.impl;

import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.mongodb.DB;
import com.taiyiyun.passport.mongo.dao.IUserEntityDao;
import com.taiyiyun.passport.mongo.po.UserEntity;

@Repository
public class UserEntityDaoImpl implements IUserEntityDao {
	
	@Resource
    private MongoTemplate mongoTemplate;  
  
    @Override  
    public void _test() {  
        Set<String> colls = this.mongoTemplate.getCollectionNames();  
        for (String coll : colls) {  
            System.out.println("CollectionName=" + coll);  
        }  
        DB db = this.mongoTemplate.getDb();  
        System.out.println("db=" + db.toString());  
    }  
  
    @Override  
    public void createCollection() {  
        if (!this.mongoTemplate.collectionExists(UserEntity.class)) {  
            this.mongoTemplate.createCollection(UserEntity.class);  
        }  
    }  
  
    @Override  
    public List<UserEntity> findList(int skip, int limit) {  
        Query query = new Query();  
        query.with(new Sort(new Sort.Order(Direction.ASC, "_id")));  
        query.skip(skip).limit(limit);  
        return this.mongoTemplate.find(query, UserEntity.class);  
    }  
  
    @Override  
    public List<UserEntity> findListByAge(int age) {  
        Query query = new Query();  
        query.addCriteria(new Criteria("age").is(age));  
        return this.mongoTemplate.find(query, UserEntity.class);  
    }  
  
    @Override  
    public UserEntity findOne(String id) {  
        Query query = new Query();  
        query.addCriteria(new Criteria("_id").is(id));  
        return this.mongoTemplate.findOne(query, UserEntity.class);  
    }  
  
    @Override  
    public UserEntity findOneByUsername(String username) {  
        Query query = new Query();  
        query.addCriteria(new Criteria("name.username").is(username));  
        return this.mongoTemplate.findOne(query, UserEntity.class);  
    }  
  
    @Override  
    public void insert(UserEntity entity) {  
        this.mongoTemplate.insert(entity);  
  
    }  
  
    @Override  
    public void update(UserEntity entity) {  
        Query query = new Query();  
        query.addCriteria(new Criteria("_id").is(entity.getId()));  
        Update update = new Update();  
        update.set("age", entity.getAge());  
        update.set("special", entity.getSpecial());  
        update.set("name", entity.getName());  
        this.mongoTemplate.updateFirst(query, update, UserEntity.class);  
    }
}
