package com.taiyiyun.passport.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ErrorController {
	
	@RequestMapping(value = "error/{code}")
	public String error(@PathVariable Integer code) {
		return "errors/" + code;
	}
	
}
