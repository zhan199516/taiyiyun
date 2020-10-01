/**
 * Created by okdos on 2017/4/18.
 */

'use strict';

require(['article_app'], function(cmp_app){
    cmp_app.lang = {};
    cmp_app.lang.inputNull = '錄入數據不能為空';
    cmp_app.lang.publishSucc = '發表成功';
    cmp_app.lang.reply = '回復';
    cmp_app.lang.authorReply = '文章作者回復';
    cmp_app.lang.replyGrammar = '回復失敗，資料結構不正確';
    cmp_app.lang.replyDelete = '回復失敗，當前評論已經被删除';
    cmp_app.lang.replySucc = '回復成功';
    cmp_app.lang.replyDeleteSucc = '評論删除成功';
    cmp_app.lang.deleteError = '删除异常';
    cmp_app.lang.lastYear = '去年';
    cmp_app.lang.yesterday = '昨天';
});
