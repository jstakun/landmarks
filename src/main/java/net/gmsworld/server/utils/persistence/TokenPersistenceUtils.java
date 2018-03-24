package net.gmsworld.server.utils.persistence;

import java.util.Calendar;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import net.gmsworld.server.persistence.Token;

public class TokenPersistenceUtils {

	public void save(Token t) {
		EMF.save(t);
	}
	
	public void update(Token t) {
		EMF.update(t);
	}
	
	public boolean isTokenValid(String key, String scope) {
		TypedQuery<Token> query = EMF.getEntityManager().createNamedQuery(Token.GET_TOKEN, Token.class);
		query.setParameter("key", key);
		query.setParameter("scope", scope);
		try {
			Token t = query.getSingleResult();
			t.setCount(t.getCount()+1);
			t.setLastUsageDate(Calendar.getInstance().getTime());
			update(t);
			return true;
		} catch (NoResultException nre) {
			return false;
		}
	}
	
	public List<Token> getTopTokens(int limit) {
		TypedQuery<Token> query = EMF.getEntityManager().createNamedQuery(Token.GET_TOP_TOKENS, Token.class);
		query.setMaxResults(limit);
		return query.getResultList();
	}
}
