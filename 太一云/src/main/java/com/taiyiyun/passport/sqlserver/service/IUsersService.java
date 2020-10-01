package com.taiyiyun.passport.sqlserver.service;

import java.util.Map;

import com.taiyiyun.passport.exception.DefinedError;
import com.taiyiyun.passport.language.PackBundle;
import com.taiyiyun.passport.sqlserver.comm.UsersInfoBody;
import com.taiyiyun.passport.sqlserver.po.AntifakeAddressStore;
import com.taiyiyun.passport.sqlserver.po.Developer;
import com.taiyiyun.passport.sqlserver.po.Users;

public interface IUsersService {

	public boolean checkMobileUnique(String mobileSuffix, String mobilePrefix);

	public UsersInfoBody saveUser(Map<String, String> paramMap, Developer developer, AntifakeAddressStore address);

	public Users getUserByAddress(String address);

	public int updateUser(Users users);

	public Users getByMobile(String mobilePrefix, String mobileSuffix);

	public int updateUserPwd(Users users);

	public Map<String, Object> login(PackBundle bundle, Developer developer, String mobile, String password) throws DefinedError;

}
