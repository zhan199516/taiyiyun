package com.taiyiyun.passport.service.transfer;

import com.alibaba.fastjson.JSON;
import com.taiyiyun.passport.consts.EnumStatus;
import com.taiyiyun.passport.dao.IPublicRedPacketDao;
import com.taiyiyun.passport.dao.IPublicRedPacketDetailDao;
import com.taiyiyun.passport.dao.IThirdAppUserBindDao;
import com.taiyiyun.passport.dao.ITradeDao;
import com.taiyiyun.passport.init.SpringContext;
import com.taiyiyun.passport.po.PublicRedPacket;
import com.taiyiyun.passport.po.PublicRedPacketDetail;
import com.taiyiyun.passport.po.ThirdAppUserBindExt;
import com.taiyiyun.passport.po.asset.Trade;
import com.taiyiyun.passport.transfer.Answer.TransferResult;
import com.taiyiyun.passport.transfer.Ask.AskBody;
import com.taiyiyun.passport.transfer.ITransferAccounts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 绑定资产用户未得到红包补发
 */
public class RedpacketRepeatPay {

    private static RedpacketRepeatPay ourInstance = new RedpacketRepeatPay();

    public static RedpacketRepeatPay getInstance() {
        return ourInstance;
    }

    public final Logger logger = LoggerFactory.getLogger(getClass());
    private ScheduledExecutorService executorService; // 定时任务
    private IThirdAppUserBindDao thirdAppUserBindDao;
    private IPublicRedPacketDao publicRedPacketDao;
    private IPublicRedPacketDetailDao publicRedPacketDetailDao;
    private ITradeDao tradeDao;

    private RedpacketRepeatPay() {
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
                repeat();
            }
        }, 1, 30, TimeUnit.MINUTES);
    }

    public void stop(){

    }

    protected void repeat(){
        logger.info("==>开始处理已领红包再次转账业务");
        List<PublicRedPacketDetail> list = publicRedPacketDetailDao.listGrabByTransferStatus();
        for(PublicRedPacketDetail publicRedPacketDetail:list){
            //查询红包明细，解冻状态为：0，转账状态为：0
            String redpacketId = publicRedPacketDetail.getPackId();
            PublicRedPacket publicRedPacket =  publicRedPacketDao.getOneById(redpacketId);
            if (publicRedPacket == null){
                continue;
            }
            //验证用户是否接触绑定
            String toUuid = publicRedPacketDetail.getToUuid();
            String platformId = publicRedPacket.getPlatformId();
            ThirdAppUserBindExt extTo = thirdAppUserBindDao.getOneExtByCondition(toUuid, platformId, null);
            if(extTo == null){
                logger.info("已领红包再次转账：查询不到绑定信息");
                continue;
            }
            if(extTo.getBindStatus() == null || extTo.getBindStatus() != 1){
                logger.info("已领红包再次转账：查询不到绑定信息");
                continue;
            }
            String fromUuid = publicRedPacket.getFromUuid();
            ThirdAppUserBindExt extFrom = thirdAppUserBindDao.getOneExtByCondition(fromUuid, platformId, null);
            if(extFrom == null || !extFrom.isBinded()){
                logger.info("转账发起方已经解除交易绑定");
                continue;
            }
            //验证应用信息
            ITransferAccounts accounts = ThirdAppManager.getInstance().getAccountById(platformId);
            if(accounts == null){
                logger.info("已领红包再次转账：查询不到第三方应用信息");
                continue;
            }
            String relateId = thirdAppUserBindDao.getRelateId(platformId);
            try {
                AskBody body = accounts.askForToken(null, extFrom, relateId);
                if (body == null || body.getToken() == null) {
                    logger.info("已领红包再次转账：获取token失败");
                    continue;
                }
                logger.info("处理红包明细为：" + JSON.toJSONString(publicRedPacketDetail));
                Trade trade = new Trade();
                String coinId = publicRedPacketDetail.getCoinId();
                BigDecimal amount = publicRedPacketDetail.getAmount();
                BigDecimal fee = publicRedPacketDetail.getFee();
                Long freezeTradeId = publicRedPacketDetail.getFreezeTradeId();
                TransferResult transferResult = accounts.askForTransfer(null, body, coinId, amount, freezeTradeId,fee,extTo.getUserKey());
                if (transferResult.isSuccess()) {
                    PublicRedPacketDetail detailParam = new PublicRedPacketDetail();
                    detailParam.setDetailId(publicRedPacketDetail.getDetailId());
                    detailParam.setTransferStatus((short) EnumStatus.ONE.getIndex());
                    detailParam.setTransferTime(new Date());
                    int rval = publicRedPacketDetailDao.updateById(detailParam);
                    if (rval > 0){
                        trade.setStatus(EnumStatus.FOUR.getIndex());
                        trade.setFrozenId(publicRedPacketDetail.getFreezeTradeId());
                        //更细交易记录，如果更新失败，说明没有交易，则插入信息的交易信息
                        int rrval = tradeDao.updateFrozenId(trade);
                        if (rrval <= 0){
                            publicRedPacketDetail.setPlatformId(platformId);
                            publicRedPacketDetail.setFromUserId(publicRedPacket.getUserId());
                            publicRedPacketDetail.setFromUuid(publicRedPacket.getFromUuid());
                            publicRedPacketDetail.setText("红包入账");
                            addTradeInfo(publicRedPacketDetail,transferResult);
                            logger.info("==>已领红包再次转账入账成功-插入新的交易明细成功");
                        }
                        else{
                            logger.info("==>已领红包再次转账入账成功-更新trade状态成功");
                        }
                    }

                } else {
                    Integer errorCode = transferResult.getErrorCode();
                    PublicRedPacketDetail detailParam = new PublicRedPacketDetail();
                    detailParam.setDetailId(publicRedPacketDetail.getDetailId());
                    detailParam.setTransferStatus((short) EnumStatus.THREE.getIndex());
                    detailParam.setUnfreezeStatus((short) EnumStatus.THREE.getIndex());
                    detailParam.setUnfreezeTime(new Date());
                    detailParam.setTransferTime(new Date());
                    //修改两个状态都为3，代表已经被处理过,需要人工手动处理
                    publicRedPacketDetailDao.updateById(detailParam);
                    logger.info("==>已领红包再次转账-转账失败-错误返回码：" + errorCode);
                }
            }
            catch (Exception e){
                logger.info("==>已领红包再次转账异常：" + e.getMessage());
            }
        }
        if (list == null || list.size() == 0){
            logger.info("==>已领红包再次转账：没有满足条件的红包记录");
        }
        logger.info("==>处理已领红包再次转账业务完成");
    }

    /**
     * 红包入账
     * @param publicRedPacketDetail
     */
    public void addTradeInfo(PublicRedPacketDetail publicRedPacketDetail,TransferResult transferResult){
        //记录转账信息
        Trade transferAnswer = new Trade();
        transferAnswer.setAmount(publicRedPacketDetail.getAmount());
 //        transferAnswer.setFromUserId(publicRedPacketDetail.getFromUserId());
        transferAnswer.setFromUuid(publicRedPacketDetail.getFromUuid());
        transferAnswer.setFrozenId(publicRedPacketDetail.getTradeId());
        transferAnswer.setCoinId(publicRedPacketDetail.getCoinId());
        transferAnswer.setFee(publicRedPacketDetail.getFee());
        transferAnswer.setPlatformId(publicRedPacketDetail.getPlatformId());
        transferAnswer.setText(publicRedPacketDetail.getText());
        transferAnswer.setToUserId(publicRedPacketDetail.getToUserId());
        transferAnswer.setToUuid(publicRedPacketDetail.getToUuid());
        transferAnswer.setWorthRmbApply(publicRedPacketDetail.getCashAmount());
        transferAnswer.setCreateTime(new Date().getTime());
        transferAnswer.setAcceptTime(new Date().getTime());
        transferAnswer.setStatus(EnumStatus.FOUR.getIndex());
        transferAnswer.setFromOverBegin(transferResult.getFromoverFront());
        transferAnswer.setFromOverEnd(transferResult.getFromover());
        transferAnswer.setToOverBegin(transferResult.getTooverFront());
        transferAnswer.setToOverEnd(transferResult.getToover());
        int rval = tradeDao.insert(transferAnswer);
        if (rval > 0){
            PublicRedPacketDetail detailParam = new PublicRedPacketDetail();
            detailParam.setTradeId(transferAnswer.getTradeId());
            detailParam.setDetailId(publicRedPacketDetail.getDetailId());
            publicRedPacketDetailDao.updateById(detailParam);
            logger.info("==>补发红包入账完成，记录转账记录成功:" + JSON.toJSONString(transferAnswer));
        }
        else{
            logger.info("==>补发红包入账完成，记录转账记录失败:" + JSON.toJSONString(transferAnswer));
        }
    }
}
