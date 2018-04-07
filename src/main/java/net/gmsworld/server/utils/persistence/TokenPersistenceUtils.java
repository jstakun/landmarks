package net.gmsworld.server.utils.persistence;

import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import net.gmsworld.server.persistence.Token;

public class TokenPersistenceUtils {

	private final Logger logger = Logger.getLogger(TokenPersistenceUtils.class.getName());
	
	public void save(Token t) {
		EMF.save(t);
	}
	
	public void update(Token t) {
		EMF.update(t);
	}
	
	public boolean isTokenValid(String key, String scope) {
		TypedQuery<Token> query = EMF.getEntityManager().createNamedQuery(Token.GET_TOKEN, Token.class);
		boolean isValid = false;
		query.setParameter("key", key);
		query.setParameter("scope", scope);
		try {
			Token t = query.getSingleResult();
			isValid = true;
			synchronized (t) {
				t.setCount(t.getCount()+1);
				t.setLastUsageDate(Calendar.getInstance().getTime());
				update(t);
			}
		} catch (NoResultException nre) {
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
		return isValid;
	}
	
	public List<Token> getTopTokens(int limit) {
		TypedQuery<Token> query = EMF.getEntityManager().createNamedQuery(Token.GET_TOP_TOKENS, Token.class);
		query.setMaxResults(limit);
		return query.getResultList();
	}
}
