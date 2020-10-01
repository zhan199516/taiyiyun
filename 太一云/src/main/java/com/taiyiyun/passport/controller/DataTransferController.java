package com.taiyiyun.passport.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.taiyiyun.passport.language.LangResource;
import com.taiyiyun.passport.language.PackBundle;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.taiyiyun.passport.service.IOldArticleService;

import java.util.ResourceBundle;

@Controller
public class DataTransferController extends BaseController{
	
	@Resource
	private IOldArticleService oldArticleService;
	
	
	@RequestMapping(value = "/api/publishOld/transfer", method = {RequestMethod.POST}, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	@ResponseBody
	public String transferArticleImage(HttpServletRequest request) {

		PackBundle bundle = LangResource.getResourceBundle(request);

		String apiName = "post + /util/article/transfer/image";
		
		try {
			
			Long count = oldArticleService.startTransferArticleImage(request, 0L);
			return toJson(0, bundle.getString("successful.immigrate", count.toString()), apiName, new Object());
			
		} catch (Exception e) {
			e.printStackTrace();
			return toJson(3, bundle.getString("failed.execute"), apiName, new Object());
		}
		
	}
	
	
	
}
