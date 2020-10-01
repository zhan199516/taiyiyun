package com.taiyiyun.passport.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class DefaultController {

	@RequestMapping(value = {"/api/*", "/Api/*"})
	public String unmappedRequest(HttpServletRequest request, HttpServletResponse response) {

		response.setStatus(HttpServletResponse.SC_NOT_FOUND);

		return null;
	}
	
	
}
