package com.taiyiyun.passport.language;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by okdos on 2017/9/6.
 */
public class ThirdTranslate {
    private static ThirdTranslate ourInstance = new ThirdTranslate();

    public static ThirdTranslate getInstance() {
        return ourInstance;
    }

    private ThirdTranslate() {
        initWord();
    }


    private List<ThirdWord> tranList = new ArrayList<>();

    private void addWord(String text, String key, ThirdWord.MatchType type){
        ThirdWord word = new ThirdWord();
        word = new ThirdWord();
        word.setExpression(text);
        word.setLangKey(key);
        word.setMatchType(type);
        tranList.add(word);
    }

    private void addWord(String text, String key){
        addWord(text, key, ThirdWord.MatchType.CONTAIN);
    }


    private void initWord(){
        addWord("发生服务器内部错误", "failed.execute");
        addWord("账户已冻结", "user.frozen");
        addWord("手机号码不正确", "mobile.grammar.fault");
        addWord("验证码错误或过期", "need.sms.error");
        addWord("手机号已经存在", "need.mobile.register");
        addWord("手机号已注册", "need.mobile.register");
        addWord("该手机号未注册", "mobile.register.first");
        addWord("手机号码未注册", "need.mobile.unregister");
        addWord("手机号码格式不正确", "mobile.grammar.fault");
        addWord("用户手机号错误", "mobile.grammar.fault");
        addWord("请输入正确的手机号", "mobile.grammar.fault");
        addWord("手机号或密码错误", "need.user.password.error");
        addWord("当前注册人数较多，请稍后重试", "register.too.many");
        addWord("密码不符合规则", "password.grammar.fault");
        addWord("请输入正确的原登录密码", "need.last.password");
        addWord("用户地址错误", "user.address.fault");
        addWord("已经完成身份认证，无法修改图片", "picture.modify.deny.register");
        addWord("头像数据存储失败", "failed.save");
        addWord("头像文件存储失败", "failed.save");
        addWord("存储用户信息失败，请联系管理员", "failed.save");
        addWord("记录存储失败，请联系管理员", "failed.save");
        addWord("图片文件太大", "picture.too.big");

        addWord("签名验证未通过", "failed.sign");
        addWord("签名不正确", "failed.sign");
        addWord("文件大小超过2M限制", "file.too.big.2m");
        addWord("文件类型不正确", "param.invalid");
        addWord("文件名称不正确", "param.invalid");
        addWord("图片类型不正确", "param.invalid");
        addWord("上传照片成功", "successful.upload");
        addWord("上传照片失败", "failed.operation");
        addWord("没有个人信息", "user.not.find");
        addWord("解除绑定成功", "successful.binding");
        addWord("解除绑定失败", "failed.binding");
        addWord("获得第三方信息失败", "failed.third.error");
        addWord("存储随机码时失败", "failed.operation");
        addWord("随机码状态更新失败", "failed.operation");
        addWord("该合作已停止。如有疑问请联系太一商务。", "third.cooperate.stop");
        addWord("身份证和护照只能选一个", "only.idcard.passport");
        addWord("不允许使用人工审核", "manual.check.deny");
        addWord("用户已有个人身份", "user.has.checked");
        addWord("文件不存在", "file.not.file");
        addWord("身份证号码已存在", "id.number.exists");
        addWord("非同一人", "not.same.person");
        addWord("同一个手机号同一验证码模板每30秒只能发送一条", "send.sms.limit30");

        addWord("去认证", "authenticate.goto");
        addWord("认证中", "authenticate.progressing");
        addWord("认证通过", "authenticate.success");
        addWord("认证失败", "authenticate.failed");

        addWord("身份证信息无效", "third.invalidIdInformation");
        addWord("非同一人(人像加密)", "third.notTheSamePerson");
        addWord("登陆信息已过期", "third.loginInfoExpired");
        addWord("认证反馈", "third.authenFeedback");
        addWord("姓名不合规", "third.nameIrregularities");
        addWord("你还未进行实名认证，请先进行实名认证", "third.notRealNameAuthen");

        addWord("(Address)不存在", "need.param", ThirdWord.MatchType.REGULAR);
        addWord("(Appkey) 验证失败", "need.param.error", ThirdWord.MatchType.REGULAR);
        addWord("(Appkey)参数错误", "need.param.error", ThirdWord.MatchType.REGULAR);
        addWord("(appkey) 不能为空", "need.param", ThirdWord.MatchType.REGULAR);
        addWord("(appkey) 不正确", "need.param.error", ThirdWord.MatchType.REGULAR);
        addWord("(Address) 不能为空", "need.param", ThirdWord.MatchType.REGULAR);
        addWord("(NikeName)参数不能为空", "need.param", ThirdWord.MatchType.REGULAR);
        addWord("(Address)参数错误", "need.param.error", ThirdWord.MatchType.REGULAR);
        addWord("(Token)未找到", "need.param", ThirdWord.MatchType.REGULAR);
        addWord("(Mobile)不存在", "need.param", ThirdWord.MatchType.REGULAR);
        addWord("(ThirdpartAppkey)参数错误", "need.param.error", ThirdWord.MatchType.REGULAR);
        addWord("(RandomCode)参数错误", "need.param.error", ThirdWord.MatchType.REGULAR);
        addWord("(RandomCode)过期", "need.param.error", ThirdWord.MatchType.REGULAR);
        addWord("(RandomCode)状态不正确", "need.param.error", ThirdWord.MatchType.REGULAR);
        addWord("找不到用户实体，请确定(EntityId) 是否正确", "need.param.error", ThirdWord.MatchType.REGULAR);
    }


    /**
     * 翻译
     * @param bundle
     * @param text
     * @return
     */
    public String translateThirdText(PackBundle bundle, String text){
        if(bundle.getLanguage().equalsIgnoreCase("zh")){
            return text;
        }

        if(text == null){
            return text;
        }

        for(ThirdWord word: this.tranList){
            if(word.getMatchType() == ThirdWord.MatchType.CONTAIN){

                if(text.contains(word.getExpression())){
                    return bundle.getString(word.getLangKey());
                }

            } else if(word.getMatchType() == ThirdWord.MatchType.REGULAR){
                if(text.matches(word.getExpression())){
                    Pattern p = Pattern.compile(word.getExpression());
                    Matcher m = p.matcher(text);
                    if(m.groupCount() == 1){
                        return bundle.getString(word.getLangKey(), m.group(1));
                    }
                    else if(m.groupCount() == 2){
                        return bundle.getString(word.getLangKey(), m.group(1), m.group(2));
                    }
                    else {
                        return bundle.getString(word.getLangKey());
                    }
                }

            }
        }

        return text;
    }


    public String translateThirdReturn(PackBundle bundle, String json){
        JSONObject result = JSON.parseObject(json);
        HashMap<String, Object> thirdResult = new HashMap<>();
        if(result != null) {
            handleData(bundle, thirdResult, result);
        }
        return JSON.toJSONString(thirdResult, SerializerFeature.WriteMapNullValue);
    }

    private void handleData(PackBundle bundle, HashMap<String, Object> dataMap, JSONObject jsonObj) {
        if(jsonObj != null) {
            Set<String> keys = jsonObj.keySet();
            if(keys != null && keys.size() > 0) {
                Iterator<String> itr = keys.iterator();
                while(itr.hasNext()) {
                    String key = itr.next();
                    Object obj = jsonObj.get(key);
                    if(obj instanceof String) {
                        String value = (String)obj;
                        String str = translateThirdText(bundle, value);
                        dataMap.put(key, str);
                    } else if(obj instanceof JSONArray) {
                        JSONArray jsonArray = (JSONArray)obj;
                        ArrayList<Object> list = new ArrayList<>();
                        handleArrayData(bundle, list, jsonArray);
                        dataMap.put(key, list);
                    }else if(obj instanceof JSONObject) {
                        HashMap<String, Object> innerDataMap = new HashMap<>();
                        dataMap.put(key, innerDataMap);
                        JSONObject innerJsonObj = (JSONObject)obj;
                        handleData(bundle, innerDataMap, innerJsonObj);
                    } else {
                        dataMap.put(key, obj);
                    }
                }
            }
        }
    }

    private void handleArrayData(PackBundle bundle, ArrayList<Object> list, JSONArray jsonArray) {
        if(jsonArray != null && jsonArray.size() > 0) {
            for(int i=0;i<jsonArray.size(); i++) {
                Object innerObj = jsonArray.get(i);
                if(innerObj instanceof JSONArray) {
                    ArrayList<Object> innerList = new ArrayList<>();
                    list.add(innerList);
                    handleArrayData(bundle, innerList, (JSONArray)innerObj);
                } else if(innerObj instanceof JSONObject) {
                    HashMap<String, Object> innerMap = new HashMap<>();
                    list.add(innerMap);
                    handleData(bundle, innerMap, (JSONObject) innerObj);
                }
            }
        }
    }


    public static void main(String[] args) {

//        Map<String, Object> map = new HashMap<String, Object>();
//        Integer a = (Integer)map.get("a");
//        System.out.println(a);
        String json = "{\"success\":true,\"error\":null,\n" +
                "\"data\":{\"EntityStatus\":0,\"FailMessage\":null,\"Menus\":[{\"Title\":\"公安部快捷认证\",\"Status\":\"去认证\"}]},\"errorCode\":0}";
        json = "{\"success\":true,\"error\":null,\"data\":{\"WebSiteURL\":\"http://www.yuanbao.com\",\"WebSiteCompany\":\"元宝网\",\"LogoUrl\":\"https://creditid.taiyiyun.com/logos/20161230/yuanbaologo.png\",\"AppName\":\"元宝网\",\"Authorized\":false,\"PublicUserEntityName\":\"\",\"PublicUserEntityId\":\"\",\"DefaultUserEntityName\":\"焦春国\",\"DefaultUserEntityId\":\"db530974c1464ee4ae4f30f3841b331f\",\"UrlScheme\":null,\"GetInfoType\":3,\"Status\":0},\"errorCode\":0}";
        PackBundle bundle = LangResource.getInstance().getResourceBundle("en");
        String str = ThirdTranslate.getInstance().translateThirdReturn(bundle, json);
        System.out.println(str);
    }


}
