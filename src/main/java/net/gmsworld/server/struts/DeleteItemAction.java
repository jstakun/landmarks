package net.gmsworld.server.struts;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.interceptor.ServletRequestAware;

import com.opensymphony.xwork2.ActionSupport;

import net.gmsworld.server.persistence.User;
import net.gmsworld.server.utils.ServiceLocator;
import net.gmsworld.server.utils.memcache.CacheUtil;
import net.gmsworld.server.utils.persistence.EMF;
import net.gmsworld.server.utils.persistence.GeocodePersistenceUtils;
import net.gmsworld.server.utils.persistence.LandmarkPersistenceUtils;
import net.gmsworld.server.utils.persistence.NotificationPersistenceUtils;
import net.gmsworld.server.utils.persistence.UserPersistenceUtils;

public class DeleteItemAction extends ActionSupport implements ServletRequestAware {

	private static final Logger logger = Logger.getLogger(DeleteItemAction.class.getName());
	private static final long serialVersionUID = 1L;
	
	private String type;
	private String secret;
	private String id;
	
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
		if (StringUtils.equals(getType(), "user") && StringUtils.isNotEmpty(getSecret())) {
			return executeUser(); 
	    } else if (StringUtils.equals(getType(), "notification") && StringUtils.isNotEmpty(getId())) {
			return executeNotification(); 
	    } else if (StringUtils.equals(getType(), "landmark") && StringUtils.isNumeric(getId())) {
	    	return executeLandmark();	
		} else if (StringUtils.equals(getType(), "geocode") && StringUtils.isNumeric(getId())) {
	    	return executeGeocode();	
		} else {
			addActionError("Missing or wrong required parameter type!");
            return ERROR;
		}
	}
	
	private String executeUser() {    
		EntityManager em = EMF.getEntityManager();
        try {
			UserPersistenceUtils userPeristenceUtils = (UserPersistenceUtils) ServiceLocator.getInstance().getService("bean/UserPersistenceUtils");
			User u = userPeristenceUtils.findBySecret(getSecret(), em);
			if (u == null) {
				request.setAttribute("output", "{\"error\":\"User not found!\"}");
				ServletActionContext.getResponse().setStatus(404);
			} else {
				userPeristenceUtils.remove(u, em);
				request.setAttribute("output", "{\"status\":\"ok\"}");    	
			}
        } catch (Exception e) {
			request.setAttribute("output", "{\"error\":\"" + e.getMessage() + "\"}");
			logger.log(Level.SEVERE, e.getMessage(), e);
			ServletActionContext.getResponse().setStatus(500);
		} finally {
			em.close();
		}
        return SUCCESS;
	}
	
	private String executeNotification() {    
		EntityManager em = EMF.getEntityManager();
        try {
			NotificationPersistenceUtils notificationPeristenceUtils = (NotificationPersistenceUtils) ServiceLocator.getInstance().getService("bean/NotificationPersistenceUtils");
			if (notificationPeristenceUtils.remove(getId(), em)) {
				request.setAttribute("output", "{\"status\":\"ok\"}");    	
			} else {
				request.setAttribute("output", "{\"error\":\"Notification not found!\"}");
				ServletActionContext.getResponse().setStatus(404);
			}
        } catch (Exception e) {
			request.setAttribute("output", "{\"error\":\"" + e.getMessage() + "\"}");
			logger.log(Level.SEVERE, e.getMessage(), e);
			ServletActionContext.getResponse().setStatus(500);
		} finally {
			em.close();
		}
        return SUCCESS;
	}
	
	private String executeLandmark() {    
		EntityManager em = EMF.getEntityManager();
        try {
			LandmarkPersistenceUtils landmarkPeristenceUtils = (LandmarkPersistenceUtils) ServiceLocator.getInstance().getService("bean/LandmarkPersistenceUtils");
			if (landmarkPeristenceUtils.remove(Integer.valueOf(getId()), em)) {
				//invalidate NewestLandmarks
        		CacheUtil.removeAll(LandmarkProviderAction.NEWEST_LANDMARKS, 1, LandmarkProviderAction.MAX_LANDMARKS);
				request.setAttribute("output", "{\"status\":\"ok\"}");    	
			} else {
				request.setAttribute("output", "{\"error\":\"Landmark not found!\"}");
				ServletActionContext.getResponse().setStatus(404);
			}
        } catch (Exception e) {
			request.setAttribute("output", "{\"error\":\"" + e.getMessage() + "\"}");
			logger.log(Level.SEVERE, e.getMessage(), e);
			ServletActionContext.getResponse().setStatus(500);
		} finally {
			em.close();
		}
        return SUCCESS;
	}

	private String executeGeocode() {    
		EntityManager em = EMF.getEntityManager();
        try {
			GeocodePersistenceUtils geocodePeristenceUtils = (GeocodePersistenceUtils) ServiceLocator.getInstance().getService("bean/LandmarkPersistenceUtils");
			if (geocodePeristenceUtils.remove(Integer.valueOf(getId()), em)) {
				//invalidate NewestGeocodes
        		CacheUtil.removeAll(ItemProviderAction.NEWEST_GEOCODES, 1, ItemProviderAction.MAX_ITEMS);
				request.setAttribute("output", "{\"status\":\"ok\"}");    	
			} else {
				request.setAttribute("output", "{\"error\":\"Geocode not found!\"}");
				ServletActionContext.getResponse().setStatus(404);
			}
        } catch (Exception e) {
			request.setAttribute("output", "{\"error\":\"" + e.getMessage() + "\"}");
			logger.log(Level.SEVERE, e.getMessage(), e);
			ServletActionContext.getResponse().setStatus(500);
		} finally {
			em.close();
		}
        return SUCCESS;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}	
}
