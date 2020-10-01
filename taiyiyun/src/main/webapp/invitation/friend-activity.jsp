<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%
    String path = request.getContextPath();
    String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + path + "/";
%>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		 <meta name="viewport" content="width=device-width, user-scalable=no, initial-scale=1, maximum-scale=1, minimum-scale=1">
		 <link rel="stylesheet" type="text/css" href="<%=basePath%>/invitation/css/wepassh5.css?v=1.1.1">
		 <script type="text/javascript" src="<%=basePath%>/invitation/js/flexible.js"></script>
		 <script type="text/javascript" src="<%=basePath%>/invitation/js/flexible_css.js"></script>
		<title>三重好礼等你拿</title>
	</head>
	<body>
<div class="wrap" style="padding-bottom: 2rem">
	<div class="padd_center">
		<div class="banner_1">
			<h3>共享护照  年末贺岁</h3>
			<h3>三重好礼等你拿！</h3>
			<div class="code_text">链橙微信公众号</div>
		</div>
	</div>
	<div class="tit">第一重：注册即送大大大礼包</div>
	<div class="padd_center">
		<div class="friend_box">
			<div class="top">
				<h4>下载并注册共享护照即可获得大红包，还有意外惊喜等着你！</h4>
			</div>
			<div class="Imgbox"><img src="<%=basePath%>/invitation/images/banner_3.png"></div>
		</div>
	</div>
	<div class="tit">第二重：邀请好友领“链橙”</div>
	<div class="padd_center">
		<div class="introduce_box">
			<div class="banner_2"><img src="<%=basePath%>/invitation/images/banner_2.png"></div>
			<h1>完成实名认证，并成功邀请10名用户注册，
即可获得链橙一箱。
包邮哦！十万箱链橙疯狂送，先来先得，送完为止。</h1>
			<div class="orangeImg"><img src="<%=basePath%>/invitation/images/orange.png"></div>
			<p>“链橙”是由中华思源工程基金会大数据公益基金、赣州新链金融信息有限公司和链橙运营商在革命老区江西赣州联合推出的中国首家“区块链技+食品”的系统应用品牌，亦是中国食品工业（集团）公司和北京太一云科技有限公司战略合作发起的“中国食品链”首个实验项目。</p>
		</div>
	</div>
	<div class="tit">第三重：扫码“链橙”领积分</div>
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
			<h1 class="num_one num_one_friend">注册完成!</h1>
			<div class="form_div_friend">
				<form class="form_1">					
					<p class="tel" id="tel">
						<input type="tel" placeholder="请输入手机号" name="Mobile">
						<span class="hint hint1">请输入正确的手机号</span>
					</p>
					<p class="p_submit p_button"><input class=""  type="button" value="注册赢取三重大礼" name=""></p>
				</form>
				<form class="form_2">					
					<p class="verify">
						<input type="text" placeholder="输入验证码" name="txt"><input type="button" value="重新发送" name="btn" id="button_cod">
						<!-- <span class="hint">请输入正确的手机号</span> -->
					</p>
					<p class="passWord">
						<input type="password" placeholder="请输入密码（6-20位，同时包含字母，数字，区分大小写）" name="password">
						<span class="hint hint_pasword">请输入正确的密码</span>
					</p>
					<p class="p_submit Btn"><input type="button" value="完成注册" name=""></p>
				</form>
			</div>
			
		</div>
	</div>
</div>
<div class="black_bg"></div>
<div class="pop_up">
	<div class="pic_div">
		<h3>恭喜您</h3>
		<h4>一个大红包已经存入你的钱包</h4>
		<p><a href="javascript:" class="down-now">点击下载APP，使用红包</a></p>
	</div>
	<div class="close"><img src="<%=basePath%>/invitation/images/close.png"></div>
</div>
<div class="prompt">
	<h1>手机号码已注册</h1>
	<a href="javascript:">ok</a>
</div>
<div class="download clear">
    <div class="logo left"></div>
    <div class="down-msg left">
        <div class="large">共享护照APP全新上线</div>
        <div class="small">iOS 和 Android 系统</div>
    </div>
    <a class="down-now right" href="javascript:;">立即下载</a>
</div>
<!-- <div class="weixin-tip">
    <img src="images/weixin-tip.jpg" alt="">
</div> -->
<div class="weixin-bg">
	<div class="weixin-tip" style="display: block;">
	    <img src="<%=basePath%>/invitation/images/open-in-browser.png" alt="">
	</div>
</div>
<script type="text/javascript" src="<%=basePath%>/invitation/js/jquery.min.js"></script>
<!-- <script type="text/javascript">  
        $(window).on("load",function(){
            var winHeight = $(window).height();
            function is_weixin() {
                var ua = navigator.userAgent.toLowerCase();
                if (ua.match(/MicroMessenger/i) == "micromessenger") {
                    return true;
                } else {
                    return false;
                }
            }
            var isWeixin = is_weixin();
            if(isWeixin){
                $(".weixin-tip").show();
                $(".wrap").css("padding-top","2.366667rem")
            }
        })
    </script> -->
<script type="text/javascript">
	$(function(){
		function isWeiXin(){
	        var ua = window.navigator.userAgent.toLowerCase();
	        if(ua.match(/MicroMessenger/i) == 'micromessenger'){
	            return true;
	        }else{
	            return false;
	        }
	    }
		$(".down-now").click(function () {
		    if (/(iPhone|iPad|iPod|iOS)/i.test(navigator.userAgent)) {
		        window.location.href ="https://itunes.apple.com/cn/app/id1250322628";
		    } else if (/(Android)/i.test(navigator.userAgent)) {
		    	if(isWeiXin()){
	                $(".weixin-bg").show();
	                return;
	            }
		    	 window.location.href ="<%=basePath%>/invitation/download/wepass_latest.apk";
		    }
		});
		$(".weixin-bg").click(function(){
			$(this).hide()
		})
		function test_tel(){
	        var reg1=/^1[0-9]{10}$/;
	        if($(".tel input").val()!=""){
	        	$(".p_button input").removeAttr("disabled");
	            if(!reg1.test($(".tel input").val())){
	                $(".hint1").css("display","block");

	            }else if(reg1.test($(".tel input").val())){
	                $(".hint1").css("display","none");
	            }
        	}
		};
		
		$(".tel input").on("blur",function(){
			test_tel()
			
		});
		
		$(".close").on("click",function(){
			$(".black_bg").hide()
			$(".pop_up").hide()
			$(".form_1").hide();
			$(".form_2").hide();
			$(".num_one_friend").show()
			$(".Btn input").removeAttr("disabled")
		});
		
		function test_password(){
	        var reg=/^[0-9a-zA-Z]{6,20}$/;
	        if(reg.test($(".passWord input").val())){
	            $(".hint_pasword").css("display","none");
	        }else{
	            $(".hint_pasword").css("display","block");
	        }
	    };
	    
	    $(".passWord input").on("blur",function(){
			test_password()
		});
	    
	    var timer = undefined;
		$(".verify>input[type=button]").on("timer.disabled", function() {
			var _this = $(this);
			_this.attr('disabled','disabled')
			var num = 60;
			if (timer) {
				clearInterval(timer);
			}
			timer = setInterval(function(){
				num=parseInt(num);
				num--;
				num+='s';
				_this.val(num)
				if (num=='0s') {
					clearInterval(timer);
					_this.val('重新发送')
					_this.removeAttr('disabled')
				}
			},1000);
		});
		
		$(".verify>input[type=button]").click(function() {
			var ret = sendSMScode();
			if (ret) {
				$(this).trigger("timer.disabled");
			}
		});

	    $(".p_button input").on("touchend",function(){
	    	$(this).removeClass('onTouchstart')
	    })
	    $(".p_button input").on("touchstart",function(){
	    	$(this).addClass('onTouchstart')
	    })


		$(".p_button input").on("click",function(){
			/*$(this).addClass('disabled_bg')*/
			var ret = sendSMScode()
			if(ret) {
				$(".form_1").hide();
				$(".form_2").show();
				$(this).attr("disabled", true);
				$(".verify>input[type=button]").trigger("timer.disabled");
				$("input[name=Mobile]").data("oldValue", $("input[name=Mobile]").val());
			}
		});
		
		$("input[name=Mobile]").on("change", function() {
			var oldValue = $(this).data("oldValue");
			if ($.trim(oldValue).length > 0 && $(this).val() === $.trim(oldValue)) {
				$(".p_button input").attr("disabled", true);
			} else if ($.trim(oldValue).length == 11 && $.trim(oldValue) != $(this).val() && /^[0-9a-zA-Z]{6,20}$/.test($(this).val())) {
				$(".p_button input").removeAttr("disabled");
			}
		});
		
		function sendSMScode(){
			var ret = false;
			var mobile = $("#tel input").val()
			if (!/^[0-9a-zA-Z]{6,20}$/.test(mobile)) {
				return;
			}
			$.ajax({
				url:"<%=basePath%>/api/invitation/SMSVerifyCode?Mobile=" + mobile,
				/*url:"https://api.wechain.im/api/invitation/SMSVerifyCode?Mobile=" + mobile,*/
				type:"get",
				dataType:'json',
				async:false,
				success:function(json){
					if (!json.success) {
						var error = json.error;
						if (error.indexOf("code") > -1 && error.indexOf("33") > -1) {
							$.alert("验证码已经发送到您的手机号");
						} else {
							$.alert(error);
						}
					} else {
						ret = true;
					}
				},
				error:function(){
					$.alert("请求失败");
				}
			});
			return ret;
		};
		$(".Btn input").on("touchend",function(){
	    	$(this).removeClass('onTouchstart')
	    })
	    $(".Btn input").on("touchstart",function(){
	    	$(this).addClass('onTouchstart')
	    })
		$(".Btn").on("click","input",function(){
			$(this).attr("disabled","disabled")
			$.ajax({
				/*url:"https://api.wechain.im/api/invitation/inviteUsersRegister",*/
				url:"<%=basePath%>/api/invitation/inviteUsersRegister",
				type:"post",
				dataType:"json",
				data:{
					Mobile:$("input[name=Mobile]").val(),
					VerifyCode: $("input[name=txt]").val(),
					Password: $("input[name=password]").val(),
					InvitationId:  "${param.InvitationId}",
					InvitationUserId:  "${param.InvitationUserId}"
				},
				success:function(json){
					if (json.status==0) {
						$(".black_bg").show()
						$(".pop_up").show()
					}else{
						$.alert(json.error)
					}	
				},
				error:function(xhr, textStatus, errorThrown){
					$.alert("请求失败")
				}

			})
		});
		
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