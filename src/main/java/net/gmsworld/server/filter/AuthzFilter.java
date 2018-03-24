package net.gmsworld.server.filter;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.utils.ServiceLocator;
import net.gmsworld.server.utils.persistence.TokenPersistenceUtils;

/**
 * Servlet Filter implementation class AuthzFilter
 */
public class AuthzFilter implements Filter {

	private static final Logger logger = Logger.getLogger(AuthzFilter.class.getName());
    /**
     * Default constructor. 
     */
    public AuthzFilter() {
    }

	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		boolean auth = false;
		if (request instanceof HttpServletRequest) {
			HttpServletRequest httpRequest = (HttpServletRequest) request;
			String authHeader = httpRequest.getHeader(Commons.TOKEN_HEADER);
    		String scope = httpRequest.getHeader(Commons.SCOPE_HEADER);
    		if (authHeader != null && scope != null) {
    			try {
    				TokenPersistenceUtils tokenPersistenceUtils = (TokenPersistenceUtils) ServiceLocator.getInstance().getService("bean/TokenPersistenceUtils");
    				auth = tokenPersistenceUtils.isTokenValid(authHeader, scope);
    			} catch (Exception e) {
        			logger.log(Level.SEVERE, e.getMessage(), e);
        		}
    		} else {
    			logger.log(Level.WARNING, "Missing token or scope header");
    		}
		}
    	
    	if (auth) {
            chain.doFilter(request, response);
        } else if (response instanceof HttpServletResponse) {
        	logger.log(Level.WARNING, "Authorizaton Failed!");
        	((HttpServletResponse)response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authorizaton Required!");
        } else {
        	response.getWriter().println("Authorizaton Required!");
        }
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
	}

}
