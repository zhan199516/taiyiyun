package com.taiyiyun.passport.service.impl;

import javax.annotation.Resource;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.taiyiyun.passport.dao.IUserDao;
import com.taiyiyun.passport.po.User;
import com.taiyiyun.passport.service.IUserService;

@Service
public class UserServiceImpl implements IUserService {
	
	@Resource
	private IUserDao userDao;

	@Override
	public User getById(Integer id) {
		return userDao.selectByPrimaryKey(id);
	}

	@Override
	@Transactional
	public void insertAutoId(User user) {
		userDao.insertAutoId(user);
		throw new RuntimeException("测试异常");
	}

}
