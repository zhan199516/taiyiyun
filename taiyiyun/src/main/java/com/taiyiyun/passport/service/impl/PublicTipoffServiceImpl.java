package com.taiyiyun.passport.service.impl;

import com.taiyiyun.passport.consts.EnumStatus;
import com.taiyiyun.passport.dao.IPublicArticleDao;
import com.taiyiyun.passport.dao.IPublicTipoffDao;
import com.taiyiyun.passport.dao.IPublicUserDao;
import com.taiyiyun.passport.dao.group.IGroupDao;
import com.taiyiyun.passport.language.PackBundle;
import com.taiyiyun.passport.po.*;
import com.taiyiyun.passport.po.group.Group;
import com.taiyiyun.passport.po.user.TipoffDto;
import com.taiyiyun.passport.service.ICodeDictionaryService;
import com.taiyiyun.passport.service.IPublicTipoffService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

@Service
public class PublicTipoffServiceImpl implements IPublicTipoffService {
	
	@Resource
	private IPublicTipoffDao publicTipoffDao;

	@Resource
	private IGroupDao groupDao;

	@Resource
	private IPublicUserDao publicUserDao;

	@Resource
	private IPublicArticleDao publicArticleDao;

	@Resource
	private ICodeDictionaryService codeDictionaryService;



	@Override
	public BaseResult<Integer> addTipoff(PackBundle bundle, TipoffDto tipoffDto) {
		BaseResult<Integer> resultData = new BaseResult<>();
		//举报类型
		//1：举报用户
		//2：举报群
		//3：举报文章
		Integer tipoffType = tipoffDto.getTipoffType();
		String tipoffId = tipoffDto.getTipoffId();
		//验证用户
		if (tipoffType != null && tipoffType == EnumStatus.ONE.getIndex()){
			PublicUser publicUser = publicUserDao.getByUserId(tipoffId);
			if (publicUser == null){
				String errMsg = "tipoff user not exist";
				if(bundle != null) {
					errMsg = bundle.getString("tipoff.target.not.exist");
				}
				resultData.setStatus(EnumStatus.ONE.getIndex());
				resultData.setError(errMsg);
				return resultData;
			}
		}
		//验证群
		if (tipoffType != null && tipoffType == EnumStatus.TWO.getIndex()){
			Group group = groupDao.selectByPrimarykey(tipoffId);
			if (group == null) {
				String errMsg = "tipoff user not exist";
				if (bundle != null) {
					errMsg = bundle.getString("tipoff.target.not.exist");
				}
				resultData.setStatus(EnumStatus.ONE.getIndex());
				resultData.setError(errMsg);
				return resultData;
			}
		}
		//验证文章
		if (tipoffType != null && tipoffType == EnumStatus.THREE.getIndex()){
			PublicArticle publicArticle = publicArticleDao.getById(tipoffId);
			if (publicArticle == null) {
				String errMsg = "tipoff user not exist";
				if (bundle != null) {
					errMsg = bundle.getString("tipoff.target.not.exist");
				}
				resultData.setStatus(EnumStatus.ONE.getIndex());
				resultData.setError(errMsg);
				return resultData;
			}
		}
		Integer illegalType = tipoffDto.getIllegalType();
		CodeDictionary codeDictionary = codeDictionaryService.getById(illegalType);
		if(codeDictionary == null) {
			String errMsg = "tipoff illegal type error";
			if(bundle != null) {
				errMsg = bundle.getString("tipoff.target.illegaltype.error");
			}
			resultData.setStatus(EnumStatus.ONE.getIndex());
			resultData.setError(errMsg);
			return resultData;
		}
		PublicTipoff publicTipoff = new PublicTipoff();
		BeanUtils.copyProperties(tipoffDto,publicTipoff);
		PublicTipoff publicTipoffResult = publicTipoffDao.getOneByUserAndTipoffId(publicTipoff);
		if (publicTipoffResult != null){
			String errMsg = "you already tip-off this user";
			if(bundle != null) {
				errMsg = bundle.getString("tipoff.target.id.exist");
			}
			resultData.setStatus(EnumStatus.ONE.getIndex());
			resultData.setError(errMsg);
			return resultData;
		}
		publicTipoff.setTipoffTime(new Date());
		int rval = publicTipoffDao.insert(publicTipoff);
		if (rval > 0) {
			resultData.setStatus(EnumStatus.ZORO.getIndex());
			resultData.setError("success");
		}
		else{
			String errMsg = "tip-off this user fail";
			if(bundle != null) {
				errMsg = bundle.getString("tipoff.target.fail");
			}
			resultData.setStatus(EnumStatus.ONE.getIndex());
			resultData.setError(errMsg);
		}
		return resultData;
	}
}
