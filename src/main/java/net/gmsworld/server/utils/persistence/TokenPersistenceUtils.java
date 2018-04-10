package net.gmsworld.server.utils.persistence;

import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import net.gmsworld.server.persistence.Token;

public class TokenPersistenceUtils {

	private final Logger logger = Logger.getLogger(TokenPersistenceUtils.class.getName());
			
	public void save(Token t, EntityManager entityManager) {
		EMF.save(t, entityManager);
	}
	
	public void update(Token t, EntityManager entityManager) {
		EMF.update(t, entityManager);
	}
	
	public boolean isTokenValid(String key, String scope, EntityManager entityManager) {
		TypedQuery<Token> query = entityManager.createNamedQuery(Token.GET_TOKEN, Token.class);
		boolean isValid = false;
		query.setParameter("key", key);
		query.setParameter("scope", scope);
		try {
			Token t = query.getSingleResult();
			isValid = true;
			t.setCount(t.getCount()+1);
			t.setLastUsageDate(Calendar.getInstance().getTime());
			update(t, entityManager);
		} catch (NoResultException nre) {
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
		return isValid;
	}
	
	public List<Token> getTopTokens(int limit, EntityManager entityManager) {
		TypedQuery<Token> query = entityManager.createNamedQuery(Token.GET_TOP_TOKENS, Token.class);
		query.setMaxResults(limit);
		return query.getResultList();
	}
}
