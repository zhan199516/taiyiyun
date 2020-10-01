package com.taiyiyun.passport.service.impl;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.taiyiyun.passport.consts.Config;
import com.taiyiyun.passport.dao.IPublicInvitationConfDao;
import com.taiyiyun.passport.dao.IPublicInvitationDeliveryAddressDao;
import com.taiyiyun.passport.dao.IPublicInvitationUserRegisterDao;
import com.taiyiyun.passport.po.PublicInvitationConf;
import com.taiyiyun.passport.po.PublicInvitationDeliveryAddress;
import com.taiyiyun.passport.po.PublicInvitationUserRegister;
import com.taiyiyun.passport.service.IPublicInvitationService;
import com.taiyiyun.passport.service.IRedisService;
import com.taiyiyun.passport.util.DateUtil;

@Service
public class PublicInvitationServiceImpl implements IPublicInvitationService {

	public final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Resource
	private IPublicInvitationUserRegisterDao publicInvitationUserRegisterDao;
	@Resource
	private IPublicInvitationDeliveryAddressDao publicInvitationDeliveryAddressDao;
	@Resource
	private IPublicInvitationConfDao publicInvitationConfDao;
	@Resource
	private IRedisService redisService;
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
	public boolean insert(PublicInvitationUserRegister publicInvitationUserRegister) {
		if (null == publicInvitationUserRegister) {
			return false;
		}
		List<PublicInvitationUserRegister> registerList = publicInvitationUserRegisterDao.listByUserId(publicInvitationUserRegister);
		logger.info("publicInvitationUserRegisterDao.listByUserId registerList.size()"+registerList.size());
		if (null ==  registerList || registerList.size() == 0) {
			int result = publicInvitationUserRegisterDao.insert(publicInvitationUserRegister);
			if (result > 0) {
				logger.info("publicInvitationUserRegisterDao.insert success");
				return true;
			}
		}
		return false;
	}


	@Override
	public Integer getInvitationUserCount(String invitationId, String invitationUserId) {
		if (null ==  invitationId || null == invitationUserId) {
			return null;
		}
		PublicInvitationUserRegister publicInvitationUserRegister = new PublicInvitationUserRegister();
		publicInvitationUserRegister.setInvitationId(invitationId);
		publicInvitationUserRegister.setInvitationUserId(invitationUserId);
		List<PublicInvitationUserRegister> userRegister = publicInvitationUserRegisterDao.listByInvitationId(publicInvitationUserRegister);
		if (null != userRegister && userRegister.size() > 0) {
			return userRegister.size();
		}
		return null;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
	public boolean insert(PublicInvitationDeliveryAddress publicInvitationDeliveryAddress) {
		if (null == publicInvitationDeliveryAddress) {
			return false;
		}
		int ret = publicInvitationDeliveryAddressDao.insert(publicInvitationDeliveryAddress);
		if (ret > 0) {
			return true;
		}
		return false;
	}


	@Override
	public Map<String, Object> getInvitationConf(String path) {
		Map<String, Object> retMap = new java.util.HashMap<>();
		String invitationId = Config.get("invitationId");
		JSONObject jsonObject = redisService.get(invitationId);
		if (null != jsonObject) {
			PublicInvitationConf invitationConf = (PublicInvitationConf) JSONObject.toJavaObject(jsonObject,PublicInvitationConf.class);
			setResultMap(retMap, invitationConf);
			logger.info("从Redis取数据。。。。。。。。。。。" + JSONObject.toJSON(invitationConf));
		} else {
			java.util.List<PublicInvitationConf> invitationConfList = publicInvitationConfDao.listByInvitationId(invitationId);
			if (null != invitationConfList && invitationConfList.size() > 0) {
				PublicInvitationConf invitationConf = invitationConfList.get(0);
				setResultMap(retMap, invitationConf);
				/*** 放入缓存 一分钟 */
				redisService.put(invitationId, JSONObject.toJSON(invitationConf), 60000);
				logger.info("从MySQL取数据。。。。。。。。。。。" + JSONObject.toJSON(invitationConf));
			}
		}
		return retMap;
	}
	
	public final void setResultMap (Map<String, Object> retMap,PublicInvitationConf invitationConf) {
		if (null != invitationConf && invitationConf.getDisabled() == 0
				&& !DateUtil.isExpired(new java.util.Date(), invitationConf.getExpiredTime())) {
			retMap.put("disabled", 0);
			retMap.put("invitationId", invitationConf.getInvitationId());
			retMap.put("invitationName", invitationConf.getInvitationName());
			retMap.put("imgUrl", invitationConf.getImgUrl());
			retMap.put("pageUrl", invitationConf.getPageUrl());
			retMap.put("sharePageUrl", invitationConf.getSharePageUrl());
			retMap.put("description", invitationConf.getDescription());
			retMap.put("thumbnailUrl", invitationConf.getThumbnailUrl());
			logger.info("这个邀请没有过期。。。。。。。。。。。。。。。。。" + JSONObject.toJSON(retMap));
		} else {
			retMap.put("disabled", 1);
			logger.info("这个邀请已经过期。。。。。。。。。。。。。。。。。。" + JSONObject.toJSON(retMap));
		}
	}


	@Override
	public boolean isPublicInvitationDeliveryAddress(String invitationId, String invitationUserId, String mobile) {
		PublicInvitationDeliveryAddress publicInvitationDeliveryAddress=new PublicInvitationDeliveryAddress();
		publicInvitationDeliveryAddress.setInvitationId(invitationId);
		publicInvitationDeliveryAddress.setInvitationUserId(invitationUserId);
		publicInvitationDeliveryAddress.setMobile(mobile);
		java.util.List<PublicInvitationDeliveryAddress> addressList = publicInvitationDeliveryAddressDao.listByUserId(publicInvitationDeliveryAddress);
		if (null != addressList && addressList.size() > 0) {
			return true;
		}
		return false;
	}

}
