package com.taiyiyun.passport.service;

import com.taiyiyun.passport.po.PublicArticleReply;
import com.taiyiyun.passport.po.PublicArticleReplyDel;

import java.util.List;

/**
 * Created by okdos on 2017/6/29.
 */
public interface IPublicArticleReplyService {
    public List<PublicArticleReply> getArticleReplies(String articleId);

    PublicArticleReply insertReply(PublicArticleReply reply);

    PublicArticleReply insertReplyChild(PublicArticleReply reply);

    int deleteReply(PublicArticleReplyDel del);
}
