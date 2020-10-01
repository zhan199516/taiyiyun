package com.taiyiyun.passport.service;

import com.taiyiyun.passport.bean.UserDetails;
import com.taiyiyun.passport.language.PackBundle;
import com.taiyiyun.passport.po.BaseResult;
import com.taiyiyun.passport.po.PublicUser;
import com.taiyiyun.passport.po.PublicUserTop;
import com.taiyiyun.passport.po.message.UserLatestTime;
import com.taiyiyun.passport.po.user.UserDto;

import java.util.List;
import java.util.Map;

public interface IPublicUserService {

	public int save(PublicUser entity);

	public PublicUser getByUserName(String userName);

	public List<PublicUser> getByUuid(String userId);

	public PublicUser getByUserId(String userId);

	public void update(PublicUser entity);

	public PublicUser getByStrictId(String uuid, String userId);

	List<UserLatestTime> getLatestUpdateTime(List<String> userList);
	
	public Map<String,Long> statUserInfo(String userId);

	public int delete(String uuid,String userId);
	
	public List<PublicUser> searchForUserList(String key);

	public List<PublicUser> getUserInfoByUserIds(List<String> userIdList);

	public PublicUser getByAppId(String appId);

	public List<PublicUser> searchForUserListByMobile(String mobile);

	public PublicUser getUnregisteredUserByMobile(String mobile);

	public BaseResult<PublicUser> createDefaultShareAccount(PackBundle bundle,UserDetails userDetails,boolean isPrize);

	public int deleteUser(PublicUser unUser);

	int updateMsgPullTime(String userId, Long lastMsgPullTime);

	PublicUserTop getUserTop(String userId);

	/**
	 * 注册自动关注指定共享号和自动入群奖励
	 * @param packBundle
	 * @param uuid
	 */
	void registerPrize(PackBundle packBundle, String uuid);

	/**
	 * 获取指定目标用户信息
	 * @param userDto
	 * @param type  0或空:获取所有 1：获取群 2：获取用户
	 * @return
	 */
	public BaseResult<Map<String,Object>> getUserInfos(UserDto userDto,Integer type);

	/**
	 * 获取指定群内的用户列表信息
	 * @param userDto
	 * @return
	 */
	public BaseResult<Map<String, Object>> getGroupUsers(UserDto userDto);
}
