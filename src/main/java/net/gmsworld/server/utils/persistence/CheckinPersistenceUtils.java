package net.gmsworld.server.utils.persistence;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import net.gmsworld.server.persistence.Checkin;

public class CheckinPersistenceUtils {
	
	public void save(Checkin c, EntityManager entityManager) {
		EMF.save(c, entityManager);
	}
	
	public List<Checkin> findByLandmark(int landmarkId, EntityManager entityManager) {
		TypedQuery<Checkin> query = entityManager.createNamedQuery(Checkin.FIND_BY_LANDMARK, Checkin.class);
        query.setParameter("id", landmarkId);
		return query.getResultList(); 
    }
	
	public int countNewer(Date from, String username, String venueid, EntityManager entityManager) {
		TypedQuery<Long> query = entityManager.createNamedQuery(Checkin.COUNT_NEWER, Long.class);
		query.setParameter("date", from);
		query.setParameter("username", username);
		query.setParameter("venueid", venueid);
		return query.getSingleResult().intValue();
	}
}
