package net.gmsworld.server.struts;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.interceptor.ServletRequestAware;

import com.opensymphony.xwork2.ActionSupport;

import net.gmsworld.server.persistence.Token;
import net.gmsworld.server.utils.DateUtils;
import net.gmsworld.server.utils.ServiceLocator;
import net.gmsworld.server.utils.TokenUtils;
import net.gmsworld.server.utils.persistence.TokenPersistenceUtils;

public class TokenAction extends ActionSupport implements ServletRequestAware {
	
	private static final Logger logger = Logger.getLogger(TokenAction.class.getName());
	private HttpServletRequest request;
	private static final long serialVersionUID = 1L;
    private String scope, user, key;
    private Integer limit;
    
    @Override
	public void setServletRequest(HttpServletRequest arg0) {
		this.request = arg0;
	}
	
	public String createToken() {
		if (scope != null) {
			try {
				Date validityDate = DateUtils.afterOneHundredYearsFromNow();
				String key = TokenUtils.generateToken();
				Token token = new Token(key, validityDate, scope, user);
				TokenPersistenceUtils tokenPersistenceUtils = (TokenPersistenceUtils) ServiceLocator.getInstance().getService(
						"java:global/ROOT/TokenPersistenceUtils!net.gmsworld.server.utils.persistence.TokenPersistenceUtils");
			    tokenPersistenceUtils.save(token);
				request.setAttribute(JSonDataAction.JSON_OUTPUT, token);
				return "json";
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				addActionError(e.getMessage());
		    	return ERROR;
			}
		} else {
			addActionError("Missing required parameter!");
	    	return ERROR;
		}
	}
	
	public String isValidToken() {
		if (key != null && scope != null) {
			boolean isValid = false;
			try {
				TokenPersistenceUtils tokenPersistenceUtils = (TokenPersistenceUtils) ServiceLocator.getInstance().getService(		
					"java:global/ROOT/TokenPersistenceUtils!net.gmsworld.server.utils.persistence.TokenPersistenceUtils");
				isValid = tokenPersistenceUtils.isTokenValid(key, scope);
				request.setAttribute(JSonDataAction.JSON_OUTPUT, isValid);
		 		return "json";
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				addActionError(e.getMessage());
		    	return ERROR;
			}			
		} else {
			addActionError("Missing required parameter!");
	    	return ERROR;
		}
	}
	
	public String getTopTokens() {
		try {
			if (limit == null) {
				limit = 10;
			}
			TokenPersistenceUtils tokenPersistenceUtils = (TokenPersistenceUtils) ServiceLocator.getInstance().getService(		
				"java:global/ROOT/TokenPersistenceUtils!net.gmsworld.server.utils.persistence.TokenPersistenceUtils");
			List<Token> tokens = tokenPersistenceUtils.getTopTokens(limit);
			request.setAttribute(JSonDataAction.JSON_OUTPUT, tokens);
	 		return "json";
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			addActionError(e.getMessage());
	    	return ERROR;
		}
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
