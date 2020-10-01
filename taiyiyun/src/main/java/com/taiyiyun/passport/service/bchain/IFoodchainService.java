package com.taiyiyun.passport.service.bchain;

import com.taiyiyun.passport.bean.UserDetails;
import com.taiyiyun.passport.po.BaseResult;
import com.taiyiyun.passport.po.bchain.FoodPointDto;
import com.taiyiyun.passport.po.bchain.FoodProductDto;

public interface IFoodchainService {

	/**
	 * 获取食品产品列表
	 * @param foodProductDto
	 * @return
	 */
	public BaseResult<Object> getFoodProducts(UserDetails userDetails, FoodProductDto foodProductDto);

	/**
	 * 获取食品积分列表
	 * @param foodPointDto
	 * @return
	 */
	public BaseResult<Object> getPointRecords(UserDetails userDetails, FoodPointDto foodPointDto);

	/**
	 * 获取积分总数量
	 * @param foodPointDto
	 * @return
	 */
	public BaseResult<Object> getPointCount(UserDetails userDetails, FoodPointDto foodPointDto);
}
