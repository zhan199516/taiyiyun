package com.taiyiyun.passport.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.taiyiyun.passport.bean.UserDetails;
import com.taiyiyun.passport.consts.Const;
import com.taiyiyun.passport.service.session.LoginInfo;

import java.util.Date;

public class SessionUtil {
	
	public static final void add(HttpServletRequest request, String key, Object value){
		request.getSession().setAttribute(key, value);
	}
	
	public static final void remove(HttpServletRequest request, String key) {
		request.getSession().removeAttribute(key);
	}
	
	public static final Object get(HttpServletRequest request, String key) {
		return request.getSession().getAttribute(key);
	}
	
	public static final UserDetails getUserDetails(HttpServletRequest request){
		return (UserDetails)get(request,Const.SESSION_SHARE_NUM);
	}
	
	public static final void addUserDetails(HttpServletRequest request, UserDetails userDetails) {
		remove(request, Const.SESSION_SHARE_NUM);
		add(request, Const.SESSION_SHARE_NUM, userDetails);
	}

	public static void setLoginInfo(HttpServletRequest request, LoginInfo loginInfo){
		remove(request, Const.SESSION_LOGIN);
		add(request, Const.SESSION_LOGIN, loginInfo);
	}

	public static LoginInfo getLoginInfo(HttpServletRequest request){
		return (LoginInfo)get(request, Const.SESSION_LOGIN);
	}
	
	public static final void removeUserDetails(HttpServletRequest request) {
		remove(request, Const.SESSION_SHARE_NUM);
	}
	
	public static final HttpSession getCurrentSession(HttpServletRequest request) {
		return request.getSession();
	}

}
