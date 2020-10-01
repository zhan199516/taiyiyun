package com.taiyiyun.passport.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.taiyiyun.passport.commons.ResultData;
import com.taiyiyun.passport.consts.Config;
import com.taiyiyun.passport.consts.EnumStatus;
import com.taiyiyun.passport.consts.EnumType;
import com.taiyiyun.passport.dao.*;
import com.taiyiyun.passport.dao.group.IGroupDao;
import com.taiyiyun.passport.dao.group.IGroupMemberDao;
import com.taiyiyun.passport.exception.DefinedError;
import com.taiyiyun.passport.language.PackBundle;
import com.taiyiyun.passport.mqtt.Message;
import com.taiyiyun.passport.mqtt.MessagePublisher;
import com.taiyiyun.passport.po.*;
import com.taiyiyun.passport.po.asset.Trade;
import com.taiyiyun.passport.po.group.Group;
import com.taiyiyun.passport.po.group.GroupMember;
import com.taiyiyun.passport.po.redpacket.*;
import com.taiyiyun.passport.service.IPublicRedPacketService;
import com.taiyiyun.passport.service.IPublicUserService;
import com.taiyiyun.passport.service.IRedisService;
import com.taiyiyun.passport.service.queue.Consumer;
import com.taiyiyun.passport.service.queue.ITask;
import com.taiyiyun.passport.service.transfer.AssetCache;
import com.taiyiyun.passport.service.transfer.ThirdAppManager;
import com.taiyiyun.passport.transfer.Answer.AskFeeResult;
import com.taiyiyun.passport.transfer.Answer.CoinInfo;
import com.taiyiyun.passport.transfer.Answer.FrozenBatchResult;
import com.taiyiyun.passport.transfer.Answer.TransferResult;
import com.taiyiyun.passport.transfer.Ask.AskBody;
import com.taiyiyun.passport.transfer.Ask.AskParam;
import com.taiyiyun.passport.transfer.ITransferAccounts;
import com.taiyiyun.passport.util.DateUtil;
import com.taiyiyun.passport.util.DistributedRedisLock;
import com.taiyiyun.passport.util.Misc;
import com.taiyiyun.passport.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.context.request.async.DeferredResult;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
public class PublicRedPacketServiceImpl implements IPublicRedPacketService {

	public final Logger logger = LoggerFactory.getLogger(getClass());

	static final Consumer consumer = new Consumer();
	static final int min = Runtime.getRuntime().availableProcessors()*20;
	static final int max = Runtime.getRuntime().availableProcessors()*50;
	static ExecutorService executorService=new ThreadPoolExecutor(min, max, 2L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());  

	@Resource
	private IThirdAppUserBindDao thirdAppUserBindDao;

	@Resource
	private IPublicRedPacketDao publicRedPacketDao;

	@Resource
	private IPublicRedPacketDetailDao publicRedPacketDetailDao;

	@Resource
	private IPublicUserDao publicUserDao;

	@Resource
	private IGroupMemberDao groupMemberDao;

	@Resource
	private IGroupDao groupDao;

	@Resource
	private ITradeDao tradeDao;

	@Resource
	private IThirdAppDao thirdAppDao;

	@Resource
	private IPublicUserService userService;

	@Resource
	private IRedisService redisService;

	@Resource(name="transactionManager")
    private DataSourceTransactionManager transactionManager;


	@Override
	public DeferredResult<String> handoutRedpacketTask(final PackBundle packBundle,
												   final RedpacketHandout redpacketHandout){
		final PublicRedPacketServiceImpl me = this;
		final DeferredResult<String> deferred = new DeferredResult<>();
		final BaseResult<Object> rst = new BaseResult<>();
		executorService.submit(new Runnable() {
			@Override
			public void run() {
//				logger.info("******************************************************");
//				long startTimes = System.currentTimeMillis();
//				logger.info("调用发送红包业务-开始时间：" + (startTimes) + "毫秒");
//				logger.info("******************************************************");
				//调用发送红包业务
				ResultData<List<Map<String,Object>>> resultData = null;
				try {
					resultData = me.handout(packBundle, redpacketHandout);
					rst.setStatus(resultData.getStatus());
					rst.setError(resultData.getMessage());
					rst.setData(resultData.getData());
					String rjson = JSON.toJSONString(rst);
					logger.info("PublicRedPacketServiceImpl.handout.error:" + rjson);
					deferred.setResult(rjson);
				} catch (DefinedError ex) {
					ex.printStackTrace();
					rst.setStatus(EnumStatus.NINETY_NINE.getIndex());
					rst.setError(ex.getReadableMsg());
					rst.setInternalError(ex.getMessage());
					String rjson = JSON.toJSONString(rst);
					logger.info("PublicRedPacketServiceImpl.handout.error:" + rjson);
					deferred.setResult(rjson);
				}
				catch (Exception e) {
					e.printStackTrace();
					rst.setStatus(EnumStatus.NINETY_NINE.getIndex());
					String msg = packBundle.getString("system.error");
					rst.setError(msg);
					rst.setInternalError(e.getMessage());
					String rjson = JSON.toJSONString(rst);
					logger.info("PublicRedPacketServiceImpl.handout.error:" + rjson);
					deferred.setResult(rjson);
				}
//				logger.info("******************************************************");
//				long endTimes = System.currentTimeMillis();
//				logger.info("调用发送红包业务-结束时间：" + (endTimes-startTimes) + "毫秒");
//				logger.info("******************************************************");
			}
		});
		return deferred;
	}

	/**
	 * 发红包
	 * @throws Exception
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
	public ResultData<List<Map<String,Object>>> handout(PackBundle bundle, RedpacketHandout redpacketHandout) throws Exception {
		//业务结果通用返回对象
		ResultData<List<Map<String,Object>>> resultData = new ResultData<>();

		//repeatToken不为空执行下面验证逻辑，保证老版本系统调用接口是无异常
		String repeatToken = redpacketHandout.getRepeatToken();
		if (!StringUtil.isEmpty(repeatToken)){
			//查询验证是否有重复发送的红包
			PublicRedPacket publicRedPacket = new PublicRedPacket();
			publicRedPacket.setUserId(redpacketHandout.getUserId());
			publicRedPacket.setRepeatToken(repeatToken);
			List<PublicRedPacket> listRedpacket = publicRedPacketDao.listByToken(publicRedPacket);
			if (listRedpacket != null && listRedpacket.size() > 0){
				String errMsg = "can not repeat send redpacket";
				if(bundle != null) {
					errMsg = bundle.getString("redpacket.not.repeat.sent");
				}
				resultData.setStatus(EnumStatus.ELEVEN.getIndex());
				resultData.setMessage(errMsg);
				return resultData;
			}
		}

		//验证业务输入参数正确性
		//验证红包类型，不能为空
		Short type = redpacketHandout.getType();
		if(type == null){
			String errMsg = "redpacket type not empty";
			if(bundle != null) {
				errMsg = bundle.getString("need.param", "redpacketType");
			}
			resultData.setStatus(EnumStatus.ONE.getIndex());
			resultData.setMessage(errMsg);
			return resultData;
		}
		//验证金额是否满足业务要求
		BigDecimal amount = new BigDecimal(0);
		//手气包判断，总金额不能为空（个人红包和手气包红包判断逻辑一样）
		if (type == EnumType.T1.getIndex()){
			BigDecimal totalAmount = redpacketHandout.getTotalAmount();
			if (totalAmount == null){
				String errMsg = "totalAmount not empty";
				if(bundle != null) {
					errMsg = bundle.getString("need.param", "totalAmount");
				}
				resultData.setStatus(EnumStatus.ONE.getIndex());
				resultData.setMessage(errMsg);
				return resultData;
			}
			else{
				amount = totalAmount;
			}
		}
		//平均红包判断，每个红包金额不能为空
		if (type == EnumType.T2.getIndex()
				|| type == EnumType.T3.getIndex()){
			BigDecimal eachAmount = redpacketHandout.getEachAmount();
			if (eachAmount == null) {
				String errMsg = "eachAmount not empty";
				if (bundle != null) {
					errMsg = bundle.getString("need.param", "eachAmount");
				}
				resultData.setStatus(EnumStatus.ONE.getIndex());
				resultData.setMessage(errMsg);
				return resultData;
			}
			else{
				amount = eachAmount;
			}
		}
		//通用同意判断公用字段不能为空
		if(redpacketHandout.getPlatformId() == null ||
				redpacketHandout.getCoinId() == null ||
				redpacketHandout.getUserId() == null ||
				redpacketHandout.getRedpacketCount() == null ||
				redpacketHandout.getSessionType() == null ||
				redpacketHandout.getSessionId() == null){
			String errMsg = "platformId, coinId, userId, redpacketCount,sessionType,sessionId not empty";
			if(bundle != null) {
				errMsg = bundle.getString("need.param", "platformId, coinId, userId, redpacketCount,sessionType,sessionId");
			}
			resultData.setStatus(EnumStatus.ONE.getIndex());
			resultData.setMessage(errMsg);
			return resultData;
		}
		logger.info("handout redpacket param:" + JSON.toJSONString(redpacketHandout));
		//验证平台信息是否存在
		String platformId = redpacketHandout.getPlatformId();
		ITransferAccounts accounts = ThirdAppManager.getInstance().getAccountById(platformId);
		if(accounts == null){
			String errMsg = "can not query thirdpart app info";
			if(bundle != null) {
				errMsg = bundle.getString("transfer.postmoney.nothirdinfo");
			}
			resultData.setStatus(EnumStatus.TWO.getIndex());
			resultData.setMessage(errMsg);
			return resultData;
		}
		//根据平台id，查询平台名称
		ThirdAppExt thirdAppExt = thirdAppDao.getOneByAppId(platformId);
		//默认与平台id一致
		String platformName = platformId;
		if (thirdAppExt != null){
			String appName = thirdAppExt.getAppName();
			if (!StringUtil.isEmpty(appName)){
				platformName = appName;
			}
		}
		redpacketHandout.setPlatformName(platformName);
		//根据sessionType 验证目标用户或群组信息
		Short sessionType = redpacketHandout.getSessionType();
		String sessionId = redpacketHandout.getSessionId();
		if (sessionType == EnumType.T1.getIndex()){
			//验证目标用户是否存在
			PublicUser publicUser = userService.getByUserId(sessionId);
			if (publicUser == null){
				String errMsg = "target user info not find";
				if(bundle != null) {
					errMsg = bundle.getString("redpacket.user.not.find");
				}
				resultData.setStatus(EnumStatus.TEN.getIndex());
				resultData.setMessage(errMsg);
				return resultData;
			}
		}
		else{
			//验证群组是否存在
			Group group = groupDao.selectByPrimarykey(sessionId);
			if (group == null){
				String errMsg = "group info not find";
				if(bundle != null) {
					errMsg = bundle.getString("redpacket.group.not.find");
				}
				resultData.setStatus(EnumStatus.TEN.getIndex());
				resultData.setMessage(errMsg);
				return resultData;
			}
		}
		//验证用户信息是否存在，并获取用户uuid
		String userId = redpacketHandout.getUserId();
		PublicUser publicUser = userService.getByUserId(userId);
		if (publicUser == null){
			String errMsg = "user info not find";
			if(bundle != null) {
				errMsg = bundle.getString("user.not.find");
			}
			resultData.setStatus(EnumStatus.THREE.getIndex());
			resultData.setMessage(errMsg);
			return resultData;
		}
		//获取平台币资产信息
		String coinId = redpacketHandout.getCoinId();
		CoinInfo coin = AssetCache.getInstance().getCoin(platformId, coinId);
		if (coin == null){
			String errMsg = "get coin info fail";
			if(bundle != null) {
				errMsg = bundle.getString("redpacket.coin.not.find");
			}
			resultData.setStatus(EnumStatus.FOUR.getIndex());
			resultData.setMessage(errMsg);
			return resultData;
		}
		//设置与rmb兑换费率：1个币=多少RMB
		BigDecimal coinprice = coin.getCoinprice();
		String coinName = coin.getName();
		//币最小单位
		BigDecimal quota = coin.getQuota();

		//验证红包金额是否满足要求
		if(amount.compareTo(quota) < 0){
			//throw new DefinedError.ParameterException("交易币量不能小于0.000001", null);
			String errMsg = "redpacket amount not less than " + quota;
			if(bundle != null) {
				errMsg = bundle.getString("redpacket.amount.error",String.valueOf(quota));
			}
			resultData.setStatus(EnumStatus.FIVE.getIndex());
			resultData.setMessage(errMsg);
			return resultData;
		}
		//发红包总数量不能小于最小币单位*红包个数
		if (type == EnumType.T1.getIndex()) {
			Integer redpacketCount = redpacketHandout.getRedpacketCount();
			BigDecimal minAmount = new BigDecimal(redpacketCount).multiply(quota);
			if (amount.compareTo(minAmount) < 0) {
				String errMsg = "redpacket amount not less than min amounnt";
				if(bundle != null) {
					errMsg = bundle.getString("redpacket.random.amount.error",String.valueOf(quota));
				}
				resultData.setStatus(EnumStatus.FIVE.getIndex());
				resultData.setMessage(errMsg);
				return resultData;
			}
		}
		//验证用户是否绑定资产账户
		String uuid = publicUser.getUuid();

		ThirdAppUserBindExt ext = thirdAppUserBindDao.getOneExtByCondition(uuid, platformId, null);
		if(ext == null || ext.getBindStatus() == null || ext.getBindStatus() == 0){
			String errMsg = "user unbind asset account";
			if(bundle != null) {
				errMsg = bundle.getString("transfer.postmoney.nobindinfo");
			}
			resultData.setStatus(EnumStatus.SIX.getIndex());
			resultData.setMessage(errMsg);
			return resultData;
		}
		//根据平台id获取用户绑定关系id，并调用接口获取第三方接口访问token
		String relateId = thirdAppUserBindDao.getRelateId(platformId);
		//设置币信息
		redpacketHandout.setQuota(quota);
		redpacketHandout.setCoinprice(coinprice);
		//验证资产余额
//		logger.info("******************************************************");
//		long startTimes = System.currentTimeMillis();
//		logger.info("验证资产余额-开始时间：" + (startTimes) + "毫秒");
//		logger.info("******************************************************");
//		AskBody bodyAsset = accounts.askForToken(bundle, ext, relateId);
//		if(bodyAsset.getToken() == null){
//			String errMsg = "get token fail";
//			if(bundle != null) {
//				errMsg = bundle.getString("yuanbao.token.failed");
//			}
//			resultData.setStatus(EnumStatus.NINETY_SEVEN.getIndex());
//			resultData.setMessage(errMsg);
//			return resultData;
//		}
//		AssetResult assetResult = accounts.askForAsset(bundle,bodyAsset);
//		if(!assetResult.isSuccess()){
//			String errMsg = "get asset fail";
//			if(bundle != null) {
//				errMsg = bundle.getString("redpacket.asset.error");
//			}
//			resultData.setStatus(EnumStatus.SEVEN.getIndex());
//			resultData.setMessage(errMsg);
//			return resultData;
//		}
//		logger.info("******************************************************");
//		long endTimes = System.currentTimeMillis();
//		logger.info("验证资产余额-结束时间：" + (endTimes-startTimes) + "毫秒");
//		logger.info("******************************************************");
//		//验证币资产余额是否满足要求
//		List<CoinOwn> coinOwns = assetResult.getCoinList();
//		for (CoinOwn coinOwn:coinOwns){
//			String coinNameTemp = coinOwn.getCoinName();
//			if (!StringUtil.isEmpty(coinNameTemp) && coinNameTemp.equals(coinId)){
//				BigDecimal availableBalance = coinOwn.getAvailableBalance();
//				if(amount.compareTo(availableBalance) > 0){
//					String errMsg = "asset amount not enough";
//					if(bundle != null) {
//						errMsg = bundle.getString("redpacket.amount.not.enough");
//					}
//					resultData.setStatus(EnumStatus.EIGHT.getIndex());
//					resultData.setMessage(errMsg);
//					return resultData;
//				}
//				break;
//			}
//		}
		BigDecimal handCharge = redpacketHandout.getHandCharge();
		if (handCharge == null) {
//			logger.info("******************************************************");
//			long startTimes2 = System.currentTimeMillis();
//			logger.info("获取手续费-开始时间：" + (startTimes2) + "毫秒");
//			logger.info("******************************************************");
			//获取手续费
			AskBody body = accounts.askForToken(bundle, ext, relateId);
			if (body.getToken() == null) {
				String errMsg = "get token fail";
				if (bundle != null) {
					errMsg = bundle.getString("yuanbao.token.failed");
				}
				resultData.setStatus(EnumStatus.NINETY_SEVEN.getIndex());
				resultData.setMessage(errMsg);
				return resultData;
			}
			AskFeeResult askFeeResult = accounts.askForFee(bundle, body, coinId, amount);
			if (!askFeeResult.isSuccess()) {
				String errMsg = "get fee fail";
				if (bundle != null) {
					errMsg = bundle.getString("redpacket.fee.error");
				}
				resultData.setStatus(EnumStatus.NINE.getIndex());
				resultData.setMessage(errMsg);
				return resultData;
			}
//			logger.info("******************************************************");
//			long endTimes2 = System.currentTimeMillis();
//			logger.info("获取手续费-结束时间：" + (endTimes2 - startTimes2) + "毫秒");
//			logger.info("******************************************************");
			//手续费信息（总手续费）
			handCharge = askFeeResult.getTotalFee();
			//手续费率
			BigDecimal rate = askFeeResult.getFee();
			//设置手续费费率
			redpacketHandout.setChargeRate(rate);
		}
		else{
			//设置手续费费率
			redpacketHandout.setChargeRate(BigDecimal.ZERO);
		}
		logger.info("手续费金额：" + handCharge);
		//设置红包对象
		PublicRedPacket publicRedPacket = new PublicRedPacket();
		publicRedPacket.setUserId(userId);
		publicRedPacket.setPlatformId(redpacketHandout.getPlatformId());
		publicRedPacket.setPlatformName(redpacketHandout.getPlatformName());
		publicRedPacket.setCoinId(redpacketHandout.getCoinId());
		publicRedPacket.setCoinName(coinName);
		publicRedPacket.setSessionType(redpacketHandout.getSessionType());
		publicRedPacket.setSessionId(redpacketHandout.getSessionId());
		publicRedPacket.setPackType(redpacketHandout.getType());
		publicRedPacket.setPackCount(redpacketHandout.getRedpacketCount());
		publicRedPacket.setRemark(redpacketHandout.getText());
		publicRedPacket.setExchangeRate(coinprice);
		publicRedPacket.setFee(handCharge);
		publicRedPacket.setPackStatus(EnumStatus.ZORO.getIndex());
		publicRedPacket.setRepeatToken(repeatToken);
		//红包数量
        Integer redpacketCount = redpacketHandout.getRedpacketCount();
		//计算币的rmb现金价值=发红包金额*兑换费率
		BigDecimal cashAmount = new BigDecimal("0");
		if (type == EnumType.T1.getIndex()
				|| type == EnumType.T3.getIndex()){
			//个人和手气包，现金价值=总额*费率
			cashAmount = amount.multiply(coinprice);
			//设置币量总额
			publicRedPacket.setAmount(amount);
		}
		else if (type == EnumType.T2.getIndex()){
			//平均包，现金价值=总额*数量*费率
			cashAmount = amount.multiply(new BigDecimal(redpacketCount)).multiply(coinprice);
			//设置币量总额
			publicRedPacket.setAmount(amount.multiply(new BigDecimal(redpacketCount)));
		}
		publicRedPacket.setCashAmount(cashAmount.setScale(2,BigDecimal.ROUND_DOWN));
		//设置过期时间
		long currentTimes = System.currentTimeMillis();
		long expireTime = currentTimes + 24*60*60*1000;
		publicRedPacket.setExpireTime(expireTime);
		//生成红包明细
		List<PublicRedPacketDetail> details = generateRedpacket(redpacketHandout);
		List<AskParam> askParams = redpacketHandout.getAskParams();
//		logger.info("******************************************************");
//		long startTimes3 = System.currentTimeMillis();
//		logger.info("冻结资产-开始时间：" + (startTimes3) + "毫秒");
//		logger.info("******************************************************");
		//获取token
		AskBody bodyFroze = accounts.askForToken(bundle, ext, relateId);
		if(bodyFroze.getToken() == null){
			String errMsg = "get token fail";
			if(bundle != null) {
				errMsg = bundle.getString("yuanbao.token.failed");
			}
			resultData.setStatus(EnumStatus.ONE.getIndex());
			resultData.setMessage(errMsg);
			return resultData;
		}
//		logger.info("redpacket handout param:" + JSON.toJSONString(redpacketHandout));
		BigDecimal handoutTotalAmount =  publicRedPacket.getAmount();
		//发起冻结资产申请过
		FrozenBatchResult frozenResult = accounts.askForBatchFroze(bundle, bodyFroze, redpacketHandout.getCoinId(),handoutTotalAmount,askParams);
//		logger.info("******************************************************");
//		long endTimes3 = System.currentTimeMillis();
//		logger.info("冻结资产-结束时间：" + (endTimes3-startTimes3) + "毫秒");
//		logger.info("******************************************************");
		if(frozenResult != null && frozenResult.isSuccess()){
			//记录转账信息
			Trade transferAnswer = new Trade();
			transferAnswer.setAmount(publicRedPacket.getAmount());
			transferAnswer.setFromUserId(publicRedPacket.getUserId());
			transferAnswer.setFromUuid(uuid);
			transferAnswer.setStatus(EnumStatus.FOUR.getIndex());
			transferAnswer.setFrozenId(0L);
			transferAnswer.setCoinId(coinId);
			transferAnswer.setCoinName(coinName);
			transferAnswer.setFee(publicRedPacket.getFee());
			transferAnswer.setPlatformId(publicRedPacket.getPlatformId());
			transferAnswer.setText(publicRedPacket.getRemark());
			transferAnswer.setToUserId("");
			transferAnswer.setToUuid("");
			transferAnswer.setWorthRmbApply(coinprice);
			//红包总冻结金额不能过期，设置过期时间为100年
			//因为红包被部分领取后，退回金额和红包总金额不一致，不能直接退回全部
			//红包退回金额有固定的红包相关的定时程序独立处理，参看RedpacketClear类
			transferAnswer.setExpireTime(currentTimes + 100*365*24*60*60*1000);
			transferAnswer.setCreateTime(currentTimes);
			transferAnswer.setAcceptTime(currentTimes);
			tradeDao.insert(transferAnswer);
			//冻结资产成功，红包与红包明细信息入库
			publicRedPacket.setFromUuid(uuid);
			publicRedPacket.setTradeId(transferAnswer.getTradeId());
			//获取红包主键id
			String packId = StringUtil.getUUID();
//			logger.info("******************************************************");
//			long startTimes4 = System.currentTimeMillis();
//			logger.info("循环处理红包明细-开始时间：" + (startTimes4) + "毫秒");
//			logger.info("******************************************************");
			List<String> redisMaps = new ArrayList<>();
			//暂无批量入库，只由单条逐一入库
			for (PublicRedPacketDetail detail:details){
				String detailId = StringUtil.getUUID();
				String mapkey = detail.getMapkey();
				BigDecimal transferAmount = detail.getAmount();
				List<Map<String,Object>> result = frozenResult.getResult();
				//冻结id
				Long frozenId = null;
				//手续费
				BigDecimal charge = BigDecimal.ZERO;
				//匹配批量冻结返回接口，获取冻结id
				if (result != null && result.size() > 0){
					for (Map<String,Object> map:result){
						Object mapkeyObj = map.get("mapkey");
						if (mapkeyObj != null && !StringUtil.isEmpty(mapkey)
								&& mapkeyObj.equals(mapkey)){
							Object frozenIdObj = map.get("frozenId") == null?"0":map.get("frozenId");
							Object chargeObj = map.get("charge") == null?"0":map.get("charge");
							if (frozenIdObj != null){
								try {
									frozenId = Long.parseLong(frozenIdObj.toString());
									charge = new BigDecimal(chargeObj.toString());
									break;
								}
								catch(Exception e){
									logger.info("get frozenId error:" + e.getMessage());
								}
							}
						}
					}
				}
				Long tradeId = transferAnswer.getTradeId();
				detail.setPackId(packId);
				detail.setFreezeTradeId(frozenId);
				detail.setFee(charge);
				detail.setFromUserId(userId);
				detail.setIsBest(detail.getIsBest());
				detail.setTradeId(tradeId);
				detail.setDetailId(detailId);
				//设置红包明细时间戳，抢红包成功后，修改次时间戳
				detail.setRecordTimestamp(System.nanoTime());
				//插入数据库
				Map<String, String> redpacketMap = new HashMap<>();
				redpacketMap.put("detailId", detailId);
				redpacketMap.put("tradeId", String.valueOf(tradeId));
				redpacketMap.put("userId", userId);
				redpacketMap.put("packId", packId);
				redpacketMap.put("amount", String.valueOf(transferAmount));
				redpacketMap.put("freezeTradeId", String.valueOf(frozenId));
				redisMaps.add(JSON.toJSONString(redpacketMap));
			}
//			logger.info("******************************************************");
//			long endTimes4 = System.currentTimeMillis();
//			logger.info("循环处理红包明细-结束时间：" + (endTimes4-startTimes4) + "毫秒");
//			logger.info("******************************************************");
//			logger.info("******************************************************");
//			long startTimes6 = System.currentTimeMillis();
//			logger.info("批量插入开始时间：" + (startTimes6) + "毫秒");
//			logger.info("******************************************************");
			//设置创建时间
			publicRedPacket.setCreateTime(new Date());
			//设置主键Id
			publicRedPacket.setPackId(packId);
			//设置时间戳
			publicRedPacket.setRecordTimestamp(System.nanoTime());
			int rval = publicRedPacketDao.insertDefaultId(publicRedPacket);
			if (rval > 0) {
				//批量插入
				long count = publicRedPacketDetailDao.insertBatch(details);
				if (count > 0) {
					redisService.pushBatch(packId, redisMaps);
				}
			}
//			logger.info("******************************************************");
//			long endTimes6 = System.currentTimeMillis();
//			logger.info("批量插入-结束时间：" + (endTimes6-startTimes6) + "毫秒");
//			logger.info("******************************************************");
//
//			long startTimes5 = System.currentTimeMillis();
//			logger.info("发送MQTT消息-开始时间：" + (startTimes5) + "毫秒");
//			logger.info("******************************************************");
			String messageId = null;
			//发送个人红包（目标用户）
			if (sessionType == EnumType.T1.getIndex()){
				PublicUser targetPublicUser = userService.getByUserId(sessionId);
				//将user对象转成GroupMember 对象
				List<GroupMember> gmembers = new ArrayList<>(1);
				GroupMember groupMember = new GroupMember();
				groupMember.setAvatarUrl(Misc.getServerUri(null, targetPublicUser.getAvatarUrl()));
				groupMember.setNikeName(targetPublicUser.getUserName());
				groupMember.setUserId(targetPublicUser.getId());
				gmembers.add(groupMember);
				messageId = sendRedpacketMessage(gmembers,publicRedPacket);
			}
			//群组红包（目标群组下的所有用户）
			else if (sessionType == EnumType.T2.getIndex()){
//				List<GroupMember> gmembers = groupMemberDao.selectGroupMemsByGroupId(sessionId);
				messageId = sendRedpacketMessage(null,publicRedPacket);
			}
//			logger.info("******************************************************");
//			long endTimes5 = System.currentTimeMillis();
//			logger.info("发送MQTT消息-结束时间：" + (endTimes5-startTimes5) + "毫秒");
//			logger.info("******************************************************");
//			logger.info("send mqtt message id:" + messageId);
			//设置返回结果map对相关，如果增加返回值信息，可直接增加
			Map<String,Object> resultMap = new HashMap<>();
			resultMap.put("version",1);
			resultMap.put("messageType",2);
			resultMap.put("fromUserId",userId);
			resultMap.put("fromClientId",Config.get("mqtt.clientId"));
			resultMap.put("messageId",messageId);
			resultMap.put("sessionType",sessionType);
			resultMap.put("sessionId",sessionId);
			resultMap.put("publishTime",currentTimes);
			resultMap.put("updateTime",currentTimes);
			resultMap.put("contentType",214);
			Map<String,Object> contentMap = new HashMap<>();
			contentMap.put("fromUserId",userId);
			contentMap.put("redPacketId",packId);
			contentMap.put("redpacketCount",redpacketCount);
			contentMap.put("redpacketType",type);
			contentMap.put("amount",amount);
			contentMap.put("platformId",platformId);
			contentMap.put("platformName",platformName);
			contentMap.put("coinId",coinId);
			contentMap.put("coinName",coinName);
			contentMap.put("text",redpacketHandout.getText());
			contentMap.put("createTime",publicRedPacket.getCreateTime().getTime());
			contentMap.put("expireTime",publicRedPacket.getExpireTime());
			contentMap.put("fee",handCharge);
			contentMap.put("status",EnumStatus.ZORO.getIndex());
			contentMap.put("name",publicUser.getUserName());
			contentMap.put("avatarUrl",Misc.getServerUri(null, publicUser.getAvatarUrl()));

			resultMap.put("content",contentMap);
			resultData.setStatus(EnumStatus.ZORO.getIndex());
			resultData.setMessage(EnumStatus.ZORO.getName());
			ArrayList<Map<String,Object>> list = new ArrayList<>();
			list.add(resultMap);
			resultData.setData(list);
			return resultData;
		}
		else {
            Integer erroeCode = frozenResult.getErrorCode();
            String errMsg = "freeze asset fail";
            //账户资金不足
            if (erroeCode != null && erroeCode.intValue() == 10104){
                if (bundle != null) {
                    errMsg = bundle.getString("redpacket.amount.not.enough");
                }
            }
            else {
                if (bundle != null) {
                    errMsg = bundle.getString("redpacket.freeze.asset.fail",erroeCode);
                }
            }
			throw new DefinedError.ThirdNotFoundException(errMsg, frozenResult.getErrorCode().toString());
		}
	}

	/**
	 * 生成红包明细
	 */
	private List<PublicRedPacketDetail> generateRedpacket(RedpacketHandout redpacketHandout){
		List<PublicRedPacketDetail> details = new ArrayList<>();
		List<AskParam> askParams = new ArrayList<>();
		//红包类型
		Short type = redpacketHandout.getType();
		//红包数量
		Integer redpacketCount = redpacketHandout.getRedpacketCount();
		BigDecimal coinprice = redpacketHandout.getCoinprice();
		BigDecimal quota = redpacketHandout.getQuota();
		//手续费率
		BigDecimal chargeRate = redpacketHandout.getChargeRate();
		if (type == EnumType.T1.getIndex()){
			//红包总金额
			BigDecimal totalAmount = redpacketHandout.getTotalAmount();
			//手气包
			List<BigDecimal> list = generateRandom(redpacketCount,totalAmount,6,quota);
			int maxIndex = 0;
			//随机包值都相等时，第一个为手气最佳
			Set set = new HashSet(list);
			if (set.size() == 1){
				maxIndex = 0;
			}
			else{
				maxIndex = list.indexOf(Collections.max(list));
			}
			for(int i=0;i<list.size();i++){
				BigDecimal amount = list.get(i);
				PublicRedPacketDetail publicRedPacketDetail = new PublicRedPacketDetail();
				publicRedPacketDetail.setCoinId(redpacketHandout.getCoinId());
				publicRedPacketDetail.setSessionType(redpacketHandout.getSessionType());
				publicRedPacketDetail.setSessionId(redpacketHandout.getSessionId());
				publicRedPacketDetail.setAmount(amount);
				publicRedPacketDetail.setExchangeRate(coinprice);
				if (maxIndex == i){
					publicRedPacketDetail.setIsBest((short) EnumStatus.ONE.getIndex());
				}
				else{
					publicRedPacketDetail.setIsBest((short) EnumStatus.ZORO.getIndex());
				}
				//计算单个红包现金价值
				BigDecimal detailCashAmount = amount.multiply(coinprice);
				publicRedPacketDetail.setCashAmount(detailCashAmount.setScale(2,BigDecimal.ROUND_DOWN));
				//计算手续费，精确到小数点后8位，四舍五入
				BigDecimal chargeFee = amount.multiply(chargeRate);
				publicRedPacketDetail.setFee(chargeFee);
				publicRedPacketDetail.setTransferStatus((short) EnumStatus.ZORO.getIndex());
				publicRedPacketDetail.setCreateTime(new Date());
				publicRedPacketDetail.setUnfreezeStatus((short) EnumStatus.ZORO.getIndex());
				//冻结资产接口参数对象
				AskParam askParam = new AskParam();
				askParam.setAmount(amount);
				askParam.setMapkey("froze_" + i);
//				askParam.setCharge(chargeFee);
				askParams.add(askParam);
				publicRedPacketDetail.setMapkey("froze_" + i);
				details.add(publicRedPacketDetail);
			}
		}
		else if (type == EnumType.T2.getIndex()){
			//平均包，按照红包数量生成红包明细
			BigDecimal eachAmount = redpacketHandout.getEachAmount();
			//BigDecimal totalAmount = eachAmount.multiply(new BigDecimal(redpacketCount));
			for(int i=0; i<redpacketCount; i++){
				PublicRedPacketDetail publicRedPacketDetail = new PublicRedPacketDetail();
				publicRedPacketDetail.setCoinId(redpacketHandout.getCoinId());
				publicRedPacketDetail.setSessionType(redpacketHandout.getSessionType());
				publicRedPacketDetail.setSessionId(redpacketHandout.getSessionId());
				publicRedPacketDetail.setAmount(eachAmount);
				publicRedPacketDetail.setExchangeRate(coinprice);
				//计算单个红包现金价值
				BigDecimal detailCashAmount = eachAmount.multiply(coinprice);
				publicRedPacketDetail.setCashAmount(detailCashAmount.setScale(2,BigDecimal.ROUND_DOWN));
				//计算手续费，精确到小数点后8位，四舍五入
				BigDecimal chargeFee = eachAmount.multiply(chargeRate).setScale(8, RoundingMode.HALF_UP);
				publicRedPacketDetail.setFee(chargeFee);
				publicRedPacketDetail.setTransferStatus((short) EnumStatus.ZORO.getIndex());
				publicRedPacketDetail.setCreateTime(new Date());
				publicRedPacketDetail.setIsBest((short) EnumStatus.ZORO.getIndex());
				publicRedPacketDetail.setUnfreezeStatus((short) EnumStatus.ZORO.getIndex());
				//冻结资产接口参数对象
				AskParam askParam = new AskParam();
				askParam.setAmount(eachAmount);
				askParam.setMapkey("froze_" + i);
//				askParam.setCharge(chargeFee);
				askParams.add(askParam);
				publicRedPacketDetail.setMapkey("froze_" + i);
				details.add(publicRedPacketDetail);
			}
		}
		else if (type == EnumType.T3.getIndex()){
			BigDecimal totalAmount = redpacketHandout.getEachAmount();
			//个人红包
			PublicRedPacketDetail publicRedPacketDetail = new PublicRedPacketDetail();
			publicRedPacketDetail.setCoinId(redpacketHandout.getCoinId());
			publicRedPacketDetail.setSessionType(redpacketHandout.getSessionType());
			publicRedPacketDetail.setSessionId(redpacketHandout.getSessionId());
			publicRedPacketDetail.setAmount(totalAmount);
			publicRedPacketDetail.setExchangeRate(coinprice);
			//计算单个红包现金价值
			BigDecimal detailCashAmount = totalAmount.multiply(coinprice);
			publicRedPacketDetail.setCashAmount(detailCashAmount.setScale(2,BigDecimal.ROUND_DOWN));
			//计算手续费，精确到小数点后8位，四舍五入
			BigDecimal chargeFee = totalAmount.multiply(chargeRate).setScale(8, BigDecimal.ROUND_HALF_UP);
			publicRedPacketDetail.setFee(chargeFee);
			publicRedPacketDetail.setTransferStatus((short) EnumStatus.ZORO.getIndex());
			publicRedPacketDetail.setCreateTime(new Date());
			publicRedPacketDetail.setIsBest((short) EnumStatus.ZORO.getIndex());
			publicRedPacketDetail.setUnfreezeStatus((short) EnumStatus.ZORO.getIndex());
			//冻结资产接口参数对象
			AskParam askParam = new AskParam();
			askParam.setAmount(totalAmount);
			askParam.setMapkey("froze_0");
//			askParam.setCharge(chargeFee);
			askParams.add(askParam);
			publicRedPacketDetail.setMapkey("froze_0");
			details.add(publicRedPacketDetail);
		}
		//设置数据到参数对象中，在函数外可以获取
		redpacketHandout.setAskParams(askParams);
		return details;
	}

	/**
	 * 发红包后-通知目标用户消息
	 */
	private String sendRedpacketMessage(List<GroupMember> gmembers ,PublicRedPacket publicRedPacket){
		long currentTimes = System.currentTimeMillis();
		String messageId = "Redpacket-uplink-" + publicRedPacket.getPackId()+ "-" + currentTimes;
		//后修改逻辑，如果gmembers为空，要发上行消息
		if (gmembers == null){
			Message<Object> message = new Message<>();
			message.setVersion(1);
			message.setMessageType(Message.MessageType.MESSAGE_IM_GENERIC.getValue());
			message.setFromUserId(publicRedPacket.getUserId());
			message.setFromClientId(Config.get("mqtt.clientId"));
			message.setMessageId(messageId);
			//message.setSessionType((int) publicRedPacket.getSessionType());
			message.setSessionType(Message.SessionType.SESSION_GROUP.getValue());
			message.setSessionId(publicRedPacket.getSessionId());
			message.setPublishTime(currentTimes);
			message.setUpdateTime(currentTimes);
			message.setContentType(Message.ContentType.CONTENT_IM_GENERIC_RD_REDPAY.getValue());
			//设置消息content
			RedpacketMessage redpacketMessage = new RedpacketMessage();
			redpacketMessage.setFromUserId(publicRedPacket.getUserId());
			redpacketMessage.setRedPacketId(publicRedPacket.getPackId());
			redpacketMessage.setRedpacketCount(publicRedPacket.getPackCount());
			redpacketMessage.setRedpacketType(publicRedPacket.getPackType());
			redpacketMessage.setAmount(publicRedPacket.getAmount());
			redpacketMessage.setPlatformId(publicRedPacket.getPlatformId());
			redpacketMessage.setPlatformName(publicRedPacket.getPlatformName());
			redpacketMessage.setCoinId(publicRedPacket.getCoinId());
			redpacketMessage.setCoinName(publicRedPacket.getCoinName());
			redpacketMessage.setText(publicRedPacket.getRemark());
			redpacketMessage.setCreateTime(publicRedPacket.getCreateTime().getTime());
			redpacketMessage.setExpireTime(publicRedPacket.getExpireTime());
			redpacketMessage.setFee(publicRedPacket.getFee());
			redpacketMessage.setStatus(EnumStatus.ZORO.getIndex());
			message.setContent(redpacketMessage);
			logger.info("redpacket mqtt message:" + JSON.toJSONString(message));
			//MessagePublisher.getInstance().addPublish(Message.UPLINK_MESSAGE + publicRedPacket.getUserId(), message);
			try {
				com.taiyiyun.passport.mqtt.v2.MessagePublisher.getInstance().publish(Message.UPLINK_MESSAGE + publicRedPacket.getUserId(), message);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else {
			for (GroupMember groupMember : gmembers) {
				Message<Object> message = new Message<>();
				message.setVersion(1);
				message.setMessageType(2);
				message.setFromUserId(publicRedPacket.getUserId());
				message.setFromClientId(Config.get("mqtt.clientId"));
				message.setMessageId(messageId);
				message.setSessionType((int) publicRedPacket.getSessionType());
				message.setSessionId(publicRedPacket.getSessionId());
				message.setPublishTime(currentTimes);
				message.setUpdateTime(currentTimes);
				message.setContentType(214);
				//设置消息content
				RedpacketMessage redpacketMessage = new RedpacketMessage();
				redpacketMessage.setFromUserId(publicRedPacket.getUserId());
				redpacketMessage.setRedPacketId(publicRedPacket.getPackId());
				redpacketMessage.setRedpacketCount(publicRedPacket.getPackCount());
				redpacketMessage.setRedpacketType(publicRedPacket.getPackType());
				redpacketMessage.setAmount(publicRedPacket.getAmount());
				redpacketMessage.setName(groupMember.getNikeName());
				redpacketMessage.setAvatarUrl(Misc.getServerUri(null, groupMember.getAvatarUrl()));
				redpacketMessage.setPlatformId(publicRedPacket.getPlatformId());
				redpacketMessage.setPlatformName(publicRedPacket.getPlatformName());
				redpacketMessage.setCoinId(publicRedPacket.getCoinId());
				redpacketMessage.setCoinName(publicRedPacket.getCoinName());
				redpacketMessage.setText(publicRedPacket.getRemark());
				redpacketMessage.setCreateTime(publicRedPacket.getCreateTime().getTime());
				redpacketMessage.setExpireTime(publicRedPacket.getExpireTime());
				redpacketMessage.setFee(publicRedPacket.getFee());
				redpacketMessage.setStatus(EnumStatus.ZORO.getIndex());
				message.setContent(redpacketMessage);
				logger.info("redpacket mqtt message:" + JSON.toJSONString(message));
				//MessagePublisher.getInstance().addPublish(Message.DOWNLINK_MESSAGE + groupMember.getUserId(), message);
				try {
					com.taiyiyun.passport.mqtt.v2.MessagePublisher.getInstance().publish(Message.UPLINK_MESSAGE + groupMember.getUserId(), message);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return messageId;
	}


	private final void sendRedpacketMessage(PublicRedPacket publicRedPacket,
			PublicRedPacketDetail publicRedPacketDetail,long messageId) {
  
		if (!publicRedPacketDetail.getToUserId().equals(publicRedPacket.getUserId()))  {
			Message<Object> message = new Message<>();
			message.setVersion(1);
			message.setMessageType(2);
			message.setFromUserId(publicRedPacketDetail.getToUserId());
			message.setToUserId(publicRedPacket.getUserId());
			message.setFromClientId(Config.get("mqtt.clientId"));
			message.setMessageId("Redpacket-"  + publicRedPacket.getPackId() + "-" +  messageId);
			message.setSessionType((int) publicRedPacket.getSessionType());
			message.setSessionId(publicRedPacket.getSessionId());
			message.setPublishTime(messageId);
			message.setUpdateTime(messageId);

			// 设置消息content
			AcceptRedpacketMessage acceptRedpacketMessage = new AcceptRedpacketMessage();
			long createTime = System.currentTimeMillis();
			acceptRedpacketMessage.setAcceptTime(createTime);
			acceptRedpacketMessage.setRedPacketId(publicRedPacket.getPackId());
			acceptRedpacketMessage.setToUserId(publicRedPacket.getUserId());
			message.setContentType(215);
			acceptRedpacketMessage.setFromUserId(publicRedPacketDetail.getToUserId());
	
			PublicUser fromUser = publicUserDao.getByUserId(publicRedPacketDetail.getToUserId());
			if (null != fromUser) {
				acceptRedpacketMessage.setFromUserName(fromUser.getUserName());
			} else {
				acceptRedpacketMessage.setFromUserName("");
			}
			
			PublicUser toUser = publicUserDao.getByUserId(publicRedPacket.getUserId());
			if (null != toUser) { 
				acceptRedpacketMessage.setToUserName(toUser.getUserName());
			} else {
				acceptRedpacketMessage.setToUserName("");
			}

			message.setContent(acceptRedpacketMessage);
			logger.info("redpacket mqtt message:" + JSON.toJSONString(message));
			//MessagePublisher.getInstance().addPublish(Message.DOWNLINK_MESSAGE + publicRedPacket.getUserId(),message);
			try {
				com.taiyiyun.passport.mqtt.v2.MessagePublisher.getInstance().publish(Message.UPLINK_MESSAGE + publicRedPacket.getUserId(), message);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		
	}

	private final Message<JSONObject> acceptRedpacketMessage(PublicRedPacket publicRedPacket,PublicRedPacketDetail publicRedPacketDetail,long messageId) {

		Message<JSONObject> message = new Message<JSONObject>();
		message.setVersion(1);
		message.setMessageType(2);
		message.setFromUserId(publicRedPacket.getUserId());
		message.setToUserId(publicRedPacketDetail.getToUserId());
		message.setFromClientId(Config.get("mqtt.clientId"));
		message.setMessageId("Redpacket-" + publicRedPacket.getPackId() + "-" + messageId);
		message.setSessionType((int) publicRedPacket.getSessionType());
		message.setSessionId(publicRedPacket.getSessionId());
		message.setPublishTime(messageId);
		message.setUpdateTime(messageId);

		// 设置消息content
		JSONObject retResult = new JSONObject();
		retResult.put("redPacketId", publicRedPacket.getPackId());
		retResult.put("fromUserId", publicRedPacket.getUserId());
		retResult.put("toUserId", publicRedPacketDetail.getToUserId());
		retResult.put("amount", publicRedPacketDetail.getAmount());
		message.setContentType(216);

		PublicUser fromUser = publicUserDao.getByUserId(publicRedPacket.getUserId());
		if (null != fromUser) {
			retResult.put("fromUserName", fromUser.getUserName());
		} else {
			retResult.put("fromUserName", "");
		}
		PublicUser toUser = publicUserDao.getByUserId(publicRedPacketDetail.getToUserId());
		if (null != toUser) {
			retResult.put("toUserName", toUser.getUserName());
		} else {
			retResult.put("toUserName", "");
		}
		
		message.setContent(retResult);
		logger.info("redpacket mqtt message:" + JSON.toJSONString(message));
	   return message;
	}

	private final Message<JSONObject> acceptRedpacketMessage(PublicRedPacket publicRedPacket) {
		Message<JSONObject> message = new Message<JSONObject>();
		// 设置消息content
		JSONObject retResult = new JSONObject();
		retResult.put("redPacketId", publicRedPacket.getPackId());
		message.setContentType(216);
		message.setContent(retResult);
		logger.info("redpacket mqtt message:" + JSON.toJSONString(message));
	   return message;
	}

	/**
	 * 生成红包
	 * @param count
	 * @param totalAmount
	 * @param scale
	 * @param quota
	 * @return
	 */
	private List<BigDecimal> generateRandom(int count,BigDecimal totalAmount,int scale,BigDecimal quota) {
		BigDecimal money;
		if (scale == 0){
			scale = 8;
		}
		//最小金额
		BigDecimal min = quota.setScale(scale,BigDecimal.ROUND_HALF_UP);
		totalAmount = totalAmount.setScale(scale,BigDecimal.ROUND_HALF_UP);
		BigDecimal total = totalAmount.setScale(scale,BigDecimal.ROUND_HALF_UP);
		BigDecimal max;
		int i = 1;
		//比率
		List<BigDecimal> result = new ArrayList<>();
		BigDecimal totalAmt = BigDecimal.ZERO;
		while (i < count) {
			//保证即使一个红包是最大的了,后面剩下的红包,每个红包也不会小于最小值
			max = total.subtract(min.multiply(new BigDecimal(count - i)));
			int k = (int)(count - i)/2 + 1 ;
			//保证最后两个人拿的红包不超出剩余红包
			if (count - i <= 2) {
				k = count - i;
			}
			//最大的红包限定的平均线上下
			max = max.divide(new BigDecimal(k),BigDecimal.ROUND_HALF_UP).setScale(scale,BigDecimal.ROUND_HALF_UP);
			//保证每个红包大于最小值,又不会大于最大值
			money = min.add(new BigDecimal(Math.random()).multiply(max.subtract(min)).setScale(scale,BigDecimal.ROUND_HALF_UP));
			//保留两位小数
			if (money.compareTo(min) < 0){
				money = min;
			}
			total = total.subtract(money).setScale(scale,BigDecimal.ROUND_HALF_UP);
			result.add(money);
			totalAmt = totalAmt.add(money);
			logger.info("第" + i + "个人拿到" + money + "剩下" + total);
			i++;
			//最后一个人拿走剩下的红包
			if (i == count) {
				result.add(total);
				totalAmt = totalAmt.add(total);
				logger.info("第" + i + "个人拿到" +  total + "剩下0");
			}
		}
		//只发一个红包
		if (count == 1 && i == count) {
			result.add(total);
			totalAmt = totalAmt.add(total);
			logger.info("第" + i + "个人拿到所有红包：" +  total);
		}
		//取数组中最大的一个值的索引
		logger.info("本轮发红包中第" + (result.indexOf(Collections.max(result)) + 1) + "个人手气最佳");
		logger.info("本轮发红包总金额：" + totalAmt);
		return result;
	}
	
	private void updateRedPacketStatus (String redPacketId,Integer packStatus,TransactionStatus status) {
		try {
			PublicRedPacket publicRedPacket = new PublicRedPacket();
			publicRedPacket.setPackId(redPacketId);
			publicRedPacket.setPackStatus(packStatus);
			publicRedPacketDao.updateRedPacketStatus(publicRedPacket);
			transactionManager.commit(status);  
		} catch (Exception ex) {
			transactionManager.rollback(status);
			ex.printStackTrace();
		}
	}
	
	private final TransactionStatus  getTransactionManager () {
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();  
		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW); // 事物隔离级别，开启新事务，这样会比较安全些。  
		TransactionStatus status = transactionManager.getTransaction(def); // 获得事务状态
		return status;
	}
	
	private final int updateRedPacketDetail (PublicRedPacketDetail redPacketDetail,String detailId,String packId,int transferStatus) {
		redPacketDetail.setDetailId(detailId);
		redPacketDetail.setPackId(packId);
		redPacketDetail.setTransferStatus((short) transferStatus);
		redPacketDetail.setTransferTime(new java.util.Date());
		return publicRedPacketDetailDao.updateRedPacketDetail(redPacketDetail);
	}
	
	/**
	 * 抢红包业务逻辑处理
	 * @param packBundle
	 * @param redpacketId
	 * @param recipientUserId
	 * @return
	 */
	public RedPacketRetParam grabS(
			final PackBundle packBundle, 
			final String redpacketId,
			final String recipientUserId)  {
		
		RedPacketRetParam  redPacketRetParam = new RedPacketRetParam();
		Message<JSONObject> retResult = null;
		if (isRedpacketExpired(redpacketId)) {
			/** 红包过期 */
			PublicRedPacket publicRedPacket = publicRedPacketDao.getOneById(redpacketId);
			redPacketRetParam.setRedpacketStatus(EnumStatus.THREE.getIndex());
			redPacketRetParam.setMessage(acceptRedpacketMessage(publicRedPacket));
			updateRedPacketStatus (redpacketId,EnumStatus.TWO.getIndex(), getTransactionManager ());
			try {
				redisService.evict(redpacketId);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return redPacketRetParam;
		}
		if (isRedpacketInvalid(redpacketId)) {
			/** 红包失效 */
			PublicRedPacket publicRedPacket = publicRedPacketDao.getOneById(redpacketId);
			redPacketRetParam.setRedpacketStatus(EnumStatus.FOUR.getIndex());
			redPacketRetParam.setMessage(acceptRedpacketMessage(publicRedPacket));
			updateRedPacketStatus (redpacketId,EnumStatus.THREE.getIndex(), getTransactionManager());
			return redPacketRetParam;
		}
		try {
		DistributedRedisLock.acquire(redpacketId.concat(recipientUserId));
		if (!isGrabbed(redpacketId, recipientUserId)) {
			String value = null;
			TransactionStatus status = null;
			try {
				value = redisService.pop(redpacketId);
				if (null != value && !"".equals(value)) {
					JSONObject json = JSONObject.parseObject(value);
					if (null != json) {
						logger.info("get pop  repacket:" + json.toJSONString());
						String detailId = json.getString("detailId");
						String packId = json.getString("packId");
						status = getTransactionManager ();
						PublicRedPacketDetail publicRedPacketDetail = new PublicRedPacketDetail();
						publicRedPacketDetail.setDetailId(detailId);
						publicRedPacketDetail.setPackId(packId);
						publicRedPacketDetail.setToUserId(recipientUserId);
						publicRedPacketDetail.setReceiveTime(new java.util.Date());
						publicRedPacketDetail.setRecordTimestamp(System.nanoTime());
						int result = publicRedPacketDetailDao.updateRedPacketDetail(publicRedPacketDetail);
						if (result > 0) {
							commit(status); 
							logger.info("updateRedPacketDetail.....toUserId update success......................."+ result);
							if (!isReceiverUserInvalid(redpacketId, recipientUserId)) {
								/** 转账处理 */
								try {
									retResult = transfer(packBundle, detailId, packId, recipientUserId);
								} catch (Exception ex) {
									status = getTransactionManager ();
									PublicRedPacketDetail redPacketDetail = new PublicRedPacketDetail();
									updateRedPacketDetail (redPacketDetail, detailId, packId, EnumStatus.TWO.getIndex());
									commit(status); 
									/** 不走转账流程 */
									redPacketRetParam = notTransferHander(packBundle, detailId, packId, recipientUserId, redPacketDetail.getTransferTime());
									if (null != redPacketRetParam) {
										logger.info("updateRedPacketDetail.....toUserId update success.....transfer exception.....Do not perform the transfer............."+ result);									
										return redPacketRetParam;
									}
									ex.printStackTrace();
								}							
								if (null != retResult) {							
									logger.info("updateRedPacketDetail.....transfer success......................." + JSONObject.toJSONString(retResult));
									status = getTransactionManager ();
									PublicRedPacketDetail redPacketDetail = new PublicRedPacketDetail();
									int ret = updateRedPacketDetail (redPacketDetail, detailId, packId, EnumStatus.ONE.getIndex());
									if (ret > 0) {
										logger.info("updateRedPacketDetail.....TransferStatus  and TransferTime update success......................."+ ret);
										retResult.getContent().put("acceptTime", redPacketDetail.getTransferTime());
										redPacketRetParam.setRedpacketStatus(EnumStatus.ONE.getIndex());
										redPacketRetParam.setMessage(retResult);
										commit(status); 
										logger.info("updateRedPacketDetail.....PackStatus update success   transactionManager.commit................."+ ret);
										return redPacketRetParam;
									} else {
										logger.info("updateRedPacketDetail.....TransferStatus  and TransferTime update failure......................." + ret);
										redPacketRetParam.setRedpacketStatus(EnumStatus.NINETY_NINE.getIndex());
										transactionManager.rollback(status);
										return redPacketRetParam;
									}
								}
							} else {
								/** 不走转账流程 */
								redPacketRetParam = notTransferHander(packBundle, detailId, packId, recipientUserId, publicRedPacketDetail.getReceiveTime());
								if (null != redPacketRetParam) {
									return redPacketRetParam;
								}
							}
						} else {
							logger.info("updateRedPacketDetail.....toUserId update failure......................."+ result);
							transactionManager.rollback(status);
							return setSystemError(redPacketRetParam, redpacketId, value);
						}
					}
				} else {
					/** 钱包ID */
					PublicRedPacket publicRedPacket = publicRedPacketDao.getOneById(redpacketId);
					redPacketRetParam.setRedpacketStatus(EnumStatus.TWO.getIndex());
					redPacketRetParam.setMessage(acceptRedpacketMessage(publicRedPacket));
					updateRedPacketStatus (redpacketId,EnumStatus.ONE.getIndex(), getTransactionManager());
					return redPacketRetParam;
				}
			} catch (Exception ex) {
				redPacketRetParam.setRedpacketStatus(EnumStatus.NINETY_NINE.getIndex());
				ex.printStackTrace();
				return redPacketRetParam;
			}
		} else {
			/** 钱包ID */
			PublicRedPacket publicRedPacket = publicRedPacketDao.getOneById(redpacketId);
			redPacketRetParam.setRedpacketStatus(EnumStatus.FIVE.getIndex());
			redPacketRetParam.setMessage(acceptRedpacketMessage(publicRedPacket));
			return redPacketRetParam;
		}}finally {
			DistributedRedisLock.release(redpacketId.concat(recipientUserId));
		}
		return null;
	}
	
	private final RedPacketRetParam notTransferHander (final PackBundle packBundle,String detailId,String packId,String recipientUserId,Date acceptTime) {
		final RedPacketRetParam redPacketRetParam = new RedPacketRetParam();
		/** 不走转账流程 */
		Message<JSONObject>	retResult = notTransfer(packBundle, detailId, packId, recipientUserId);
		if (null != retResult) {
			retResult.getContent().put("acceptTime", acceptTime);
			redPacketRetParam.setRedpacketStatus(EnumStatus.SIX.getIndex());
			redPacketRetParam.setMessage(retResult);
			logger.info("updateRedPacketDetail.....toUserId update success..........Do not perform the transfer............."+ retResult);
			return redPacketRetParam;
		}
		return null;
	}
	
	 final void commit (final TransactionStatus status) {
		try {
			transactionManager.commit(status);
		} catch (Exception ex) {
			transactionManager.rollback(status);
			ex.printStackTrace();
		}
	}
    
	/**
	 * 抢红包处理
	 */
	@Override
	public DeferredResult<String> grab (
			final PackBundle packBundle,
			final String redpacketId,
			final String recipientUserId) {
		
		final DeferredResult<String> deferred = new DeferredResult<>();
		try {
			PublicRedPacket publicRedPacket = publicRedPacketDao.getOneById(redpacketId);
			if (null == publicRedPacket) {
				setRetunMessage(deferred, EnumStatus.THREE.getIndex(),packBundle.getString("redpacketid.incorrect"));
				return deferred;
			}
			if (null == recipientUserId || "".equals(recipientUserId)) {
				setRetunMessage(deferred, EnumStatus.NINETY_EIGHT.getIndex(),packBundle.getString("redpacket.not.login"));
				return deferred;
			}
			PublicUser sendUser = publicUserDao.getByUserId(publicRedPacket.getUserId());
			PublicUser acceptUser = publicUserDao.getByUserId(recipientUserId);
			if (null == sendUser || null == acceptUser) {
				setRetunMessage(deferred, EnumStatus.ONE.getIndex(),packBundle.getString("redpacket.userinfo.exit"));
				return deferred;
			}
		} catch (Exception ex) {
			setRetunMessage(deferred, EnumStatus.NINETY_NINE.getIndex(),packBundle.getString("system.error"));
			return deferred;
		}
		  executorService.submit(new Runnable() {
			@Override
			public void run() {
				RedPacketRetParam redPacketRetParam;
				try {
					long startTime = System.currentTimeMillis();
					redPacketRetParam = grabS(packBundle,redpacketId,recipientUserId);
					if (null != redPacketRetParam) {					
						if (EnumStatus.NINETY_NINE.getIndex() == redPacketRetParam.getRedpacketStatus()) {
							setRetunMessage(deferred, EnumStatus.NINETY_NINE.getIndex(),packBundle.getString("system.error"));
						} else {							
							if (null != redPacketRetParam.getMessage() 
									&& (redPacketRetParam.getRedpacketStatus() == EnumStatus.ONE.getIndex() 
									|| redPacketRetParam.getRedpacketStatus() == EnumStatus.SIX.getIndex())) {
								ReturnResult<JSONObject> retContext = new ReturnResult<JSONObject>();
								JSONObject context = new JSONObject();
							    context.put("amount", redPacketRetParam.getMessage().getContent().getBigDecimal("amount"));
							    context.put("redpacketStatus", redPacketRetParam.getRedpacketStatus());
							    context.put("redpacketId", redPacketRetParam.getMessage().getContent().getString("redPacketId"));
							    redPacketRetParam.getMessage().getContent().remove("amount");
							    JSONArray jsonArray=new JSONArray();
							    jsonArray.add(redPacketRetParam.getMessage());
							    context.put("message",jsonArray);
								retContext.setStatus(0);
								retContext.setData(context);
								String rjson = JSON.toJSONString(retContext);
								logger.info("PublicRedPacketServiceImpl.grabS.error:" + rjson);
								deferred.setResult(rjson);
							} else {
								ReturnResult<JSONObject> retContext = new ReturnResult<JSONObject>();
								retContext.setStatus(0);
								JSONObject context = new JSONObject();
							    context.put("amount", null);
							    context.put("redpacketStatus", redPacketRetParam.getRedpacketStatus());
							    context.put("redpacketId", redPacketRetParam.getMessage().getContent().getString("redPacketId"));
							    context.put("message",null);
							    retContext.setData(context);
								String rjson = JSON.toJSONString(retContext);
								logger.info("PublicRedPacketServiceImpl.grabS.error:" + rjson);
								deferred.setResult(rjson);
							}
						}
					}
					System.out.println("每个线程消耗的时间......................................."+(System.currentTimeMillis()-startTime));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					logger.info("PublicRedPacketServiceImpl.grabS.error:" + e.getMessage());
				}
			}
		});

		return deferred;
	}
	
	private final RedPacketRetParam setSystemError (final RedPacketRetParam redPacketRetParam,String redpacketId,String value) {
		PublicRedPacket publicRedPacket = publicRedPacketDao.getOneById(redpacketId);
		if (null != publicRedPacket) {
			redisService.push(redpacketId, value, publicRedPacket.getExpireTime());
			redPacketRetParam.setRedpacketStatus(EnumStatus.NINETY_NINE.getIndex());
			return redPacketRetParam;
		}
		redPacketRetParam.setRedpacketStatus(EnumStatus.NINETY_NINE.getIndex());
		return redPacketRetParam;
	}
	
	private final void setRetunMessage (final DeferredResult<String> deferred,Integer status,String bundleStr) {
		ReturnResult<Message<JSONObject>> rst = new ReturnResult<Message<JSONObject>>();
		rst.setStatus(status);
		rst.setError(bundleStr);
		rst.setData(new Message<JSONObject>());
		deferred.setResult(JSON.toJSONString(rst));
	}


	/**
	 * 抢红包转账
	 * @param packBundle
	 * @param detailId
	 * @param packId
	 * @param acceptUserId
	 * @return
	 * @throws DefinedError
	 */
	private final Message<JSONObject> transfer(PackBundle packBundle, String detailId, String packId, String acceptUserId) throws DefinedError {

		PublicRedPacketDetail publicRedPacketDetailP = new PublicRedPacketDetail();
		publicRedPacketDetailP.setDetailId(detailId);
		publicRedPacketDetailP.setPackId(packId);

		PublicRedPacketDetail publicRedPacketDetail = null;
		List<PublicRedPacketDetail> list = publicRedPacketDetailDao.list(publicRedPacketDetailP);
		if (null != list && list.size() > 0) {
			publicRedPacketDetail = list.get(0);
		}

		if (null == publicRedPacketDetail) {
			throw new RuntimeException ("publicRedPacketDetailDao.list(publicRedPacketDetailP) is null");
		}

		/** 钱包ID */
		PublicRedPacket publicRedPacket = publicRedPacketDao.getOneById(packId);
		if (null  == publicRedPacket) {
			throw new RuntimeException ("publicRedPacketDao.getOneById is null"+packId);
		}

		// 抢红包者ID
		PublicUser acceptUser = publicUserDao.getByUserId(acceptUserId);
		if (null  == acceptUser) {
			throw new RuntimeException ("publicUserDao.getByUserId is null"+acceptUserId);
		}
		PublicUser sendUser = publicUserDao.getByUserId(publicRedPacket.getUserId());
		if (null  == sendUser) {
			throw new RuntimeException ("publicUserDao.getByUserId is null"+publicRedPacket.getUserId());
		}

		Trade trade = new Trade();
		trade.setPlatformId(publicRedPacket.getPlatformId());
		trade.setCoinId(publicRedPacket.getCoinId());
		trade.setFee(publicRedPacketDetail.getFee());
		trade.setAmount(publicRedPacketDetail.getAmount());
		trade.setFromUserId(null);
		trade.setFromUuid(sendUser.getUuid());
		trade.setToUserId(publicRedPacketDetail.getToUserId());
		trade.setToUuid(acceptUser.getUuid());
		trade.setText(publicRedPacket.getRemark());
		trade.setFrozenId(publicRedPacketDetail.getFreezeTradeId());

		CoinInfo coin = AssetCache.getInstance().getCoin(trade.getPlatformId(), trade.getCoinId());
		BigDecimal price = null;
		if (coin != null) {
			trade.setCoinName(coin.getDisplay());
			price = coin.getCoinprice();
			trade.setWorthRmbAccept(price);
		}

		ThirdAppUserBindExt extTo = thirdAppUserBindDao.getOneExtByCondition(acceptUser.getUuid(),
				publicRedPacket.getPlatformId(), null);
		if (extTo == null || !extTo.isBinded()) {
			String errMsg = "接收方未绑定指定货币";
			if (packBundle != null) {
				errMsg = packBundle.getString("transfer.doacceptmoney.condition.receivernobindmoney");
			}
			throw new DefinedError.ConditionException(errMsg, null);
		}

		ThirdAppUserBindExt extFrom = thirdAppUserBindDao.getOneExtByCondition(sendUser.getUuid(),
				publicRedPacket.getPlatformId(), null);
		if (extFrom == null || !extFrom.isBinded()) {
			String errMsg = "转账发起方已经解除交易绑定";
			if (packBundle != null) {
				errMsg = packBundle.getString("transfer.acceptmoney.fail.transoriginremovedtransbind");
			}
			throw new DefinedError.ConditionException(errMsg, null);
		}

		ITransferAccounts accounts = ThirdAppManager.getInstance().getAccountById(publicRedPacket.getPlatformId());
		String relateId = thirdAppUserBindDao.getRelateId(publicRedPacket.getPlatformId());
		AskBody body = accounts.askForToken(packBundle, extFrom, relateId);
		if (body.getToken() == null) {
			String errMsg = "获取token失败";
			logger.info("errMsg:" + errMsg);
			if (packBundle != null) {
				errMsg = packBundle.getString("yuanbao.token.failed");
			}
			throw new DefinedError.ThirdRefuseException(errMsg, null);
		}
		
		long startTime = System.currentTimeMillis();
		/** 转账处理 */
		TransferResult transferResult = accounts.askForTransfer(packBundle, body, 
				publicRedPacket.getCoinId(),
				publicRedPacketDetail.getAmount(), 
				publicRedPacketDetail.getFreezeTradeId(),
				publicRedPacketDetail.getFee(), extTo.getUserKey());
		if (transferResult.isSuccess()) {
			logger.info("tradeId:" + trade.getTradeId()+"--转账确认到账.askForTransfer--消耗时间:"+(System.currentTimeMillis() - startTime));
			try {
				trade.setStatus(4);
				trade.setError("");
				trade.setAcceptTime(new Date().getTime());
				trade.setCreateTime(System.currentTimeMillis());
				int result = tradeDao.saveTransferInfo(trade);
				System.out.println("tradeId:" + trade.getTradeId()+"--tradeDao.saveTransferInfo："+result+"--消耗时间:"+(System.currentTimeMillis() - startTime));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			String errMsg = "转账接收成功";
			if (packBundle != null) {
				errMsg = packBundle.getString("transfer.doacceptmoney.success.transsuccess");
			}
			logger.info("errMsg:" + errMsg+"|getFroId():"+transferResult.getFroId());
			long messageId = System.currentTimeMillis();
			sendRedpacketMessage(publicRedPacket, publicRedPacketDetail,messageId);
			Message<JSONObject> message = acceptRedpacketMessage(publicRedPacket, publicRedPacketDetail, messageId);
			try {
				String topic = Message.UPLINK_MESSAGE + publicRedPacketDetail.getToUserId();
				com.taiyiyun.passport.mqtt.v2.MessagePublisher.getInstance().publish(topic, message);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return message;
		} else {
			String errMsg = "转账失败";
			if (packBundle != null) {
				errMsg = packBundle.getString("transfer.doacceptmoney.fail.transfailed");
			}
			throw new DefinedError.ThirdRefuseException(errMsg, transferResult.getErrorCode().toString());
		}
	}

	private final Message<JSONObject> notTransfer(PackBundle packBundle, String detailId, String packId, String acceptUserId) {
		PublicRedPacketDetail publicRedPacketDetailP = new PublicRedPacketDetail();
		publicRedPacketDetailP.setDetailId(detailId);
		publicRedPacketDetailP.setPackId(packId);
		PublicRedPacketDetail publicRedPacketDetail = null;
		List<PublicRedPacketDetail> list = publicRedPacketDetailDao.list(publicRedPacketDetailP);
		if (null != list && list.size() > 0) {
			publicRedPacketDetail = list.get(0);
		}
		if (null == publicRedPacketDetail) {
			throw new RuntimeException ("publicRedPacketDetailDao.list(publicRedPacketDetailP) is null");
		}
		/** 钱包ID */
		PublicRedPacket publicRedPacket = publicRedPacketDao.getOneById(packId);
		if (null  == publicRedPacket) {
			throw new RuntimeException ("publicRedPacketDao.getOneById is null"+packId);
		}
		long messageId = System.currentTimeMillis();
		sendRedpacketMessage(publicRedPacket, publicRedPacketDetail,messageId);
		return acceptRedpacketMessage(publicRedPacket, publicRedPacketDetail,messageId);
	}

	@Override
	public ResultData<RedpacketDto<Map<String,Object>>> detail(PackBundle bundle, String userId, String redpacketId, Integer page, Integer rows,Long timestamp) {
		//业务结果通用返回对象
		ResultData<RedpacketDto<Map<String,Object>>> resultData = new ResultData<>();
		//验证用户信息是否存在
		PublicUser publicUser = userService.getByUserId(userId);
		if (publicUser == null){
			String errMsg = "user not find";
			if(bundle != null) {
				errMsg = bundle.getString("user.not.find");
			}
			resultData.setStatus(EnumStatus.TWO.getIndex());
			resultData.setMessage(errMsg);
			return resultData;
		}
		//验证红包信息是否存在
		PublicRedPacket publicRedPacket = publicRedPacketDao.getOneById(redpacketId);
		if (publicRedPacket == null){
			String errMsg = "redpacket not find";
			if(bundle != null) {
				errMsg = bundle.getString("redpacket.not.find");
			}
			resultData.setStatus(EnumStatus.THREE.getIndex());
			resultData.setMessage(errMsg);
			return resultData;
		}
		//查询群组信息,如果sessiontype为1，代表群组
		String nickName = null;
		Short sessionType = publicRedPacket.getSessionType();
		if (sessionType.intValue() == EnumType.T1.getIndex()){
			String sessionId = publicRedPacket.getSessionId();
			Map<String,String> paramMap = new HashMap<>();
			paramMap.put("groupId",sessionId);
			paramMap.put("userId",userId);
			GroupMember groupMember = groupMemberDao.selectGroupUserInfoOnLogin(paramMap);
			if (groupMember != null){
				nickName = groupMember.getNikeName();
			}
		}
		//红包总数量
		Integer packCount = publicRedPacket.getPackCount();
		//红包总金额
		BigDecimal amount = publicRedPacket.getAmount();
		//查询已抢红包金额和数量
		BigDecimal grabedAmount = new BigDecimal(0);
		//发红包人信息
		String handoutUserId = publicRedPacket.getUserId();
		PublicUser handoutUser = userService.getByUserId(handoutUserId);
		String handOutNickName = null;
		String sessionId = publicRedPacket.getSessionId();
		Map<String,String> paramMap = new HashMap<>();
		paramMap.put("groupId",sessionId);
		paramMap.put("userId",handoutUser.getId());
		GroupMember groupMember = groupMemberDao.selectGroupUserInfoOnLogin(paramMap);
		if (groupMember != null){
			handOutNickName = groupMember.getNikeName();
		}
		//返回业务数据
		RedpacketDto<Map<String,Object>> redpacket = new RedpacketDto<>();
		redpacket.setRedpacketId(redpacketId);
		if (handoutUser != null) {
			redpacket.setUserId(handoutUser.getId());
			redpacket.setUserName(handoutUser.getUserName());
			redpacket.setNikeName(handOutNickName == null ? handoutUser.getUserName() : handOutNickName);
			redpacket.setAvatarUrl(Misc.getServerUri(null, handoutUser.getAvatarUrl()));
		}
		//币信息
		String handoutCoinId = publicRedPacket.getCoinId();
		String handoutPlatformId = publicRedPacket.getPlatformId();
		String handoutCoinName = null;
		CoinInfo handoutCoin = AssetCache.getInstance().getCoin(handoutPlatformId, handoutCoinId);
		if (handoutCoin != null){
			handoutCoinName = handoutCoin.getName();
		}
		redpacket.setPlatformId(publicRedPacket.getPlatformId());
		redpacket.setCoinId(publicRedPacket.getCoinId());
		redpacket.setCoinName(handoutCoinName);
		redpacket.setText(publicRedPacket.getRemark());
		redpacket.setTotalCount(publicRedPacket.getPackCount());
		redpacket.setTotalAmount(amount);

		//分页查询红包明细信息
		page = (page == null || page == 0)?1:page;
		rows = (rows == null || rows == 0) ?20:rows;
		int start = (page -1)*rows;
		redpacket.setCurrentPage(page);
		PublicRedPacketDetail publicRedPacketDetailParam = new PublicRedPacketDetail();
		publicRedPacketDetailParam.setStart(start);
		publicRedPacketDetailParam.setOffset(rows);
		publicRedPacketDetailParam.setPackId(redpacketId);
		publicRedPacketDetailParam.setRecordTimestamp(timestamp);
//        publicRedPacketDetailParam.setTransferStatus((short) EnumStatus.ONE.getIndex());
		List<PublicRedPacketDetail> details = publicRedPacketDetailDao.listRedpacketToUserPage(publicRedPacketDetailParam);
		PublicRedPacketDetail publicRedPacketDetail = publicRedPacketDetailDao.getSumAndCountByPackId(redpacketId);
		Integer grabedCount = 0;
		if (publicRedPacketDetail != null){
			grabedAmount = publicRedPacketDetail.getGrabedAmount() == null?new BigDecimal(0):publicRedPacketDetail.getGrabedAmount();
			grabedCount = publicRedPacketDetail.getGrabedCount();
		}
		//设置总数量
		redpacket.setGrabedCount(grabedCount);
		//设置抢的总金额
		redpacket.setGrabAmount(grabedAmount);
		//处理返回列表
		if (details != null && details.size() > 0){
			//设置红包明细dto
			List<Map<String,Object>> detailDtos = new ArrayList<>(details.size());
			for (PublicRedPacketDetail detail:details){
				Map<String,Object> resultMap = new HashMap<>();
				resultMap.put("amount",detail.getAmount());
				resultMap.put("avatarUrl",Misc.getServerUri(null, detail.getAvatarUrl()));
				resultMap.put("grabTime",DateUtil.toString(detail.getReceiveTime(), DateUtil.Format.DATE_TIME_HYPHEN));
				//只有红包抢完了，才有是否手气最佳的现实，否则都是0
				if (grabedCount.intValue() == packCount.intValue() && grabedCount > 0) {
					resultMap.put("isBest", detail.getIsBest());
				}
				else{
					resultMap.put("isBest", EnumStatus.ZORO.getIndex());
				}
				resultMap.put("nikeName",detail.getNikeName());
				resultMap.put("userId",detail.getToUserId());
				resultMap.put("userName",detail.getUserName());
				resultMap.put("timestamp",detail.getRecordTimestamp());
				detailDtos.add(resultMap);
			}
			redpacket.setPageList(detailDtos);
		}
		else{
			redpacket.setPageList(new ArrayList<>());
		}
		//如果红包数量等于抢的数量，红包已经抢完，获取抢红包花费的时间
		if (grabedCount != null && packCount.equals(grabedCount)){
			Date createDate = publicRedPacket.getCreateTime();
			long ltime = 0;
			PublicRedPacketDetail lastRedPacketDetail = publicRedPacketDetailDao.getLastOneGrabed(redpacketId);
			if (lastRedPacketDetail != null){
				Date lastDate = lastRedPacketDetail.getReceiveTime();
				if (lastDate != null){
					ltime = lastDate.getTime();
				}
			}
			long ctime = createDate.getTime();
			if (ltime > 0){
				long second = (ltime - ctime)/1000;
				if (second > 60) {
					long h = second/3600;
					long m = (second%3600)/60;
					long s = (second%3600)%60;
					if (h >= 1){
						String hour = bundle.getString("redpacket.grab.hour",String.valueOf(h),String.valueOf(m),String.valueOf(s));
						redpacket.setGrabAllTimes(hour);
					}
					else{
						String hour = bundle.getString("redpacket.grab.minute",String.valueOf(m),String.valueOf(s));
						redpacket.setGrabAllTimes(hour);
					}
				}
				else{
					if (second == 0){
						second = 1;
					}
					String secondStr = bundle.getString("redpacket.grab.second",String.valueOf(second));
					redpacket.setGrabAllTimes(secondStr);
				}
			}
		}
		//查询当前用户抢红包情况
		PublicRedPacketDetail redPacketDetail = new PublicRedPacketDetail();
		redPacketDetail.setPackId(redpacketId);
		redPacketDetail.setToUserId(userId);
		redPacketDetail = publicRedPacketDetailDao.getOneByPackAndUserId(redPacketDetail);
		if (redPacketDetail != null){
			String platformId = publicRedPacket.getPlatformId();
			String coinId = publicRedPacket.getCoinId();
			String coinName = null;
			CoinInfo coin = AssetCache.getInstance().getCoin(platformId, coinId);
			if (coin != null){
				coinName = coin.getName();
			}
			redpacket.setIsGrab((short) EnumStatus.ONE.getIndex());
			Map<String,Object> grabUserMap = new HashMap<>();
			grabUserMap.put("userId",userId);
			grabUserMap.put("userName",publicUser.getUserName());
			grabUserMap.put("nikeName",nickName);
			grabUserMap.put("avatarUrl",Misc.getServerUri(null, publicUser.getAvatarUrl()));
			grabUserMap.put("platformId",platformId);
			grabUserMap.put("coinId",coinId);
			grabUserMap.put("coinName",coinName);
			grabUserMap.put("text",publicRedPacket.getRemark());
			grabUserMap.put("redpacketType",publicRedPacket.getPackType());
			grabUserMap.put("grabAmount",redPacketDetail.getAmount());
			BigDecimal grabCashAmount = redPacketDetail.getCashAmount() == null?BigDecimal.ZERO:redPacketDetail.getCashAmount();
			grabUserMap.put("grabCashAmount",grabCashAmount);
			grabUserMap.put("grabTime",DateUtil.toString(redPacketDetail.getReceiveTime(), DateUtil.Format.DATE_TIME_HYPHEN));
			//只有红包抢完了，才有是否手气最佳的现实，否则都是0
			if (grabedCount.intValue() == packCount.intValue() && grabedCount > 0){
				grabUserMap.put("isBest",redPacketDetail.getIsBest());
			}
			else{
				grabUserMap.put("isBest",EnumStatus.ZORO.getIndex());
			}

			String uuid = publicUser.getUuid();
			//验证用户是否绑定资产账户
			ThirdAppUserBindExt ext = thirdAppUserBindDao.getOneExtByCondition(uuid, platformId, null);
			if (ext == null){
				grabUserMap.put("isBind",EnumStatus.ZORO.getIndex());
			}
			else{
				Integer bindStatus = ext.getBindStatus();
				if (bindStatus != null && bindStatus.intValue() == EnumStatus.ONE.getIndex()) {
					grabUserMap.put("isBind", EnumStatus.ONE.getIndex());
				}
				else{
					grabUserMap.put("isBind", EnumStatus.ZORO.getIndex());
				}
			}
			redpacket.setGrabedUser(grabUserMap);
		}
		else{
			redpacket.setIsGrab((short) EnumStatus.ZORO.getIndex());
		}
		//成功结果返回
		resultData.setStatus(EnumStatus.ZORO.getIndex());
		resultData.setMessage(EnumStatus.ZORO.getName());
		resultData.setData(redpacket);
		return resultData;
	}

	@Override
	public ResultData<RedpacketDto<Map<String,Object>>> handoutDetail(PackBundle bundle,String userId, Integer page, Integer rows,Long timestamp) {
		//业务结果通用返回对象
		ResultData<RedpacketDto<Map<String,Object>>> resultData = new ResultData<>();
		//验证用户信息是否存在
		PublicUser publicUser = userService.getByUserId(userId);
		if (publicUser == null){
			String errMsg = "user not find";
			if(bundle != null) {
				errMsg = bundle.getString("user.not.find");
			}
			resultData.setStatus(EnumStatus.ONE.getIndex());
			resultData.setMessage(errMsg);
			return resultData;
		}
		//接口返回对象
		RedpacketDto<Map<String,Object>> redpacketDto = new RedpacketDto<>();
		redpacketDto.setUserId(publicUser.getId());
		redpacketDto.setUserName(publicUser.getUserName());
		redpacketDto.setAvatarUrl(Misc.getServerUri(null, publicUser.getAvatarUrl()));

		Integer totalCount = 0;
		BigDecimal totalCashAmount = new BigDecimal(0);

		//分页查询红包明细信息
		page = (page == null || page == 0)?1:page;
		rows = (rows == null || rows == 0) ?20:rows;
		int start = (page -1)*rows;
		redpacketDto.setCurrentPage(page);
		PublicRedPacket publicRedPacketParam = new PublicRedPacket();
		publicRedPacketParam.setUserId(userId);
		publicRedPacketParam.setStart(start);
		publicRedPacketParam.setOffset(rows);
		publicRedPacketParam.setRecordTimestamp(timestamp);
		List<PublicRedPacket> redPackets = publicRedPacketDao.listPageHandoutByUserId(publicRedPacketParam);
		if (redPackets != null && redPackets.size() > 0){
			//设置红包明细dto
			List<Map<String,Object>> listMap = new ArrayList<>(redPackets.size());
			for (PublicRedPacket redPacket:redPackets){
				Map<String,Object> resultMap = new HashMap<>();
				String platformId = redPacket.getPlatformId();
				String coinId = redPacket.getCoinId();
				String coinName = null;
				//币信息
				CoinInfo coin = AssetCache.getInstance().getCoin(platformId, coinId);
				if (coin != null){
					coinName = coin.getName();
				}
				else{
					coinName = coinId;
				}
				//红包数量
				Integer packCount = redPacket.getPackCount();
				//已经抢的数量
				Integer grabedCount = redPacket.getGrabedCount() == null?0:redPacket.getGrabedCount();
				Long currentTimes = System.currentTimeMillis();
				Long expireTime = redPacket.getExpireTime();
				//红包状态- 0：可抢红包（调用抢红包接口）
				// 1：已抢红包（调用红包明细查询接口）
				// 2：未抢到红包（提示未抢到信息，点击“查看红包明细”，查看红包信息）
				// 3：过期红包 （直接显示红包过期信息）
				// 4：已失效（用户解绑，目前只考虑到此一种情况）
				Integer packStatus = redPacket.getPackStatus() == null?0:redPacket.getPackStatus();
				resultMap.put("redpacketId",redPacket.getPackId());
				resultMap.put("redpacketType",redPacket.getPackType());
				resultMap.put("redpacketCount",packCount);
				resultMap.put("grabedCount",grabedCount);
				if (packStatus == EnumStatus.FOUR.getIndex()){
					//4：过红包失效
					resultMap.put("redpacketStatus",EnumStatus.FOUR.getIndex());
				}
				else {
					if (currentTimes > expireTime) {
						//3：过期红包
						resultMap.put("redpacketStatus", EnumStatus.THREE.getIndex());
					} else {
						if (grabedCount< packCount) {
							//1：正在抢红包
							resultMap.put("redpacketStatus", EnumStatus.ZORO.getIndex());
						} else if (grabedCount.intValue() == packCount.intValue()) {
							//2：已抢完
							resultMap.put("redpacketStatus", EnumStatus.TWO.getIndex());
						}
					}
				}
				resultMap.put("coinId",coinId);
				resultMap.put("coinName",coinName);
				resultMap.put("amount",redPacket.getAmount());
				resultMap.put("handoutTime",DateUtil.toString(redPacket.getCreateTime(), DateUtil.Format.DATE_TIME_HYPHEN));
				resultMap.put("timestamp",redPacket.getRecordTimestamp());
				listMap.add(resultMap);
			}
			redpacketDto.setPageList(listMap);
		}
		else{
			redpacketDto.setPageList(new ArrayList<>());
		}
		//查询红包被抢的总数量
		PublicRedPacket publicRedPacket = publicRedPacketDao.listPageHandoutTotalByUserId(publicRedPacketParam);
		if (publicRedPacket != null){
			//红包总数量
			totalCount = publicRedPacket.getTotalCount();
			//红包总现金价值
			totalCashAmount = publicRedPacket.getTotalCashAmount().setScale(2,BigDecimal.ROUND_DOWN);
		}
		redpacketDto.setTotalCount(totalCount);
		redpacketDto.setTotalCashAmount(totalCashAmount);
		//成功结果返回
		resultData.setStatus(EnumStatus.ZORO.getIndex());
		resultData.setMessage(EnumStatus.ZORO.getName());
		resultData.setData(redpacketDto);
		return resultData;
	}

	@Override
	public ResultData<RedpacketDto<Map<String,Object>>> receiveDetail(PackBundle bundle,String userId, Integer page, Integer rows,Long timestamp) {
		//业务结果通用返回对象
		ResultData<RedpacketDto<Map<String,Object>>> resultData = new ResultData<>();
		//验证用户信息是否存在
		PublicUser publicUser = userService.getByUserId(userId);
		if (publicUser == null){
			String errMsg = "user not find";
			if(bundle != null) {
				errMsg = bundle.getString("user.not.find");
			}
			resultData.setStatus(EnumStatus.ONE.getIndex());
			resultData.setMessage(errMsg);
			return resultData;
		}
		//接口返回对象
		RedpacketDto<Map<String,Object>> redpacketDto = new RedpacketDto<>();
		//设置当前用户信息
		redpacketDto.setUserId(publicUser.getId());
		redpacketDto.setUserName(publicUser.getUserName());
		redpacketDto.setAvatarUrl(Misc.getServerUri(null, publicUser.getAvatarUrl()));
		redpacketDto.setCurrentPage(page);
		//分页查询红包明细信息
		page = (page == null || page == 0)?1:page;
		rows = (rows == null || rows == 0) ?20:rows;
		int start = (page -1)*rows;
		redpacketDto.setCurrentPage(page);
		PublicRedPacketDetail publicRedPacketDetailParam = new PublicRedPacketDetail();
		publicRedPacketDetailParam.setToUserId(userId);
		publicRedPacketDetailParam.setStart(start);
		publicRedPacketDetailParam.setOffset(rows);
		publicRedPacketDetailParam.setRecordTimestamp(timestamp);
//        publicRedPacketDetailParam.setTransferStatus((short) EnumStatus.ONE.getIndex());
		List<PublicRedPacketDetail> details = publicRedPacketDetailDao.listRedpacketFromUserPage(publicRedPacketDetailParam);
		if (details != null && details.size() > 0){
			//设置红包明细dto
			List<Map<String,Object>> listMap = new ArrayList<>(details.size());
			for (PublicRedPacketDetail detail:details){
				Map<String,Object> resultMap = new HashMap<>();
				resultMap.put("redpacketId",detail.getPackId());
				resultMap.put("redpacketType",detail.getPackType());
				resultMap.put("amount",detail.getAmount());
				resultMap.put("avatarUrl",Misc.getServerUri(null, detail.getAvatarUrl()));
				resultMap.put("receiveTime",DateUtil.toString(detail.getReceiveTime(), DateUtil.Format.DATE_TIME_HYPHEN));
				resultMap.put("isBest",detail.getIsBest());
				resultMap.put("userId",detail.getFromUserId());
				resultMap.put("coinId",detail.getCoinId());
				resultMap.put("userId",detail.getFromUserId());
				resultMap.put("userName",detail.getUserName());
				resultMap.put("timestamp",detail.getRecordTimestamp());
				listMap.add(resultMap);
			}
			redpacketDto.setPageList(listMap);
		}
		else{
			redpacketDto.setPageList(new ArrayList<>());
		}
		//统计查询收到红包现金总额，总数量，总币数
		PublicRedPacketDetail publicRedPacketDetail = publicRedPacketDetailDao.getSumAndCountByToUserId(userId);
		if (publicRedPacketDetail != null){
			BigDecimal grabedCashAmount = publicRedPacketDetail.getGrabedCashAmount()==null?new BigDecimal(0):publicRedPacketDetail.getGrabedCashAmount();
			redpacketDto.setTotalCashAmount(grabedCashAmount.setScale(2, BigDecimal.ROUND_DOWN));
			redpacketDto.setTotalCount(publicRedPacketDetail.getGrabedCount());
		}
		//成功结果返回
		resultData.setStatus(EnumStatus.ZORO.getIndex());
		resultData.setMessage(EnumStatus.ZORO.getName());
		resultData.setData(redpacketDto);
		return resultData;
	}

	/***
	 * 判断红包是否失效
	 * @param redpacketId
	 * @return
	 */
	public boolean isRedpacketInvalid(String redpacketId) {
		PublicRedPacket publicRedPacket = publicRedPacketDao.getOneById(redpacketId);
		if (null == publicRedPacket) {
			throw new RuntimeException("publicRedPacketDao.getOneById("+redpacketId+") is return null");
		}
	    PublicUser  sendUser = publicUserDao.getByUserId(publicRedPacket.getUserId());
	    if (null ==  sendUser) {
	    	throw new RuntimeException("publicUserDao.getByUserId("+publicRedPacket.getUserId()+") is return null");
	    }
		ThirdAppUserBindExt extFrom = thirdAppUserBindDao.getOneExtByCondition(sendUser.getUuid(), publicRedPacket.getPlatformId(), null);
		if (extFrom == null || !extFrom.isBinded()) {
			String errMsg = "转账发起方已经解除交易绑定";
			logger.error("publicRedPacketDao.isRedpacketInvalid|" +errMsg);
			return true;
		}
		return false;
	}
	
	
	public boolean isReceiverUserInvalid(String redpacketId, String receiverUserId) {
		PublicRedPacket publicRedPacket = publicRedPacketDao.getOneById(redpacketId);
		if (null == publicRedPacket) {
			throw new RuntimeException("publicRedPacketDao.getOneById("+redpacketId+") is return null");
		}
	    PublicUser  sendUser = publicUserDao.getByUserId(receiverUserId);
	    if (null ==  sendUser) {
	    	throw new RuntimeException("publicUserDao.getByUserId("+receiverUserId+") is return null");
	    }
		ThirdAppUserBindExt extFrom = thirdAppUserBindDao.getOneExtByCondition(sendUser.getUuid(), publicRedPacket.getPlatformId(), null);
		if (extFrom == null || !extFrom.isBinded()) {
			String errMsg = "转账接收方方已经解除交易绑定";
			logger.error("publicRedPacketDao.isRedpacketInvalid|" +errMsg);
			return true;
		}
		return false;
	}

	/**
	 * 判断这个红包用户是否已经抢过
	 * @param redpacketId
	 * @param userId
	 * @return
	 */
	public boolean isGrabbed(String redpacketId,String userId) {
		if (null == redpacketId || "".equals(redpacketId)) {
			throw new RuntimeException("redpacketId is nulll");
		}
		if (null == userId || "".equals(userId)) {
			throw new RuntimeException("userId is nulll");
		}
		PublicRedPacketDetail publicRedPacketDetail = new PublicRedPacketDetail();
    	publicRedPacketDetail.setPackId(redpacketId);
    	publicRedPacketDetail.setToUserId(userId);
    	logger.info("判断这个红包用户是否已经抢过-------isGrabbed redpacketId:"+redpacketId+"---ToUserId:"+userId);
    	List<PublicRedPacketDetail> list = publicRedPacketDetailDao.list(publicRedPacketDetail);
    	logger.info("判断这个红包用户是否已经抢过-------isGrabbed redpacketId size:"+list.size());
		if (null != list && list.size() > 0) {
			return true;
		}
	   return false;
	}



	/***
	 * 红包是否过期
	 *
	 * @param redpacketId
	 * @return
	 */
	public boolean isRedpacketExpired(String redpacketId) {
		if (null == redpacketId || "".equals(redpacketId)) {
			throw new RuntimeException("isRedpacketExpired  redpacketId  is null");
		}

		PublicRedPacket publicRedPacket = publicRedPacketDao.getOneById(redpacketId);
		if (null == publicRedPacket || null == publicRedPacket.getExpireTime()) {
			logger.error("publicRedPacketDao.getOneById|" + redpacketId + "|does not exist");
			throw new RuntimeException("publicRedPacketDao.getOneById|" + redpacketId + "|does not exist");
		}
		java.util.Date redExpiredDate = new java.util.Date(publicRedPacket.getExpireTime());
             
		try {
			if (null != redExpiredDate) {
				boolean result = DateUtil.isExpired(new java.util.Date(), redExpiredDate);
				if (result) {
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	
	
	@Override
	public DeferredResult<String> getStatus(final PackBundle packBundle, String redpacketId, String userId) {
		final DeferredResult<String> deferred = new DeferredResult<>();
		final ReturnResult<RedpacketStatus> rst = new ReturnResult<RedpacketStatus>();
		final RedpacketStatus rStatusnew =  new RedpacketStatus();
		final RedpacketStatus redpacketStatus = new RedpacketStatus();
		try {
			PublicRedPacket publicRedPacket = publicRedPacketDao.getOneById(redpacketId);
			if (null == publicRedPacket) {
				rst.setData(rStatusnew);
				rst.setStatus(EnumStatus.TWO.getIndex());
				rst.setError(packBundle.getString("redpacket.not.exit"));
				String rjson = JSON.toJSONString(rst);
				logger.info("PublicRedPacketServiceImpl.getStatus:" + rjson);
				deferred.setResult(rjson);
				return deferred;
			}
			if (null == userId || "".equals(userId)) {
				rst.setData(rStatusnew);
				rst.setStatus(EnumStatus.NINETY_EIGHT.getIndex());
				rst.setError(packBundle.getString("redpacket.not.login"));
				String rjson = JSON.toJSONString(rst);
				logger.info("PublicRedPacketServiceImpl.getStatus.error:" + rjson);
				deferred.setResult(rjson);
				return deferred;
			}
			PublicUser sendUser = publicUserDao.getByUserId(publicRedPacket.getUserId());
			PublicUser acceptUser = publicUserDao.getByUserId(userId);
			if (null == sendUser || null == acceptUser) {
				rst.setData(rStatusnew);
				rst.setStatus(EnumStatus.ONE.getIndex());
				rst.setError(packBundle.getString("redpacket.userinfo.exit"));
				String rjson = JSON.toJSONString(rst);
				logger.info("PublicRedPacketServiceImpl.getStatus.error:" + rjson);
				deferred.setResult(rjson);
				return deferred;
			}
//			if (isReceiverUserInvalid(redpacketId, userId)) {
//				rst.setData(rStatusnew);
//				rst.setStatus(EnumStatus.THREE.getIndex());
//				rst.setError(packBundle.getString("redpacket.unbound.account"));
//				deferred.setResult(JSONObject.toJSONString(rst));
//				return deferred;
//			}
		} catch (Exception ex) {
			rst.setData(rStatusnew);
			rst.setStatus(EnumStatus.NINETY_NINE.getIndex());
			rst.setError(packBundle.getString("system.error"));
			String rjson = JSON.toJSONString(rst);
			logger.info("PublicRedPacketServiceImpl.getStatus:" + rjson);
			deferred.setResult(rjson);
			return deferred;
		}
		
		consumer.offerTask(new ITask() {
			@Override
			public void run() {
				String value = null;
				try {
					try {
						value = redisService.pop(redpacketId);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					if (isGrabbed(redpacketId, userId)) {
						/** 红包这个用户已经抢过 */
						redpacketStatus.setRedpacketStatus(EnumStatus.ONE.getIndex());
					} else if (isRedpacketExpired(redpacketId)) {
						/** 红包已经过期 */
						redpacketStatus.setRedpacketStatus(EnumStatus.THREE.getIndex());
						try {
							redisService.evict(redpacketId);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					} else if (isRedpacketInvalid(redpacketId)) {
						/** 发起方解绑，红包失效 */
						redpacketStatus.setRedpacketStatus(EnumStatus.FOUR.getIndex());
					} else if (null == value || "".equals(value)) {
						/** 红包已经抢空 */
						redpacketStatus.setRedpacketStatus(EnumStatus.TWO.getIndex());
					} else {
						/*** 可抢红包 */
						redpacketStatus.setRedpacketStatus(EnumStatus.ZORO.getIndex());
					}

					PublicRedPacket publicRedPacket = publicRedPacketDao.getOneById(redpacketId);
					if (null == publicRedPacket || null == publicRedPacket.getPackId()) {
						rst.setStatus(1);
						logger.error("getStatus publicRedPacketDao.getOneById|" + redpacketId + "|does not exist");
						throw new RuntimeException("getStatus publicRedPacketDao.getOneById|" + redpacketId + "|does not exist");
					} else {
						rst.setStatus(EnumStatus.ZORO.getIndex());
					}
					redpacketStatus.setRedpacketId(redpacketId);
					redpacketStatus.setUserId(publicRedPacket.getUserId());
					redpacketStatus.setRedpacketType(publicRedPacket.getPackType());

					PublicUser publicUser = publicUserDao.getByUserId(publicRedPacket.getUserId());
					if (null == publicUser) {
						throw new RuntimeException("publicUserDao.getByUserId|" + publicRedPacket.getUserId() + "|does not exist");
					}

					redpacketStatus.setUserName(publicUser.getUserName());
					redpacketStatus.setAvatarUrl(Misc.getServerUri(null, publicUser.getAvatarUrl()));
					redpacketStatus.setText(publicRedPacket.getRemark());

					rst.setData(redpacketStatus);
					deferred.setResult(JSONObject.toJSONString(rst));
					if (null != value && !"".equals(value)) {
						redisService.push(redpacketId, value, publicRedPacket.getExpireTime());
					}
				} catch (Exception ex) {
					ex.printStackTrace();
					rst.setData(rStatusnew);
					rst.setStatus(EnumStatus.NINETY_NINE.getIndex());
					String rjson = JSON.toJSONString(rst);
					logger.info("PublicRedPacketServiceImpl.getStatus.error:" + rjson);
					deferred.setResult(rjson);
					if (null != value && !"".equals(value)) {
						redisService.push(redpacketId, value, 7600);
					}
				}
			}
		});
		return deferred;

	}
}
