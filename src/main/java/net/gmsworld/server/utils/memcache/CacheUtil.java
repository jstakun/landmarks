package net.gmsworld.server.utils.memcache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.gmsworld.server.utils.ServiceLocator;

/*
 * Facade for CacheBean
 */
public class CacheUtil {
	
	private static final Logger logger = Logger.getLogger(CacheUtil.class.getName());
	private static CacheBean cacheBean;
	
	private static CacheBean getCache() {
		
		if (cacheBean == null) {
			try {
				cacheBean = (CacheBean) ServiceLocator.getInstance().getService("bean/CacheBean");
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
			}
		}
		
		return cacheBean;
	}
	
	public static void put(String key, Object value) {
		if (getCache() != null) {
			getCache().put(key, value);
		}
	}
	
	public static void putShort(String key, Object value) {
		if (getCache() != null) {
			getCache().putShort(key, value);
		}
	}
	
	public static String getString(String key) {
		return (String) getObject(key);
	}	
	
	public static Object getObject(String key) {
		if (getCache() != null) {
			return getCache().getObject(key);
		} else {
			return null;
		}
	}
	
	public static void increment(String key) {
		getCache().increment(key);
	}
	
	public static boolean containsKey(String key) {
		return getCache().containsKey(key);
	}
	
	public static <T> T getObject(Class<T> type, String key) {
		Object o = null;
		if (getCache() != null) {
			o = getCache().getObject(key);
		}
		if (o != null && type.isAssignableFrom(o.getClass())) {
			return type.cast(o);
	    } else {
	    	return null;
	    }
	}
	
	public static <T> List<T> getList(Class<T> type, String key) {
		//return getObject(List.class, key);
	    Collection<?> c = null; 
	    if (getCache() != null) { 
	    	c = (Collection<?>) getCache().getObject(key);
	    }
	    if (c != null) {
	    	List<T> r = new ArrayList<T>(c.size());
	    	for (Object o : c) {
				if (type.isAssignableFrom(o.getClass())) {
					r.add(type.cast(o));
				}
			}
	    	return r;
		}
	    return null;
	}
}
