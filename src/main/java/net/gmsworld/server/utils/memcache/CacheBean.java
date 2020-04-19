package net.gmsworld.server.utils.memcache;

import java.util.concurrent.TimeUnit;

import org.infinispan.Cache;

public class CacheBean {
	
   private Cache<String, Object> cache;
	
	public CacheBean()
    {
		cache = Config.getLocalCache();
		cache.addListener(new LoggingListener());
	}
	
	public void put(String key, Object value) {
		cache.put(key, value);
	}
	
	public Object getObject(String key) {
		return cache.get(key);
		//return null;
	}
	
	public void putShort(String key, Object value) {
		cache.put(key, value, 120, TimeUnit.SECONDS);
	}
	
	public void increment(String key) {
		Integer value = (Integer)cache.get(key);
		if (value != null) {
			cache.replace(key, value+1);
		}
	}
	
	public boolean containsKey(String key) {
		return cache.containsKey(key);
	}
	
	public Object remove(String key) {
		return cache.remove(key);
	}
	
	public void removeAll(String prefix, int minSuffix, int maxSuffix) {
		for (int i=minSuffix;i<=maxSuffix;i++) {
			if (cache.containsKey(prefix + i)) {
				cache.remove(prefix + i);
			}
		}
	}
}
