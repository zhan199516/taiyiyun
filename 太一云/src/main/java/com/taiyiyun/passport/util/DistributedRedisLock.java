package com.taiyiyun.passport.util;

import java.util.concurrent.TimeUnit;

import org.redisson.Config;
import org.redisson.Redisson;
import org.redisson.core.RLock;

public class DistributedRedisLock {
	private static final String LOCK_TITLE = "redisLock_";
	private static Redisson redisson = null;

	static {
		String host = RedisConfUtil.get("redis.host");
		String port = RedisConfUtil.get("redis.port");
		String password = RedisConfUtil.get("redis.password");
		String database = RedisConfUtil.get("redis.database");
		Config config = new Config();
		config.useSingleServer().setAddress(host + ":" + port).setPassword(password)
				.setDatabase(Integer.parseInt(database));
		redisson = Redisson.create(config);
	}

	private DistributedRedisLock() {

	}

	public static void acquire(String lockName) {
		String key = LOCK_TITLE + lockName;
		RLock mylock = redisson.getLock(key);
		mylock.lock(1, TimeUnit.MINUTES);
	}

	public static void release(String lockName) {
		String key = LOCK_TITLE + lockName;
		RLock mylock = redisson.getLock(key);
		mylock.unlock();
	}

	public static void close() {
		redisson.shutdown();
	}
}
