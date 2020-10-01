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
        ブロックチェーンにおける知的財産権の登録証明
        <span>Beta</span>
    </div>
    <fieldset>
        <legend data-name="RegisterTime"><fmt:formatDate value="${article.publishTime }" pattern="yyyy.MM.dd HH:mm:ss"></fmt:formatDate></legend>
    </fieldset>
    <div class="cer-number">
        <div class="cer-number-key">登録番号</div>
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
            作品のタイトル
        </div>
        <div class="works-name-val" data-name="MsgTitle">
			${article.title }
        </div>
        <div class="platform work-info-key">
            リリースプラットフォーム
        </div>
        <div class="work-info-val" data-name="PublicCircleName">
			${user.userName }
        </div>
        <div class="work-info-id">
            ID：<span data-name="PublicCircleID">${user.userName }</span>
        </div>
        <div class="work-info-key">
            著者
        </div>
        <div class="work-info-val" data-name="NikeName">
			${user.userName }
        </div>
        <div class="work-info-id">
            ID：<span data-name="UserID">${user.userName }</span>
        </div>
        <div class="work-info-key">
            著作権所有者
        </div>
        <div class="work-info-val" data-name="NikeName">
			${user.userName }
        </div>
        <div class="work-info-id">
            ID：<span data-name="UserID">${user.userName }</span>
        </div>
        <div class="statement">
            この証明書は、技術的なテストと概念証明のためのものであり、他の目的に使用することはできません
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