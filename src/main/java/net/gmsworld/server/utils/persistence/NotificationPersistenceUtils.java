package net.gmsworld.server.utils.persistence;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;

import net.gmsworld.server.persistence.Notification;


public class NotificationPersistenceUtils {
	
	private static final Logger logger = Logger.getLogger(NotificationPersistenceUtils.class.getName());

	public Notification persist(String id, Notification.Status status, EntityManager entityManager) {
		Notification n = null;
		if (StringUtils.isNotEmpty(id)) {
			n = findById(id, entityManager);
			if (n == null) {
				n = new Notification(id, status);
				n.setSecret(generateSecret());
				EMF.save(n, entityManager);
			} else {
				n.setStatus(status);
				if (status == Notification.Status.UNVERIFIED && (System.currentTimeMillis() - n.getLastUpdateDate().getTime() > 60 * 60 * 1000)) {
					//if secret is older that 1 hour create new one
					n.setSecret(generateSecret());
				}	
				n.setLastUpdateDate(new Date());
				EMF.update(n ,entityManager);
			}
		}
		return n;
    }
	
	public boolean remove(String id, EntityManager entityManager) {
		Notification n = findById(id, entityManager);
		if (n != null) {
			EMF.remove(n, entityManager);
			return true;
		} else {
			return false;
		}
		
    }
	
	public Notification findById(String id, EntityManager pm) {
		if (StringUtils.isNotEmpty(id)) {
			try {
				TypedQuery<Notification> query = pm.createNamedQuery(Notification.NOTIFICATION_FINDBYID, Notification.class);
				query.setParameter("id", id);
				return query.getSingleResult();
			} catch (Exception e) {
				return null; 
			}
		}
		return null;
	 }
	
	public Notification findBySecret(String secret, EntityManager pm) {
		if (StringUtils.isNotEmpty(secret)) {
			try {
				TypedQuery<Notification> query = pm.createNamedQuery(Notification.NOTIFICATION_FINDBYSECRET, Notification.class);
				query.setParameter("secret", secret);
				return query.getSingleResult();
			} catch (Exception e) {
				return null; 
			}
		}
		return null;
	}
	
	public List<Notification> findByStatus(Notification.Status status, EntityManager pm) {
		List<Notification> notifications = null;
		try {
        	TypedQuery<Notification> query = pm.createNamedQuery(Notification.NOTIFICATION_FINDALLWITHSTATUS, Notification.class);
        	query.setParameter("status", status);
        	notifications = query.getResultList();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } 
		return notifications;
	}
	
	private static String generateSecret() {
		return RandomStringUtils.randomAlphabetic(32) + "." + RandomStringUtils.randomNumeric(4);
	}
}