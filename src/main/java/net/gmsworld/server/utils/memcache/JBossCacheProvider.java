package net.gmsworld.server.utils.memcache;

import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.utils.HttpUtils;

public class JBossCacheProvider implements CacheProvider {

	private static final Logger logger = Logger.getLogger(JBossCacheProvider.class.getName());
	
	@Override
	public boolean containsKey(String key) {
		return CacheUtil.containsKey(key);
	}

	@Override
	public Object getObject(String key) {
		return CacheUtil.getObject(key);
	}

	@Override
	public String getString(String key) {
		return CacheUtil.getString(key);
	}

	@Override
	public void put(String key, Object value) {
		CacheUtil.put(key, value);
		
	}

	@Override
	public void put(String key, Object value, int options) {
		CacheUtil.put(key, value);	
	}

	@Override
	public void putToSecondLevelCache(String key, String value) {
		try {
			URL cacheUrl = new URL("http://cache-gmsworld.rhcloud.com/rest/cache/" + key);
			String resp = HttpUtils.processFileRequestWithBasicAuthn(cacheUrl, "POST", null, value, "application/json", Commons.getProperty(Property.RH_GMS_USER));
			logger.log(Level.INFO, "Cache response: " + resp);
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
	}
	
	@Override
	public String getFromSecondLevelCache(String key) {
		try {
			URL cacheUrl = new URL("http://cache-gmsworld.rhcloud.com/rest/cache/" + key);
			return HttpUtils.processFileRequestWithBasicAuthn(cacheUrl, Commons.getProperty(Property.RH_GMS_USER), false);
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			return null;
		}
	}

	@Override
	public <T> T getObject(Class<T> type, String key) {
		return CacheUtil.getObject(type, key);
	}
		
	@Override
	public <T> List<T> getList(Class<T> type, String key) {
		return CacheUtil.getList(type, key);
	}
}
