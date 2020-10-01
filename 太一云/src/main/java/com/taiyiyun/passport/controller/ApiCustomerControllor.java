package com.taiyiyun.passport.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.taiyiyun.passport.bean.UserDetails;
import com.taiyiyun.passport.consts.Config;
import com.taiyiyun.passport.consts.Const;
import com.taiyiyun.passport.dao.ILoginLogDao;
import com.taiyiyun.passport.exception.DefinedError;
import com.taiyiyun.passport.language.LangResource;
import com.taiyiyun.passport.language.PackBundle;
import com.taiyiyun.passport.mqtt.Message;
import com.taiyiyun.passport.mqtt.v2.MessagePublisher;
import com.taiyiyun.passport.po.LoginLog;
import com.taiyiyun.passport.po.PublicUser;
import com.taiyiyun.passport.service.ICustomerService;
import com.taiyiyun.passport.service.IPasswordLockService;
import com.taiyiyun.passport.service.IPublicUserService;
import com.taiyiyun.passport.service.IRedisService;
import com.taiyiyun.passport.service.session.INotify;
import com.taiyiyun.passport.service.session.LoginInfo;
import com.taiyiyun.passport.service.session.MobileSessionCache;
import com.taiyiyun.passport.sms.ModelType;
import com.taiyiyun.passport.sms.SmsYPClient;
import com.taiyiyun.passport.sqlserver.comm.PictureType;
import com.taiyiyun.passport.sqlserver.comm.UsersInfoBody;
import com.taiyiyun.passport.sqlserver.po.*;
import com.taiyiyun.passport.sqlserver.service.*;
import com.taiyiyun.passport.util.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;


//@Controller
public class ApiCustomerControllor extends BaseController {
	
	private final String PASSWORD_ERROR = "login.error";

	@Autowired
	private IUsersService usersService;

	@Autowired
	private IDeveloperService developerService;

	@Autowired
	private ICustomerService customerService;

	@Autowired
	private IPictureTempService pictureTempService;

	@Resource
	private IRealNameAuthService rnAuthService;

	@Autowired
	private IAntifakeAddressStoreService antifakeAddressStoreService;

	@Autowired
	private IPictureTypeService pictureTypeService;

	@Autowired
	private IUserEntityService userEntityService;

	@Autowired
	private IEntityService entityService;
	
	@Autowired
	private IRedisService redisService;
	
	@Autowired
	private IPasswordLockService passwordLockService;
	
	@Autowired
	private ILoginLogDao loginLogDao;
	
	@Autowired
	private IPublicUserService publicUserService;
	
	
	/**
	 * 检查手机号是否已被注册
	 */
	@ResponseBody
	@RequestMapping(value = "/Api/Mobile", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
	public String validmobile(HttpServletRequest request, @RequestParam("Mobile") String mobile) {

		PackBundle bundle = LangResource.getResourceBundle(request);

		if (StringUtil.isEmpty(mobile)) {
			return toJson(false, bundle.getString("need.mobile"), null);
		}
		PhoneUtil pu = PhoneUtil.getInstance();
		String[] phones = mobile.split("-");
		String mobilePrefix = "86";
		String mobileSuffix = null;
		boolean flag = true;
		if (phones.length == 1) {// 默认是中文
			flag = pu.checkPhoneNumberByCountryCode(mobile, "86");
			mobileSuffix = phones[0];
		} else if (phones.length == 2) {// 直接合法性验证
			flag = pu.checkPhoneNumberByCountryCode(phones[1], phones[0]);
			mobilePrefix = phones[0];
			mobileSuffix = phones[1];
		}
		if (!flag) {
			return toJson(false, bundle.getString("mobile.grammar.fault"), null);
		}

		try {
			if (!usersService.checkMobileUnique(mobileSuffix, mobilePrefix)) {
				return toJson(false, bundle.getString("need.mobile.register"), null);
			}

			return toJson(true, bundle.getString("user.not.register"), null);
		} catch (Exception ex) {
			return toJson(false, bundle.getString("failed.execute"), null);
		}

	}

	/**
	 * 发送注册验证码
	 */
	@ResponseBody
	@RequestMapping(value = "/Api/SMSVerifyCode", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
	public String sendRegisterVerifyCode(HttpServletRequest request, @RequestParam("Mobile") String mobile) {

		PackBundle bundle = LangResource.getResourceBundle(request);

		if (StringUtil.isEmpty(mobile)) {
			return toJson(false, bundle.getString("need.mobile"), null);
		}

		PhoneUtil pu = PhoneUtil.getInstance();
		String[] phones = mobile.split("-");
		boolean flag = true;
		if (phones.length == 1) {// 默认是中文
			flag = pu.checkPhoneNumberByCountryCode(mobile, "86");
		} else if (phones.length == 2) {// 直接合法性验证
			flag = pu.checkPhoneNumberByCountryCode(phones[1], phones[0]);
		}
		if (!flag) {
			return toJson(false, bundle.getString("mobile.grammar.fault"), null);
		}

		try {
			if (!usersService.checkMobileUnique(mobile, phones.length == 1 ? "86" : phones[0])) {
				return toJson(false, bundle.getString("need.mobile.register"), null);
			}

			if (!customerService.checkAllowSendVerifyCode(mobile)) {
				return toJson(false, "短信发送太频繁，请一分钟之后再试", null);
			}

			String verifyCode = RandomUtil.getFixLenthString(7);
			if (!SMSHelper.sendVerifyCode(mobile, verifyCode)) {
				return toJson(false, "验证码发送失败", null);
			}
			customerService.saveVerifyCode(mobile, verifyCode);

			return toJson(false, "验证码发送成功", null);
		} catch (Exception ex) {
			ex.printStackTrace();
			return toJson(false, bundle.getString("failed.execute"), null);
		}
	}

	/**
	 * 验证注册验证码
	 */
	@ResponseBody
	@RequestMapping(value = "/Api/SMSVerifyCode", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
	public String smsVerifyCode(HttpServletRequest request, @RequestParam("Mobile") String mobile, @RequestParam("Code") String code) {

		PackBundle bundle = LangResource.getResourceBundle(request);

		if (StringUtil.isEmpty(mobile)) {
			return toJson(false, bundle.getString("need.mobile"), null);
		}

		PhoneUtil pu = PhoneUtil.getInstance();
		String[] phones = mobile.split("-");
		boolean flag = true;
		if (phones.length == 1) {// 默认是中文
			flag = pu.checkPhoneNumberByCountryCode(mobile, "86");
		} else if (phones.length == 2) {// 直接合法性验证
			flag = pu.checkPhoneNumberByCountryCode(phones[1], phones[0]);
		}
		if (!flag) {
			return toJson(false, bundle.getString("mobile.grammar.fault"), null);
		}

		try {
			if (!customerService.verifyCode(mobile, code, false)) {
				return toJson(false, bundle.getString("need.sms.error"), null);
			}
			return toJson(true, "验证码验证通过", null);
		} catch (Exception ex) {
			ex.printStackTrace();
			return toJson(false, bundle.getString("failed.execute"), null);
		}
	}

	/**
	 * 用户注册
	 */
	@ResponseBody
	@RequestMapping(value = "/Api/CustomerRegist", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public Object register(HttpServletRequest request, @RequestParam("Mobile") String mobile, @RequestParam("Sign") String sign, @RequestParam("Appkey") String appKey,
			@RequestParam("Password") String password, @RequestParam("VerifyCode") String verifyCode) {

		PackBundle bundle = LangResource.getResourceBundle(request);

		if (StringUtil.isEmpty(verifyCode)) {
			return toJson(false, bundle.getString("need.sms"), null);
		}

		if (StringUtil.isEmpty(mobile)) {
			return toJson(false, bundle.getString("need.mobile"), null);
		}

		PhoneUtil pu = PhoneUtil.getInstance();
		String[] phones = mobile.split("-");
		String mobileSuffix = "";
		String mobilePrefix = "86";
		boolean flag = true;
		if (phones.length == 1) {
			flag = pu.checkPhoneNumberByCountryCode(mobile, "86");
			mobileSuffix = phones[0];
		} else if (phones.length == 2) {
			flag = pu.checkPhoneNumberByCountryCode(phones[1], phones[0]);
			mobilePrefix = phones[0];
			mobileSuffix = phones[1];
		}
		if (!flag) {
			return toJson(false, bundle.getString("mobile.grammar.fault"), null);
		}

		if (StringUtil.isEmpty(password)) {
			return toJson(false, bundle.getString("need.password"), null);
		}

		if (StringUtil.isEmpty(appKey)) {
			return toJson(false, bundle.getString("need.param", "appKey"), null);
		}

		if (StringUtil.isEmpty(sign)) {
			return toJson(false, bundle.getString("need.sign"), null);
		}

		try {

			if (!customerService.verifyCode(mobile, verifyCode, true)) {
				return toJson(false, bundle.getString("need.sms.error"), null);
			}

			Developer developer = developerService.getByAppKey(appKey);
			if (developer == null) {
				return toJson(false, bundle.getString("article.appkey.match"), null);
			}

			String decryptPwd = AESUtil.decrypt(password, developer.getAppSecret());
			if (!Pattern.matches("^(?![0-9]+$)(?![a-zA-Z]+$)[a-zA-Z0-9]{6,20}", decryptPwd)) {
				return toJson(false, bundle.getString("password.grammar.fault"), null);
			}

			Map<String, String> paramMap = new HashMap<String, String>();
			paramMap.put("Appkey", appKey);
			paramMap.put("Mobile", mobile);
			paramMap.put("Password", password);
			paramMap.put("VerifyCode", verifyCode);
			if (!sign.equals(MD5Signature.signMd5(paramMap, developer.getAppSecret()))) {
				return toJson(false, bundle.getString("failed.sign"), null);
			}

			if (!usersService.checkMobileUnique(mobileSuffix, mobilePrefix)) {
				return toJson(false, bundle.getString("need.mobile.register"), null);
			}

			List<AntifakeAddressStore> addressList = antifakeAddressStoreService.getAvailableAddress("CID", 1);
			if (addressList == null || addressList.size() == 0) {
				return toJson(false, bundle.getString("register.too.many"), null);
			}

			paramMap.put("password", decryptPwd);
			paramMap.put("Mobile", mobileSuffix);
			UsersInfoBody rsBody = usersService.saveUser(paramMap, developer, addressList.get(0));
			if (rsBody != null && rsBody.getUsers() != null) {
				String ip = RequestUtil.getIpAddr(request);
				String deviceId = ip;

				UserDetails userDetails = new UserDetails();
				userDetails.setUuid(rsBody.getUsers().getUUID());
				userDetails.setNikeName(rsBody.getUsers().getNikeName());
				userDetails.setMobile(rsBody.getUsers().getMobile());
				SessionUtil.addUserDetails(request, userDetails);

				LoginInfo loginInfo = new LoginInfo();
				loginInfo.setSessionId(request.getSession().getId());
				loginInfo.setIp(ip);
				loginInfo.setLoginTime(new Date().getTime());
				loginInfo.setMobile(rsBody.getUsers().getMobile());
				loginInfo.setAppKey(appKey);
				loginInfo.setDeviceId(deviceId);

				SessionUtil.setLoginInfo(request, loginInfo);
				return toJson(true, 0, bundle.getString("successful.register"), rsBody.getBody());
			}
			return toJson(false, bundle.getString("failed.register"), null);
		} catch (Exception e) {
			e.printStackTrace();
			return toJson(false, bundle.getString("failed.execute"), null);
		}
	}

	/**
	 * 证件照正面上传
	 */
	@ResponseBody
	@RequestMapping(value = "/Api/IDCardFront", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
	public String iDCardFront(HttpServletRequest request, @RequestParam("Address") String address, @RequestParam("imgBase64_NoSign") String imgBase64_NoSign) {

		final PackBundle bundle = LangResource.getResourceBundle(request);

		try {

			Users users = usersService.getUserByAddress(address);
			if (users == null) {
				return toJson(false, bundle.getString("user.address.fault"), null);
			}

			PictureTemp pictureTemp = pictureTempService.getPictureTemp(users.getUUID(), PictureType.IDCARD_FRONT);
			if (pictureTemp != null && pictureTemp.getFinished() == 1) {
				return toJson(false, bundle.getString("picture.modify.deny.register"), null);
			}

			pictureTempService.saveFile(users.getUUID(), pictureTemp, imgBase64_NoSign, PictureType.IDCARD_FRONT);
			return toJson(true, bundle.getString("successful.upload"), null);
		} catch (Exception ex) {
			return toJson(false, bundle.getString("failed.execute"), null);
		}
	}

	/**
	 * 证件照正反面上传
	 */
	@ResponseBody
	@RequestMapping(value = "/api/idCardFrontBack", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
	public String idCardFrontBack(HttpServletRequest request, @RequestParam("address") final String address, @RequestParam("idCardPhotoBase64Front") String idCardPhotoBase64Front,
			@RequestParam("idCardPhotoBase64Back") final String idCardPhotoBase64Back) {

		final PackBundle bundle = LangResource.getResourceBundle(request);

		try {
			Users users = usersService.getUserByAddress(address);
			if (users == null) {
				return toJson(false, bundle.getString("user.address.fault"), null);
			}

			PictureTemp pictureTempFront = pictureTempService.getPictureTemp(users.getUUID(), PictureType.IDCARD_FRONT);
			if (pictureTempFront != null && pictureTempFront.getFinished() == 1) {
				return toJson(false, bundle.getString("picture.modify.deny.register"), null);
			}

			PictureTemp pictureTempBack = pictureTempService.getPictureTemp(users.getUUID(), PictureType.IDCARD_BACK);
			if (pictureTempBack != null && pictureTempBack.getFinished() == 1) {
				return toJson(false, bundle.getString("picture.modify.deny.register"), null);
			}

			pictureTempService.saveFile(users.getUUID(), pictureTempFront, idCardPhotoBase64Front, pictureTempBack, idCardPhotoBase64Back);
			return toJson(true, bundle.getString("successful.upload"), null);
		} catch (Exception e) {
			e.printStackTrace();
			return toJson(false, bundle.getString("failed.execute"), null);
		}
	}

	/**
	 * 实名信息填写
	 */
	@ResponseBody
	@RequestMapping(value = "/api/fillUpRealNameInfo", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
	public String identityCardNew(HttpServletRequest request, @RequestParam("address") String address, @RequestParam("identityNumber") String identityNumber,
			@RequestParam("userName") String userName, @RequestParam("validDateStart") String validDateStart, @RequestParam("validDateEnd") String validDateEnd,
			@RequestParam("sex") String sex, @RequestParam("nation") String nation, @RequestParam("birthDay") String birthDay, @RequestParam("homeAddr") String homeAddr,
			@RequestParam("idCardPhotoBase64Back") String idCardPhotoBase64Back) {

		PackBundle bundle = LangResource.getResourceBundle(request);
		// 1.验证信息
		if (StringUtils.isEmpty(address)) {// 地址
			return toJson(false, 2, bundle.getString("filluprealnameinfo.error.address"), null);
		}
		if (StringUtils.isEmpty(validDateStart)) {
			return toJson(false, 2, bundle.getString("filluprealnameinfo.error.validdatestart"), null);
		}
		if (StringUtils.isEmpty(validDateEnd)) {
			return toJson(false, 2, bundle.getString("filluprealnameinfo.error.validdateend"), null);
		}
		if (StringUtils.isEmpty(sex)) {
			return toJson(false, 2, bundle.getString("filluprealnameinfo.error.sex"), null);
		}
		if (StringUtils.isEmpty(nation)) {
			return toJson(false, 2, bundle.getString("filluprealnameinfo.error.nation"), null);
		}
		if (StringUtils.isEmpty(birthDay)) {
			return toJson(false, 2, bundle.getString("filluprealnameinfo.error.birthDay"), null);
		}
		if (StringUtils.isEmpty(homeAddr)) {
			return toJson(false, 2, bundle.getString("filluprealnameinfo.error.homeAddr"), null);
		}
		if (StringUtils.isEmpty(idCardPhotoBase64Back)) {
			return toJson(false, 2, bundle.getString("filluprealnameinfo.error.idcardback"), null);
		}
		// 2.保存实名认证信息
		Map<String, Object> result = rnAuthService.fillUpRealNameInfo(idCardPhotoBase64Back, address, identityNumber, userName, validDateStart, validDateEnd, sex, nation,
				birthDay, homeAddr, bundle);
		return JSON.toJSONString(result);
	}

	/**
	 * 身份证实名认证(姓名+身份证号码+人像) 该接口可能废弃
	 */
	@ResponseBody
	@RequestMapping(value = "/Api/IdentityCard", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
	public String identityCard(HttpServletRequest request, @RequestParam("Address") String address, @RequestParam("ImgData_NoSign") String imgDataNoSign,
			@RequestParam("IdentityNumber") String idcardNo, @RequestParam("UserName") String userName) {

		PackBundle bundle = LangResource.getResourceBundle(request);

		try {
			Users users = usersService.getUserByAddress(address);
			if (users == null) {
				return toJson(false, bundle.getString("user.address.fault"), null);
			}

			return JSON.toJSONString(customerService.realNameAuth(bundle, users, idcardNo, imgDataNoSign, userName, null));
		} catch (Exception e) {
			e.printStackTrace();
			return toJson(false, bundle.getString("failed.execute"), null);
		}
	}

	/**
	 * 检查是否验证证件有效期
	 */
	@ResponseBody
	@RequestMapping(value = "/api/checkUserHasValidDate", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
	public String checkUserHasValidDate(HttpServletRequest request, @RequestParam("appKey") String appKey, @RequestParam("sign") String sign,
			@RequestParam("mobile") String mobile, @RequestParam("mobilePrefix") String mobilePrefix) {
		PackBundle bundle = LangResource.getResourceBundle(request);
		Map<String, Object> result = new HashMap<>();
		result.put("errorCode", null);
		result.put("success", true);
		result.put("error", null);
		Map<String, String> params = new HashMap<>();
		params.put("appKey", appKey);
		params.put("mobile", mobile);
		params.put("mobilePrefix", mobilePrefix);
		boolean b = SignChecker.checkSign(sign, params, appKey);
		if (!b) {
			return toJson(false, 2, bundle.getString("failed.sign"), null);
		}

		EntityDetail ed = rnAuthService.queryEntityDetailHasValidDateByMobile(mobile, mobilePrefix);
		Map<String, Object> data = new HashMap<>();
		if (ed != null) {
			data.put("hasValidDate", 0);
		} else {
			data.put("hasValidDate", 1);
		}
		result.put("data", data);
		return JSON.toJSONString(result, SerializerFeature.WriteMapNullValue);
	}

	/**
	 * 身份证实名认证接口
	 */
	@ResponseBody
	@RequestMapping(value = "/api/identityCardNew", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
	public String identityCardNew(HttpServletRequest request, @RequestParam("address") String address, @RequestParam("sign") String sign, @RequestParam("appKey") String appKey,
			@RequestParam("facePhotoBase64") String facePhotoBase64, @RequestParam("identityNumber") String identityNumber, @RequestParam("userName") String userName,
			@RequestParam("validDateStart") String validDateStart, @RequestParam("validDateEnd") String validDateEnd, @RequestParam("sex") String sex,
			@RequestParam("nation") String nation, @RequestParam("birthDay") String birthDay, @RequestParam("homeAddr") String homeAddr) {

		PackBundle bundle = LangResource.getResourceBundle(request);

		try {
			Users users = usersService.getUserByAddress(address);
			if (users == null) {
				return toJson(false, 2, bundle.getString("user.address.fault"), null);
			}

			return JSON.toJSONString(customerService.realNameAuth(bundle, users, appKey, identityNumber, userName, validDateStart, validDateEnd, nation, sex, homeAddr, birthDay,
					facePhotoBase64, sign, "1"));
		} catch (Exception e) {
			e.printStackTrace();
			return toJson(false, 2, bundle.getString("failed.execute"), null);
		}
	}

	/**
	 * 护照实名认证
	 */
	@ResponseBody
	@RequestMapping(value = "/api/identityCardNewForWePass", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
	public String identityCardNewForWePass(HttpServletRequest request, @RequestParam("address") String address, @RequestParam("sign") String sign,
			@RequestParam("appKey") String appKey, @RequestParam("facePhotoBase64") String facePhotoBase64, @RequestParam("identityNumber") String identityNumber,
			@RequestParam("userName") String userName, @RequestParam("validDateStart") String validDateStart, @RequestParam("validDateEnd") String validDateEnd,
			@RequestParam("sex") String sex, @RequestParam("nation") String nation, @RequestParam("birthDay") String birthDay, @RequestParam("homeAddr") String homeAddr) {

		PackBundle bundle = LangResource.getResourceBundle(request);

		try {
			Users users = usersService.getUserByAddress(address);
			if (users == null) {
				return toJson(false, 2, bundle.getString("user.address.fault"), null);
			}

			return JSON.toJSONString(customerService.realNameAuth(bundle, users, appKey, identityNumber, userName, validDateStart, validDateEnd, nation, sex, homeAddr, birthDay,
					facePhotoBase64, sign, "2"));
		} catch (Exception e) {
			e.printStackTrace();
			return toJson(false, 2, bundle.getString("failed.execute"), null);
		}
	}

	/**
	 * 获取允许认证的途径
	 */
	@ResponseBody
	@RequestMapping(value = "/api/RealNameAuth", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
	public String identityCard(HttpServletRequest request, @RequestParam("Address") String address) {

		PackBundle bundle = LangResource.getResourceBundle(request);

		try {
			Users users = usersService.getUserByAddress(address);
			if (users == null) {
				return toJson(false, bundle.getString("user.address.fault"), null);
			}

			return JSON.toJSONString(customerService.checkAuthStatus(bundle, users));
		} catch (Exception ex) {
			return toJson(false, bundle.getString("failed.execute"), null);
		}
	}

	/**
	 * 
	 */
	@ResponseBody
	@RequestMapping(value = "/Api/HeadPicture", method = { RequestMethod.POST }, produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
	public String uploadHeadPicture(@RequestParam("Address") String address, @RequestParam("1") MultipartFile file, HttpServletRequest request) {

		PackBundle bundle = LangResource.getResourceBundle(request);

		if (request.getContentLengthLong() > 8 * 1024 * 1024) {
			return toJson(false, "文件上传数据大小超出8M限制", null);
		}

		try {
			Users users = usersService.getUserByAddress(address);
			if (users == null) {
				return toJson(false, bundle.getString("user.address.fault"), null);
			}

			if (!FileUtil.checkPictures(file.getOriginalFilename())) {
				return toJson(false, "文件类型必须为jpg、jpeg、png格式", null);
			}

			if (file.getSize() > 2 * 1024 * 1024) {
				return toJson(false, "文件大小超过2M限制", null);
			}

			if (!pictureTypeService.existPictureType(FileUtil.getFileNameWithoutExtension(file.getOriginalFilename()))) {
				return toJson(false, "图片类型不正确", null);
			}

			return null;
		} catch (Exception ex) {
			ex.printStackTrace();
			return toJson(false, bundle.getString("failed.execute"), null);
		}
	}

	/**
	 * 
	 */
	@ResponseBody
	@RequestMapping(value = "/Api/CustomerDetailPerson", method = { RequestMethod.GET }, produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
	public String customerDetailPerson(@RequestParam("Address") String address, @RequestParam("Appkey") String appKey, HttpServletRequest request) {

		PackBundle bundle = LangResource.getResourceBundle(request);

		try {
			Users users = usersService.getUserByAddress(address);
			if (users == null) {
				return toJson(false, bundle.getString("user.address.fault"), new HashMap<>());
			}

			List<UserEntity> userEntitiys = userEntityService.getByUUIDAndTypeId(users.getUUID(), 1);
			if (userEntitiys == null || userEntitiys.size() == 0) {
				return toJson(false, 0, "没有个人信息", new HashMap<>());
			}

			UserEntity userEntity = userEntitiys.get(0);

			Entity entity = entityService.getByEntityId(userEntity.getEntityId());

			Map<String, Object> rsMap = new HashMap<>();
			rsMap.put("EntityId", userEntity.getEntityId());
			rsMap.put("IDCard", entity.getEntityUnqueId());
			rsMap.put("Mobile", users.getMobile());
			rsMap.put("Name", entity.getEntityName());

			return toJson(true, 0, bundle.getString("successful.search"), rsMap);
		} catch (Exception ex) {
			return toJson(false, bundle.getString("failed.execute"), new HashMap<>());
		}
	}

	/**
	 * 人工认证申请
	 */
	@ResponseBody
	@RequestMapping(value = "/api/UploadInfo", method = RequestMethod.POST, produces = { Const.PRODUCES_JSON })
	public String uploadInfo(String Appkey, String Address, String Name, String IDCard, String Passport, @RequestParam("1") MultipartFile file1,
			@RequestParam("2") MultipartFile file2, @RequestParam("3") MultipartFile file3, @RequestParam("4") MultipartFile file4, HttpServletRequest request) {

		PackBundle bundle = LangResource.getResourceBundle(request);
		final long LIMIT_SIZE = 2 * 1024 * 1024;

		if (StringUtil.isNotEmpty(IDCard) && StringUtil.isNotEmpty(Passport)) {
			return toJson(false, bundle.getString("only.idcard.passport"), null);
		}

		if (StringUtil.isEmpty(IDCard) && StringUtil.isEmpty(Passport)) {
			return toJson(false, bundle.getString("authenticate.error.msg.idcardandpassport"), null);
		}

		if (file1.getSize() > LIMIT_SIZE || file2.getSize() > LIMIT_SIZE || file3.getSize() > LIMIT_SIZE || file4.getSize() > LIMIT_SIZE) {
			return toJson(false, bundle.getString("file.too.big.2m"), null);
		}

		if (!FileUtil.checkPictures(file1.getOriginalFilename(), file2.getOriginalFilename(), file3.getOriginalFilename(), file4.getOriginalFilename())) {
			return toJson(false, bundle.getString("file.type.incorrect"), null);
		}

		if (!PictureType.checkExistType(FileUtil.getFileNameWithoutExtension(file1.getOriginalFilename()), FileUtil.getFileNameWithoutExtension(file2.getOriginalFilename()),
				FileUtil.getFileNameWithoutExtension(file3.getOriginalFilename()), FileUtil.getFileNameWithoutExtension(file4.getOriginalFilename()))) {
			return toJson(false, bundle.getString("file.name.incorrect"), null);
		}

		try {
			Users users = usersService.getUserByAddress(Address);
			if (users == null) {
				return toJson(false, bundle.getString("user.address.fault"), new HashMap<>());
			}

			if (StringUtil.isNotEmpty(IDCard)) {
				List<Entity> entityList = entityService.getByEntityUnqueIdAndTypeId(IDCard, 1);
				if (entityList != null && entityList.size() > 0) {
					return toJson(false, bundle.getString("id.number.exists"), new HashMap<>());
				}
			}

			if (StringUtil.isNotEmpty(Passport)) {
				List<Entity> entityList = entityService.getByEntityUnqueIdAndTypeId(Passport, 1);
				if (entityList != null && entityList.size() > 0) {
					return toJson(false, "护照号码已存在", new HashMap<>());
				}
			}

			return JSON.toJSONString(customerService.saveArtificialAuthInfo(bundle, users, IDCard, Passport, Name, file1, file2, file3, file4));
		} catch (Exception ex) {
			ex.printStackTrace();
			return toJson(false, bundle.getString("failed.execute"), new HashMap<>());
		}
	}
	
	/**
	 * 用户登录 (暂时签名仍然不包含DeviceId, DeviceName， GPS和SMS)
	 */
	@ResponseBody
	@RequestMapping(value = "/Api/ShareLogin", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
	public String shareLogin(final HttpServletRequest request, @RequestParam("Mobile") String mobile,@RequestParam(name="ClientId",required=false) String clientId, @RequestParam("Password") String password, @RequestParam("Sign") String sign,
			@RequestParam("Appkey") String appKey, @RequestParam("DeviceId") final String deviceId, @RequestParam("DeviceName") final String deviceName,
			@RequestParam(value = "GPS", required = false) String gps, @RequestParam(value = "SMS", required = false) String sms, HttpServletResponse response) {

		PackBundle bundle = LangResource.getResourceBundle(request);

		String ip = RequestUtil.getIpAddr(request);

		String apiName = "/Api/ShareLogin";

		if (StringUtil.isEmpty(mobile)) {
			return toJson(DefinedError.Status.PARAM_ERROR.getValue(), bundle.getString("need.mobile"), apiName, null);
		}

		PhoneUtil pu = PhoneUtil.getInstance();
		String[] phones = mobile.split("-");
		boolean flag = true;
		if (phones.length == 1) {// 默认是中文
			flag = pu.checkPhoneNumberByCountryCode(mobile, "86");
		} else if (phones.length == 2) {// 直接合法性验证
			flag = pu.checkPhoneNumberByCountryCode(phones[1], phones[0]);
		}
		if (!flag) {
			return toJson(DefinedError.Status.PARAM_ERROR.getValue(), bundle.getString("mobile.grammar.fault"), apiName, null);
		}

		if (StringUtil.isEmpty(password)) {
			return toJson(DefinedError.Status.PARAM_ERROR.getValue(), bundle.getString("need.password"), apiName, null);
		}

		if (StringUtil.isEmpty(appKey)) {
			return toJson(DefinedError.Status.PARAM_ERROR.getValue(), bundle.getString("need.param", "appKey"), apiName, null);
		}

		if (StringUtil.isEmpty(deviceId)) {
			return toJson(DefinedError.Status.PARAM_ERROR.getValue(), bundle.getString("need.param", "deviceId"), apiName, null);
		}

		try {
			Developer developer = developerService.getByAppKey(appKey);
			if (developer == null) {
				return toJson(false, bundle.getString("article.appkey.match"), null);
			}

			if (passwordLockService.checkLock(PASSWORD_ERROR, mobile)) {
				int seconds = Config.getInt(PASSWORD_ERROR + ".lock", 1800);
				Integer minute = (seconds + 59) / 60;
				return toJson(DefinedError.Status.LOCK.getValue(), bundle.getString("user.lock.detail", minute.toString()), apiName, null);
			}

			LoginLog oldLog = null;
			final Map<String, Object> rstMap = usersService.login(bundle, developer, mobile, password);
			String mobileTemp2 = mobile;
			String[] split = mobileTemp2.split("-");
			if (split.length == 1) {
				mobileTemp2 = "86-" + mobileTemp2;
			}
			if (!PhoneUtil.getInstance().check99PhoneNum(mobileTemp2)) {
				List<LoginLog> logs = loginLogDao.getRecentMarksLogs(mobile, Config.getInt("login.device.limit", 5));
				if (logs != null && mobile.equals("86-15011351329")) {
					System.out.println("手机号为86-15011351329的登录历史记录条数为：" + logs.size());
				}
				if (logs != null && logs.size() > 0 && !mobile.equals("86-15011351329")) {
					for (LoginLog log : logs) {
						if (log.getDeviceId().equals(deviceId)) {
							oldLog = log;
							break;
						}
					}

					if (oldLog == null) {
						if (StringUtil.isEmpty(sms)) {
							return toJson(DefinedError.Status.SMS_NEED.getValue(), bundle.getString("need.other.login.sms"), apiName, null);
						} else {

							try {
								Map<String, String> p = new HashMap<>();
								p.put("Appkey", appKey);
								p.put("Mobile", mobile);
								p.put("Code", sms);
								p.put("Sign", MD5Signature.signMd5(p, developer.getAppSecret()));

								String responseText = HttpClientUtil.doGet(Config.get("remote.url") + "/Api/SMSVerifyCode", p);
								JSONObject jsonObject = JSON.parseObject(responseText);
								if (!jsonObject.getBoolean("success")) {
									return toJson(DefinedError.Status.SMS_INVALID.getValue(), bundle.getString("need.sms.error"), apiName, null);
								}

							} catch (DefinedError ex) {
								return toJson(DefinedError.Status.THIRD_ERROR.getValue(), ex.getReadableMsg(), apiName, null, ex.getMessage());
							}

						}
					}
				}
			}
			final LoginInfo loginInfo = new LoginInfo();
			loginInfo.setSessionId(request.getSession().getId());
			loginInfo.setDeviceId(deviceId);
			loginInfo.setDeviceName(deviceName);
			loginInfo.setIp(ip);
			loginInfo.setMobile(mobile);
			loginInfo.setAppKey(appKey);
			loginInfo.setSms(sms);
			loginInfo.setLoginTime(new Date().getTime());

			// 登录成功的处理

			final UserDetails userDetails = new UserDetails();
			userDetails.setUuid(rstMap.get("UUID").toString());
			userDetails.setNikeName(rstMap.get("NikeName").toString());
			userDetails.setMobile(rstMap.get("Mobile").toString());

			List<PublicUser> userList = publicUserService.getByUuid(userDetails.getUuid());
			if (null != userList && userList.size() > 0) {
				PublicUser publicUser = userList.get(0);
				//登录设置客户端ID
				userDetails.setClientId(clientId);
				userDetails.setUserId(publicUser.getId());
				userDetails.setUserName(publicUser.getUserName());
				userDetails.setUserKey(publicUser.getUserKey());
				userDetails.setDescription(publicUser.getDescription());
				userDetails.setAvatarUrl(Misc.getServerUri(request, publicUser.getAvatarUrl()));
				userDetails.setBackgroundImgUrl(Misc.getServerUri(request, publicUser.getBackgroundImgUrl()));
				userDetails.setThumbAvatarUrl(Misc.getServerUri(request, publicUser.getThumbAvatarUrl()));
			}

			SessionUtil.addUserDetails(request, userDetails);
			SessionUtil.setLoginInfo(request, loginInfo);

			MobileSessionCache.getInstance().isValidSession(request.getSession(), true, new INotify() {
				@Override
				public void callback(LoginInfo cache) {

					System.out.println(JSON.toJSONString(cache));

					PackBundle bundle = LangResource.getResourceBundle(request);

					// todo 踢掉其它用户的通知，新设备则短信提示+mqtt，否则仅仅mqtt
					Date loginTime = new Date(loginInfo.getLoginTime());

					// 设备不一致，发mqtt
					if (!deviceId.equals(cache.getDeviceId())) {
						System.out.println("设备变更，mqtt提示");

						Message<Map<String, String>> message = new Message<>();
						message.setContentType(Message.ContentType.CONTENT_CIRCLE_LOGIN.getValue());
						message.setMessageType(Message.MessageType.MESSAGE_CIRCLE_LOGIN.getValue());
						message.setVersion(1);
						message.setUpdateTime(new Date().getTime());
						message.setSessionType(Message.SessionType.SESSION_P2P.getValue());
						message.setSessionId(cache.getUserId());
						message.setMessageId(Misc.getMessageId());
						message.setFromUserId(userDetails.getUserId());
						message.setFromClientId(Misc.getClientId());

						String alert = bundle.getString("user.other.login", DateUtil.toString(loginTime, DateUtil.Format.DATE_TIME_HYPHEN), deviceName);

						Map<String, String> content = new HashMap<>();
						content.put("text", alert);
						content.put("deviceId", deviceId);
						content.put("deviceName", deviceName);
						message.setContent(content);

						//MessagePublisher.getInstance().addPublish(Message.DOWNLINK_MESSAGE + cache.getUserId(), message);
						try {
							com.taiyiyun.passport.mqtt.v2.MessagePublisher.getInstance().publish(Message.UPLINK_MESSAGE + cache.getUserId(), message);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					String mobileTemp = rstMap.get("Mobile").toString();
					String[] split = mobileTemp.split("-");
					if (split.length == 1) {
						mobileTemp = "86-" + mobileTemp;
					}
					// 如果是新设备，则短信通知
					if (!PhoneUtil.getInstance().check99PhoneNum(mobileTemp)) {// 对于99开头的中国账号，跳过新设备验证
						List<LoginLog> list = loginLogDao.getRecentMarksLogs(mobileTemp, Config.getInt("login.device.limit", 5));
						if (list.size() > 0) {
							LoginLog find = null;
							for (LoginLog log : list) {
								if (deviceId.equals(log.getDeviceId())) {
									find = log;
									break;
								}
							}
							if (find == null) {
								System.out.println("新设备，需要发送短信");
								if (!StringUtil.isEmpty(cache.getMobile())) {// 如果手机号是中文的话，用中文发送
									if (mobileTemp.contains("86-")) {
										SmsYPClient.singleSendSMS(cache.getMobile(), ModelType.SMS_YP_SIGNNAME_OTHERCLIENT_CN,
												DateUtil.toString(loginTime, DateUtil.Format.DATE_TIME_HYPHEN), deviceName);
									} else {
										SmsYPClient.singleSendSMS(cache.getMobile(), ModelType.SMS_YP_SIGNNAME_OTHERCLIENT_EN,
												DateUtil.toString(loginTime, DateUtil.Format.DATE_TIME_HYPHEN), deviceName);
									}
								}
							}
						}
					}
				}
			});

			passwordLockService.releaseLock(PASSWORD_ERROR, mobile);

			LoginLog newLog = new LoginLog();
			newLog.setDeviceId(deviceId);
			newLog.setDeviceName(deviceName);
			newLog.setGps(gps);
			newLog.setIp(ip);
			newLog.setMobile(mobile);
			newLog.setUuid(rstMap.get("UUID").toString());

			// 保存日志
			if (oldLog == null) {
				newLog.setMarkTime(new Date().getTime());
			} else {
				oldLog.setMarkTime(new Date().getTime());
				loginLogDao.updateMarkTime(oldLog);
			}
			loginLogDao.newLog(newLog);
			rstMap.put("heartbeat", Config.getInt("sharelogin.heartbeat", 60));
			return toJson(DefinedError.Status.SUCC.getValue(), "", apiName, rstMap);
		} catch (DefinedError ex) {
			LoginLog failedLog = new LoginLog();
			failedLog.setDeviceId(deviceId);
			failedLog.setDeviceName(deviceName);
			failedLog.setGps(gps);
			failedLog.setIp(ip);
			failedLog.setMobile(mobile);
			loginLogDao.newFailedLog(failedLog);

			return toJson(ex.getErrorCode().getValue(), ex.getReadableMsg(), apiName, null, ex.getMessage());
		} catch (Exception ex) {
			return toJson(DefinedError.Status.OTHER_ERROR.getValue(), bundle.getString("failed.execute"), apiName, null, ex.getMessage());
		}
	}

	/**
	 * 修改密码中验证老密码
	 */
	@ResponseBody
	@RequestMapping(value = "/Api/Password", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
	public String verifyOldPwd2(String Appkey, String Mobile, String Password, String Sign, HttpServletRequest request) {

		PackBundle bundle = LangResource.getResourceBundle(request);
		
		if (StringUtils.isEmpty(Appkey)) {
			return toJson(false, bundle.getString("need.appkey"), null);
		}
		
		if (StringUtils.isEmpty(Mobile)) {
			return toJson(false, bundle.getString("need.mobile"), null);
		}
		
		if (StringUtils.isEmpty(Password)) {
			return toJson(false, bundle.getString("need.password"), null);
		}
		
		if (StringUtils.isEmpty(Sign)) {
			return toJson(false, bundle.getString("need.sign"), null);
		}
		
		String mobilePrefix = "86";
		String mobileSuffix = null;
		PhoneUtil pu = PhoneUtil.getInstance();
		String[] phones = Mobile.split("-");
		boolean flag = true;
		if (phones.length == 1) {// 默认是中文
			flag = pu.checkPhoneNumberByCountryCode(Mobile, "86");
			mobileSuffix = phones[0];
		} else if (phones.length == 2) {// 直接合法性验证
			flag = pu.checkPhoneNumberByCountryCode(phones[1], phones[0]);
			mobilePrefix = phones[0];
			mobileSuffix = phones[1];
		}
		if (!flag) {
			return toJson(false, bundle.getString("mobile.grammar.fault"), null);
		}

		try {
			Developer developer = developerService.getByAppKey(Appkey);
			if (developer == null) {
				return toJson(false, bundle.getString("article.appkey.match"), null);
			}

			Map<String, String> params = new HashMap<>();
			params.put("Appkey", Appkey);
			params.put("Mobile", Mobile);
			params.put("Password", Password);
			if (!Sign.equals(MD5Signature.signMd5(params, developer.getAppSecret()))) {
				return toJson(false, bundle.getString("failed.sign"), null);
			}

			Users users = usersService.getByMobile(mobilePrefix, mobileSuffix);
			if (users == null) {
				return toJson(false, bundle.getString("user.not.register"), null);
			}
			
			if (users.getStatus() == 0) {
				return toJson(false, "该账户已经冻结", null);
			}
			
			String pwd = AESUtil.decrypt(Password, developer.getAppSecret());
			
			if (!MD5Util.MD5Encode(pwd, false).equals(users.getPwd())) {
				return toJson(false, bundle.getString("password.old.fault"), null);
			}

			return toJson(true, bundle.getString("successful.search"), null);
		} catch (Exception ex) {
			return toJson(false, bundle.getString("failed.execute"), new HashMap<>());
		}
	}
	
	/**
	 * 发送短信验证码
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/Api/SendSMSCode", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
	public String sendSMSCode(String Mobile, String Appkey, String Type, String Sign, HttpServletRequest request) {

		PackBundle bundle = LangResource.getResourceBundle(request);

		if (StringUtils.isEmpty(Appkey)) {
			return toJson(false, bundle.getString("need.appkey"), null);
		}

		if (StringUtils.isEmpty(Mobile)) {
			return toJson(false, bundle.getString("need.mobile"), null);
		}

		if (StringUtils.isEmpty(Type)) {
			return toJson(false, bundle.getString("need.param").replace("{0}", "Type"), null);
		}

		if (StringUtils.isEmpty(Sign)) {
			return toJson(false, bundle.getString("need.sign"), null);
		}

		String mobilePrefix = "86";
		String mobileSuffix = null;
		PhoneUtil pu = PhoneUtil.getInstance();
		String[] phones = Mobile.split("-");
		boolean flag = true;
		if (phones.length == 1) {// 默认是中文
			flag = pu.checkPhoneNumberByCountryCode(Mobile, "86");
			mobileSuffix = phones[0];
		} else if (phones.length == 2) {// 直接合法性验证
			flag = pu.checkPhoneNumberByCountryCode(phones[1], phones[0]);
			mobilePrefix = phones[0];
			mobileSuffix = phones[1];
		}
		if (!flag) {
			return toJson(false, bundle.getString("mobile.grammar.fault"), null);
		}

		try {
			Developer developer = developerService.getByAppKey(Appkey);
			if (developer == null) {
				return toJson(false, bundle.getString("article.appkey.match"), null);
			}
			
			Map<String, String> params = new HashMap<>();
			params.put("Mobile", Mobile);
			params.put("Appkey", Appkey);
			params.put("Type", Type);
			
			if (!Sign.equals(MD5Signature.signMd5(params, developer.getAppSecret()))) {
				return toJson(false, bundle.getString("failed.sign"), null);
			}

			Users users = usersService.getByMobile(mobilePrefix, mobileSuffix);
			if (Type.equals("0") && users == null) {
				return toJson(false, bundle.getString("user.not.register"), null);
			}
			
			if (Type.equals("1") && users != null) {
				return toJson(false, bundle.getString("need.mobile.register"), null);
			}
			
			if (Type.equals("0") && users.getStatus() == 0) {
				return toJson(false, "该账户已经冻结", null);
			}

			if (!customerService.checkAllowSendVerifyCode(Mobile)) {
				return toJson(false, "短信发送太频繁，请一分钟之后再试", null);
			}

			String verifyCode = RandomUtil.getFixLenthString(7);
			if (!SMSHelper.sendVerifyCode(Mobile, verifyCode)) {
				return toJson(false, "验证码发送失败", null);
			}
			customerService.saveVerifyCode(Mobile, verifyCode);

			return toJson(false, "验证码发送成功", null);

		} catch (Exception ex) {
			return toJson(false, bundle.getString("failed.execute"), new HashMap<>());
		}
	}
	
	/**
	 * 修改密码
	 */
	@ResponseBody
	@RequestMapping(value = "/Api/Password", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
	public String verifyOldPwd1(String Appkey, String Mobile, String NewPassword, String OldPassword, String Code, String Sign, HttpServletRequest request) {

		PackBundle bundle = LangResource.getResourceBundle(request);

		if (StringUtils.isEmpty(Appkey)) {
			return toJson(false, bundle.getString("need.appkey"), null);
		}

		if (StringUtils.isEmpty(Mobile)) {
			return toJson(false, bundle.getString("need.mobile"), null);
		}

		if (StringUtils.isEmpty(NewPassword)) {
			return toJson(false, bundle.getString("need.password"), null);
		}

		if (StringUtils.isEmpty(Code)) {
			return toJson(false, bundle.getString("need.sms"), null);
		}

		if (StringUtils.isEmpty(Sign)) {
			return toJson(false, bundle.getString("need.sign"), null);
		}

		String mobilePrefix = "86";
		String mobileSuffix = null;
		PhoneUtil pu = PhoneUtil.getInstance();
		String[] phones = Mobile.split("-");
		boolean flag = true;
		if (phones.length == 1) {// 默认是中文
			flag = pu.checkPhoneNumberByCountryCode(Mobile, "86");
			mobileSuffix = phones[0];
		} else if (phones.length == 2) {// 直接合法性验证
			flag = pu.checkPhoneNumberByCountryCode(phones[1], phones[0]);
			mobilePrefix = phones[0];
			mobileSuffix = phones[1];
		}
		if (!flag) {
			return toJson(false, bundle.getString("mobile.grammar.fault"), null);
		}

		try {
			Developer developer = developerService.getByAppKey(Appkey);
			if (developer == null) {
				return toJson(false, bundle.getString("article.appkey.match"), null);
			}
			
			StringBuffer signBf = new StringBuffer();
			signBf.append(developer.getAppSecret()).append("Appkey").append(Appkey).append("Code").append(Code);
			signBf.append("Mobile").append(Mobile).append("NewPassword").append(NewPassword).append("OldPassword");
			signBf.append(StringUtils.isEmpty(OldPassword) ? "" : OldPassword).append(developer.getAppSecret());
			
			if (!Sign.equals(MD5Util.MD5Encode(signBf.toString()))) {
				return toJson(false, bundle.getString("failed.sign"), null);
			}
			
			if (!customerService.verifyCode(Mobile, Code, true)) {
				return toJson(false, bundle.getString("need.sms.error"), null);
			}

			Users users = usersService.getByMobile(mobilePrefix, mobileSuffix);
			if (users == null) {
				return toJson(false, bundle.getString("user.not.register"), null);
			}
			
			if (users.getStatus() == 0) {
				return toJson(false, "该账户已经冻结", null);
			}
			
			if (StringUtils.isNotEmpty(OldPassword)) {
				if (!users.getPwd().equals(AESUtil.decrypt(OldPassword, developer.getAppSecret()))) {
					return toJson(false, bundle.getString("password.old.fault"), null);
				}
			}
			
			String pwd = AESUtil.decrypt(NewPassword, developer.getAppSecret());
			if (!Pattern.matches("^(?![0-9]+$)(?![a-zA-Z]+$)[a-zA-Z0-9]{6,20}", pwd)) {
				return toJson(false, bundle.getString("password.grammar.fault"), null);
			}
			
			users.setPwd(MD5Util.MD5Encode(pwd, false));
			int count = usersService.updateUserPwd(users);
			
			if (count > 0) {
				redisService.evict(Const.TRANS_ID + Mobile);
				
				// 修改成功，强制下线
				MobileSessionCache.getInstance().setCurrentSessionStatus(Mobile, DefinedError.Status.USER_PASSWORD_CHANGED.getValue());

				LoginInfo loginInfo = MobileSessionCache.getInstance().getCurrentSession(Mobile);
				if (loginInfo != null) {
					System.out.println("设备变更，mqtt提示");

					Message<Map<String, String>> message = new Message<>();
					message.setContentType(Message.ContentType.CONTENT_CIRCLE_LOGIN.getValue());
					message.setMessageType(Message.MessageType.MESSAGE_CIRCLE_LOGIN.getValue());
					message.setVersion(1);
					message.setUpdateTime(new Date().getTime());
					message.setSessionType(Message.SessionType.SESSION_P2P.getValue());
					message.setSessionId(loginInfo.getUserId());
					message.setMessageId(Misc.getMessageId());
					message.setFromUserId(loginInfo.getUserId());
					message.setFromClientId(Misc.getClientId());

					// Date loginTime = new Date(loginInfo.getLoginTime());

					// 除非修改密码加入设备参数，否则只能有这样的提示。

					String alert = bundle.getString("user.password.out");
					Map<String, String> content = new HashMap<>();
					content.put("text", alert);
					content.put("deviceId", "");
					content.put("deviceName", "");
					message.setContent(content);

					//MessagePublisher.getInstance().addPublish(Message.DOWNLINK_MESSAGE + loginInfo.getUserId(), message);
					MessagePublisher.getInstance().publish(Message.UPLINK_MESSAGE + loginInfo.getUserId(), message);
				}
				return toJson(true, bundle.getString("successful.modify"), null);
			}

			return toJson(false, bundle.getString("failed.modify"), null);
		} catch (Exception ex) {
			ex.printStackTrace();
			return toJson(false, bundle.getString("failed.execute"), new HashMap<>());
		}
	}
}
