package com.taiyiyun.passport.service.impl;

import com.taiyiyun.passport.consts.Config;
import com.taiyiyun.passport.service.IPasswordLockService;
import com.taiyiyun.passport.service.IRedisService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class PasswordLockServiceImpl implements IPasswordLockService {

    @Resource
    IRedisService redisService;

    @Override
    public int refreshError(String business, String key) {
        Object redisErrorTimes = redisService.get( business + ".times." + key);
        Integer allowTimes = Config.getInt(business + ".times", 5);

        Integer errorTimes = 0;

        if(null != redisErrorTimes) {
            errorTimes = (Integer)redisErrorTimes;
        }

        errorTimes++;

        if(errorTimes >= allowTimes) {
            redisService.put(business + ".lock." + key, true, Config.getInt(business + ".lock", 1800));
            redisService.evict(business + ".times." + key);
        }else {
            redisService.put(business + ".times." + key, errorTimes, 3600 * 24);
        }

        int count = allowTimes - errorTimes;
        if(count > 0){
            return count;
        } else {
            return 0;
        }
    }

    @Override
    public void releaseLock(String business, String key) {
        redisService.evict(business + ".lock." + key);
        redisService.evict(business + ".times." + key);
    }

    @Override
    public boolean checkLock(String business, String key) {
        try {
            Object lock = redisService.get(business + ".lock." + key);

            if(null == lock) {
                return false;
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }
}
