package com.taiyiyun.passport.service.impl;

import com.taiyiyun.passport.consts.Config;
import com.taiyiyun.passport.dao.IWechatDao;
import com.taiyiyun.passport.po.WeiConfigRes;
import com.taiyiyun.passport.service.IWechatService;
import com.taiyiyun.passport.util.StringUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

@Service
public class WechatServiceImp implements IWechatService {


    String we_appid = Config.get("wechat.appid");

    @Resource
    IWechatDao wechatDao;

    @Override
    public WeiConfigRes singature(String url) {
        String ticket = wechatDao.selectById(we_appid);

        WeiConfigRes weiConfigRes = new WeiConfigRes();
        weiConfigRes.setAppId(we_appid);
        weiConfigRes.setNonceStr(StringUtil.getRandomString(10, true, true, true));
        weiConfigRes.setTimestamp((new Date().getTime()) / 1000);
        weiConfigRes.setReqestUrl(url);

        String input = "jsapi_ticket="+ticket+"&noncestr="+weiConfigRes.getNonceStr()
                +"&timestamp="+ weiConfigRes.getTimestamp()+"&url=" + weiConfigRes.getReqestUrl();


        String sign = DigestUtils.sha1Hex(input);

        weiConfigRes.setSignature(sign);

        return weiConfigRes;
    }
}
