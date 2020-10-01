/**
 * Created by okdos on 2017/4/14.
 */

define(['config', 'when.min', 'exports'], function(config, when, exports){
    exports.getQueryString = function(name)
    {
        var reg = new RegExp("(^|&)"+ name +"=([^&]*)(&|$)");
        var r = window.location.search.substr(1).match(reg);
        if(r!=null){
            var r2 = r[2];
            return decodeURI(r2);
        }
        return null;
    }

    function packQuery(query){
        var list = [];

        for(var name in query){
            //为空或者undefined的时候，等号后面不加东西
            if(query[name] == null) {
                list.push(name + '=');
            } else {
                list.push(name + '=' + query[name]);
            }
        }

        return list.reduce(function(p1, p2){
            return p1 + "&" + p2;
        })
    }

    //获取当前文章的统计信息
    exports.getArticleLikeAmount = function(article_id){
        var query = {
            articleId: article_id
        };

        var ask = config.askServer + "/circle/article/levelAmount?" + packQuery(query);
        return Vue.http.get(ask).then(function(res){
            return res.body;
        });
    }

    //获取当前公众号的统计信息
    exports.getCircleLikeAmount = function(circle_id){
        var query = {
            circle_id: circle_id
        }

        var ask = config.askServer + "/circle/article/levelAmount?" + packQuery(query);
        return Vue.http.get(ask).then(function(res){
            return res.body;
        });
    }

    //获取某人对文章的评价
    exports.getArticleLike = function(article_id, reader_id){
        var query = {
            articleId: article_id,
            userId: reader_id
        }

        var ask = config.askServer + "/circle/article/level?" + packQuery(query);

        return Vue.http.get(ask).then(function(res){
            return res.body;
        });
    }

    //获取文章的所有评论及回复
    exports.getArticleReply = function(article_id, reply_id){
        var query = {
            articleId: article_id,
        };
        if(reply_id){
            query.replyId = reply_id;
        }

        var ask = config.askServer + "/circle/article/reply?" + packQuery(query);

        return Vue.http.get(ask).then(function(res){
            return res.body;
        });
    }

    exports.postArticleReply = function(body){
        var ask = config.askServer + "/circle/article/reply";
        return Vue.http.post(ask, body).then(function(res){
            return res.body;
        });
    };

    exports.postArticleReplyChild = function(body){
        var ask = config.askServer + "/circle/article/replyChild";

        return Vue.http.post(ask, body).then(function(res){
            return res.body;
        });
    };

    exports.postArticleLike = function(article_id, reader_id, likeLevel){
        var body = {
            likeLevel: likeLevel,
            articleId: article_id,
            userId: reader_id,
        }

        var ask = config.askServer + "/circle/article/level";

        return Vue.http.post(ask, body).then(function(res){
            return res.body;
        });
    }

    exports.deleteReply = function(articleOwner, userId, reply_id){
        var body = {
            operator: userId,
            replyId: reply_id
        }

        var ask = config.askServer + "/circle/article/replyDelete";

        return Vue.http.post(ask, body).then(function(res){
            return res.body;
        });
    }

    exports.deleteReplyChild = function(articleOwner, userId, reply_id, parent_id){
        var body = {
            operator: userId,
            replyId: reply_id,
            parentId: parent_id
        }

        var ask = config.askServer + "/circle/article/replyDelete" ;

        return Vue.http.post(ask, body).then(function(res){
            return res.body;
        });
    };

    exports.getWechatSign = function(url){
        var body = {
            url: url
        };

        var ask = config.askServer + "/circle/Weichat";

        return Vue.http.post(ask, body).then(function(res){
           return res.body;
        });

    }

    exports.getArticleInfo = function (article_id, uuid) {
        var query = {
            article_id: article_id,
            uuid: uuid
        }

        var ask = config.askServer + "/circle/Article/Info?" + packQuery(query);
        return Vue.http.get(ask).then(function (res) {
            return res.body;
        })

    }

})

