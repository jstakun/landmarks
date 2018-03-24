package net.gmsworld.server.utils;

import java.util.concurrent.ThreadFactory;

public class TomcatThreadProvider implements ThreadFactory {

	@Override
	public Thread newThread(Runnable r) {
		//TODO use tomcat thread pool
		return new Thread(r);
	}

}
