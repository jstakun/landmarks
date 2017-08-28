package net.gmsworld.server.utils.memcache;

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;

import org.infinispan.Cache;
import org.infinispan.manager.EmbeddedCacheManager;

import javax.inject.Inject;

@Singleton
public class CacheBean {
	
   //<cache-container name="gms-world" default-cache="local" jndi-name="java:jboss/infinispan/gmsworld">
   //    <local-cache name="local">
    //      <expiration lifespan="360000" />
     //  </local-cache> 
    //</cache-container>
	
	//@Resource(lookup = "java:jboss/infinispan/gmsworld")
	//private EmbeddedCacheManager container;
	 
	@Inject
	private Cache<String, Object> cache;
	
	@PostConstruct
	public void start()
    {
		//cache = container.getCache();
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
		//return false;
	}
}
