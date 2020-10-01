package com.taiyiyun.passport.service.transfer;

import com.taiyiyun.passport.consts.Config;
import com.taiyiyun.passport.consts.EnumStatus;
import com.taiyiyun.passport.dao.IPublicRedPacketLimitDao;
import com.taiyiyun.passport.dao.IPublicUserDao;
import com.taiyiyun.passport.dao.IThirdAppUserBindDao;
import com.taiyiyun.passport.exception.DefinedError;
import com.taiyiyun.passport.init.SpringContext;
import com.taiyiyun.passport.po.PublicRedPacketLimit;
import com.taiyiyun.passport.po.PublicUser;
import com.taiyiyun.passport.po.ThirdAppUserBindExt;
import com.taiyiyun.passport.transfer.Answer.LimitResult;
import com.taiyiyun.passport.transfer.Ask.AskBody;
import com.taiyiyun.passport.transfer.ITransferAccounts;
import com.taiyiyun.passport.util.SHAUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 指定特殊用户发红包限额设置，自动处理程序
 */
public class LimitAmountManager {

    private static LimitAmountManager ourInstance = new LimitAmountManager();

    public static LimitAmountManager getInstance() {
        return ourInstance;
    }

    public final Logger logger = LoggerFactory.getLogger(getClass());
    private ScheduledExecutorService executorService; // 定时任务
    private IThirdAppUserBindDao thirdAppUserBindDao;
    private IPublicUserDao publicUserDao;
    private IPublicRedPacketLimitDao publicRedPacketLimitDao;

    private LimitAmountManager() {
        executorService = Executors.newSingleThreadScheduledExecutor();
        thirdAppUserBindDao = SpringContext.getBean(IThirdAppUserBindDao.class);
        publicUserDao = SpringContext.getBean(IPublicUserDao.class);
        publicRedPacketLimitDao = SpringContext.getBean(IPublicRedPacketLimitDao.class);
    }

    public void start(){
        //每5分钟，执行一次
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                config();
            }
        }, 1, 5, TimeUnit.MINUTES);
    }

    public void stop(){

    }

    public void config(){
        logger.info("==>开始进行特殊用户指定限额处理任务");
        //借用阿里云配置访问秘钥
        String secretKey = Config.get("aliyun.push.accessKeySecret","8uEldf04RGvaGuK3hhMwkIVOqQuP6L");

        PublicRedPacketLimit publicRedPacketLimitParam = new PublicRedPacketLimit();
        publicRedPacketLimitParam.setLimitStatus(EnumStatus.ONE.getIndex());
        List<PublicRedPacketLimit> publicRedPacketLimits = publicRedPacketLimitDao.list(publicRedPacketLimitParam);
        for(PublicRedPacketLimit publicRedPacketLimit:publicRedPacketLimits){
            try {
                String userId = publicRedPacketLimit.getUserId();
                String platformId = publicRedPacketLimit.getPlatformId();
                BigDecimal amount = publicRedPacketLimit.getCashAmount();
                if (amount == null){
                    amount = BigDecimal.ZERO;
                }
                String dataSign = publicRedPacketLimit.getDataSign();
                try {
                    String key = secretKey + "-"  + userId + "-" + platformId + "-" + String.valueOf(amount.intValue());
                    String sign = SHAUtil.shaEncode(key);
                    if (dataSign == null || !dataSign.equals(sign)){
                        logger.info("==>设置的目标用户数据签名错误，不准设置额度数据");
                        continue;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                PublicUser publicUser = publicUserDao.getByUserId(userId);
                if (publicUser == null) {
                    continue;
                }
                String uuid = publicUser.getUuid();
                ThirdAppUserBindExt ext = thirdAppUserBindDao.getOneExtByCondition(uuid, platformId, null);
                if (ext == null || ext.getBindStatus() == null || ext.getBindStatus() == 0) {
                    logger.info("==>查询不到绑定信息");
                    continue;
                }
                //根据平台id获取用户绑定关系id，并调用接口获取第三方接口访问token
                String relateId = thirdAppUserBindDao.getRelateId(platformId);
                ITransferAccounts accounts = ThirdAppManager.getInstance().getAccountById(platformId);
                if (accounts == null) {
                    logger.info("==>查询不到第三方应用信息");
                    continue;
                }
                AskBody bodyAsset = accounts.askForToken(null, ext, relateId);
                if (bodyAsset.getToken() == null) {
                    logger.info("==>获取token失败");
                    continue;
                }
                BigDecimal cashAmount = publicRedPacketLimit.getCashAmount();
                LimitResult limitResult = accounts.askForLimit(null, bodyAsset,cashAmount);
                if (!limitResult.isSuccess()) {
                    logger.info("==>获取资产信息失败-错误码：" + limitResult.getErrorCode());
                    continue;
                }
                //更新状态为0,标识已经设置完成
                PublicRedPacketLimit limit = new PublicRedPacketLimit();
                limit.setId(publicRedPacketLimit.getId());
                limit.setLimitStatus(EnumStatus.ZORO.getIndex());
                publicRedPacketLimitDao.updateById(limit);
            }
            catch (DefinedError definedError){
                logger.info("==>设置指定用户限额返回异常：" + definedError.getReadableMsg());
                logger.info("==>设置指定用户限额返回异常：" + definedError.getMessage());
            }
        }
        if(publicRedPacketLimits.size() <= 0){
            logger.info("==>不满足处理条件");
        }
        logger.info("==>进行特殊用户指定限额处理任务完成");
    }
}
