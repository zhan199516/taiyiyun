package com.taiyiyun.passport.service.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.taiyiyun.passport.consts.Config;
import com.taiyiyun.passport.language.PackBundle;
import com.taiyiyun.passport.service.ICustomerService;
import com.taiyiyun.passport.service.IRedisService;
import com.taiyiyun.passport.sqlserver.comm.PictureType;
import com.taiyiyun.passport.sqlserver.dao.IEntityDao;
import com.taiyiyun.passport.sqlserver.dao.IPictureDao;
import com.taiyiyun.passport.sqlserver.dao.IUserEntityDao;
import com.taiyiyun.passport.sqlserver.po.CustomerDetailPerson;
import com.taiyiyun.passport.sqlserver.po.Entity;
import com.taiyiyun.passport.sqlserver.po.Picture;
import com.taiyiyun.passport.sqlserver.po.UserEntity;
import com.taiyiyun.passport.sqlserver.po.Users;
import com.taiyiyun.passport.sqlserver.service.IRealNameAuthService;
import com.taiyiyun.passport.sqlserver.service.IUsersService;
import com.taiyiyun.passport.util.DateUtil;
import com.taiyiyun.passport.util.FileUtil;
import com.taiyiyun.passport.util.StringUtil;
import com.taiyiyun.passport.util.YunSignHelper;

@Service
public class CustomerServiceImpl implements ICustomerService {
	
	private final static String VERIFYCODE_EXPIRED_KEY = "redis:verifycode:expired:";
	
	private final static String VERIFYCODE_PERIOD_KEY = "redis:verifycode:period:";
	
	private final Logger logger = Logger.getLogger(getClass());
	
	@Autowired
	private IUserEntityDao userEntityDao;
	
	@Autowired
	private IEntityDao entityDao;
	
	@Autowired
	private IRedisService redisService;
	
	@Autowired
	private IUsersService usersService;
	
	@Autowired
	private IPictureDao pictureDao;
	
	@Autowired
	private IRealNameAuthService realNameAuthService;

	@Override
	public boolean checkAllowSendVerifyCode(String mobile) {
		if (StringUtil.isEmpty(mobile)) {
			return false;
		}
		
		Long period = redisService.get(VERIFYCODE_PERIOD_KEY + mobile);
		if (period != null && (System.currentTimeMillis() - period) < Config.getInt("sms.verifycode.period.time", 60000)) {
			return false;
		}
		
		return true;
	}

	@Override
	public void saveVerifyCode(String mobile, String verifyCode) throws Exception {
		if (StringUtil.isEmpty(mobile) || StringUtil.isEmpty(verifyCode)) {
			throw new NullPointerException("mobile is null or verifyCode is null");
		}
		
		redisService.put(VERIFYCODE_EXPIRED_KEY + mobile, verifyCode, Config.getInt("sms.verifycode.expired.time", 600000));
		redisService.put(VERIFYCODE_PERIOD_KEY + mobile, System.currentTimeMillis(), Config.getInt("sms.verifycode.period.time", 60000));
	}

	@Override
	public boolean verifyCode(String mobile, String code, boolean clear) {
		if (StringUtil.isEmpty(mobile) || StringUtil.isEmpty(code)) {
			return false;
		}
		
		String verifyCode = redisService.get(VERIFYCODE_EXPIRED_KEY + mobile);
		if (verifyCode != null && verifyCode.equals(code)) {
			if (clear) {
				redisService.evict(VERIFYCODE_EXPIRED_KEY + mobile);
				redisService.evict(VERIFYCODE_PERIOD_KEY + mobile);
			}
			return true;
		}
		
		return false;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, transactionManager="transactionManagerSqlserver", rollbackFor = Exception.class)
	public Map<String, Object> realNameAuth(PackBundle bundle, Users users, String idcardNo, String facePotoImgData, String userName, String national) throws Exception {
	
		Map<String, Object> rsMap = new HashMap<String, Object>();
		
		try {
			YunSignHelper.AuthResponse response = YunSignHelper.realNameAuth(users.getUUID(), userName, idcardNo, facePotoImgData, national);
			if (!response.isSuccess()) {
				rsMap.put("success", false);
				rsMap.put("error", response.getMessage());
				return rsMap;
			}
			
			CustomerDetailPerson cdp = new CustomerDetailPerson();
	        cdp.setAddress(users.getAddress());
	        cdp.setIDCard(idcardNo);
	        cdp.setEntityName(userName);
	        cdp.setName(userName);
	       
			return realNameAuthService.addCustomerPerson(cdp, bundle, facePotoImgData);
		
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("云签认证失败", e);
			throw e;
		}
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, transactionManager="transactionManagerSqlserver", rollbackFor = Exception.class)
	public Map<String, Object> realNameAuth(PackBundle bundle, Users users, String appKey, String identityNumber, String userName, String validDateStart, String validDateEnd,
			String nation, String sex, String homeAddr, String birthDay, String facePhotoBase64, String sign, String national) throws Exception{
		
		CustomerDetailPerson cdp = new CustomerDetailPerson();
        cdp.setAppKey(appKey);
        cdp.setAddress(users.getAddress());
        cdp.setIDCard(identityNumber);
        cdp.setEntityName(userName);
        cdp.setName(userName);
        cdp.setValidDateStart(validDateStart);
        cdp.setValidDateEnd(validDateEnd);
        cdp.setNation(nation);
        cdp.setSex(sex);
        cdp.setHomeAddr(homeAddr);
        cdp.setBirthDay(birthDay);
        
        boolean authResult = false;
        /*暂时不用公安一所的认证
        GongAnHelper.AuthResponse gongAnResult = GongAnHelper.realNameAuth(bundle, userName, identityNumber, validDateStart, validDateEnd, facePhotoBase64);
        if (gongAnResult.getStatus() == 1) {
        	return toReturnMap(gongAnResult.isSuccess(), gongAnResult.getStatus(), gongAnResult.getMessage(), null);
        } else if (gongAnResult.getStatus() == 2){
        	YunSignHelper.AuthResponse yunSignResult = YunSignHelper.realNameAuth(users.getUUID(), userName, identityNumber, facePhotoBase64, national);
        	if (!yunSignResult.isSuccess()) {
        		return toReturnMap(yunSignResult.isSuccess(), 2, yunSignResult.getMessage(), null);
			}
        	authResult = true;
        } else if (gongAnResult.isSuccess()){
        	authResult = true;
        }*/
        
        YunSignHelper.AuthResponse yunSignResult = YunSignHelper.realNameAuth(users.getUUID(), userName, identityNumber, facePhotoBase64, national);
    	if (!yunSignResult.isSuccess()) {
    		return toReturnMap(yunSignResult.isSuccess(), 2, yunSignResult.getMessage(), null);
		}
    	authResult = true;
        
        if (authResult) {
        	return realNameAuthService.addCustomerPerson(cdp, bundle, facePhotoBase64);
        }
		return toReturnMap(false, 2, bundle.getString("authenticate.failed"), null);
	}
	
	private Map<String, Object> toReturnMap(boolean success, int errorCode, String error, Object data) {
		HashMap<String, Object> rst = new HashMap<>();
        rst.put("success", success);
        rst.put("error", error);
        rst.put("data", data);
        rst.put("errorCode", errorCode);
        return rst;
	}

	@Override
	public Map<String, Object> checkAuthStatus(PackBundle bundle, Users users) {
		
		Map<String, Object> params = new HashMap<>();
		params.put("uuid", users.getUUID());
		params.put("typeId", 1);
		List<UserEntity> userEntityList = userEntityDao.selectUserEntityByUUIDAndTypeId(params);
		
		Map<String, Object> dataMap = new HashMap<String,Object>();
		List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
		
		if (userEntityList == null || userEntityList.size() == 0) { //未认证
			dataMap.put("EntityStatus", 0);
			users.setArtificialAuth(1);
			
			if (users.getArtificialAuth() == 0) {//不允许人工认证
				Map<String, Object> infoMap = new HashMap<String, Object>();
				infoMap.put("Status", bundle.getString("authenticate.goto"));
				infoMap.put("Title", "公安部快捷认证");
				dataList.add(infoMap);
			} else {
				Map<String, Object> infoMap = new HashMap<String, Object>();
				infoMap.put("Status", bundle.getString("authenticate.goto"));
				infoMap.put("Title", "公安部快捷认证");
				dataList.add(infoMap);
				
				infoMap = new HashMap<String, Object>();
				infoMap.put("Status", bundle.getString("authenticate.goto"));
				infoMap.put("Title", "人工认证");
				dataList.add(infoMap);
			}
		} else {
			UserEntity userEntity = userEntityList.get(0);
			if (userEntity.getStatus() == 0) { //认证中
				dataMap.put("EntityStatus", 1);
				
				Map<String, Object> infoMap = new HashMap<String, Object>();
				infoMap.put("Status", bundle.getString("authenticate.goto"));
				infoMap.put("Title", "公安部快捷认证");
				dataList.add(infoMap);
				
				infoMap = new HashMap<String, Object>();
				infoMap.put("Status", bundle.getString("authenticate.progressing"));
				infoMap.put("Title", "人工认证");
				dataList.add(infoMap);
			} else if (userEntity.getStatus() == 1) {//认证通过
				dataMap.put("EntityStatus", 2);
				Entity entity = entityDao.getById(userEntity.getEntityId());
				
				Map<String, Object> infoMap = new HashMap<String, Object>();
				infoMap.put("Status", bundle.getString("authenticate.success"));
				infoMap.put("Title", entity.getEntityName());
				dataList.add(infoMap);
			} else if (userEntity.getStatus() == 2) {//认证失败
				dataMap.put("EntityStatus", 3);
				dataMap.put("FailMessage", bundle.getString("third.authenFeedback") + userEntity.getFailMessage());
				
				Map<String, Object> infoMap = new HashMap<String, Object>();
				infoMap.put("Status", bundle.getString("authenticate.goto"));
				infoMap.put("Title", "公安部快捷认证");
				dataList.add(infoMap);
				
				infoMap = new HashMap<String, Object>();
				infoMap.put("Status", bundle.getString("authenticate.failed"));
				infoMap.put("Title", "人工认证");
				dataList.add(infoMap);
			}
		}
		dataMap.put("Menus", dataList);
		return toReturnMap(true, 0, null, dataMap);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, transactionManager = "transactionManagerSqlserver", rollbackFor = Exception.class)
	public Map<String, Object> saveArtificialAuthInfo(PackBundle bundle, Users users, String idCard, String passport, String trueName, MultipartFile file1, MultipartFile file2, MultipartFile file3, MultipartFile file4) {
		if (bundle == null || users == null || file1 == null || file2 == null || file3 == null || file4 == null) {
			throw new IllegalArgumentException("params is null");
		}
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("uuid", users.getUUID());
		params.put("typeId", 1);
		List<UserEntity> userEntitiys = userEntityDao.selectUserEntityByUUIDAndTypeId(params);
		
		UserEntity userEntity = null;
		if (userEntitiys != null && userEntitiys.size() > 0) {
			userEntity = userEntitiys.get(0);
			if (userEntity.getStatus() == 1) {
				return toReturnMap(false, 1, bundle.getString("user.has.checked"), null);
			}
		}
		
		Map<Integer, String> pictures = saveArtificialAuthFile(userEntity, file1, file2, file3, file4);
		
		CustomerDetailPerson cdp = new CustomerDetailPerson();
		cdp.setIDCard(idCard);
		cdp.setName(trueName);
		cdp.setPassport(passport);
		cdp.setAddress(users.getAddress());
		cdp.setEntityName(trueName);
       
		return realNameAuthService.addCustomerPerson(cdp, users, bundle, pictures, 0);
	}
	
	private Map<Integer, String> saveArtificialAuthFile(UserEntity userEntity, MultipartFile file1, MultipartFile file2, MultipartFile file3, MultipartFile file4) {
		Map<Integer, String> rsMap = new HashMap<Integer, String>();
		
		try {
			String root = Config.get("uploadpath");
			Date now = new Date();
	        
			//保存文件1
	        PictureType pictureType = PictureType.get(FileUtil.getFileNameWithoutExtension(file1.getOriginalFilename()));
			if (pictureType == null) {
				throw new NullPointerException("pictureType is null");
			}
			
			String fileName = DateUtil.toString(now, "yyyy") + File.separator + DateUtil.toString(now, "MM") + File.separator + DateUtil.toString(now, "dd") + File.separator + StringUtil.getUUID() + ".jpg";
			if (userEntity != null) {
				Picture picture = pictureDao.getByEntityIdAndTypeId(userEntity.getEntityId(), pictureType.getPtypeId());
				if (picture != null) {
					fileName = picture.getFileName();
				}
			}
			
			FileUtil.copyTo(file1.getBytes(), root + fileName);
			rsMap.put(pictureType.getPtypeId(), fileName);
			
			//保存文件2
	        pictureType = PictureType.get(FileUtil.getFileNameWithoutExtension(file2.getOriginalFilename()));
			if (pictureType == null) {
				throw new NullPointerException("pictureType is null");
			}
			
			fileName = DateUtil.toString(now, "yyyy") + File.separator + DateUtil.toString(now, "MM") + File.separator + DateUtil.toString(now, "dd") + File.separator + StringUtil.getUUID() + ".jpg";
			if (userEntity != null) {
				Picture picture = pictureDao.getByEntityIdAndTypeId(userEntity.getEntityId(), pictureType.getPtypeId());
				if (picture != null) {
					fileName = picture.getFileName();
				}
			}
			
			FileUtil.copyTo(file2.getBytes(), root + fileName);
			rsMap.put(pictureType.getPtypeId(), fileName);
			
			//保存文件3
	        pictureType = PictureType.get(FileUtil.getFileNameWithoutExtension(file3.getOriginalFilename()));
			if (pictureType == null) {
				throw new NullPointerException("pictureType is null");
			}
			
			fileName = DateUtil.toString(now, "yyyy") + File.separator + DateUtil.toString(now, "MM") + File.separator + DateUtil.toString(now, "dd") + File.separator + StringUtil.getUUID() + ".jpg";
			if (userEntity != null) {
				Picture picture = pictureDao.getByEntityIdAndTypeId(userEntity.getEntityId(), pictureType.getPtypeId());
				if (picture != null) {
					fileName = picture.getFileName();
				}
			}
			
			FileUtil.copyTo(file3.getBytes(), root + fileName);
			rsMap.put(pictureType.getPtypeId(), fileName);
			
			//保存文件4
	        pictureType = PictureType.get(FileUtil.getFileNameWithoutExtension(file4.getOriginalFilename()));
			if (pictureType == null) {
				throw new NullPointerException("pictureType is null");
			}
			
			fileName = DateUtil.toString(now, "yyyy") + File.separator + DateUtil.toString(now, "MM") + File.separator + DateUtil.toString(now, "dd") + File.separator + StringUtil.getUUID() + ".jpg";
			if (userEntity != null) {
				Picture picture = pictureDao.getByEntityIdAndTypeId(userEntity.getEntityId(), pictureType.getPtypeId());
				if (picture != null) {
					fileName = picture.getFileName();
				}
			}
			
			FileUtil.copyTo(file4.getBytes(), root + fileName);
			rsMap.put(pictureType.getPtypeId(), fileName);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			throw new RuntimeException(e.getMessage());
		}
		return rsMap;
	}

}
