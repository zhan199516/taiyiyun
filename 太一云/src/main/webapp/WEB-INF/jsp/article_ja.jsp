<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.io.*,java.util.*" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, user-scalable=no">
    <meta name="format-detection" content="telephone=no"/>
    <link rel="icon" href="favicon.png" type="../resources/image/png"/>
    <title>${fn:escapeXml(data_title)}</title>
    <link rel="stylesheet" href="../resources/css/delete.css?v=0.1">
    <script src="../resources/js/public.js"></script>
</head>
<body>

<div id="app" class="default-box" >

    <div class="hid" v-base-param
        data-article_id='<%= request.getAttribute("data_articleId") %>'
        data-title='${fn:escapeXml(data_title)}'
        data-abstract='${fn:escapeXml(data_abstract)}'
        data-url='<%= request.getAttribute("data_url") %>'
        data-vid='<%= request.getAttribute("data_vid") %>'
        data-vname='<%= request.getAttribute("data_vname") %>'
        data-vpic='<%= request.getAttribute("data_vpic") %>'
        data-owner='<%= request.getAttribute("data_owner") %>'
        data-focused='<%= request.getAttribute("data_focused") %>'

        data-original='<%= request.getAttribute("data_original") %>'
        data-from='<%= request.getAttribute("data_from") %>'
    ></div>

    <template v-if="showEntrance">
        <div class="pos-top ">
            <div class="center" style="background: none">
                <dl>
                    <dt><img src="../resources/images/taiyi.png?ver=1" alt=""></dt>
                    <dd>
                        <h3>SharePass</h3>
                        <p>The block chain world entrance</p>
                    </dd>
                </dl>
                <div class="pos-top-r">
                    <input type="button" value="開く" class="hid">
                    <a :href="urlEntrance" ><div>開く</div></a>
                    <span id="close" @click="showEntrance= false"></span>
                </div>
            </div>
        </div>
    </template>


    <div class="center">
        <div class="source-box clear" :style='{"padding-top": showEntrance? "50px" : "10px"}'>
            <div class="top left">
                <a :href="circlePage"><img class="avatar left" name="circle_logo" src=<%= request.getAttribute("user_head") %> alt="" @error="logoError"></a>
                <ul class="left">
                    <li class="name"><span name="circle_name"><%= request.getAttribute("user_name") %></span>&nbsp;<span class="time" name="publishTime"><%= request.getAttribute("update_time") %></span></li>
                    <li class="original"><span v-text="isOriginal == 0 ? 'オリジナル':'転載'"></span>&nbsp;<span v-text="forwardFrom"></span></li>
                </ul>
                <div class="original right">
                    <template v-if="ver == 2 && viewerId && circle_isFocused != 2">
                        <template v-if="circle_isFocused == 0">
                            <span class="focus-before" @click="focusCircle">+注意</span>
                        </template>
                        <template v-else>
                            <span >懸念されている</span>
                        </template>
                    </template>
                    <template v-if="ver == 2  && viewerId && isOriginal">
                        <a :href="certPage"><span class="article-cert">記事の証明書</span></a>
                    </template>
                </div>
            </div>
        </div>

        <!--<button @click="refreshData">刷新</button>-->
        <!--<p class="content">关注返回值：<span v-text="focusResponse"></span></p>-->

        <h3 class="title" name="articleTitle" v-title>${fn:escapeXml(data_title)}</h3>

        <div id="mainHtml" class="content" :class='{"hid": replyChildStatus}' ><%= request.getAttribute("mainHtml") %></div>
        <div class="hid" v-article></div>
    </div>

    <div class="cover" :style="{display: alertConfirm}">
        <div class="tip-box" :class="{animate: alertConfirm != 'none'}">
            <h6>注意</h6>
            <p>コメント削除後はコメントリストに表示されません</p>
            <div class="button-box">
                <button class="cancel-btn" @click="confirmCancel">キャンセル</button>
                <button class="delete-btn" @click="confirmYes">削除済み</button>
            </div>
        </div>
    </div>



    <div class="task" :class="{'task-block': alertDisplay != 'none'}" v-text="alertTxt" ></div>

    <ul class="operation-list clear" :class='{"hid": replyChildStatus}'>
        <li class="view">読む&nbsp;<span v-text="readCount"></span></li>
        <li class="share">シェア&nbsp;<span v-text="forwardCount"></span></li>
        <li :class="{up: likeLevel!=1, upped: likeLevel==1, cursor: viewerId}" @click="doUp"><span v-text="upCount"></span></li>
        <li :class="{down: likeLevel!=2, downed: likeLevel==2, cursor: viewerId}" @click="doDown"><span v-text="downCount"></span></li>
    </ul>

    <div class="comment-list">
        <div class="comment-box clear" :class='{"hid": replyChildStatus}'>
            <div class="comment-top clear">
                <span class="left comment-number">コメント <span v-text="replyCount"></span></span>
                <span v-if="viewerId && !ownArticle" class="right write-comment" @click="doReply">コメントを書く</span>
            </div>
        </div>
        <div v-for="item in reply_list" :class='{"hid": replyStatus }'>
            <reply-item :viewer-id="viewerId" :reader-name="readerName" :item="item" :own-article="ownArticle" :owner-id="ownerId" :comment-width="commentWidth"
                        v-on:on-reply-child="replyChildOnBegin" v-on:on-delete="replyDeleteBegin"
                        :class='{"hid": replyChildStatus && (replyChildItem.Id != item.Id)}'></reply-item>
        </div>
    </div>


    <reply-input :class='{"hid": !replyStatus }' v-bind:is-input="replyStatus" v-on:on-send ="replyOnSend" v-on:on-cancel="replyOnCancel" v-on:on-alert="alertOnShow"></reply-input>
    <reply-input :class='{"hid": !replyChildStatus }' v-bind:is-input="replyChildStatus" v-on:on-send ="replyOnSend" v-on:on-cancel="replyOnCancel" v-on:on-alert="alertOnShow"></reply-input>
</div>



<script type="text/x-template" id="imgHeadPic">
    <div>
        <template v-if="loadError">
            <img :src="defaultPath" class="comment-head left">
        </template>
        <template v-else>
            <img :src="imgPath" alt="" @error="onImgError" class="comment-head left">
        </template>
    </div>
</script>

<script type="text/x-template" id="replyItem">
    <div>
        <div class="comment-box pad-top10 clear">
            <img-dom :img-path="item.userPicture" default-path="../resources/images/head.png" ></img-dom>
            <div class="comment-content left" :style="{width: commentWidth}">
                <div class="name-time clear">
                    <div class="left" v-text="item.userName"></div>
                    <div class="right" v-text="createTimeFormat">12:34:05</div>
                </div>
                <div class="write-content clear">
                    <div v-for="s in comments" v-text="s"></div>
                    <div>
                        <span class="right delete" v-if="canDelete" @click="deleteMe">削除済み</span>
                    </div>
                </div>
                <div>
                    <reply-subitem v-for="rpl in item.replies" v-bind:item="rpl" v-bind:viewer-id="viewerId" :owner-id="ownerId" v-bind:own-article="ownArticle" v-on:delete-me="deleteItem"></reply-subitem>
                </div>
                <div><span class="reply-btn right" v-if="canReply" @click="reply">返信</span></div>
                <div></div>
            </div>
        </div>
    </div>
</script>

<script type="text/x-template" id="replySubitem">
    <div class="reply-box clear">
        <div id="child-19">
            <span class="reply-name" v-text="replyNameFormat"></span>
            <span class="right" v-text="createTimeFormat">12:53:06</span>
            <div v-for="str in comments" v-text="str"></div>
        </div>
        <span class="right delete" v-if="canDelete" @click="deleteMe">削除済み</span>
    </div>
</script>

<script type="text/x-template" id="replyInput">
    <div>
        <div class="write">
            <textarea placeholder="コメントは誰にでも見える" v-model="replyInput" ref="replyDom"></textarea>
            <button @click="send">送信</button>
            <button class="back write-back" @click="cancel">戻る</button>
        </div>
    </div>
</script>

<script src="../resources/js/vue.min.js"></script>
<script src="../resources/js/vue-resource.min.js"></script>
<script data-main="../resources/js/articleMain_ja" src="../resources/js/require.js"></script>

<script>
    require.config({
        urlArgs: 'v=0.7'
    });
</script>

<script>
    var _mtac = {};
    (function() {
        var mta = document.createElement("script");
        mta.src = "http://pingjs.qq.com/h5/stats.js?v2.0.4";
        mta.setAttribute("name", "MTAH5");
        mta.setAttribute("sid", "500528731");

        var s = document.getElementsByTagName("script")[0];
        s.parentNode.insertBefore(mta, s);
    })();
</script>

</body>
</html>