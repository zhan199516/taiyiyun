package com.taiyiyun.passport.transfer.allcoin;

import com.taiyiyun.passport.exception.DefinedError;
import com.taiyiyun.passport.language.PackBundle;
import com.taiyiyun.passport.po.ThirdAppUserBindExt;
import com.taiyiyun.passport.transfer.Answer.*;
import com.taiyiyun.passport.transfer.Ask.AskBody;
import com.taiyiyun.passport.transfer.Ask.AskParam;
import com.taiyiyun.passport.transfer.ITransferAccounts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * 元宝转账接口
 * @author LiuQing
 *
 */
@Component("allcoinTransferAccountsImpl")
public class AllcoinTransferAccountsImpl implements ITransferAccounts {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public String getAppId() {
		return "allcoin";
	}




	@Override
	public String buildBindUrl(ThirdAppUserBindExt ext){
		//return "http://47.93.77.13/test/sharepassport.html";

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
//
//		return uri + "?" + param;
		return uri;
	}

	/**
	 * 获取token
	 */
	@Override
	public AskBody askForToken(PackBundle packBundle, ThirdAppUserBindExt ext, String relateId) {
		AskBody body = new AskBody();
		body.setAppKey(ext.getAppKey());
		body.setAppSecret(ext.getAppSecret());
		body.setUserKey(ext.getUserKey());
		body.setUserSecretKey(ext.getUserSecretKey());
		body.setRelateId(relateId);

		return body;
	}


	@Override
	public AssetResult askForAsset(PackBundle packBundle, AskBody body){
		return null;
	}

	@Override
	public AskFeeResult askForFee(PackBundle packBundle, AskBody body, String coinName, BigDecimal amount){
		return null;
	}

	@Override
	public FrozenResult askForFroze(PackBundle packBundle, AskBody body, String coinName, BigDecimal fronum){
		return null;
	}

	@Override
	public FrozenBatchResult askForBatchFroze(PackBundle packBundle, AskBody body, String coinName, BigDecimal totalAmount, List<AskParam> askParams) throws DefinedError {
		return null;
	}

	@Override
	public FrozenResult askForUnfreeze(PackBundle packBundle, AskBody body, String coinName, BigDecimal fronum, Long froId){
		return null;
	}

	@Override
	public TransferResult askForTransfer(PackBundle packBundle, AskBody body, String coinName, BigDecimal fronum, Long froId, BigDecimal fee, String toUserKey){
		return null;
	}

	@Override
	public ErrorCodeResult askForUnbind(PackBundle packBundle, AskBody askBody){
		return null;
	}

	@Override
	public List<CoinInfo> askForCoinInfos(String baseUrl) {
		return null;
	}

	@Override
	public LimitResult askForLimit(PackBundle packBundle, AskBody body, BigDecimal amount) throws DefinedError {
		return null;
	}


	private static final String PARAM_ACCESS_TOKEN = "access_token";
	private static final String PARAM_USER_KEY = "user_key";
	private static final String PARAM_COIN_NAME = "coinname";


	
}
