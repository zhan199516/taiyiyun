package com.taiyiyun.passport.service.impl;

import java.math.BigDecimal;

import javax.annotation.Resource;

import com.taiyiyun.passport.language.PackBundle;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.taiyiyun.passport.bean.NotEnoughException;
import com.taiyiyun.passport.bean.NotOpenedExcption;
import com.taiyiyun.passport.bean.TransferFailedException;
import com.taiyiyun.passport.dao.ISharePointDao;
import com.taiyiyun.passport.po.SharePoint;
import com.taiyiyun.passport.service.ISharePointService;

@Service
public class SharePointServiceImpl implements ISharePointService {
	
	@Resource
	private ISharePointDao dao;
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = {RuntimeException.class})
	public void transferSharePoint(PackBundle packBundle, String fromUUID, String toUUID, BigDecimal balance) throws NotOpenedExcption, NotEnoughException, TransferFailedException {
		SharePoint fromSharePoint = dao.getSharePointByUUID(fromUUID);
		
		if(null == fromSharePoint) {
			//throw new NotOpenedExcption("用户未开通共享点");
			String errMsg = "用户未开通共享点";
			if(packBundle != null) {
				errMsg = packBundle.getString("sharepoint.transfersharepoint.usernotopensharepoint");
			}
			throw new NotOpenedExcption(errMsg);
		}
		
		if(fromSharePoint.getBalance().compareTo(BigDecimal.ZERO) == 0 || fromSharePoint.getBalance().subtract(balance).compareTo(BigDecimal.ZERO) == -1) {
			//throw new NotEnoughException("共享点余额不足");
			String errMsg = "共享点余额不足";
			if(packBundle != null) {
				errMsg = packBundle.getString("sharepoint.transfersharepoint.sharepointnotenogh");
			}
			throw new NotEnoughException(errMsg);
		}
		
		int deductCount = dao.deductSharePoint(fromUUID, balance);
		//逻辑未完成
		
		if(deductCount == 1) {
			int addCount = dao.addSharePoint(toUUID, balance);
			if(addCount <= 0) {
				//throw new TransferFailedException("转账失败");
				String errMsg = "转账失败";
				if(packBundle != null) {
					errMsg = packBundle.getString("transfer.doacceptmoney.fail.transfailed");
				}
				throw new TransferFailedException(errMsg);
			}
		}
		
	}
	
	@Override
	public SharePoint getSharePoint(String uuid) {
		return dao.getSharePointByUUID(uuid);
	}
	
	@Override
	public int save(SharePoint bean) {
		return dao.save(bean);
	}

}
