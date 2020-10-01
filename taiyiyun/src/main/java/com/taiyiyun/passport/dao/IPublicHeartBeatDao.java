package com.taiyiyun.passport.dao;

import com.taiyiyun.passport.po.PublicHeartBeat;

/**
 * Created by nina on 2018/1/26.
 */
public interface IPublicHeartBeatDao {

    void save(PublicHeartBeat publicHeartBeat);

    PublicHeartBeat selectByUserId(String userId);
}
