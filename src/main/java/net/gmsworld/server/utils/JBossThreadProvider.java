package net.gmsworld.server.utils;

import java.util.concurrent.ThreadFactory;
import org.jboss.threads.JBossThreadFactory;

public class JBossThreadProvider implements ThreadFactory {

	private static final JBossThreadFactory threadFactory = new JBossThreadFactory(null, null, null, "GMS World Landmark Loader", null, null);

	@Override
	public Thread newThread(Runnable r) {
		return threadFactory.newThread(r);
	}

}
