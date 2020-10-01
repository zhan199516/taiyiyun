package com.taiyiyun.passport.util;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class NumberUtil {
	
	private NumberUtil() {
		
	}
	
	public static boolean isInteger(String str) {
		return Pattern.matches("^-?[\\d]{1,}", str);
	}
	
	public static boolean isPositiveInteger(String str) {
		return Pattern.matches("[\\d]{1,}", str);
	}
	
	public static boolean isNegtiveInteger(String str) {
		return Pattern.matches("^-[\\d]{1,}", str);
	}
	
	public static boolean isFloat(String str) {
		return Pattern.matches("^-?[\\d]{0,}\\.[\\d]{0,}", str);
	}
	
	public static boolean isPositiveFloat(String str) {
		return Pattern.matches("[\\d]{0,}\\.[\\d]{0,}", str);
	}
	
	public static boolean isNegtiveFloat(String str) {
		return Pattern.matches("^-[\\d]{0,}\\.[\\d]{0,}", str);
	}
	
	public static boolean isNumber(String str) {
		return !StringUtil.isEmptyOrBlank(str) && Pattern.matches("^-?[\\d]{0,}\\.?[\\d]{0,}", str);
	}
	
	public static Integer parseInt(String numberString) {
		return parseInt(numberString, null);
	}
	
	public static Integer parseInt(String numberString, Integer defaultValue) {
		return parseT(numberString, defaultValue, Integer.class);
	}
	
	public static Long parseLong(String numberString) {
		return parseLong(numberString, null);
	}
	
	public static Long parseLong(String numberString, Long defaultValue) {
		return parseT(numberString, defaultValue, Long.class);
	}
	
	public static Float parseFloat(String numberString) {
		return parseFloat(numberString, null);
	}
	
	public static Float parseFloat(String numberString, Float defaultValue) {
		return parseT(numberString, null, Float.class);
	}
	
	public static Double parseDouble(String numberString) {
		return parseDouble(numberString, null);
	}
	
	public static Double parseDouble(String numberString, Double defaultValue) {
		return parseT(numberString, null, Double.class);
	}
	
	public static BigDecimal parseBigDecimal(String numberString) {
		return parseBigDecimal(numberString, null);
	}
	
	public static BigDecimal parseBigDecimal(String numberString, BigDecimal defaultValue) {
		return parseT(numberString, null, BigDecimal.class);
	}
	
	public static String formatInteger(Number number) {
		return String.format("%d", number.longValue());
	}
	
	public static String formatInteger(Number number, int length) {
		return formatInteger(number, length, false);
	}
	
	public static String formatInteger(Number number, int length, boolean isZero) {
		return String.format("%" + (isZero ? "0" : "") + length + "d", number.longValue());
	}
	
	public static String formatFloat(Number number) {
		return StringUtil.trimEnd(StringUtil.trimEnd(formatFloat(number, 0), '0'), '.');
	}
	
	public static String formatFloat(Number number, int scale) {
		String partener = "%" + ((scale <= 0) ? "" : "." + scale) + "f";
		String result = "";
		if (number instanceof Float || number instanceof Double || number instanceof BigDecimal) {
			result =  String.format(partener, number);
		} else {
			result =  String.format(partener, number.doubleValue());
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private static <T> T getFromNumber(Number number, Class<T> clazz) {
		if (ReflectUtil.isFloat(clazz)) {
			return (T) (new Float(number.floatValue()));
		} else if (ReflectUtil.isDouble(clazz)) {
			return (T) (new Double(number.doubleValue()));
		} else if (ReflectUtil.isBigDecimal(clazz)) {
			return (T) ((number instanceof BigDecimal) ? number : new BigDecimal(number.doubleValue()));
		} else if (ReflectUtil.isInteger(clazz)) {
			return (T) (new Integer(number.intValue()));
		} else if (ReflectUtil.isLong(clazz)) {
			return (T) (new Long(number.longValue()));
		}
		throw new RuntimeException("Unsupport class: " + clazz);
	}
	
	private static <T> T parseT(String numberString, T defaultValue, Class<T> clazz) {
		return (!StringUtil.isEmptyOrBlank(numberString) && isNumber(numberString)) ? getFromNumber(new BigDecimal(numberString), clazz) : defaultValue;
	}
	
	public static BigDecimal fromDouble(double d){
		BigDecimal b = new BigDecimal(d);
		return b.setScale(8, BigDecimal.ROUND_HALF_EVEN);
	}

	public static double fromDecimal(BigDecimal b){
		return b.doubleValue();
	}
	
	public static boolean isMobile(String mobile) {
		if(StringUtil.isEmpty(mobile)) {
			return false;
		}
		
		Pattern pattern = Pattern.compile("^1[34578][0-9]{9}$");
		Matcher matcher = pattern.matcher(mobile);
		
		return matcher.matches();
	}
	
	public static String formatAmount(String amount) {
		
		if(StringUtil.isNotEmpty(amount) && amount.contains(".")) {
			String[] _tp = amount.split("\\.");
			
			if(StringUtil.isNotEmpty(_tp[1])) {
				if(Long.parseLong(_tp[1]) == 0L) {
					return _tp[0];
				}
				
				char[] ns = _tp[1].toCharArray();
				
				boolean needAdd = false;
				StringBuffer decimalBf = new StringBuffer();
				for (int i = (ns.length - 1) ; i >= 0; i--) {
					if(Integer.parseInt(String.valueOf(ns[i])) > 0) {
						needAdd =true;
					}
					
					if(needAdd) {
						decimalBf.insert(0, ns[i]);
					}
				}
				
				return decimalBf.insert(0, ".").insert(0, _tp[0]).toString();
			}
		}
		
		return amount;
	}
}
