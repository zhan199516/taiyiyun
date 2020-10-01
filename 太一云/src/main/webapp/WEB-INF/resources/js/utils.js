
'use strict';

define(['exports', 'config'], function(exports, config){

    exports.formatMinuteOrDate = function(timeNumber, lang){
        if(!timeNumber){
            return "";
        }

        var now = new Date();
        var nowTime = now.getTime();

        var dateTime = new Date(timeNumber);
        var thenTime = dateTime.getTime();

        var y = dateTime.getFullYear();
        var m = dateTime.getMonth()+1;
        var d = dateTime.getDate();
        var h = dateTime.getHours();
        var mm = dateTime.getMinutes();
        var ss = dateTime.getSeconds();

        var distance = nowTime - thenTime;

        if(distance >= 365 * 24 * 60 * 60 * 1000){
            return y + '-' + (m<10?('0'+m):m);
        }
        else if(distance >= 24 * 60 * 60 * 1000){
            if(y != now.getFullYear()){
                return lang.lastYear+(m<10?('0'+m):m)+'-'+(d<10?('0'+d):d);
            } else {
                return (m<10?('0'+m):m)+'-'+(d<10?('0'+d):d);
            }
        }
        else if(d != now.getDate()){
            return lang.yesterday + (h<10?('0'+h):h) + ':' + (mm<10?('0'+mm):mm);
        }
        else {
            return (h<10?('0'+h):h) + ':' + (mm<10?('0'+mm):mm) + ":" + (ss<10?('0'+ss):ss);
        }
    }

    exports.formatDateTime = function(timeNumber){
        if(!timeNumber){
            return "";
        }

        var dateTime = new Date(timeNumber);
        var thenTime = dateTime.getTime();

        var y = dateTime.getFullYear();
        var m = dateTime.getMonth()+1;
        var d = dateTime.getDate();
        var h = dateTime.getHours();
        var mm = dateTime.getMinutes();
        var ss = dateTime.getSeconds();

        return y + '-' + (m<10?('0'+m):m)+'-'+(d<10?('0'+d):d) + ' ' + (h<10?('0'+h):h) + ':' + (mm<10?('0'+mm):mm) + ":" + (ss<10?('0'+ss):ss);
    }

    exports.convertHtml = function(html){
        if(html){
            //html = html.replace(/<script/g, '<div').replace(/<p>/g, '<p class="content">');
            //html = html.replace(/(<p)\s(style)/g, '$1 class="content" $2');
            //html = html.replace(/(<img\s.+?src=")(.+?)(".+?\/>)/g, '$1' + config.articleImagePath + '$2$3');
            return html;
        } else {
            return "";
        }
    }


    exports.reconvert = function(str){
        str = str.replace(/(\\u)(\w{1,4})/gi,function($0){
            return (String.fromCharCode(parseInt((encodeURIComponent($0).replace(/(%5Cu)(\w{1,4})/g,"$2")),16)));
        });
        str = str.replace(/(&#x)(\w{1,4});/gi,function($0){
            return String.fromCharCode(parseInt(encodeURIComponent($0).replace(/(%26%23x)(\w{1,4})(%3B)/g,"$2"),16));
        });
        str = str.replace(/(&#)(\d{1,6});/gi,function($0){
            return String.fromCharCode(parseInt(encodeURIComponent($0).replace(/(%26%23)(\d{1,6})(%3B)/g,"$2")));
        });

        return str;
    }

});