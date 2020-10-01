package com.taiyiyun.passport.service;

import com.taiyiyun.passport.language.PackBundle;
import com.taiyiyun.passport.po.BaseResult;
import com.taiyiyun.passport.po.user.TipoffDto;

public interface IPublicTipoffService {

	public BaseResult<Integer> addTipoff(PackBundle bundle, TipoffDto tipoffDto);
}
