package com.taiyiyun.passport.service;

/**
 * 密码错误锁定的service
 */
public interface IPasswordLockService {

    int refreshError(String business, String key);
    void releaseLock(String business, String key);

    boolean checkLock(String business, String key);
}
