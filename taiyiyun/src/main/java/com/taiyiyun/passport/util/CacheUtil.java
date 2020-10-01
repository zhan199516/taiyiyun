package com.taiyiyun.passport.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;
import com.taiyiyun.passport.init.SpringContext;

public final class CacheUtil {
	
	/** 短缓存（5分钟） */
	public static final String SHORT_CACHE = "shortCache";

	/** 10分钟 缓存 */
	public static final String TEN_MIN_CACHE = "tenMinCache";
	
	/** 15分钟 缓存 */
	public static final String QUARTER_HOUR_CACHE = "quarterHourCache";

	/** 30分钟 缓存 */
	public static final String HALF_HOUR_CACHE = "halfHourCache";

	/** 一天缓存 */
	public static final String ONE_DAY_CACHE = "onedayCache";

	/** 永久缓存 */
	public static final String FINAL_CACHE = "finalCache";
	
	private CacheUtil() {}

	private static final Logger logger = LoggerFactory.getLogger(CacheUtil.class);

	private static CacheManager cacheManager;

	public static final void setCacheManager(CacheManager manager) {
		cacheManager = manager;
	}
	
	private static synchronized final CacheManager getCacheManager() {
		if (null == cacheManager) {
			cacheManager = SpringContext.getBean(CacheManager.class);
		}
		return cacheManager;
	}

	private static final Object put(String cacheName, String key, Object value) {
		logger.debug("[PUT][cache] <- " + cacheName + ", [key] <- " + key + ", [value] <- " + value);
		try {
			getCacheManager().getCache(cacheName).put(key, value);
		} catch (Exception e) {
			logger.error("put [" + key + "] <- " + value + " to cache[" + cacheName + "] error.", e);
		}
		return value;
	}

	public static final Object putShort(String key, Object value) {
		return put(SHORT_CACHE, key, value);
	}
	
	public static final Object putTenMin(String key, Object value) {
		return put(TEN_MIN_CACHE, key, value);
	}
	
	public static final Object putQuarterHour(String key, Object value) {
		return put(QUARTER_HOUR_CACHE, key, value);
	}
	
	public static final Object putHalfHour(String key, Object value) {
		return put(HALF_HOUR_CACHE, key, value);
	}

	public static final Object putFinal(String key, Object value) {
		return put(FINAL_CACHE, key, value);
	}

	public static final Object putOneDay(String key, Object value) {
		return put(ONE_DAY_CACHE, key, value);
	}

	@SuppressWarnings("unchecked")
	private static final <T> T get(String cacheName, String key) {
		ValueWrapper wrapper = getCacheManager().getCache(cacheName).get(key);
		Object obj = (null == wrapper) ? null : wrapper.get();
		logger.debug("[GET][cache] -> " + cacheName + ", [key] -> " + key + ", [value] -> " + obj);
		return (null == obj) ? null : (T) obj;
	}
	
	public static final <T> T getShort(String key) {
		return get(SHORT_CACHE, key);
	}
	
	public static final <T> T getTenMin(String key) {
		return get(TEN_MIN_CACHE, key);
	}
	
	public static final <T> T getQuarterHour(String key) {
		return get(QUARTER_HOUR_CACHE, key);
	}
	
	public static final <T> T getHalfHour(String key) {
		return get(HALF_HOUR_CACHE, key);
	}

	public static final <T> T getFinal(String key) {
		return get(FINAL_CACHE, key);
	}

	public static final <T> T getOneDay(String key) {
		return get(ONE_DAY_CACHE, key);
	}
	
	public static final void clearShort() {
		getCacheManager().getCache(SHORT_CACHE).clear();
	}
	
	public static final void clearTenMin() {
		getCacheManager().getCache(TEN_MIN_CACHE).clear();
	}
	
	public static final void clearHalfHour() {
		getCacheManager().getCache(HALF_HOUR_CACHE).clear();
	}
	
	public static final void clearOneDay() {
		getCacheManager().getCache(ONE_DAY_CACHE).clear();
	}
	
	public static final void clearFinal() {
		getCacheManager().getCache(FINAL_CACHE).clear();
	}
	
	public static final void evict(String key) {
		logger.debug("[EVICT][cache] -> all cache, [key] -> " + key);
		getCacheManager().getCache(SHORT_CACHE).evict(key);
		getCacheManager().getCache(TEN_MIN_CACHE).evict(key);
		getCacheManager().getCache(HALF_HOUR_CACHE).evict(key);
		getCacheManager().getCache(ONE_DAY_CACHE).evict(key);
		getCacheManager().getCache(FINAL_CACHE).evict(key);
	}
}
