package net.gmsworld.server.struts;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.interceptor.ServletRequestAware;

import com.opensymphony.xwork2.ActionSupport;

import net.gmsworld.server.persistence.Token;
import net.gmsworld.server.utils.DateUtils;
import net.gmsworld.server.utils.ServiceLocator;
import net.gmsworld.server.utils.TokenUtils;
import net.gmsworld.server.utils.persistence.EMF;
import net.gmsworld.server.utils.persistence.TokenPersistenceUtils;

public class TokenAction extends ActionSupport implements ServletRequestAware {
	
	private static final Logger logger = Logger.getLogger(TokenAction.class.getName());
	private HttpServletRequest request;
	private static final long serialVersionUID = 1L;
    private String scope, user, key;
    private Integer limit;
    
    private TokenPersistenceUtils getTokenPersistenceUtils() throws Exception { 
    	return (TokenPersistenceUtils) ServiceLocator.getInstance().getService("bean/TokenPersistenceUtils");
    }
    
    @Override
	public void setServletRequest(HttpServletRequest arg0) {
		this.request = arg0;
	}
	
	public String createToken() {
		String result;
		if (scope != null) {
			EntityManager em = EMF.getEntityManager();
			try {
				Date validityDate = DateUtils.afterOneHundredYearsFromNow();
				String key = TokenUtils.generateToken();
				Token token = new Token(key, validityDate, scope, user);
				getTokenPersistenceUtils().save(token, em);
				request.setAttribute(JSonDataAction.JSON_OUTPUT, token);
				result = "json";
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				addActionError(e.getMessage());
		    	result = ERROR;
			} finally {
				em.close();
			}
		} else {
			addActionError("Missing required parameter!");
	    	result = ERROR;
		}
		return result;
	}
	
	public String isValidToken() {
		String result;
		if (key != null && scope != null) {
			boolean isValid = false;
			EntityManager em = EMF.getEntityManager();
			try {
				isValid = getTokenPersistenceUtils().isTokenValid(key, scope, em);
				request.setAttribute(JSonDataAction.JSON_OUTPUT, isValid);
		 		result = "json";
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				addActionError(e.getMessage());
		    	result = ERROR;
			}			
		} else {
			addActionError("Missing required parameter!");
	    	result = ERROR;
		}
		return result;
	}
	
	public String getTopTokens() {
		EntityManager em = EMF.getEntityManager();
		String result;
		try {
			if (limit == null) {
				limit = 10;
			}
			List<Token> tokens = getTokenPersistenceUtils().getTopTokens(limit, em);
			request.setAttribute(JSonDataAction.JSON_OUTPUT, tokens);
	 		result = "json";
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			addActionError(e.getMessage());
	    	result = ERROR;
		} finally {
			em.close();
		}
		return result;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Integer getLimit() {
		return limit;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}
}
