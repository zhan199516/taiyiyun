package com.taiyiyun.passport.aliyun.sts;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.Callback;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.http.ProtocolType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.aliyuncs.sts.model.v20150401.AssumeRoleRequest;
import com.aliyuncs.sts.model.v20150401.AssumeRoleResponse;
import org.apache.commons.lang.RandomStringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;

public class StsServiceSample {

	private static final String ACCESS_KEY_ID = "LTAIbM9ovlDnnVs0";
	private static final String ACCESS_KEY_SECRET = "yzt1AgFwQSoM7IBYwkTsc5UZrrk2ZU";
	private static final String ROLE_ARN = "acs:ram::1897774526758764:role/gxhz-test";
	private static final String POLICY = " {\n" +
			"  \"Version\": \"1\",\n" +
			"  \"Statement\": [\n" +
			"    {\n" +
			"      \"Effect\": \"Allow\",\n" +
			"      \"Action\": [\n" +
			"        \"oss:*\"\n" +
			"      ],\n" +
			"      \"Resource\": [\n" +
			"        \"*\"\n" +
			"      ]\n" +
			"    }\n" +
			"  ]\n" +
			"}";
	
	// 目前只有"cn-hangzhou"这个region可用, 不要使用填写其他region的值
	  public static final String REGION_CN_HANGZHOU = "cn-hangzhou";
	  // 当前 STS API 版本
	  public static final String STS_API_VERSION = "2015-04-01";
	  public static AssumeRoleResponse assumeRole() throws ClientException {
	  	String id = RandomStringUtils.randomNumeric(4);
		  String roleSessionName = "taiyiyun-"+new Date().getTime()+id;
	      // 创建一个 Aliyun Acs Client, 用于发起 OpenAPI 请求
	      IClientProfile profile = DefaultProfile.getProfile(REGION_CN_HANGZHOU, ACCESS_KEY_ID, ACCESS_KEY_SECRET);
	      DefaultAcsClient client = new DefaultAcsClient(profile);
	      // 创建一个 AssumeRoleRequest 并设置请求参数
	      final AssumeRoleRequest request = new AssumeRoleRequest();
	      request.setVersion(STS_API_VERSION);
	      request.setMethod(MethodType.POST);
	      request.setProtocol(ProtocolType.HTTPS);
	      request.setRoleArn(ROLE_ARN);
	      request.setRoleSessionName(roleSessionName);
	      request.setPolicy(POLICY);
	      // 发起请求，并得到response
	      final AssumeRoleResponse response = client.getAcsResponse(request);
	      return response;

	  }

	  public static void up(String accessKeyId,String accessKeySecret) {
		  // endpoint以杭州为例，其它region请按实际情况填写
		  String endpoint = "http://oss-cn-beijing.aliyuncs.com";
// 创建OSSClient实例
		  OSSClient ossClient = new OSSClient(endpoint, ACCESS_KEY_ID, ACCESS_KEY_SECRET);
// 上传
		  InputStream inputStream = null;
		  try {
			  inputStream = new URL("http://c.hiphotos.baidu.com/image/pic/item/7af40ad162d9f2d3252dba25a0ec8a136327cc38.jpg").openStream();
		  } catch (IOException e) {
			  e.printStackTrace();
		  }
String callbackUrl = "http://47.95.220.249:8080/api/chat/uploadCallback";


		  long time = Calendar.getInstance().getTimeInMillis();
		  PutObjectRequest putObjectRequest = new PutObjectRequest("taiyiyun", time+".jpg",
				  inputStream);

		  Callback callback = new Callback();
		  callback.setCallbackUrl(callbackUrl);
		  callback.setCallbackHost("oss-cn-hangzhou.aliyuncs.com");
		  callback.setCallbackBody("bucket=${bucket}&object=${object}&etag=${etag}&size=${size}&mimeType=${mimeType}&imageInfo.height=${imageInfo.height}&imageInfo.width=${imageInfo.width}&imageInfo.format=${imageInfo.format}");
//		  callback.setCallbackBodyType(CallbackBodyType.JSON);
		  putObjectRequest.setCallback(callback);
		  PutObjectResult putObjectResult = ossClient.putObject(putObjectRequest);

//		  ossClient.putObject("taiyiyun", time+"", inputStream);
// 关闭client
		  ossClient.shutdown();
	  }


	public static void main(String[] args) {

		try {
			 AssumeRoleResponse response = assumeRole();
			System.out.println("Access Key Id: " + response.getCredentials().getAccessKeyId());
			System.out.println("Access Key Secret: " + response.getCredentials().getAccessKeySecret());
			up(response.getCredentials().getAccessKeyId(),response.getCredentials().getAccessKeySecret());
			System.out.println("Expiration: " + response.getCredentials().getExpiration());
			System.out.println("Access Key Id: " + response.getCredentials().getAccessKeyId());
			System.out.println("Access Key Secret: " + response.getCredentials().getAccessKeySecret());
			System.out.println("Security Token: " + response.getCredentials().getSecurityToken());
		} catch (ClientException e) {
			System.out.println("Failed to get a token.");
			System.out.println("Error code: " + e.getErrCode());
			System.out.println("Error message: " + e.getErrMsg());
		}
	}
}

