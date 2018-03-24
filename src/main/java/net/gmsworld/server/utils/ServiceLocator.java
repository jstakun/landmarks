package net.gmsworld.server.utils;

import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class ServiceLocator {
	
	private static ServiceLocator serviceLocator = null;
	Context context = null;
    Map<String, Object> serviceCache = null;

	private ServiceLocator() throws NamingException {
		InitialContext ic = new InitialContext();
		context = (Context) ic.lookup("java:comp/env");
		serviceCache = new HashMap<String, Object>(5);
	}

	public synchronized static ServiceLocator getInstance() throws NamingException {
		if (serviceLocator == null) {
			serviceLocator = new ServiceLocator();
		}
		return serviceLocator;
	}

	public Object getService(String jndiName) throws NamingException {
		if (!serviceCache.containsKey(jndiName)) {
			serviceCache.put(jndiName, context.lookup(jndiName));
		}
		return serviceCache.get(jndiName);
	}
}
