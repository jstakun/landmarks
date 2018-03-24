package net.gmsworld.server.utils.persistence;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

public class EMF {

	private static EntityManager entityManager = Persistence.createEntityManagerFactory("landmarksdb").createEntityManager();
	
	public static EntityManager getEntityManager() {
		return entityManager;
	}
	
	public static void save(Object entity) {
		entityManager.getTransaction().begin();
		entityManager.persist(entity);
		entityManager.getTransaction().commit();
	} 
	
	public static void update(Object entity) {
		entityManager.getTransaction().begin();
		entityManager.merge(entity);
		entityManager.getTransaction().commit();		
	}
	
	public static void remove(Object entity) {
		entityManager.getTransaction().begin();
		entityManager.remove(entity);
		entityManager.getTransaction().commit();		
	}
}
