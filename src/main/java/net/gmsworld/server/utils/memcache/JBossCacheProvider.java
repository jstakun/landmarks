package net.gmsworld.server.utils.memcache;

import java.util.List;

public class JBossCacheProvider implements CacheProvider {

	private static JBossCacheProvider instance = new JBossCacheProvider();
	
	public static JBossCacheProvider getInstance() {
		  return instance;
	}
	
	private JBossCacheProvider() {}
	
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
	public <T> T getObject(Class<T> type, String key) {
		return CacheUtil.getObject(type, key);
	}
		
	@Override
	public <T> List<T> getList(Class<T> type, String key) {
		return CacheUtil.getList(type, key);
	}

	@Override
	public Object remove(String key) {
		return CacheUtil.remove(key);
	}
}
