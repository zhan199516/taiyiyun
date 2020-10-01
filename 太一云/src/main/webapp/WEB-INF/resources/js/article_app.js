/**
 * Created by okdos on 2017/4/18.
 */

'use strict';

var taiyi_page_refresh = function(){};

define(['when.min', 'config', 'query', 'utils', 'weichat'], function(when, config, query, utils, weichat){

    Vue.component('img-dom', {
        template: '#imgHeadPic',
        data: function(){
            return {
                loadError: false
            }
        },
        props: {
            imgPath: String,
            defaultPath: String
        },
        methods: {
            onImgError: function () {
                this.loadError = true;
            }
        }
    });

    //评论/回复录入框
    Vue.component('reply-input', {
        template: '#replyInput',
        data: function(){
            return {
                replyInput: ''
            }
        },
        methods: {
            send: function() {
                if(this.replyInput == "") {
                    this.$emit('on-alert', this.$root.lang.inputNull);
                    return;
                }
                this.$emit('on-send', this.replyInput);
                this.replyInput = "";
            },
            cancel: function(){
                this.$emit('on-cancel');
            }
        },
        props:{
            isInput: Boolean
        },
        watch:{
            isInput: function(val){
                var self = this;
                if(val){
                    Vue.nextTick(function () {
                        self.$refs.replyDom.focus();
                    });
                }
            }
        }
    });

    //回复内容
    Vue.component('reply-subitem', {
        template: '#replySubitem',
        data: function(){
            return {
            }
        },
        props: {
            item: Object,
            ownArticle: Boolean,
            ownerId: String,
            viewerId: String
        },
        computed:{
            comments: function(){
                return this.item.comment.replace(/[\r\n]/g, '\r').split('\r');
            },
            replyNameFormat: function(){
                if(this.item.userId != this.ownerId){
                    return this.item.userName + this.$root.lang.reply + "："
                } else {
                    return this.$root.lang.authorReply + "："
                }
            },
            createTimeFormat: function(){
                return utils.formatMinuteOrDate(this.item.CreateTime, this.$root.lang);
            },
            //是否可以删除
            canDelete: function(){
                if(!this.viewerId){
                    return false;
                } else
                if(this.ownArticle){
                    return true;
                } else
                if(this.viewerId == this.item.userId){
                    return true;
                } else {
                    return false;
                }
            }
        },
        methods:{
            deleteMe: function(){
                this.$emit('delete-me', this.item);
            }
        }
    });

    //评论内容
    Vue.component('reply-item', {
        template: '#replyItem',
        data: function(){
            return {
            }
        },
        props: {
            item: Object,
            ownArticle: Boolean,
            ownerId: String,
            viewerId: String,
            commentWidth: String
        },
        computed:{
            comments: function(){
                return this.item.comment.replace(/[\r\n]/g, '\r').split('\r');
            },
            createTimeFormat: function(){
                return utils.formatMinuteOrDate(this.item.createTime, this.$root.lang);
            },
            lastChild: function(){
                if(this.item.replies){
                    return this.item.replies[this.item.replies.length - 1];
                } else {
                    return null;
                }
            },
            //是否可以删除
            canDelete: function(){
                if(!this.viewerId){
                    return false;
                } else
                if(this.ownArticle){
                    return true;
                } else
                if(this.viewerId == this.item.userId){
                    return true;
                } else {
                    return false;
                }
            },
            canReply: function(){
                if(this.viewerId){
                    var list = this.item.replies || [];
                    var last = list[list.length - 1];
                    //如果有回复
                    if(last){
                        //最后一个回复的操作者id
                        var lastId = last.userId;

                        if(this.item.userId == this.viewerId && this.item.userId != lastId){
                            return true;
                        }

                        if(this.ownArticle && lastId != this.ownerId){
                            return true;
                        }
                    } else {
                        //如果没有回复，文章作者一定可以回复
                        if(this.ownArticle){
                            return true;
                        }
                    }
                }
                return false;
            }
        },
        methods:{
            deleteItem: function(item){
                this.$emit('on-delete', {
                    reply: this.item,
                    child: item
                });
            },
            deleteMe: function(){
                this.$emit('on-delete', {
                    reply: this.item,
                    child: null
                });
            },
            reply: function(){
                this.$emit('on-reply-child', this.item);
            }
        }
    });

    var app = new Vue({
        el: '#app',
        data: {

            //circle_id: 0,
            circle_isFocused: 1,
            article_id: 0,
            mainHtml: '',

            isOriginal: false,
            forwardFrom: '',

            viewerId: '',

            likeLevel: 0,

            ownerId: '',
            ownArticle: false,

            readerName: "",
            readerPic: "",

            readCount: 0,
            forwardCount: 0,
            upCount: 0,
            downCount: 0,

            reply_list: [],

            replyStatus: false,
            replyChildStatus: false,
            //用作文章回复的item
            replyChildItem: null,

            alertTxt: '',
            alertDisplay: 'none',
            alertConfirm: 'none',

            //当前操作的reply
            currentReply: null,
            //当前操作的reply的子节点
            currentChild: null,

            commentWidth: '200px',

            //是否显示太一护照入口
            showEntrance: false,
            urlEntrance: '',

            operateMethod: '',
            ver: 1,
            platform: '',

            focusResponse: '',
            focusing: false
        },
        computed: {
            publishTime: function(){
                return utils.formatDateTime(this.articlePublishTime);
            },
            replyCount: function(){
                return this.reply_list.reduce(function(p1, p2){
                    return p1 + p2.replies.reduce(function(p3, p4){
                            return ++p3;
                        }, 1);
                }, 0);
            },
            circlePage: function(){
                if(this.ver != 2 || this.viewerId == ""){
                    return null;
                } else {
                    return config.internalUrl + "/PublicCircleHomepage?PublicCircleID=" + this.ownerId;
                }
            },
            certPage: function(){
                if(this.isOriginal) {
                    return config.internalUrl + "/ArticleCer?PublicCircleID=" + this.ownerId + "&ArticleID=" + this.article_id;
                } else {
                    return '';
                }
            }
        },
        directives: {
            title: {
                update: function (el, binding, vnode) {
                    document.title = el.innerText;
                }
            },
            article: {
                bind: function(el, binding, vnode){
                    vnode.context.mainHtml = utils.reconvert(el.innerText);

                    el.innerText = "";
                }
            },
            "base-param": {
                inserted: function(el, binding, vnode){
                    var self = vnode.context;
                    self.article_id = el.dataset.article_id;
                    self.articleTitle = el.dataset.title;
                    self.articleAbstract = el.dataset.abstract;
                    self.imgUrl = el.dataset.url;
                    self.ownerId = el.dataset.owner;

                    self.circle_isFocused = el.dataset.focused;

                    self.viewerId = el.dataset.vid == "null"? "": el.dataset.vid;
                    self.readerName = el.dataset.vname == "null" ? "": el.dataset.vname;
                    self.readerPic = el.dataset.vpic == "null" ? "": el.dataset.vpic;


                    if(el.dataset.original == "true"){
                        self.isOriginal = true;
                    } else {
                        self.isOriginal = false;
                    }
                    if(self.isOriginal){
                        self.forwardFrom = el.dataset.from;
                    } else {
                        self.forwardFrom = "";
                    }

                    if(self.viewerId == self.ownerId){
                        self.ownArticle = true;
                    }

                    //分享链接中，显示iosandrod入口，加入微信sdk
                    if(!self.viewerId){
                        var url = window.location.href.split('#')[0];
                        query.getWechatSign(url).then(function(resBody){
                            var configBody = {
                                config: resBody.Messages,
                                title : self.articleTitle,
                                desc: self.articleAbstract,
                                link: url,
                                imgUrl: self.imgUrl
                            }
                            weichat.share(configBody);
                        });

                        var u = navigator.userAgent;
                        var isAndroid = u.indexOf('Android') > -1 || u.indexOf('Adr') > -1; //android终端
                        var isiOS = !!u.match(/\(i[^;]+;( U;)? CPU.+Mac OS X/); //ios终端
                        if(isiOS){
                            self.showEntrance = true;
                            if(isAndroid){
                                self.urlEntrance = config.androidUrl;
                            } else if(isiOS){
                                self.urlEntrance = config.iosUrl;
                            }
                        }
                    }

                    query.getArticleLikeAmount(self.article_id).then(function(body){
                        body = body || {};
                        self.readCount =  body.readCount || 0;
                        self.forwardCount = body.forwardCount || 0;
                        self.upCount = body.upCount || 0;
                        self.downCount = body.downCount || 0;

                    }, function(err){
                        console.log(err);
                    });


                    //获得所有回复信息
                    query.getArticleReply(self.article_id).then(function(body){
                        self.reply_list = body;
                    }, function(err){
                        console.log(err);
                    })

                    query.getArticleLike(self.article_id, self.viewerId).then(function(body){
                        if(body){
                            self.likeLevel = body.likeLevel;
                        }
                    }, function(err){
                        console.log(err);
                    });

                }
            }
        },
        created: function () {
            this.clientWidthChange();
            this.operateMethod = query.getQueryString('method');
            this.ver = query.getQueryString('ver');
            this.platform = query.getQueryString('platform');
        },
        mounted: function(){
            var self = this;
            window.onresize = function(){
                return (function() {
                    if (!self.timer) {
                        self.timer = true
                        setTimeout(function () {
                            self.clientWidthChange();
                            self.timer = false
                        }, 400)
                    }
                })()
            }
        },
        methods:{
            logoError: function(){

            },
            focusCircle: function(){
                var self = this;

                if(this.circle_isFocused != 0){
                    return;
                }
                var url = config.internalUrl + '/PublicCircle?Concerns=1&PublicCircleID=' + self.ownerId;;
                self.$http.jsonp(url, {
                    jsonp: 'callback'
                }).then(function (req) {
                    self.focusResponse = req.body;
                    if (req.body == '1') {
                        self.circle_isFocused = 1;
                    }

                 }, function(err){
                     self.focusResponse = 'err' + JSON.stringify(err);
                 });
            },
            clientWidthChange: function(){
                var midWidth = 6.9/7.5*document.body.clientWidth;
                this.commentWidth =(midWidth-36-12).toFixed(0)+"px";
            },
            //顶
            doUp: function(){
                console.log('doUp');
                if(!this.viewerId){
                    return;
                }


                if(this.likeLevel == 1){
                    this.likeLevel = 0;
                    this.upCount--;
                } else
                if(this.likeLevel == 2){
                    this.likeLevel = 1;
                    this.upCount++;
                    this.downCount--;
                } else {
                    this.likeLevel = 1;
                    this.upCount++;
                }

                query.postArticleLike(this.article_id, this.viewerId, this.likeLevel);
            },
            //赞
            doDown: function(){
                console.log('doDown');

                if(!this.viewerId){
                    return;
                }

                if(this.likeLevel == 2){
                    this.likeLevel = 0;
                    this.downCount--;
                }
                else if(this.likeLevel == 1){
                    this.likeLevel = 2;
                    this.upCount--;
                    this.downCount++;
                }
                else{
                    this.likeLevel = 2;
                    this.downCount++;
                }
                query.postArticleLike(this.article_id, this.viewerId, this.likeLevel);
            },
            //开始发表评论
            doReply: function(){
                if(!this.viewerId){
                    return;
                }

                console.log('doReply');
                this.replyStatus = true;
            },
            //开始提交评论
            replyOnSend: function(txt){
                var self = this;

                var item = {
                    replyId: 0,
                    comment: txt,
                    articleId: this.article_id,
                    userId: this.viewerId,
                    createTime: new Date(),
                    userPicture: '',
                    userName: '',
                    replies: []
                };

                if(this.replyStatus){
                    this.replyStatus = false;

                    query.postArticleReply(item).then(function(body){
                        body.userPicture = self.readerPic;
                        body.userName = self.readerName;

                        self.alertOnShow(self.$root.lang.publishSucc);
                        self.reply_list.push(body);
                        setTimeout(function(){
                            var height1=document.documentElement.clientHeight;
                            var height2=document.body.clientHeight;
                            var height3=height1 || height2;
                            var height4=height2 || height1;
                            if(height4 > height3){
                                height3 = height4;
                            }

                            document.body.scrollTop= height3;
                        }, 500);

                    }, function(err){
                        console.log(err);
                    });
                };

                if(this.replyChildStatus){
                    this.replyChildStatus = false;

                    item.parentId = this.replyChildItem.replyId;

                    query.postArticleReplyChild(item).then(function(body){
                        if(body.code){
                            self.alertOnShow(self.$root.lang.replyGrammar);
                            return;
                        }


                        if(body.replyId == 0){
                            self.alertOnShow(self.$root.lang.replyDelete);
                            return;
                        }

                        if(!self.ownArticle){
                            body.userPicture = self.readerPic;
                            body.userName = self.readerName;
                        }

                        self.alertOnShow(self.$root.lang.replySucc);
                        self.replyChildItem.replies.push(body);

                        setTimeout(function(){
                            window.location.hash = "#child-" + body.replyId;
                        }, 500);

                    }, function(err){
                        console.log(err);
                    });



                }


            },
            //取消发表评论
            replyOnCancel: function(){
                if(this.replyStatus){
                    this.replyStatus = false;
                } else if(this.replyChildStatus) {
                    this.replyChildStatus = false;
                }
            },
            //开始发表回复
            replyChildOnBegin: function(param){
                if(!this.viewerId){
                    return;
                }
                console.log(param);

                this.replyChildItem = param;
                this.replyChildStatus = true;
            },
            //弹出提示
            alertOnShow: function(param){
                var self = this;

                this.alertTxt = param;
                this.alertDisplay = 'block';
                setTimeout(function(){
                    self.alertDisplay = 'none';
                }, 1000);
            },
            //取消
            confirmCancel: function(){
                var self = this;
                self.alertConfirm = 'none';
                self.currentChild = null;
                self.currentReply = null;
            },
            //确认
            confirmYes: function(param){
                var self = this;

                self.replyOnCancel();

                self.alertConfirm = 'none';


                var index = -1;
                var i;
                //删除回复
                if(self.currentChild) {
                    for(i = 0; i < self.currentReply.replies.length; i++){
                        var reply = self.currentReply.replies[i];
                        if(reply.replyId == self.currentChild.replyId){
                            index = i;
                            break;
                        }
                    }

                    if(index != -1){

                        query.deleteReplyChild(self.ownArticle, self.viewerId, self.currentChild.replyId, self.currentChild.parentId).then(function(body){
                            if(body){
                                self.alertOnShow(self.$root.lang.replyDeleteSucc);
                                self.currentReply.replies.splice(index);
                            } else {
                                self.alertOnShow(self.$root.lang.deleteError);
                            }
                        }, function(err){
                            self.alertOnShow(self.$root.lang.deleteError);
                            console.log(err);
                        })


                    }

                } else { //删除评论

                    for(i = self.reply_list.length -1; i >= 0; i--){
                        var reply = self.reply_list[i];
                        if(reply.replyId == self.currentReply.replyId){
                            index = i;
                            break;
                        }
                    }

                    if(index != -1){

                        query.deleteReply(self.ownArticle, self.viewerId, self.currentReply.replyId).then(function(body){
                            if(body){
                                self.alertOnShow(self.$root.lang.replyDeleteSucc);
                                self.reply_list.splice(i, 1);
                            } else {
                                self.alertOnShow(self.$root.lang.deleteError);
                            }
                        }, function(err){
                            self.alertOnShow(self.$root.lang.deleteError);
                            console.log(err);
                        })


                    }
                }

            },
            //开始准备删除回复或评论
            replyDeleteBegin: function(param){
                if(!this.viewerId){
                    return;
                }

                var self = this;
                //删除评论时候，child不存在
                self.currentChild = param.child;
                self.currentReply = param.reply;

                self.alertConfirm = 'block';
            },
            refreshData: function () {
                var self = this;
                this.focusResponse = "refresh" + self.viewerId + new Date();
         
                if (!self.viewerId) {
                    return;
                }
                query.getArticleInfo(self.article_id, self.viewerId).then(function (body) {
                    if (!body) {
                        return;
                    }

                    self.focusResponse = JSON.stringify(body);
                    self.circle_isFocused = body.ViewerFocused;
                })


            }
        }
    });


    taiyi_page_refresh = function(){

        app.refreshData();

    }


    return app;

});

