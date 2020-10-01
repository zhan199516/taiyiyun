package com.taiyiyun.passport.dao;

import com.taiyiyun.passport.po.PublicArticleReply;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by okdos on 2017/6/29.
 */
public interface IPublicArticleReplyDao {

    public List<PublicArticleReply> getRichByArticleId(String articleId);

    public int insertReply(PublicArticleReply reply);

    public int insertReplyChild(PublicArticleReply reply);

    public int deleteReply(@Param("operator") String operator, @Param("replyId") long replyId);

    public int deleteReplyChild(@Param("operator") String operator, @Param("replyId") long replyId, @Param("parentId") long parentId);

}
