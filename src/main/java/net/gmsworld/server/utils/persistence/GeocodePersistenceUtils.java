package net.gmsworld.server.utils.persistence;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import net.gmsworld.server.persistence.Geocode;

public class GeocodePersistenceUtils {
	
	public void save(Geocode g, EntityManager entityManager) {
		EMF.save(g, entityManager);
	} 
	
	public synchronized List<Geocode> findNewest(int limit, EntityManager entityManager) {
        TypedQuery<Geocode> query = entityManager.createNamedQuery(Geocode.FIND_NEWEST, Geocode.class);
        query.setMaxResults(limit);
        return query.getResultList();
    }
	
	public Geocode findByAddress(String address, EntityManager entityManager){
		TypedQuery<Geocode> query = entityManager.createNamedQuery(Geocode.FIND_ADDRESS, Geocode.class);
		query.setParameter("address", address);
		Geocode g = null;
		List<Geocode> geocodes = query.getResultList();
		if (!geocodes.isEmpty()) {
			g = geocodes.get(0);
			g.setCreationDate(new Date(System.currentTimeMillis()));
			EMF.update(g, entityManager);
		}			
	    return g;
	}
	
	public Geocode findByCoords(double lat, double lng, double precision, EntityManager entityManager){
		Query query = entityManager.createNativeQuery("select * from geocode where abs(latitude - " + lat + ") < " + precision + " and abs(longitude - " + lng + ") < " + precision + " order by creation_date desc", Geocode.class); 
		Geocode g = null;
		List<Geocode> geocodes = query.getResultList();
		if (!geocodes.isEmpty()) {
			g = geocodes.get(0);
			g.setCreationDate(new Date(System.currentTimeMillis()));
			EMF.update(g, entityManager);
		}			
	    return g;
	}
	
	public Geocode findById(int id, EntityManager entityManager) {
		return entityManager.find(Geocode.class, id);
	}
	
	public boolean remove(int id, EntityManager entityManager) {
		Geocode geocode = findById(id, entityManager);
		if (geocode != null) {
			EMF.remove(geocode, entityManager);
			return true;
		} else {
			return false;
		}
	}
}
