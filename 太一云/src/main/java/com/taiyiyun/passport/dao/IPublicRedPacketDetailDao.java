package com.taiyiyun.passport.dao;


import com.taiyiyun.passport.po.PublicRedPacketDetail;

import java.util.List;

public interface IPublicRedPacketDetailDao {


	/**
	 * 保存红包信息
	 * @param publicRedPacketDetail
	 * @return
	 */
	public int insert(PublicRedPacketDetail publicRedPacketDetail);

	/**
	 * 批量插入
	 * @param details
	 * @return
	 */
	public int insertBatch(List<PublicRedPacketDetail> details);

	/**
	 * 修改数据
	 * @param publicRedPacketDetail
	 * @return
	 */
	public int update(PublicRedPacketDetail publicRedPacketDetail);

	/**
	 * 根据主键id修改记录
	 * @param publicRedPacketDetail
	 * @return
	 */
	public int updateById(PublicRedPacketDetail publicRedPacketDetail);
	
	/**
	 * 修改数据
	 * @param publicRedPacketDetail
	 * @return
	 */
	public int updateRedPacketDetail(PublicRedPacketDetail publicRedPacketDetail);

	/**
	 * 根据id删除数据
	 * @param redPacketId
	 * @return
	 */
	public int deleteById(String redPacketId);

	/**
	 * 分页查询数据
	 * @param publicRedPacketDetail
	 * @return
	 */
	public List<PublicRedPacketDetail> listPage(PublicRedPacketDetail publicRedPacketDetail);

	/**
	 * 带用户信息(toUserId)和用户群组信息的分页查询
	 * @param publicRedPacketDetail
	 * @return
	 */
	public List<PublicRedPacketDetail> listRedpacketToUserPage(PublicRedPacketDetail publicRedPacketDetail);


	/**
	 * 带用户信息(fromUserId)和用户群组信息的分页查询
	 * @param publicRedPacketDetail
	 * @return
	 */
	public List<PublicRedPacketDetail> listRedpacketFromUserPage(PublicRedPacketDetail publicRedPacketDetail);
	/**
	 * 列表数据
	 * @param publicRedPacketDetail
	 * @return
	 */
	public List<PublicRedPacketDetail> list(PublicRedPacketDetail publicRedPacketDetail);

	/**
	 * 根据主键Id，查询列表数据
	 * @param packId
	 * @return
	 */
	public PublicRedPacketDetail getOneById(String packId);

	/**
	 * 根据红包和用户id，查询抢红包信息
	 * @param publicRedPacketDetail
	 * @return
	 */
	public PublicRedPacketDetail getOneByPackAndUserId(PublicRedPacketDetail publicRedPacketDetail);

	/**
	 * 获取该红包最后一个被抢的红包信息
	 * @param packId
	 * @return
	 */
	public PublicRedPacketDetail getLastOneGrabed(String packId);

	/**
	 * 根据红包id，查询已抢红包金额和数量
	 * @param packId
	 * @return
	 */
	public PublicRedPacketDetail getSumAndCountByPackId(String packId);

	/**
	 * 获取该红包下，未被抢且解冻成功的红包总金额
	 * @param packId
	 * @return
	 */
	public PublicRedPacketDetail getSumByPackIdAndStatus(String packId);

	/**
	 * 获取该红包下，已抢且解冻成功的红包总金额
	 * @param publicRedPacketDetail
	 * @return
	 */
	public PublicRedPacketDetail getGrabSumByPackIdAndStatus(PublicRedPacketDetail publicRedPacketDetail);

	/**
	 * 分组获取红包id
	 * @param publicRedPacketDetail
	 * @return
	 */
	public List<String> listGrabByStatus(PublicRedPacketDetail publicRedPacketDetail);

	/**
	 * 查询被抢到红包，但转账失败的数据
	 * @return
	 */
	public List<PublicRedPacketDetail> listGrabByTransferStatus();

	/**
	 * 查询绑定用户红包未到账列表
	 * @return
	 */
	public List<PublicRedPacketDetail> listGrabByBindStatus();

	/**
	 * 格局红包id获取，未被抢的红包总额
	 * @param packId
	 * @return
	 */
	public List<PublicRedPacketDetail> listByPackIdAndStatus(String packId);

	/**
	 * 统计领取未绑定资产的红包明细（30天过期）
	 * @param publicRedPacketDetail
	 * @return
	 */
	public List<PublicRedPacketDetail> listGrabByPackIdAndStatus(PublicRedPacketDetail publicRedPacketDetail);



	/**
	 * 根据用户id获取收到红包的总金额和总数量
	 * @param userId
	 * @return
	 */
	public PublicRedPacketDetail getSumAndCountByToUserId(String userId);

	/**
	 * 根据
	 * @param publicRedPacketDetail
	 * @return
	 */
	public  List<PublicRedPacketDetail> listGrabedRedpacket(PublicRedPacketDetail publicRedPacketDetail);

	/**
	 * 根据用户主键Id，查询列表数据
	 * @param redpacketId
	 * @return
	 */
	public  List<PublicRedPacketDetail> listByRedpacketId(String redpacketId);


}
