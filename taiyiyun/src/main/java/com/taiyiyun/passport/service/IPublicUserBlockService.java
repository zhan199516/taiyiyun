package com.taiyiyun.passport.service;

import com.taiyiyun.passport.po.PublicUser;
import com.taiyiyun.passport.po.PublicUserBlock;

import java.util.List;

import org.apache.ibatis.annotations.Param;

/**
 * Created by okdos on 2017/6/29.
 */
public interface IPublicUserBlockService {

    List<PublicUser> getBlockByUserId(String userId);

    int blockUser(String userId, String focusId);

    int unblockUser(String userId, String focusId);
    
    public PublicUser getMyBlock(String userId, String blockId);
}
