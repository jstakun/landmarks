package net.gmsworld.server.struts;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import net.gmsworld.server.persistence.Token;
import net.gmsworld.server.utils.TokenUtils;
import net.gmsworld.server.utils.DateUtils;
import net.gmsworld.server.utils.NumberUtils;
import net.gmsworld.server.utils.ServiceLocator;
import net.gmsworld.server.utils.persistence.TokenPersistenceUtils;

import org.apache.struts2.interceptor.ParameterAware;
import org.apache.struts2.interceptor.ServletRequestAware;

import com.opensymphony.xwork2.ActionSupport;

public class TokenAction extends ActionSupport implements ParameterAware, ServletRequestAware {
	
	private static final Logger logger = Logger.getLogger(TokenAction.class.getName());
	private Map<String, String[]> parameters;
	private HttpServletRequest request;
	private static final long serialVersionUID = 1L;

	@Override
	public void setParameters(Map<String, String[]> arg0) {
		this.parameters = arg0;		
	}
	
	@Override
	public void setServletRequest(HttpServletRequest request) {
		this.request = request;
	}
	
	private String getParameter(String key) {
		if (parameters.containsKey(key)) {
			return parameters.get(key)[0];
		} else {
			return null;
		}
	}
	
	public String createToken() {
		if (getParameter("scope") != null) {
			try {
				String scope = getParameter("scope");
				String user = getParameter("user");
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
		if (getParameter("key") != null && getParameter("scope") != null) {
			boolean isValid = false;
			try {
				TokenPersistenceUtils tokenPersistenceUtils = (TokenPersistenceUtils) ServiceLocator.getInstance().getService(		
					"java:global/ROOT/TokenPersistenceUtils!net.gmsworld.server.utils.persistence.TokenPersistenceUtils");
				isValid = tokenPersistenceUtils.isTokenValid(getParameter("key"), getParameter("scope"));
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
			int limit = NumberUtils.getInt(getParameter("limit"), 10);		
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
}
