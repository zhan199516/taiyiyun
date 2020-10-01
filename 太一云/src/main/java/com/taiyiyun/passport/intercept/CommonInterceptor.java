package com.taiyiyun.passport.intercept;

import com.alibaba.fastjson.JSON;
import com.taiyiyun.passport.bean.UserDetails;
import com.taiyiyun.passport.exception.DefinedError;
import com.taiyiyun.passport.service.session.INotify;
import com.taiyiyun.passport.service.session.LoginInfo;
import com.taiyiyun.passport.service.session.MobileSessionCache;
import com.taiyiyun.passport.util.DateUtil;
import com.taiyiyun.passport.util.SessionUtil;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class CommonInterceptor extends HandlerInterceptorAdapter {

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
		super.afterCompletion(request, response, handler, ex);
	}

	@Override
	public void afterConcurrentHandlingStarted(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		super.afterConcurrentHandlingStarted(request, response, handler);
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
		super.postHandle(request, response, handler, modelAndView);
		if (null != modelAndView && null != modelAndView.getModel()) {
			String portStr = (80 == request.getServerPort()) ? "" : ":" + request.getServerPort();
			modelAndView.getModel().put("basePath", "//" + request.getServerName() + portStr + request.getContextPath() + "/");
		}
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

//		String langName = request.getHeader("lang.name");
//		ResourceBundle resourceBundle = LangResource.getInstance().getResourceBundle(langName);
//		request.setAttribute("lang.name", resourceBundle);


		UserDetails userDetails = SessionUtil.getUserDetails(request);

		if(null == userDetails) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);

			return false;
		}


		//踢人暂时去掉
		final List<LoginInfo> loginInfos = new ArrayList<>();

		//缓存session
		if(!MobileSessionCache.getInstance().isValidSession(request.getSession(), false, new INotify() {
			@Override
			public void callback(LoginInfo cache) {
				//被挤掉的时候，回调
				loginInfos.add(cache);
			}
		})){

			if(loginInfos.size() > 0){
				LoginInfo loginInfo = loginInfos.get(0);

				response.setStatus(HttpServletResponse.SC_OK);
				response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);

				HashMap<String, Object> map = new HashMap<>();
				map.put("status", DefinedError.Status.FORCE_OUT.getValue());
				Date loginTime = new Date(loginInfo.getLoginTime());
				String alert = "";
				if(loginInfo.getStatus() == DefinedError.Status.SUCC.getValue()){
					alert = "您的账户在" + DateUtil.toString(loginTime, DateUtil.Format.DATE_TIME_HYPHEN) + "使用" + loginInfo.getDeviceName() + "刚刚登录。"
							+ "如果非您本人操作，请尽快更改您的密码。";
				} else if(loginInfo.getStatus() == DefinedError.Status.USER_PASSWORD_CHANGED.getValue()){
					alert = "您的账户密码已经被修改，您已经被强制退出";
				} else {
					alert = "您的账户已经在其它设备上登录，您已经被强制退出";
				}

				map.put("error", alert);
				String rst = JSON.toJSONString(map);

				PrintWriter out = null;
				try {
					out = response.getWriter();
					out.append(rst);
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					if (out != null) {
						out.close();
					}
				}

				return false;
			} else {
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				return false;
			}
		}

		return super.preHandle(request, response, handler);
	}

}
