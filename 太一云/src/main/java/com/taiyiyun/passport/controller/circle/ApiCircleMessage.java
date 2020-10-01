package com.taiyiyun.passport.controller.circle;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.taiyiyun.passport.language.LangResource;
import com.taiyiyun.passport.language.PackBundle;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import com.alibaba.fastjson.JSON;
import com.taiyiyun.passport.po.circle.Article;
import com.taiyiyun.passport.po.circle.BodyInfo;
import com.taiyiyun.passport.service.ICircleMsgService;

/**
 * Created by okdos on 2017/6/27.
 */


@Controller
@RequestMapping("/api/circle")
public class ApiCircleMessage {

    @Resource
    ICircleMsgService service;


    @ResponseBody
    @RequestMapping(value = "/CircleMsg",method = {RequestMethod.GET}, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String GetCircleMsg(String message, Long start, String circle, HttpServletRequest request){

        HashMap<String, Object> map = new HashMap<>();

        if(start == null){
            start = 0L;
        }

        if(message != null){
            map.put("articleId", message);
        }
        else {
            Timestamp time = new Timestamp(start);

            map.put("start", time);
            map.put("userId", circle);
        }

        List<Article> list = service.getCircleMsgByMap(map, request, false);
        return JSON.toJSONString(list);
    }


    @ResponseBody
    @RequestMapping(value = "/CircleMsg2",method = {RequestMethod.GET}, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String GetCircleMsg2(Long start, Long end, String circle_id, HttpServletRequest request){

        try{
            Timestamp dtStart = null;
            if(start != null && start != 0){
                dtStart = new Timestamp(start);
            }
            
            Timestamp dtEnd = null;
            if(end != null && end != 0){
            	dtEnd = new Timestamp(end);
            }

            HashMap<String, Object> map = new HashMap<>();


            map.put("start", dtStart);
            map.put("end", dtEnd);
            map.put("userId", circle_id);
            map.put("onlineStatus", 2);
            map.put("limit", 21);


            List<Article> list = service.getCircleMsgByMap(map, request, false);

            BodyInfo<List<Article>> bodyInfo = new BodyInfo<>();
            bodyInfo.setStatus("0");
            if(list.size() > 20){
                bodyInfo.setHasMore(true);
                list = list.subList(0, 20);
            } else {
                bodyInfo.setHasMore(false);
            }
            bodyInfo.setMessages(list);

            return JSON.toJSONString(bodyInfo);
        } catch(Exception ex){
            BodyInfo<Object> error = new BodyInfo<>();
            error.setStatus("1");
            error.setErrorMsg(ex.getMessage());
            return JSON.toJSONString(error);
        }
    }

    @ResponseBody
    @RequestMapping(value = "/ArticleCer",method = {RequestMethod.GET}, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String GetCircleMsg(Long start, HttpServletRequest request){

        Timestamp dtStart = null;
        if(start != null && start != 0){
            dtStart = new Timestamp(start);
        }

        HashMap<String, Object> map = new HashMap<>();

        map.put("start", dtStart);
        map.put("onlineStatus", 2);
        map.put("isOriginal", 1);
        
        List<Article> list = service.getArticleCertificateByMap(map, request, true);
        BodyInfo<List<Article>> bodyInfo = new BodyInfo<>();
        bodyInfo.setStatus("0");
        if(list.size() > 20){
            bodyInfo.setHasMore(true);
            list = list.subList(0, 20);
        } else {
            bodyInfo.setHasMore(false);
        }
        bodyInfo.setMessages(list);

        return JSON.toJSONString(bodyInfo);
    }


    @ResponseBody
    @RequestMapping(value = "/CircleStatus",method = {RequestMethod.GET}, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String GetCircleStatus(){
        HashMap<String, Object> map = new HashMap<>();

        //todo 先在此处写死，之后新版本根据是情况写入到default.properties中 (CircleStatus 的get和post接口可能根本不使用了)


        map.put("username", "LTAI9pYHa6FtGIMC");
        map.put("password", "zU7N3sTexYVT6Q2OwPYaUsRwokw");
        map.put("host", "mqf-9i85egvp24.mqtt.aliyuncs.com");
        map.put("port", 1883);
        map.put("root_topic", "tyy_passport");
        map.put("group_id", "GID_tyy_passport");
        map.put("public_key", "-----BEGIN PUBLIC KEY-----\\r\\nMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC0H78S479P1MJ3Lap479jJrddk\\r\\nTbc1eaT2NXl11S5EpUV6pCue7i7HZ0NpBj6k24PLw/ThONMy8xwuIZQpxmSvJfm6\\r\\n+4WA5deTEHA6xDDR6RDbHT4QIzytcMiQxhmH5qTPSOsdd8r3toHCUxDINaBdt5Yu\\r\\nYT2/d+PWVLO9wBCS6QIDAQAB\\r\\n-----END PUBLIC KEY-----\\r\\n");
        map.put("tls", 0);
        map.put("keepalive", 60);
        map.put("clean", 1);
        map.put("auth", 1);

        return JSON.toJSONString(map);
    }
    
    @ResponseBody
    @RequestMapping(value = "/BlockchainStat", method = {RequestMethod.GET}, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String getBlockchainStat(HttpServletRequest request) {

        PackBundle bundle = LangResource.getResourceBundle(request);
        String apiName = "GET /BlockchainStat";

    	Map<String, Object> statMap = new HashMap<>();
    	try {
    		
    		statMap = service.getBlockchainStat();
    		statMap.put("status", 0);
    		
		} catch (Exception e) {
			e.printStackTrace();
			statMap.put("status", 1);
			statMap.put("errorCode", 1);
			statMap.put("error", bundle.getString("failed.execute"));
		}
    	
    	return JSON.toJSONString(statMap);
    }


}
