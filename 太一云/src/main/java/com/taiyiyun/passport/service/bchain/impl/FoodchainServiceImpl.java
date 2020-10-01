package com.taiyiyun.passport.service.bchain.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.taiyiyun.passport.bean.UserDetails;
import com.taiyiyun.passport.consts.EnumStatus;
import com.taiyiyun.passport.po.BaseResult;
import com.taiyiyun.passport.po.bchain.FoodPointDto;
import com.taiyiyun.passport.po.bchain.FoodProductDto;
import com.taiyiyun.passport.service.bchain.IFoodchainService;
import com.taiyiyun.passport.util.HttpClientUtil;
import com.taiyiyun.passport.util.MD5Util;
import com.taiyiyun.passport.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class FoodchainServiceImpl implements IFoodchainService {

	public final Logger logger = LoggerFactory.getLogger(getClass());

	@Value("#{fchainConfig['fchain.appkey']}")
	private String appKey;

	@Value("#{fchainConfig['fchain.appsecret']}")
	private String appSecret;

	@Value("#{fchainConfig['fchain.domain.name']}")
	private String domainName;

	@Value("#{fchainConfig['fchain.interface.product.list.url']}")
	private String productListUrl;

	@Value("#{fchainConfig['fchain.interface.point.list.url']}")
	private String pointListUrl;

	@Value("#{fchainConfig['fchain.interface.point.count.url']}")
	private String pointCountUrl;

	/**
	 * 获取食品产品列表
	 *
	 * @param foodProductDto
	 * @return
	 */
	@Override
	public BaseResult<Object> getFoodProducts(UserDetails userDetails, FoodProductDto foodProductDto) {
		BaseResult<Object> resultData = new BaseResult<>();
		String userId = userDetails.getUserId();
		//生成签名
		String secret=appSecret+"Appkey"+appKey+"UserId"+userId+appSecret;
		String sign = MD5Util.MD5Encode(secret, true);
		Long timestamp = foodProductDto.getTimestamp();
		//请求参数
		Map<String,String> paramMap = new HashMap<>();
		if (timestamp != null && timestamp.longValue() > 0){
			paramMap.put("TimeTag",String.valueOf(timestamp));
		}
//		paramMap.put("Start",String.valueOf(timestamp == null? 1:timestamp));
		Integer rows = foodProductDto.getRows();
		paramMap.put("PageSize",rows == null?"10":String.valueOf(rows));
		paramMap.put("AppKey",appKey);
		paramMap.put("Sign",sign);
		logger.info("getFoodProducts params:" + JSON.toJSONString(paramMap));
		//请求url地址
		String requestUrl = domainName + productListUrl;
		String resultJson = HttpClientUtil.doHttpsPost(requestUrl,paramMap,"UTF-8");
		logger.info("getFoodProducts response:" + resultJson);
		if (!StringUtil.isEmpty(resultJson)) {
			JSONObject resBody = JSONObject.parseObject(resultJson);
			Boolean statusBoo = resBody.getBoolean("success");
			String error =  resBody.getString("message");
			Object resData = resBody.get("data");
			//设置放回结果
			if (statusBoo != null && statusBoo == true){
				resultData.setStatus(EnumStatus.ZORO.getIndex());
			}
			else{
				resultData.setStatus(EnumStatus.ONE.getIndex());
			}
			resultData.setError(error);
			resultData.setData(resData == null?"":resData);
		}
		return resultData;
	}

	/**
	 * 获取食品积分列表
	 *
	 * @param foodPointDto
	 * @return
	 */
	@Override
	public BaseResult<Object> getPointRecords(UserDetails userDetails, FoodPointDto foodPointDto) {
		BaseResult<Object> resultData = new BaseResult<>();
		String userId = userDetails.getUserId();
		String mobile = userDetails.getMobile();
		//生成签名
		String secret=appSecret+"Appkey"+appKey+"UserId"+userId+appSecret;
		String sign = MD5Util.MD5Encode(secret, true);
		Long timestamp = foodPointDto.getTimestamp();
		//请求参数
		Map<String,String> paramMap = new HashMap<>();
		if (timestamp != null && timestamp.longValue() > 0){
			paramMap.put("TimeTag",String.valueOf(timestamp));
		}
//		paramMap.put("Start",String.valueOf(timestamp == null? 0:timestamp));
		paramMap.put("UserId",userId);
		paramMap.put("TelNum",mobile);
		paramMap.put("OperType",foodPointDto.getOperType() == null?"0":String.valueOf(foodPointDto.getOperType()));
		paramMap.put("PageSize",foodPointDto.getRows() == null?"10":String.valueOf(foodPointDto.getRows()));
		paramMap.put("AppKey",appKey);
		paramMap.put("Sign",sign);
		logger.info("getPointRecords params:" + JSON.toJSONString(paramMap));
		//请求url地址
		String requestUrl = domainName + pointListUrl;
		String resultJson = HttpClientUtil.doHttpsPost(requestUrl,paramMap,"UTF-8");
		logger.info("getPointRecords response:" + resultJson);
		if (!StringUtil.isEmpty(resultJson)) {
			JSONObject resBody = JSONObject.parseObject(resultJson);
			Boolean statusBoo = resBody.getBoolean("success");
			String error =  resBody.getString("message");
			Object resData = resBody.get("data");
			//设置放回结果
			if (statusBoo != null && statusBoo == true){
				resultData.setStatus(EnumStatus.ZORO.getIndex());
			}
			else{
				resultData.setStatus(EnumStatus.ONE.getIndex());
			}
			resultData.setError(error);
			resultData.setData(resData);
		}
		return resultData;
	}

	/**
	 * 获取积分总数量
	 *
	 * @param foodPointDto
	 * @return
	 */
	@Override
	public BaseResult<Object> getPointCount(UserDetails userDetails,FoodPointDto foodPointDto) {
		BaseResult<Object> resultData = new BaseResult<>();
		String userId = userDetails.getUserId();
		String mobile = userDetails.getMobile();
		//生成签名
		String secret = appSecret+"Appkey"+appKey+"UserId"+userId+appSecret;
		String sign = MD5Util.MD5Encode(secret, true);
		//请求参数
		Map<String,String> paramMap = new HashMap<>();
		paramMap.put("UserId",userId);
		paramMap.put("TelNum",mobile);
		paramMap.put("AppKey",appKey);
		paramMap.put("Sign",sign);
		logger.info("getPointCount params:" + JSON.toJSONString(paramMap));
		//请求url地址
		String requestUrl = domainName + pointCountUrl;
		String resultJson = HttpClientUtil.doHttpsPost(requestUrl,paramMap,"UTF-8");
		logger.info("getPointCount response:" + resultJson);
		if (!StringUtil.isEmpty(resultJson)) {
			JSONObject resBody = JSONObject.parseObject(resultJson);
			Boolean statusBoo = resBody.getBoolean("success");
			String error =  resBody.getString("message");
			JSONObject resData = resBody.getJSONObject("data");
			//设置放回结果
			if (statusBoo != null && statusBoo == true){
				resultData.setStatus(EnumStatus.ZORO.getIndex());
			}
			else{
				resultData.setStatus(EnumStatus.ONE.getIndex());
			}
			resultData.setError(error);
			resultData.setData(resData);
		}
		return resultData;
	}
}
