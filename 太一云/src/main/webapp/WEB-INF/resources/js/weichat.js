/**
 * Created by okdos on 2017/5/10.
 */

'use strict';

define(['exports', 'jweixin-1.0.0'], function(exports, wx){

    function share(body){
        wx.config({
            debug: false,
            appId: body.config.appId,
            timestamp: body.config.timestamp,
            nonceStr: body.config.nonceStr,
            signature: body.config.signature,
            jsApiList: [
                'checkJsApi',
                'onMenuShareTimeline',
                'onMenuShareAppMessage'
            ]
        });

        wx.ready(function () {
            var shareData = {
                title: body.title,
                desc: body.desc,
                link: body.link,
                imgUrl: body.imgUrl
            };
            wx.onMenuShareAppMessage(shareData);
            wx.onMenuShareTimeline(shareData);
        });

        wx.error(function (res) {
            //alert(res.errMsg);
        });
    }

    exports.share = share;

    
});