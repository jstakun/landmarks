package net.gmsworld.server.utils.persistence;

import java.security.MessageDigest;
import java.util.Base64;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import net.gmsworld.server.persistence.User;

public class UserPersistenceUtils {
	
	private final Logger logger = Logger.getLogger(UserPersistenceUtils.class.getName());
	
	public void save(User u, EntityManager entityManager) {
		try {
			u.setPassword(getHash(u.getPassword()));
			EMF.save(u, entityManager);
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
	}
	
	public void update(User u, EntityManager entityManager) {
		EMF.update(u, entityManager);
	}
	
	public void remove(User u, EntityManager entityManager) {
		EMF.remove(u, entityManager);
	}
	
	public User findById(String login, EntityManager entityManager) {
		return entityManager.find(User.class, login);
	}
	
	public User findBySecret(String secret, EntityManager entityManager) {
		 TypedQuery<User> query = entityManager.createNamedQuery(User.FIND_BY_SECRET, User.class);
		 query.setParameter("secret", secret);
		 return query.getSingleResult();
	}
	
	public void setConfirmation(String login, EntityManager entityManager) {
		//
		User u = findById(login, entityManager);
		if (u != null) {
			u.setConfirmDate(new Date(System.currentTimeMillis()));
			u.setConfirmed(true);
			update(u, entityManager);
		} else {
			logger.log(Level.SEVERE, "User " + login + " doesn't exists!");
		}
	}
	
	public void setLastLogonDate(String login, EntityManager entityManager) {
		User u = findById(login, entityManager);
		if (u != null) {
		    u.setLastLogonDate(new Date(System.currentTimeMillis()));	
			update(u, entityManager);
		} else {
			logger.log(Level.SEVERE, "User " + login + " doesn't exists!");
		} 
	}
	
	public static String getHash(String password) throws Exception {
		MessageDigest digester = MessageDigest.getInstance("SHA-256");
	    digester.update(password.getBytes());
	    return Base64.getEncoder().encodeToString(digester.digest());
	}
	
	public boolean login(String login, String password, boolean enc, EntityManager entityManager) {
		boolean auth = false;
		User u = findById(login, entityManager);
		if (u != null) {
			try {
				if (enc) {
					auth = password.equals(u.getPassword());
				} else {
					auth = (getHash(password).equals(u.getPassword()));
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
			}
		} 
		return auth;
	}
}
