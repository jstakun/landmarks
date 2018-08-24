package net.gmsworld.server.struts;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import net.gmsworld.server.persistence.Checkin;
import net.gmsworld.server.persistence.Comment;
import net.gmsworld.server.persistence.Geocode;
import net.gmsworld.server.persistence.Layer;
import net.gmsworld.server.persistence.Notification;
import net.gmsworld.server.persistence.Screenshot;
import net.gmsworld.server.persistence.User;
import net.gmsworld.server.utils.NumberUtils;
import net.gmsworld.server.utils.ServiceLocator;
import net.gmsworld.server.utils.memcache.CacheUtil;
import net.gmsworld.server.utils.persistence.CheckinPersistenceUtils;
import net.gmsworld.server.utils.persistence.CommentPersistenceUtils;
import net.gmsworld.server.utils.persistence.EMF;
import net.gmsworld.server.utils.persistence.GeocodePersistenceUtils;
import net.gmsworld.server.utils.persistence.LayerPersistenceUtils;
import net.gmsworld.server.utils.persistence.NotificationPersistenceUtils;
import net.gmsworld.server.utils.persistence.ScreenshotPersistenceUtils;
import net.gmsworld.server.utils.persistence.UserPersistenceUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.interceptor.ParameterAware;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.json.JSONUtil;

import com.opensymphony.xwork2.ActionSupport;

public class ItemProviderAction extends ActionSupport implements ParameterAware, ServletRequestAware {

	private Logger logger = Logger.getLogger(getClass().getName());
	private static final long serialVersionUID = 1L;
	private Map<String, String[]> parameters;
	private HttpServletRequest request;
	
	@Override
	public void setParameters(Map<String, String[]> arg0) {
		this.parameters = arg0;		
	}
	
	private String getParameter(String key) {
		if (parameters.containsKey(key)) {
			return parameters.get(key)[0];
		} else {
			return null;
		}
	}
	
	@Override
	public void setServletRequest(HttpServletRequest request) {
		this.request = request;
		
	}
	
	public String execute()
	{
		String type = getParameter("type");
		if (StringUtils.equals(type, "landmark")) {
			addActionError("Please use /landmarkProvider instread!");
            return ERROR;
		} else if (StringUtils.equals(type, "geocode")) {
			return executeGeocode();
		} else if (StringUtils.equals(type, "checkin")) {
			return executeCheckin();
		} else if (StringUtils.equals(type, "comment")) {
			return executeComment();
		} else if (StringUtils.equals(type, "layer")) {
			return executeLayer(); 
	    } else if (StringUtils.equals(type, "screenshot")) {
			return executeScreenshot(); 
	    } else if (StringUtils.equals(type, "user")) {
			return executeUser(); 
	    } else if (StringUtils.equals(type, "notification")) {
			return executeNotification(); 
	    } else {
			addActionError("Missing or wrong required parameter type!");
            return ERROR;
		}
	}
	
	private String executeGeocode() {
		if (getParameter("address") != null) {
	    	return findByAddressGeocode(getParameter("address"));
		} else if (getParameter("lat") != null && getParameter("lng") != null) {
		    return findByCoordsGeocode(NumberUtils.getDouble(getParameter("lat")), NumberUtils.getDouble(getParameter("lng")));
		} else if (getParameter("limit") != null) {
	    	int limit = NumberUtils.getInt(getParameter("limit"), 10);
	    	return findNewestGeocodes(limit);
	    } else if (getParameter("id") != null) {
	    	return findByIdGeocode(NumberUtils.getInt(getParameter("id"),-1));
	    } else { 
	    	addActionError("Missing required geocode parameter!");
	    	return ERROR;
	    }
	}
	
	private String findNewestGeocodes(int limit) {
		final String key = "NewestGeocodes";
		String output = null;
		long startTime = System.currentTimeMillis();
    	List<Geocode> newest = CacheUtil.getList(Geocode.class, key);
    	EntityManager em = null;
    	try {
    		if (newest == null) {
    			em = EMF.getEntityManager();
    	        GeocodePersistenceUtils geocodePeristenceUtils = (GeocodePersistenceUtils) ServiceLocator.getInstance().getService("bean/GeocodePersistenceUtils");
    			newest = geocodePeristenceUtils.findNewest(limit, em);	
    		    if (newest != null && !newest.isEmpty()) {
    		    	CacheUtil.putShort(key, newest);
    		    }
    		}
    		output = JSONUtil.serialize(newest);
        } catch (Exception e) {
        	output = "{\"error\":\"" + e.getMessage() + "\"}";
			logger.log(Level.SEVERE, e.getMessage(), e);
		} finally {
			if (em!= null) {
				em.close();
			}
		}
    	logger.log(Level.INFO, "Found " + (newest == null ? 0 : newest.size()) + " geocodes in " + (System.currentTimeMillis() - startTime) + " millis");
		request.setAttribute("output", output);
		return SUCCESS;
	}
	
	private String findByIdGeocode(int id) {
		String output = null;
		EntityManager em = EMF.getEntityManager();
    	try {
    		GeocodePersistenceUtils geocodePeristenceUtils = (GeocodePersistenceUtils) ServiceLocator.getInstance().getService("bean/GeocodePersistenceUtils");
    		Geocode g = geocodePeristenceUtils.findById(id, em);
    		if (g != null) {
    			output = JSONUtil.serialize(g);
    		} else {   	
    			logger.log(Level.INFO, "No geocode found wih id: " + id);
    			output = "{}";
    		}  		
    	} catch (Exception e) {
        	output = "{\"error\":\"" + e.getMessage() + "\"}";
			logger.log(Level.SEVERE, e.getMessage(), e);
		} finally {
			em.close();
		}
    	request.setAttribute("output", output);
    	return SUCCESS;
	}
	
	private String findByAddressGeocode(String address) {
		String output = null;
		EntityManager em = EMF.getEntityManager();
    	try {
    		GeocodePersistenceUtils geocodePeristenceUtils = (GeocodePersistenceUtils) ServiceLocator.getInstance().getService("bean/GeocodePersistenceUtils");
    		Geocode g = geocodePeristenceUtils.findByAddress(address, em);
    		if (g != null) {
    			output = JSONUtil.serialize(g);
    		} else {
    			logger.log(Level.INFO, "No geocode found for address: " + address);
    			output = "{}";
    		}
    	} catch (Exception e) {
        	output = "{\"error\":\"" + e.getMessage() + "\"}";
			logger.log(Level.SEVERE, e.getMessage(), e);
		} finally {
			em.close();
		}
    	request.setAttribute("output", output);
    	return SUCCESS;
	}
	
	private String findByCoordsGeocode(double lat, double lng) {
		String output = null;
		EntityManager em = EMF.getEntityManager();
    	try {
    		GeocodePersistenceUtils geocodePeristenceUtils = (GeocodePersistenceUtils) ServiceLocator.getInstance().getService("bean/GeocodePersistenceUtils");
    		Geocode g = geocodePeristenceUtils.findByCoords(lat, lng, em);
    		if (g != null) {
    			output = JSONUtil.serialize(g);
    		} else {
    			logger.log(Level.INFO, "No geocode found for coords: " + lat + "," + lng);
    			output = "{}";
    		}
    	} catch (Exception e) {
        	output = "{\"error\":\"" + e.getMessage() + "\"}";
			logger.log(Level.SEVERE, e.getMessage(), e);
		} finally {
			em.close();
		}
    	request.setAttribute("output", output);
    	return SUCCESS;
	}

	private String executeComment() {
		if (getParameter("landmarkId") != null) {
			int landmarkId = NumberUtils.getInt(getParameter("landmarkId"), -1);
			EntityManager em = EMF.getEntityManager();
			String output = null;
			try {
            	CommentPersistenceUtils commentPeristenceUtils = (CommentPersistenceUtils) ServiceLocator.getInstance().getService("bean/CommentPersistenceUtils");
				List<Comment> comments = commentPeristenceUtils.findByLandmark(landmarkId, em);
				output = JSONUtil.serialize(comments);
			} catch (Exception e) {
            	output = "{\"error\":\"" + e.getMessage() + "\"}";
				logger.log(Level.SEVERE, e.getMessage(), e);
			} finally {
				em.close();
			}
            request.setAttribute("output", output);
            return SUCCESS;
		} else {
			addActionError("Missing required comment parameter landmarkId!");
	    	return ERROR;
		}
	}
	
	private String executeCheckin() {
		if (getParameter("landmarkId") != null) {
			int landmarkId = NumberUtils.getInt(getParameter("landmarkId"), -1);
			EntityManager em = EMF.getEntityManager();
			String output = null;
			try {
            	CheckinPersistenceUtils checkinPeristenceUtils = (CheckinPersistenceUtils) ServiceLocator.getInstance().getService("bean/CheckinPersistenceUtils");
				List<Checkin> checkins = checkinPeristenceUtils.findByLandmark(landmarkId, em);
				output = JSONUtil.serialize(checkins);
			} catch (Exception e) {
            	output = "{\"error\":\"" + e.getMessage() + "\"}";
				logger.log(Level.SEVERE, e.getMessage(), e);
			} finally {
				em.close();
			}
            request.setAttribute("output", output);
            return SUCCESS;
		} else {
			addActionError("Missing required checkin parameter landmarkId!");
	    	return ERROR;
		}
	}
	
	private String executeLayer() {
		String output = null;
		EntityManager em = EMF.getEntityManager();
		try {
			LayerPersistenceUtils layerPeristenceUtils = (LayerPersistenceUtils) ServiceLocator.getInstance().getService("bean/LayerPersistenceUtils");
			List<Layer> layers = layerPeristenceUtils.findAll(em);
			output = JSONUtil.serialize(layers);
		} catch (Exception e) {
        	output = "{\"error\":\"" + e.getMessage() + "\"}";
			logger.log(Level.SEVERE, e.getMessage(), e);
		} finally {
			em.close();
		}
        request.setAttribute("output", output);
        return SUCCESS;		
	}
	
	private String executeScreenshot() {
		int id = NumberUtils.getInt(getParameter("id"),-1);
		int ndays = NumberUtils.getInt(getParameter("ndays"), -1);
		String action = getParameter("action");
		if (id >= 0) {
			if (StringUtils.equalsIgnoreCase(action, "remove")) {
				return removeScreenshot(id);
			} else {
				return findByIdScreenshot(id);
			}
	    } else if (ndays >= 0) {
	    	return findOlder(ndays);
	    } else {	
	    	addActionError("Missing required screenshot parameter!");
	    	return ERROR;
	    }
	}
	
	private String executeUser() {
		String id = getParameter("login");
		String pwd = getParameter("password");
		String secret = getParameter("secret");
		boolean confirm = (NumberUtils.getInt(getParameter("confirm"), 0)==1);
		
		if (StringUtils.isNotEmpty(id) && StringUtils.isNotEmpty(pwd)) {
			return login(id, pwd);
		} else if (StringUtils.isNotEmpty(id)) {
	    	return findByIdUser(id, confirm);
		} else if (StringUtils.isNotEmpty(secret)) {
	    	return findBySecretUser(secret);	
	    } else { 
	    	addActionError("Missing required user parameter!");
	    	return ERROR;
	    }
	}
	
	private String executeNotification() {
		String response = "";
		String id = getParameter("id");
		String secret = getParameter("secret");
		String statusStr = getParameter("status");
		EntityManager em = EMF.getEntityManager();
		try {
			NotificationPersistenceUtils notificationPeristenceUtils = (NotificationPersistenceUtils) ServiceLocator.getInstance().getService("bean/NotificationPersistenceUtils");
		
			if (StringUtils.isNotEmpty(id)) {
				response =  JSONUtil.serialize(notificationPeristenceUtils.findById(id, em));
			} else if (StringUtils.isNotEmpty(id)) {
				response = JSONUtil.serialize(notificationPeristenceUtils.findBySecret(secret, em));
			} else if (StringUtils.equals(statusStr, "1") || StringUtils.equalsIgnoreCase(statusStr, "true")) {
				response = 	JSONUtil.serialize(notificationPeristenceUtils.findByStatus(Notification.Status.VERIFIED, em));
			} else if (StringUtils.equals(statusStr, "0") || StringUtils.equalsIgnoreCase(statusStr, "false")) {
				response = 	JSONUtil.serialize(notificationPeristenceUtils.findByStatus(Notification.Status.UNVERIFIED, em));
			} else { 
				addActionError("Missing required notification parameter!");
				response = ERROR;
			}
		} catch (Exception e) {
			addActionError("Failed to execute action!");
	    	logger.log(Level.SEVERE, e.getMessage(), e);
			response = ERROR;
		} finally {
			em.close();
		}
		return response;
	}
	
	private String findByIdScreenshot(int id) {
		String output = null;
		EntityManager em = EMF.getEntityManager();
		try {
    		ScreenshotPersistenceUtils screenshotPeristenceUtils = (ScreenshotPersistenceUtils) ServiceLocator.getInstance().getService("bean/ScreenshotPersistenceUtils");
    		Screenshot s = screenshotPeristenceUtils.findById(id, em);
    		if (s != null) {
    			output = JSONUtil.serialize(s);
    		} else {   	
    			logger.log(Level.INFO, "No screenshot found with id: " + id);
    			output = "{}";
    		}  		
    	} catch (Exception e) {
        	output = "{\"error\":\"" + e.getMessage() + "\"}";
			logger.log(Level.SEVERE, e.getMessage(), e);
		} finally {
			em.close();
		}
    	request.setAttribute("output", output);
    	return SUCCESS;
	}
	
	private String removeScreenshot(int id) {
		String output = null;
		EntityManager em = EMF.getEntityManager();
		try {
    		ScreenshotPersistenceUtils screenshotPeristenceUtils = (ScreenshotPersistenceUtils) ServiceLocator.getInstance().getService("bean/ScreenshotPersistenceUtils");
    		boolean deleted = screenshotPeristenceUtils.delete(id, em);
    		if (deleted) {
    			output = "{\"message\": \"screenshot deleted\"}";
    		} else {   	
    			String msg = "No screenshot found with id: " + id;
    			output = "{\"error\":\"" + msg + "\"}";
    			logger.log(Level.INFO, msg);
    		}  		
    	} catch (Exception e) {
        	output = "{\"error\":\"" + e.getMessage() + "\"}";
			logger.log(Level.SEVERE, e.getMessage(), e);
		} finally {
			em.close();
		}
    	request.setAttribute("output", output);
    	return SUCCESS;
	}
	
	private String findOlder(int ndays) {
		String output = null;
		EntityManager em = EMF.getEntityManager();
    	try {
    		ScreenshotPersistenceUtils screenshotPeristenceUtils = (ScreenshotPersistenceUtils) ServiceLocator.getInstance().getService("bean/ScreenshotPersistenceUtils");
    	    List<Screenshot> s = screenshotPeristenceUtils.findOlder(ndays, 100, em);
    	    if (s != null) {
    	    	output = JSONUtil.serialize(s, null, null, true, true);
    	    	logger.log(Level.INFO, "Found " + s.size() + " screenshots older than " + ndays + " days");
    	    } else {   	
    			logger.log(Level.INFO, "No screenshots older than " + ndays + " days found");
    			output = "{}";
    		}  	
    	} catch (Exception e) {
        	output = "{\"error\":\"" + e.getMessage() + "\"}";
			logger.log(Level.SEVERE, e.getMessage(), e);
		} finally {
			em.close();
		}
    	request.setAttribute("output", output);
    	return SUCCESS;	
	}
	
	private String findByIdUser(String id, boolean confirm) {
		String output = null;
		EntityManager em = EMF.getEntityManager();
    	try {
    		UserPersistenceUtils userPeristenceUtils = (UserPersistenceUtils) ServiceLocator.getInstance().getService("bean/UserPersistenceUtils");
    		User u = userPeristenceUtils.findById(id, em);
    		if (u != null) {
    			userPeristenceUtils.setLastLogonDate(id, em);
    			if (confirm) {
    				userPeristenceUtils.setConfirmation(id, em);
    			}
    			output = JSONUtil.serialize(u);
    		} else {   	
    			logger.log(Level.INFO, "No user found wih id: " + id);
    			output = "{}";
    		}  		
    	} catch (Exception e) {
        	output = "{\"error\":\"" + e.getMessage() + "\"}";
			logger.log(Level.SEVERE, e.getMessage(), e);
		} finally {
			em.close();
		}
    	request.setAttribute("output", output);
    	return SUCCESS;
	}
	
	private String findBySecretUser(String secret) {
		String output = null;
		EntityManager em = EMF.getEntityManager();
    	try {
    		UserPersistenceUtils userPeristenceUtils = (UserPersistenceUtils) ServiceLocator.getInstance().getService("bean/UserPersistenceUtils");
    		User u = userPeristenceUtils.findBySecret(secret, em);
    		if (u != null) {
    			output = JSONUtil.serialize(u);
    		} else {   	
    			logger.log(Level.INFO, "No user found wih provided secret!");
    			output = "{}";
    		}  		
    	} catch (Exception e) {
        	output = "{\"error\":\"" + e.getMessage() + "\"}";
			logger.log(Level.SEVERE, e.getMessage(), e);
		} finally {
			em.close();
		}
    	request.setAttribute("output", output);
    	return SUCCESS;
	}
	
	private String login(String login, String password) {
		String output = null;
		EntityManager em = EMF.getEntityManager();
    	try {
    		UserPersistenceUtils userPeristenceUtils = (UserPersistenceUtils) ServiceLocator.getInstance().getService("bean/UserPersistenceUtils");
    		boolean auth = userPeristenceUtils.login(login, password, em);
    		output = "{\"auth\": " + auth + "}";
    	} catch (Exception e) {
        	output = "{\"error\":\"" + e.getMessage() + "\"}";
			logger.log(Level.SEVERE, e.getMessage(), e);
		} finally {
			em.close();
		}
    	request.setAttribute("output", output);
    	return SUCCESS;
	}
}
