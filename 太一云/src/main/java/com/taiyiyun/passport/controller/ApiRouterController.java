package com.taiyiyun.passport.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.taiyiyun.passport.bean.SessionTimeoutException;
import com.taiyiyun.passport.bean.UserDetails;
import com.taiyiyun.passport.commons.PassPortUtil;
import com.taiyiyun.passport.consts.Config;
import com.taiyiyun.passport.consts.Const;
import com.taiyiyun.passport.dao.ILoginAppDao;
import com.taiyiyun.passport.dao.ILoginLogDao;
import com.taiyiyun.passport.exception.DefinedError;
import com.taiyiyun.passport.exception.router.RouterException;
import com.taiyiyun.passport.language.LangResource;
import com.taiyiyun.passport.language.PackBundle;
import com.taiyiyun.passport.language.ThirdTranslate;
import com.taiyiyun.passport.mqtt.Message;
import com.taiyiyun.passport.po.LoginApp;
import com.taiyiyun.passport.po.LoginLog;
import com.taiyiyun.passport.po.PublicUser;
import com.taiyiyun.passport.po.SharePoint;
import com.taiyiyun.passport.service.IPasswordLockService;
import com.taiyiyun.passport.service.IPublicUserService;
import com.taiyiyun.passport.service.IRedisService;
import com.taiyiyun.passport.service.ISharePointService;
import com.taiyiyun.passport.service.session.INotify;
import com.taiyiyun.passport.service.session.LoginInfo;
import com.taiyiyun.passport.service.session.MobileSessionCache;
import com.taiyiyun.passport.sms.ModelType;
import com.taiyiyun.passport.sms.SmsYPClient;
import com.taiyiyun.passport.sqlserver.po.CustomerDetailPerson;
import com.taiyiyun.passport.sqlserver.po.EntityDetail;
import com.taiyiyun.passport.sqlserver.po.PictureTemp;
import com.taiyiyun.passport.sqlserver.po.Users;
import com.taiyiyun.passport.sqlserver.service.IRealNameAuthService;
import com.taiyiyun.passport.util.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ApiRouterController extends BaseController {

    public final Logger logger = LoggerFactory.getLogger(getClass());
    private static Log log = LogFactory.getLog(ApiRouterController.class);
    @Resource
    private IPublicUserService publicUserService;

    @Resource
    private ISharePointService sharePointService;

    @Resource
    private IRedisService redisService;

    @Resource
    private IPasswordLockService passwordLockService;

    @Resource
    private IRealNameAuthService rnAuthService;

    private final String PASSWORD_ERROR = "login.error";

    @Resource
    private ILoginAppDao loginAppDao;

    @Resource
    private ILoginLogDao loginLogDao;

    private String getErrorResponse(Exception ex) {
        HashMap<String, Object> rst = new HashMap<>();
        rst.put("success", false);
        rst.put("error", ex.toString());
        rst.put("data", null);
        rst.put("errorCode", 2);
        return JSON.toJSONString(rst);
    }

    private String getSuccessResponse(boolean success, String error, Object data, Integer errorCode) {
        HashMap<String, Object> rst = new HashMap<>();
        rst.put("success", success);
        rst.put("error", error);
        rst.put("data", data);
        rst.put("errorCode", errorCode);
        return JSON.toJSONString(rst, SerializerFeature.WriteMapNullValue);
    }

    private Map<String, Object> getSuccessMap(boolean success, String error, Object data, Integer errorCode) {
        HashMap<String, Object> rst = new HashMap<>();
        rst.put("success", success);
        rst.put("error", error);
        rst.put("data", data);
        rst.put("errorCode", errorCode);
        return rst;
    }


    @ResponseBody
    @RequestMapping(value = "/Api/Mobile", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String validmobile(HttpServletRequest request, @RequestParam("Mobile") String phone, @RequestParam("Sign") String sign,
                              @RequestParam("Appkey") String appKey) {

        PackBundle bundle = LangResource.getResourceBundle(request);

        if (StringUtil.isEmpty(phone)) {
            return toJson(false, bundle.getString("need.mobile"), null);
        }
        PhoneUtil pu = PhoneUtil.getInstance();
        String[] phones = phone.split("-");
        boolean flag = true;
        if (phones.length == 1) {//默认是中文
            flag = pu.checkPhoneNumberByCountryCode(phone, "86");
        } else if (phones.length == 2) {//直接合法性验证
            flag = pu.checkPhoneNumberByCountryCode(phones[1], phones[0]);
        }
        if (!flag) {
            return toJson(false, bundle.getString("mobile.grammar.fault"), null);
        }
        Map<String, String> params = new HashMap<String, String>();
        params.put("Appkey", appKey);
        params.put("Mobile", phone);
        params.put("sign", sign);

        try {
            String rst = HttpClientUtil.doGet(Config.get("remote.url") + "/Api/Mobile", params);

            return ThirdTranslate.getInstance().translateThirdReturn(bundle, rst);

        } catch (Exception ex) {
            return getErrorResponse(ex);
        }

    }

    @ResponseBody
    @RequestMapping(value = "/Api/SMSVerifyCode", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String smsVerifyCode1(HttpServletRequest request, @RequestParam("Mobile") String phone, @RequestParam("Sign") String sign,
                                 @RequestParam("Appkey") String appKey) {

        PackBundle bundle = LangResource.getResourceBundle(request);

        if (StringUtil.isEmpty(phone)) {
            return toJson(false, bundle.getString("need.mobile"), null);
        }

        PhoneUtil pu = PhoneUtil.getInstance();
        String[] phones = phone.split("-");
        boolean flag = true;
        if (phones.length == 1) {//默认是中文
            flag = pu.checkPhoneNumberByCountryCode(phone, "86");
        } else if (phones.length == 2) {//直接合法性验证
            flag = pu.checkPhoneNumberByCountryCode(phones[1], phones[0]);
        }
        if (!flag) {
            return toJson(false, bundle.getString("mobile.grammar.fault"), null);
        }

        Map<String, String> params = new HashMap<String, String>();
        params.put("Appkey", appKey);
        params.put("Mobile", phone);
        params.put("Sign", sign);

        try {
            String rst = HttpClientUtil.doPost(Config.get("remote.url") + "/Api/SMSVerifyCode", params);
            return ThirdTranslate.getInstance().translateThirdReturn(bundle, rst);
        } catch (Exception ex) {
            return getErrorResponse(ex);
        }
    }

    @ResponseBody
    @RequestMapping(value = "/Api/SMSVerifyCode", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String smsVerifyCode2(HttpServletRequest request, @RequestParam("Mobile") String phone, @RequestParam("Sign") String sign,
                                 @RequestParam("Appkey") String appKey, @RequestParam("Code") String code) {

        PackBundle bundle = LangResource.getResourceBundle(request);

        if (StringUtil.isEmpty(phone)) {
            return toJson(false, bundle.getString("need.mobile"), null);
        }

        PhoneUtil pu = PhoneUtil.getInstance();
        String[] phones = phone.split("-");
        boolean flag = true;
        if (phones.length == 1) {//默认是中文
            flag = pu.checkPhoneNumberByCountryCode(phone, "86");
        } else if (phones.length == 2) {//直接合法性验证
            flag = pu.checkPhoneNumberByCountryCode(phones[1], phones[0]);
        }
        if (!flag) {
            return toJson(false, bundle.getString("mobile.grammar.fault"), null);
        }

        Map<String, String> params = new HashMap<String, String>();
        params.put("Appkey", appKey);
        params.put("Mobile", phone);
        params.put("Sign", sign);
        params.put("Code", code);

        try {
            String rst = HttpClientUtil.doGet(Config.get("remote.url") + "/Api/SMSVerifyCode", params);
            return ThirdTranslate.getInstance().translateThirdReturn(bundle, rst);
        } catch (Exception ex) {
            return getErrorResponse(ex);
        }
    }


    /**
     * 用户注册，其中的Sign不包括DeviceId和GPS
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/Api/ShareRegist", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String doRegister(HttpServletRequest request, @RequestParam("Mobile") String phone,@RequestParam(name="ClientId",required=false) String clientId, @RequestParam("Sign") String sign,
                             @RequestParam("Appkey") String appKey, @RequestParam("Password") String password, @RequestParam("VerifyCode") String verifyCode,
                             @RequestParam("DeviceId") String deviceId, @RequestParam(value = "DeviceName", required = false) String deviceName,
                             @RequestParam(value = "GPS", required = false) String gps) {

        PackBundle bundle = LangResource.getResourceBundle(request);

        String ip = RequestUtil.getIpAddr(request);

        if (StringUtil.isEmpty(phone)) {
            return toJson(false, bundle.getString("need.mobile"), null);
        }

        PhoneUtil pu = PhoneUtil.getInstance();
        String[] phones = phone.split("-");
        boolean flag = true;
        if (phones.length == 1) {//默认是中文
            flag = pu.checkPhoneNumberByCountryCode(phone, "86");
        } else if (phones.length == 2) {//直接合法性验证
            flag = pu.checkPhoneNumberByCountryCode(phones[1], phones[0]);
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

        if (StringUtil.isEmpty(verifyCode)) {
            return toJson(false, bundle.getString("need.sms"), null);
        }


        try {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("Appkey", appKey);
            params.put("Mobile", phone);
            params.put("password", password);
            params.put("sign", sign);
            params.put("VerifyCode", verifyCode);

            String responseText = HttpClientUtil.doJsonPost(Config.get("remote.url") + "/Api/CustomerRegist", params);

            JSONObject json = (JSONObject) JSONObject.parse(responseText);
            if (StringUtil.isNotEmpty(json.get("Message"))) {
                return toJson(false, bundle.getString("need.http.null"), null);
            }
            if (json.get("success").equals(false)) {
                return toJson(false, json.getString("error"), null);
            }

            JSONObject data = (JSONObject) json.get("data");
            if (json.get("success").equals(true) && StringUtil.isNotEmpty(data)) {
                if (data.containsKey("myOutPutUser")) {

                    JSONObject userData = (JSONObject) data.get("myOutPutUser");
                    if (null != data && userData.containsKey("UUID") && StringUtil.isNotEmpty(userData.getString("UUID"))) {
                        SharePoint bean = new SharePoint();
                        bean.setBalance(BigDecimal.ZERO);
                        bean.setUuid(userData.getString("UUID"));
                        sharePointService.save(bean);

                        UserDetails userDetails = new UserDetails();
                        //注册时候，设置clientId
                        userDetails.setClientId(clientId);
                        userDetails.setUuid(userData.getString("UUID"));
                        userDetails.setNikeName(userData.getString("NikeName"));
                        userDetails.setMobile(userData.getString("Mobile"));

                        SessionUtil.addUserDetails(request, userDetails);

                        LoginInfo loginInfo = new LoginInfo();
                        loginInfo.setSessionId(request.getSession().getId());
                        loginInfo.setSms(verifyCode);
                        loginInfo.setAppKey(appKey);
                        loginInfo.setMobile(phone);
                        loginInfo.setLoginTime(new Date().getTime());
                        loginInfo.setDeviceName(deviceName);
                        loginInfo.setIp(ip);

                        SessionUtil.setLoginInfo(request, loginInfo);

                        LoginLog log = new LoginLog();
                        log.setMarkTime(new Date().getTime());
                        log.setUuid(userData.getString("UUID"));
                        log.setMobile(userData.getString("Mobile"));
                        log.setIp(ip);
                        log.setGps(gps);
                        log.setDeviceId(deviceId);
                        log.setDeviceName(deviceName);

                        loginLogDao.newLog(log);

                        MobileSessionCache.getInstance().isValidSession(request.getSession(), true, null);
                    }
                }
            }

            //return responseText;
            return ThirdTranslate.getInstance().translateThirdReturn(bundle, responseText);
        } catch (Exception ex) {
            return getErrorResponse(ex);
        }

    }

//    /**
//     * 注册测试
//     * @param request
//     * @param uuid
//     * @return
//     */
//    @ResponseBody
//    @RequestMapping(value="/api/registertest", method={RequestMethod.GET}, produces={MediaType.APPLICATION_JSON_UTF8_VALUE})
//    public String registertest(String uuid, HttpServletRequest request) throws Exception {
//        PackBundle bundle = LangResource.getResourceBundle(request);
//        publicUserService.registerPrize(bundle,uuid);
//        return "success";
//    }

    @ResponseBody
    @RequestMapping(value = "/Api/CustomerRegist", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String doRegister(HttpServletRequest request, @RequestParam("Mobile") String phone, @RequestParam("Sign") String sign,
                             @RequestParam("Appkey") String appKey, @RequestParam("Password") String password, @RequestParam("VerifyCode") String verifyCode) {

        PackBundle bundle = LangResource.getResourceBundle(request);

        String ip = RequestUtil.getIpAddr(request);
        String deviceId = ip;


        if (StringUtil.isEmpty(phone)) {
            return toJson(false, bundle.getString("need.mobile"), null);
        }

        PhoneUtil pu = PhoneUtil.getInstance();
        String[] phones = phone.split("-");
        boolean flag = true;
        if (phones.length == 1) {//默认是中文
            flag = pu.checkPhoneNumberByCountryCode(phone, "86");
        } else if (phones.length == 2) {//直接合法性验证
            flag = pu.checkPhoneNumberByCountryCode(phones[1], phones[0]);
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

        if (StringUtil.isEmpty(verifyCode)) {
            return toJson(false, bundle.getString("need.sms"), null);
        }

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("Appkey", appKey);
        params.put("Mobile", phone);
        params.put("password", password);
        params.put("sign", sign);
        params.put("VerifyCode", verifyCode);

        try {
            String responseText = HttpClientUtil.doJsonPost(Config.get("remote.url") + "/Api/CustomerRegist", params);

            JSONObject json = (JSONObject) JSONObject.parse(responseText);
            if (StringUtil.isNotEmpty(json.get("Message"))) {
                return toJson(false, bundle.getString("need.http.null"), null);
            }
            if (json.get("success").equals(false)) {
                return toJson(false, json.getString("error"), null);
            }

            JSONObject data = (JSONObject) json.get("data");
            if (json.get("success").equals(true) && StringUtil.isNotEmpty(data)) {
                if (data.containsKey("myOutPutUser")) {

                    JSONObject userData = (JSONObject) data.get("myOutPutUser");
                    if (null != data && userData.containsKey("UUID") && StringUtil.isNotEmpty(userData.getString("UUID"))) {
                        SharePoint bean = new SharePoint();
                        bean.setBalance(BigDecimal.ZERO);
                        bean.setUuid(userData.getString("UUID"));
                        sharePointService.save(bean);

                        UserDetails userDetails = new UserDetails();
                        userDetails.setUuid(userData.getString("UUID"));
                        userDetails.setNikeName(userData.getString("NikeName"));
                        userDetails.setMobile(userData.getString("Mobile"));
                        SessionUtil.addUserDetails(request, userDetails);

                        LoginInfo loginInfo = new LoginInfo();
                        loginInfo.setSessionId(request.getSession().getId());
                        loginInfo.setIp(ip);
                        loginInfo.setLoginTime(new Date().getTime());
                        loginInfo.setMobile(phone);
                        loginInfo.setAppKey(appKey);
                        loginInfo.setDeviceId(deviceId);

                        SessionUtil.setLoginInfo(request, loginInfo);
                    }
                }
            }


            //return responseText;
            return ThirdTranslate.getInstance().translateThirdReturn(bundle, responseText);
        } catch (Exception ex) {
            return getErrorResponse(ex);
        }
    }

    @ResponseBody
    @RequestMapping(value = "/Api/IDCardFront", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String iDCardFront(HttpServletRequest request, @RequestParam("Address") String address, @RequestParam("Sign") String sign,
                              @RequestParam("Appkey") String appKey, @RequestParam("imgBase64_NoSign") String imgBase64_NoSign) {

        final PackBundle bundle = LangResource.getResourceBundle(request);

        Map<String, String> params = new HashMap<String, String>();
        params.put("Appkey", appKey);
        params.put("Address", address);
        params.put("imgBase64_NoSign", imgBase64_NoSign);
        params.put("sign", sign);
        try {

            String rst = HttpClientUtil.doPost(Config.get("remote.url") + "/Api/IDCardFront", params);
            return ThirdTranslate.getInstance().translateThirdReturn(bundle, rst);
        } catch (Exception ex) {
            return getErrorResponse(ex);
        }
    }

    @ResponseBody
    @RequestMapping(value = "/api/idCardFrontBack", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String idCardFrontBack(HttpServletRequest request, @RequestParam("address") final String address, @RequestParam("sign") String sign,
                                  @RequestParam("appKey") String appKey, @RequestParam("idCardPhotoBase64Front") String idCardPhotoBase64Front, @RequestParam("idCardPhotoBase64Back") final String idCardPhotoBase64Back) {

        final PackBundle bundle = LangResource.getResourceBundle(request);

        Map<String, String> params = new HashMap<>();
        params.put("Appkey", appKey);
        params.put("Address", address);
        params.put("imgBase64_NoSign", idCardPhotoBase64Front);
        params.put("sign", sign);
        //1.处理反面照片
        UserDetails userDetails = SessionUtil.getUserDetails(request);
        Map<String, Object> resultBack = idCardBack(userDetails.getUuid(), address, idCardPhotoBase64Back, bundle);
        if (!(boolean) resultBack.get("success")) {
            return JSON.toJSONString(resultBack, SerializerFeature.WriteMapNullValue);
        }
        //2.处理正面
        try {
            String rst = HttpClientUtil.doPost(Config.get("remote.url") + "/Api/IDCardFront", params);
            if(StringUtils.isNotEmpty(rst)) {
                return ThirdTranslate.getInstance().translateThirdReturn(bundle, rst);
            }
        } catch (DefinedError definedError) {
            definedError.printStackTrace();
            return getErrorResponse(definedError);
        }
        return getSuccessResponse(true, null, null, null);
    }

    private Map<String, Object> checkAddress(String Address, PackBundle bundle) {
        Map<String, Object> result = null;
        Users users = rnAuthService.getUserFromAddress(Address);
        if (users == null) {
            result = new HashMap<>();
            result.put("success", false);
            result.put("error", bundle.getString("user.address.fault"));
            result.put("data", null);
            result.put("errorCode", null);
        }
        return result;
    }

    private Map<String, Object> idCardBack(String UUID, String Address, String imgBase64Back_NoSign, PackBundle bundle) {
        Map<String, Object> resultBack = new HashMap<>();
        resultBack.put("success", true);
        resultBack.put("error", null);
        resultBack.put("data", null);
        resultBack.put("errorCode", null);
        Map<String, Object> checkResult = checkAddress(Address, bundle);
        if (checkResult != null) {
            return checkResult;
        }
        PictureTemp pictureTemp = rnAuthService.getPictureTemp(UUID, 2);
        if (pictureTemp != null && pictureTemp.getFinished() == 1) {
            resultBack.put("success", false);
            resultBack.put("error", bundle.getString("picture.modify.deny.register"));
            return resultBack;
        }
        String filePath;
        try {
            String oldPath = null;
            if(pictureTemp != null) {
                oldPath = pictureTemp.getFileName();
            }
            filePath = PassPortUtil.saveIdCardBack(imgBase64Back_NoSign, oldPath);
        } catch (IOException e) {
            e.printStackTrace();
            resultBack.put("success", false);
            resultBack.put("error", bundle.getString("photo.file.save.failed"));
            return resultBack;
        }
        if (pictureTemp == null) {
            PictureTemp picTemp = new PictureTemp();
            picTemp.setCreationTime(new Date());
            picTemp.setFileName(filePath);
            picTemp.setFinished(0);
            picTemp.setPTypeID(2);
            picTemp.setUUID(UUID);
            rnAuthService.insertPictureTemp(picTemp);
        } else {
            pictureTemp.setCreationTime(new Date());
            rnAuthService.updatePictureTemp(pictureTemp);
        }
        return resultBack;
    }
    @ResponseBody
    @RequestMapping(value = "/api/fillUpRealNameInfo", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String identityCardNew(HttpServletRequest request, @RequestParam("address") String address, @RequestParam("identityNumber") String identityNumber, @RequestParam("userName") String userName, @RequestParam("validDateStart") String validDateStart, @RequestParam("validDateEnd") String validDateEnd
            , @RequestParam("sex") String sex, @RequestParam("nation") String nation
            , @RequestParam("birthDay") String birthDay, @RequestParam("homeAddr") String homeAddr, @RequestParam("idCardPhotoBase64Back") String idCardPhotoBase64Back) {
        PackBundle bundle = LangResource.getResourceBundle(request);
        //1.验证信息
        if(StringUtils.isEmpty(address)) {//地址
            return getSuccessResponse(false, bundle.getString("filluprealnameinfo.error.address"), null, null);
        }
        if(StringUtils.isEmpty(validDateStart)) {
            return getSuccessResponse(false, bundle.getString("filluprealnameinfo.error.validdatestart"), null, null);
        }
        if(StringUtils.isEmpty(validDateEnd)) {
            return getSuccessResponse(false, bundle.getString("filluprealnameinfo.error.validdateend"), null, null);
        }
        if(StringUtils.isEmpty(sex)) {
            return getSuccessResponse(false, bundle.getString("filluprealnameinfo.error.sex"), null, null);
        }
        if(StringUtils.isEmpty(nation)) {
            return getSuccessResponse(false, bundle.getString("filluprealnameinfo.error.nation"), null, null);
        }
        if(StringUtils.isEmpty(birthDay)) {
            return getSuccessResponse(false, bundle.getString("filluprealnameinfo.error.birthDay"), null, null);
        }
        if(StringUtils.isEmpty(homeAddr)) {
            return getSuccessResponse(false, bundle.getString("filluprealnameinfo.error.homeAddr"), null, null);
        }
        if(StringUtils.isEmpty(idCardPhotoBase64Back)) {
            return getSuccessResponse(false, bundle.getString("filluprealnameinfo.error.idcardback"), null, null);
        }
        //2.保存实名认证信息
        Map<String, Object> result = rnAuthService.fillUpRealNameInfo(idCardPhotoBase64Back, address,identityNumber, userName, validDateStart, validDateEnd, sex, nation, birthDay, homeAddr, bundle);
        return JSON.toJSONString(result);
    }

    @ResponseBody
    @RequestMapping(value = "/Api/IdentityCard", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String identityCard(HttpServletRequest request, @RequestParam("Address") String address, @RequestParam("Sign") String sign,
                               @RequestParam("Appkey") String appKey, @RequestParam("ImgData_NoSign") String ImgData_NoSign,
                               @RequestParam("IdentityNumber") String identityNumber, @RequestParam("UserName") String userName) {

        PackBundle bundle = LangResource.getResourceBundle(request);

        Map<String, String> params = new HashMap<String, String>();
        params.put("Appkey", appKey);
        params.put("Address", address);
        params.put("ImgData_NoSign", ImgData_NoSign);
        params.put("IdentityNumber", identityNumber);
        params.put("UserName", userName);
        params.put("sign", sign);
        try {

            String rst = HttpClientUtil.doPost(Config.get("remote.url") + "/Api/IdentityCard", params);
            return ThirdTranslate.getInstance().translateThirdReturn(bundle, rst);
        } catch (Exception ex) {
            return getErrorResponse(ex);
        }
    }

    @ResponseBody
    @RequestMapping(value = "/api/checkUserHasValidDate", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String checkUserHasValidDate(HttpServletRequest request, @RequestParam("appKey") String appKey, @RequestParam("sign") String sign
            , @RequestParam("mobile") String mobile, @RequestParam("mobilePrefix") String mobilePrefix) {
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
        if(!b) {
            return getSuccessResponse(false, bundle.getString("failed.sign"), null, null);
        }
        EntityDetail ed = rnAuthService.queryEntityDetailHasValidDateByMobile(mobile, mobilePrefix);
        Map<String, Object> data = new HashMap<>();
        if(ed != null) {
            data.put("hasValidDate", 0);
        } else {
            data.put("hasValidDate", 1);
        }
        result.put("data", data);
        return JSON.toJSONString(result, SerializerFeature.WriteMapNullValue);
    }


    @ResponseBody
    @RequestMapping(value = "/api/identityCardNew", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String identityCardNew(HttpServletRequest request, @RequestParam("address") String address, @RequestParam("sign") String sign,
                                  @RequestParam("appKey") String appKey, @RequestParam("facePhotoBase64") String facePhotoBase64,
                                  @RequestParam("identityNumber") String identityNumber, @RequestParam("userName") String userName
            , @RequestParam("validDateStart") String validDateStart, @RequestParam("validDateEnd") String validDateEnd
            , @RequestParam("sex") String sex, @RequestParam("nation") String nation, @RequestParam("birthDay") String birthDay
            , @RequestParam("homeAddr") String homeAddr) {
        return doIdentityCard(request, address, appKey, identityNumber, userName, validDateStart, validDateEnd, nation, sex,
                homeAddr, birthDay, facePhotoBase64, sign, "1");
    }

    @ResponseBody
    @RequestMapping(value = "/api/identityCardNewForWePass", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String identityCardNewForWePass(HttpServletRequest request, @RequestParam("address") String address, @RequestParam("sign") String sign,
                                  @RequestParam("appKey") String appKey, @RequestParam("facePhotoBase64") String facePhotoBase64,
                                  @RequestParam("identityNumber") String identityNumber, @RequestParam("userName") String userName
            , @RequestParam("validDateStart") String validDateStart, @RequestParam("validDateEnd") String validDateEnd
            , @RequestParam("sex") String sex, @RequestParam("nation") String nation, @RequestParam("birthDay") String birthDay
            , @RequestParam("homeAddr") String homeAddr) {
        return doIdentityCard(request, address, appKey, identityNumber, userName, validDateStart, validDateEnd, nation, sex,
                homeAddr, birthDay, facePhotoBase64, sign, "2");
    }

    private String doIdentityCard(HttpServletRequest request, String address, String appKey, String identityNumber,
                                  String userName, String validDateStart, String validDateEnd, String nation, String sex,
                                  String homeAddr, String birthDay, String facePhotoBase64, String sign, String national) {
        PackBundle bundle = LangResource.getResourceBundle(request);
        Map<String, Object> checkResult = checkAddress(address, bundle);
        if (checkResult != null) {
            return JSON.toJSONString(checkResult, SerializerFeature.WriteMapNullValue);
        }
        CustomerDetailPerson cdp = new CustomerDetailPerson();
        cdp.setAppKey(appKey);
        cdp.setAddress(address);
        cdp.setIDCard(identityNumber);
        cdp.setEntityName(userName);
        cdp.setName(userName);
        cdp.setValidDateStart(validDateStart);
        cdp.setValidDateEnd(validDateEnd);
        cdp.setNation(nation);
        cdp.setSex(sex);
        cdp.setHomeAddr(homeAddr);
        cdp.setBirthDay(birthDay);
//        Map<String, Object> gongAnResultMap = identityCardAuthByGongAn(cdp, facePhotoBase64, bundle);
//        boolean flag = (boolean) gongAnResultMap.get("success");
//        Integer errorCode = (Integer) gongAnResultMap.get("errorCode");
        boolean flag = false;
        Integer errorCode = 2;//设置成非1值
        if (flag) {
            return null;
            //return JSON.toJSONString(gongAnResultMap, SerializerFeature.WriteMapNullValue);
        } else {
            if(errorCode == 1) {// 不是本人 直接返回不经过云签验证
                return JSON.toJSONString(getSuccessMap(false, bundle.getString("not.same.person"), null, 1));
            } else {//其他的公安一所验证不通过的直接走云签
                System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>云签实名认证");
                log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>云签实名认证");
                Map<String, String> params = new HashMap<>();
                params.put("Appkey", appKey);
                params.put("Address", address);
                params.put("ImgData_NoSign", facePhotoBase64);
                params.put("IdentityNumber", identityNumber);
                params.put("UserName", userName);
                params.put("_NoSign_National", national);
                params.put("sign", sign);
                try {
                    String rst = HttpClientUtil.doPost(Config.get("remote.url") + "/Api/IdentityCard", params);
                    JSONObject resultObj = JSON.parseObject(rst);
                    boolean success = resultObj.getBooleanValue("success");
                    if(success) {//云签实名认证需要保存额外身份信息
                        cdp.setIDCard(null);
                        cdp.setEntityName(null);
                        cdp.setName(null);
                        rnAuthService.addOtherCustomerPerson(cdp);
                    }
                    return ThirdTranslate.getInstance().translateThirdReturn(bundle, rst);
                } catch (Exception ex) {
                    return getErrorResponse(ex);
                }
            }
        }
    }

    private Map<String, Object> identityCardAuthByGongAn(CustomerDetailPerson cd, String facePhotoBase64, PackBundle bundle) {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("imgDataBase64", facePhotoBase64);
            params.put("idNumber", cd.getIDCard());
            params.put("userName", cd.getEntityName());
            params.put("validDateStart", cd.getValidDateStart());
            params.put("validDateEnd", cd.getValidDateEnd());
            String isTwo = Config.get("gongan.auth.proxy.interface.istwo");
            String api = "/api/realNameAuthFourInfo";
            if (StringUtils.equalsIgnoreCase("0", isTwo)) {
                api = "/api/realNameAuthTwoInfo";
            }
            String url = Config.get("gongan.auth.proxy.url") + api;
            String rst = HttpClientUtil.doPost(url, params, 5000);
            JSONObject res = JSONObject.parseObject(rst);
            int status = res.getIntValue("status");
            String errorMsg = res.getString("errorMsg");
            StringBuffer sb = new StringBuffer();
            log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            sb.append("身份信息为：姓名：" + cd.getEntityName() + "身份证号：" + cd.getIDCard() + "的用户,验证时间："+DateUtil.toString(new Date(), "yyyy-MM-dd HH:mm:ss")+"，公安一所实名认证结果：");
            sb.append(errorMsg);
            log.info(sb.toString());
            if (status == 0) {//认证成功！
                log.error("公安一所实名认证成功：【用户身份证号】=" + cd.getIDCard());
            } else {
                if(status == 1) {
                    return getSuccessMap(false, bundle.getString("not.same.person"), null, 1);//errCode == 1代表 不是本人！
                } else {
                    return getSuccessMap(false, bundle.getString("authenticate.failed"), null, 2);//errCode == 2代表 验证不通过
                }
            }
        } catch (Exception e) {
            log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            log.error("公安一所实名认证失败：【用户身份证号】=" + cd.getIDCard() + "异常信息：" + e.getMessage());
            return getSuccessMap(false, bundle.getString("authenticate.failed"), null, 2);
        }
        //提交个人信息
        return addCustomerPerson(cd, facePhotoBase64, bundle);
    }

    /**
     * 提交个人信息
     * @param cd
     * @param facePhotoBase64
     * @param bundle
     */
    private Map<String, Object> addCustomerPerson(CustomerDetailPerson cd, String facePhotoBase64, PackBundle bundle) {
        cd.setPassport("");//默认设置为空字符串
        try {
            return rnAuthService.addCustomerPerson(cd, bundle, facePhotoBase64);
        } catch (IOException e) {
            e.printStackTrace();
            return getSuccessMap(false, bundle.getString("authenticate.failed"), null, null);
        }
    }

    @ResponseBody
    @RequestMapping(value = "/api/RealNameAuth", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String identityCard(HttpServletRequest request, @RequestParam("Address") String address, @RequestParam("Sign") String sign,
                               @RequestParam("Appkey") String appKey) {

        PackBundle bundle = LangResource.getResourceBundle(request);

        Map<String, String> params = new HashMap<String, String>();
        params.put("Appkey", appKey);
        params.put("Address", address);
        params.put("sign", sign);

        try {

            String rst = HttpClientUtil.doGet(Config.get("remote.url") + "/Api/RealNameAuth", params);
            return ThirdTranslate.getInstance().translateThirdReturn(bundle, rst);
        } catch (Exception ex) {
            return getErrorResponse(ex);
        }
    }

    @ResponseBody
    @RequestMapping(value = "/Api/HeadPicture", method = {RequestMethod.POST}, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String uploadHeadPicture(String Address, String Appkey, String Sign, @RequestParam("1") MultipartFile file, HttpServletRequest request) {

        PackBundle bundle = LangResource.getResourceBundle(request);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("Address", Address);
        params.put("Appkey", Appkey);
        params.put("sign", Sign);
        params.put("1", file);
        try {

            String rst = HttpClientUtil.doUpload(Config.get("remote.url") + "/Api/HeadPicture", params);
            return ThirdTranslate.getInstance().translateThirdReturn(bundle, rst);
        } catch (Exception ex) {
            return getErrorResponse(ex);
        }
    }

    @ResponseBody
    @RequestMapping(value = "/Api/CustomerDetailPerson", method = {RequestMethod.GET}, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String customerDetailPerson(String Address, String Appkey, String Sign, HttpServletRequest request) {

        PackBundle bundle = LangResource.getResourceBundle(request);

        Map<String, String> params = new HashMap<String, String>();
        params.put("Address", Address);
        params.put("Appkey", Appkey);
        params.put("sign", Sign);

        try {
            String rst = HttpClientUtil.doGet(Config.get("remote.url") + "/Api/CustomerDetailPerson", params);
            return ThirdTranslate.getInstance().translateThirdReturn(bundle, rst);
        } catch (Exception ex) {
            return getErrorResponse(ex);
        }
    }

    @RequestMapping(value = "/circle/protocol.html")
    public String protocol(String NikeName, String Address, String Appkey, String Sign, HttpServletRequest request) {
        PackBundle bundle = LangResource.getResourceBundle(request);

        return "protocol_" + bundle.getLanguage();
    }

    @ResponseBody
    @RequestMapping(value = "/Api/NikeName", method = {RequestMethod.POST}, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String updateNikeName(String NikeName, String Address, String Appkey, String Sign, HttpServletRequest request) {

        PackBundle bundle = LangResource.getResourceBundle(request);

        Map<String, String> params = new HashMap<String, String>();
        params.put("Address", Address);
        params.put("Appkey", Appkey);
        params.put("sign", Sign);
        params.put("NikeName", NikeName);
        try {

            String rst = HttpClientUtil.doPost(Config.get("remote.url") + "/Api/NikeName", params);
            return ThirdTranslate.getInstance().translateThirdReturn(bundle, rst);
        } catch (Exception ex) {
            return getErrorResponse(ex);
        }
    }

//	@ResponseBody
//	@RequestMapping(value = "/circle/api/CircleMsg2", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
//	public String getPublicShareNums(@RequestParam("circle_id") String userId, @RequestParam String uuid,
//			@RequestParam String start,
//			HttpServletRequest request) {
//		List<Map<String,Object>> jsonList = new ArrayList<Map<String,Object>>();
//		if(StringUtil.isEmpty(uuid)) {
//			return toJson("1", "uuid为空", jsonList);
//		}
//		try {
//			List<PublicUser> userList = publicUserService.getByUuid(uuid);
//			if(null != userList && userList.size() > 0) {
//				for(PublicUser user : userList) {
//					Map<String,Object> jsonMap = new HashMap<String,Object>();
//
//					jsonMap.put("PublicCircleId", user.getId());
//					jsonMap.put("PublicCircleName", user.getUserName());
//					jsonMap.put("Description", user.getDescription());
//					jsonMap.put("uuid", user.getUuid());
//					jsonMap.put("Version", user.getVersion());
//					if(StringUtil.isNotEmpty(user.getAvatarUrl())) {
//						jsonMap.put("PublicCirclePic", Misc.getServerUri(request, user.getAvatarUrl()));
//					}
//
//					if(StringUtil.isNotEmpty(user.getThumbAvatarUrl())) {
//						jsonMap.put("ThumbAvatarUrl", Misc.getServerUri(request, user.getThumbAvatarUrl()));
//					}
//					jsonMap.put("Description", user.getDescription());
//
//					if(StringUtil.isNotEmpty(user.getBackgroundImgUrl())) {
//						jsonMap.put("backgroundImgUrl", Misc.getServerUri(request, user.getBackgroundImgUrl()));
//					}
//					jsonList.add(jsonMap);
//				}
//			}
//
//			return toJson("0", "查询成功", jsonList);
//		} catch (Exception e) {
//			e.printStackTrace();
//			return toJson("2", "程序异常", jsonList);
//		}
//	}

    //暂时签名仍然不包含DeviceId, DeviceName， GPS和SMS
    @ResponseBody
    @RequestMapping(value = "/Api/ShareLogin", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String shareLogin(final HttpServletRequest request, @RequestParam("Mobile") String mobile, @RequestParam("Password") String password,
                             @RequestParam("Sign") String sign, @RequestParam("Appkey") String appKey, @RequestParam("DeviceId") final String deviceId, @RequestParam("DeviceName") final String deviceName,
                             @RequestParam(value = "GPS", required = false) String gps, @RequestParam(value = "SMS", required = false) String sms,
                             HttpServletResponse response) {

        PackBundle bundle = LangResource.getResourceBundle(request);

        String ip = RequestUtil.getIpAddr(request);


        String apiName = "/Api/ShareLogin";

        if (StringUtil.isEmpty(mobile)) {
            return toJson(DefinedError.Status.PARAM_ERROR.getValue(), bundle.getString("need.mobile"), apiName, null);
        }

        PhoneUtil pu = PhoneUtil.getInstance();
        String[] phones = mobile.split("-");
        boolean flag = true;
        if (phones.length == 1) {//默认是中文
            flag = pu.checkPhoneNumberByCountryCode(mobile, "86");
        } else if (phones.length == 2) {//直接合法性验证
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


        LoginApp loginApp = loginAppDao.getItem(appKey);
        if (loginApp == null) {
            return toJson(DefinedError.Status.PARAM_ERROR.getValue(), bundle.getString("need.param.invalid", "appKey"), apiName, null);
        }

        if (passwordLockService.checkLock(PASSWORD_ERROR, mobile)) {
            int seconds = Config.getInt(PASSWORD_ERROR + ".lock", 1800);
            Integer minute = (seconds + 59) / 60;
            return toJson(DefinedError.Status.LOCK.getValue(), bundle.getString("user.lock.detail", minute.toString()), apiName, null);
        }


        Map<String, String> remotParams = new HashMap<>();
        remotParams.put("Appkey", appKey);
        remotParams.put("Mobile", mobile);
        remotParams.put("Password", password);
        remotParams.put("Sign", sign);

        try {
            LoginLog oldLog = null;
            final Map<String, Object> rstMap = remoteLogin(request, remotParams);
            String mobileTemp2 = mobile;
            String[] split = mobileTemp2.split("-");
            if (split.length == 1) {
                mobileTemp2 = "86-" + mobileTemp2;
            }
            if (!PhoneUtil.getInstance().check99PhoneNum(mobileTemp2)) {
                List<LoginLog> logs = loginLogDao.getRecentMarksLogs(mobile, Config.getInt("login.device.limit", 5));
                if(logs != null && mobile.equals("86-15011351329")) {
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
                                p.put("Sign", MD5Signature.signMd5(p, loginApp.getAppSecret()));


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

            //登录成功的处理

            final UserDetails userDetails = new UserDetails();
            userDetails.setUuid(rstMap.get("UUID").toString());
            userDetails.setNikeName(rstMap.get("NikeName").toString());
            userDetails.setMobile(rstMap.get("Mobile").toString());

            List<PublicUser> userList = publicUserService.getByUuid(userDetails.getUuid());
            if (null != userList && userList.size() > 0) {
                PublicUser publicUser = userList.get(0);

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

                    //todo 踢掉其它用户的通知，新设备则短信提示+mqtt，否则仅仅mqtt
                    Date loginTime = new Date(loginInfo.getLoginTime());

                    //设备不一致，发mqtt
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
                    //如果是新设备，则短信通知
                    if (!PhoneUtil.getInstance().check99PhoneNum(mobileTemp)) {//对于99开头的中国账号，跳过新设备验证
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
                                if (!StringUtil.isEmpty(cache.getMobile())) {//如果手机号是中文的话，用中文发送
                                    if (mobileTemp.contains("86-")) {
                                        SmsYPClient.singleSendSMS(cache.getMobile(), ModelType.SMS_YP_SIGNNAME_OTHERCLIENT_CN, DateUtil.toString(loginTime, DateUtil.Format.DATE_TIME_HYPHEN), deviceName);
                                    } else {
                                        SmsYPClient.singleSendSMS(cache.getMobile(), ModelType.SMS_YP_SIGNNAME_OTHERCLIENT_EN, DateUtil.toString(loginTime, DateUtil.Format.DATE_TIME_HYPHEN), deviceName);
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


            //保存日志
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

    @ResponseBody
    @RequestMapping(value = "/Api/CustomerLogin", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String login(HttpServletRequest request, @RequestParam("Mobile") String phone, @RequestParam("Password") String password,
                        @RequestParam("Sign") String sign, @RequestParam("Appkey") String appKey, HttpServletResponse response) {

        PackBundle bundle = LangResource.getResourceBundle(request);

        String deviceId = RequestUtil.getIpAddr(request);
        String ip = deviceId;

        if (StringUtil.isEmpty(phone)) {
            return toJson(false, bundle.getString("need.mobile"), null);
        }

        PhoneUtil pu = PhoneUtil.getInstance();
        String[] phones = phone.split("-");
        boolean flag = true;
        if (phones.length == 1) {//默认是中文
            flag = pu.checkPhoneNumberByCountryCode(phone, "86");
        } else if (phones.length == 2) {//直接合法性验证
            flag = pu.checkPhoneNumberByCountryCode(phones[1], phones[0]);
        }
        if (!flag) {
            return toJson(false, bundle.getString("mobile.grammar.fault"), null);
        }

        if (StringUtil.isEmpty(password)) {
            return toJson(false, bundle.getString("need.password"), null);
        }

        System.out.println("》》》》》》》》》》》》》》》》》》》》/Api/CustomerLogin");

        Map<String, String> params = new HashMap<String, String>();
        params.put("Appkey", appKey);
        params.put("Mobile", phone);
        params.put("Password", password);
        //String sign = MD5Signature.signMd5(params,secret);
        params.put("Sign", sign);

        try {
            Map<String, Object> rstMap = remoteLogin(request, params);

            //登录成功的处理

            final UserDetails userDetails = new UserDetails();
            userDetails.setUuid(rstMap.get("UUID").toString());
            userDetails.setNikeName(rstMap.get("NikeName").toString());
            userDetails.setMobile(rstMap.get("Mobile").toString());

            List<PublicUser> userList = publicUserService.getByUuid(userDetails.getUuid());
            if (null != userList && userList.size() > 0) {
                PublicUser publicUser = userList.get(0);

                userDetails.setUserId(publicUser.getId());
                userDetails.setUserName(publicUser.getUserName());
                userDetails.setUserKey(publicUser.getUserKey());
                userDetails.setDescription(publicUser.getDescription());
                userDetails.setAvatarUrl(Misc.getServerUri(request, publicUser.getAvatarUrl()));
                userDetails.setBackgroundImgUrl(Misc.getServerUri(request, publicUser.getBackgroundImgUrl()));
                userDetails.setThumbAvatarUrl(Misc.getServerUri(request, publicUser.getThumbAvatarUrl()));
            }

            SessionUtil.addUserDetails(request, userDetails);

            return toJson(true, 0, rstMap);
        } catch (DefinedError ex) {
            String rst = toJson(false, 2, ex.getReadableMsg(), null);
            return rst;
        } catch (Exception ex) {
            String rst = getErrorResponse(ex);
            return rst;
        }
    }

    /**
     * 远程调用登录接口
     *
     * @param request
     * @param params
     * @return
     * @throws DefinedError
     */
    private Map<String, Object> remoteLogin(final HttpServletRequest request, Map<String, String> params) throws DefinedError {
        PackBundle bundle = LangResource.getResourceBundle(request);

        String responseText = HttpClientUtil.doGet(Config.get("remote.url") + "/Api/CustomerLogin", params);
        //responseText = "{\"data\":{\"Address\":\"14DLJPK2wzuum4XezGUywMP6wnuEkSSaGP\",\"Entitys\":[{\"EntityStatus\":0,\"GradeId\":0,\"Type\":0,\"UserEntityId\":\"8b730ada45284ad1a6080eccf01e6606\"},{\"EntityStatus\":1,\"GradeId\":0,\"Type\":1,\"UserEntityId\":\"ebda77ec181d485d8c60d262be96824e\"}],\"HeadPicture\":\"\",\"Mobile\":\"13651178890\",\"NikeName\":\"Dinstein\",\"Status\":1,\"UUID\":\"cf141e2fd3464ddc84a917c3c0153bdd\"},\"errorCode\":0,\"success\":true}";
        JSONObject json = (JSONObject) JSONObject.parse(responseText);

        if (StringUtil.isNotEmpty(json.get("Message"))) {
            throw new DefinedError.RemoteResourceNotFoundException(null, null);
        }
        if (json.get("success").equals(false)) {
            //return toJson(false, json.getString("error"), null);
            String error = json.getString("error");
            if (!StringUtil.isEmpty(error) && error.contains("密码错误")) {
                Integer times = passwordLockService.refreshError(PASSWORD_ERROR, params.get("Mobile"));
                if (times <= 0) {
                    int seconds = Config.getInt(PASSWORD_ERROR + ".lock", 1800);
                    Integer minute = (seconds + 59) / 60;
                    throw new DefinedError.PasswordLockException(bundle.getString("user.lock.detail", minute.toString()), null);
                } else {
                    throw new DefinedError.PasswordWrongException(bundle.getString("user.password.error.times", times.toString()), null);
                }
            } else {

                error = ThirdTranslate.getInstance().translateThirdText(bundle, error);

                throw new DefinedError.OtherException(error, null);
            }
        }

        final JSONObject data = (JSONObject) json.get("data");
        if (StringUtil.isEmpty(data)) {
            throw new DefinedError.OtherException(bundle.getString("failed.third.error"), null);
        }

        Map<String, Object> jsonMap = new HashMap<String, Object>();
        jsonMap.put("Mobile", data.getString("Mobile"));
        jsonMap.put("NikeName", data.getString("NikeName"));
        jsonMap.put("UUID", data.getString("UUID"));
        jsonMap.put("HeadPicture", "");
        jsonMap.put("Status", data.get("Status"));
        jsonMap.put("Address", data.get("Address"));
        jsonMap.put("Entitys", data.get("Entitys"));

        return jsonMap;
    }

    @ResponseBody
    @RequestMapping(value = "/permitCheck", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String permitCheck(HttpServletRequest request, HttpServletResponse response) {

        PackBundle bundle = LangResource.getResourceBundle(request);

        HttpSession session = SessionUtil.getCurrentSession(request);
        if (null == session) {
            throw new SessionTimeoutException();
        }
        return toJson("true", null);
    }

    @ResponseBody
    @RequestMapping(value = "/Api/IdentityCard2", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String identityCard2(String ImgData_NoSign, String Mobile, String Appkey, String Sign, HttpServletRequest request) {

        PackBundle bundle = LangResource.getResourceBundle(request);

        if (StringUtils.isNotEmpty(Mobile)) {
            PhoneUtil pu = PhoneUtil.getInstance();
            String[] phones = Mobile.split("-");
            boolean flag = true;
            if (phones.length == 1) {//默认是中文
                flag = pu.checkPhoneNumberByCountryCode(Mobile, "86");
            } else if (phones.length == 2) {//直接合法性验证
                flag = pu.checkPhoneNumberByCountryCode(phones[1], phones[0]);
            }
            if (!flag) {
                return toJson(false, bundle.getString("mobile.grammar.fault"), null);
            }
        }

        Map<String, String> params = new HashMap<>();
        params.put("ImgData_NoSign", ImgData_NoSign);
        params.put("Mobile", Mobile);
        params.put("Appkey", Appkey);
        params.put("Sign", Sign);

        try {

            String responseText = HttpClientUtil.doPost(Config.get("remote.url") + "/Api/IdentityCard2", params);

            JSONObject json = (JSONObject) JSONObject.parse(responseText);
            if (StringUtil.isNotEmpty(json.get("Message"))) {
                return toJson(false, bundle.getString("need.http.null"), null);
            }
            System.out.println(responseText);
            JSONObject data = (JSONObject) json.get("data");
            if (json.get("success").equals(true) && StringUtil.isNotEmpty(data)) {
                String transId = Misc.getUUID();
                boolean transStatus = redisService.put(Const.TRANS_ID + Mobile, transId, Config.getInt("transId.expired.time", 30 * 60));
                if (!transStatus) {
                    return toJson(false, bundle.getString("error.execute"), null);
                }
                Map<String, String> dataJson = new HashMap<>();
                dataJson.put("transId", transId);
                return toJson(true, bundle.getString("successful.operation"), dataJson);
            }

            //return responseText;
            return ThirdTranslate.getInstance().translateThirdReturn(bundle, responseText);
        } catch (Exception ex) {
            return getErrorResponse(ex);
        }
    }

    /**
     * 账号解绑
     */
    @ResponseBody
    @RequestMapping(value = "/Api/UserBinding", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String userBinding(String Token, String Appkey, String Sign, HttpServletRequest request) {

        PackBundle bundle = LangResource.getResourceBundle(request);

        Map<String, String> params = new HashMap<>();

        params.put("Token", Token);
        params.put("Appkey", Appkey);
        params.put("Sign", Sign);

        try {

            String rst = HttpClientUtil.doPost(Config.get("remote.url") + "/Api/UserBinding", params);
            return ThirdTranslate.getInstance().translateThirdReturn(bundle, rst);
        } catch (Exception ex) {
            return getErrorResponse(ex);
        }
    }

    /**
     * 账号解绑
     */
    @ResponseBody
    @RequestMapping(value = "/Api/UserBinding", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String getUserBinding(String Mobile, String Appkey, String Sign, HttpServletRequest request) {

        PackBundle bundle = LangResource.getResourceBundle(request);

        if (StringUtils.isNotEmpty(Mobile)) {
            PhoneUtil pu = PhoneUtil.getInstance();
            String[] phones = Mobile.split("-");
            boolean flag = true;
            if (phones.length == 1) {//默认是中文
                flag = pu.checkPhoneNumberByCountryCode(Mobile, "86");
            } else if (phones.length == 2) {//直接合法性验证
                flag = pu.checkPhoneNumberByCountryCode(phones[1], phones[0]);
            }
            if (!flag) {
                return toJson(false, bundle.getString("mobile.grammar.fault"), null);
            }
        }

        Map<String, String> params = new HashMap<>();

        params.put("Mobile", Mobile);
        params.put("Appkey", Appkey);
        params.put("Sign", Sign);

        try {

            String rst = HttpClientUtil.doGet(Config.get("remote.url") + "/Api/UserBinding", params);
            return ThirdTranslate.getInstance().translateThirdReturn(bundle, rst);
        } catch (Exception ex) {
            return getErrorResponse(ex);
        }
    }

    /**
     * 获取app版本
     */
    @ResponseBody
    @RequestMapping(value = "/Api/AppVersion", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String getAppVersion(String Appkey, String Version, String Sign, HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();

        PackBundle bundle = LangResource.getResourceBundle(request);

        params.put("Appkey", Appkey);
        params.put("Version", Version);
        params.put("Sign", Sign);

        try {

            String rst = HttpClientUtil.doGet(Config.get("remote.url") + "/Api/AppVersion", params);
            return ThirdTranslate.getInstance().translateThirdReturn(bundle, rst);
        } catch (Exception ex) {
            return getErrorResponse(ex);
        }
    }

    /**
     * 修改密码中验证老密码
     */
    @ResponseBody
    @RequestMapping(value = "/Api/Password", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String verifyOldPwd1(String Appkey, String Mobile, String NewPassword, String OldPassword, String Code, String Sign, HttpServletRequest request) {

        PackBundle bundle = LangResource.getResourceBundle(request);

        Map<String, String> params = new HashMap<>();
        if (StringUtils.isNotEmpty(Mobile)) {
            PhoneUtil pu = PhoneUtil.getInstance();
            String[] phones = Mobile.split("-");
            boolean flag = true;
            if (phones.length == 1) {//默认是中文
                flag = pu.checkPhoneNumberByCountryCode(Mobile, "86");
            } else if (phones.length == 2) {//直接合法性验证
                flag = pu.checkPhoneNumberByCountryCode(phones[1], phones[0]);
            }
            if (!flag) {
                return toJson(false, bundle.getString("mobile.grammar.fault"), null);
            }
        }
        params.put("Appkey", Appkey);
        params.put("Mobile", Mobile);
        params.put("NewPassword", NewPassword);
        params.put("OldPassword", OldPassword);
        params.put("Code", Code);
        params.put("Sign", Sign);

        try {
            redisService.evict(Const.TRANS_ID + Mobile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            String rst = HttpClientUtil.doPost(Config.get("remote.url") + "/Api/Password", params);

            JSONObject jsonObject = JSON.parseObject(rst);
            //修改成功，强制下线
            if (jsonObject.getBoolean("success")) {
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

                    //Date loginTime = new Date(loginInfo.getLoginTime());

                    //除非修改密码加入设备参数，否则只能有这样的提示。

                    String alert = bundle.getString("user.password.out");
                    Map<String, String> content = new HashMap<>();
                    content.put("text", alert);
                    content.put("deviceId", "");
                    content.put("deviceName", "");
                    message.setContent(content);

                    //MessagePublisher.getInstance().addPublish(Message.DOWNLINK_MESSAGE + loginInfo.getUserId(), message);
                    com.taiyiyun.passport.mqtt.v2.MessagePublisher.getInstance().publish(Message.UPLINK_MESSAGE + loginInfo.getUserId(), message);
                }
            }

            //return rst;
            return ThirdTranslate.getInstance().translateThirdReturn(bundle, rst);
        } catch (Exception ex) {
            return getErrorResponse(ex);
        }
    }

    /**
     * 修改密码中验证老密码
     */
    @ResponseBody
    @RequestMapping(value = "/Api/Password", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String verifyOldPwd2(String Appkey, String Mobile, String Password, String Sign, HttpServletRequest request) {

        PackBundle bundle = LangResource.getResourceBundle(request);

        if (StringUtils.isNotEmpty(Mobile)) {
            PhoneUtil pu = PhoneUtil.getInstance();
            String[] phones = Mobile.split("-");
            boolean flag = true;
            if (phones.length == 1) {//默认是中文
                flag = pu.checkPhoneNumberByCountryCode(Mobile, "86");
            } else if (phones.length == 2) {//直接合法性验证
                flag = pu.checkPhoneNumberByCountryCode(phones[1], phones[0]);
            }
            if (!flag) {
                return toJson(false, bundle.getString("mobile.grammar.fault"), null);
            }
        }

        Map<String, String> params = new HashMap<>();

        params.put("Appkey", Appkey);
        params.put("Mobile", Mobile);
        params.put("Password", Password);
        params.put("Sign", Sign);

        try {

            String rst = HttpClientUtil.doGet(Config.get("remote.url") + "/Api/Password", params);
            return ThirdTranslate.getInstance().translateThirdReturn(bundle, rst);
        } catch (Exception ex) {
            return getErrorResponse(ex);
        }
    }

    @ResponseBody
    @RequestMapping(value = "/api/CustomerPosition", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String customerPosition(String Address, String Appkey, String OperatorAddress, String Longitude, String Latitude,
                                   String Sign, HttpServletRequest request) {

        PackBundle bundle = LangResource.getResourceBundle(request);

        Map<String, String> params = new HashMap<>();

        params.put("Appkey", Appkey);
        params.put("Address", Address);
        params.put("OperatorAddress", OperatorAddress);
        params.put("Longitude", Longitude);
        params.put("Latitude", Latitude);
        params.put("Sign", Sign);

        try {
            String rst = HttpClientUtil.doPost(Config.get("remote.url") + "/api/CustomerPosition", params);
            return ThirdTranslate.getInstance().translateThirdReturn(bundle, rst);
        } catch (Exception ex) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("success", false);
            map.put("error", ex.toString());
            map.put("data", null);
            map.put("errorCode", 1);
            return JSON.toJSONString(map);
        }
    }

    @ResponseBody
    @RequestMapping(value = "/Api/SendSMSCode", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String sendSMSCode(String Mobile, String Appkey, String Type, String Sign, HttpServletRequest request) {

        PackBundle bundle = LangResource.getResourceBundle(request);
        if (StringUtils.isNotEmpty(Mobile)) {
            PhoneUtil pu = PhoneUtil.getInstance();
            String[] phones = Mobile.split("-");
            boolean flag = true;
            if (phones.length == 1) {//默认是中文
                flag = pu.checkPhoneNumberByCountryCode(Mobile, "86");
            } else if (phones.length == 2) {//直接合法性验证
                flag = pu.checkPhoneNumberByCountryCode(phones[1], phones[0]);
            }
            if (!flag) {
                return toJson(false, bundle.getString("mobile.grammar.fault"), null);
            }
        }
        Map<String, String> params = new HashMap<>();

        params.put("Mobile", Mobile);
        params.put("Appkey", Appkey);
        params.put("Type", Type);
        params.put("Sign", Sign);

        try {

            String rst = HttpClientUtil.doPost(Config.get("remote.url") + "/Api/SendSMSCode", params);
            return ThirdTranslate.getInstance().translateThirdReturn(bundle, rst);
        } catch (Exception ex) {
            return getErrorResponse(ex);
        }
    }

    @ResponseBody
    @RequestMapping(value = "/api/RealNameAuth2", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String realNameAuth2(String Appkey, String Mobile, String Sign, HttpServletRequest request) {

        PackBundle bundle = LangResource.getResourceBundle(request);
        if (StringUtils.isNotEmpty(Mobile)) {
            PhoneUtil pu = PhoneUtil.getInstance();
            String[] phones = Mobile.split("-");
            boolean flag = true;
            if (phones.length == 1) {//默认是中文
                flag = pu.checkPhoneNumberByCountryCode(Mobile, "86");
            } else if (phones.length == 2) {//直接合法性验证
                flag = pu.checkPhoneNumberByCountryCode(phones[1], phones[0]);
            }
            if (!flag) {
                return toJson(false, bundle.getString("mobile.grammar.fault"), null);
            }
        }

        Map<String, String> params = new HashMap<>();

        params.put("Mobile", Mobile);
        params.put("Appkey", Appkey);
        params.put("Sign", Sign);

        try {

            String rst = HttpClientUtil.doGet(Config.get("remote.url") + "/api/RealNameAuth2", params);
            return ThirdTranslate.getInstance().translateThirdReturn(bundle, rst);
        } catch (Exception ex) {
            return getErrorResponse(ex);
        }
    }

    @ResponseBody
    @RequestMapping(value = "/api/DeveloperDetail", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String developerDetail(String Appkey, String State, String Address, String ThirdpartAppkey, String RandomCode, String Os, String Version, String Sign, HttpServletRequest request) {

        PackBundle bundle = LangResource.getResourceBundle(request);

        Map<String, String> params = new HashMap<>();

        params.put("Appkey", Appkey);
        params.put("Sign", Sign);

        if (StringUtil.isNotEmpty(State)) {
            params.put("State", State);
        }

        if (StringUtil.isNotEmpty(Address)) {
            params.put("Address", Address);
        }

        if (StringUtil.isNotEmpty(ThirdpartAppkey)) {
            params.put("ThirdpartAppkey", ThirdpartAppkey);
        }

        if (StringUtil.isNotEmpty(RandomCode)) {
            params.put("RandomCode", RandomCode);
        }

        if (StringUtil.isNotEmpty(Os)) {
            params.put("Os", Os);
        }

        if (StringUtil.isNotEmpty(Version)) {
            params.put("Version", Version);
        }

        try {

            String rst = HttpClientUtil.doGet(Config.get("remote.url") + "/api/DeveloperDetail", params);
            return ThirdTranslate.getInstance().translateThirdReturn(bundle, rst);
        } catch (Exception ex) {
            return getErrorResponse(ex);
        }
    }

    @RequestMapping(value = "/web/ServiceAgreement", method = RequestMethod.GET, produces = {Const.PRODUCES})
    public String serviceAgreement(@RequestParam(value = "lang-name", required = false) String langName, HttpServletRequest request) {

        PackBundle bundle = LangResource.getResourceBundle(request);
        return "serviceAgreement_" + bundle.getLanguage();
    }

    @ResponseBody
    @RequestMapping(value = "/Api/UserAuthorizationPro", method = RequestMethod.POST, produces = {Const.PRODUCES_JSON})
    public String userAuthorizationPro(String Address, String Appkey, String RandomCode, String Sign, String UserEntityId, HttpServletRequest request) {

        PackBundle bundle = LangResource.getResourceBundle(request);

        try {
            Map<String, String> params = new HashMap<>();

            params.put("Address", Address);
            params.put("Appkey", Appkey);
            params.put("RandomCode", RandomCode);
            params.put("Sign", Sign);
            params.put("UserEntityId", UserEntityId);

            String rst = HttpClientUtil.doPost(Config.get("remote.url") + "/Api/UserAuthorizationPro", params);
            return ThirdTranslate.getInstance().translateThirdReturn(bundle, rst);
        } catch (Exception ex) {
            ex.printStackTrace();
            return getErrorResponse(ex);
        }
    }


    @ResponseBody
    @RequestMapping(value = "/api/UploadInfo", method = RequestMethod.POST, produces = {Const.PRODUCES_JSON})
    public String uploadInfo(String Appkey, String Address, String Name, String IDCard, String Passport, String Sign,
                             @RequestParam("1") MultipartFile file1, @RequestParam("2") MultipartFile file2, @RequestParam("3") MultipartFile file3,
                             @RequestParam("4") MultipartFile file4, HttpServletRequest request) {

        PackBundle bundle = LangResource.getResourceBundle(request);

        try {
            Map<String, Object> params = new HashMap<>();

            params.put("Appkey", Appkey);
            params.put("Address", Address);
            params.put("Name", Name);
            params.put("IDCard", IDCard);
            params.put("Passport", Passport);
            params.put("Sign", Sign);

            params.put("1", file1);
            params.put("2", file2);
            params.put("3", file3);
            params.put("4", file4);

            String rst = HttpClientUtil.doUpload(Config.get("remote.url") + "/api/UploadInfo", params);
            return ThirdTranslate.getInstance().translateThirdReturn(bundle, rst);

        } catch (Exception ex) {
            ex.printStackTrace();
            return getErrorResponse(ex);
        }
    }

    @ResponseBody
    @RequestMapping(value = "/api/uploadInfoForManualReview", method = RequestMethod.POST, produces = {Const.PRODUCES_JSON})
    public String uploadInfoForManualReview(String appKey, String address, String name, String idCard, String passport, String sign,
                                            String country, String countryCode, String validDateStart, String validDateEnd, String sex,
                                            String nation, String birthDay, String homeAddr, @RequestParam("fileCardFront") MultipartFile fileCardFront, @RequestParam("fileCardBack") MultipartFile fileCardBack, @RequestParam("fileHandCard") MultipartFile fileHandCard,
                                            @RequestParam("fileHomeAddr") MultipartFile fileHomeAddr, HttpServletRequest request) {
        String apiName = "/api/uploadInfoForManualReview";
        //验证图片参数是否为空(正面照、反面照、手持、居住证明)
        if(FileUtil.multipartFileIsNull(fileCardFront) || FileUtil.multipartFileIsNull(fileCardBack) || FileUtil.multipartFileIsNull(fileHandCard) || FileUtil.multipartFileIsNull(fileHomeAddr)) {
            throw new RouterException("", "四张图片必须全部上传", null);
        }
        //验证图片大小
        long maxLength = 2 * 1024 * 1024;
        if(fileCardFront.getSize() > maxLength || fileCardBack.getSize() > maxLength || fileHandCard.getSize() > maxLength || fileHomeAddr.getSize() > maxLength) {
            throw new RouterException("", "图片大小不能超过2M", null);
        }
        //检查文件类型
        String fileCardFrontName = fileCardFront.getOriginalFilename();
        String fileCardBackName = fileCardBack.getOriginalFilename();
        String fileHandCardName = fileHandCard.getOriginalFilename();
        String fileHomeAddrName = fileHomeAddr.getOriginalFilename();
        if(FileUtil.checkPictures(fileCardFrontName, fileCardBackName, fileHandCardName, fileHomeAddrName)) {
            throw new RouterException("", "文件类型必须为jpg、jpeg、png格式", null);
        }

        //验证参数是否为空
        if(StringUtils.isEmpty(name)) {
            throw new RouterException("", "姓名不能为空", null);
        }
        if(StringUtils.isEmpty(idCard) && StringUtils.isEmpty(passport)) {
            throw new RouterException("", "身份证号和护照号不能同时为空", null);
        }
        if(StringUtils.isNotEmpty(idCard) && StringUtils.isNotEmpty(passport)) {
            throw new RouterException("", "身份证号和护照号只能传一个", null);
        }
        if(StringUtils.isEmpty(country)) {
            throw new RouterException("", "国家名称不能为空", null);
        }
        if(StringUtils.isEmpty(countryCode)) {
            throw new RouterException("", "国家码不能为空", null);
        }
        if(StringUtils.isEmpty(validDateStart)) {
            throw new RouterException("", "证件有效日期始不能为空", null);
        }
        if(StringUtils.isEmpty(validDateEnd)) {
            throw new RouterException("", "证件有效日期止不能为空", null);
        }
        if(StringUtils.isEmpty(birthDay)) {
            throw new RouterException("", "出生日期不能为空", null);
        }
        if(StringUtils.isEmpty(homeAddr)) {
            throw new RouterException("", "家庭住址不能为空", null);
        }
        rnAuthService.uploadInfoForManualReview(appKey, address, name, idCard, passport, sign, country, countryCode, validDateStart, validDateEnd, sex, nation, birthDay, homeAddr, fileCardFront, fileCardBack, fileHandCard, fileHomeAddr);
        return toJson(0, "", apiName, "");
    }

    @ResponseBody
    @RequestMapping(value = "/Api/UserAuthorization", method = RequestMethod.POST, produces = {Const.PRODUCES_JSON})
    public String userAuthorization(String RandomCode, String Appkey, String Address, String UserEntityId, String Sign, HttpServletRequest request) {

        PackBundle bundle = LangResource.getResourceBundle(request);

        try {
            Map<String, String> params = new HashMap<>();

            params.put("RandomCode", RandomCode);
            params.put("Appkey", Appkey);
            params.put("Address", Address);
            params.put("UserEntityId", UserEntityId);
            params.put("Sign", Sign);

            String rst = HttpClientUtil.doPost(Config.get("remote.url") + "/Api/UserAuthorization", params);
            return ThirdTranslate.getInstance().translateThirdReturn(bundle, rst);
        } catch (Exception ex) {
            ex.printStackTrace();
            return getErrorResponse(ex);
        }
    }

    @ResponseBody
    @RequestMapping(value = "/Api/Picture", method = RequestMethod.GET, produces = {Const.PRODUCES_JSON})
    public String userAuthorization(String Appkey, String EntityId, String Sign, HttpServletRequest request) {

        PackBundle bundle = LangResource.getResourceBundle(request);

        try {
            Map<String, String> params = new HashMap<>();

            params.put("Appkey", Appkey);
            params.put("EntityId", EntityId);
            params.put("Sign", Sign);

            String rst = HttpClientUtil.doGet(Config.get("remote.url") + "/Api/Picture", params);
            return ThirdTranslate.getInstance().translateThirdReturn(bundle, rst);
        } catch (Exception ex) {
            ex.printStackTrace();
            return getErrorResponse(ex);
        }
    }

    @ResponseBody
    @RequestMapping(value = "/api/UserToken", method = RequestMethod.GET, produces = {Const.PRODUCES_JSON})
    public String userToken(String Appkey, String Code, String Sign, HttpServletRequest request) {

        PackBundle bundle = LangResource.getResourceBundle(request);

        try {
            Map<String, String> params = new HashMap<>();

            params.put("Appkey", Appkey);
            params.put("Code", Code);
            params.put("Sign", Sign);

            String rst = HttpClientUtil.doGet(Config.get("remote.url") + "/api/UserToken", params);

            return ThirdTranslate.getInstance().translateThirdReturn(bundle, rst);
        } catch (Exception ex) {
            ex.printStackTrace();
            return getErrorResponse(ex);
        }
    }

    @ResponseBody
    @RequestMapping(value = "/api/CustomerUserBinding", method = {RequestMethod.GET, RequestMethod.POST}, produces = {Const.PRODUCES_JSON})
    public String customerUserBinding1(String Appkey, String Token, String Sign, String UserName, HttpServletRequest request) {

        PackBundle bundle = LangResource.getResourceBundle(request);

        String method = request.getMethod().toUpperCase();
        if (method.equals("GET")) {
            try {
                Map<String, String> params = new HashMap<>();

                params.put("Appkey", Appkey);
                params.put("Token", Token);
                params.put("Sign", Sign);

                String rst = HttpClientUtil.doGet(Config.get("remote.url") + "/api/CustomerUserBinding", params);

                return ThirdTranslate.getInstance().translateThirdReturn(bundle, rst);
            } catch (Exception ex) {
                ex.printStackTrace();
                return getErrorResponse(ex);
            }
        } else {

            try {
                Map<String, String> params = new HashMap<>();

                params.put("Appkey", Appkey);
                params.put("Token", Token);
                params.put("UserName", UserName);
                params.put("Sign", Sign);

                String rst = HttpClientUtil.doPost(Config.get("remote.url") + "/api/CustomerUserBinding", params);

                return ThirdTranslate.getInstance().translateThirdReturn(bundle, rst);
            } catch (Exception ex) {
                ex.printStackTrace();
                return getErrorResponse(ex);
            }
        }
    }

//	@ResponseBody
//	@RequestMapping(value = "/api/CustomerUserBinding", method = RequestMethod.POST, produces = {Const.PRODUCES_JSON})
//	public String customerUserBinding2(String Appkey, String Token,String Sign) {
//		try{
//			Map<String,String> params = new HashMap<>();
//
//			params.put("Appkey", Appkey);
//			params.put("Code", Token);
//			params.put("Sign", Sign);
//
//			return HttpClientUtil.doPost(Config.get("remote.url") + "/api/CustomerUserBinding", params);
//		}catch (Exception ex){
//			ex.printStackTrace();
//			return getErrorResponse(ex);
//		}
//	}

    @ResponseBody
    @RequestMapping(value = "/web/File/DownloadPictureFile", produces = {Const.PRODUCES})
    public String downloadPictureFile(String Appkey, String PID, String Timestamp, String Sign, HttpServletRequest request) {

        PackBundle bundle = LangResource.getResourceBundle(request);

        try {
            Map<String, String> params = new HashMap<>();

            params.put("Appkey", Appkey);
            params.put("PID", PID);
            params.put("Timestamp", Timestamp);
            params.put("Sign", Sign);

            String rst = HttpClientUtil.doGet(Config.get("remote.url") + "/web/File/DownloadPictureFile", params);

            return ThirdTranslate.getInstance().translateThirdReturn(bundle, rst);
        } catch (Exception ex) {
            ex.printStackTrace();
            return getErrorResponse(ex);
        }
    }
    
    /**
     * MD5加密(信息加密算法)
     * @param message 要进行MD5加密的字符串
     * @return 加密结果为32位字符串
     */
    public static String getMD5(String message, boolean isUpperCase) {

        MessageDigest messageDigest;
        StringBuilder md5StrBuff = new StringBuilder();

        try {

            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(message.getBytes("UTF-8"));

            byte[] byteArray = messageDigest.digest();

            for (int i = 0; i < byteArray.length; i++) {
                if (Integer.toHexString(0xFF & byteArray[i]).length() == 1){
                    md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
                } else{
                    md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
                }
            }

        } catch (Exception e) {
            throw new RuntimeException();
        }

        if (isUpperCase){
            return md5StrBuff.toString().toUpperCase(); // 字母大写
        } else {
            return md5StrBuff.toString();
        }

    }
    /**
     * 签名算法
     * @param map_get 上传参数Map
     */
    public static String mSignatureAlgorithm(Map<String,String> map_get){

        // 拼接字符串
        String str_Stitch = "";
        for (Map.Entry<String, String> entry : map_get.entrySet()) {
            str_Stitch = str_Stitch + entry.getKey() + entry.getValue() ;
        }
      
       final String sc ="CA3A46524E0D4867A535E967A7F6BB2F".trim();
      
        String str_StitchAll =  sc + str_Stitch.trim() + sc;
       /// Log.e("str_StitchAll",str_StitchAll.trim());
        System.out.println("......str_StitchAll."+str_StitchAll);
        System.out.println("......."+MD5Util.MD5Encode(str_StitchAll, true));
       return  getMD5(str_StitchAll,true);
    }
    
    

}
