package com.taiyiyun.passport.service.weichat;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.taiyiyun.passport.consts.Config;
import com.taiyiyun.passport.dao.IWechatDao;
import com.taiyiyun.passport.exception.DefinedError;
import com.taiyiyun.passport.init.SpringContext;
import com.taiyiyun.passport.util.HttpClientUtil;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WechatToken {
    private static WechatToken ourInstance = new WechatToken();

    public static WechatToken getInstance() {
        return ourInstance;
    }

    private WechatToken() {
        executorService = Executors.newSingleThreadScheduledExecutor();
        wechatDao = SpringContext.getBean(IWechatDao.class);
    }

    private ScheduledExecutorService executorService; // 定时任务


    private IWechatDao wechatDao;

    private String we_token_url = Config.get("wechat.token_url");
    private String we_ticket_url = Config.get("wechat.ticket_url");
    private String we_appid = Config.get("wechat.appid");
    private String we_secret = Config.get("wechat.secret");

    private String _refreshAccessToken;
    private String _refreshTiket;


    public void start(){
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Refresh(15 * 60);
            }
        }, 1, 60, TimeUnit.SECONDS);
    }

    private void Refresh(int seconds){

        Date now = new Date();

        long nowLong = now.getTime() / 1000;
        long oldLong = nowLong - seconds;
        long thenLong = nowLong - 100;

        try{
            int count = wechatDao.updateBegin(we_appid, nowLong, oldLong, thenLong);
            if(count == 0){
                return;
            }

            getAccessToken();
            getTicket();

            wechatDao.updateEnd(we_appid, nowLong, nowLong, _refreshAccessToken, _refreshTiket);
        } catch (Exception ex){
            ex.printStackTrace();
            wechatDao.updateRoll(we_appid);
        }

    }

    private void getAccessToken() throws DefinedError{

        Map<String, String> param = new HashMap<>();
        param.put("grant_type", "client_credential");
        param.put("appid", we_appid);
        param.put("secret", we_secret);

        String str = HttpClientUtil.doGet(we_token_url, param);

        JSONObject jsonObject = JSON.parseObject(str);

        this._refreshAccessToken = jsonObject.getString("access_token");
    }

    private void getTicket() throws DefinedError{

        Map<String, String> param = new HashMap<>();
        param.put("access_token", this._refreshAccessToken);
        param.put("type", "jsapi");

        String str = HttpClientUtil.doGet(we_ticket_url, param);

        JSONObject jsonObject = JSON.parseObject(str);

        this._refreshTiket = jsonObject.getString("ticket");
    }

}
