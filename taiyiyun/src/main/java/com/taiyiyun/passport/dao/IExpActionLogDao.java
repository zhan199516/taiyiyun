package com.taiyiyun.passport.dao;

import com.taiyiyun.passport.po.chat.ExpActionLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by elan on 2017/11/22.
 */
public interface IExpActionLogDao {

    public List<ExpActionLog> getByTime(@Param("userId") String userId , @Param("createTime") Long createTime);

    public void save(@Param("expActionLogList") List<ExpActionLog> expActionLogList);

}
