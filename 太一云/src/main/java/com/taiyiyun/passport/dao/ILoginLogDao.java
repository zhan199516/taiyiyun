package com.taiyiyun.passport.dao;

import com.taiyiyun.passport.po.LoginLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ILoginLogDao {
    /**
     * 获取最近的多个deviceId（默认3个）
     * @param mobile
     * @return
     */
    List<LoginLog> getRecentMarksLogs(@Param("mobile") String mobile, @Param("limit") int limit);

    /**
     * 更新mark时间
     * @param log
     * @return
     */
    int updateMarkTime(LoginLog log);

    int newLog(LoginLog log);

    int newFailedLog(LoginLog log);
}
