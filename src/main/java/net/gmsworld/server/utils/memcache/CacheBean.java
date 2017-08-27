package net.gmsworld.server.utils.memcache;

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;

import org.infinispan.Cache;
import org.infinispan.manager.EmbeddedCacheManager;

@Singleton
public class CacheBean {
	@Resource(lookup = "java:jboss/infinispan/gmsworld")
	private EmbeddedCacheManager container;
	private Cache<String, Object> cache;
	
	@PostConstruct
	public void start()
    {
		cache = container.getCache();
		cache.addListener(new LoggingListener());
	}
	
	public void put(String key, Object value) {
		cache.put(key, value);
	}
	
	public Object getObject(String key) {
		return cache.get(key);
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
}
