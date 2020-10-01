package com.taiyiyun.passport.service;

import com.taiyiyun.passport.po.BaseResult;
import com.taiyiyun.passport.po.PublicUser;
import com.taiyiyun.passport.po.PublicUserFollower;
import com.taiyiyun.passport.po.follower.UserFollower;

import java.util.List;

public interface IPublicUserFollowerService {
	
	public List<PublicUser> getFollowerByUserId(String userId);
	
	public int focusPublicUser(String userId, String followerId);
	
	public int unfollowPublicUser(String userId, String followerId);

	public PublicUser getMyFollower(String userId, String followerId);

	public PublicUserFollower getRecord(String userId, String followerId);

	public BaseResult<List<UserFollower>> listFansPage(UserFollower userFollower);

	public BaseResult<List<UserFollower>> listFollowerPage(UserFollower userFollower);
}
