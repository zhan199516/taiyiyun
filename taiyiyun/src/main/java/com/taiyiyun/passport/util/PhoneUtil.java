package com.taiyiyun.passport.util;

import com.alibaba.fastjson.JSON;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.*;

/**
 * 国际电话号码校验类
 * Created by nina on 2017/9/15.
 */
public class PhoneUtil {

    private static PhoneUtil instance = null;
    private static PhoneNumberUtil phoneUtil = null;
    private Map<Integer, String> countryRegionMap = new HashMap<>();
    private Map<String, Integer> regionCountryMap = new HashMap<>();
    //private Set<Integer> countryCodeSet = new HashSet<>();
    //private Set<String> regionCodeSet = new HashSet<>();

    private PhoneUtil() {
        phoneUtil = PhoneNumberUtil.getInstance();
        initMap();
    }

    public static PhoneUtil getInstance() {
        if(instance == null) {
            synchronized (PhoneUtil.class) {
                if (instance == null) {
                    instance = new PhoneUtil();
                }
            }
        }
        return instance;
    }

    public boolean checkPhoneNumberByCountryCode(String phoneNumber, String countryCode) {
        if(StringUtils.isEmpty(phoneNumber) || StringUtils.isEmpty(countryCode)) {
            return false;
        }
        if(check99PhoneNum(phoneNumber, countryCode)) {
            return true;
        }
        int intCountryCode = 0;
        try {
            intCountryCode = Integer.parseInt(countryCode);
        } catch(Exception e) {
            return false;
        }
        return checkPhoneNumberByCountryCode(phoneNumber, intCountryCode);
    }

    public boolean checkPhoneNumberByCountryCode(String phoneNumber, Integer countryCode) {
        if(StringUtils.isEmpty(phoneNumber) || null == countryCode || countryCode < 0) {
            return false;
        }
        if(check99PhoneNum(phoneNumber, countryCode+"")) {
            return true;
        }
        Long intPhoneNumber = 0L;
        try {
            intPhoneNumber = Long.parseLong(StringUtils.trim(phoneNumber));
        } catch (Exception e) {
            return false;
        }
        Phonenumber.PhoneNumber pn = new Phonenumber.PhoneNumber();
        pn.setCountryCode(countryCode);
        pn.setNationalNumber(intPhoneNumber);
        return phoneUtil.isValidNumber(pn);
    }

    public boolean checkPhoneNumberByRegionCode(String phoneNumber, String regionCode) throws NumberParseException{
        Phonenumber.PhoneNumber pn = phoneUtil.parse(phoneNumber, regionCode);
        if(check99PhoneNum(phoneNumber, pn.getCountryCode()+"")) {
            return true;
        }
        return phoneUtil.isValidNumber(pn);
    }

    public boolean checkPhoneNumberByDefaultRegionCode(String phoneNumber) throws  NumberParseException{
        Phonenumber.PhoneNumber pn = phoneUtil.parse(phoneNumber, Locale.getDefault().getCountry());
        if(check99PhoneNum(phoneNumber, pn.getCountryCode() + "")) {
            return true;
        }
        return phoneUtil.isValidNumber(pn);
    }

    private void initMap() {
        Set<String> regions = phoneUtil.getSupportedRegions();
        Iterator<String> ite = regions.iterator();
        while (ite.hasNext()) {
            String regionCode = ite.next();
            int countryCode = phoneUtil.getCountryCodeForRegion(regionCode);
            countryRegionMap.put(countryCode, regionCode);
            regionCountryMap.put(regionCode, countryCode);
            //countryCodeSet.add(countryCode);
            //regionCodeSet.add(regionCode);
        }
    }

    /**
     * 根据地区简码获取地区名称
     * @param country  国家（地区）简码，如中国-CN
     * @return
     */
    public List<National> fetchNational(String country) throws IOException {
        //Map<String, String> regionCountryMap = this.getRegionCountryNameMap(country);
        LinkedHashMap<String, String> regionCountryNameMap = this.getRegionCountryNameMap(country);
        List<National> list = new ArrayList<>();
        List<String> nameList = getFileNameList();
        int n = 0;
        for(Map.Entry<String, String> entry : regionCountryNameMap.entrySet()) {
            //System.out.println(entry.getKey() + "-" + regionCountryMap.get(entry.getKey()) + "-" + entry.getValue());
            if(nameList.contains(entry.getKey())) {
                n++;
                National national = new National();
                national.setRegionCode(entry.getKey());
                if(regionCountryMap.get(entry.getKey()) != null) {
                    national.setCountryCode(regionCountryMap.get(entry.getKey()));
                }
                national.setCountryName(entry.getValue());
                list.add(national);
            }
        }
        System.out.println(n);
        return list;
    }

    public void printAllLocaleInfos() {
        Locale[] locales = Locale.getAvailableLocales();
        System.out.println(locales.length);
        for(int i=0; i<locales.length; i++) {
            Locale locale = locales[i];
            Locale ltemp = new Locale(locale.getLanguage(), locale.getCountry());
            System.out.println(locale.getDisplayCountry() + "-" + locale.getCountry() + "-" + this.regionCountryMap.get(locale.getCountry()) + "-" + locale.getDisplayName() + "-" + locale.getDisplayLanguage() + "-" + locale.getLanguage() + "-" + locale.getDisplayLanguage(ltemp));
            //System.out.println(locale.getDisplayCountry() + "-" + locale.getCountry() + "-" + pu.regionCountryMap.get(locale.getCountry()) + "-" + locale.getDisplayLanguage(ltemp));
        }
    }

    public static void main(String[] args) throws NumberParseException, IOException, TransformerException, ParserConfigurationException {
        PhoneUtil pu = PhoneUtil.getInstance();
        //pu.printAllLocaleInfos();
        //pu.fetchNational("JP");
        //pu.fetchNational("RU");
        //pu.fetchNational("CN");
        //pu.getRegionCountryNameMap("CN");
        //pu.generateCountryInfoXml("RU");
        //pu.generateCountryInfoJson("RU");
        //getFileNameList();
        //boolean b = pu.checkPhoneNumberByCountryCode("4373718064", "86");
        //boolean b = pu.checkPhoneNumberByCountryCode("99000000289", "86");
        //boolean b = pu.checkPhoneNumberByCountryCode("4087139297", "86");
        //System.out.println(b);
//        String a = "abcd";
//        String substring = a.substring(0, 2);
//        System.out.println(substring);
        boolean b = pu.checkPhoneNumberByCountryCode("99000000289", "86");
        b = pu.checkPhoneNumberByCountryCode("16601106287", 86);
        //b = pu.checkPhoneNumberByRegionCode("99000000289", "CN");
        //b = pu.checkPhoneNumberByDefaultRegionCode("99000000289");
        System.out.println(b);
//        boolean b1 = pu.check99PhoneNum("86-99000000138");
//        System.out.println("验证结果：" + b1);
//        try {
//            makeCode();
//        } catch (SAXException e) {
//            e.printStackTrace();
//        }
    }

    public static void makeCode() throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new File("E:\\xml\\中文版-地区码-国家码-国家名称.xml"));
        NodeList countries = document.getElementsByTagName("country");
        Map<String, String> map = new HashMap<>();
        for(int i=0; i<countries.getLength(); i++) {
            String countryCode = document.getElementsByTagName("countryCode").item(i).getFirstChild().getNodeValue();
            if(!StringUtils.equalsIgnoreCase("1", countryCode)) {
                String regionCode = document.getElementsByTagName("regionCode").item(i).getFirstChild().getNodeValue();
                map.put(countryCode, regionCode);
            }
        }
        StringBuffer sb = new StringBuffer();
        int i = 0;
        for(Map.Entry entry : map.entrySet()) {
            StringBuffer sbi = new StringBuffer("countryDict.Add(\"");
            sbi.append(entry.getKey());
            sbi.append("\",\"");
            sbi.append(entry.getValue());
            sbi.append("\");");
            sbi.append("\r\n");
            sb.append(sbi.toString());
            i++;
        }
        System.out.println(sb.toString());
    }

    private static List<String> getFileNameList() {
        List<String> list = new ArrayList<>();
        File dir = new File("E://png//48");
        if(dir.isDirectory()) {
            File[] files = dir.listFiles();
            for(File file : files) {
                String name = file.getName();
                name = name.substring(0, 2);
                name = name.toUpperCase();
                list.add(name);
            }
        }
        return list;
    }

    private LinkedHashMap<String, String> getRegionCountryNameMap(String country) throws IOException {
        Map<String, String> fileMap = new HashMap<>();
        fileMap.put("ZH", "/countryinfos/country-ZH.txt");
        fileMap.put("RU", "/countryinfos/country-RU.txt");
        fileMap.put("EN", "/countryinfos/country-EN.txt");
        fileMap.put("TW", "/countryinfos/country-TW.txt");
        fileMap.put("JP", "/countryinfos/country-JP.txt");
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        String fileName = fileMap.get(country.toUpperCase());
        if(StringUtils.isEmpty(fileName)) {
            fileName = fileMap.get("ZH");
        }
        InputStream is = this.getClass().getResourceAsStream(fileName);
        InputStreamReader isr = new InputStreamReader(is, "utf-8");
        BufferedReader br = new BufferedReader(isr);
        String s = null;
        while((s = br.readLine()) != null) {
            String countryName = s.substring(0, s.length()-5);
            String region = s.substring(s.length()-5);
            region = region.substring(2,4);
            map.put(region, countryName);
        }
        return map;
    }

    private void generateCountryInfoJson(String countryCode) throws IOException {
        List<National> nationals = this.fetchNational(countryCode);
        String s = JSON.toJSONString(nationals);
        System.out.println(s);
    }

    private void generateCountryInfoXml(String cCode) throws IOException, ParserConfigurationException, TransformerException {
        Map<String, String> regionCountryNameMap = this.getRegionCountryNameMap(cCode);
        List<National> list = new ArrayList<>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();
        Element countries = document.createElement("countries");
        for(Map.Entry<String, String> entry : regionCountryNameMap.entrySet()) {
            Element country = document.createElement("country");
            Element regionCodeElement = document.createElement("regionCode");
            String regionCode = entry.getKey();
            regionCodeElement.setTextContent(regionCode);
            country.appendChild(regionCodeElement);
            Element countryCodeElement = document.createElement("countryCode");
            Integer countryCode = regionCountryMap.get(entry.getKey());
            if(countryCode != null) {
                countryCodeElement.setTextContent(countryCode.toString());
            } else {
                countryCodeElement.setTextContent("");
            }
            country.appendChild(countryCodeElement);
            Element countryNameElement = document.createElement("countryName");
            countryNameElement.setTextContent(entry.getValue());
            country.appendChild(countryNameElement);
            countries.appendChild(country);
        }
        document.appendChild(countries);
        TransformerFactory tff = TransformerFactory.newInstance();
        Transformer tf = tff.newTransformer();
        tf.setOutputProperty(OutputKeys.INDENT, "yes");
        File file = new File("E://xml/"+ cCode + ".xml");
        tf.transform(new DOMSource(document), new StreamResult(file));
    }

    public boolean check99PhoneNum(String phoneNumber, String countryCode) {
        if(StringUtils.isNotEmpty(phoneNumber) && phoneNumber.length() == 11 && StringUtils.equalsIgnoreCase(countryCode, "86")) {
            String str = phoneNumber.substring(0, 2);
            if(StringUtils.equalsIgnoreCase(str, "99")) {
                return true;
            }
        }
        return false;
    }

    public boolean check99PhoneNum(String phoneNumber) {
        String[] phones = phoneNumber.split("-");
        System.out.println(phones.length);
        if(phones.length != 2) {
            return false;
        }
        return check99PhoneNum(phones[1], phones[0]);
    }

    public class National{
        String regionCode;
        String countryName;
        int countryCode;
        public String getRegionCode() {
            return regionCode;
        }

        public void setRegionCode(String regionCode) {
            this.regionCode = regionCode;
        }

        public String getCountryName() {
            return countryName;
        }

        public void setCountryName(String countryName) {
            this.countryName = countryName;
        }

        public int getCountryCode() {
            return countryCode;
        }

        public void setCountryCode(int countryCode) {
            this.countryCode = countryCode;
        }
    }
}
