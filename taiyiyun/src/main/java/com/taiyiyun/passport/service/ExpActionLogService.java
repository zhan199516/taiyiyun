package com.taiyiyun.passport.service;

import com.taiyiyun.passport.po.chat.ExpActionLog;

import java.util.List;

/**
 * Created by elan on 2017/11/22.
 */
public interface ExpActionLogService {

    public List<ExpActionLog> getByTime(String userId , Long createTime);

    public void save(List<ExpActionLog> expActionLogList);

}
