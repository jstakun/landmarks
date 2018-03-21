package net.gmsworld.server.utils.persistence;

import java.security.MessageDigest;
import java.util.Base64;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.gmsworld.server.persistence.User;

public class UserPersistenceUtils {
	@PersistenceContext
    private EntityManager entityManager;
	
	private final Logger logger = Logger.getLogger(UserPersistenceUtils.class.getName());
	
	public void save(User u) {
		try {
			u.setPassword(getHash(u.getPassword()));
			entityManager.persist(u);
			entityManager.flush();
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
	}
	
	private void update(User u) {
		entityManager.merge(u);
		entityManager.flush();
	}
	
	public User findById(String login) {
		return entityManager.find(User.class, login);
	}
	
	public void setConfirmation(String login) {
		//
		User u = findById(login);
		if (u != null) {
			u.setConfirmDate(new Date(System.currentTimeMillis()));
			u.setConfirmed(true);
			update(u);
		} else {
			logger.log(Level.SEVERE, "User " + login + " doesn't exists!");
		}
	}
	
	public void setLastLogonDate(String login) {
		User u = findById(login);
		if (u != null) {
		    u.setLastLogonDate(new Date(System.currentTimeMillis()));	
			update(u);
		} else {
			logger.log(Level.SEVERE, "User " + login + " doesn't exists!");
		} 
	}
	
	private static String getHash(String password) throws Exception {
		MessageDigest digester = MessageDigest.getInstance("SHA-256");
	    digester.update(password.getBytes());
	    return Base64.getEncoder().encodeToString(digester.digest());
	}
	
	public boolean login(String login, String password) {
		boolean auth = false;
		User u = findById(login);
		if (u != null) {
			try {
				auth = (getHash(password).equals(u.getPassword()));
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
			}
		} 
		return auth;
	}
	
}
