package com.taiyiyun.passport.util;

import java.security.MessageDigest;
public class MD5Util {
	private final static String[] hexDigits = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f" };
	private static final String[] DIGITS_UPPER = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F" };
	/**
	 * 转换字节数组为16进制字串
	 * 
	 * @param b
	 *            字节数组
	 * @return 16进制字串
	 */
	public static String byteArrayToHexString(byte[] b, boolean toUpperCase) {
		StringBuilder resultSb = new StringBuilder();
		for (byte aB : b) {
			resultSb.append(byteToHexString(aB, toUpperCase));
		}
		return resultSb.toString();
	}

	/**
	 * 转换byte到16进制
	 * 
	 * @param b
	 *            要转换的byte
	 * @return 16进制格式
	 */
	private static String byteToHexString(byte b, boolean toUpperCase) {
		int n = b;
		if (n < 0) {
			n = 256 + n;
		}
		int d1 = n / 16;
		int d2 = n % 16;
		return toUpperCase ? (DIGITS_UPPER[d1] + DIGITS_UPPER[d2]) : (hexDigits[d1] + hexDigits[d2]);
	}

	/**
	 * MD5编码
	 * 
	 * @param origin
	 *            原始字符串
	 * @return 经过MD5加密之后的结果
	 */
	public static String MD5Encode(String origin, boolean toUpperCase) {
		String resultString = null;
		try {
			resultString = origin;
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] ff = resultString.getBytes("UTF-8");
			resultString = byteArrayToHexString(md.digest(ff), toUpperCase);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultString;
	}
	/**
	 * 默认返回大写十六进制MD5
	 * @param origin
	 * @return
	 */
	public static String MD5Encode(String origin) {
		
		return MD5Encode(origin,true);
	}
}
