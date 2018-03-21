package net.gmsworld.server.utils.memcache;

import java.util.concurrent.TimeUnit;

import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;

public class Config {

	public static org.infinispan.AdvancedCache<String, Object> getLocalCache() {
		org.infinispan.Cache<String, Object> basicCache = getLocalCacheManager().getCache("gmsworld-cache",true);
		return basicCache.getAdvancedCache();
	}
		
	private static EmbeddedCacheManager getLocalCacheManager() {
		GlobalConfiguration glob = new GlobalConfigurationBuilder()
			.globalJmxStatistics().allowDuplicateDomains(true).enable().build();
	
		org.infinispan.configuration.cache.Configuration loc = new org.infinispan.configuration.cache.ConfigurationBuilder()
			.expiration().lifespan(1,TimeUnit.HOURS)
			.build();
		
		return new DefaultCacheManager(glob, loc, true);
	}

}

