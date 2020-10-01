package com.taiyiyun.passport.language;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;

public class LangResource {

    public static PackBundle getResourceBundle(HttpServletRequest request){
        return (PackBundle)request.getAttribute(LANG_NAME);
    }

    public static String LANG_NAME = "lang-name";

    private static LangResource ourInstance = new LangResource();

    public static LangResource getInstance() {
        return ourInstance;
    }

    private HashMap<String, PackBundle> langs = new HashMap<>();

    private LangResource() {

        ResourceBundle rb = ResourceBundle.getBundle("property.Message", Locale.SIMPLIFIED_CHINESE);
        langs.put("zh", new PackBundle(rb, "zh"));
        rb = ResourceBundle.getBundle("property.Message", Locale.US);
        langs.put("en", new PackBundle(rb, "en"));
        Locale locale = new Locale("ru", "");
        rb = ResourceBundle.getBundle("property.Message", locale);
        langs.put("ru", new PackBundle(rb, "ru"));
        rb = ResourceBundle.getBundle("property.Message", Locale.TRADITIONAL_CHINESE);
        langs.put("tw", new PackBundle(rb, "tw"));
        rb = ResourceBundle.getBundle("property.Message", Locale.JAPAN);
        langs.put("ja", new PackBundle(rb, "ja"));
        locale = new Locale("es", "");
        rb = ResourceBundle.getBundle("property.Message", locale);
        langs.put("es", new PackBundle(rb, "es"));
    }

    /**
     * 获取一个语言资源，默认使用中文
     * @param lang
     * @return
     */
    public PackBundle getResourceBundle(String lang){
        PackBundle rb = langs.get(lang);

        //default
        if(rb == null) {
            rb = langs.get("zh");
        }
        return rb;
    }


}
