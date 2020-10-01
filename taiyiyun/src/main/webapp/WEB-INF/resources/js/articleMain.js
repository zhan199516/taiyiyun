/**
 * Created by okdos on 2017/4/18.
 */

'use strict';

require(['article_app'], function(cmp_app){
    cmp_app.lang = {};
    cmp_app.lang.inputNull = '录入数据不能为空';
    cmp_app.lang.publishSucc = '发表成功';
    cmp_app.lang.reply = '回复';
    cmp_app.lang.authorReply = '文章作者回复';
    cmp_app.lang.replyGrammar = '回复失败,数据结构不正确';
    cmp_app.lang.replyDelete = '回复失败，当前评论已经被删除';
    cmp_app.lang.replySucc = '回复成功';
    cmp_app.lang.replyDeleteSucc = '评论删除成功';
    cmp_app.lang.deleteError = '删除异常';
    cmp_app.lang.lastYear = '去年';
    cmp_app.lang.yesterday = '昨天';
});
