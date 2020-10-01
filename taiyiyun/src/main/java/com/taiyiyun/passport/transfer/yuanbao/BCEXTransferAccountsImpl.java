package com.taiyiyun.passport.transfer.yuanbao;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.taiyiyun.passport.exception.DefinedError;
import com.taiyiyun.passport.language.PackBundle;
import com.taiyiyun.passport.po.ThirdAppUserBindExt;
import com.taiyiyun.passport.transfer.Answer.*;
import com.taiyiyun.passport.transfer.Ask.AskBody;
import com.taiyiyun.passport.transfer.Ask.AskParam;
import com.taiyiyun.passport.util.DateUtil;
import com.taiyiyun.passport.util.DateUtil.Format;
import com.taiyiyun.passport.util.HttpClientUtil;
import com.taiyiyun.passport.util.MD5Util;
import com.taiyiyun.passport.util.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

/**
 * 元宝转账接口
 * @author LiuQing
 *
 */
@Component("BCEXTransferAccountsImpl")
public class BCEXTransferAccountsImpl implements IBCEXTransferAccounts {


	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public String getAppId() {
		return "bcex";
	}

	@Override
	public List<CoinInfo> askForCoinInfos(String baseUrl) {
		String url = baseUrl + YuanBaoApis.URL_COINS; //正式版本上线时候，使用正式的。
//		String url = "http://wu.yuanbaobang.com/oauth_api/ybexcoins";

		try{
			String result = HttpClientUtil.doGet(url, null);
			if (!StringUtil.isEmpty(result)) {
				JSONObject resBody = JSONObject.parseObject(result);
				JSONObject resData = resBody.getJSONObject("data");
				if("success".equals(resData.getString("msg"))){
					JSONArray array= resData.getJSONArray("coins");
					List<CoinInfo> list = new ArrayList<>();
					for(int i = 0; i < array.size(); i++){
						CoinInfo info = array.getObject(i, CoinInfo.class);
						list.add(info);
					}
					return list;
				} else {
					logger.error("cannot find any coins");
				}
			}
			else {
				logger.error("cannot find any coins");
			}
		} catch (Exception ex){
			logger.error("error while getting coin infos", ex);
		}

		return null;
	}

	@Override
	public LimitResult askForLimit(PackBundle packBundle, AskBody body,BigDecimal amount) throws DefinedError {
		Map<String, String> params = new HashMap<String, String>();
		params.put(PARAM_ACCESS_TOKEN, body.getToken());
		params.put(PARAM_USER_KEY, body.getUserKey());
		if (amount == null) {
			amount = BigDecimal.ZERO;
		}
		params.put("amount", String.valueOf(amount));
		try{
			logger.info("yuanBaoTransferAccountsImpl.askForLimit\r\n" + JSON.toJSONString(params));

			String result = HttpClientUtil.doPost(body.getCoinCallUrl() + YuanBaoApis.URL_SET_LIMIT, params);

			logger.info("yuanBaoTransferAccountsImpl.askForLimit\r\n" + result);

			JSONObject resBody = JSONObject.parseObject(result);
			return resBody.getObject("data", LimitResult.class);
		}
		catch(Exception ex){
			logger.error(ex.getMessage());
			String errMsg = "元宝网平台返回错误，请稍后重试";
			if(packBundle != null) {
				errMsg = packBundle.getString("yuanbao.return.error");
			}
			throw new DefinedError.ThirdErrorException(errMsg, ex.getMessage());
		}
	}


	/**
	 * 获取有效期code
	 **/
	private AuthorizeResult askForCode(PackBundle packBundle, AskBody body) throws DefinedError {

		String relateKey = body.getRelateId();
		if(StringUtil.isEmptyOrBlank(relateKey)){
			relateKey = "1";
		}


		String signData = "app_secret=" + body.getAppSecret() +
				"&response_type=" + "code" +
				"&authorized=yes" +
				"&flag=" + DateUtil.toString(new Date(), Format.DATE_MINUTE)+
				"&bindtime=" + relateKey;


		String sign = MD5Util.MD5Encode(signData, false);

		Map<String, String> params = new HashMap<String, String>();
		params.put("response_type", "code");
		params.put("authorized", "yes");
		params.put("client_id", body.getUserKey());
		params.put("app_key", body.getAppKey());
		params.put("sign", sign);

		try{
			logger.info("yuanBaoTransferAccountsImpl.askForCode\r\n" + JSON.toJSONString(params));

			String result = HttpClientUtil.doPost(body.getCoinCallUrl() + YuanBaoApis.URL_AUTHORIZE, params);

			logger.info("yuanBaoTransferAccountsImpl.askForCode\r\n" + result);

			JSONObject resBody = JSONObject.parseObject(result);
			if(resBody == null){
				//throw new DefinedError.ThirdErrorException("元宝网平台返回错误，请稍后重试", "");
				String errMsg = "元宝网平台返回错误，请稍后重试";
				if(packBundle != null) {
					errMsg = packBundle.getString("yuanbao.return.error");
				}
				throw new DefinedError.ThirdErrorException(errMsg, "");
			}

			return resBody.getObject("data", AuthorizeResult.class);
		}
		catch(Exception ex){
			logger.error(ex.getMessage());
			//throw new DefinedError.ThirdErrorException("元宝网平台返回错误，请稍后重试", ex.getMessage());
			String errMsg = "元宝网平台返回错误，请稍后重试";
			if(packBundle != null) {
				errMsg = packBundle.getString("yuanbao.return.error");
			}
			throw new DefinedError.ThirdErrorException(errMsg, "");
		}
	}


	@Override
	public String buildBindUrl(ThirdAppUserBindExt ext){
		String uri = ext.getCoinBindUrl();
//		String appKey = ext.getAppKey();
//		String uuid = ext.getAppSecret();
//		String appSecret =  ext.getUuid();
//
//		String param = "app_key=" + appKey +
//				"&uuid=" + uuid;
//
//		String strForSign = "app_secret=" + appSecret + "&uuid=" + uuid;
//
//		String md5Str = MD5Util.MD5Encode(strForSign, false);
//		param = param + "&sign=" + md5Str;
//		return uri + "?" + param;
		return uri;
	}

	/**
	 * 获取token
	 */
	@Override
	public AskBody askForToken(PackBundle packBundle, ThirdAppUserBindExt ext, String relateId) throws DefinedError {
		AskBody body = new AskBody();
		body.setAppKey(ext.getAppKey());
		body.setAppSecret(ext.getAppSecret());
		body.setUserKey(ext.getUserKey());
		body.setUserSecretKey(ext.getUserSecretKey());
		body.setCoinBindUrl(ext.getCoinBindUrl());
		body.setCoinCallUrl(ext.getCoinCallUrl());
		if(relateId == null){
			body.setRelateId("");
		} else {
			body.setRelateId(relateId);
		}
		long startTime = System.currentTimeMillis();
		AuthorizeResult authorizeResult = askForCode(packBundle, body);
		logger.info("askForCode result:" + JSON.toJSONString(authorizeResult) + "--消耗时间:"+(System.currentTimeMillis() - startTime));
		if(!authorizeResult.isSuccess()){
			//throw new DefinedError.ThirdUserKeyInvalid("元宝网平台绑定已经过期", authorizeResult.getErrorCode().toString());
//			String errMsg = "元宝网平台绑定已经过期";
//			if(packBundle != null) {
//				errMsg = packBundle.getString("yuanbao.bind.expired");
//			}
			Integer errorCode = authorizeResult.getErrorCode();
			String errMsg = "第三方平台连接失败";
			if(packBundle != null) {
				errMsg = packBundle.getString("thirdappuserbind.fillcoinplatform.connectthirdfailed");
			}
			throw new DefinedError.ThirdErrorException(errMsg+"("+errorCode+")", String.valueOf(errorCode));
		}
		body.setCode(authorizeResult.getAuthorizationCode());
		String signData = "app_secret=" + body.getAppSecret() +
				"&grant_type=" + "authorization_code" +
				"&user_secret_key=" + body.getUserSecretKey();
		String sign = MD5Util.MD5Encode(signData, false);
		Map<String, String> params = new HashMap<String, String>();
		params.put("grant_type", "authorization_code");
		params.put("code", body.getCode());
		params.put("user_key", body.getUserKey());
		params.put("app_key", body.getAppKey());
		params.put("sign", sign);
		try{
			logger.info("yuanBaoTransferAccountsImpl.askForToken\r\n" + JSON.toJSONString(params));
			long startTime1 = System.currentTimeMillis();
			String result = HttpClientUtil.doPost(body.getCoinCallUrl() + YuanBaoApis.URL_TOKEN, params);
			logger.info("yuanBaoTransferAccountsImpl.askForToken\r\n" + result);
			JSONObject resBody = JSONObject.parseObject(result);
			TokenGetResult tokenGetResult = resBody.getObject("data", TokenGetResult.class);
			if(tokenGetResult.isSuccess()){
				logger.info("askForToken success:"+ YuanBaoApis.URL_TOKEN+"----消耗时间:"+(System.currentTimeMillis() - startTime1));
				body.setToken(tokenGetResult.getAccess_token());
			} else {
				body.setToken(null);
				String errMsg = "获取token失败";
				if(packBundle != null) {
					errMsg = packBundle.getString("yuanbao.token.failed");
				}
				throw new DefinedError.ThirdErrorException(errMsg, tokenGetResult.getErrorCode().toString());
			}
			return body;
		}
		catch(Exception ex){
			logger.error(ex.getMessage());
			//throw new DefinedError.ThirdErrorException("元宝网平台返回错误，请稍后重试", ex.getMessage());
			String errMsg = "元宝网平台返回错误，请稍后重试";
			if(packBundle != null) {
				errMsg = packBundle.getString("yuanbao.return.error");
			}
			throw new DefinedError.ThirdErrorException(errMsg, ex.getMessage());
		}


	}


	@Override
	public AssetResult askForAsset(PackBundle packBundle, AskBody body) throws DefinedError {
		Map<String, String> params = new HashMap<String, String>();
		params.put(PARAM_ACCESS_TOKEN, body.getToken());
		params.put(PARAM_USER_KEY, body.getUserKey());
		try{
			logger.info("yuanBaoTransferAccountsImpl.askForAsset\r\n" + JSON.toJSONString(params));
			String result = HttpClientUtil.doPost(body.getCoinCallUrl() + YuanBaoApis.URL_USER_INFO, params);
			logger.info("yuanBaoTransferAccountsImpl.askForAsset\r\n" + result);
			JSONObject resBody = JSONObject.parseObject(result);
			return resBody.getObject("data", AssetResult.class);
		}
		catch(Exception ex){
			logger.error(ex.getMessage());
			//throw new DefinedError.ThirdErrorException("元宝网平台返回错误，请稍后重试", ex.getMessage());
			String errMsg = "元宝网平台返回错误，请稍后重试";
			if(packBundle != null) {
				errMsg = packBundle.getString("yuanbao.return.error");
			}
			throw new DefinedError.ThirdErrorException(errMsg, ex.getMessage());
		}
	}

	@Override
	public AskFeeResult askForFee(PackBundle packBundle, AskBody body, String coinName, BigDecimal amount) throws DefinedError {
		Map<String, String> params = new HashMap<String, String>();
		params.put(PARAM_ACCESS_TOKEN, body.getToken());
		params.put(PARAM_USER_KEY, body.getUserKey());
		params.put(PARAM_COIN_NAME, coinName);
		params.put("amount", amount.toString());
		try{
			logger.info("yuanBaoTransferAccountsImpl.askForFee\r\n" + JSON.toJSONString(params));
			String result = HttpClientUtil.doPost(body.getCoinCallUrl() + YuanBaoApis.URL_SERVICE_CHARGE, params);
			logger.info("yuanBaoTransferAccountsImpl.askForFee\r\n" + result);
			JSONObject resBody = JSONObject.parseObject(result);
			AskFeeResult askFeeResult = resBody.getObject("data", AskFeeResult.class);
			return askFeeResult;
		}
		catch(Exception ex){
			logger.error(ex.getMessage());
			//throw new DefinedError.ThirdErrorException("元宝网平台返回错误，请稍后重试", ex.getMessage());
			String errMsg = "元宝网平台返回错误，请稍后重试";
			if(packBundle != null) {
				errMsg = packBundle.getString("yuanbao.return.error");
			}
			throw new DefinedError.ThirdErrorException(errMsg, ex.getMessage());
		}
	}

	@Override
	public FrozenResult askForFroze(PackBundle packBundle, AskBody body, String coinName, BigDecimal fronum) throws DefinedError {
		Map<String, String> params = new HashMap<String, String>();
		params.put(PARAM_ACCESS_TOKEN, body.getToken());
		params.put(PARAM_USER_KEY, body.getUserKey());
		params.put(PARAM_COIN_NAME, coinName);
		params.put("fronum", fronum.toString());
		try{
			logger.info("yuanBaoTransferAccountsImpl.askForFroze\r\n" + JSON.toJSONString(params));
			String result = HttpClientUtil.doPost(body.getCoinCallUrl() + YuanBaoApis.URL_FROZEN_MONEY, params);
			logger.info("yuanBaoTransferAccountsImpl.askForFroze\r\n" + result);
			JSONObject resBody = JSONObject.parseObject(result);
			return resBody.getObject("data", FrozenResult.class);
		}
		catch(Exception ex){
			logger.error(ex.getMessage());
			//throw new DefinedError.ThirdErrorException("元宝网平台返回错误，请稍后重试", ex.getMessage());
			String errMsg = "元宝网平台返回错误，请稍后重试";
			if(packBundle != null) {
				errMsg = packBundle.getString("yuanbao.return.error");
			}
			throw new DefinedError.ThirdErrorException(errMsg, ex.getMessage());
		}


	}

	/**
	 * 批量冻结
	 * @param packBundle
	 * @param body
	 * @param coinName
	 * @param totalAmount
	 * @param askParam
	 * @return
	 * @throws DefinedError
	 */
	@Override
	public FrozenBatchResult askForBatchFroze(PackBundle packBundle, AskBody body, String coinName,BigDecimal totalAmount, List<AskParam> askParam) throws DefinedError {

		Map<String, String> params = new HashMap<String, String>();
		params.put(PARAM_ACCESS_TOKEN, body.getToken());
		params.put(PARAM_USER_KEY, body.getUserKey());
		params.put(PARAM_COIN_NAME, coinName);
		params.put("fronum", totalAmount == null?"0":totalAmount.toString());
		params.put("fronumjson", JSON.toJSONString(askParam));
		try{
			logger.info("yuanBaoTransferAccountsImpl.askForBatchFroze-begin");
			String result = HttpClientUtil.doPost(body.getCoinCallUrl() + YuanBaoApis.URL_FROZEN_BATCH_MONEY, params);
			logger.info("yuanBaoTransferAccountsImpl.askForBatchFroze-result:" + result);
			JSONObject resBody = JSONObject.parseObject(result);
			return resBody.getObject("data", FrozenBatchResult.class);
		}
		catch(Exception ex){
			logger.error(ex.getMessage());
			//throw new DefinedError.ThirdErrorException("元宝网平台返回错误，请稍后重试", ex.getMessage());
			String errMsg = "元宝网平台返回错误，请稍后重试";
			if(packBundle != null) {
				errMsg = packBundle.getString("yuanbao.return.error");
			}
			throw new DefinedError.ThirdErrorException(errMsg, ex.getMessage());
		}
	}

	@Override
	public FrozenResult askForUnfreeze(PackBundle packBundle, AskBody body, String coinName, BigDecimal fronum, Long froId) throws DefinedError {
		Map<String, String> params = new HashMap<String, String>();
		params.put(PARAM_ACCESS_TOKEN, body.getToken());
		params.put(PARAM_USER_KEY, body.getUserKey());
		params.put(PARAM_COIN_NAME, coinName);
		params.put("refronum", fronum.toString());
		params.put("froId", froId.toString());

		try{
			logger.info("yuanBaoTransferAccountsImpl.askForUnfreeze\r\n" + JSON.toJSONString(params));
			String result = HttpClientUtil.doPost(body.getCoinCallUrl() + YuanBaoApis.URL_REVOKE_FROZEN_MONEY, params);
			logger.info("yuanBaoTransferAccountsImpl.askForUnfreeze\r\n" + result);
			JSONObject resBody = JSONObject.parseObject(result);
			return resBody.getObject("data", FrozenResult.class);
		}
		catch(Exception ex){
			logger.error(ex.getMessage());
			//throw new DefinedError.ThirdErrorException("元宝网平台返回错误，请稍后重试", ex.getMessage());
            String errMsg = "元宝网平台返回错误，请稍后重试";
            if(packBundle != null) {
                errMsg = packBundle.getString("yuanbao.return.error");
            }
            throw new DefinedError.ThirdErrorException(errMsg, ex.getMessage());
		}
	}

	/**
	 * 转账
	 * @param body body
	 * @param coinName 币种id
	 * @param fronum 转账总数
	 * @param froId 冻结id
	 * @param fee 转账手续费
	 * @param toUserKey 对方userId
	 * @return
	 */
	@Override
	public TransferResult askForTransfer(PackBundle packBundle, AskBody body, String coinName, BigDecimal fronum, Long froId, BigDecimal fee, String toUserKey) throws DefinedError {
		Map<String, String> params = new HashMap<String, String>();
		params.put(PARAM_ACCESS_TOKEN, body.getToken());
		params.put("from_user_key", body.getUserKey());
		params.put(PARAM_COIN_NAME, coinName);
		params.put("fronum", fronum.toString());
		params.put("froId", froId.toString());
		params.put("fee", fee.toString());
		params.put("to_user_key", toUserKey);

		try{
			logger.info("yuanBaoTransferAccountsImpl.askForTransfer\r\n" + JSON.toJSONString(params));
			String result = HttpClientUtil.doPost(body.getCoinCallUrl() + YuanBaoApis.URL_TRANSFER_ACCOUNTS, params);
			logger.info("yuanBaoTransferAccountsImpl.askForTransfer\r\n" + result);
			JSONObject resBody = JSONObject.parseObject(result);
			TransferResult transferResult = resBody.getObject("data", TransferResult.class);
			return transferResult;
		}
		catch(Exception ex){
			logger.error(ex.getMessage());
			//throw new DefinedError.ThirdErrorException("元宝网平台返回错误，请稍后重试", ex.getMessage());
            String errMsg = "元宝网平台返回错误，请稍后重试";
            if(packBundle != null) {
                errMsg = packBundle.getString("yuanbao.return.error");
            }
            throw new DefinedError.ThirdErrorException(errMsg, ex.getMessage());
		}
	}

	@Override
	public ErrorCodeResult askForUnbind(PackBundle packBundle, AskBody body) throws DefinedError {

		Map<String, String> params = new HashMap<String, String>();
		params.put(PARAM_ACCESS_TOKEN, body.getToken());
		params.put(PARAM_USER_KEY, body.getUserKey());
		params.put("unbind", "yes");

		try{
			logger.info("yuanBaoTransferAccountsImpl.askForUnbind\r\n" + JSON.toJSONString(params));
			String result = HttpClientUtil.doPost(body.getCoinCallUrl() + YuanBaoApis.URL_UNBIND, params);
			logger.info("yuanBaoTransferAccountsImpl.askForUnbind\r\n" + result);
			JSONObject resBody = JSONObject.parseObject(result);
			return resBody.getObject("data", FrozenResult.class);
		}
		catch(Exception ex){
			logger.error(ex.getMessage());
			//throw new DefinedError.ThirdErrorException("元宝网平台返回错误，请稍后重试", ex.getMessage());
            String errMsg = "元宝网平台返回错误，请稍后重试";
            if(packBundle != null) {
                errMsg = packBundle.getString("yuanbao.return.error");
            }
            throw new DefinedError.ThirdErrorException(errMsg, ex.getMessage());
		}


	}
	private static final String PARAM_ACCESS_TOKEN = "access_token";
	private static final String PARAM_USER_KEY = "user_key";
	private static final String PARAM_COIN_NAME = "coinname";
	
}
