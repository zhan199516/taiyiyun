<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, user-scalable=no">
    <meta content="telephone=no" name="format-detection" />
    <title>证书</title>
    <link rel="stylesheet" href="/resources/css/certificate.css?v=1">
    <script src="/resources/js/public.js"></script>
</head>
<body class="cer-body">
<div class="section-top">
    <div class="cer-name">
        區塊鏈知識產權登記證明
        <span>Beta</span>
    </div>
    <fieldset>
        <legend data-name="RegisterTime"><fmt:formatDate value="${article.publishTime }" pattern="yyyy.MM.dd HH:mm:ss"></fmt:formatDate></legend>
    </fieldset>
    <div class="cer-number">
        <div class="cer-number-key">登記號</div>
        <div class="cer-number-value" data-name="RegisterCoder">${article.registerNo }</div>
    </div>
</div>
<div class="section-bottom">
    <div class="hash clear">
        <img src="/resources/images/hash.png" alt="" class="left">
        <div class="left" data-name="ArticleHash">
			${article.articleHash }
        </div>
    </div>
    <div class="works-info">
        <div class="works-name-key">
            作品名稱
        </div>
        <div class="works-name-val" data-name="MsgTitle">
			${article.title }
        </div>
        <div class="platform work-info-key">
            發布平台
        </div>
        <div class="work-info-val" data-name="PublicCircleName">
			${user.userName }
        </div>
        <div class="work-info-id">
            ID：<span data-name="PublicCircleID">${user.userName }</span>
        </div>
        <div class="work-info-key">
            作者
        </div>
        <div class="work-info-val" data-name="NikeName">
			${user.userName }
        </div>
        <div class="work-info-id">
            ID：<span data-name="UserID">${user.userName }</span>
        </div>
        <div class="work-info-key">
            著作權人
        </div>
        <div class="work-info-val" data-name="NikeName">
			${user.userName }
        </div>
        <div class="work-info-id">
            ID：<span data-name="UserID">${user.userName }</span>
        </div>
        <div class="statement">
            本證明僅用於技術測試和概念證明，不得用於任何其它用途。
        </div>
    </div>
</div>

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