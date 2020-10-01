package com.taiyiyun.passport.controller;

import com.taiyiyun.passport.consts.Const;
import com.taiyiyun.passport.language.LangResource;
import com.taiyiyun.passport.language.PackBundle;
import com.taiyiyun.passport.po.UpdateCheck;
import com.taiyiyun.passport.service.IUpdateCheckService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class ApiSystemController extends BaseController {
	
	@Resource
	private IUpdateCheckService updateCheckService;
	
	@ResponseBody
	@RequestMapping(value = "/api/system/updateCheck", method = RequestMethod.GET, produces = {Const.PRODUCES_JSON})
	public String updateCheck(Integer deviceType, String version, HttpServletRequest request) {

		PackBundle bundle = LangResource.getResourceBundle(request);

		try {
			
			UpdateCheck upCheck = updateCheckService.getCurrentUpdateCheck(deviceType, version);
			if(null != upCheck) {
				Map<String, Object> dataMap = new HashMap<>();
				Map<String, String> leftBtn = new HashMap<>();
				leftBtn.put("text", upCheck.getLeftButton());
				leftBtn.put("url", "");
				
				Map<String, String> rightBtn = new HashMap<>();
				rightBtn.put("text", upCheck.getRightButton());
				rightBtn.put("url", "");
				
				if(upCheck.getType() != null) {
					if(upCheck.getType().intValue() == 0) {
						leftBtn.put("url", upCheck.getUrl());
					}else if(upCheck.getType().intValue() == 1) {
						rightBtn.put("url", upCheck.getUrl());
					}
				}
				dataMap.put("id", upCheck.getId());
				dataMap.put("updateStatus", upCheck.getUpdateStatus());
				dataMap.put("title", upCheck.getTitle());
				dataMap.put("text", upCheck.getText());
				dataMap.put("leftButton", leftBtn);
				dataMap.put("rightButton", rightBtn);
				dataMap.put("QQGroup", upCheck.getQqGroup());

				return toJson(0, "", "get + /api/system/updateCheck", dataMap);
			}
			return toJson(0, "", "get + /api/system/updateCheck", new Object());
		} catch (Exception e) {
			e.printStackTrace();
			return toJson(1, bundle.getString("failed.execute"), "get + /api/system/updateCheck", new Object());
		}
		
	}

}
