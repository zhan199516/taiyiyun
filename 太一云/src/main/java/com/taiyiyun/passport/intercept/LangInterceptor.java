package com.taiyiyun.passport.intercept;

import com.taiyiyun.passport.language.CurrentPackBundle;
import com.taiyiyun.passport.language.LangResource;
import com.taiyiyun.passport.language.PackBundle;
import com.taiyiyun.passport.util.RequestUtil;
import com.taiyiyun.passport.util.StringUtil;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

public class LangInterceptor extends HandlerInterceptorAdapter {

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
		CurrentPackBundle.remove();
		super.postHandle(request, response, handler, modelAndView);
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		String langName = request.getHeader(LangResource.LANG_NAME);

		if(StringUtil.isEmpty(langName)){
			String str = request.getQueryString();
			if(!StringUtil.isEmpty(str)){
				HashMap<String, String> map = RequestUtil.parseQueryString(str);
				if(map.containsKey(LangResource.LANG_NAME)){
					langName =  map.get(LangResource.LANG_NAME);
				}
			}
		}

		PackBundle resourceBundle = LangResource.getInstance().getResourceBundle(langName);
		CurrentPackBundle.set(resourceBundle);
		request.setAttribute(LangResource.LANG_NAME, resourceBundle);

		return super.preHandle(request, response, handler);
	}
	
}
