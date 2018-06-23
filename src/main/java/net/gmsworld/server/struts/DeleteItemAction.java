package net.gmsworld.server.struts;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.interceptor.ServletRequestAware;

import com.opensymphony.xwork2.ActionSupport;

import net.gmsworld.server.persistence.User;
import net.gmsworld.server.utils.ServiceLocator;
import net.gmsworld.server.utils.persistence.EMF;
import net.gmsworld.server.utils.persistence.UserPersistenceUtils;

public class DeleteItemAction extends ActionSupport implements ServletRequestAware {

	private static final Logger logger = Logger.getLogger(DeleteItemAction.class.getName());
	private static final long serialVersionUID = 1L;
	
	private String type;
	private String secret;
	
	private HttpServletRequest request;
	
	@Override
	public void setServletRequest(HttpServletRequest request) {
		this.request = request;
	}
	
	public DeleteItemAction() {
		super();
	}
	
	public String execute()
	{	
		if (StringUtils.equals(type, "user") && StringUtils.isNotEmpty(secret)) {
			return executeUser(); 
	    } else {
			addActionError("Missing or wrong required parameter type!");
            return ERROR;
		}
	}
	
	private String executeUser() {
		    
			EntityManager em = EMF.getEntityManager();
            try {
				UserPersistenceUtils userPeristenceUtils = (UserPersistenceUtils) ServiceLocator.getInstance().getService("bean/UserPersistenceUtils");
				User u = userPeristenceUtils.findBySecret(secret, em);
				if (u == null) {
					request.setAttribute("output", "{\"error\":\"User not found!\"}");
				} else {
					userPeristenceUtils.remove(u, em);
					request.setAttribute("output", "{\"status\":\"ok\"}");    	
				}
            } catch (Exception e) {
				request.setAttribute("output", "{\"error\":\"" + e.getMessage() + "\"}");
				logger.log(Level.SEVERE, e.getMessage(), e);
			} finally {
				em.close();
			}
            return SUCCESS;
		}	
}
