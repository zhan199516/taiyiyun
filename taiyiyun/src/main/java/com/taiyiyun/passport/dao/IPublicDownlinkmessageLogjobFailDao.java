package com.taiyiyun.passport.dao;

import com.taiyiyun.passport.po.PublicDownlinkmessageLogjobFail;

/**
 * Created by nina on 2018/3/15.
 */
public interface IPublicDownlinkmessageLogjobFailDao {

    PublicDownlinkmessageLogjobFail selectByPrimarykey(int jobId);

    void insertSelective(PublicDownlinkmessageLogjobFail downlinkmessageLogjobFail);

    void deleteByPrimaryKey(int jobId);

    void updateByPrimaryKeySelective(PublicDownlinkmessageLogjobFail downlinkmessageLogjobFail);

}
