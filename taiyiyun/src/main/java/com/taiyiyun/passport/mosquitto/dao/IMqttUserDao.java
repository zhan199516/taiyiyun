package com.taiyiyun.passport.mosquitto.dao;

import com.taiyiyun.passport.mosquitto.po.MqttUser;
import org.apache.ibatis.annotations.Param;

/**
 * Created by okdos on 2017/7/3.
 */
public interface IMqttUserDao {

    // 通过userId获取连接数据
    MqttUser getByUserId(String userId);

    int insertUserIn(@Param("userId") String userId, @Param("pw") String pw);

}
