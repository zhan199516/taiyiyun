package com.taiyiyun.passport.bean;

import com.taiyiyun.passport.util.StringUtil;

/**
 * Created by okdos on 2017/9/26.
 * 国际号码的处理
 */
public class GlobleMobile {
    public GlobleMobile(String mobile, String mobilePrefix){
        readMobile(mobile);
        if(StringUtil.isEmptyOrBlank(mobilePrefix)){
            this.mobilePrefix = mobilePrefix;
        }
    }

    public GlobleMobile(String mobile){
        readMobile(mobile);
    }


    private void readMobile(String mobile){
        this.mobilePrefix = "86";

        if(StringUtil.isEmptyOrBlank(mobile)){
            this.mobile = "";
            return;
        }

        int index = mobile.indexOf("-");

        if(index >= 0)
        {
            this.mobilePrefix = mobile.substring(0, index);
            this.mobile = mobile.substring(index + 1);
        } else
        {
            this.mobile = mobile;
        }
    }

    private String mobile;
    private String mobilePrefix;

    /**
     * 单独的手机号
     * @return
     */
    public String getMobile() {
        return mobile;
    }

    /**
     * 单独的国际码
     * @return
     */
    public String getMobilePrefix() {
        return mobilePrefix;
    }

    /**
     * 国际码+手机号
     * @return
     */
    public String getGloble() {
        if(mobilePrefix == "86"){
            return mobile;
        } else {
            return mobilePrefix + "-" + mobile;
        }
    }
}
