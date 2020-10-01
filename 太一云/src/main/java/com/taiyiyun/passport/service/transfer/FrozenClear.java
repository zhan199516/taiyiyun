package com.taiyiyun.passport.service.transfer;

import com.alibaba.fastjson.JSON;
import com.taiyiyun.passport.dao.IThirdAppUserBindDao;
import com.taiyiyun.passport.dao.ITradeDao;
import com.taiyiyun.passport.exception.DefinedError;
import com.taiyiyun.passport.init.SpringContext;
import com.taiyiyun.passport.po.ThirdAppUserBindExt;
import com.taiyiyun.passport.po.asset.Trade;
import com.taiyiyun.passport.transfer.Answer.FrozenResult;
import com.taiyiyun.passport.transfer.Ask.AskBody;
import com.taiyiyun.passport.transfer.ITransferAccounts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by okdos on 2017/7/24.
 */
public class FrozenClear {
    private static FrozenClear ourInstance = new FrozenClear();

    public static FrozenClear getInstance() {
        return ourInstance;
    }

    public final Logger logger = LoggerFactory.getLogger(getClass());

    private FrozenClear() {
        executorService = Executors.newSingleThreadScheduledExecutor();
        tradeDao = SpringContext.getBean(ITradeDao.class);
        thirdAppUserBindDao = SpringContext.getBean(IThirdAppUserBindDao.class);
    }

    private ScheduledExecutorService executorService; // 定时任务
    private IThirdAppUserBindDao thirdAppUserBindDao;


    public void start(){
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                clear();
            }
        }, 1, 10, TimeUnit.MINUTES);
    }

    public void stop(){

    }

    ITradeDao tradeDao;

    protected void clear(){
        logger.info("开始进行过期转账清算：" + new Date().getTime());
        List<Trade> list = tradeDao.getTimeout(new Date().getTime());
        logger.info("可处理的过期转账数据：" + list == null?"0":String.valueOf(list.size()));
        for(int i = 0; i < list.size(); i++){
            Trade trade = list.get(i);
            try{
                logger.info("过期转账清算数据：" + JSON.toJSONString(trade));
                unfroze(trade);
            } catch(DefinedError definedError){
                logger.error("DefinedError",definedError);
                logger.info("过期转账清算异常：" + definedError.getReadableMsg() + "(" + definedError.getErrorCode() + ")");
                trade.setStatus(3);
                trade.setError(definedError.getReadableMsg());
                updateTrade(trade);
            }
        }
        if (list == null || list.size() == 0){
            logger.info("没有满足条件的过期转账数据");
        }
        logger.info("过期转账清算完成");
    }

    private void unfroze(Trade trade) throws DefinedError {

        ThirdAppUserBindExt ext = thirdAppUserBindDao.getOneExtByCondition(trade.getFromUuid(), trade.getPlatformId(), null);

        if(ext == null){
            logger.info("过期转账清算异常：" + "查询不到绑定信息");
            throw new DefinedError.ThirdNotFoundException("解除冻结资金：查询不到绑定信息", null);
        }

        if(ext.getBindStatus() == null || ext.getBindStatus() != 1){
            logger.info("过期转账清算异常：" + "查询不到绑定信息");
            throw new DefinedError.UnBindException("解除冻结资金：查询不到绑定信息", null);
        }

        ITransferAccounts accounts = ThirdAppManager.getInstance().getAccountById(trade.getPlatformId());

        if(accounts == null){
            logger.info("过期转账清算异常：" + "查询不到第三方应用信息");
            throw new DefinedError.ThirdNotFoundException("解除冻结资金：查询不到第三方应用信息", null);
        }

        String relateId = thirdAppUserBindDao.getRelateId(trade.getPlatformId());

        AskBody body = accounts.askForToken(null, ext, relateId);

        if(body == null || body.getToken() == null){
            logger.info("过期转账清算异常：" + "获取token失败");
            throw new DefinedError.ThirdNotFoundException("解除冻结资金：获取token失败", null);
        }
        logger.info("过期转账清算：开始解冻资产");
        FrozenResult frozenResult = accounts.askForUnfreeze(null, body, trade.getCoinId(), trade.getAmount(), trade.getFrozenId());
        if(frozenResult.isSuccess()){
            logger.info("过期转账清算成功：" + frozenResult.getFroId());
            trade.setStatus(2);
            updateTrade(trade);
        } else {
            logger.info("过期转账清算异常：" + "第三方返回("+frozenResult.getErrorCode().toString()+")");
            trade.setStatus(3);
            trade.setError("解除冻结资金：第三方返回" + frozenResult.getErrorCode().toString());
            updateTrade(trade);
        }
    }

    public void updateTrade(Trade trade){

        int rval = tradeDao.acceptApply(trade.getTradeId(), trade.getStatus(), null, trade.getError(), null, null, null, null, null);
        if (rval > 0){
            logger.info("过期转账信息更新成功！");
        }
        else{
            logger.info("过期转账信息更新失败！");
        }
    }
}
