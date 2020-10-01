package com.taiyiyun.passport.consts;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Properties;

public final class MsgTemplate {

	public static final Logger logger = LoggerFactory.getLogger(MsgTemplate.class);

	private MsgTemplate() {}
	
	private static final String TEMP_FILE = "/template.properties";

	private static Properties configProperties = null;
	
	/**
	 * 加载config.properties数据到缓存中
	 */
	public static void reload() {
		configProperties = new Properties();
		try {
			logger.debug("loading config from file: " + TEMP_FILE);
			configProperties.load(MsgTemplate.class.getResourceAsStream(TEMP_FILE));
		} catch (IOException e) {
			throw new RuntimeException("加载[" + TEMP_FILE + "]文件异常，在：getResourceAsStream", e);
		}
	}
	
	public static String getValue(String key,Object... arguments) {
		return getValue(key, "",arguments);
	}
	
	public static String getValue(String key, String dft,Object... arguments) {
		if (null == configProperties) {
			reload();
		}
		String value = configProperties.getProperty(key, dft);
		if (arguments != null && arguments.length > 0){
			value = getString(value,arguments);
		}
		logger.debug("config[" + key + "] -> " + value);
		return value;
	}

	private static String getString(String pattern, Object ... arguments){
		String result = "";
		try {
			MessageFormat temp = new MessageFormat(pattern);
			result = temp.format(arguments);
		} catch (Exception e) {
			logger.info("MsgTemplate getString error:" + e);
		}
		return result;
	}
}
