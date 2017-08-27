package net.gmsworld.server.utils.persistence;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jboss.security.auth.spi.Util;

import net.gmsworld.server.persistence.User;

@Stateless
public class UserPersistenceUtils {
	@PersistenceContext
    private EntityManager entityManager;
	
	private final Logger logger = Logger.getLogger(UserPersistenceUtils.class.getName());
	
	public void save(User u) {
		u.setPassword(getHash(u.getPassword()));
		entityManager.persist(u);
		entityManager.flush();
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
	
	private static String getHash(String password) {
		return Util.createPasswordHash("SHA-256", "BASE64", null, null, password);
	}
	
	public boolean login(String login, String password) {
		boolean auth = false;
		User u = findById(login);
		if (u != null) {
			auth = (getHash(password).equals(u.getPassword()));
		} 
		return auth;
	}
	
}
