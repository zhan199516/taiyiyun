package com.taiyiyun.passport.sqlserver.dao;

import com.taiyiyun.passport.sqlserver.po.Users;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

/**
 * Created by nina on 2017/10/23.
 */
public interface IUsersDao {

    Users getUserFromAddress(String address);

    Users getUserByUUID(String uuid);

    void updateDefaultUserEntity(Map<String, String> params);

	List<Users> findByMobile(@Param("mobile") String mobile, @Param("mobilePrefix") String mobilePrefix);

	int save(Users bean);

	int getTotalCount();

	int updateUser(Users users);

	int updateUserPwd(Users users);
}
