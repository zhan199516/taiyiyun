package com.taiyiyun.passport.sms;

import com.taiyiyun.passport.consts.Config;
import com.taiyiyun.passport.consts.Const;
import com.yunpian.sdk.YunpianClient;
import com.yunpian.sdk.model.Result;
import com.yunpian.sdk.model.SmsSingleSend;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.MessageFormat;
import java.util.Map;

/**
 *
 * Created by nina on 2017/9/21.
 */
public class SmsYPClient {
    private static final Log log = LogFactory.getLog(SmsYPClient.class);
    private static final String APIKEY = Config.get(Const.SMS_YP_APIKEY);

    private SmsYPClient(){}

    public static String singleSendSMS(String mobile, ModelType modelType, Object... arguments) {
        return send(getShortMessage(mobile, modelType, arguments));
    }

    private static ShortMessage getShortMessage(String mobile, ModelType modelType, Object... arguments){
        ShortMessage shortMessage = new ShortMessage();
        String text = getString(modelType.getText(), arguments);
        shortMessage.setText(text);
        shortMessage.setType(0);
        String[] mobiles = mobile.split("-");
        if(mobiles.length == 1) {//默认中国号码
            shortMessage.setMobile(mobile);
        } else if(mobiles.length == 2) {
            if(StringUtils.equalsIgnoreCase(mobiles[0], "86")) {//中国号码
                shortMessage.setMobile(mobiles[1]);
            } else {//国外号码
                    shortMessage.setType(1);
                    shortMessage.setMobile("+" + mobiles[0] + mobiles[1]);
            }
        }
        return shortMessage;
    }

    /**
     * 验证是否是国内手机号：0-是，1-否
     * @param mobile
     * @return
     */
    public static int isChina(String mobile) {
        int result = 0;
        String[] mobiles = mobile.split("-");
        if(mobiles.length == 1) {//默认中国号码
        } else if(mobiles.length == 2) {
            if(StringUtils.equalsIgnoreCase(mobiles[0], "86")) {//中国号码
            } else {//国外号码
                result = 1;
            }
        }
        return result;
    }

    public static String send(ShortMessage shortMessage) {
        YunpianClient client = new YunpianClient(APIKEY).init();
        Result<SmsSingleSend> r;
        try {
            Map<String, String> param = client.newParam(2);
            param.put(YunpianClient.MOBILE, shortMessage.getMobile());
            param.put(YunpianClient.TEXT, shortMessage.getText());
            r = client.sms().single_send(param);
            System.out.println(r.getCode());
            System.out.println(r.getMsg());

        } finally {
            client.close();
        }
        return r.getMsg();
    }

    private static String getString(String pattern, Object ... arguments){
        String result = "";
        try {
            MessageFormat temp = new MessageFormat(pattern);
            result = temp.format(arguments);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return result;
    }

    public static void main(String[] args) {
        //singleSendSMS("18612239838", "1233131");
        //singleSendSMS("1-8252512040", "111111");
        //singleSendSMS("44-1787540043", ModelType.SMS_YP_SIGNNAME_CHECKCODE_CN, "111111");
        //singleSendSMS("18612239838", ModelType.SMS_YP_SIGNNAME_CHECKCODE_CN, "111111");
        //singleSendSMS("18612239838", ModelType.SMS_YP_SIGNNAME_NOTICE_CN, "111111");
        //singleSendSMS("18612239838", ModelType.SMS_YP_SIGNNAME_OTHERCLIENT_CN, "111111", "222222");
        //singleSendSMS("1-4087139297", ModelType.SMS_YP_SIGNNAME_OTHERCLIENT_EN, "111111", "222222");
        //singleSendSMS("1-4087139297", ModelType.SMS_YP_SIGNNAME_SENDCOIN_EN, "111111", "222222", "33333");
        //singleSendSMS("1-4087139297", ModelType.SMS_YP_SIGNNAME_NOTICE_EN, "111111");
    }





}
