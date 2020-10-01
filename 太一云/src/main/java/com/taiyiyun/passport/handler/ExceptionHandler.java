package com.taiyiyun.passport.handler;

import com.alibaba.fastjson.JSON;
import com.taiyiyun.passport.bean.ErrorMessageResult;
import com.taiyiyun.passport.exception.PassportException;
import com.taiyiyun.passport.language.CurrentPackBundle;
import com.taiyiyun.passport.language.PackBundle;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

@Component
public class ExceptionHandler implements HandlerExceptionResolver {

	public final Logger logger = LoggerFactory.getLogger(getClass());
	
	private static final String defaultViewName = "errors/internalMsg";
	
	@Override
	public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
		if (handler != null && handler instanceof HandlerMethod 
				&& null != ((HandlerMethod) handler).getMethodAnnotation(ResponseBody.class)) {
			if(ex instanceof PassportException) {
				PassportException e = (PassportException)ex;
				String errMsg = "";
				if(StringUtils.isNotEmpty(e.getCode())) {
					PackBundle bundle = CurrentPackBundle.get();
					if(StringUtils.isNotEmpty(e.getCode())) {
						if(e.getArguments() != null && !e.getArguments().isEmpty()) {
							errMsg = bundle.getString(e.getCode(), e.getArguments().toArray());
						} else {
							errMsg = bundle.getString(e.getCode());
						}
					}
				}
				sendJson(response, new ErrorMessageResult(1, null, StringUtils.isNotEmpty(errMsg) ? errMsg : ex.getMessage()));
			} else {
				//去掉-1000的错误码，统一返回1的错误，即使是系统错误
				sendJson(response, new ErrorMessageResult(1, null,null == ex ? "system error" : ex.getMessage()));
//				sendJson(response, new ErrorResult(ErrorResult.CODE_SYSERR).setDetail(null == ex ? "" : ex.getMessage()));
			}
			return null;
		} else {
			ModelAndView modelAndView = new ModelAndView(defaultViewName);
			if (null != modelAndView.getModel()) {
				modelAndView.addObject("ex", ex);
			}
			return modelAndView;
		}
	}
	
	protected void sendJson(HttpServletResponse response, Object result) {
		PrintWriter printWriter = null;
		String json = "";
		try {
			json = JSON.toJSONString(result);
			response.reset();
			response.setContentType("text/html;charset=UTF-8");
			printWriter = response.getWriter();
			printWriter.print(json);
			printWriter.flush();
		} catch (Exception e) {
			logger.error("程序异常，在：sendJson，详情：[json] = " + json, e);
		} finally {
			if (null != printWriter) {
				printWriter.close();
			}
		}
	}
}
