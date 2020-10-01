/**
 * Created by okdos on 2017/4/14.
 */

'use strict';

define(function(require, exports){

    exports.messagePrefix = "";

    //服务器地址，迁移到。net后，会变更为"/api"
    exports.askServer = "../api";
    //exports.askServer = "/api";

    //用户图像的相对路径
    //exports.headPath = "/files/HeadPicture/";

    //消息图片的绝对路径
    //exports.articleImagePath = "/MessagePush"

    exports.iosUrl = "https://itunes.apple.com/us/app/id1250322628";
    exports.androidUrl = "https://taiyiyun.com/mobile/pub";

    exports.internalUrl = "https://taiyipassport/api";
});