package com.taiyiyun.passport.dao;


import com.taiyiyun.passport.po.PublicRedPacket;

import java.util.List;

public interface IPublicRedPacketDao {

	/**
	 * 保存红包信息
	 * @param publicRedPacket
	 * @return
	 */
	public int insert(PublicRedPacket publicRedPacket);

	/**
	 * 指定主键id插入
	 * @param publicRedPacket
	 * @return
	 */
	public int insertDefaultId(PublicRedPacket publicRedPacket);

	/**
	 * 修改数据
	 * @param publicRedPacket
	 * @return
	 */
	public int update(PublicRedPacket publicRedPacket);

	/**
	 * 根据主键id修改记录
	 * @param publicRedPacket
	 * @return
	 */
	public int updateById(PublicRedPacket publicRedPacket);

	/**
	 * 根据id删除数据
	 * @param redPacketId
	 * @return
	 */
	public int deleteById(String redPacketId);

	/**
	 * 分页查询数据
	 * @param publicRedPacket
	 * @return
	 */
	public List<PublicRedPacket> listPage(PublicRedPacket publicRedPacket);

	/**
	 * 列表数据
	 * @param publicRedPacket
	 * @return
	 */
	public List<PublicRedPacket> list(PublicRedPacket publicRedPacket);

	/**
	 * 根据token获取红包列表
	 * @param publicRedPacket
	 * @return
	 */
	public List<PublicRedPacket> listByToken(PublicRedPacket publicRedPacket);


	/**
	 * 根据用户查询发红包信息
	 * @param userId
	 * @return
	 */
	public List<PublicRedPacket> listHandoutByUserId(String userId);

	/**
	 * 根据过期时间，查询已过期的红包
	 * @param expireTime
	 * @return
	 */
	public List<PublicRedPacket> listExpireTime(Long expireTime);


	/**
	 * 分页查询发红包信息
	 * @param publicRedPacket
	 * @return
	 */
	public List<PublicRedPacket> listPageHandoutByUserId(PublicRedPacket publicRedPacket);

	/**
	 * 统计总数量
	 * @param publicRedPacket
	 * @return
	 */
	public PublicRedPacket listPageHandoutTotalByUserId(PublicRedPacket publicRedPacket);

	/**
	 * 根据主键Id，查询列表数据
	 * @param packId
	 * @return
	 */
	public PublicRedPacket getOneById(String packId);

	/**
	 * 根据用户主键Id，查询列表数据
	 * @param userId
	 * @return
	 */
	public  List<PublicRedPacket> listByUserId(String userId);
	
	
	/**
	 * 修改状态
	 * @param publicRedPacket
	 * @return
	 */
	public int updateRedPacketStatus(PublicRedPacket publicRedPacket);

}
