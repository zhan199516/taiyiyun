package com.taiyiyun.passport.dao;

import com.taiyiyun.passport.bean.CustomBean;
import com.taiyiyun.passport.po.PublicUser;
import com.taiyiyun.passport.po.PublicUserFollower;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface IPublicUserFollowerDao {

	public List<PublicUser> getFollowerByUserId(String userId);
	
	public int delete(@Param("userId") String userId, @Param("followerId") String followerId);
	
	public int save(PublicUserFollower bean);
	
	public List<CustomBean> statFollowers(String userId);

	public PublicUser getMyFollower(@Param("userId") String userId, @Param("followerId") String followerId);

	public PublicUserFollower getRecord(@Param("userId") String userId, @Param("followerId") String followerId);

	public List<PublicUser> getFocusMeUsers(@Param("userId") String userId);

	public List<PublicUserFollower> listFansPageByUserId(PublicUserFollower publicUserFollower);

	public List<PublicUserFollower> listFollowersByUserId(PublicUserFollower publicUserFollower);

	public List<PublicUserFollower> listFollowersByUserIds(PublicUserFollower publicUserFollower);

	public List<PublicUserFollower> listFansByUserIds(PublicUserFollower publicUserFollower);


}
