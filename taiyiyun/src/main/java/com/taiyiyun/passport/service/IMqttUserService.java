package com.taiyiyun.passport.service;

import com.taiyiyun.passport.bean.MqttClientSetting;
import com.taiyiyun.passport.language.PackBundle;
import com.taiyiyun.passport.po.BaseResult;

/**
 * Created by okdos on 2017/7/3.
 */
public interface IMqttUserService {
    MqttClientSetting getByUserId(String userId) throws Exception;

    /**
     * 重新发送消息
     * @param bundle
     * @param messageId
     * @param userId
     * @param topic
     * @return
     */
    public BaseResult<Integer> repeatMessage(PackBundle bundle, String messageId,String userId,String topic);
}
