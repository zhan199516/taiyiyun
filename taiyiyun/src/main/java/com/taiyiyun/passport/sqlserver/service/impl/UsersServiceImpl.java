package com.taiyiyun.passport.sqlserver.service.impl;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.taiyiyun.passport.dao.ISharePointDao;
import com.taiyiyun.passport.exception.DefinedError;
import com.taiyiyun.passport.language.PackBundle;
import com.taiyiyun.passport.po.SharePoint;
import com.taiyiyun.passport.sqlserver.comm.UsersInfoBody;
import com.taiyiyun.passport.sqlserver.dao.IAntifakeAddressStoreDao;
import com.taiyiyun.passport.sqlserver.dao.IDeveloperDao;
import com.taiyiyun.passport.sqlserver.dao.IEntityDao;
import com.taiyiyun.passport.sqlserver.dao.IUserEntityDao;
import com.taiyiyun.passport.sqlserver.dao.IUsersDao;
import com.taiyiyun.passport.sqlserver.po.AntifakeAddressStore;
import com.taiyiyun.passport.sqlserver.po.Developer;
import com.taiyiyun.passport.sqlserver.po.Entity;
import com.taiyiyun.passport.sqlserver.po.UserEntity;
import com.taiyiyun.passport.sqlserver.po.Users;
import com.taiyiyun.passport.sqlserver.service.IUsersService;
import com.taiyiyun.passport.util.AESUtil;
import com.taiyiyun.passport.util.MD5Util;
import com.taiyiyun.passport.util.Misc;
import com.taiyiyun.passport.util.StringUtil;

@Service
public class UsersServiceImpl implements IUsersService {
	
	@Autowired
	private IDeveloperDao developerDao;
	
	@Autowired
	private IEntityDao entityDao;
	
	@Autowired
	private IUsersDao usersDao;
	
	@Autowired
	private IUserEntityDao userEntityDao;
	
	@Autowired
	private ISharePointDao sharePointDao;
	
	@Autowired
	private IAntifakeAddressStoreDao antifakeAddressStoreDao;

	@Override
	public boolean checkMobileUnique(String mobileSuffix, String mobilePrefix) {
		List<Users> userList = usersDao.findByMobile(mobileSuffix, mobilePrefix);
		return (userList == null || userList.size() == 0) ? true : false;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, transactionManager = "transactionManagerSqlserver", rollbackFor = Exception.class)
	public UsersInfoBody saveUser(Map<String, String> paramMap, Developer developer, AntifakeAddressStore address) {
		UsersInfoBody body = new UsersInfoBody();
		String password = paramMap.get("password");
		String mobile = paramMap.get("Mobile");
		
		int count = usersDao.getTotalCount();
		
		Users bean = new Users();
		bean.setUUID(Misc.getUUID());
		bean.setNikeName(String.format("%s%s%s", address.getSymbol(), count, 80000));
		bean.setMobile(mobile);
		bean.setCreationTime(new Timestamp(new Date().getTime()));
		bean.setStatus(1);
		bean.setVersion("1.0.0");
		bean.setHeadPicture("");
		bean.setAddress(address.getAddress());
		bean.setDefaultUserEntity("");
		bean.setAppID(developer.getAppId());
		bean.setPwd(MD5Util.MD5Encode(password, false));
		bean.setPersionFailCount(0);
		bean.setArtificialAuth(1);
		int rsCount = usersDao.save(bean);
		if (rsCount > 0) {
			Entity entity = new Entity();
			entity.setCreationTime(new Timestamp(new Date().getTime()));
			entity.setEntityId(Misc.getUUID());
			entity.setEntityUnqueId("");
			entity.setTypeId(0);
			entity.setStatus(1);
			entity.setGradeId(0);
			entity.setEntityName("");
			entityDao.insertSelective(entity);
			
			UserEntity userEntity = new UserEntity();
			userEntity.setDefaultAmount(10000);
			userEntity.setEntityId(entity.getEntityId());
			userEntity.setFailMessage("");
			userEntity.setStatus(1);
			userEntity.setUserEntityId(Misc.getUUID());
			userEntity.setUuid(bean.getUUID());
			userEntity.setAuditChannel(1);
			userEntityDao.insertSelective(userEntity);
			
			antifakeAddressStoreDao.changeStatusUsed(bean.getAddress());
			
			SharePoint sharePoint = new SharePoint();
			sharePoint.setBalance(BigDecimal.ZERO);
			sharePoint.setUuid(bean.getUUID());
			sharePointDao.save(sharePoint);
			
			List<UserEntity> userEntitiys = new ArrayList<UserEntity>();
			userEntitiys.add(userEntity);
			
			List<Entity> entitiys = new ArrayList<Entity>();
			entitiys.add(entity);
			
			body.setUsers(bean);
			body.setUserEntitys(userEntitiys);
			body.setEntitys(entitiys);
			body.setPrivKey(AESUtil.encrypt(developer.getAppKey(), developer.getAppSecret()));
		}
		return body;
	}

	@Override
	public Users getUserByAddress(String address) {
		if (StringUtil.isEmpty(address)) {
			return null;
		}
		return usersDao.getUserFromAddress(address);
	}

	@Override
	public int updateUser(Users users) {
		return usersDao.updateUser(users);
	}

	@Override
	public Users getByMobile(String mobilePrefix, String mobileSuffix) {
		if (StringUtils.isEmpty(mobilePrefix) && StringUtils.isEmpty(mobileSuffix)) {
			return null;
		}
		
		List<Users> userList = usersDao.findByMobile(mobileSuffix, mobilePrefix);
		return userList == null || userList.size() <= 0 ? null : userList.get(0);
	}

	@Override
	public int updateUserPwd(Users users) {
		if (users == null || StringUtils.isEmpty(users.getUUID())) {
			return 0;
		}
		return usersDao.updateUserPwd(users);
	}

	@Override
	public Map<String, Object> login(PackBundle bundle, Developer developer, String mobile, String password) throws DefinedError {
		if (bundle == null || developer == null || StringUtils.isEmpty(mobile) || StringUtils.isEmpty(password)) {
			throw new IllegalArgumentException("params is null at login method");
		}
		
		String[] phones = mobile.split("-");
		String mobilePrefix = "86";
		String mobileSuffix = null;
		if (phones.length == 1) {
			mobileSuffix = phones[0];
		} else {
			mobilePrefix = phones[0];
			mobileSuffix = phones[1];
		}
		
		List<Users> usersList = usersDao.findByMobile(mobileSuffix, mobilePrefix);
		if (usersList == null || usersList.size() == 0) {
			throw new DefinedError.OtherException(bundle.getString("user.not.register"), null);
		}
		
		Users users = usersList.get(0);
		
		if (users.getStatus() == 0) {
			throw new DefinedError.OtherException(bundle.getString("user.not.register"), null);
		}
		
		String pwd = AESUtil.decrypt(password, developer.getAppSecret());
		if (!MD5Util.MD5Encode(pwd, false).equals(users.getPwd())) {
			throw new DefinedError.OtherException(bundle.getString("need.user.password.error"), null);
		}
		
				
		return null;
	}
	
}
