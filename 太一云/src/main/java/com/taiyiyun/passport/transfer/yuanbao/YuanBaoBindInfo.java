package com.taiyiyun.passport.transfer.yuanbao;

import java.io.Serializable;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * 元宝绑定信息实体
 * 
 * @author LiuQing
 */
public final class YuanBaoBindInfo implements Serializable {

	@JSONField(serialize = false)
	private static final long serialVersionUID = -8545179277611360821L;

	@JSONField(name = "data")
	private BindInfo data;

	public BindInfo getData() {
		return data;
	}

	public void setData(BindInfo data) {
		this.data = data;
	}

	public class BindInfo implements Serializable {

		@JSONField(serialize = false)
		private static final long serialVersionUID = -7702071046107200781L;



		public String getUserKey() {
			return userKey;
		}

		public void setUserKey(String userKey) {
			this.userKey = userKey;
		}

		public String getUserSecretKey() {
			return userSecretKey;
		}

		public void setUserSecretKey(String userSecretKey) {
			this.userSecretKey = userSecretKey;
		}

		public String getUniqueKey() {
			return uniqueKey;
		}

		public void setUniqueKey(String uniqueKey) {
			this.uniqueKey = uniqueKey;
		}


		public String getAppKey() {
			return appKey;
		}

		public void setAppKey(String appKey) {
			this.appKey = appKey;
		}

		public String getBindTime() {
			return bindTime;
		}

		public void setBindTime(String bindTime) {
			this.bindTime = bindTime;
		}

		public String getUuid() {
			return uuid;
		}

		public void setUuid(String uuid) {
			this.uuid = uuid;
		}

		public String getSign() {
			return sign;
		}

		public void setSign(String sign) {
			this.sign = sign;
		}

		@JSONField(name = "user_key")
		private String userKey;  //用户key

		@JSONField(name = "user_secret_key")
		private String userSecretKey;  //用户私钥，用于签名

		@JSONField(name = "unique_key")
		private String uniqueKey; //用户身份证号的base64

		@JSONField(name = "app_key")
		private String appKey; //应用key，用户服务器查询私钥验证签名

		@JSONField(name = "bindTime")
		private String bindTime; //绑定时间，用于之后的数据获取数据的签名

		@JSONField(name = "uuid")
		private String uuid;  //等于上次服务器发给用户的一串数字

		@JSONField(name = "sign")
		private String sign; //签名

	}
}
