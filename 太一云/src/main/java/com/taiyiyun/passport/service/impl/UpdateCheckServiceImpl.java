package com.taiyiyun.passport.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.taiyiyun.passport.dao.IUpdateCheckDao;
import com.taiyiyun.passport.po.UpdateCheck;
import com.taiyiyun.passport.service.IUpdateCheckService;
import com.taiyiyun.passport.util.StringUtil;

@Service
public class UpdateCheckServiceImpl implements IUpdateCheckService {
	
	@Resource
	private IUpdateCheckDao dao;

	@Override
	public UpdateCheck getCurrentUpdateCheck(Integer deviceType, String version) {
		if(StringUtil.isEmpty(version) || null == deviceType) {
			return null;
		}

		UpdateCheck check = dao.getData(deviceType, version);
		if(check == null){
			List<UpdateCheck> appVersions = dao.getCurrentUpdateCheck(deviceType, version);
			if(null != appVersions && appVersions.size() > 0) {
				UpdateCheck updateCheck = appVersions.get(0);
				if(null != updateCheck) {
					if(StringUtil.isNotEmpty(updateCheck.getVersion())) {
						if(checkVersion(version, updateCheck.getVersion())) {
							return updateCheck;
						}
					}
				}

			}

			return null;
		}
		else {
			return check;
		}
	}
	
	private boolean checkVersion(String version, String srcVersion) {
		if(StringUtil.isEmpty(version) || StringUtil.isEmpty(srcVersion)) {
			return false;
		}
		
		String[] verions = version.split("\\.");
		String[] srcVersions = srcVersion.split("\\.");
		
		if(verions.length >= srcVersions.length) {
			for(int i = 0; i < srcVersions.length; i++) {
				
				if(Integer.parseInt(verions[i]) < Integer.parseInt(srcVersions[i])) {
					return true;
				}else if(Integer.parseInt(verions[i]) > Integer.parseInt(srcVersions[i])) {
					return false;
				}
			}
		}else if(verions.length < srcVersions.length) {
			for(int i = 0; i < verions.length; i++) {
				
				if(Integer.parseInt(verions[i]) < Integer.parseInt(srcVersions[i])) {
					return true;
				}else if(Integer.parseInt(verions[i]) > Integer.parseInt(srcVersions[i])) {
					return false;
				}
			}
			
			return true;
		}
		
		
		return false;
	}
	
	

}
