package com.taiyiyun.passport.service.impl;

import com.taiyiyun.passport.dao.IPublicArticleStatisticDao;
import com.taiyiyun.passport.po.PublicArticle;
import com.taiyiyun.passport.po.PublicArticleStatistic;
import com.taiyiyun.passport.service.IPublicArticleStatisticService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * Created by okdos on 2017/7/3.
 */
@Service
public class PublicArticleStaticServiceImpl implements IPublicArticleStatisticService {

    @Resource
    IPublicArticleStatisticDao dal;

    @Override
    public PublicArticleStatistic getByArticleId(String articleId) {
        List<PublicArticleStatistic> statList = dal.getByTowId(articleId, null);

        if(statList.size() == 0){
            dal.insertStatistic(articleId, null);
            dal.updateStatistic(articleId, null);

            statList = dal.getByTowId(articleId, null);
            if(statList.size() > 0){
                return statList.get(0);
            } else {
                return null;
            }

        } else {
            Date n = new Date();
            PublicArticleStatistic stat = statList.get(0);

            if(stat.getCreateTime().getTime() < n.getTime() - 2*1000){
                dal.updateStatistic(articleId, null);
                statList = dal.getByTowId(articleId, null);
                if(statList.size() > 0){
                    return statList.get(0);
                } else {
                    return null;
                }
            } else {
                return stat;
            }
        }


    }

    @Override
    public PublicArticleStatistic getByUserId(String userId) {
        dal.insertStatistic(null, userId);
        List<PublicArticleStatistic> statList = dal.getByTowId(null, userId);

        Date n = new Date();
        boolean needUpdate = false;
        for(PublicArticleStatistic stat : statList){
            if(stat.getCreateTime().getTime() < n.getTime() - 3*1000){
                needUpdate = true;
                break;
            }
        }

        if(needUpdate){
            dal.updateStatistic(null, userId);
        }

        statList = dal.getByTowId(null, userId);
        PublicArticleStatistic statAll = new PublicArticleStatistic();
        statAll.setUserId(userId);

        for(PublicArticleStatistic stat: statList){
            statAll.setForwardCount(statAll.getForwardCount() + stat.getForwardCount());
            statAll.setReadCount(statAll.getReadCount() + stat.getReadCount());
            statAll.setReplyCount(statAll.getReplyCount() + stat.getReplyCount());
            statAll.setUpCount(statAll.getUpCount() + stat.getUpCount());
        }

        return statAll;
    }


}
