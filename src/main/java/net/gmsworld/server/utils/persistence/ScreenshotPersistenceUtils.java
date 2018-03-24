package net.gmsworld.server.utils.persistence;

import java.util.List;

import javax.persistence.Query;

import net.gmsworld.server.persistence.Screenshot;

public class ScreenshotPersistenceUtils {

	public void save(Screenshot s) {
		EMF.save(s);
	}
	
	public Screenshot findById(int id) {
		return EMF.getEntityManager().find(Screenshot.class, id);
	}
	
	public List<Screenshot> findOlder(int ndays, int limit) { 
		Query query = EMF.getEntityManager().createNativeQuery("select * from screenshot where now() - interval '" + ndays + " days' > creation_date order by creation_date asc limit :limit", Screenshot.class);
		query.setParameter("limit", limit);
		query.setHint("org.hibernate.cacheable", Boolean.TRUE);
		return query.getResultList();
	}
	
	public boolean delete(int id) {
		Screenshot s = findById(id);
		if (s != null) {
			EMF.remove(s);
			return true;
		} else {
			return false;
		}
	}
}
