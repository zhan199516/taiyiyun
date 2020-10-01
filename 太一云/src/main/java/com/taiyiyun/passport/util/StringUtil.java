package com.taiyiyun.passport.util;

import java.util.Date;
import java.util.Random;
import java.util.UUID;


public final class StringUtil {
	
	private StringUtil() {
		
	}
	
	public static boolean isNull(Object obj) {
		return (null == obj);
	}
	
	public static boolean isEmptyOrBlank(Object obj) {
		return (null == obj) ? true : obj.toString().trim().length() == 0;
	}
	
	public static boolean isEmpty(Object obj) {
		return isEmptyOrBlank(obj);
	}
	
	public static String trimEnd(String src, char suffix) {
		if (isNull(src) || isNull(suffix)) {
			return "";
		}
		char[] chars = src.toCharArray();
		int index = chars.length;
		for (int i = chars.length - 1; i >= 0; --i) {
			if (chars[i] != suffix) {
				index = i + 1;
			}
		}
		char[] newchars = new char[index];
		System.arraycopy(chars, 0, newchars, index, index);
		return new String(newchars);
	}
	
	public static String uuid() {
		return new String(UUID.randomUUID().toString().replace("-", ""));
	}
	
	/**
	 * 首字母小写
	 * 
	 * @param src
	 * @return
	 */
	public static String firstLower(String src) {
		if (isEmptyOrBlank(src)) {
			return "";
		}
		return new String(src.substring(0, 1).toLowerCase() + src.substring(1));
	}
	
	public static String toStr(Object val) {
		if (null == val) {
			return "";
		}
		if (val instanceof Date) {
			return DateUtil.toString((Date) val, DateUtil.Format.DATE_TIME_HYPHEN);
		}
		return String.valueOf(val);
	}
	
	public static String firstUpper(String src) {
		if (isEmptyOrBlank(src)) {
			return src;
		}
		return new String(src.substring(0, 1).toUpperCase() + src.substring(1));
	}

	public static boolean isNotEmpty(Object value) {
		
		return !isEmpty(value);
	}

	public static String getRandomString(int length, boolean useNum, boolean useLow, boolean useUpp){
		Random random = new Random();

		String str = "";
		String s = null;
		if (useNum) { str += "0123456789"; }
		if (useLow) { str += "abcdefghijklmnopqrstuvwxyz"; }
		if (useUpp) { str += "ABCDEFGHIJKLMNOPQRSTUVWXYZ"; }
		byte[] bytes = str.getBytes();

		for(int i = 0; i < length; i++){
			int pos = random.nextInt(str.length() - 1);
			s += str.substring(pos, pos + 1);
		}
		return s;
	}

	public static String getUUID() {
		UUID uuid = UUID.randomUUID();
		return uuid.toString().replace("-", "");
	}

}
