package com.taiyiyun.passport.util;

import com.taiyiyun.passport.init.SpringContext;
import com.taiyiyun.passport.sqlserver.dao.IDeveloperDao;
import com.taiyiyun.passport.sqlserver.po.Developer;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by nina on 2017/11/8.
 */
public final class SignChecker {

    public static final boolean checkSign(String sign, Map<String, String> params, String appKey) {
        IDeveloperDao developerDao = SpringContext.getBean(IDeveloperDao.class);
        Developer developer = developerDao.selectDeveloperByAppKey(appKey);
        String signByParams = MD5Signature.signMd5(params, developer.getAppSecret());
        if(StringUtils.equals(sign, signByParams)) {
            return true;
        } else {
            return false;
        }
    }

    public static void main(String[] args) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("mobile", "18580955696");
        params.put("mobilePrefix", "86");
        params.put("appKey", "1A051FEAA0A0451E8D2112AF2A24716C");
        boolean b = checkSign("4810927716E3A02C2F39F4A6036A5B4C", params, "1A051FEAA0A0451E8D2112AF2A24716C");
        System.out.println(b);
    }
}
