package com.taiyiyun.passport.service.impl;

import com.taiyiyun.passport.dao.IPublicUserDao;
import com.taiyiyun.passport.dao.IThirdAppDao;
import com.taiyiyun.passport.po.PublicUser;
import com.taiyiyun.passport.po.ThirdApp;
import com.taiyiyun.passport.service.IThirdAppService;
import com.taiyiyun.passport.sqlserver.dao.IEntityDao;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ThirdAppServiceImpl implements IThirdAppService {
	private static final String TAG = "?platform=sharepassport";
	@Resource
	private IThirdAppDao dao;
	@Resource
	private IEntityDao entityDao;
	@Resource
	private IPublicUserDao publicUserDao;
	
	@Override
	public List<ThirdApp> getByUserId(String userId){
		List<ThirdApp> thirdAppList = dao.getByUserId(userId);
//		if(thirdAppList != null && thirdAppList.size() > 0) {
//			for(ThirdApp app : thirdAppList) {
//				if(StringUtils.equals("bcex", app.getAppId())) {
//					PublicUser publicUser = publicUserDao.getByUserId(userId);
//					if(publicUser != null && StringUtils.isNotEmpty(publicUser.getUuid())) {
//						int count = entityDao.selectEntityHasAuthed(publicUser.getUuid());
//						if(count == 0) {
//							String appUrl = app.getAppUrl();
//							if(StringUtils.isNotEmpty(appUrl) && StringUtils.contains(appUrl, TAG)) {
//								app.setAppUrl(appUrl.substring(0, appUrl.lastIndexOf(TAG)));
//							}
//						}
//					}
//				}
//			}
//		}
		return thirdAppList;
	}
}
