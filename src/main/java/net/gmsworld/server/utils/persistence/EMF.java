package net.gmsworld.server.utils.persistence;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

public class EMF {

	private static EntityManager entityManager = null;
	
	public static EntityManager getEntityManager() {
		if (entityManager == null) {
			entityManager = Persistence.createEntityManagerFactory("landmarksdb").createEntityManager();
		}
		return entityManager;
	}
}
