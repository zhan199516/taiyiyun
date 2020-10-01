package com.taiyiyun.passport.sms;

/**
 * Created by nina on 2017/9/26.
 */
public enum ModelType {

    /**
     * 中文验证码
     */
    SMS_YP_SIGNNAME_CHECKCODE_CN(1, "【共享护照】您的验证码是{0}"),

    /**
     * 英文验证码
     */
    SMS_YP_SIGNNAME_CHECKCODE_EN(2, "【WePass】Your Verification Code is{0}"),


    /**
     * 中文其它客户端登陆通知
     */
    SMS_YP_SIGNNAME_OTHERCLIENT_CN(3, "【共享护照】您的账户在{0}使用{1}刚刚登录。如果非您本人操作，请尽快更改您的密码。"),


    /**
     * 英文其它客户端登陆通知
     */
    SMS_YP_SIGNNAME_OTHERCLIENT_EN(4, "【WePass】Your account logged on {0}.Please change your password if you not use the {1}device."),

    /**
     * 中文其它客户端登陆通知
     */
    SMS_YP_SIGNNAME_SENDCOIN_CN(5, "【共享护照】{0} 给您发送了 {1}{2}，请用您的手机号注册“共享护照”收取，http://wechain.im"),


    /**
     * 英文其它客户端登陆通知
     */
    //SMS_YP_SIGNNAME_SENDCOIN_EN(6, "【WePass】{0} sent you an {1} {2}, please register your \"WePass\" with your mobile number, http://wechain.im."),
    SMS_YP_SIGNNAME_SENDCOIN_EN(6, "【WePass】{0}sent you an {1}{2}, please register \"WePass\" with your mobile number, http://wechain.im."),


    /**
     * 中文通知接收短信
     */
    SMS_YP_SIGNNAME_NOTICE_CN(7, "【共享护照】{0} 给您发送了一条消息，请用您的手机号注册登录共享护照收取。下载地址 http://wechain.im 。"),


    /**
     * 英文通知接收短信
     */
    SMS_YP_SIGNNAME_NOTICE_EN(8, "【WePass】{0} sent you a message, please use your mobile phone number to sign up for WePass. Download the address http://wechain.im.");




    private int code;
    private String text;

    ModelType(int code, String text) {
        this.code = code;
        this.text = text;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
