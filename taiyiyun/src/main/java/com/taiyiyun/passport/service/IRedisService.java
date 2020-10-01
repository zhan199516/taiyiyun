package com.taiyiyun.passport.service;

import java.util.List;

public interface IRedisService {

	public boolean put(String key, Object value, long expires);

	public <T> T get(String key);

	public long evict(String key);

	public boolean setNX(String key, String value);

	public boolean setNX(String key, Object value, long expire);
	
	public Long getIncreNumber(String key, long expire);

	public <T> T getSet(String key, String value);
	
	public boolean push(String key, Object value, long expires);

	public boolean pushBatch(String key,List<String> values);


	public <T> T pop(String key);

}
