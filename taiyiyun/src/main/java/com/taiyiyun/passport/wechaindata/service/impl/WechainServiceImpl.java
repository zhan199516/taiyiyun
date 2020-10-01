package com.taiyiyun.passport.wechaindata.service.impl;

import com.taiyiyun.passport.sqlserver.dao.IStatisticalDataDao;
import com.taiyiyun.passport.sqlserver.po.StatisticalData;
import com.taiyiyun.passport.wechaindata.service.IWechainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by nina on 2017/12/26.
 */
@Service
public class WechainServiceImpl implements IWechainService {
    @Autowired
    private IStatisticalDataDao dataDao;

    @Override
    public StatisticalData queryStatisticalData() {
        return dataDao.selectStatisticalData();
    }
}
