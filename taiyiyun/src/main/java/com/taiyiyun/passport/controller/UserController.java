package com.taiyiyun.passport.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.taiyiyun.passport.consts.Const;
import com.taiyiyun.passport.service.IUserService;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Resource
	private IUserService userService;
	
	@ResponseBody
	@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = {Const.PRODUCES})
	public String toIndex(HttpServletRequest request, Model model, @PathVariable Integer id) {
		return JSON.toJSONString(userService.getById(id));
	}
	
}
