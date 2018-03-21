package net.gmsworld.server.utils.persistence;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import net.gmsworld.server.persistence.Screenshot;

public class ScreenshotPersistenceUtils {

	@PersistenceContext
    private EntityManager entityManager;
	
	public void save(Screenshot s) {
		entityManager.persist(s);
		entityManager.flush();
	}
	
	public Screenshot findById(int id) {
		return entityManager.find(Screenshot.class, id);
	}
	
	public List<Screenshot> findOlder(int ndays, int limit) { 
		Query query = entityManager.createNativeQuery("select * from screenshot where now() - interval '" + ndays + " days' > creation_date order by creation_date asc limit :limit", Screenshot.class);
		query.setParameter("limit", limit);
		query.setHint("org.hibernate.cacheable", Boolean.TRUE);
		return query.getResultList();
	}
	
	public boolean delete(int id) {
		Screenshot s = findById(id);
		if (s != null) {
			entityManager.remove(s);
			entityManager.flush();
			return true;
		} else {
			return false;
		}
	}
}
