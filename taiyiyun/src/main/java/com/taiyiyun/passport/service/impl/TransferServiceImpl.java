package com.taiyiyun.passport.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.taiyiyun.passport.commons.ResultData;
import com.taiyiyun.passport.consts.Config;
import com.taiyiyun.passport.consts.EnumStatus;
import com.taiyiyun.passport.dao.*;
import com.taiyiyun.passport.dao.group.IGroupPromoteDetailDao;
import com.taiyiyun.passport.exception.DefinedError;
import com.taiyiyun.passport.language.PackBundle;
import com.taiyiyun.passport.mqtt.Message;
import com.taiyiyun.passport.mqtt.MessagePublisher;
import com.taiyiyun.passport.po.BaseResult;
import com.taiyiyun.passport.po.PublicRedPacketDetail;
import com.taiyiyun.passport.po.PublicUser;
import com.taiyiyun.passport.po.ThirdAppUserBindExt;
import com.taiyiyun.passport.po.asset.Trade;
import com.taiyiyun.passport.po.asset.TransferAccept;
import com.taiyiyun.passport.po.asset.TransferAnswer;
import com.taiyiyun.passport.po.group.GroupPromoteDetail;
import com.taiyiyun.passport.service.IPublicUserFollowerService;
import com.taiyiyun.passport.service.IThirdAppUserBindService;
import com.taiyiyun.passport.service.ITransferService;
import com.taiyiyun.passport.service.queue.Consumer;
import com.taiyiyun.passport.service.queue.ITask;
import com.taiyiyun.passport.service.transfer.AssetCache;
import com.taiyiyun.passport.service.transfer.ThirdAppManager;
import com.taiyiyun.passport.transfer.Answer.AskFeeResult;
import com.taiyiyun.passport.transfer.Answer.CoinInfo;
import com.taiyiyun.passport.transfer.Answer.FrozenResult;
import com.taiyiyun.passport.transfer.Answer.TransferResult;
import com.taiyiyun.passport.transfer.Ask.AskBody;
import com.taiyiyun.passport.transfer.ITransferAccounts;
import com.taiyiyun.passport.util.DateUtil;
import com.taiyiyun.passport.util.NumberUtil;
import com.taiyiyun.passport.util.SMSHelper;
import com.taiyiyun.passport.util.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by okdos on 2017/7/7.
 */
@Service
public class TransferServiceImpl implements ITransferService {

    public final Logger logger = LoggerFactory.getLogger(getClass());

    static final Consumer consumer = new Consumer();
    static final int min =Runtime.getRuntime().availableProcessors()*20;
    static final int max =Runtime.getRuntime().availableProcessors()*50;
    static ExecutorService executorService=new ThreadPoolExecutor(min, max, 2L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

    @Resource
    ITradeDao tradeDao;

    @Resource
    IThirdAppDao thirdAppDao;

    @Resource
    IThirdAppUserBindDao thirdAppUserBindDao;

    @Resource
    IThirdAppUserBindService thirdAppUserBindService;

    @Resource
    private IPublicUserDao publicUserDao;

    @Resource
    private IPublicRedPacketDetailDao publicRedPacketDetailDao;

    @Resource
    private IGroupPromoteDetailDao groupPromoteDetailDao;


    @Resource
    private IPublicUserFollowerService publicUserFollowerService;

    @Override
    public DeferredResult<String> postMoney(final PackBundle packBundle, final TransferAnswer apply) {

        final TransferServiceImpl me = this;

        final DeferredResult<String> deferred = new DeferredResult<>();

        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try{
                    String rst = me.doPostMoney(packBundle, apply);
                    deferred.setResult(rst);
                }
                catch(DefinedError.ThirdUserKeyInvalid ex){
                    BaseResult<Object> rst = new BaseResult<>();
                    rst.setStatus(2);
                    rst.setError(ex.getReadableMsg());
                    rst.setInternalError(ex.getMessage());
                    String rjson = JSON.toJSONString(rst);
                    logger.info("TransferServiceImpl.doPostMoney:" + rjson);
                    deferred.setResult(rjson);
                    thirdAppUserBindDao.updateUnBinding(apply.getFromUuid(), apply.getPlatformId());
                } catch(DefinedError ex){
                    BaseResult<Object> rst = new BaseResult<>();
                    rst.setStatus(2);
                    rst.setError(ex.getReadableMsg());
                    rst.setInternalError(ex.getMessage());
                    String rjson = JSON.toJSONString(rst);
                    logger.info("TransferServiceImpl.doPostMoney:" + rjson);
                    deferred.setResult(rjson);
                }
                catch(Exception ex){
                    BaseResult<Object> rst = new BaseResult<>();
                    rst.setStatus(3);
                    String errMsg = "执行失败";
                    if(packBundle != null) {
                        errMsg = packBundle.getString("failed.execute");
                    }
                    rst.setError(errMsg);
                    rst.setInternalError(ex.getMessage());
                    String rjson = JSON.toJSONString(rst);
                    logger.info("TransferServiceImpl.doPostMoney:" + rjson);
                    deferred.setResult(rjson);
                }
            }
        });
        return deferred;
    }

    /**
     * 转账冻结
     *
     * @param packBundle
     * @param apply
     * @return
     */
    @Override
    public String transferFrozen(PackBundle packBundle, TransferAnswer apply) throws DefinedError {
        return this.doPostMoney(packBundle,apply);
    }


    private String doPostMoney(PackBundle packBundle, TransferAnswer apply) throws DefinedError {

        //repeatToken不为空执行下面验证逻辑，保证老版本系统调用接口是无异常
        String repeatToken = apply.getRepeatToken();
        if (!StringUtil.isEmpty(repeatToken)){
            //查询验证是否有重复发送的红包
            Trade trade = new Trade();
            trade.setRepeatToken(repeatToken);
            trade.setFromUserId(apply.getFromUserId());
            List<Trade> listRedpacket = tradeDao.listByToken(trade);
            if (listRedpacket != null && listRedpacket.size() > 0){
                String errMsg = "can not repeat transfer";
                if(packBundle != null) {
                    errMsg = packBundle.getString("transfer.postmoney.not.repeat");
                }
                BaseResult<List<Message<Object>>> rst = new BaseResult<>();
                rst.setStatus(EnumStatus.SIX.getIndex());
                rst.setError(errMsg);
                return JSON.toJSONString(rst);
            }
        }

        if(apply.getPlatformId() == null ||
                apply.getCoinId() == null ||
                apply.getAmount() == null ||
                apply.getToUserId() == null){
            //throw new DefinedError.ParameterException("coinId, amount, fee, toUserId 不能为空", null);
            String errMsg = "coinId, amount, toUserId 不能为空";
            if(packBundle != null) {
                errMsg = packBundle.getString("need.param", "coinId, amount, fee, toUserId");
            }
            throw new DefinedError.ParameterException(errMsg, null);
        }

        if(apply.getAmount().compareTo(new BigDecimal("0.000001")) < 0){
            //throw new DefinedError.ParameterException("交易币量不能小于0.000001", null);
            String errMsg = "交易币量不能小于0.000001";
            if(packBundle != null) {
                errMsg = packBundle.getString("transfer.postmoney.check_coinamount");
            }
            throw new DefinedError.ParameterException(errMsg, null);
        }

        String uuid = apply.getFromUuid();
        String platformId = apply.getPlatformId();
        String coinId = apply.getCoinId();

        ThirdAppUserBindExt ext = thirdAppUserBindDao.getOneExtByCondition(uuid, platformId, null);
        if(ext == null){
            //throw new DefinedError.ThirdNotFoundException("查询不到绑定信息", null);
            String errMsg = "查询不到绑定信息";
            if(packBundle != null) {
                errMsg = packBundle.getString("transfer.postmoney.nobindinfo");
            }
            throw new DefinedError.ThirdNotFoundException(errMsg, null);
        }

        ext.setUuid(uuid);
        apply.setPlatformName(ext.getAppName());

        CoinInfo coin = AssetCache.getInstance().getCoin(platformId, coinId);

        if(coin != null){
            apply.setCoinName(coin.getDisplay());
            apply.setWorthRmbApply(coin.getCoinprice());
        }

        ITransferAccounts accounts = ThirdAppManager.getInstance().getAccountById(platformId);
        if(accounts == null){
            //throw new DefinedError.ThirdNotFoundException("查询不到第三方应用信息", null);
            String errMsg = "查询不到第三方应用信息";
            if(packBundle != null) {
                errMsg = packBundle.getString("transfer.postmoney.nothirdinfo");
            }
            throw new DefinedError.ThirdNotFoundException(errMsg, null);
        }

        String relateId = thirdAppUserBindDao.getRelateId(platformId);
        BigDecimal fee = apply.getFee();
        //如果手续费金额为空或者为0，代表页面没有获取手续费，并将值传到后台
        //需要自己获取，此场景在进推广群获取币奖励转账业务下发生
        if (fee == null || fee.compareTo(BigDecimal.ZERO) == 0){
            AskBody body = accounts.askForToken(packBundle, ext, relateId);
            if(body.getToken() == null){
                //throw new DefinedError.ThirdNotFoundException("获取token失败", null);
                String errMsg = "获取token失败";
                if(packBundle != null) {
                    errMsg = packBundle.getString("yuanbao.token.failed");
                }
                throw new DefinedError.ThirdNotFoundException(errMsg, null);
            }
            BigDecimal amount = apply.getAmount();
            AskFeeResult askFeeResult = accounts.askForFee(packBundle,body, coinId, amount);
            if(!askFeeResult.isSuccess()){
                String errMsg = "get fee fail";
                if(packBundle != null) {
                    errMsg = packBundle.getString("redpacket.fee.error");
                }
                throw new DefinedError.ThirdNotFoundException(errMsg, null);
            }
            fee = askFeeResult.getTotalFee();
            apply.setFee(fee);
        }

//        AskFeeResult askFeeResult = accounts.askForFee(body, apply.getCoinId(), apply.getAmount());
//
//        if(!askFeeResult.isSuccess()){
//            throw new DefinedError.ThirdErrorException("获取手续费失败", askFeeResult.getErrorCode().toString());
//        }
//
//        //todo 计算手续费一致性
//
//        if(apply.getFee().compareTo(askFeeResult.getTotalFee()) != 0){
//            throw new DefinedError.ThirdRefuseException("手续费不符", askFeeResult.getErrorCode().toString());
//        }
        AskBody body = accounts.askForToken(packBundle, ext, relateId);
        if(body.getToken() == null){
            //throw new DefinedError.ThirdNotFoundException("获取token失败", null);
            String errMsg = "获取token失败";
            if(packBundle != null) {
                errMsg = packBundle.getString("yuanbao.token.failed");
            }
            throw new DefinedError.ThirdNotFoundException(errMsg, null);
        }
        BigDecimal total = apply.getAmount();
        FrozenResult frozenResult = accounts.askForFroze(packBundle, body, apply.getCoinId(), total);
        if(frozenResult.isSuccess()){
            apply.setFrozenId(frozenResult.getFroId()); //设置冻结id
            tradeDao.saveTransApply(apply);
            BaseResult<List<Message<Object>>> rst = new BaseResult<>();
            rst.setStatus(0);
            Message<Object> message = createApplyMessage(apply);
            rst.setData(Arrays.asList(message));
            //MessagePublisher.getInstance().addPublish(Message.UPLINK_MESSAGE + message.getFromUserId(), message);
            try {
                com.taiyiyun.passport.mqtt.v2.MessagePublisher.getInstance().publish(Message.UPLINK_MESSAGE + message.getFromUserId(), message);
            } catch (Exception e) {
                e.printStackTrace();
            }
            SMSHelper.sendTransMoneyInfo(apply);
            return JSON.toJSONString(rst);
        } else {
            //throw new DefinedError.ThirdNotFoundException("转账申请提交失败", frozenResult.getErrorCode().toString());
            String errMsg = "转账申请提交失败";
            if(packBundle != null) {
                errMsg = packBundle.getString("transfer.postmoney.commitapplyfailed");
            }
            throw new DefinedError.ThirdNotFoundException(errMsg, frozenResult.getErrorCode().toString());
        }
    }

    private String doAcceptMoney(PackBundle packBundle, TransferAccept accept,boolean isSendMqttMsg) throws DefinedError{

        String tradeNo = accept.getTradeNo();
        if(!NumberUtil.isInteger(tradeNo)){
            //throw new DefinedError.ParameterException("tradeNo无效", null);
            String errMsg = "tradeNo无效";
            if(packBundle != null) {
                errMsg = packBundle.getString("need.param.invalid", "tradeNo");
            }
            throw new DefinedError.ParameterException(errMsg, null);
        }

        Long tradId = NumberUtil.parseLong(tradeNo, 0L);
        Trade trade = tradeDao.getByTradeId(tradId);
        if(trade == null){
            //throw new DefinedError.ParameterException("tradeNo无效", null);
            String errMsg = "tradeNo无效";
            if(packBundle != null) {
                errMsg = packBundle.getString("need.param.invalid", "tradeNo");
            }
            throw new DefinedError.ParameterException(errMsg, null);
        }

        //没有toUuid的时候，更新toUuid
        if(StringUtil.isEmptyOrBlank(trade.getToUuid())){
            if(!accept.getToUserId().equals(trade.getToUserId())){
                //throw new DefinedError.UnauthorizedException("选定交易记录接收方与当前用户不一致", null);
                String errMsg = "选定交易记录接收方与当前用户不一致";
                if(packBundle != null) {
                    errMsg = packBundle.getString("transfer.doacceptmoney.unauthorized.receivernotuser");
                }
                throw new DefinedError.UnauthorizedException(errMsg, null);
            }
            trade.setToUuid(accept.getToUuid());
            //更新toUuid
            tradeDao.updateUuid(trade.getTradeId(), trade.getToUuid());
        } else {
            if(!accept.getToUuid().equals(trade.getToUuid())){
                //throw new DefinedError.UnauthorizedException("选定交易记录接收方与当前用户不一致", null);
                String errMsg = "选定交易记录接收方与当前用户不一致";
                if(packBundle != null) {
                    errMsg = packBundle.getString("transfer.doacceptmoney.unauthorized.receivernotuser");
                }
                throw new DefinedError.UnauthorizedException(errMsg, null);
            }
        }

        if(trade.getStatus() == 2 || trade.getExpireTime() < (new Date()).getTime()){
            //throw new DefinedError.ExpireException("转账交易过期", null);
            String errMsg = "转账交易过期";
            if(packBundle != null) {
                errMsg = packBundle.getString("transfer.doacceptmoney.expire.transferaccountexpired");
            }
            throw new DefinedError.ExpireException(errMsg, null);
        }

        if(trade.getStatus() != 0){
            //throw new DefinedError.ExpireException("转账交易已经完成/过期/失败", null);
            String errMsg = "转账交易已经完成/过期/失败";
            if(packBundle != null) {
                errMsg = packBundle.getString("transfer.doacceptmoney.expire.transdoneexpirefail");
            }
            throw new DefinedError.ExpireException(errMsg, null);
        }

        ThirdAppUserBindExt extTo = thirdAppUserBindDao.getOneExtByCondition(trade.getToUuid(), trade.getPlatformId(), null);
        if(extTo == null || !extTo.isBinded()){
            //throw new DefinedError.ConditionException("接收方未绑定指定货币", null);
            String errMsg = "接收方未绑定指定货币";
            if(packBundle != null) {
                errMsg = packBundle.getString("transfer.doacceptmoney.condition.receivernobindmoney");
            }
            throw new DefinedError.ConditionException(errMsg, null);
        }

        ThirdAppUserBindExt extFrom = thirdAppUserBindDao.getOneExtByCondition(trade.getFromUuid(), trade.getPlatformId(), null);
        if(extFrom == null || !extFrom.isBinded()){
            //throw new DefinedError.ConditionException("转账发起方已经解除交易绑定", null);
            String errMsg = "转账发起方已经解除交易绑定";
            if(packBundle != null) {
                errMsg = packBundle.getString("transfer.acceptmoney.fail.transoriginremovedtransbind");
            }
            throw new DefinedError.ConditionException(errMsg, null);
        }

        ITransferAccounts accounts = ThirdAppManager.getInstance().getAccountById(trade.getPlatformId());
        if(accounts == null){
            //throw new DefinedError.ConditionException("查询不到第三方应用信息", null);
            String errMsg = "查询不到第三方应用信息";
            if(packBundle != null) {
                errMsg = packBundle.getString("transfer.postmoney.nothirdinfo");
            }
            throw new DefinedError.ConditionException(errMsg, null);
        }

        String relateId = thirdAppUserBindDao.getRelateId(trade.getPlatformId());
        AskBody body = accounts.askForToken(packBundle, extFrom, relateId);
        if(body.getToken() == null){
            //throw new DefinedError.ThirdRefuseException("获取token失败", null);
            String errMsg = "获取token失败";
            if(packBundle != null) {
                errMsg = packBundle.getString("yuanbao.token.failed");
            }
            throw new DefinedError.ThirdRefuseException(errMsg, null);
        }

        CoinInfo coin = AssetCache.getInstance().getCoin(trade.getPlatformId(), trade.getCoinId());
        BigDecimal price = null;
        if(coin != null){
            trade.setCoinName(coin.getDisplay());
            price = coin.getCoinprice();
            trade.setWorthRmbAccept(price);
        }

        BigDecimal total = trade.getAmount();
        TransferResult transferResult =accounts.askForTransfer(packBundle, body, trade.getCoinId(), total, trade.getFrozenId(), trade.getFee(), extTo.getUserKey());
        if(transferResult.isSuccess()){
            trade.setFromOverBegin(transferResult.getFromoverFront());
            trade.setFromOverEnd(transferResult.getFromover());
            trade.setToOverBegin(transferResult.getTooverFront());
            trade.setToOverEnd(transferResult.getToover());
            trade.setAcceptTime(accept.getAcceptTime());
            trade.setStatus(4);
            if(transferResult.getFromoverFront().subtract(transferResult.getFromover()).subtract(trade.getFee()).compareTo(total) != 0 ||
                    transferResult.getToover().subtract(transferResult.getTooverFront()).compareTo(total) != 0){
                //trade.setError("交易不正确：转账发起者转账接收者金额处理不符");
                String errMsg = "交易不正确：转账发起者转账接收者金额处理不符";
                if(packBundle != null) {
                    errMsg = packBundle.getString("transfer.doacceptmoney.error.transfail");
                }
                trade.setError(errMsg);
            }
            Long acceptTime = new Date().getTime();
            tradeDao.acceptApply(tradId, 4, acceptTime, null,
                    transferResult.getFromoverFront(), transferResult.getFromover(), transferResult.getTooverFront(), transferResult.getToover(),
                    price
                    );
            BaseResult<List<Message<Object>>> rst = new BaseResult<>();
            rst.setStatus(0);
            //rst.setError("转账接收成功");
            String errMsg = "转账接收成功";
            if(packBundle != null) {
                errMsg = packBundle.getString("transfer.doacceptmoney.success.transsuccess");
            }
            rst.setError(errMsg);
            Message<Object> message = createAcceptMessage(trade);
            rst.setData(Arrays.asList(message));
            //是否发送mqtt消息，未绑定资产用户抢到红包，完成转账后，不必在发送mqtt消息
            if (isSendMqttMsg) {
                //MessagePublisher.getInstance().addPublish(Message.UPLINK_MESSAGE + message.getFromUserId(), message);
                try {
                    com.taiyiyun.passport.mqtt.v2.MessagePublisher.getInstance().publish(Message.UPLINK_MESSAGE + message.getFromUserId(), message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //自动关注公众号，允许异常
            try{
                publicUserFollowerService.focusPublicUser(trade.getToUserId(), trade.getFromUserId());
            } catch(Exception ex){
                ex.printStackTrace();
                logger.info("focusPublicUser.error:" + ex.getMessage());
            }
            return JSON.toJSONString(rst);

        } else {
            //throw new DefinedError.ThirdRefuseException("转账失败", transferResult.getErrorCode().toString());
            String errMsg = "转账失败";
            if(packBundle != null) {
                errMsg = packBundle.getString("transfer.doacceptmoney.fail.transfailed");
            }
            throw new DefinedError.ThirdRefuseException(errMsg, transferResult.getErrorCode().toString());
        }
    }


    private Message<Object> createApplyMessage(TransferAnswer answer){
        Message<Object> message = new Message<>();
        message.setVersion(1);
        message.setMessageType(2);
        message.setFromUserId(answer.getFromUserId());
        message.setFromClientId(Config.get("mqtt.clientId"));
        message.setMessageId("PostMoney-" + answer.getTradeNo());
        message.setSessionType(1);
        message.setSessionId(answer.getToUserId());
        message.setPublishTime(answer.getCreateTime());
        message.setUpdateTime(answer.getCreateTime());
        message.setContentType(211);
        message.setContent(answer);
        return message;
    }

    private Message<Object> createAcceptMessage(Trade trade){
        Message<Object> message = new Message<>();
        message.setVersion(1);
        message.setMessageType(2);
        message.setFromUserId(trade.getToUserId());
        message.setFromClientId(Config.get("mqtt.clientId"));
        message.setMessageId("ApplyMoney-" + trade.getTradeNo());
        message.setSessionType(1);
        message.setSessionId(trade.getFromUserId());
        message.setPublishTime(trade.getCreateTime());
        message.setUpdateTime(trade.getAcceptTime());
        message.setContentType(212);
        message.setContent(trade);
        return message;
    }


    @Override
    public DeferredResult<String> acceptMoney(final PackBundle packBundle, final TransferAccept accept) {
        final TransferServiceImpl me = this;
        final DeferredResult<String> deferred = new DeferredResult<>();
        consumer.offerTask(new ITask() {
            @Override
            public void run() {
                try{
                    String rst = me.doAcceptMoney(packBundle, accept,true);
                    deferred.setResult(rst);
                }
                catch(DefinedError.ThirdUserKeyInvalid ex){
                    ex.printStackTrace();
                    BaseResult<Object> rst = new BaseResult<>();
                    rst.setStatus(2);
                    //rst.setError("转账发起方已经解除了绑定");
                    String errMsg = "转账发起方已经解除了绑定";
                    if(packBundle != null) {
                        errMsg = packBundle.getString("transfer.acceptmoney.fail.transoriginremovedtransbind");
                    }
                    rst.setError(errMsg);
                    rst.setInternalError(ex.getMessage());
                    String rjson = JSON.toJSONString(rst);
                    logger.info("TransferServiceImpl.doAcceptMoney:" + rjson);
                    deferred.setResult(rjson);
                }
                catch(DefinedError ex){
                    ex.printStackTrace();
                    BaseResult<Object> rst = new BaseResult<>();
                    rst.setStatus(2);
                    rst.setError(ex.getReadableMsg());
                    rst.setInternalError(ex.getMessage());
                    String rjson = JSON.toJSONString(rst);
                    logger.info("TransferServiceImpl.doAcceptMoney:" + rjson);
                    deferred.setResult(rjson);
                } catch(Exception ex){
                    ex.printStackTrace();
                    BaseResult<Object> rst = new BaseResult<>();
                    rst.setStatus(3);
                    //rst.setError("执行失败");
                    String errMsg = "执行失败";
                    if(packBundle != null) {
                        errMsg = packBundle.getString("failed.execute");
                    }
                    rst.setError(errMsg);
                    rst.setInternalError(ex.getMessage());
                    String rjson = JSON.toJSONString(rst);
                    logger.info("TransferServiceImpl.doAcceptMoney:" + rjson);
                    deferred.setResult(rjson);
                }
            }
        });
        return deferred;
    }

    private void setStatus(Trade trade, String viewUuid, String viewUserId){
        if(trade.getStatus() == 0){
            if(trade.getExpireTime() < new Date().getTime()){
                trade.setStatus(2);
            } else {
                //接收方需要知道是否要绑定了第三方交易账号
                if(viewUserId.equals(trade.getToUserId())){
                    ThirdAppUserBindExt thirdAppUserBindExt = thirdAppUserBindDao.getOneExtByCondition(viewUuid, trade.getPlatformId(), null);
                    if(thirdAppUserBindExt != null && thirdAppUserBindExt.isBinded()){
                        trade.setStatus(1);
                    }
                } else {
                    trade.setStatus(1);
                }
            }
        }
    }

    @Override
    public List<Trade> getTradeHistory(PackBundle packBundle,Long start, Long end, Integer ioType, String platformId, String coinId, Integer line, String viewUserId, String viewUuid){

        if(start != null && start == 0){
            start = null;
        }
        if(end != null && end == 0){
            end = null;
        }
        //发红包中文名称
        String redpHandoutName = null;
        //发红包中文名称
        String redpGrabName = null;
        //红包退回中文名称
        String redpReturnName = null;
        try{
            redpHandoutName = packBundle.getString("redpacket.handout.name");
            redpGrabName = packBundle.getString("redpacket.grab.name");
            redpReturnName = packBundle.getString("redpacket.return.name");
            if (StringUtil.isEmpty(redpHandoutName)){
                redpHandoutName = "发红包";
            }
            if (StringUtil.isEmpty(redpGrabName)){
                redpGrabName = "抢红包";
            }
            if (StringUtil.isEmpty(redpReturnName)){
                redpReturnName = "红包退回";
            }
        }
        catch(Exception e){
            System.out.println("get redpacket info error:"+ e.getMessage());
        }
        List<Trade> tradeList = tradeDao.getHistory(viewUserId, ioType, start, end, platformId, coinId, line);
        for(int i = 0; i < tradeList.size(); i++){
            Trade trade = tradeList.get(i);
            if(trade.getStatus() == 0){
                if(trade.getExpireTime() < new Date().getTime()){
                    trade.setStatus(2);
                } else {
                    trade.setStatus(1);
                }
            }
            //发红包时，toUserId为空，toUserName默认值为：发红包
            String toUserId = trade.getToUserId();
            if (StringUtil.isEmpty(toUserId)){
                trade.setToUserName(redpHandoutName);
            }
            //抢红包时，fromUserId为空，fromUserName默认为：抢红包
            String fromUserId = trade.getFromUserId();
            if (StringUtil.isEmpty(fromUserId)){
                trade.setFromUserName(redpGrabName);
            }
            //红包退回时，fromUserId与toUserId为不为空且相等，fromUserName默认为：红包退回
            if (!StringUtil.isEmpty(toUserId)
                    && !StringUtil.isEmpty(fromUserId)
                    && fromUserId.equals(toUserId)){
                trade.setFromUserName(redpReturnName);
            }
            String fromUserName =  trade.getFromUserName();
            CoinInfo coin = AssetCache.getInstance().getCoin(trade.getPlatformId(), trade.getCoinId());
            if(coin != null){
                trade.setCoinName(coin.getDisplay());
                trade.setCoinLogo(coin.getLogo());
            } else {
                trade.setCoinName("");
            }
        }
        return tradeList;
    }


    @Override
    public Trade getTradeByTradeNo(String tradeNo, String viewUuid, String viewUserId){
        Long tradeId = NumberUtil.parseLong(tradeNo, 0L);
        if(tradeId == null || tradeId == 0){
            return null;
        }
        Trade trade = tradeDao.getByTradeId(tradeId);
        if(trade == null){
            return null;
        }
        //只有用户自己和接收方可以查看状态
        if(!viewUserId.equals(trade.getToUserId()) && !viewUserId.equals(trade.getFromUserId())){
            return null;
        }
        CoinInfo coin = AssetCache.getInstance().getCoin(trade.getPlatformId(), trade.getCoinId());
        if(coin != null){
            trade.setCoinName(coin.getDisplay());
            trade.setCoinLogo(coin.getLogo());
        }
        setStatus(trade, viewUuid, viewUserId);
        return trade;
    }


    /**
     * 用户首次绑定资产平台后，将入群奖励和红包资产转入到对应的资产平台下
     *
     * @param packBundle
     * @param uuid
     * @return
     */
    @Override
    public ResultData<Integer> prizeTransferAccount(PackBundle packBundle, String uuid) {
        //根据uuid查询用户信息
        List<PublicUser> publicUsers = publicUserDao.getByUuid(uuid);
        logger.info("prizeTransferAccount:get user info:" + JSON.toJSONString(publicUsers));
        //目前情况uuid是唯一标识查询出来一个结果
        for (PublicUser publicUser:publicUsers){
            String text = packBundle.getString("redpacket.grab.income");
            if (StringUtil.isEmpty(text)){
                text = "红包入账";
            }
            //已抢红包过期时间
            int grabExpire = Config.getInt("grab.redpacket.expire",30);
            String userId = publicUser.getId();
            //根据userid查询未过期且未确认收账和未解冻的红包
            PublicRedPacketDetail publicRedPacketDetail = new PublicRedPacketDetail();
            publicRedPacketDetail.setToUserId(userId);
            publicRedPacketDetail.setReceiveTime(DateUtil.addDay(new Date(),-grabExpire));
            publicRedPacketDetail.setTransferStatus((short) EnumStatus.ZORO.getIndex());
            publicRedPacketDetail.setUnfreezeStatus((short) EnumStatus.ZORO.getIndex());
            logger.info("prizeTransferAccount:get user redpacket info param:" + JSON.toJSONString(publicRedPacketDetail));
            List<PublicRedPacketDetail> publicRedPacketDetails = publicRedPacketDetailDao.listGrabedRedpacket(publicRedPacketDetail);
            logger.info("获取需要自动转账的已领取红包列表信息[publicRedPacketDetails]：" + publicRedPacketDetails.size());
            for(PublicRedPacketDetail detail:publicRedPacketDetails){
                //插入红包进账交易记录
                Trade transferAnswer = new Trade();
                try {
                    transferAnswer.setText(text);
                    transferAnswer.setFee(detail.getFee());
                    transferAnswer.setAmount(detail.getAmount());
                    transferAnswer.setFromUserId(detail.getFromUserId());
                    transferAnswer.setFromUuid(detail.getFromUuid());
                    transferAnswer.setToUuid(uuid);
                    transferAnswer.setToUserId(userId);
                    transferAnswer.setCoinId(detail.getCoinId());
                    transferAnswer.setPlatformId(detail.getPlatformId());
                    transferAnswer.setFrozenId(detail.getFreezeTradeId());
                    Long createTime = null;
                    Date date = detail.getCreateTime();
                    if (date != null){
                        createTime = date.getTime();
                    }
                    else{
                        createTime = new Date().getTime();
                    }
                    transferAnswer.setCreateTime(createTime);
                    transferAnswer.setWorthRmbApply(detail.getCashAmount());
                    transferAnswer.setExpireTime(detail.getExpireTime() == null?new Date().getTime():detail.getExpireTime());
                    transferAnswer.setStatus(EnumStatus.ZORO.getIndex());
                    //预先设置接收时间，转账成功后会更新时间
                    transferAnswer.setAcceptTime(new Date().getTime());
                    String result = null;
                    logger.info("prizeTransferAccount-saveTransApply param:" + JSON.toJSONString(transferAnswer));
                    int rval = tradeDao.insert(transferAnswer);
                    if (rval > 0){
                        TransferAccept transferAccept = new TransferAccept();
                        transferAccept.setToUuid(uuid);
                        transferAccept.setToUserId(userId);
                        transferAccept.setAcceptTime(new Date().getTime());
                        transferAccept.setTradeNo(String.valueOf(transferAnswer.getTradeId()));
                        logger.info("prizeTransferAccount-doAcceptMoney param:" + JSON.toJSONString(transferAccept));
                        //调用确认转账,且不发送mqtt消息
                        result = this.doAcceptMoney(packBundle, transferAccept,false);
                        if (result == null){
                            continue;
                        }
                        logger.info("prizeTransferAccount-doAcceptMoney result:" + result);
                        BaseResult<List<JSONObject>> baseResult = JSON.parseObject(result, new TypeReference<BaseResult<List<JSONObject>>>(){});
                        //返回结果成功后，更新红包明细转账信息
                        if (baseResult != null && baseResult.getStatus().intValue() == EnumStatus.ZORO.getIndex()) {
                            publicRedPacketDetail.setTransferStatus((short) EnumStatus.ONE.getIndex());
                            publicRedPacketDetail.setUnfreezeStatus((short) EnumStatus.TWO.getIndex());
                            publicRedPacketDetail.setDetailId(detail.getDetailId());
                            publicRedPacketDetail.setUnfreezeTime(new Date());
                            publicRedPacketDetail.setTransferTime(new Date());
                            publicRedPacketDetailDao.updateById(publicRedPacketDetail);
                            logger.info("更新需要转账的红包状态");
                        }
                        else{
                            logger.info("转账返回结果错误：" + result);
                        }
                    }
                } catch (Exception definedError) {
                    Long tradeId =  transferAnswer.getTradeId();
                    if (tradeId != null) {
                        String error = "领取红包自动转账失败";
                        long currentTimes = System.currentTimeMillis();
                        tradeDao.acceptApply(tradeId,EnumStatus.THREE.getIndex(),
                                currentTimes,error,
                                null,null,
                                null,null,detail.getCashAmount());
                    }
                    //转账异常状态，值为：2
                    publicRedPacketDetail.setTransferStatus((short) EnumStatus.TWO.getIndex());
                    publicRedPacketDetail.setUnfreezeStatus((short) EnumStatus.TWO.getIndex());
                    publicRedPacketDetail.setUnfreezeTime(new Date());
                    publicRedPacketDetail.setDetailId(detail.getDetailId());
                    publicRedPacketDetail.setTransferTime(new Date());
                    publicRedPacketDetailDao.updateById(publicRedPacketDetail);
                    definedError.printStackTrace();
                    continue;
                }
            }
            //处理进群红包奖励
//            GroupPromoteDetail groupPromoteDetailParam = new GroupPromoteDetail();
//            groupPromoteDetailParam.setTargetUserId(userId);
//            groupPromoteDetailParam.setExpireTime(new Date().getTime());
//            groupPromoteDetailParam.setTransferStatus(EnumStatus.ONE.getIndex());
//            logger.info("prizeTransferAccount:get user group prize info:" + JSON.toJSONString(groupPromoteDetailParam));
//            List<GroupPromoteDetail> groupPromoteDetails = groupPromoteDetailDao.listCriteria(groupPromoteDetailParam);
//            for (GroupPromoteDetail groupPromoteDetail:groupPromoteDetails){
//                try {
//                    TransferAccept transferAccept = new TransferAccept();
//                    transferAccept.setTradeNo(String.valueOf(groupPromoteDetail.getTradeId()));
//                    transferAccept.setToUuid(uuid);
//                    transferAccept.setToUserId(userId);
//                    transferAccept.setAcceptTime(new Date().getTime());
//                    logger.info("prizeTransferAccount-doAcceptMoney param:" + JSON.toJSONString(transferAccept));
//                    //调用确认转账
//                    String result = this.doAcceptMoney(packBundle,transferAccept);
//                    if (result == null){
//                        continue;
//                    }
//                    logger.info("prizeTransferAccount-doAcceptMoney result:" + result);
//                    BaseResult<List<JSONObject>> baseResult = JSON.parseObject(result, new TypeReference<BaseResult<List<JSONObject>>>(){});
//                    //返回结果成功后，更新红包明细转账信息
//                    if (baseResult != null && baseResult.getStatus().intValue() == EnumStatus.ZORO.getIndex()) {
//                        groupPromoteDetailParam.setTransferStatus(EnumStatus.TWO.getIndex());
//                        groupPromoteDetailParam.setId(groupPromoteDetail.getId());
//                        groupPromoteDetailParam.setTransferTime(new Date());
//                        int rval = groupPromoteDetailDao.updateById(groupPromoteDetailParam);
//                        if (rval > 0){
//                            logger.info("prizeTransferAccount-group promote update succes:" + JSON.toJSONString(groupPromoteDetailParam));
//                        }
//                        else{
//                            logger.info("prizeTransferAccount-group promote update fail：" + JSON.toJSONString(groupPromoteDetailParam));
//                        }
//                    }
//                } catch (DefinedError definedError) {
//                    definedError.printStackTrace();
//                    continue;
//                }
//            }
        }
        return null;
    }

}
