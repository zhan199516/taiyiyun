package com.taiyiyun.passport.dao;

import com.taiyiyun.passport.po.MqttUserPWD;
import com.taiyiyun.passport.po.PublicUser;
import com.taiyiyun.passport.po.PublicUserTop;
import com.taiyiyun.passport.po.message.UserLatestTime;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface IPublicUserDao {

	public int save(PublicUser bean);

	public PublicUser getByUserName(String userName);

	public List<PublicUser> getByUuid(String uuid);

	public PublicUser getByUserId(String userId);

	public Integer update(PublicUser entity);

	public PublicUser getByStrictId(@Param("uuid") String uuid, @Param("userId") String userId);

	public List<UserLatestTime> getLatestUpdateTime(@Param("userList") List<String> userList);

	public List<PublicUser> getByType(Integer Type);

	public int delete(@Param("uuid") String uuid, @Param("userId") String userId);

	public MqttUserPWD getMqttPwdById(String userId);

	public int newMqttPwd(MqttUserPWD mqttUserPWD);
	
	public List<PublicUser> searchForUserList(@Param("key") String key);

	public List<PublicUser> getUserInfoByUserIds(@Param("userIdList") List<String> userIdList);

	public PublicUser getByAppId(@Param("appId") String appId);

	public List<PublicUser> searchForUserListByMobile(@Param("mobile") String mobile);

	public PublicUser getUnregisteredUserByMobile(@Param("mobile") String mobile);

	public int deleteUser(PublicUser user);

	public int updateMsgPullTime(@Param("userId") String userId, @Param("lastMsgPullTime") Long lastMsgPullTime);

	public PublicUserTop getUserTop(@Param("userId") String userId);

	public List<PublicUser> getByUserIds(PublicUser publicUser);

}
