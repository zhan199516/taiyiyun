package com.taiyiyun.passport.language;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public class PackBundle {
    private static Log log = LogFactory.getLog(PackBundle.class);
    public PackBundle(ResourceBundle bundle, String key){
        this.bundle = bundle;
        this.key = key;
    }

    private String key;
    private ResourceBundle bundle;


    public String getString(String key){
        String result = "";
        try {
            result = bundle.getString(key);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return result;
    }

    public String getString(String key, Object ... arguments){
        String result = "";
        try {
            String pattern = bundle.getString(key);
            MessageFormat temp = new MessageFormat(pattern);
            result = temp.format(arguments);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return result;
    }

    public String getLanguage(){
        return key;
    }
}
