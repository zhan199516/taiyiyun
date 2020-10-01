package com.taiyiyun.passport.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.taiyiyun.passport.consts.Config;
import com.taiyiyun.passport.dao.ILoginAppDao;
import com.taiyiyun.passport.dao.IThirdAppUserBindDao;
import com.taiyiyun.passport.exception.DefinedError;
import com.taiyiyun.passport.language.PackBundle;
import com.taiyiyun.passport.po.LoginApp;
import com.taiyiyun.passport.po.ThirdAppUserBind;
import com.taiyiyun.passport.po.ThirdAppUserBindExt;
import com.taiyiyun.passport.po.asset.Coin;
import com.taiyiyun.passport.po.asset.CoinPlatform;
import com.taiyiyun.passport.po.asset.Fee;
import com.taiyiyun.passport.service.IThirdAppUserBindService;
import com.taiyiyun.passport.service.transfer.AssetCache;
import com.taiyiyun.passport.service.transfer.ThirdAppManager;
import com.taiyiyun.passport.transfer.Answer.AskFeeResult;
import com.taiyiyun.passport.transfer.Answer.AssetResult;
import com.taiyiyun.passport.transfer.Answer.CoinOwn;
import com.taiyiyun.passport.transfer.Answer.ErrorCodeResult;
import com.taiyiyun.passport.transfer.Ask.AskBody;
import com.taiyiyun.passport.transfer.ITransferAccounts;
import com.taiyiyun.passport.transfer.po.BindEntity;
import com.taiyiyun.passport.transfer.po.CallbackStatus;
import com.taiyiyun.passport.util.HttpClientUtil;
import com.taiyiyun.passport.util.MD5Signature;
import com.taiyiyun.passport.util.MD5Util;
import org.apache.commons.lang.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import sun.misc.BASE64Decoder;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by okdos on 2017/7/14.
 */
@Service
public class ThirdAppUserBindServiceImp implements IThirdAppUserBindService {

    @Resource
    IThirdAppUserBindDao thirdAppUserBindDao;

    @Resource
    ILoginAppDao loginAppDao;


    public final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public CallbackStatus checkValidParam(PackBundle packBundle, ThirdAppUserBindExt appUserBindExt, BindEntity entity) {
        CallbackStatus status = new CallbackStatus();

        if(appUserBindExt == null){
            status.setStatus(2);
            String errMsg = "查询不到绑定信息";
            if(packBundle != null) {
                errMsg = packBundle.getString("transfer.postmoney.nobindinfo");
            }
            status.setError(errMsg);
            return status;
        }

        StringBuilder builder = new StringBuilder();
        builder.append("app_secret=")
                .append(appUserBindExt.getAppSecret())
                .append("&user_key=").append(entity.getUserKey())
                .append("&user_sercret_key=").append(entity.getUserSecretKey())
                .append("&uuid=").append(entity.getUuid());

        if(!MD5Util.MD5Encode(builder.toString(), false).equals(entity.getSign())){
            status.setStatus(5);
            //status.setError("签名不正确");
            String errMsg = "签名不正确";
            if(packBundle != null) {
                errMsg = packBundle.getString("failed.sign");
            }
            status.setError(errMsg);
            return status;
        }

        //允许重复绑定
//        if(appUserBindExt.isBinded()){
//            status.setStatus(3);
//            status.setError("用户已经绑定，无法重复绑定");
//            return status;
//        }

//简化操作，暂时不考虑过期问题
//        if(appUserBindExt.getBindExpireTime() == null || appUserBindExt.getBindExpireTime().getTime() < new Date().getTime()){
//            status.setStatus(4);
//            status.setError("用户绑定请求未准备或已经过期");
//            return status;
//        }


        status.setStatus(0);
        return status;
    }

    @Override
    public boolean checkIfIdCardMatch(String uuid, String base64IdCard) throws DefinedError {

        if("yes".equals(Config.get("bind.ignoreIdCard"))){
            return true;
        }

        if(base64IdCard == null){
            throw new DefinedError.ConvertException(null, "");
        }

        String idCard = null;
        BASE64Decoder decoder = new BASE64Decoder();
        try{
            byte[] b = decoder.decodeBuffer(base64IdCard);
            idCard = new String(b, "utf-8");
        } catch(Exception ex){
            throw new DefinedError.ConvertException(null, ex.getMessage());
        }

        Map<String,String> params = new HashMap<String,String>();
        params.put("Appkey", Config.get("appKey"));
        params.put("uuid", uuid);
        params.put("idCard", idCard);

        LoginApp loginApp = loginAppDao.getItem(Config.get("appKey"));

        String sign = MD5Signature.signMd5(params,loginApp.getAppSecret());
        params.put("Sign", sign);

        String responseText = HttpClientUtil.doGet(Config.get("remote.url") + "/Api/CustomerUserInfo/IsMatch", params);

        JSONObject jsonObject = JSON.parseObject(responseText);
        return jsonObject.getBooleanValue("success");
    }

    @Override
    public ThirdAppUserBindExt getOneExtByCondition(String uuid, String appId, String appKey) {
        ThirdAppUserBindExt ext = thirdAppUserBindDao.getOneExtByCondition(uuid, appId, appKey);
        if(ext != null){
            ext.setUuid(uuid);
        }

        return ext;
    }

    @Override
    public int updateBinding(ThirdAppUserBind thirdAppUserBind) {
        try{
            //插入数据前先更新该用户所有绑定状态-为未绑定
            String uuid = thirdAppUserBind.getUuid();
            String appid = thirdAppUserBind.getAppId();
            thirdAppUserBindDao.updateUnBinding(uuid,appid);
            //插入新的绑定
            int changeCount = thirdAppUserBindDao.updateBinding(thirdAppUserBind);
            return changeCount;
        } catch(Exception ex){
            logger.error(ex.getMessage());
            throw ex;
        }
    }

    @Override
    public int updateUnBinding(PackBundle packBundle, String uuid, String appId) throws DefinedError {


        ThirdAppUserBindExt ext = thirdAppUserBindDao.getOneExtByCondition(uuid, appId, null);
        if(ext == null || !ext.isBinded()){
            //throw new DefinedError.ThirdNotFoundException("未绑定指定服务", null);
            String errMsg = "未绑定指定服务";
            if(packBundle != null) {
                errMsg = packBundle.getString("transfer.postmoney.nobindinfo");
            }
            throw new DefinedError.ThirdNotFoundException(errMsg, null);
        }

        ext.setUuid(uuid);

        ITransferAccounts accounts = ThirdAppManager.getInstance().getAccountById(appId);

        if(accounts == null){
            throw new DefinedError.ThirdNotFoundException(null, null);
        }

        String relateId = thirdAppUserBindDao.getRelateId(appId);
        AskBody body = accounts.askForToken(packBundle, ext, relateId);

        if(body == null || body.getToken() == null){
            throw new DefinedError.ThirdNotFoundException(null, null);
        }

        ErrorCodeResult errorResult = accounts.askForUnbind(packBundle, body);

        if(errorResult.isSuccess()){
            return thirdAppUserBindDao.updateUnBinding(uuid, appId);
        } else {
            //throw new DefinedError.ThirdRefuseException("执行失败", errorResult.getErrorCode().toString());
            String errMsg = "执行失败";
            if(packBundle != null) {
                errMsg = packBundle.getString("failed.execute");
            }
            throw new DefinedError.ThirdRefuseException(errMsg, errorResult.getErrorCode().toString());
        }
    }

    @Override
    public int updateToken(ThirdAppUserBind thirdAppUserBind) {
        try{
            return thirdAppUserBindDao.updateToken(thirdAppUserBind);
        } catch(Exception ex){
            logger.error(ex.getMessage());
            throw ex;
        }
    }

    @Override
    public List<CoinPlatform> getCoinPlatform(PackBundle packBundle, String uuid) {

        List<ThirdAppUserBindExt> list = thirdAppUserBindDao.getExtByCondition(uuid, null, null);

        List<CoinPlatform> listPlatform = new ArrayList<>();

        for(int i = 0; i < list.size(); i++){
            ThirdAppUserBindExt ext = list.get(i);
            ext.setUuid(uuid);

            CoinPlatform coinPlatform = new CoinPlatform();
            listPlatform.add(coinPlatform);

            fillCoinPlatform(packBundle, coinPlatform, ext);
        }

        return listPlatform;
    }


    private void fillCoinPlatform(PackBundle packBundle, CoinPlatform coinPlatform, ThirdAppUserBindExt ext) {

        coinPlatform.setPlatformId(ext.getAppId());
        coinPlatform.setPlatformName(ext.getAppName());
        coinPlatform.setPlatformLogo(ext.getLogoUrl());
        coinPlatform.setThirdpartAppkey(ext.getThirdpartAppkey());


        ITransferAccounts accounts = ThirdAppManager.getInstance().getAccountById(ext.getAppId());
        if(accounts == null){
            return;
        }

        coinPlatform.setBindUri(accounts.buildBindUrl(ext));

        if(!ext.isBinded()){
            return;
        }

        coinPlatform.setBinded(true);

        try{
            String relateId = thirdAppUserBindDao.getRelateId(ext.getAppId());
            AskBody body = accounts.askForToken(packBundle, ext, relateId);

            if(body != null && body.getToken() != null){

                coinPlatform.setBinded(false);
                AssetResult assetResult = accounts.askForAsset(packBundle, body);

                if(assetResult.isSuccess()){

                    coinPlatform.setAvailableRmb(assetResult.getSurplus());
                    coinPlatform.setBinded(true);

                    if(assetResult.getCoinList() != null){
                        for(int j = 0; j < assetResult.getCoinList().size(); j++){
                            CoinOwn coinOwn = assetResult.getCoinList().get(j);
                            Coin coin = new Coin();
                            coinPlatform.getCoinList().add(coin);

                            coin.setAsset(coinOwn.getAvailableBalance().add(coinOwn.getBlockedFunds()));
                            coin.setCoinId(coinOwn.getCoinName());
                            coin.setName(coinOwn.getChinaName());
                            coin.setFeeBalance(coinOwn.getFee());
                            coin.setFeeMin(new BigDecimal(0));
                            coin.setFrozen(coinOwn.getBlockedFunds());
                            coin.setLogo(coinOwn.getLogo());
                            if(AssetCache.getInstance().getCoin(ext.getAppId(), coinOwn.getCoinName()) != null) {
                                coin.setLogo(AssetCache.getInstance().getCoin(ext.getAppId(), coinOwn.getCoinName()).getLogo());
                            } else {
                                if(StringUtils.isNotEmpty(ext.getCoinCallUrl())) {
                                    coin.setLogo(ext.getCoinCallUrl() + coinOwn.getLogo());
                                }
                            }
                            coin.setWorthRmb(coinOwn.getValuation());
                            //AssetCache.getInstance().storeCoin(ext.getAppId(), coin);
                        }
                    }
                } else {
                    if (assetResult.getErrorCode() == 1007){
                        throw new DefinedError.ThirdUserKeyInvalid(null, null);
                    }
                }
            } else {
                //coinPlatform.setPlatformCheck("第三方平台连接失败");
                String errMsg = "第三方平台连接失败";
                if(packBundle != null) {
                    errMsg = packBundle.getString("thirdappuserbind.fillcoinplatform.connectthirdfailed");
                }
                coinPlatform.setPlatformCheck(errMsg);
            }
        }
        catch (DefinedError.ThirdErrorException ex){
            logger.error(ex.getMessage());
            //coinPlatform.setPlatformCheck(ext.getAppName() + "平台返回错误，请重试");
            String errMsg = ext.getAppName() + "平台返回错误，请重试";
            if(packBundle != null) {
                errMsg = packBundle.getString("thirdappuserbind.fillcoinplatform.returnerror", ext.getAppName());
            }
            coinPlatform.setPlatformCheck(errMsg);
            //todo 删除绑定信息
            //thirdAppUserBindDao.updateUnBinding(ext.getUuid(), coinPlatform.getPlatformId());
        }
        catch (DefinedError.ThirdUserKeyInvalid ex){
            logger.error(ex.getMessage());
            //coinPlatform.setPlatformCheck(ext.getAppName() + "平台绑定已经过期，请重新绑定");
            String errMsg = ext.getAppName() + "平台绑定已经过期，请重新绑定";
            if(packBundle != null) {
                errMsg = packBundle.getString("thirdappuserbind.fillcoinplatform.platformbindexpired", ext.getAppName());
            }
            coinPlatform.setPlatformCheck(errMsg);
            //todo 删除绑定信息
            thirdAppUserBindDao.updateUnBinding(ext.getUuid(), coinPlatform.getPlatformId());
        }
        catch (DefinedError ex){
            logger.error(ex.getMessage());
            //coinPlatform.setPlatformCheck(ext.getAppName() + "平台连接失败");
            String errMsg = ext.getAppName() + "平台连接失败";
            if(packBundle != null) {
                errMsg = packBundle.getString("thirdappuserbind.fillcoinplatform.platformconnectfailed", ext.getAppName());
            }
            coinPlatform.setPlatformCheck(errMsg);
        }


    }

    @Override
    public Fee getFee(PackBundle packBundle, String uuid, String platformId, String coinId, BigDecimal amount) throws DefinedError {

        ThirdAppUserBindExt ext = thirdAppUserBindDao.getOneExtByCondition(uuid, platformId, null);

        if(ext == null){
            return null;
        }

        ext.setUuid(uuid);

        ITransferAccounts accounts = ThirdAppManager.getInstance().getAccountById(platformId);
        if(accounts == null){
            return null;
        }


        String relateId = thirdAppUserBindDao.getRelateId(platformId);
        AskBody body = accounts.askForToken(packBundle, ext, relateId);
        if(body.getToken() == null){
            return null;
        }

        AskFeeResult feeResult = accounts.askForFee(packBundle, body, coinId, amount);
        if(!feeResult.isSuccess()){
            return null;
        }

        Fee fee = new Fee();
        fee.setUserId(uuid);
        fee.setPlatformId(platformId);
        fee.setCoinId(coinId);
        fee.setCoinName(null);
        fee.setAmount(amount);

        fee.setFee(feeResult.getTotalFee().setScale(8, BigDecimal.ROUND_FLOOR));
        fee.setSaving(feeResult.getBalance());

        return fee;
    }

}
