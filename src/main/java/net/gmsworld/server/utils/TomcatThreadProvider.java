package net.gmsworld.server.utils;

import java.util.concurrent.ThreadFactory;

public class TomcatThreadProvider implements ThreadFactory {

	@Override
	public Thread newThread(Runnable r) {
		return new Thread(r);
	}

}
