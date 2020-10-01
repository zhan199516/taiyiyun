package com.taiyiyun.passport.dao;

import com.taiyiyun.passport.po.PublicUser;
import com.taiyiyun.passport.po.PublicUserBlock;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * Created by okdos on 2017/6/29.
 */
public interface IPublicUserBlockDao {

    public PublicUser getMyBlock(@Param("userId") String userId, @Param("blockId") String blockId);

    public List<PublicUser> getBlockByUserId(String userId);

    public int delete(@Param("userId") String userId, @Param("blockId") String blockId);

    public int save(PublicUserBlock bean);

    public List<PublicUser> getBlockMeUsers(@Param("userId") String userId);

    Integer selectBlockByUserIdAndBlockUserId(Map<String, String> params);
}
