package com.taiyiyun.passport.service;

import com.taiyiyun.passport.po.User;

public interface IUserService {
	
	public User getById(Integer id);
	
	public void insertAutoId(User user);
}
