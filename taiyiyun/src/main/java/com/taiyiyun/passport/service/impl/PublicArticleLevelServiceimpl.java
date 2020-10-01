package com.taiyiyun.passport.service.impl;

import com.taiyiyun.passport.dao.IPublicArticleLevelDao;
import com.taiyiyun.passport.po.PublicArticleLevel;
import com.taiyiyun.passport.service.IPublicArticleLevelService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Timestamp;

/**
 * Created by okdos on 2017/6/29.
 */
@Service
public class PublicArticleLevelServiceimpl implements IPublicArticleLevelService {

    @Resource
    IPublicArticleLevelDao dao;

    @Override
    public PublicArticleLevel getByTowId(String userId, String articleId) {
        return dao.getByTowId(userId, articleId);
    }

    @Override
    public void doUp(String userId, String articleId) {
        PublicArticleLevel level = new PublicArticleLevel();
        level.setArticleId(articleId);
        level.setUserId(userId);
        level.setCreateTime(new Timestamp(System.currentTimeMillis()));
        level.setLikeLevel(1);

        doUpdate(level);

    }

    @Override
    public void doDown(String userId, String articleId) {
        PublicArticleLevel level = new PublicArticleLevel();
        level.setArticleId(articleId);
        level.setUserId(userId);
        level.setCreateTime(new Timestamp(System.currentTimeMillis()));
        level.setLikeLevel(2);

        doUpdate(level);
    }

    @Override
    public void doCancel(String userId, String articleId) {
        PublicArticleLevel level = new PublicArticleLevel();
        level.setArticleId(articleId);
        level.setUserId(userId);
        level.setCreateTime(new Timestamp(System.currentTimeMillis()));
        level.setLikeLevel(0);

        doUpdate(level);
    }

    private void doUpdate(PublicArticleLevel level){
        int count = dao.insert(level);

        if(count == 0){
            dao.update(level);
        }
    }

}
