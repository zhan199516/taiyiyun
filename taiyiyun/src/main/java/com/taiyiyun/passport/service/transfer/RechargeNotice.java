package com.taiyiyun.passport.service.transfer;

import com.taiyiyun.passport.consts.EnumStatus;
import com.taiyiyun.passport.consts.MsgTemplate;
import com.taiyiyun.passport.dao.IPublicUserDao;
import com.taiyiyun.passport.dao.IThirdAppUserBindDao;
import com.taiyiyun.passport.dao.group.IGroupPromoteConfigDao;
import com.taiyiyun.passport.exception.DefinedError;
import com.taiyiyun.passport.init.SpringContext;
import com.taiyiyun.passport.po.PublicUser;
import com.taiyiyun.passport.po.ThirdAppUserBindExt;
import com.taiyiyun.passport.po.group.GroupPromoteConfig;
import com.taiyiyun.passport.po.mail.MailDto;
import com.taiyiyun.passport.service.IMailService;
import com.taiyiyun.passport.sms.ShortMessage;
import com.taiyiyun.passport.sms.SmsYPClient;
import com.taiyiyun.passport.transfer.Answer.AssetResult;
import com.taiyiyun.passport.transfer.Answer.CoinOwn;
import com.taiyiyun.passport.transfer.Ask.AskBody;
import com.taiyiyun.passport.transfer.ITransferAccounts;
import com.taiyiyun.passport.util.DateUtil;
import com.taiyiyun.passport.util.StringUtil;
import com.taiyiyun.passport.util.ValidatorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 账户余额不足自动提醒功能
 */
public class RechargeNotice {

    private static RechargeNotice ourInstance = new RechargeNotice();

    public static RechargeNotice getInstance() {
        return ourInstance;
    }

    public final Logger logger = LoggerFactory.getLogger(getClass());
    private ScheduledExecutorService executorService; // 定时任务
    private IThirdAppUserBindDao thirdAppUserBindDao;
    private IGroupPromoteConfigDao groupPromoteConfigDao;
    private IPublicUserDao publicUserDao;
    private IMailService mailService;

    private RechargeNotice() {
        executorService = Executors.newSingleThreadScheduledExecutor();
        thirdAppUserBindDao = SpringContext.getBean(IThirdAppUserBindDao.class);
        groupPromoteConfigDao = SpringContext.getBean(IGroupPromoteConfigDao.class);
        publicUserDao = SpringContext.getBean(IPublicUserDao.class);
        mailService = SpringContext.getBean(IMailService.class);
    }

    public void start(){
        //每10分钟，执行一次
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                notice();
            }
        }, 1, 10, TimeUnit.MINUTES);
    }

    public void stop(){

    }

    public void notice(){
        logger.info("==>开始进行推广群群主账户余额验证通知");
        int hour = DateUtil.getHour();
        GroupPromoteConfig groupPromoteConfigParam = new GroupPromoteConfig();
        groupPromoteConfigParam.setIsNotice(EnumStatus.ONE.getIndex());
        groupPromoteConfigParam.setNoticeHour(hour);
        groupPromoteConfigParam.setNoticeTime(new Date());
        //共享号手机集合，防止重复发送短信和邮件
        Map<String,String> shareAccountMobile = new HashMap<>();
        List<GroupPromoteConfig> groupPromoteConfigs = groupPromoteConfigDao.listNotice(groupPromoteConfigParam);
        for(GroupPromoteConfig groupPromoteConfig:groupPromoteConfigs){
            try {
                String userId = groupPromoteConfig.getOwnerId();
                String coinId = groupPromoteConfig.getCoinId();
                String platformId = groupPromoteConfig.getPlatformId();
                //数量阀值
                Integer countThreshold = groupPromoteConfig.getCountThreshold();
                PublicUser publicUser = publicUserDao.getByUserId(userId);
                if (publicUser == null) {
                    continue;
                }
                String mobile = publicUser.getMobile();
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
                AssetResult assetResult = accounts.askForAsset(null, bodyAsset);
                if (!assetResult.isSuccess()) {
                    logger.info("==>获取资产信息失败-错误码：" + assetResult.getErrorCode());
                    continue;
                }
                //是否更新通知时间
                boolean isModifyNoticeTime= false;
                //联系人手机号和邮箱
                String noticePhone = groupPromoteConfig.getNoticePhone();
                String noticeEmail = groupPromoteConfig.getNoticeEmail();
                //验证币资产余额是否满足要求
                List<CoinOwn> coinOwns = assetResult.getCoinList();
                for (CoinOwn coinOwn : coinOwns) {
                    String coinNameTemp = coinOwn.getCoinName();
                    if (!StringUtil.isEmpty(coinNameTemp) && coinNameTemp.equals(coinId)) {
                        BigDecimal availableBalance = coinOwn.getAvailableBalance();
                        //如果阀值数量，大于等于账户币剩余数量，发告警短信和邮件
                        if (new BigDecimal(countThreshold).compareTo(availableBalance) >= 0) {
                            //更新通知时间
                            isModifyNoticeTime = true;
                            break;
                        }
                    }
                }
                //更新通知时间
                if (isModifyNoticeTime){
                    //获取最近一次通知时间，如果为空获取当前时间为最近一次时间
                    Date noticeTime = groupPromoteConfig.getNoticeTime();
                    if (noticeTime == null){
                        noticeTime = new Date();
                    }
                    //获取去通知时间间隔,若为0，默认值为600（10小时通知一次）
                    int noticeInterval = groupPromoteConfig.getNoticeInterval();
                    if (noticeInterval == 0){
                        noticeInterval = 600;
                    }
                    //更新通知时间
                    GroupPromoteConfig config = new GroupPromoteConfig();
                    config.setId(groupPromoteConfig.getId());
                    config.setNoticeTime(DateUtil.addMinute(noticeTime,noticeInterval));
                    config.setModifyTime(new Date());
                    groupPromoteConfigDao.updateById(config);

                    //发短信和邮件
                    //验证共享号群主的手机号码，是否正确，正确发送短信
                    boolean boo = ValidatorUtil.isMobile(mobile);
                    if (boo){
                        String value = shareAccountMobile.get(mobile);
                        if (value == null){
                            //短信内容
                            String smcontent = MsgTemplate.getValue("sm.content.group.recharge","",platformId,coinId,String.valueOf(countThreshold));
                            //发送短信
                            ShortMessage shortMessage = new ShortMessage();
                            shortMessage.setMobile(noticePhone);
                            shortMessage.setText(smcontent);
                            shortMessage.setType(0);
                            SmsYPClient.send(shortMessage);
                        }
                    }
                    //判断手机号，中国手机号标准，否则不发送短信
                    boolean isNoticePhone = ValidatorUtil.isMobile(noticePhone);
                    if (isNoticePhone){
                        //短信内容
                        String smcontent = MsgTemplate.getValue("sm.content.group.recharge","",platformId,coinId,String.valueOf(countThreshold));
                        //发送短信
                        ShortMessage shortMessage = new ShortMessage();
                        shortMessage.setMobile(noticePhone);
                        shortMessage.setText(smcontent);
                        shortMessage.setType(0);
                        SmsYPClient.send(shortMessage);
                    }
                    //发送邮件
                    boolean isEmail = ValidatorUtil.isEmail(noticeEmail);
                    if (isEmail){
                        //邮件内容
                        String smcontent = MsgTemplate.getValue("email.content.group.recharge","",platformId,coinId,String.valueOf(countThreshold));
                        MailDto mail = new MailDto();
                        mail.setContent(smcontent);
                        mail.setToEmails(noticeEmail);
                        mailService.sendEmail(mail);
                    }
                }
            }
            catch (DefinedError definedError){
                logger.info("==>群主通知返回异常：" + definedError.getReadableMsg());
                logger.info("==>群主通知返回异常：" + definedError.getMessage());
            }
            //清空
            shareAccountMobile.clear();
        }
        if(groupPromoteConfigs.size() <= 0){
            logger.info("==>不满足通知条件");
        }
        logger.info("==>推广群群主账户余额验证通知完成");
    }
}
