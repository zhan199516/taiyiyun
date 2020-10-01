package com.taiyiyun.passport.service;

import com.taiyiyun.passport.exception.DefinedError;
import com.taiyiyun.passport.language.PackBundle;
import com.taiyiyun.passport.po.ThirdAppUserBind;
import com.taiyiyun.passport.po.ThirdAppUserBindExt;
import com.taiyiyun.passport.po.asset.CoinPlatform;
import com.taiyiyun.passport.po.asset.Fee;
import com.taiyiyun.passport.transfer.po.BindEntity;
import com.taiyiyun.passport.transfer.po.CallbackStatus;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by okdos on 2017/7/14.
 */
public interface IThirdAppUserBindService {

    CallbackStatus checkValidParam(PackBundle packBundle, ThirdAppUserBindExt appUserBindExt, BindEntity entity);

    boolean checkIfIdCardMatch(String uuid, String base64IdCard) throws DefinedError;

    ThirdAppUserBindExt getOneExtByCondition(String uuid, String appId, String appKey);

    int updateBinding(ThirdAppUserBind thirdAppUserBind);

    int updateUnBinding(PackBundle packBundle, String uuid, String appId) throws DefinedError;

    int updateToken(ThirdAppUserBind thirdAppUserBind);


    List<CoinPlatform> getCoinPlatform(PackBundle packBundle, String uuid);

    Fee getFee(PackBundle packBundle, String uuid, String platformId, String coinId, BigDecimal amount) throws DefinedError ;

}
