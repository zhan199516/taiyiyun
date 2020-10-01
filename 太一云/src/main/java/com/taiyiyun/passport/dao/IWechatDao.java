package com.taiyiyun.passport.dao;

import org.apache.ibatis.annotations.Param;

public interface IWechatDao {
    String selectById(String appId);
    int updateBegin(@Param("appId") String appId, @Param("nowLong") long nowLong, @Param("oldLong") long oldLong,
                    @Param("thenLong") long thenLong);
    int updateRoll(String appId);
    int updateEnd(@Param("appId") String appId,
                  @Param("accessLastTime") long accessLastTime,
                  @Param("jsApiLastTime") long jsApiLastTime,
                  @Param("accessToken") String accessToken, @Param("jsApiTicket") String jsApiTicket);

}
