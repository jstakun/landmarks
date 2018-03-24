package net.gmsworld.server.utils.persistence;

import java.util.Date;
import java.util.List;

import javax.persistence.TypedQuery;

import net.gmsworld.server.persistence.Checkin;

public class CheckinPersistenceUtils {
	
	public void save(Checkin c) {
		EMF.save(c);
	}
	
	public List<Checkin> findByLandmark(int landmarkId) {
		TypedQuery<Checkin> query = EMF.getEntityManager().createNamedQuery(Checkin.FIND_BY_LANDMARK, Checkin.class);
        query.setParameter("id", landmarkId);
		return query.getResultList(); 
    }
	
	public int countNewer(Date from, String username, String venueid) {
		TypedQuery<Long> query = EMF.getEntityManager().createNamedQuery(Checkin.COUNT_NEWER, Long.class);
		query.setParameter("date", from);
		query.setParameter("username", username);
		query.setParameter("venueid", venueid);
		return query.getSingleResult().intValue();
	}
}
