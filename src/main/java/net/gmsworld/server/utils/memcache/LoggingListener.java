package net.gmsworld.server.utils.memcache;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryCreated;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryRemoved;
import org.infinispan.notifications.cachelistener.event.CacheEntryCreatedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryRemovedEvent;

@Listener
public class LoggingListener {

   private static final Logger logger = Logger.getLogger(LoggingListener.class.getName());

   @CacheEntryCreated
   public void observeAdd(CacheEntryCreatedEvent<?, ?> event) {
      if (!event.isPre()) { // So that message is only logged after operation succeeded
    	  logger.log(Level.INFO, "Entry with key {0} added to cache {1}", new Object[]{event.getKey(), event.getCache().getName()});
      }	  
   }

   @CacheEntryRemoved
   public void observeRemove(CacheEntryRemovedEvent<?, ?> event) {
	   logger.log(Level.INFO, "Entry with key {0} removed from cache {1}", new Object[]{event.getKey(), event.getCache().getName()});
   }
}
