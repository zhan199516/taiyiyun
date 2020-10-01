package com.taiyiyun.passport.dao;

import com.taiyiyun.passport.po.User;

public interface IUserDao {
	
    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

	void insertAutoId(User user);
}