package com.taiyiyun.passport.controller;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.taiyiyun.passport.dao.ILoginAppDao;
import com.taiyiyun.passport.exception.DefinedError;
import com.taiyiyun.passport.language.LangResource;
import com.taiyiyun.passport.language.PackBundle;
import org.springframework.beans.factory.xml.DefaultNamespaceHandlerResolver;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.taiyiyun.passport.consts.Config;
import com.taiyiyun.passport.consts.Const;
import com.taiyiyun.passport.service.IRegisterService;
import com.taiyiyun.passport.service.ISharePointService;
import com.taiyiyun.passport.util.AESUtil;
import com.taiyiyun.passport.util.HttpClientUtil;
import com.taiyiyun.passport.util.MD5Signature;
import com.taiyiyun.passport.util.Misc;
import com.taiyiyun.passport.util.SessionUtil;
import com.taiyiyun.passport.util.StringUtil;

@Controller
public class RegisterController extends BaseController{
	
	private static final Color[] FONT_COLORS = {Color.BLUE, Color.RED, Color.GREEN, new Color(180, 4, 4), new Color(38, 226, 97)};
	private static final int IMAGE_WIDTH = 88;
	private static final int IMAGE_HEIGHT = 34;
	
	@Resource
	private IRegisterService registerService;
	
	@Resource
	private ISharePointService sharePointService;

	@Resource
	private ILoginAppDao loginAppDao;
	
	@RequestMapping("/user/register")
	public String userRegister(String recommendId,Model model) {
		model.addAttribute("recommendId", recommendId);
		return "user/user_register";
	}
	
	@RequestMapping(value = "validate/imageCode")
	public void imageCode(HttpServletRequest request, HttpServletResponse response) {

		PackBundle bundle = LangResource.getResourceBundle(request);

		int width = IMAGE_WIDTH;
		int height = IMAGE_HEIGHT;

		String validateCode = Misc.randomCode(4).toUpperCase();

		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		Graphics g = image.getGraphics();

		g.setColor(new Color(240, 243, 250));
		g.fillRect(0, 0, width, height);

		for (int i = 0; i < width * height / 2; ++i) {
			int x = getRandom(width - 1, 0);
			int y = getRandom(height - 1, 0);
			image.setRGB(x, y, Color.WHITE.getRGB());
		} // 随机噪点

		Point[] points = new Point[5];
		points[0] = new Point(0, getRandom(height, 0));
		points[points.length - 1] = new Point(width, getRandom(height, 0));
		for (int i = 1; i < points.length - 1; ++i) {
			points[i] = new Point(getRandom(5, width / 3 * i + 5), getRandom(height - 10, 5));
		}

		if (g instanceof Graphics2D) { // 绘制曲线
			g.setColor(new Color(0, 140, 245));

			Map<RenderingHints.Key, Object> map = new HashMap<>();
			map.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			((Graphics2D) g).setRenderingHints(map);

			GeneralPath path = new GeneralPath();
			path.moveTo(points[0].x, points[0].y);
			for (int i = 0; i < points.length - 1; ++i) {
				Point sp = points[i];
				Point ep = points[i + 1];
				Point c1 = new Point((sp.x + ep.x) / 2, sp.y);
				Point c2 = new Point((sp.x + ep.x) / 2, ep.y);
				path.curveTo(c1.x, c1.y, c2.x, c2.y, ep.x, ep.y);
			}

			((Graphics2D) g).setStroke(new BasicStroke(1));
			((Graphics2D) g).draw(path);
		}

		Font font = new Font("黑体", Font.TRUETYPE_FONT, (width / 100) * 8 + 20);
		g.setFont(font);
		int startX = (width - 4 * font.getSize() - 15) / 2;
		for (int i = 0; i < validateCode.length(); ++i) {
			drawChar(g, getRandom(5, font.getSize() * i + 5 + startX), getRandom(height - font.getSize(), font.getSize()), validateCode.charAt(i));
		}

		g.dispose();

		request.getSession(true).removeAttribute(Const.IMG_VALIDATE_CODE);
		SessionUtil.add(request, Const.IMG_VALIDATE_CODE, validateCode);

		OutputStream os = null;
		try {
			os = response.getOutputStream();
			ImageIO.write(image, "JPEG", os);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != os) {
				try {
					os.close();
				} catch (Exception e) {
				}
			}
		}
	}
	
	@ResponseBody
	@RequestMapping(value = "/user/checkMobile", produces = {Const.PRODUCES})
	public String checkMobile(String mobile, String code, HttpServletRequest request) {

		PackBundle bundle = LangResource.getResourceBundle(request);

		if(StringUtil.isEmpty(code)) {
			return toJson(false, bundle.getString("need.sms"),null);
		}
		
		Object validCode = SessionUtil.get(request, Const.IMG_VALIDATE_CODE);
		if(!code.equalsIgnoreCase(String.valueOf(validCode))) {
			SessionUtil.remove(request, Const.IMG_VALIDATE_CODE);
			return toJson(false, bundle.getString("need.sms.error"),null);
		}
		SessionUtil.remove(request, Const.IMG_VALIDATE_CODE);
		
		if(StringUtil.isEmpty(mobile)) {
			return toJson(false, bundle.getString("need.mobile"),null);
		}
		Map<String,String> dataMap = new HashMap<String,String>();
		dataMap.put("Mobile", mobile);
		dataMap.put("Appkey", Config.get("appKey"));
		
		String sign = MD5Signature.signMd5(dataMap, loginAppDao.getItem(Config.get("appKey")).getAppSecret());
		dataMap.put("sign", sign);

		JSONObject json = null;
		String responseText = null;
		try{
			responseText  = HttpClientUtil.doGet(Config.get("remote.url") + "/Api/Mobile", dataMap);
			System.out.println(responseText);

			json = (JSONObject) JSONObject.parse(responseText);
			if(StringUtil.isNotEmpty(json.get("Message"))) {
				return toJson(false, bundle.getString("need.http.null"), null);
			}
		} catch(DefinedError ex){
			return toJson(false, bundle.getString("need.http.null"), null);
		}

		if(json.get("success").equals(false)) {
			return toJson(false, json.getString("error"), null);
		}
		
		JSONObject data = (JSONObject) json.get("data");
		if(json.get("success").equals(true) && StringUtil.isNotEmpty(data)) {
			if(data.containsKey("IsRegistered") && data.getBooleanValue("IsRegistered")) {
				return toJson(false, bundle.getString("failed.mobile.registered"), null);
			}
		}
		
		return responseText;
	}
	
	@RequestMapping(value = "/user/smsVerifyCode", method = RequestMethod.GET)
	public String sendSMSVerifyCode(String mobile, String recommendId, Model model, HttpServletRequest request) {

		PackBundle bundle = LangResource.getResourceBundle(request);

		Map<String, String> params = new HashMap<>();
		params.put("Mobile", mobile);
		params.put("Appkey", Config.get("appKey"));
		
		String sign = MD5Signature.signMd5(params, loginAppDao.getItem(Config.get("appKey")).getAppSecret());
		params.put("sign", sign);

		try{
			HttpClientUtil.doPost(Config.get("remote.url") + "/Api/SMSVerifyCode", params);

			model.addAttribute("mobile", mobile);
			model.addAttribute("recommendId", recommendId);
		} catch(DefinedError ex){
		}
		return "user/user_psw";
	}
	
	@ResponseBody
	@RequestMapping(value = "/user/doRegister", method = RequestMethod.POST, produces = {Const.PRODUCES})
	public String doRegister(HttpServletRequest request, @RequestParam("mobile") String mobile,@RequestParam("password") String password,
			  @RequestParam("verifyCode") String verifyCode, String recommendId) {

		PackBundle bundle = LangResource.getResourceBundle(request);

		if(StringUtil.isEmpty(mobile)) {
			return toJson(false, bundle.getString("need.mobile"), null);
		}
		
		if(StringUtil.isEmpty(password)) {
			return toJson(false, bundle.getString("need.password"), null);
		}
		
		if(StringUtil.isEmpty(verifyCode)) {
			return toJson(false, bundle.getString("need.sms.error"), null);
		}
		
		Map<String,String> params = new HashMap<String,String>();
		params.put("Appkey", Config.get("appKey"));
		params.put("Mobile", mobile);
		params.put("Password", AESUtil.encrypt(password, loginAppDao.getItem(Config.get("appKey")).getAppSecret()));
		params.put("VerifyCode", verifyCode);
		params.put("recommendId", recommendId);
		
		String msg = registerService.saveRegisterInfo(bundle, params);
		if(StringUtil.isNotEmpty(msg)) {
			if("true".equals(msg)) {
				return toJson(true, bundle.getString("successful.register"), "[]");
			}
			return toJson(true, msg, "[]");
		}
		
		return toJson(true, bundle.getString("failed.register"), "[]");
	}
	
	private static void drawChar(Graphics g, int x, int y, char c) {
		g.setColor(Color.WHITE);
		g.drawString(String.valueOf(c), x + 1, y + 1);
		g.setColor(FONT_COLORS[getRandom(FONT_COLORS.length - 1, 0)]);
		g.drawString(String.valueOf(c), x, y);
	}
	
	private static final int getRandom(int max, int base) {
		return (int) (base + Math.round((Math.random() * max)));
	}

}
