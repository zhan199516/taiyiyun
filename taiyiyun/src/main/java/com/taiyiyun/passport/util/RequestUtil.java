package com.taiyiyun.passport.util;

import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.StringTokenizer;

public class RequestUtil {
    public static String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("X-real-ip");//先从nginx自定义配置获取
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("x-forwarded-for");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    public static HashMap<String, String> parseQueryString(String queryString) {
        HashMap<String, String> map = new HashMap<>();

        if(queryString == null){
            return map;
        }

        String[] strs = queryString.split("&");

        for(String pairs: strs){
            try{
                int i = pairs.indexOf('=');
                if (i >= 0){
                    String key = pairs.substring(0, pairs.indexOf('='));
                    String value = pairs.substring(pairs.indexOf('=') + 1);
                    value = URLDecoder.decode(value, "utf-8");
                    map.put(key, value);
                }
            } catch(Exception ex){
                ex.printStackTrace();
            }

        }

        return map;
    }

}
