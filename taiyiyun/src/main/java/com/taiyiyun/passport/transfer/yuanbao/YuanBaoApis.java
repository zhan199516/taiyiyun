package com.taiyiyun.passport.transfer.yuanbao;

public final class YuanBaoApis {
	
	private YuanBaoApis() {
	}

	public static final String URL_COINS = "/oauth_api/ybexcoins";

	/** 绑定接口URL */
	protected static final String URL_BIND = "/oauthapi/callbackurl";
	
	/** 获取临时CODE接口URL */
	protected static final String URL_AUTHORIZE = "/oauth_api/authorize";
	
	/** 获取临时TOKEN接口URL */
	protected static final String URL_TOKEN = "/oauth_api/getToken";
	
	/** 获取用户信息接口URL */
	protected static final String URL_USER_INFO = "/oauth_api/suInfo";
	
	/** 获取手续费接口URL */
	protected static final String URL_SERVICE_CHARGE = "/oauth_api/sscarge";
	
	/** 冻结资金接口URL */
	protected static final String URL_FROZEN_MONEY = "/oauth_api/frozen";
	/** 批量冻结资金接口URL */
	protected static final String URL_FROZEN_BATCH_MONEY = "/oauth_api/batchfrozen";
	/** 资金转账接口URL */
	protected static final String URL_TRANSFER_ACCOUNTS = "/oauth_api/taccounts";
	/** 撤回冻结资金接口URL */
	protected static final String URL_REVOKE_FROZEN_MONEY = "/oauth_api/revokef";
	
	/** 解除绑定接口URL */
	protected static final String URL_UNBIND = "/oauth_api/unbind";

	/** 设置指定用户发红包现金限额接口URL */
	protected static final String URL_SET_LIMIT = "/oauth_api/limit";
}
