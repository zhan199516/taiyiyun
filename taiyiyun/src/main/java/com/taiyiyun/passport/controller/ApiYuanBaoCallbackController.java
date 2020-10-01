package com.taiyiyun.passport.controller;

import com.alibaba.fastjson.JSON;
import com.taiyiyun.passport.language.LangResource;
import com.taiyiyun.passport.language.PackBundle;
import com.taiyiyun.passport.transfer.yuanbao.YuanBaoBindInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.HashMap;

@Controller("apiYuanBaoCallbackController")
public class ApiYuanBaoCallbackController extends BaseController {

	public final Logger logger = LoggerFactory.getLogger(getClass());


	private boolean checkbindInfo(){
		return false;
	}


	@ResponseBody
	@RequestMapping(value = "/api/yuanbao/bind")
	public String bind(HttpServletRequest request, HttpServletResponse response) {
		HashMap<String, Object> rst = new HashMap<>();

		PackBundle bundle = LangResource.getResourceBundle(request);

		try {
			request.setCharacterEncoding("UTF-8");
		} catch (Exception e) {
			logger.error("设置Request字符编码为UTF-8异常。", e);
			return this.toJson(0, bundle.getString("failed.third.error"), "GET+bind", null);
		}
		
		try (InputStream is = request.getInputStream()) {
			if (is.available() > 0) {
				byte[] bytes = new byte[is.available()];
				is.read(bytes);
				String json = new String(bytes, "UTF-8");
				YuanBaoBindInfo yuanBaoBindInfo = JSON.parseObject(json, YuanBaoBindInfo.class);
				if (null != yuanBaoBindInfo && null != yuanBaoBindInfo.getData()) {
					YuanBaoBindInfo.BindInfo data = yuanBaoBindInfo.getData();



					String userKey = data.getUserKey();
					String userSecretKey = data.getUserSecretKey();
					String uniqueKey = data.getUniqueKey();


				} else {
					return this.toJson(0, bundle.getString("failed.execute"), "GET+bind", null);
				}
			}
		} catch (Exception e) {
			logger.error("写入用户绑定信息异常。", e);
			return this.toJson(0, bundle.getString("failed.execute"), "GET+bind", null);
		}
		
		return toJson(0, null, null, null);
	}
	
}
