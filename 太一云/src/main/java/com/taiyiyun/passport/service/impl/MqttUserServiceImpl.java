package com.taiyiyun.passport.service.impl;

import com.taiyiyun.passport.bean.MqttClientSetting;
import com.taiyiyun.passport.consts.EnumStatus;
import com.taiyiyun.passport.dao.IPublicUserDao;
import com.taiyiyun.passport.de.rtner.PbkdfEncoder;
import com.taiyiyun.passport.language.PackBundle;
import com.taiyiyun.passport.mongo.dao.IMessageLogDao;
import com.taiyiyun.passport.mongo.po.MessageLog;
import com.taiyiyun.passport.mosquitto.dao.IMqttUserDao;
import com.taiyiyun.passport.mosquitto.po.MqttUser;
import com.taiyiyun.passport.mqtt.Message;
import com.taiyiyun.passport.po.BaseResult;
import com.taiyiyun.passport.po.MqttUserPWD;
import com.taiyiyun.passport.service.IMqttUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

/**
 * Created by okdos on 2017/7/3.
 */
@Service
public class MqttUserServiceImpl implements IMqttUserService{

    public final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    IMqttUserDao mqttUserDao;

    @Resource
    IPublicUserDao publicUserDao;

    @Resource
    IMessageLogDao messageLogDao;


    PbkdfEncoder pbkdfEncoder = PbkdfEncoder.getInstance();

    @Override
    public MqttClientSetting getByUserId(String userId) throws Exception {

        MqttUserPWD pwd = publicUserDao.getMqttPwdById(userId);
        if(pwd == null){
            return null;
        }

        MqttUser mu = mqttUserDao.getByUserId(userId);

        if(null == mu || null == pwd.getMqttPwdExpireTime() || pwd.getMqttPwdExpireTime() < (new Date()).getTime()){
            String password = pbkdfEncoder.newRandomString();
            String encodedPWD = pbkdfEncoder.encodePassword(password);

            pwd.setMqttPwd(password);
            pwd.setMqttPwdEncode(encodedPWD);
            pwd.setMqttPwdExpireTime((new Date()).getTime() + 1000 * 60 * 60 * 24);
            publicUserDao.newMqttPwd(pwd);
            mqttUserDao.insertUserIn(userId, encodedPWD);
        }

        MqttClientSetting mqttClientSetting = new MqttClientSetting();

        mqttClientSetting.setUsername(pwd.getUserId());
        mqttClientSetting.setPw(pwd.getMqttPwd());
        mqttClientSetting.setSup("0");

        return mqttClientSetting;
    }

    /**
     * 重新发送消息
     *
     * @param bundle
     * @param messageId
     * @return
     */
    @Override
    public BaseResult<Integer> repeatMessage(PackBundle bundle, String messageId,String userId,String clientId) {
        BaseResult<Integer> resultData = new BaseResult<>();
        try {
            MessageLog messageLog = messageLogDao.getOneMessage(messageId,userId,clientId);
            if (messageLog == null){
                String errMsg = "message not exist";
                if(bundle != null) {
                    errMsg = bundle.getString("message.repeat.send.fail");
                }
                resultData.setStatus(EnumStatus.ONE.getIndex());
                resultData.setError(errMsg);
                return resultData;
            }
            //设置消息更新时间
            messageLog.setUpdateTime(System.currentTimeMillis());
            Message<Object> message = new Message<>();
            //拷贝原始消息内容
            BeanUtils.copyProperties(messageLog,message);
            com.taiyiyun.passport.mqtt.v2.MessagePublisher.getInstance().publish(Message.UPLINK_MESSAGE + userId, message);
            resultData.setStatus(EnumStatus.ZORO.getIndex());
            resultData.setError("success");
            return resultData;
        }
        catch (Exception e){
            e.printStackTrace();
            logger.info("MqttUserServiceImpl.repeatMessage.error:" + e.getMessage());
            resultData.setStatus(EnumStatus.NINETY_NINE.getIndex());
            resultData.setError(e.getMessage());
            return resultData;
        }
    }
}
