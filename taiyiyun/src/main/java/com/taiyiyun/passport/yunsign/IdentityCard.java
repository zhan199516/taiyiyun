package com.taiyiyun.passport.yunsign;


public class IdentityCard {
	
	public String twoItemsAuthen(String appId, String appSecretKey, String userName, String identityNumber, String imgData, String timespan, String sign) {
		
		//Date needdate = new Date();
		//long needtime = needdate.getTime();
		//String time = needtime + "";
		
	    // MD5拼接，校验
 		//String md5str = appId + "&" + identityNumber + "&"+ time + "&" + userName;
 		//String md5str1 = md5str + "&" + appSecretKey;
 		//String sign = MD5Util.MD5Encode(md5str1, "UTF-8");
 		
		//EnvUtil.SetEnv();
		String endpoint = GlobalUtil.IdentityCard_Endpoint;
		
		String[] paramName = new String[] {"appId","time","sign","signType","userName","identityNumber","imgData"};
		String[] paramValue = new String[] {appId,timespan,sign,"MD5",userName,identityNumber,imgData};
		
		return CallWebServiceUtil.CallHttpsService(endpoint, "http://wsdl.com/", "twoItemsAuthen", paramName, paramValue);
	}

}
