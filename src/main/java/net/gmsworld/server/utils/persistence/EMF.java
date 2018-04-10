package net.gmsworld.server.utils.persistence;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class EMF {

	private static EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("landmarksdb");
	
	public static EntityManager getEntityManager() {
		return entityManagerFactory.createEntityManager();
	}
	
	protected static void save(Object entity, EntityManager entityManager) {
		entityManager.getTransaction().begin();
		entityManager.persist(entity);
		entityManager.getTransaction().commit();
	} 
	
	protected static void update(Object entity, EntityManager entityManager) {
		entityManager.getTransaction().begin();
		entityManager.merge(entity);
		entityManager.getTransaction().commit();		
	}
	
	protected static void remove(Object entity, EntityManager entityManager) {
		entityManager.getTransaction().begin();
		entityManager.remove(entity);
		entityManager.getTransaction().commit();		
	}
	
	public static void close(EntityManager entityManager) {
		entityManager.close();
	}
}
