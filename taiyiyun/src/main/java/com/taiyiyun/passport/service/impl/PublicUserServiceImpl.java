package com.taiyiyun.passport.service.impl;

import com.alibaba.fastjson.JSON;
import com.taiyiyun.passport.bean.CustomBean;
import com.taiyiyun.passport.bean.GlobleMobile;
import com.taiyiyun.passport.bean.UserCache;
import com.taiyiyun.passport.bean.UserDetails;
import com.taiyiyun.passport.consts.ClientID;
import com.taiyiyun.passport.consts.Config;
import com.taiyiyun.passport.consts.Const;
import com.taiyiyun.passport.consts.EnumStatus;
import com.taiyiyun.passport.dao.IAccountDao;
import com.taiyiyun.passport.dao.IPublicArticleStatisticDao;
import com.taiyiyun.passport.dao.IPublicUserDao;
import com.taiyiyun.passport.dao.IPublicUserFollowerDao;
import com.taiyiyun.passport.dao.group.IGroupDao;
import com.taiyiyun.passport.dao.group.IGroupMemberDao;
import com.taiyiyun.passport.language.PackBundle;
import com.taiyiyun.passport.po.*;
import com.taiyiyun.passport.po.group.AddGroupUserParam;
import com.taiyiyun.passport.po.group.Group;
import com.taiyiyun.passport.po.group.GroupMember;
import com.taiyiyun.passport.po.message.UserLatestTime;
import com.taiyiyun.passport.po.user.UserDto;
import com.taiyiyun.passport.service.IPublicUserFollowerService;
import com.taiyiyun.passport.service.IPublicUserService;
import com.taiyiyun.passport.service.group.IGroupService;
import com.taiyiyun.passport.util.CacheUtil;
import com.taiyiyun.passport.util.Misc;
import com.taiyiyun.passport.util.RandomUtil;
import com.taiyiyun.passport.util.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.*;

@Service
public class PublicUserServiceImpl implements IPublicUserService {

	public final Logger logger = LoggerFactory.getLogger(getClass());
	@Resource
	private IPublicUserDao dao;
	
	@Resource
	private IPublicArticleStatisticDao statisticDao; 
	
	@Resource
	private IPublicUserFollowerDao userFollowerDao;
	
	@Resource
	private IAccountDao accountDao;

	@Resource
	private IGroupDao groupDao;

	@Resource
	private IGroupMemberDao groupMemberDao;

	@Resource
	private IGroupService groupService;

	@Resource
	private IPublicUserFollowerService publicUserFollowerService;

	@Resource
	private IPublicUserFollowerDao publicUserFollowerDao;

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public int save(PublicUser entity) {
		String mobile = entity.getMobile();
		GlobleMobile globleMobile = new GlobleMobile(mobile);
		entity.setMobile(globleMobile.getMobile());
		entity.setMobilePrefix(globleMobile.getMobilePrefix());
		int count = dao.save(entity);
		Account account = accountDao.getByUUID(entity.getUuid());
		if(null != account) {
			PublicUserFollower follower = new PublicUserFollower();
			follower.setFollowerId(account.getRecommendUserId());
			follower.setUserId(entity.getId());
			userFollowerDao.save(follower);
		}
		//判断客户端id，为空走原始业务流程否则走新业务流程
		String clientId = entity.getClientId();
		List<PublicUser> fUsers = null;
		if (StringUtil.isEmpty(clientId)) {
			fUsers = dao.getByType(1);

		}
		else if (clientId.equals(ClientID.FWP.getId())){
			//食品护照查询类型为2
			fUsers = dao.getByType(2);
		}
		//其他护照继续增加相应逻辑代码
		//add yao code
		if (null != fUsers && fUsers.size() > 0) {
			for (PublicUser fUser : fUsers) {
				PublicUserFollower follower = new PublicUserFollower();
				follower.setFollowerId(fUser.getId());
				follower.setUserId(entity.getId());
				userFollowerDao.save(follower);
			}
		}
		
		return count;
	}

	@Override
	public PublicUser getByUserName(String userName) {
		PublicUser publicUser = dao.getByUserName(userName);
		if(publicUser == null){
			return null;
		}
		String mobilePrefix = publicUser.getMobilePrefix();
		if(StringUtils.isNotEmpty(mobilePrefix) && !StringUtils.equalsIgnoreCase(mobilePrefix, "86")) {
			publicUser.setMobile(mobilePrefix + "-" + publicUser.getMobile());
		}
		return publicUser;
	}

	@Override
	public List<PublicUser> getByUuid(String uuid) {
		List<PublicUser> userList = dao.getByUuid(uuid);
		if(userList != null && userList.size() > 0) {
			for(PublicUser user : userList) {
				String mobilePrefix = user.getMobilePrefix();
				if(StringUtils.isNotEmpty(mobilePrefix) && !StringUtils.equalsIgnoreCase(mobilePrefix, "86")) {
					user.setMobile(mobilePrefix + "-" + user.getMobile());
				}
			}
		}
		return userList;
	}

	@Override
	public PublicUser getByUserId(String userId) {
		PublicUser user = dao.getByUserId(userId);
		if(user == null){
			return null;
		}
		String mobilePrefix = user.getMobilePrefix();
		if(StringUtils.isNotEmpty(mobilePrefix) && !StringUtils.equalsIgnoreCase(mobilePrefix, "86")) {
			user.setMobile(mobilePrefix + "-" + user.getMobile());
		}
		return user;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void update(PublicUser entity) {
		String mobile = entity.getMobile();
		GlobleMobile globleMobile = new GlobleMobile(mobile);
		entity.setMobile(globleMobile.getMobile());
		entity.setMobilePrefix(globleMobile.getMobilePrefix());
		dao.update(entity);
	}

	@Override
	public PublicUser getByStrictId(String uuid, String userId) {
		PublicUser user = dao.getByStrictId(uuid, userId);
		if(user == null){
			return null;
		}
		String mobilePrefix = user.getMobilePrefix();
		if(StringUtils.isNotEmpty(mobilePrefix) && !StringUtils.equalsIgnoreCase(mobilePrefix, "86")) {
			user.setMobile(mobilePrefix + "-" + user.getMobile());
		}
		return user;
	}
	
	

	@Override
	public List<UserLatestTime> getLatestUpdateTime(List<String> userList) {
		if(null == userList || userList.size() <= 0) {
			return null;
		}
		return dao.getLatestUpdateTime(userList);
	}

	@Override
	public Map<String, Long> statUserInfo(String userId) {
		Map<String, Long> dataMap = new HashMap<>();
		List<CustomBean> datas = userFollowerDao.statFollowers(userId);
		if(null != datas) {
			for (Iterator<CustomBean> it = datas.iterator(); it.hasNext();) {
				CustomBean customBean = it.next();
				dataMap.put(customBean.getName(),StringUtil.isNotEmpty(customBean.getValue())? Long.parseLong(customBean.getValue()) : 0);
			}
		}
		
		Long readCount = 0L;
		Long upCount = 0L;
		Long forwardCount = 0L;
		Long replyCount = 0L;
		Long articleCount = 0L;
	    
	    articleCount = statisticDao.getArticleStatistic(userId);
	    
		List<PublicArticleStatistic> stats = statisticDao.getByTowId(null, userId);
		if(stats != null && stats.size() > 0) {
			for(PublicArticleStatistic statistic : stats) {
				if(statistic.getReadCount() != null) {
					readCount = readCount + statistic.getReadCount();
				}
				if(statistic.getUpCount() != null) {
					upCount = upCount + statistic.getUpCount();
				}
				if(statistic.getForwardCount() != null) {
					forwardCount = forwardCount + statistic.getForwardCount();
				}
				if(statistic.getReplyCount() != null) {
					replyCount = replyCount + statistic.getReplyCount();
				}
				
			}
			
		}
		dataMap.put("read", readCount);
		dataMap.put("thumbUp", upCount);
		dataMap.put("follow", forwardCount);
		dataMap.put("shareCount", forwardCount);
		dataMap.put("replay", replyCount);
		dataMap.put("article", articleCount);
		dataMap.put("thumbDown", 0L);
		return dataMap;
	}

	@Override
	public int delete(String uuid, String userId) {
		return dao.delete(uuid, userId);
	}

	@Override
	public List<PublicUser> searchForUserList(String key) {
		if(StringUtil.isEmpty(key)) {
			return null;
		}
		List<PublicUser> userList = dao.searchForUserList(key);
		if(userList != null && userList.size() > 0) {
			for(PublicUser user : userList) {
				String mobilePrefix = user.getMobilePrefix();
				if(StringUtils.isNotEmpty(mobilePrefix) && !StringUtils.equalsIgnoreCase(mobilePrefix, "86")) {
					user.setMobile(mobilePrefix + "-" + user.getMobile());
				}
			}
		}
		return userList;
	}

	@Override
	public List<PublicUser> getUserInfoByUserIds(List<String> userIdList) {
		if(null == userIdList || userIdList.size() <= 0) {
			return null;
		}
		List<PublicUser> userList = dao.getUserInfoByUserIds(userIdList);
		if(userList != null && userList.size() > 0) {
			for(PublicUser user : userList) {
				String mobilePrefix = user.getMobilePrefix();
				if(StringUtils.isNotEmpty(mobilePrefix) && !StringUtils.equalsIgnoreCase(mobilePrefix, "86")) {
					user.setMobile(mobilePrefix + "-" + user.getMobile());
				}
			}
		}
		return userList;
	}

	@Override
	public PublicUser getByAppId(String appId) {
		PublicUser user = dao.getByAppId(appId);
		String mobilePrefix = user.getMobilePrefix();
		if(StringUtils.isNotEmpty(mobilePrefix) && !StringUtils.equalsIgnoreCase(mobilePrefix, "86")) {
			user.setMobile(mobilePrefix + "-" + user.getMobile());
		}
		return user;
	}

	@Override
	public List<PublicUser> searchForUserListByMobile(String mobile) {
		if(StringUtil.isEmpty(mobile)) {
			return null;
		}
		GlobleMobile globleMobile = new GlobleMobile(mobile);
		List<PublicUser> userList = dao.searchForUserListByMobile(globleMobile.getMobile());
		if(userList != null && userList.size() > 0) {
			for(PublicUser user : userList) {
				String mobilePrefix = user.getMobilePrefix();
				if(StringUtils.isNotEmpty(mobilePrefix) && !StringUtils.equalsIgnoreCase(mobilePrefix, "86")) {
					user.setMobile(mobilePrefix + "-" + user.getMobile());
				}
			}
		}
		return userList;
	}

	@Override
	public PublicUser getUnregisteredUserByMobile(String mobile) {
		if(StringUtil.isEmpty(mobile)) {
			return null;
		}
		GlobleMobile globleMobile = new GlobleMobile(mobile);
		PublicUser user = dao.getUnregisteredUserByMobile(globleMobile.getMobile());
		if(user == null){
			return null;
		}
		String mobilePrefix = user.getMobilePrefix();
		if(StringUtils.isNotEmpty(mobilePrefix) && !StringUtils.equalsIgnoreCase(mobilePrefix, "86")) {
			user.setMobile(mobilePrefix + "-" + user.getMobile());
		}
		return user;
	}

	@Override
	public  BaseResult<PublicUser> createDefaultShareAccount(PackBundle bundle,UserDetails userDetails,boolean isPrize) {
		BaseResult<PublicUser> resultData = new BaseResult<>();
		String userName = null;
		//循环生成随机字符串，并到数据库中验证
		//测试循环1000w次没出现重复的随机串
		while (true) {
			//生成系统默认的共享号名称
			String random = RandomUtil.generateLowerString(10);
			userName = "wp_" + random;
			PublicUser psBean = this.getByUserName(userName);
			if (psBean == null){
				break;
			}
		}
		try {
			PublicUser user = new PublicUser();
			user.setAvatarUrl("resources/images/user/user_log_b.png");
			user.setThumbAvatarUrl("resources/images/user/user_logo_s.png");
			user.setBackgroundImgUrl("resources/images/user/user_bg.png");
			user.setVersion(2);
			user.setStatus("1");
			user.setTypeId(0);
			user.setUserName(userName);
			user.setUuid(userDetails.getUuid());
			user.setCreateTime(new Timestamp(new Date().getTime()));
			user.setMobile(userDetails.getMobile());
			//客户端id
			String clientId = userDetails.getClientId();
			//验证手机号，并更新共享号信息
			PublicUser unUser = this.getUnregisteredUserByMobile(userDetails.getMobile());
			if (null != unUser) {
				user.setId(unUser.getId());
				this.update(user);
				try {
					//缓存用户信息
					UserCache userCache = CacheUtil.getOneDay(Const.APP_USERCACHE + user.getId());
					if (null != userCache) {
						userCache.setUuid(user.getUuid());
						userCache.setUserName(user.getUserName());
						userCache.setMobile(user.getMobile());
						CacheUtil.putOneDay(Const.APP_USERCACHE + user.getId(), userCache);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				//设置clientId，区分客户端业务类型，食品护照、版权护照等
				//如果clientId为空，走老护照流程，否则根据业务码走其他护照业务类型
				user.setClientId(clientId);
				this.save(user);
			}
			//设置自返回结果
			resultData.setStatus(EnumStatus.ZORO.getIndex());
			resultData.setData(user);
			//如果clientId不为空，一下奖励不执行
			if (!StringUtil.isEmpty(clientId)){
				//是否奖励判断
				if (isPrize) {
					//用户注册成功后，关注通用公共账户
					//同时创建群，让该用户自动进群获取奖励
					try {
						logger.info("开始自动关注、建群、入群，注册用户:" + JSON.toJSONString(user));
						this.registerPrize(bundle, user.getUuid());
					} catch (Exception e) {
						e.printStackTrace();
						logger.info("注册关注、建群、入群异常：" + e.getMessage());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("PublicUserServiceImpl.createDefaultShareAccount.error:" + e.getMessage());
			resultData.setStatus(EnumStatus.NINETY_NINE.getIndex());
			resultData.setError(e.getMessage());
		}
		return resultData;
	}

	@Override
	public int deleteUser(PublicUser user) {
		String mobile = user.getMobile();
		if(StringUtils.isNotEmpty(mobile)) {
			GlobleMobile globleMobile = new GlobleMobile(mobile);
			user.setMobile(globleMobile.getMobile());
			user.setMobilePrefix(globleMobile.getMobilePrefix());
		}
		return dao.deleteUser(user);
	}

	@Override
	public int updateMsgPullTime(String userId, Long lastMsgPullTime) {

		return dao.updateMsgPullTime(userId, lastMsgPullTime);
	}

	@Override
	public PublicUserTop getUserTop(String userId) {
		return dao.getUserTop(userId);
	}

	/**
	 * 注册自动关注指定共享号和自动入群奖励
	 *
	 * @param uuid
	 */
	@Override
	public void registerPrize(PackBundle packBundle, String uuid) {
		if (StringUtil.isEmpty(uuid)){
			logger.info("注册用户uuid为空");
			return;
		}
		List<PublicUser> publicUsers = dao.getByUuid(uuid);
		if (publicUsers == null || publicUsers.size() == 0){
			logger.info("注册用户信息不存在：uuid=" + uuid);
			return;
		}
		//进群用户信息
		PublicUser publicUser = publicUsers.get(0);
		logger.info("注册用户信息："  + JSON.toJSONString(publicUser));
		//获取注册奖励开关值
		int groupPrizeSwitch = 1;
		try {
			groupPrizeSwitch = Config.getInt("register.prize.switch",1);
		}
		catch (Exception e){
			groupPrizeSwitch = 1;
		}
		if (groupPrizeSwitch == EnumStatus.ONE.getIndex()){
			//获取共享号用户id
			String shareAccounts = Config.get("register.prize.share.account.ids");
			if (StringUtil.isEmpty(shareAccounts)){
				logger.info("配置的默认共享号用户ID为空："  + shareAccounts);
				return;
			}
			logger.info("配置的默认共享号用户ID："  + shareAccounts);
			//进群用户id
			String userId = publicUser.getId();
			//群最大数量，未设置默认值为：500
			int maxCount = Config.getInt("group.max.num", 500);
			//注册进群数量
			int registerMaxCount = Config.getInt("register.into.group.max", 450);
			logger.info("注册进群数量阈值："  + registerMaxCount);
			//****************************************************************************
			//处理的默认关注，建群，入群逻辑
			//****************************************************************************
			String shareAccountArr [] = shareAccounts.split(",");
			for (String ownerId:shareAccountArr){
				//群名称
				String groupName = null;
				//群id
				String groupId = null;
				//关注该共享号，如果已经关注，删除重新关注
//				publicUserFollowerService.focusPublicUser(userId,ownerId);
				//查询该共享号创建的推广群以及群用户数量
				List<GroupMember> listCount = groupMemberDao.listGroupMemberCount(ownerId);
				//是否创建群
				boolean isCreateGroup = false;
				//无建群数量信息，需要默认创建新群
				if (listCount == null || listCount.size() == 0){
					isCreateGroup = true;
				}
				else {
					for (GroupMember groupMember : listCount) {
						//顺序取出群的会员数量
						Long mcount = groupMember.getMemberCount();
						//只要存在一个群的数量小于注册入群数量限制
						if (mcount < registerMaxCount) {
							groupId = groupMember.getGroupId();
							break;
						}
						//如果没有一个群满足数量要求者创建群，设置创建标识为：true
						isCreateGroup = true;
					}
				}
				//创建群标识为true，创建一个新群，同时添加该用户入群
				if (isCreateGroup){
					logger.info("群不存在或群已满员创建新群！");
					//进群用户列表
					List<String> userIdList = new ArrayList<>();
					userIdList.add(userId);
					//设置默认邀请人信息
					UserDetails userDetails = new UserDetails();
					userDetails.setUserId(ownerId);
					//群名称编号
					int groupNo = listCount.size() + 1;
					//获取群名称
					groupName = packBundle.getString("group.auto.register.name",groupNo);
					//创建群并添加用户
					groupService.addGroup(packBundle,ownerId,groupName,EnumStatus.ONE.getIndex(),userIdList,userDetails);
				}
				else {
					//groupId 不为空，直接入群
					if (!StringUtil.isEmpty(groupId)) {
						logger.info("群ID为["+groupId+"]的群增加新成员！");
						//设置进群用户id
						List<String> userList = new ArrayList<>();
						userList.add(userId);
						AddGroupUserParam addGroupUserParam = new AddGroupUserParam();
						addGroupUserParam.setUserIdList(userList);
						addGroupUserParam.setGroupId(groupId);
						addGroupUserParam.setJoinType(0);
						addGroupUserParam.setInviteReason("注册自动入群");
						addGroupUserParam.setInviterId(ownerId);
						addGroupUserParam.setUserIdList(userList);
						//设置默认邀请人信息
						UserDetails userDetails = new UserDetails();
						userDetails.setUserId(ownerId);
						//进群操作
						groupService.addGroupUser(packBundle,addGroupUserParam,userDetails);
					}
				}
			}
		}
	}

	/**
	 * 获取指定目标用户信息
	 *
	 * @param userDto
	 * @return
	 */
	@Override
	public BaseResult<Map<String, Object>> getUserInfos(UserDto userDto,Integer type) {
		//type 为空，默认设置为0
		type = type == null?0:type;
		BaseResult<Map<String, Object>> resultData = new BaseResult<>();
		String groupIds [] = userDto.getGroupIds();
		String userIds [] = userDto.getUserIds();
		//获取群相关信息
		List<Map<String, Object>> groupInfos = null;
		if (type.intValue() == EnumStatus.ZORO.getIndex()
				|| type.intValue() == EnumStatus.ONE.getIndex()) {
			if (groupIds != null && groupIds.length > 0) {
				groupInfos = this.getTargetGroupInfos(userDto);
			}
		}
		//获取群相关信息
		List<Map<String, Object>> userInfos = null;
		if (type.intValue() == EnumStatus.ZORO.getIndex()
				|| type.intValue() == EnumStatus.TWO.getIndex()) {
			if (userIds != null && userIds.length > 0) {
				userInfos = this.getTargetUserInfos(userDto);
			}
		}
		Map<String,Object> resultMap = new HashMap<>();
		resultMap.put("groupList",groupInfos == null?new ArrayList<>():groupInfos);
		resultMap.put("userList",userInfos == null?new ArrayList<>():userInfos);
		//设置放回结果
		resultData.setStatus(EnumStatus.ZORO.getIndex());
		resultData.setError("success");
		resultData.setData(resultMap);
		return resultData;
	}

	@Override
	public BaseResult<Map<String, Object>> getGroupUsers(UserDto userDto) {
		BaseResult<Map<String, Object>> resultData = new BaseResult<>();
		//获取群相关信息,此接口返回
		Map<String, Object> groupUserInfos = this.getGroupUserInfos(userDto);
		//设置放回结果
		resultData.setStatus(EnumStatus.ZORO.getIndex());
		resultData.setError("success");
		resultData.setData(groupUserInfos == null?new HashMap<>():groupUserInfos);
		return resultData;
	}

	/**
	 * 获取用户群相关消息
	 * @return
	 */
	private List<Map<String, Object>> getTargetGroupInfos(UserDto userDto){
		String [] groupIds = userDto.getGroupIds();
		List<Map<String,Object>> listMap = new ArrayList<>();;
		Integer needMemberNumber = userDto.getNeedMemberNumber();

		//如果为0或者空，不查询获取群成员信息
		if (needMemberNumber == null || needMemberNumber.intValue() == 0){
			//查询群信息
			Group groupParam = new Group();
			groupParam.setGroupIds(groupIds);
			List<Group> listGroups = groupDao.listGroupByIds(groupParam);
			//查询当前用户所在群信息
			GroupMember groupMemberParam = new GroupMember();
			groupMemberParam.setGroupIds(groupIds);
			groupMemberParam.setUserId(userDto.getCurrentUserId());
			groupMemberParam.setJoinState(EnumStatus.ONE.getIndex());
			List<GroupMember> listCurrentMembers = groupMemberDao.listMembersByGroupIds(groupMemberParam);
			//查询群成员数量
			//设置当前用户用户id为空，查询所有群的群成员数量信息
			groupMemberParam.setUserId(null);
			List<GroupMember> listMemberCount = groupMemberDao.getMemberCountByGroupIds(groupMemberParam);
			for (Group group:listGroups){
				//处理返回结果
				listMap = handleResultData(listMap,group,listCurrentMembers,null,listMemberCount);
			}
		}
		else {
			Group groupParam = new Group();
			groupParam.setGroupIds(groupIds);
			List<Group> listGroups = groupDao.listGroupByIds(groupParam);
			GroupMember groupMemberParam = new GroupMember();
			//查询群成员数量
			groupMemberParam.setGroupIds(groupIds);
			groupMemberParam.setJoinState(EnumStatus.ONE.getIndex());
			List<GroupMember> listMemberCount = groupMemberDao.getMemberCountByGroupIds(groupMemberParam);
			//设置查询当前用户所在群的信息
			groupMemberParam.setUserId(userDto.getCurrentUserId());
			List<GroupMember> listCurrentMembers = groupMemberDao.listMembersByGroupIds(groupMemberParam);
			//如果设置查询成员信息，直接查询成员信息，并附带群信息
			for (Group group:listGroups){
				String arrGroupId [] = {group.getGroupId()};
				groupMemberParam.setGroupIds(arrGroupId);
				if (needMemberNumber.intValue() > 0) {
					groupMemberParam.setOffset(needMemberNumber);
				}
				groupMemberParam.setUserId(null);
				long ddd = System.currentTimeMillis();
				List<GroupMember> listMembers = groupMemberDao.listMembersByGroupIds(groupMemberParam);
				//处理返回结果
				listMap = handleResultData(listMap,group,listCurrentMembers,listMembers,listMemberCount);
			}
		}
		return listMap;
	}

	/**
	 * 处理返回结果
	 * @param group
	 * @param listCurrentMembers
	 * @param listMembers
	 * @param listMemberCount
	 * @return
	 */
	private List<Map<String,Object>> handleResultData(List<Map<String,Object>> listMap,
													  Group group,
													  List<GroupMember> listCurrentMembers,
													  List<GroupMember> listMembers,
												  List<GroupMember> listMemberCount){
		if (listMap == null){
			listMap = new ArrayList<>();
		}
		//当前登录用户所在群的信息，可能不存在
		GroupMember currentGroupMember = null;
		if (group == null){
			return listMap;
		}
		String groupId = group.getGroupId();
		//循环比对，获取当前用户所在群的成员信息
		for (GroupMember groupMember:listCurrentMembers) {
			String groupIdTemp = groupMember.getGroupId();
			if (!StringUtil.isEmpty(groupId)
					&& !StringUtil.isEmpty(groupIdTemp)
					&& groupId.equals(groupIdTemp)) {
				currentGroupMember = groupMember;
				break;
			}
		}
		//会员数量
		long memberCount = 0l;
		for (GroupMember mcount : listMemberCount) {
			String countGroupId = mcount.getGroupId();
			if (!StringUtil.isEmpty(groupId)
					&& !StringUtil.isEmpty(countGroupId)
					&& groupId.equals(countGroupId)) {
				memberCount = mcount.getMemberCount() == null ? 0 : mcount.getMemberCount().intValue();
				break;
			}
		}
		Map<String,Object> groupMap = new HashMap<>();
		List<Map<String, Object>> listMemberMap = null;
		//循环输出成员列表，到返回对象中
		if (listMembers != null && listMembers.size() > 0) {
			listMemberMap = new ArrayList<>();
			for (GroupMember groupMemberTemp : listMembers) {
				Map<String, Object> memberMap = new HashMap<>();
				memberMap.put("groupId", groupMemberTemp.getGroupId());
				memberMap.put("userId", groupMemberTemp.getUserId());
				memberMap.put("nickname", groupMemberTemp.getNikeName());
				memberMap.put("nicknameAltered", groupMemberTemp.getNikeNameAltered());
				String avatarUrl = Misc.getServerUri(null,groupMemberTemp.getAvatarUrl());
				memberMap.put("avatarUrl", avatarUrl == null?"":avatarUrl);
				memberMap.put("joinTime",groupMemberTemp.getJoinTime());
				memberMap.put("joinType", groupMemberTemp.getJoinType());
				listMemberMap.add(memberMap);
			}
			//如果群信息为空，直接返回空数组
			if (group == null){
				return listMap;
			}
		}
		groupMap.put("groupId",group.getGroupId());
		groupMap.put("ownerId",group.getOwnerId());
		groupMap.put("groupName",group.getGroupName());
		groupMap.put("groupHeader", Misc.getServerUri(null,group.getGroupHeader()));
		groupMap.put("groupState",group.getGroupState());
		groupMap.put("groupType",group.getGroupType());
		groupMap.put("memberCount",memberCount);
		groupMap.put("modifyRight",group.getModifyRight());
		groupMap.put("needAuth",group.getNeedAuth());
		groupMap.put("inviteType",group.getInviteType());
		if (currentGroupMember != null){
			groupMap.put("isTop",currentGroupMember.getTopTalk() == null?0:currentGroupMember.getTopTalk());
			groupMap.put("isDisturb",currentGroupMember.getMsgReceiveType() == null?0:currentGroupMember.getMsgReceiveType());
			groupMap.put("nickname",currentGroupMember.getNikeName());
			groupMap.put("isShowNickname",currentGroupMember.getShowNikeName() == null?0:currentGroupMember.getShowNikeName());
		}
		else{
			groupMap.put("isTop",0);
			groupMap.put("isDisturb",0);
			groupMap.put("nickname","");
			groupMap.put("isShowNickname",0);
		}
		groupMap.put("description",group.getDescription());
		groupMap.put("updateTime",group.getUpdateTime());
		groupMap.put("createTime",group.getCreateTime());
		groupMap.put("memberList", listMemberMap);
		listMap.add(groupMap);
		return listMap;
	}

	/**
	 * 获取指定群内用户列表信息
	 * @param userDto
	 * @return
	 */
	private Map<String, Object> getGroupUserInfos(UserDto userDto){
		Map<String, Object> resultMap = new HashMap<>();
		List<Map<String, Object>> listMap = new ArrayList<>();
		//设置参数，关联用户表查询指定群内的用户信息
		String groupId = userDto.getGroupId();
		String [] userIds =  userDto.getUserIds();
		//查询群信息
		Group group = groupDao.selectByPrimarykey(groupId);
		//群信息为空，直接返回
		if (group == null){
			return resultMap;
		}
		//查询群成员信息
		GroupMember groupMemberParam = new GroupMember();
		groupMemberParam.setUserIds(userIds);
		groupMemberParam.setGroupId(groupId);
		List<GroupMember> listMembers = groupMemberDao.listMembersByIds(groupMemberParam);
		Long memberCount = 0l;
		for (GroupMember groupMemberTemp:listMembers){
			//获取群成员数量
			if (memberCount.longValue() == 0) {
				memberCount = groupMemberTemp.getMemberCount();
			}
			Map<String, Object> memberMap = new HashMap<>();
			memberMap.put("groupId", groupMemberTemp.getGroupId());
			memberMap.put("userId", groupMemberTemp.getUserId());
			memberMap.put("nickname", groupMemberTemp.getNikeName());
			memberMap.put("nicknameAltered", groupMemberTemp.getNikeNameAltered());
			String avatarUrl = Misc.getServerUri(null,groupMemberTemp.getAvatarUrl());
			memberMap.put("avatarUrl", avatarUrl == null?"":avatarUrl);
			memberMap.put("joinTime",groupMemberTemp.getJoinTime());
			memberMap.put("joinType", groupMemberTemp.getJoinType());
			listMap.add(memberMap);
		}
		resultMap.put("groupId",group.getGroupId());
		resultMap.put("ownerId",group.getOwnerId());
		resultMap.put("groupName",group.getGroupName());
		resultMap.put("groupHeader", Misc.getServerUri(null,group.getGroupHeader()));
		resultMap.put("groupState",group.getGroupState());
		resultMap.put("groupType",group.getGroupType());
		resultMap.put("memberCount",memberCount);
		resultMap.put("modifyRight",group.getModifyRight());
		resultMap.put("needAuth",group.getNeedAuth());
		resultMap.put("inviteType",group.getInviteType());
		resultMap.put("description",group.getDescription());
		resultMap.put("updateTime",group.getUpdateTime());
		resultMap.put("createTime",group.getCreateTime());
		resultMap.put("memberList", listMap);
		return resultMap;
	}


	private List<Map<String, Object>> getTargetUserInfos(UserDto userDto){
		List<Map<String, Object>> listMap = new ArrayList<>();
		String [] userIds =  userDto.getUserIds();
		//获取粉丝数量
		Integer needFansNumber = userDto.getNeedFansNumber();
		//获取关注人数量
		Integer needFollowersNumber = userDto.getNeedFollowersNumber();
		PublicUserFollower publicUserFollowerParam = new PublicUserFollower();
		publicUserFollowerParam.setUserIds(userIds);
		PublicUser publicUserParam = new PublicUser();
		publicUserParam.setUserIds(userIds);
		publicUserParam.setSetupUserId(userDto.getCurrentUserId());
		List<PublicUser> listUsers = dao.getByUserIds(publicUserParam);
		for (PublicUser publicUser:listUsers){
			Map<String, Object> userMap = new HashMap<>();
			userMap.put("userId",publicUser.getId());
			userMap.put("userName",publicUser.getUserName());
			userMap.put("avatarUrl", Misc.getServerUri(null,publicUser.getAvatarUrl()));
			userMap.put("userType",0);
			userMap.put("isTop",publicUser.getIsTop() == null?0:publicUser.getIsTop());
			userMap.put("isDisturb",publicUser.getIsDisturb() == null?0:publicUser.getIsDisturb());
			userMap.put("thumbAvatarUrl",Misc.getServerUri(null,publicUser.getThumbAvatarUrl()));
			userMap.put("backgroundImgUrl",Misc.getServerUri(null,publicUser.getBackgroundImgUrl()));
			userMap.put("fansCount",publicUser.getFansCount());
			userMap.put("followersCount",publicUser.getFollowersCount());
			userMap.put("description",publicUser.getDescription());
			userMap.put("createTime",publicUser.getCreateTime());
			String userId = publicUser.getId();
			//粉丝列表
			List<Map<String, Object>> fansListMap = null;
			publicUserFollowerParam.setUserIds(new String[]{userId});
			//值大于0 获取指定数量的数据，否则获取所有数据
			if (needFansNumber.intValue() > 0) {
				publicUserFollowerParam.setOffset(needFansNumber);
				List<PublicUserFollower> listFans = publicUserFollowerDao.listFansByUserIds(publicUserFollowerParam);
				fansListMap = handleUserInfos(listFans,EnumStatus.ONE.getIndex());
				userMap.put("fansList",fansListMap);
			}
			else{
				userMap.put("fansList",new ArrayList<>());
			}

			//获取处理关注人列表
			List<Map<String, Object>> followersListMap = null;
			publicUserFollowerParam.setUserIds(new String[]{userId});
			//值大于0 获取指定数量的数据，否则获取所有数据
			if (needFollowersNumber.intValue() > 0) {
				publicUserFollowerParam.setOffset(needFollowersNumber);
				List<PublicUserFollower> listFollowers = publicUserFollowerDao.listFollowersByUserIds(publicUserFollowerParam);
				followersListMap =  handleUserInfos(listFollowers,EnumStatus.TWO.getIndex());
				userMap.put("followersList",followersListMap);
			}
			else{
				userMap.put("followersList",new ArrayList<>());
			}
			listMap.add(userMap);
		}
		return listMap;
	}

	/**
	 * 处理用户返回信息
	 * @param listUser
	 * @return
	 */
	private List<Map<String, Object>> handleUserInfos(List<PublicUserFollower> listUser,Integer type){
		List<Map<String, Object>> listMap = new ArrayList<>();
		for (PublicUserFollower publicUserFollower:listUser){
			Map<String, Object> resultMap = new HashMap<>();
			//粉丝获取userId，关注的人获取followerId
			if (type != null && type == 1) {
				resultMap.put("userId", publicUserFollower.getUserId());
			}
			else{
				resultMap.put("userId", publicUserFollower.getFollowerId());
			}
			resultMap.put("userName",publicUserFollower.getUserName());
			resultMap.put("avatarUrl",Misc.getServerUri(null,publicUserFollower.getAvatarUrl()));
			resultMap.put("userType",0);
			resultMap.put("thumbAvatarUrl",Misc.getServerUri(null,publicUserFollower.getThumbAvatarUrl()));
			resultMap.put("backgroundImgUrl",Misc.getServerUri(null,publicUserFollower.getBackgroundImgUrl()));
			resultMap.put("description",publicUserFollower.getDescription());
			resultMap.put("createTime",publicUserFollower.getCreateTime());
			resultMap.put("fansCount",publicUserFollower.getFansCount());
			resultMap.put("followersCount",publicUserFollower.getFollowersCount());
			listMap.add(resultMap);
		}
		return listMap;
	}
}
