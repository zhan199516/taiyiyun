package com.taiyiyun.passport.service;

import com.taiyiyun.passport.commons.ResultData;
import com.taiyiyun.passport.language.PackBundle;
import com.taiyiyun.passport.po.redpacket.RedpacketDto;
import com.taiyiyun.passport.po.redpacket.RedpacketHandout;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.List;
import java.util.Map;

public interface IPublicRedPacketService {

	/**
	 * 发普通服务
	 * @param redpacketHandout
	 * @param bundle
	 * @return
	 */
	public DeferredResult<String> handoutRedpacketTask(final PackBundle bundle, final RedpacketHandout redpacketHandout);

	
	/**
	 * 发红包方法
	 * @param bundle
	 * @param redpacketHandout
	 * @return
	 */
	public ResultData<List<Map<String,Object>>> handout(PackBundle bundle, RedpacketHandout redpacketHandout) throws Exception ;

	
	/**
	 * 抢红包
	 * @param redpacketId
	 * @return
	 */
	public DeferredResult<String> grab(final PackBundle packBundle,String redpacketId,String recipientUserId);

	/**
	 * 红包明细列表信息（分页）
	 * @param redpacketId
	 * @param page
	 * @param row
	 * @return
	 */
	public ResultData<RedpacketDto<Map<String,Object>>> detail(PackBundle bundle, String userId, String redpacketId, Integer page, Integer row,Long timestamp) throws Exception;

	/**
	 * 发送红包记录（分页）
	 * @param userId
	 * @param page
	 * @param row
	 * @return
	 */
	public ResultData<RedpacketDto<Map<String,Object>>> handoutDetail(PackBundle bundle,String userId, Integer page, Integer row,Long timestamp) throws Exception;

	/**
	 * 收红包记录（分页）
	 * @param userId
	 * @param page
	 * @param row
	 * @return
	 */
	public ResultData<RedpacketDto<Map<String,Object>>> receiveDetail(PackBundle bundle,String userId, Integer page, Integer row,Long timestamp) throws Exception;

	/**
	 * 红包状态数据
	 * @param redpacketId
	 * @return
	 */
	public DeferredResult<String> getStatus(final PackBundle packBundle,String redpacketId,String userId);

}
