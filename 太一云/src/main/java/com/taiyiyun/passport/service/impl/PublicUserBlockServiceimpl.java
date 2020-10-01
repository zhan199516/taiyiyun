package com.taiyiyun.passport.service.impl;

import com.taiyiyun.passport.dao.IPublicUserBlockDao;
import com.taiyiyun.passport.dao.IPublicUserFollowerDao;
import com.taiyiyun.passport.po.PublicUser;
import com.taiyiyun.passport.po.PublicUserBlock;
import com.taiyiyun.passport.po.PublicUserFollower;
import com.taiyiyun.passport.po.PublicUserLike;
import com.taiyiyun.passport.service.IPublicUserBlockService;
import com.taiyiyun.passport.util.StringUtil;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import java.util.List;

/**
 * Created by okdos on 2017/6/29.
 */
@Service
public class PublicUserBlockServiceimpl implements IPublicUserBlockService{
    @Resource
    private IPublicUserBlockDao dao;

    @Resource
    private IPublicUserFollowerDao publicUserFollowerDao;

    @Override
    public List<PublicUser> getBlockByUserId(String userId) {
        return dao.getBlockByUserId(userId);
    }

    /**
     * 拉黑时，自动取消关注
     * @param userId
     * @param focusId
     * @return
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int blockUser(String userId, String focusId) {
        publicUserFollowerDao.delete(userId, focusId);

        PublicUserBlock block = new PublicUserBlock();
        block.setUserId(userId);
        block.setBlockId(focusId);
        return dao.save(block);
    }

    @Override
    public int unblockUser(String userId, String focusId) {
        return dao.delete(userId, focusId);
    }

	@Override
	public PublicUser getMyBlock(String userId, String blockId) {
		if (StringUtil.isEmpty(blockId) || StringUtil.isEmpty(userId)) {
			return null;
		}
		
		return dao.getMyBlock(userId, blockId);
	}


}
