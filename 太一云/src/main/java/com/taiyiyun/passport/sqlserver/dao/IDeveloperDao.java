package com.taiyiyun.passport.sqlserver.dao;

import com.taiyiyun.passport.sqlserver.po.Developer;

/**
 * Created by nina on 2017/10/25.
 */
public interface IDeveloperDao {
    Developer selectDeveloperByAppKey(String appKey);
}
