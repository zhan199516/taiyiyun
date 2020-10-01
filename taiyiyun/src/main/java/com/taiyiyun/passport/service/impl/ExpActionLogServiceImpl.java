package com.taiyiyun.passport.service.impl;

import com.taiyiyun.passport.dao.IExpActionLogDao;
import com.taiyiyun.passport.po.chat.ExpActionLog;
import com.taiyiyun.passport.service.ExpActionLogService;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.Date;
import java.util.List;

/**
 * Created by elan on 2017/11/22.
 */
@Service
public class ExpActionLogServiceImpl implements ExpActionLogService {

    @Resource
    private IExpActionLogDao dao;

    @Override
    public List<ExpActionLog> getByTime(String userId, Long createTime) {
        return dao.getByTime(userId,createTime);
    }

    @Override
    public void save(List<ExpActionLog> expActionLogList) {

        dao.save(expActionLogList);
    }

}
