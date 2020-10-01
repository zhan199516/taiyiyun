package com.taiyiyun.passport.service.impl;

import com.taiyiyun.passport.service.IRedisService;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
import java.util.List;

@Service
public class RedisServiceImpl<K> implements IRedisService {

	@Resource
	private RedisTemplate<String, Object> redisTemplate;

	@Override
	public boolean put(final String key, final Object value, final long expires) {
		try {
			return redisTemplate.execute(new RedisCallback<Boolean>() {
				@Override
				public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
					RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
					byte[] byteKey = serializer.serialize(key);
					byte[] byteValue = serialize(value);
					connection.set(byteKey, byteValue);
					connection.expire(byteKey, expires);
					return true;
				}

			});
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public <T> T get(final String key) {
		return redisTemplate.execute(new RedisCallback<T>() {

			@SuppressWarnings("unchecked")
			@Override
			public T doInRedis(RedisConnection connection) throws DataAccessException {
				byte[] byteKey = redisTemplate.getStringSerializer().serialize(key);
				byte[] byteValue = connection.get(byteKey);
				if (null == byteValue || byteValue.length <= 0) {
					return null;
				}
				return (T) toObject(byteValue);
			}
		});
	}
	
	@Override
	public long evict(final String key) {
		try {
			return redisTemplate.execute(new RedisCallback<Long>() {
				@Override
				public Long doInRedis(RedisConnection connection) throws DataAccessException {
					byte[] byteKey = redisTemplate.getStringSerializer().serialize(key);
					return connection.del(byteKey);
				}
			});
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0l;
	}

	public static byte[] serialize(Object obj) {
		ObjectOutputStream obi = null;
		ByteArrayOutputStream bai = null;
		try {
			bai = new ByteArrayOutputStream();
			obi = new ObjectOutputStream(bai);
			obi.writeObject(obj);
			byte[] byt = bai.toByteArray();
			return byt;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (obi != null) {
				try {
					obi.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				obi = null;
			}
			if (null != bai) {
				try {
					bai.close();
					bai = null;
				} catch (Exception e2) {

				}
			}
		}
		return null;
	}

	private Object toObject(byte[] bytes) {
		ByteArrayInputStream bin = null;
		ObjectInputStream oin = null;
		try {
			bin = new ByteArrayInputStream(bytes);
			oin = new ObjectInputStream(bin);
			return oin.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (null != oin) {
				try {
					oin.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				oin = null;
			}
			if (null != bin) {
				try {
					bin.close();
					bin.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}
	
	@Override
	public boolean setNX(final String key, final String value) {
		Object obj = null;
		
		try {
			obj = redisTemplate.execute(new RedisCallback<Object>() {
				@Override
				public Object doInRedis(RedisConnection connection) throws DataAccessException {
					byte[] byteKey = redisTemplate.getStringSerializer().serialize(key);
					byte[] byteValue = redisTemplate.getStringSerializer().serialize(value);
					Boolean success = connection.setNX(byteKey, byteValue);
					connection.expire(byteKey, 60 * 30);
					connection.close();
					return success;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return obj == null ? false : (Boolean)obj;
	}
	
	@Override
	public boolean setNX(final String key, final Object value, final long expire) {
		Object obj = null;
		
		try {
			obj = redisTemplate.execute(new RedisCallback<Object>() {
				@Override
				public Object doInRedis(RedisConnection connection) throws DataAccessException {
					byte[] byteKey = redisTemplate.getStringSerializer().serialize(key);
					byte[] byteValue = RedisServiceImpl.serialize(value);
					Boolean success = connection.setNX(byteKey, byteValue);
					
					if(expire > 0){
						connection.expire(byteKey, expire);
					}
					connection.close();
					return success;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return obj == null ? false : (Boolean)obj;
	}

	@Override
	public Long getIncreNumber(final String key, final long expire) {
		
		Long rs = null;
		
		try {
			rs = redisTemplate.execute(new RedisCallback<Long>() {
				@Override
				public Long doInRedis(RedisConnection connection) throws DataAccessException {
					byte[] byteKey = redisTemplate.getStringSerializer().serialize(key);
					Long num = connection.incr(byteKey);
					if(expire > 0){
						connection.expire(byteKey, expire);
					}
					connection.close();
					return num;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return rs;
	}

	@Override
	public <T> T getSet(final String key, final String value) {
		return redisTemplate.execute(new RedisCallback<T>() {

			@Override
			public T doInRedis(RedisConnection connection) throws DataAccessException {
				byte[] keyByte = redisTemplate.getStringSerializer().serialize(key);
				byte[] valueByte = redisTemplate.getStringSerializer().serialize(value);
				
				return (T) toObject(connection.getSet(keyByte, valueByte));
			}
		});
	}

	@Override
	public boolean push(final String key,final Object value, final long expires) {
		try {
			return redisTemplate.execute(new RedisCallback<Boolean>() {
				@Override
				public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
					RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
					byte[] byteKey = serializer.serialize(key);
					byte[] byteValue = serialize(value);
					connection.lPush(byteKey, byteValue);
					return true;
				}

			});
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 批量插入
	 * @param key
	 * @param values
	 * @return
	 */
	@Override
	public boolean pushBatch(final String key, final List<String> values) {
		try {
			return redisTemplate.execute(new RedisCallback<Boolean>() {
				@Override
				public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
					RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
					byte[] byteKey = serializer.serialize(key);
					connection.openPipeline();
					for (String value:values){
						byte[] byteValue = serialize(value);
						connection.lPush(byteKey, byteValue);
					}
					connection.closePipeline();
					return true;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public <T> T pop(final String key) {
		return redisTemplate.execute(new RedisCallback<T>() {
			@SuppressWarnings("unchecked")
			@Override
			public T doInRedis(RedisConnection connection) throws DataAccessException {
				byte[] byteKey = redisTemplate.getStringSerializer().serialize(key);
				byte[] byteValue = connection.lPop(byteKey);
				if (null == byteValue || byteValue.length <= 0) {
					return null;
				}
				return (T) toObject(byteValue);
			}
		});
	}

}
