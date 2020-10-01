package com.taiyiyun.passport.service;

import com.taiyiyun.passport.bean.NotEnoughException;
import com.taiyiyun.passport.language.PackBundle;
import com.taiyiyun.passport.po.SharePoint;

import java.math.BigDecimal;

public interface ISharePointService {

	public SharePoint getSharePoint(String uuid);

	public int save(SharePoint bean);

	public void transferSharePoint(PackBundle packBundle, String fromUUID, String toUUID, BigDecimal balance) throws NotEnoughException;

}
