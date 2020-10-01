package com.taiyiyun.passport.util;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.Map;


public class MyUtils {
	private static final Logger logger = LoggerFactory.getLogger(MyUtils.class);
	private static final String SECRET = "CA3A46524E0D4867A535E967A7F6BB2F";
	public static final String AESKEY = "CA3A46524E0D4867";  
	/**
	 * 签名算法
	 * @param map_get 上传参数Map
	 */
	public static String mSignatureAlgorithm(Map<String,String> map_get){
        String str_Stitch = "";
	    for (Map.Entry<String, String> entry : map_get.entrySet()) {
	        str_Stitch = str_Stitch + entry.getKey() + entry.getValue() ;
	    }
	    String str_StitchAll = SECRET + str_Stitch.trim() + SECRET;
		logger.info("str_StitchAll"+str_StitchAll.trim());
	    System.out.println("str_StitchAll................"+str_StitchAll.trim());
	    // MD5加密
	    return getMD5(str_StitchAll,true);
	}
	
	/**
	 * MD5加密(信息加密算法)
	 * @param message 要进行MD5加密的字符串
	 * @return 加密结果为32位字符串
	 */
	public static String getMD5(String message, boolean isUpperCase) {

	    MessageDigest messageDigest;
	    StringBuilder md5StrBuff = new StringBuilder();

	    try {

	        messageDigest = MessageDigest.getInstance("MD5");
	        messageDigest.reset();
	        messageDigest.update(message.getBytes("UTF-8"));

	        byte[] byteArray = messageDigest.digest();

	        for (int i = 0; i < byteArray.length; i++) {
	            if (Integer.toHexString(0xFF & byteArray[i]).length() == 1){
	                md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
	            } else{
	                md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
	            }
	        }

	    } catch (Exception e) {
	        throw new RuntimeException();
	    }

	    if (isUpperCase){
	        return md5StrBuff.toString().toUpperCase(); // 字母大写
	    } else {
	        return md5StrBuff.toString();
	    }

	}
	
	
	/**
	 * 加密
	 * @param sSrc  密码
	 * @param sKey  此处使用AES-128-ECB加密模式，key需要为16位。
	 * @throws Exception
	 */
	public static String Encrypt(String sSrc, String sKey) throws Exception {

	    // 判断Key是否为空
	    if (sKey == null) {
	        System.out.print("Key为空null");
	        return null;
	    }

	    // 判断Key是否为16位
	    if (sKey.length() != 16) {
	        System.out.print("Key长度不是16位");
	        return null;
	    }

	    byte[] raw = sKey.getBytes("utf-8");
	    SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
	    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");//"算法/模式/补码方式"
	    cipher.init(Cipher.ENCRYPT_MODE, skeySpec,new IvParameterSpec(getIV()));
	    byte[] encrypted = cipher.doFinal(sSrc.getBytes("utf-8"));

	    return Base64.encodeBase64String(encrypted);

	}

	static byte[] getIV() throws UnsupportedEncodingException {
	    String iv = "tPqMUvpBP4Y3LU2l"; //IV length: must be 16 bytes long
	    return iv.getBytes("utf-8");
	}
	
}
