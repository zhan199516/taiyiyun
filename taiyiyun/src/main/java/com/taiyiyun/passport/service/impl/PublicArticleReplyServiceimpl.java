package com.taiyiyun.passport.service.impl;

import com.taiyiyun.passport.dao.IPublicArticleReplyDao;
import com.taiyiyun.passport.po.PublicArticleReply;
import com.taiyiyun.passport.po.PublicArticleReplyDel;
import com.taiyiyun.passport.service.IPublicArticleReplyService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by okdos on 2017/6/29.
 */
@Service
public class PublicArticleReplyServiceimpl implements IPublicArticleReplyService {

    @Resource
    private IPublicArticleReplyDao dao;

    @Override
    public List<PublicArticleReply> getArticleReplies(String articleId) {

        List<PublicArticleReply> list = dao.getRichByArticleId(articleId);

        List<PublicArticleReply> outList = new ArrayList<>();
        HashMap<Long, PublicArticleReply> dict = new HashMap<>();

        for (PublicArticleReply model : list) {
            if (model.getParentId() == null) {
                outList.add(model);
                dict.put(model.getReplyId(), model);
            }
        }

        for (PublicArticleReply model : list) {
            if(model.getParentId() != null && dict.containsKey(model.getParentId())){
                dict.get(model.getParentId()).getReplies().add(model);
            }
        }

        return outList;
    }

    @Override
    public PublicArticleReply insertReply(PublicArticleReply reply) {
        dao.insertReply(reply);
        return reply;
    }

    @Override
    public PublicArticleReply insertReplyChild(PublicArticleReply reply) {
        dao.insertReplyChild(reply);
        return reply;
    }

    @Override
    public int deleteReply(PublicArticleReplyDel del) {

        if(del.getParentId() == null || del.getParentId() == 0){
            return dao.deleteReply(del.getOperator(), del.getReplyId());
        } else {
            return dao.deleteReplyChild(del.getOperator(), del.getReplyId(), del.getParentId());
        }

    }
}
