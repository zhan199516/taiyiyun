package com.taiyiyun.passport.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.taiyiyun.passport.consts.Const;
import com.taiyiyun.passport.consts.EnumStatus;
import com.taiyiyun.passport.dao.IPublicUserBlockDao;
import com.taiyiyun.passport.dao.IPublicUserConfigDao;
import com.taiyiyun.passport.dao.IPublicUserDao;
import com.taiyiyun.passport.dao.group.IGroupDao;
import com.taiyiyun.passport.dao.group.IGroupMemberDao;
import com.taiyiyun.passport.language.PackBundle;
import com.taiyiyun.passport.po.BaseResult;
import com.taiyiyun.passport.po.PublicUser;
import com.taiyiyun.passport.po.PublicUserConfig;
import com.taiyiyun.passport.po.group.Group;
import com.taiyiyun.passport.po.group.GroupMember;
import com.taiyiyun.passport.po.setting.UserConfig;
import com.taiyiyun.passport.service.IPublicUserConfigService;
import com.taiyiyun.passport.service.IRedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
public class PublicUserConfigServiceImpl implements IPublicUserConfigService {

	public final Logger logger = LoggerFactory.getLogger(getClass());

	@Resource
	private IPublicUserConfigDao publicUserConfigDao;

	@Resource
	private IPublicUserDao publicUserDao;

	@Resource
	private IGroupMemberDao groupMemberDao;

	@Resource
	private IGroupDao groupDao;

	@Resource
	private IPublicUserBlockDao publicUserBlockDao;

	@Resource
	private IRedisService redisService;


	/**
	 * 设置目标用户免打扰状态
	 *
	 * @param userConfig
	 * @return
	 */
	@Override
	public BaseResult<Integer> configDisturbStatus(PackBundle bundle,UserConfig userConfig) {
		Integer userType = userConfig.getUserType();
		//0:全局设置
		//1:普通用户，设置用户配置表中的数据
		//2:群组，设置群会员表中的数据
		if (userType != null && userType == EnumStatus.ZORO.getIndex()){
			return this.configGlobalDisturb(bundle,userConfig);
		}
		else if (userType != null && userType == EnumStatus.ONE.getIndex()){
			return this.userConfigDisturb(bundle,userConfig);
		}
		else{
			return this.groupConfigDisturb(bundle,userConfig);
		}
	}

	/**
	 * 设置全局消息免打扰
	 * @param bundle
	 * @param userConfig
	 * @return
	 */
	private BaseResult<Integer> configGlobalDisturb(PackBundle bundle,UserConfig userConfig){
		BaseResult<Integer> resultData = new BaseResult<>();
		try {
			PublicUserConfig publicUserConfig = new PublicUserConfig();
			BeanUtils.copyProperties(userConfig, publicUserConfig);
			publicUserConfig.setModifyTime(new Date());
			publicUserConfig.setTargetId(null);
			//先去更新数据库，如果更新失败说明没有记录，则直接插入
			int rval = publicUserConfigDao.updateBySetupUserId(publicUserConfig);
			if (rval <= 0){
				publicUserConfig.setModifyTime(null);
				publicUserConfig.setCreateTime(new Date());
				publicUserConfig.setIsTop(EnumStatus.ZORO.getIndex());
				rval = publicUserConfigDao.insert(publicUserConfig);
			}
			if (rval > 0 ){
				resultData.setStatus(EnumStatus.ZORO.getIndex());
				resultData.setError("success");
			}
			else{
				resultData.setStatus(EnumStatus.ONE.getIndex());
				resultData.setError("fail");
			}
			return resultData;
		}
		catch (Exception e){
			e.printStackTrace();
			logger.info("PublicUserConfigServiceImpl.configGlobalDisturb.error:" + e.getMessage());
			resultData.setStatus(EnumStatus.NINETY_NINE.getIndex());
			resultData.setError(e.getMessage());
			return resultData;
		}
	}


	/**
	 * 修改设置用户免打扰状态
	 * @param userConfig
	 * @return
	 */
	private BaseResult<Integer> userConfigDisturb(PackBundle bundle,UserConfig userConfig){
		BaseResult<Integer> resultData = new BaseResult<>();
		try {
			//判断用户信息是否存在
			String targetId = userConfig.getTargetId();
			PublicUser publicUser = publicUserDao.getByUserId(targetId);
			if (publicUser == null){
				String errMsg = "target user not exist";
				if(bundle != null) {
					errMsg = bundle.getString("config.fail.target.user.not.exist");
				}
				resultData.setStatus(EnumStatus.ONE.getIndex());
				resultData.setError(errMsg);
				return resultData;
			}
			PublicUserConfig publicUserConfig = new PublicUserConfig();
			BeanUtils.copyProperties(userConfig, publicUserConfig);
			publicUserConfig.setModifyTime(new Date());
			//先去更新数据库，如果更新失败说明没有记录，则直接插入
			int rval = publicUserConfigDao.updateByUserId(publicUserConfig);
			if (rval <= 0){
				publicUserConfig.setModifyTime(null);
				publicUserConfig.setCreateTime(new Date());
				publicUserConfig.setIsTop(EnumStatus.ZORO.getIndex());
				rval = publicUserConfigDao.insert(publicUserConfig);
			}
			if (rval > 0 ){
				resultData.setStatus(EnumStatus.ZORO.getIndex());
				resultData.setError("success");
			}
			else{
				resultData.setStatus(EnumStatus.ONE.getIndex());
				resultData.setError("fail");
			}
			return resultData;
		}
		catch (Exception e){
			e.printStackTrace();
			logger.info("PublicUserConfigServiceImpl.setupDisturbStatus.error:" + e.getMessage());
			resultData.setStatus(EnumStatus.NINETY_NINE.getIndex());
			resultData.setError(e.getMessage());
			return resultData;
		}
	}

	/**
	 * 设置群成员免打扰状态
	 * @param userConfig
	 * @return
	 */
	private BaseResult<Integer> groupConfigDisturb(PackBundle bundle, UserConfig userConfig){
		BaseResult<Integer> resultData = new BaseResult<>();
		try {
			//判断群信息是否存在
			String targetId = userConfig.getTargetId();
			Group group = groupDao.selectByPrimarykey(targetId);
			if (group == null){
				String errMsg = "target group not exist";
				if(bundle != null) {
					errMsg = bundle.getString("config.fail.target.group.not.exist");
				}
				resultData.setStatus(EnumStatus.ONE.getIndex());
				resultData.setError(errMsg);
				return resultData;
			}
			GroupMember groupMember = new GroupMember();
			groupMember.setGroupId(userConfig.getTargetId());
			groupMember.setUserId(userConfig.getSetupUserId());
			groupMember.setMsgReceiveType(userConfig.getIsDisturb());
			//更新当前用户在群中的消息秒打扰
			int rval = groupMemberDao.updateDisturbById(groupMember);
			if (rval > 0){
				resultData.setStatus(EnumStatus.ZORO.getIndex());
				resultData.setError("success");
			}
			else{
				String errMsg = "target user not in group";
				if(bundle != null) {
					errMsg = bundle.getString("config.fail.user.not.exist");
				}
				resultData.setStatus(EnumStatus.ONE.getIndex());
				resultData.setError(errMsg);
			}

			String groupId = groupMember.getGroupId();
			List<String> userIds = groupMemberDao.selectSetDisturbUserIds(groupId);
			String userIdJson = JSONObject.toJSONString(userIds);
			redisService.evict(Const.GROUP_SETDISTURB_USERIDS + groupId);
			redisService.put(Const.GROUP_SETDISTURB_USERIDS + groupId, userIdJson, 3600);
			return resultData;
		}
		catch (Exception e){
			e.printStackTrace();
			logger.info("PublicUserConfigServiceImpl.groupConfigDisturb.error:" + e.getMessage());
			resultData.setStatus(EnumStatus.NINETY_NINE.getIndex());
			resultData.setError(e.getMessage());
			return resultData;
		}
	}


	/**
	 * 设置目标用户置顶状态
	 *
	 * @param userConfig
	 * @return
	 */
	@Override
	public BaseResult<Integer> configTopStatus(PackBundle bundle,UserConfig userConfig) {
		Integer userType = userConfig.getUserType();
		//1:普通用户，设置用户配置表中的数据
		//2:群组，设置群会员表中的数据
		if (userType != null && userType == EnumStatus.ONE.getIndex()){
			return this.userConfigTop(bundle,userConfig);
		}
		else{
			return this.groupConfigTop(bundle,userConfig);
		}
	}


	/**
	 * 修改配置指定用户的指定状态
	 * @param userConfig
	 * @return
	 */
	private BaseResult<Integer> userConfigTop(PackBundle bundle,UserConfig userConfig) {
		BaseResult<Integer> resultData = new BaseResult<>();
		try {
			//判断用户信息是否存在
			String targetId = userConfig.getTargetId();
			PublicUser publicUser = publicUserDao.getByUserId(targetId);
			if (publicUser == null){
				String errMsg = "target user not exist";
				if(bundle != null) {
					errMsg = bundle.getString("config.fail.target.user.not.exist");
				}
				resultData.setStatus(EnumStatus.ONE.getIndex());
				resultData.setError(errMsg);
				return resultData;
			}
			PublicUserConfig publicUserConfig = new PublicUserConfig();
			BeanUtils.copyProperties(userConfig, publicUserConfig);
			publicUserConfig.setModifyTime(new Date());
			//先去更新数据库，如果更新失败说明没有记录，则直接插入
			int rval = publicUserConfigDao.updateByUserId(publicUserConfig);
			if (rval <= 0){
				publicUserConfig.setModifyTime(null);
				publicUserConfig.setCreateTime(new Date());
				publicUserConfig.setIsDisturb(EnumStatus.ZORO.getIndex());
				rval = publicUserConfigDao.insert(publicUserConfig);
			}
			if (rval > 0 ){
				resultData.setStatus(EnumStatus.ZORO.getIndex());
				resultData.setError("success");
			}
			else{
				resultData.setStatus(EnumStatus.ONE.getIndex());
				resultData.setError("fail");
			}
			return resultData;
		}
		catch (Exception e){
			e.printStackTrace();
			logger.info("PublicUserConfigServiceImpl.configTopStatus.error:" + e.getMessage());
			resultData.setStatus(EnumStatus.NINETY_NINE.getIndex());
			resultData.setError(e.getMessage());
			return resultData;
		}
	}

	/**
	 * 设置群置顶状态
	 * @param userConfig
	 * @return
	 */
	private BaseResult<Integer> groupConfigTop(PackBundle bundle,UserConfig userConfig) {
		BaseResult<Integer> resultData = new BaseResult<>();
		try {
			//判断群信息是否存在
			String targetId = userConfig.getTargetId();
			Group group = groupDao.selectByPrimarykey(targetId);
			if (group == null){
				String errMsg = "target group not exist";
				if(bundle != null) {
					errMsg = bundle.getString("config.fail.target.group.not.exist");
				}
				resultData.setStatus(EnumStatus.ONE.getIndex());
				resultData.setError(errMsg);
				return resultData;
			}
			GroupMember groupMember = new GroupMember();
			groupMember.setGroupId(userConfig.getTargetId());
			groupMember.setUserId(userConfig.getSetupUserId());
			groupMember.setTopTalk(userConfig.getIsTop());
			//更新当前用户在群中的消息秒打扰
			int rval = groupMemberDao.updateTopById(groupMember);
			if (rval > 0) {
				resultData.setStatus(EnumStatus.ZORO.getIndex());
				resultData.setError("success");
			}
			else{
				String errMsg = "target user not in group";
				if(bundle != null) {
					errMsg = bundle.getString("config.fail.user.not.exist");
				}
				resultData.setStatus(EnumStatus.ONE.getIndex());
				resultData.setError(errMsg);
			}
			return resultData;
		}
		catch (Exception e){
			e.printStackTrace();
			logger.info("PublicUserConfigServiceImpl.groupConfigTop.error:" + e.getMessage());
			resultData.setStatus(EnumStatus.NINETY_NINE.getIndex());
			resultData.setError(e.getMessage());
			return resultData;
		}
	}


	@Override
	public BaseResult<Map<String, Object>> getUserConfigInfos(UserConfig userConfig) {
		BaseResult<Map<String, Object>> resultData = new BaseResult<>();
		Map<String, Object> resultMap = new HashMap<>();
		String userId = userConfig.getSetupUserId();
		//查询用户所有群组配置信息
		GroupMember groupMember = new GroupMember();
		groupMember.setUserId(userId);
		List<GroupMember> listGroupMembers = groupMemberDao.listMembersByUserId(groupMember);
		List<String> groupMemberList = new ArrayList<>();
		for (GroupMember groupMemberTemp:listGroupMembers){
			if (groupMemberTemp == null){
				continue;
			}
//			Group group = groupMemberTemp.getGroup();
//			if (group == null){
//				continue;
//			}
//			Map<String, Object> groupMap = new HashMap<>();
//			groupMap.put("userId",groupMemberTemp.getUserId());
//			groupMap.put("groupId",groupMemberTemp.getGroupId());
//			groupMap.put("ownerId",group.getOwnerId());
//			groupMap.put("groupName",group.getGroupName());
//			groupMap.put("groupHeader",group.getGroupHeader());
//			groupMap.put("groupState",group.getGroupState());
//			groupMap.put("groupType",group.getGroupType());
//			groupMap.put("memberCount",groupMemberTemp.getMemberCount());
//			groupMap.put("modifyRight",group.getModifyRight());
//			groupMap.put("needAuth",group.getNeedAuth());
//			groupMap.put("inviteType",group.getInviteType());
//			groupMap.put("isTop",groupMemberTemp.getTopTalk());
//			groupMap.put("isDisturb",groupMemberTemp.getMsgReceiveType());
//			groupMap.put("nickname",groupMemberTemp.getNikeName());
//			groupMap.put("isShowNickname",groupMemberTemp.getShowNikeName());
//			groupMap.put("description",group.getDescription());
//			groupMap.put("updateTime",group.getUpdateTime());
//			groupMap.put("createTime",group.getCreateTime());
			groupMemberList.add(groupMemberTemp.getGroupId());
		}
		resultMap.put("groupList",groupMemberList);
		//查询用户所有用户配置信息
		List<PublicUserConfig> listConfigs = publicUserConfigDao.listBySetupUserId(userId);
		List<String> configList = new ArrayList<>();
		for (PublicUserConfig publicUserConfig:listConfigs){
			if (publicUserConfig == null){
				continue;
			}
			PublicUser user = publicUserConfig.getUser();
//			Map<String, Object> configMap = new HashMap<>();
//			configMap.put("userId",user.getId());
//			configMap.put("userName",user.getUserName());
//			configMap.put("avatarUrl",user.getAvatarUrl());
//			configMap.put("userType",user.getTypeId());
//			configMap.put("isTop",publicUserConfig.getIsTop());
//			configMap.put("isDisturb",publicUserConfig.getIsDisturb());
//			configMap.put("thumbAvatarUrl",user.getThumbAvatarUrl());
//			configMap.put("backgroundImgUrl",user.getBackgroundImgUrl());
//			configMap.put("description",user.getDescription());
//			configMap.put("createTime",user.getCreateTime());
			configList.add(user.getId());
		}
		resultMap.put("userList",configList);

		//查询用户所有黑名单信息
		List<PublicUser> userBlackList = publicUserBlockDao.getBlockByUserId(userId);
		List<String> blackList = new ArrayList<>();
		for (PublicUser publicUser:userBlackList){
			if (publicUser == null){
				continue;
			}
			String userIdTemp = publicUser.getId();
			if (userIdTemp == null){
				continue;
			}
//			Map<String, Object> blackMap = new HashMap<>();
//			blackMap.put("userId",userIdTemp);
			blackList.add(userIdTemp);
		}
		resultMap.put("blackList",blackList);
		//封装结果返回
		resultData.setData(resultMap);
		resultData.setError("success");
		resultData.setStatus(EnumStatus.ZORO.getIndex());
		return resultData;
	}

	/**
	 * 获取免打扰状态
	 *
	 * @param userConfig
	 * @return
	 */
	@Override
	public BaseResult<UserConfig> getOneUserConfig(UserConfig userConfig) {
		BaseResult<UserConfig> resultData = new BaseResult<>();
		PublicUserConfig publicUserConfig = new PublicUserConfig();
		//拷贝参数值
		BeanUtils.copyProperties(userConfig,publicUserConfig);
		List<PublicUserConfig> resultList = publicUserConfigDao.list(publicUserConfig);
		if (resultList != null && resultList.size() > 0){
			publicUserConfig = resultList.get(0);
			if (publicUserConfig == null){
				return null;
			}
			//拷贝值
			BeanUtils.copyProperties(publicUserConfig,userConfig);
			resultData.setData(userConfig);
			return resultData;
		}
		return null;
	}
}
