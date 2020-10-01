package com.taiyiyun.passport.service.impl;

import com.taiyiyun.passport.consts.EnumStatus;
import com.taiyiyun.passport.dao.IPublicUserBlockDao;
import com.taiyiyun.passport.dao.IPublicUserFollowerDao;
import com.taiyiyun.passport.po.BaseResult;
import com.taiyiyun.passport.po.PublicUser;
import com.taiyiyun.passport.po.PublicUserFollower;
import com.taiyiyun.passport.po.follower.UserFollower;
import com.taiyiyun.passport.service.IPublicUserFollowerService;
import com.taiyiyun.passport.util.Misc;
import com.taiyiyun.passport.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class PublicUserFollowerServiceImpl implements IPublicUserFollowerService {

	public final Logger logger = LoggerFactory.getLogger(getClass());

	@Resource
	private IPublicUserFollowerDao publicUserFollowerDao;

	@Resource
	private IPublicUserBlockDao publicUserBlockDao;

	@Override
	public List<PublicUser> getFollowerByUserId(String userId) {
		if(StringUtil.isEmpty(userId)) {
			return null;
		}
		return publicUserFollowerDao.getFollowerByUserId(userId);
	}

	/**
	 * 关注时，自动取消拉黑
	 * @param userId
	 * @param followerId
	 * @return
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public int focusPublicUser(String userId, String followerId) {

		publicUserBlockDao.delete(userId, followerId);

		PublicUserFollower bean = new PublicUserFollower();
		bean.setUserId(userId);
		bean.setFollowerId(followerId);
		
		return publicUserFollowerDao.save(bean);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public int unfollowPublicUser(String userId, String followerId) {
		
		return publicUserFollowerDao.delete(userId, followerId);
	}

	@Override
	public PublicUser getMyFollower(String userId, String followerId) {
		if(StringUtil.isEmpty(userId) || StringUtil.isEmpty(followerId)) {
			return null;
		}
		return publicUserFollowerDao.getMyFollower(userId, followerId);
	}

	@Override
	public PublicUserFollower getRecord(String userId, String followerId) {
		if(StringUtil.isEmpty(userId) || StringUtil.isEmpty(followerId)) {
			return null;
		}
		return publicUserFollowerDao.getRecord(userId, followerId);
	}

	@Override
	public BaseResult<List<UserFollower>> listFansPage(UserFollower userFollower) {
		//业务结果通用返回对象
		BaseResult<List<UserFollower>> resultData = new BaseResult<>();
		try{
			Integer rows = userFollower.getRows();
			rows = (rows == null || rows == 0) ?20:rows;
			PublicUserFollower publicUserFollower = new PublicUserFollower();
			publicUserFollower.setFollowerId(userFollower.getUserId());
			publicUserFollower.setOffset(rows);
			Long timestamp = userFollower.getTimestamp();
			if (timestamp != null && timestamp.longValue() > 0){
				timestamp = timestamp >> 20;
			}
			if (timestamp == null || timestamp.longValue() == 0){
				timestamp = null;
			}
			publicUserFollower.setId(timestamp);
			List<PublicUserFollower> listFans = publicUserFollowerDao.listFansPageByUserId(publicUserFollower);
			List<UserFollower> listResults = new ArrayList<>();
			for (PublicUserFollower publicUserFollowerTemp:listFans){
				UserFollower userFollowerTemp = new UserFollower();
				userFollowerTemp.setAvatarUrl(Misc.getServerUri(null,publicUserFollowerTemp.getAvatarUrl()));
				Long id = publicUserFollowerTemp.getId();
				id = id << 20;
				userFollowerTemp.setTimestamp(id);
				userFollowerTemp.setUserId(publicUserFollowerTemp.getUserId());
				userFollowerTemp.setUserName(publicUserFollowerTemp.getUserName());
				userFollowerTemp.setDescription(publicUserFollowerTemp.getDescription());
				listResults.add(userFollowerTemp);
			}
			resultData.setData(listResults);
			resultData.setStatus(EnumStatus.ZORO.getIndex());
			resultData.setError("success");
			return resultData;
		}
		catch (Exception e){
			logger.info("PublicUserFollowerServiceImpl.listFansPage.error:" + e.getMessage());
			resultData.setStatus(EnumStatus.NINETY_NINE.getIndex());
			resultData.setError(e.getMessage());
			return resultData;
		}
	}

	@Override
	public BaseResult<List<UserFollower>> listFollowerPage(UserFollower userFollower) {
		//业务结果通用返回对象
		BaseResult<List<UserFollower>> resultData = new BaseResult<>();
		try{
			Integer rows = userFollower.getRows();
			rows = (rows == null || rows == 0) ?20:rows;
			PublicUserFollower publicUserFollower = new PublicUserFollower();
			publicUserFollower.setUserId(userFollower.getFollowerId());
			publicUserFollower.setOffset(rows);
			Long timestamp = userFollower.getTimestamp();
			//不为空且大于0 ，右移位30
			if (timestamp != null && timestamp.longValue() > 0){
				timestamp = timestamp >> 20;
			}
			//为空或0，默认设置为空
			if (timestamp == null || timestamp.longValue() == 0){
				timestamp = null;
			}
			publicUserFollower.setId(timestamp);
			List<PublicUserFollower> listFans = publicUserFollowerDao.listFollowersByUserId(publicUserFollower);
			List<UserFollower> listResults = new ArrayList<>();
			for (PublicUserFollower publicUserFollowerTemp:listFans){
				UserFollower userFollowerTemp = new UserFollower();
				userFollowerTemp.setAvatarUrl(Misc.getServerUri(null,publicUserFollowerTemp.getAvatarUrl()));
				Long id = publicUserFollowerTemp.getId();
				id = id << 20;
				userFollowerTemp.setTimestamp(id);
				userFollowerTemp.setUserId(publicUserFollowerTemp.getFollowerId());
				userFollowerTemp.setUserName(publicUserFollowerTemp.getUserName());
				userFollowerTemp.setDescription(publicUserFollowerTemp.getDescription());
				listResults.add(userFollowerTemp);
			}
			resultData.setData(listResults);
			resultData.setStatus(EnumStatus.ZORO.getIndex());
			resultData.setError("success");
			return resultData;
		}
		catch (Exception e){
			logger.info("PublicUserFollowerServiceImpl.listFollowerPage.error:" + e.getMessage());
			resultData.setStatus(EnumStatus.NINETY_NINE.getIndex());
			resultData.setError(e.getMessage());
			return resultData;
		}
	}
}
