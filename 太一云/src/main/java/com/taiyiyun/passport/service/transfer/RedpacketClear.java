package com.taiyiyun.passport.service.transfer;

import com.alibaba.fastjson.JSON;
import com.taiyiyun.passport.consts.Config;
import com.taiyiyun.passport.consts.EnumStatus;
import com.taiyiyun.passport.dao.IPublicRedPacketDao;
import com.taiyiyun.passport.dao.IPublicRedPacketDetailDao;
import com.taiyiyun.passport.dao.IThirdAppUserBindDao;
import com.taiyiyun.passport.dao.ITradeDao;
import com.taiyiyun.passport.exception.DefinedError;
import com.taiyiyun.passport.init.SpringContext;
import com.taiyiyun.passport.po.PublicRedPacket;
import com.taiyiyun.passport.po.PublicRedPacketDetail;
import com.taiyiyun.passport.po.ThirdAppUserBindExt;
import com.taiyiyun.passport.po.asset.Trade;
import com.taiyiyun.passport.transfer.Answer.FrozenResult;
import com.taiyiyun.passport.transfer.Ask.AskBody;
import com.taiyiyun.passport.transfer.ITransferAccounts;
import com.taiyiyun.passport.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 红包过期清算定时任务
 */
public class RedpacketClear {

    public final Logger logger = LoggerFactory.getLogger(getClass());

    private static RedpacketClear ourInstance = new RedpacketClear();

    public static RedpacketClear getInstance() {
        return ourInstance;
    }

    private ScheduledExecutorService executorService; // 定时任务
    private IThirdAppUserBindDao thirdAppUserBindDao;
    private IPublicRedPacketDao publicRedPacketDao;
    private IPublicRedPacketDetailDao publicRedPacketDetailDao;
    private ITradeDao tradeDao;

    private RedpacketClear() {
        executorService = Executors.newSingleThreadScheduledExecutor();
        tradeDao = SpringContext.getBean(ITradeDao.class);
        thirdAppUserBindDao = SpringContext.getBean(IThirdAppUserBindDao.class);
        publicRedPacketDao = SpringContext.getBean(IPublicRedPacketDao.class);
        publicRedPacketDetailDao = SpringContext.getBean(IPublicRedPacketDetailDao.class);
    }

    public void start(){
        //每30分钟，执行一次
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                clear();
            }
        }, 1, 30, TimeUnit.MINUTES);
    }

    public void stop(){

    }

    protected void clear(){
        //处理过期红包且未被抢的红包
        List<PublicRedPacket> list = publicRedPacketDao.listExpireTime(new Date().getTime());
        for(PublicRedPacket publicRedPacket:list){
            boolean boo = unfrozeNotGrab(publicRedPacket);
            if (!boo){
                try {
                    //更新红包状态为过期状态，设置红包已经过期
                    PublicRedPacket publicRedPacketParam = new PublicRedPacket();
                    publicRedPacketParam.setPackId(publicRedPacket.getPackId());
                    publicRedPacketParam.setClearTime(new Date());
                    publicRedPacketParam.setClearStatus((short) EnumStatus.ZORO.getIndex());
                    publicRedPacketParam.setPackStatus(EnumStatus.TWO.getIndex());
                    publicRedPacketDao.updateById(publicRedPacketParam);
                }
                catch (Exception e){
                    logger.info("==>未领取红包过期设置状态异常：" + e.getMessage());
                }
            }
        }
        if (list == null || list.size() == 0){
            logger.info("==>未领取红包过期清算：没有满足清算条件的红包记录");
        }
        //已领取红包，且未开通共享账户
        PublicRedPacketDetail publicRedPacketDetail = new PublicRedPacketDetail();
        int grabExpire = Config.getInt("grab.redpacket.expire",30);
        publicRedPacketDetail.setReceiveTime(DateUtil.addDay(new Date(),-grabExpire));
        List<String> listPackid = publicRedPacketDetailDao.listGrabByStatus(publicRedPacketDetail);
        for (String redpactId:listPackid){
            unfrozeGrabed(redpactId);
        }
        if (listPackid == null || listPackid.size() == 0){
            logger.info("==>已经领取红包过期清算：没有满足清算条件的红包记录");
        }
    }

    private boolean unfrozeNotGrab(PublicRedPacket publicRedPacket) {
        //查询红包明细，解冻状态为：0，转账状态为：0
        String redpacketId = publicRedPacket.getPackId();
        logger.info("==>未领取的过期红包清算开始");
        try{
            List<PublicRedPacketDetail> details = publicRedPacketDetailDao.listByPackIdAndStatus(redpacketId);
            //type=1,过期清爽  type=2，被抢到的过期清算
            return redpacketExpire(details,publicRedPacket,1);
        } catch(DefinedError definedError){
            logger.info("==>未领取红包过期清算异常：" + definedError.getReadableMsg());
            logger.error(definedError.getMessage());
        }
        catch(Exception e){
            logger.info("==>未领取红包过期清算异常：" + e.getMessage());
        }
        logger.info("==>未领取的过期红包清算结束");
        return false;
    }
    private boolean unfrozeGrabed(String redpacketId) {
        //查询红包明细，解冻状态为：0，转账状态为：0
        PublicRedPacket publicRedPacket = publicRedPacketDao.getOneById(redpacketId);
        logger.info("==>已被抢且用户未绑定资产过期（抢红包日期+30天）红包清算开始");
        try{
            //已抢红包过期时间
            int grabExpire = Config.getInt("grab.redpacket.expire",30);
            PublicRedPacketDetail publicRedPacketDetailParam = new PublicRedPacketDetail();
            publicRedPacketDetailParam.setPackId(redpacketId);
            publicRedPacketDetailParam.setReceiveTime(DateUtil.addDay(new Date(),-grabExpire));
            List<PublicRedPacketDetail> grabDetails = publicRedPacketDetailDao.listGrabByPackIdAndStatus(publicRedPacketDetailParam);
            return redpacketExpire(grabDetails,publicRedPacket,2);
        } catch(Exception e){
            logger.info("==>已被抢红包过期清算异常：" + e.getMessage());
        }
        logger.info("==>已被抢且用户未绑定资产过期（抢红包日期+30天）红包清算结束");
        return false;
    }

    private boolean redpacketExpire(List<PublicRedPacketDetail> details,PublicRedPacket publicRedPacket,int type) throws DefinedError{
        if (details != null && details.size() > 0){
            //查询红包明细，解冻状态为：0，转账状态为：0
            String redpacketId = publicRedPacket.getPackId();
            //验证用户是否接触绑定
            String fromUuid = publicRedPacket.getFromUuid();
            String platformId = publicRedPacket.getPlatformId();
            ThirdAppUserBindExt ext = thirdAppUserBindDao.getOneExtByCondition(fromUuid, platformId, null);
            if(ext == null){
                throw new DefinedError.ThirdNotFoundException("解除冻结资金：查询不到绑定信息", null);
            }
            if(ext.getBindStatus() == null || ext.getBindStatus() != 1){
                throw new DefinedError.ThirdNotFoundException("解除冻结资金：查询不到绑定信息", null);
            }
            //验证应用信息
            ITransferAccounts accounts = ThirdAppManager.getInstance().getAccountById(platformId);
            if(accounts == null){
                throw new DefinedError.ThirdNotFoundException("解除冻结资金：查询不到第三方应用信息", null);
            }
            String relateId = thirdAppUserBindDao.getRelateId(platformId);
            logger.info("==>待清算红包明细个数：" + details.size());
            for (PublicRedPacketDetail detail:details){
                try {
                    AskBody body = accounts.askForToken(null, ext, relateId);
                    if (body == null || body.getToken() == null) {
                        logger.info("红包过期清算：获取token失败");
                        continue;
                    }
                    String coinId = detail.getCoinId();
                    BigDecimal amount = detail.getAmount();
                    Long freezeTradeId = detail.getFreezeTradeId();
                    FrozenResult frozenResult = accounts.askForUnfreeze(null, body, coinId, amount, freezeTradeId);
                    if (frozenResult.isSuccess()) {
                        PublicRedPacketDetail detailParam = new PublicRedPacketDetail();
                        detailParam.setDetailId(detail.getDetailId());
                        detailParam.setUnfreezeStatus((short) EnumStatus.ONE.getIndex());
                        detailParam.setUnfreezeTime(new Date());
                        publicRedPacketDetailDao.updateById(detailParam);
                    } else {
                        Integer errorCode = frozenResult.getErrorCode();
                        PublicRedPacketDetail detailParam = new PublicRedPacketDetail();
                        //若返回状态值为1021，表示该笔冻结已经转账
                        //若返回状态值为1023，表示该笔冻结已经解冻
                        if (errorCode != null && errorCode.intValue() == 1021){
                            detailParam.setDetailId(detail.getDetailId());
                            detailParam.setUnfreezeStatus((short) EnumStatus.TWO.getIndex());
                            detailParam.setUnfreezeTime(new Date());
                        }
                        else if (errorCode != null && errorCode.intValue() == 1023){
                            detailParam.setDetailId(detail.getDetailId());
                            detailParam.setUnfreezeStatus((short) EnumStatus.ONE.getIndex());
                            detailParam.setUnfreezeTime(new Date());
                        }
                        else{
                            //冻结取消异常记录状态为：3
                            detailParam.setDetailId(detail.getDetailId());
                            detailParam.setUnfreezeStatus((short) EnumStatus.THREE.getIndex());
                            detailParam.setUnfreezeTime(new Date());
                        }
                        publicRedPacketDetailDao.updateById(detailParam);
                        logger.info("==>红包过期清算：第三方返回" + frozenResult.getErrorCode().toString());
                    }
                }
                catch (Exception e){
                    logger.info("==>红包过期清算异常：" + e.getMessage());
                }
            }
            //获取未抢红包且已经解冻完成的总额，记录进账交易信息
            PublicRedPacketDetail publicRedPacketDetail = null;
            if (type == 1) {
                publicRedPacketDetail = publicRedPacketDetailDao.getSumByPackIdAndStatus(redpacketId);
            }
            else{
                //已抢红包过期时间
                int grabExpire = Config.getInt("grab.redpacket.expire",30);
                PublicRedPacketDetail publicRedPacketDetailParam = new PublicRedPacketDetail();
                publicRedPacketDetailParam.setPackId(redpacketId);
                publicRedPacketDetailParam.setReceiveTime(DateUtil.addDay(new Date(),-grabExpire));
                publicRedPacketDetail = publicRedPacketDetailDao.getGrabSumByPackIdAndStatus(publicRedPacketDetailParam);
            }
            if (publicRedPacketDetail != null) {
                BigDecimal ungrabedAmount = publicRedPacketDetail.getGrabedAmount();
                if (ungrabedAmount == null){
                    ungrabedAmount = BigDecimal.ZERO;
                }
                BigDecimal grabedCashAmount = publicRedPacketDetail.getGrabedCashAmount();
                if (grabedCashAmount == null){
                    grabedCashAmount = BigDecimal.ZERO;
                }
                BigDecimal feeAmounnt = publicRedPacketDetail.getFeeAmount();
                if (feeAmounnt == null){
                    feeAmounnt = BigDecimal.ZERO;
                }
                BigDecimal amount = ungrabedAmount.add(feeAmounnt);
                logger.info("==>解冻完成且退回的(红包+手续费)总金额与现金价值[红包ID:" + redpacketId + "]："+ amount + "--" + grabedCashAmount);
                //所有未抢红包都解冻完成，设置红包清算和红包状态，并记录进账交易记录
                PublicRedPacket publicRedPacketParam = new PublicRedPacket();
                //如果未抢金额为0，表示红包已经抢光
                //设置清算状态为2：无需清算，否则为1：已清算
                if (ungrabedAmount.compareTo(BigDecimal.ZERO) == 0){
                    publicRedPacketParam.setClearStatus((short) EnumStatus.TWO.getIndex());
                }
                else{
                    publicRedPacketParam.setClearStatus((short) EnumStatus.ONE.getIndex());
                }
                publicRedPacketParam.setPackId(redpacketId);
                publicRedPacketParam.setClearTime(new Date());
                publicRedPacketParam.setPackStatus(EnumStatus.TWO.getIndex());
                publicRedPacketDao.updateById(publicRedPacketParam);
                //只有金额大于0的时候，才记录交易进账信息
                if (ungrabedAmount.compareTo(BigDecimal.ZERO) > 0) {
                    //红包金额+服务费金额，为进账总金额
                    publicRedPacket.setAmount(amount);
                    publicRedPacket.setCashAmount(grabedCashAmount);
                    //设置备注信息为：红包退回
                    publicRedPacket.setRemark("红包退回");
                    //红包退回入账
                    addTradeInfo(publicRedPacket);
                }
                return true;
            }
            return false;
        }
        else {
            if (type == 1){
                logger.info("==>暂无可以清算的过期红包明细信息.");
            }
            else{
                logger.info("==>暂无可以清算的已被抢过期红包明细信息.");
            }
            return false;
        }
    }

    /**
     * 红包退回入账
     * @param publicRedPacket
     */
    public void addTradeInfo(PublicRedPacket publicRedPacket){
        //记录转账信息
        Trade transferAnswer = new Trade();
        transferAnswer.setAmount(publicRedPacket.getAmount());
        transferAnswer.setFromUserId(publicRedPacket.getUserId());
        transferAnswer.setFromUuid(publicRedPacket.getFromUuid());
        transferAnswer.setFrozenId(publicRedPacket.getTradeId());
        transferAnswer.setCoinId(publicRedPacket.getCoinId());
        transferAnswer.setFee(publicRedPacket.getFee());
        transferAnswer.setPlatformId(publicRedPacket.getPlatformId());
        transferAnswer.setText(publicRedPacket.getRemark());
        transferAnswer.setToUserId(publicRedPacket.getUserId());
        transferAnswer.setToUuid(publicRedPacket.getFromUuid());
        transferAnswer.setWorthRmbApply(publicRedPacket.getCashAmount());
        transferAnswer.setCreateTime(new Date().getTime());
        transferAnswer.setAcceptTime(new Date().getTime());
        transferAnswer.setStatus(EnumStatus.FOUR.getIndex());
        int rval = tradeDao.insert(transferAnswer);
        if (rval > 0){
            logger.info("==>解冻完成，记录转账记录成功:" + JSON.toJSONString(transferAnswer));
        }
        else{
            logger.info("==>解冻完成，记录转账记录失败:" + JSON.toJSONString(transferAnswer));
        }
    }
}
