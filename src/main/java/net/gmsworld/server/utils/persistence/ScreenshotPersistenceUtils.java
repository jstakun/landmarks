package net.gmsworld.server.utils.persistence;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import net.gmsworld.server.persistence.Screenshot;

public class ScreenshotPersistenceUtils {

	public void save(Screenshot s, EntityManager entityManager) {
		EMF.save(s, entityManager);
	}
	
	public Screenshot findById(int id, EntityManager entityManager) {
		return entityManager.find(Screenshot.class, id);
	}
	
	public List<Screenshot> findOlder(int ndays, int limit, EntityManager entityManager) { 
		Query query = entityManager.createNativeQuery("select * from screenshot where now() - interval '" + ndays + " days' > creation_date order by creation_date asc limit :limit", Screenshot.class);
		query.setParameter("limit", limit);
		query.setHint("org.hibernate.cacheable", Boolean.TRUE);
		return query.getResultList();
	}
	
	public boolean delete(int id, EntityManager entityManager) {
		Screenshot s = findById(id, entityManager);
		if (s != null) {
			EMF.remove(s, entityManager);
			return true;
		} else {
			return false;
		}
	}
}
