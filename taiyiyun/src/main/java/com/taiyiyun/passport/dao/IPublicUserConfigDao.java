package com.taiyiyun.passport.dao;


import com.taiyiyun.passport.po.PublicUserConfig;

import java.util.List;

public interface IPublicUserConfigDao {

	/**
	 * 保存信息
	 * @param publicUserConfig
	 * @return
	 */
	public int insert(PublicUserConfig publicUserConfig);


	/**
	 * 修改数据
	 * @param publicUserConfig
	 * @return
	 */
	public int update(PublicUserConfig publicUserConfig);

	/**
	 * 根据设置人id修改
	 * @param publicUserConfig
	 * @return
	 */
	public int updateByTargetUserId(PublicUserConfig publicUserConfig);

	/**
	 * 根据被设置人id修改
	 * @param publicUserConfig
	 * @return
	 */
	public int updateBySetupUserId(PublicUserConfig publicUserConfig);

	/**
	 * 根据设置人和被设置人id修改
	 * @param publicUserConfig
	 * @return
	 */
	public int updateByUserId(PublicUserConfig publicUserConfig);


	/**
	 * 根据主键id修改记录
	 * @param publicUserConfig
	 * @return
	 */
	public int updateById(PublicUserConfig publicUserConfig);

	/**
	 * 根据id删除数据
	 * @param id
	 * @return
	 */
	public int deleteById(Long id);

	/**
	 *根据设置人id删除
	 * @param setupUserId
	 * @return
	 */
	public int deleteBySetupUserId(String setupUserId);

	/**
	 * 根据目标设置人删除
	 * @param targetUserId
	 * @return
	 */
	public int deleteByTargetUserId(String targetUserId);


	/**
	 * 根据设置人和被设置人删除数据
	 * @param setupUserId
	 * @param targetUserId
	 * @return
	 */
	public int deleteByUserId(String setupUserId,String targetUserId);

	/**
	 * 分页查询数据
	 * @param publicUserConfig
	 * @return
	 */
	public List<PublicUserConfig> listPage(PublicUserConfig publicUserConfig);

	/**
	 * 列表数据
	 * @param publicUserConfig
	 * @return
	 */
	public List<PublicUserConfig> list(PublicUserConfig publicUserConfig);


	/**
	 * 根据主键Id，查询列表数据
	 * @param id
	 * @return
	 */
	public PublicUserConfig getOneById(String id);

	/**
	 * 根据设置人和被设置人，查询设置人信息
	 * @param setupUserId
	 * @param targetUserId
	 * @return
	 */
	public PublicUserConfig getOneById(String setupUserId, String targetUserId);

	/**
	 * 根据用户主键Id，查询列表数据
	 * @param publicUserConfig
	 * @return
	 */
	public  List<PublicUserConfig> listByTargetId(PublicUserConfig publicUserConfig);

	/**
	 * 根据设置用户id查询列表
	 * @param userId
	 * @return
	 */
	public  List<PublicUserConfig> listBySetupUserId(String userId);

	/**
	 * 动态条件获取单一用户配置数据（注意设置条件的唯一性）
	 * @param publicUserConfig
	 * @return
	 */
	public List<PublicUserConfig> getOneByCriterias(PublicUserConfig publicUserConfig);

}
