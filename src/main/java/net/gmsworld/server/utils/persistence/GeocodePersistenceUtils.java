package net.gmsworld.server.utils.persistence;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import net.gmsworld.server.persistence.Geocode;

public class GeocodePersistenceUtils {
	
	public void save(Geocode g) {
		EMF.save(g);
	}
	
	public List<Geocode> findNewest(int limit) {
        TypedQuery<Geocode> query = EMF.getEntityManager().createNamedQuery(Geocode.FIND_NEWEST, Geocode.class);
        query.setMaxResults(limit);
        return query.getResultList();
    }
	
	public Geocode findAddress(String address){
		TypedQuery<Geocode> query = EMF.getEntityManager().createNamedQuery(Geocode.FIND_ADDRESS, Geocode.class);
		query.setParameter("address", address);
		Geocode g = null;
		/*try {
			g = query.getSingleResult();
			g.setCreationDate(new Date(System.currentTimeMillis()));
			save(g); 
		} catch (NoResultException nre) {
		} catch (NonUniqueResultException nure) {
		}*/
		
		List<Geocode> geocodes = query.getResultList();
		if (!geocodes.isEmpty()) {
			g = geocodes.get(0);
			g.setCreationDate(new Date(System.currentTimeMillis()));
			save(g);
		}
					
	    return g;
	}
	
	public Geocode findById(int id) {
		return EMF.getEntityManager().find(Geocode.class, id);
	}
}
