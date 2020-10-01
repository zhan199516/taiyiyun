/**
 * Created by admin on 2017/4/17.
 */
var winW=document.documentElement.clientWidth;
var desW=750;
var scale=750/100;
document.documentElement.style.fontSize=winW/scale+"px";
window.onresize=function(){
    var winW=document.documentElement.clientWidth;
    document.documentElement.style.fontSize=winW/scale+"px";
}