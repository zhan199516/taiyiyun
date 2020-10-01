package com.taiyiyun.passport.service;

import com.taiyiyun.passport.language.PackBundle;
import com.taiyiyun.passport.po.BaseResult;
import com.taiyiyun.passport.po.setting.UserConfig;

import java.util.Map;

public interface IPublicUserConfigService {


	/**
	 * 设置目标用户免打扰状态
	 * @param userConfig
	 * @return
	 */
	public BaseResult<Integer> configDisturbStatus(PackBundle bundle,UserConfig userConfig);

	/**
	 * 设置目标用户置顶状态
	 * @param userConfig
	 * @return
	 */
	public BaseResult<Integer> configTopStatus(PackBundle bundle, UserConfig userConfig);


	/**
	 * 获取配置信息
	 * @param userConfig
	 * @return
	 */
	public BaseResult<Map<String,Object>> getUserConfigInfos(UserConfig userConfig);


	/**
	 * 获取免打扰状态
	 * @param userConfig
	 * @return
	 */
	public BaseResult<UserConfig> getOneUserConfig(UserConfig userConfig);

}
