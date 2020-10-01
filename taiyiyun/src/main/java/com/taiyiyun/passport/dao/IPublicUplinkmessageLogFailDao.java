package com.taiyiyun.passport.dao;

import com.taiyiyun.passport.po.PublicUplinkmessageLogFail;

/**
 * Created by nina on 2018/3/15.
 */
public interface IPublicUplinkmessageLogFailDao {

    PublicUplinkmessageLogFail selectByPrimarykey(int logId);

    void insertSelective(PublicUplinkmessageLogFail uplinkmessageLogFail);

    void deleteByPrimaryKey(int logId);

    void updateByPrimaryKeySelective(PublicUplinkmessageLogFail uplinkmessageLogFail);
}
