package com.taiyiyun.passport.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Properties;



public class RedisConfUtil {
	private static final Logger logger = LoggerFactory.getLogger(RedisConfUtil.class);
	private RedisConfUtil() {}
	
	private static final String CONFIG_FILE = "/redis.properties";

	private static Properties configProperties = null;
	
	/**
	 * 加载redis.propertiess数据到缓存中
	 */
	public static void reload() {
		configProperties = new Properties();
		try {
			logger.debug("loading config from file: " + CONFIG_FILE);
			configProperties.load(RedisConfUtil.class.getResourceAsStream(CONFIG_FILE));
		} catch (IOException e) {
			throw new RuntimeException("加载[" + CONFIG_FILE + "]文件异常，在：getResourceAsStream", e);
		}
	}
	
	public static String get(String key) {
		return get(key, "");
	}
	
	public static String get(String key, String dft) {
		if (null == configProperties) {
			reload();
		}
		String value = configProperties.getProperty(key, dft);
		logger.debug("config[" + key + "] -> " + value);
		return value;
	}
	
	public static int getInt(String key, int dft) {
		String value = get(key);
		return NumberUtil.isInteger(value) ? new BigDecimal(value).intValue() : dft;
	}
	
	public static long getLong(String key, long dft) {
		String value = get(key);
		return NumberUtil.isInteger(value) ? new BigDecimal(value).longValue() : dft;
	}
	
	public static double getDouble(String key, double dft) {
		String value = get(key);
		return NumberUtil.isFloat(value) ? new BigDecimal(value).doubleValue() : dft;
	}
	
	public static String[] getArray(String key, String[] dft) {
		String value = get(key);
		if (StringUtil.isEmptyOrBlank(value)) {
			return dft;
		}
		return value.split(",");
	}

	public static Boolean get(String key, Boolean dft) {
		String value = get(key, "").toLowerCase();
		if (StringUtil.isEmptyOrBlank(value)) {
			return dft;
		}
		return "true".equals(value) || "1".equals(value) || "yes".equals(value) || "on".equals(value);
	}
}
