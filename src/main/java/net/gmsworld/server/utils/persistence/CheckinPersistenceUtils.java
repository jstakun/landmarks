package net.gmsworld.server.utils.persistence;

import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import net.gmsworld.server.persistence.Checkin;

@Stateless
public class CheckinPersistenceUtils {
	
	@PersistenceContext
    private EntityManager entityManager;
	
	public void save(Checkin c) {
		entityManager.persist(c);
		entityManager.flush();
	}
	
	public List<Checkin> findByLandmark(int landmarkId) {
		TypedQuery<Checkin> query = entityManager.createNamedQuery(Checkin.FIND_BY_LANDMARK, Checkin.class);
        query.setParameter("id", landmarkId);
		return query.getResultList(); 
    }
	
	public int countNewer(Date from, String username, String venueid) {
		TypedQuery<Long> query = entityManager.createNamedQuery(Checkin.COUNT_NEWER, Long.class);
		query.setParameter("date", from);
		query.setParameter("username", username);
		query.setParameter("venueid", venueid);
		return query.getSingleResult().intValue();
	}
}
