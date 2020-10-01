<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%
    String path = request.getContextPath();
    String basePath = request.getScheme() + "://"
            + request.getServerName() + ":" + request.getServerPort()
            + path + "/";
%>
<html>
<head>
	<meta charset="UTF-8">
	 <meta name="viewport"
          content="width=device-width, user-scalable=no, initial-scale=1, maximum-scale=1, minimum-scale=1">
	 <link rel="stylesheet" type="text/css" href="<%=basePath%>/invitation/css/wepassh5.css?v=1.1.1">
	 <script type="text/javascript" src="<%=basePath%>/invitation/js/flexible.js"></script>
	 <script type="text/javascript" src="<%=basePath%>/invitation/js/flexible_css.js"></script>
	<title>三重好礼等你拿</title>
</head>
<body>
<div class="wrap">
	<div class="padd_center">
		<div class="banner_1">
			<h1>邀请好友领链橙</h1>
			<h2>还有积分可以拿</h2>
			<p>邀请多多，奖励多多</p>
			<div class="code_text">链橙微信公众号</div>
		</div>
	</div>
	<div class="tit">邀请好友领“链橙”</div>
	<div class="padd_center">
		<div class="introduce_box">
			<div class="banner_2"><img src="<%=basePath%>/invitation/images/banner_2.png"></div>
			<h1>完成实名认证，并成功邀请10名用户注册，
即可获得链橙一箱。
包邮哦！十万箱链橙疯狂送，先来先得，送完为止。</h1>
			<div class="orangeImg"><img src="<%=basePath%>/invitation/images/orange.png"></div>
			<p>“链橙”是由中华思源工程基金会大数据公益基金、赣州新链金融信息有限公司和链橙运营商在命老区江西赣州联合推出的中国首家“区块链技+食品”的系统应用品牌，亦是中国食品工业（集团）公司和北京太一云科技有限公司战略合作发起的“中国食品链”首个实验项目。</p>
		</div>
	</div>
	<div class="tit">扫码“链橙”领积分</div>
	<div class="padd_center">
		<div class="identify_box">
			<div class="topText">
				<p>每个橙子都有身份标识，扫码得积分
更有机会兑换神秘大奖！</p>
			</div>
			<div class="orangeMaximg">
				<h3>一橙一码</h3>
				<img src="<%=basePath%>/invitation/images/orange_max.png">
			</div>
		</div>
		<div class="info">
		    <c:if test="${param.IUserCount == 0}">
			<h1 class="num_one">点击右上角，分享出去吧！<span><img src="<%=basePath%>/invitation/images/arrows.png"></span></h1>
			</c:if>
			<c:if test="${param.IUserCount > 0 && param.IUserCount < 10}">
			<h1 class="num_two">您已分享了<i>${param.IUserCount}</i>名用户，加油哦！<span><img src="<%=basePath%>/invitation/images/arrows.png"></span></h1>
			</c:if>
			<c:if test="${param.IUserCount >= 10  && param.Authentication > 0}">
			<h1 class="num_three">恭喜您，完成目标！请输入您的地址，
我们将第一时间给您发送橙子！</h1>
			<!-- 第三步显示form_div -->
			<div class="form_div">
				<form action="">
					<p><input style="margin-top: 0" type="text" placeholder="请输入详细地址" name="Address"></p>
					<p class="tel">
						<input type="tel" placeholder="请输入电话" name="mobile">
						<span class="hint">请输入正确的手机号</span>
					</p>

					<p><input type="text" placeholder="请输入姓名" name="UserName"></p>
					<p class="p_submit"><input type="button" value="提交" name=""></p>
				</form>

			</div>
			<h1 class="finish">感谢您支持共享护照</h1>
			</c:if>
			 <c:if test="${param.IUserCount >= 10  && param.Authentication < 1}">
			            您还未完成实名认证，还不能领哦！
			 </c:if>
		</div>
	</div>
</div>

<div class="black_bg"></div>
<!-- <div class="pop_up pop_up1">
	<div class="pic_div_box">
		<img src="images/alertImg.png">
		<p>感谢您支持共享护照</p>
	</div>
	<div class="close"><img src="images/close.png"></div>
</div> -->
<div class="prompt">
	<h1>手机号码已注册</h1>
	<a href="javascript:">ok</a>
</div>

<script type="text/javascript" src="<%=basePath%>/invitation/js/jquery.min.js"></script>
<script type="text/javascript">
	$(function(){
		/*var Address = $("input[name=Address]"),
			mobile = $("input[name=mobile]"),
			UserName = $("input[name=UserName]");
			console.log(Address);*/
			/*if(Address && mobile && UserName){
				alert(1)
			}*/
			/*Address.on("keydown",function(){
				console.log(mobile.val())
				if(mobile.val()!="" && UserName.val()!=""){
					$(".disabled").removeClass('disabled').removeAttr('disabled')
				}if(){

				}
			})
			mobile.on("keydown",function(){
				if(Address.val()!="" && UserName.val()!=""){
					$(".disabled").removeClass('disabled').removeAttr('disabled')
				}
			})
			UserName.on("keydown",function(){
				if(Address.val()!="" && mobile.val()!=""){
					$(".disabled").removeClass('disabled').removeAttr('disabled')
				}
			})*/
		$(".p_submit input").on('click',function(){
			$.ajax({
				url:'<%=basePath%>/api/invitation/addDeliveryAddress',
				type:"post",
				dataTpye:'json',
				data:{
					Mobile: $("input[name=mobile]").val(),
					UserAddress: $("input[name=Address]").val(),
					UserName: $("input[name=UserName]").val(),
					InvitationId: "${param.InvitationId}",
					InvitationUserId: "${param.InvitationUserId}"
				},
				success:function(json){
					if (json.success) {
						$(".finish").show()
					}else{
						$.alert(json.error)
					}					
				},
				error:function(xhr, textStatus, errorThrown){
					/*if (xhr.responseJSON.success) {
						$(".black_bg").show()
						$(".pop_up").show()
					} else {

					}*/
					$.alert("请求失败");
				}
			})
		})

		function test_tel(){
	        var reg1=/^1[0-9]{10}$/;
	        if($(".tel input").val()!=""){
	            if(!reg1.test($(".tel input").val())){
	                $(".hint").css("display","block");

	            }else if(reg1.test($(".tel input").val())){
	            	
	                $(".hint").css("display","none");	      
	            }
        	}
		}
		$(".tel input").on("blur",function(){
			test_tel()
		})
		$(".close").on("click",function(){
			$(".black_bg").hide()
			$(".pop_up").hide()
		})
		$.alert = function(text) {			
			$(".prompt").show().find("h1").empty().text(text);
		}
		
		$("a", ".prompt").on("click", function(){
			$(".prompt").hide();
		});

	})
</script>
</body>
</html>