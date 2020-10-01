package com.taiyiyun.passport.controller;

import com.alibaba.fastjson.JSON;
import com.google.i18n.phonenumbers.NumberParseException;
import com.taiyiyun.passport.language.LangResource;
import com.taiyiyun.passport.language.PackBundle;
import com.taiyiyun.passport.util.PhoneUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by nina on 2017/9/18.
 */
@Controller
public class ApiToolController extends BaseController{

    private static Log log = LogFactory.getLog(ApiToolController.class);

    @ResponseBody
    @RequestMapping(value="/api/tool/checkPhoneNumberIsValid", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String checkPhoneNumberIsValid(String phoneNumber, String countryCode, String regionCode, HttpServletRequest request) {
        //绑定的语言
        PackBundle bundle = LangResource.getResourceBundle(request);
        Map<String, Object> json = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        if(StringUtils.isEmpty(phoneNumber)) {
            json.put("sataus", 1);
            json.put("error", bundle.getString("tool.phonecheck.phonenotnull"));
            json.put("data", data);
            return JSON.toJSONString(json);
        }
        PhoneUtil pu = PhoneUtil.getInstance();
        try {
            boolean flag = false;
            if(StringUtils.isNotEmpty(countryCode)) {
                flag = pu.checkPhoneNumberByCountryCode(phoneNumber, countryCode);
            } else {
                flag = pu.checkPhoneNumberByRegionCode(phoneNumber, regionCode);
            }
            if(StringUtils.isEmpty(countryCode) && StringUtils.isEmpty(regionCode)) {
                flag = pu.checkPhoneNumberByDefaultRegionCode(phoneNumber);
            }
            int isValid = 0;//0-合法
            if(!flag) {
                isValid = 1;//1-不合法
            }
            json.put("status", 0);
            data.put("isValid", isValid);
            json.put("data", data);
        } catch (NumberParseException e) {
            NumberParseException.ErrorType errorType = e.getErrorType();
            String errMsg = bundle.getString("tool.phonecheck.paramisnotvalid");
            if(errorType == NumberParseException.ErrorType.NOT_A_NUMBER) {
                errMsg = bundle.getString("tool.phonecheck.phoneisnotphone");
            } else if(errorType == NumberParseException.ErrorType.TOO_LONG) {
                errMsg = bundle.getString("tool.phonecheck.phoneistoolong");
            } else if(errorType == NumberParseException.ErrorType.INVALID_COUNTRY_CODE) {
                errMsg = bundle.getString("tool.phonecheck.missingorinvalidregion");
            } else if(errorType == NumberParseException.ErrorType.TOO_SHORT_NSN) {
                errMsg = bundle.getString("tool.phonecheck.phoneistooshort");
            }
            json.put("status", 1);
            json.put("error", errMsg);
            log.error("国际化手机校验出现异常：" + errMsg);
            return JSON.toJSONString(json);
        }
        return JSON.toJSONString(json);
    }

    @ResponseBody
    @RequestMapping(value="/api/tool/fetchCountryCodeNameRegionCodeList", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String fetchCountryCodeNameRegionCodeList(String countryAlias, HttpServletRequest request) {
        //绑定的语言
        PackBundle bundle = LangResource.getResourceBundle(request);
        List<PhoneUtil.National> nationals = null;
        PhoneUtil pn = PhoneUtil.getInstance();
        Map<String, Object> json = new HashMap<>();
        if(StringUtils.isEmpty(countryAlias)) {
            json.put("status", 1);
            json.put("error", bundle.getString("tool.fetchcountryinfo.countrynamenotnull"));
            return JSON.toJSONString(json);
        }
        try {
            nationals = pn.fetchNational(countryAlias);
            json.put("status", 0);
            json.put("data", nationals);
        } catch (IOException e) {
            e.printStackTrace();
            json.put("status", 1);
            json.put("error", bundle.getString("tool.fetchcountryinfo.getcountrycodelistfaild"));
            log.error("获取国家码集合失败：" + e.getMessage());
        }
        return JSON.toJSONString(json);
    }

}
