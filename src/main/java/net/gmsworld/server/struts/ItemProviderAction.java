package net.gmsworld.server.struts;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import net.gmsworld.server.persistence.Checkin;
import net.gmsworld.server.persistence.Comment;
import net.gmsworld.server.persistence.Geocode;
import net.gmsworld.server.persistence.Layer;
import net.gmsworld.server.persistence.Screenshot;
import net.gmsworld.server.persistence.User;
import net.gmsworld.server.utils.NumberUtils;
import net.gmsworld.server.utils.ServiceLocator;
import net.gmsworld.server.utils.memcache.CacheUtil;
import net.gmsworld.server.utils.persistence.CheckinPersistenceUtils;
import net.gmsworld.server.utils.persistence.CommentPersistenceUtils;
import net.gmsworld.server.utils.persistence.GeocodePersistenceUtils;
import net.gmsworld.server.utils.persistence.LayerPersistenceUtils;
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
	    } else {
			addActionError("Missing or wrong required parameter type!");
            return ERROR;
		}
	}
	
	private String executeGeocode() {
		if (getParameter("address") != null) {
	    	return findByAddressGeocode(getParameter("address"));
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
    	try {
    		if (newest == null) {
    			GeocodePersistenceUtils geocodePeristenceUtils = (GeocodePersistenceUtils) ServiceLocator.getInstance().getService("bean/GeocodePersistenceUtils");
    			newest = geocodePeristenceUtils.findNewest(limit);	
    		    if (newest != null && !newest.isEmpty()) {
    		    	CacheUtil.putShort(key, newest);
    		    }
    		}
    		output = JSONUtil.serialize(newest);
        } catch (Exception e) {
        	output = "{\"error\":\"" + e.getMessage() + "\"}";
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
    	logger.log(Level.INFO, "Found " + (newest == null ? 0 : newest.size()) + " geocodes in " + (System.currentTimeMillis() - startTime) + " millis");
		request.setAttribute("output", output);
		return SUCCESS;
	}
	
	private String findByIdGeocode(int id) {
		String output = null;
    	try {
    		GeocodePersistenceUtils geocodePeristenceUtils = (GeocodePersistenceUtils) ServiceLocator.getInstance().getService("bean/GeocodePersistenceUtils");
    		Geocode g = geocodePeristenceUtils.findById(id);
    		if (g != null) {
    			output = JSONUtil.serialize(g);
    		} else {   	
    			logger.log(Level.INFO, "No geocode found wih id: " + id);
    			output = "{}";
    		}  		
    	} catch (Exception e) {
        	output = "{\"error\":\"" + e.getMessage() + "\"}";
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
    	request.setAttribute("output", output);
    	return SUCCESS;
	}
	
	private String findByAddressGeocode(String address) {
		String output = null;
    	try {
    		GeocodePersistenceUtils geocodePeristenceUtils = (GeocodePersistenceUtils) ServiceLocator.getInstance().getService("bean/GeocodePersistenceUtils");
    		Geocode g = geocodePeristenceUtils.findAddress(address);
    		if (g != null) {
    			output = JSONUtil.serialize(g);
    		} else {
    			logger.log(Level.INFO, "No geocode found for address: " + address);
    			output = "{}";
    		}
    	} catch (Exception e) {
        	output = "{\"error\":\"" + e.getMessage() + "\"}";
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
    	request.setAttribute("output", output);
    	return SUCCESS;
	}

	private String executeComment() {
		if (getParameter("landmarkId") != null) {
			int landmarkId = NumberUtils.getInt(getParameter("landmarkId"), -1);
			String output = null;
			try {
            	CommentPersistenceUtils commentPeristenceUtils = (CommentPersistenceUtils) ServiceLocator.getInstance().getService("bean/CommentPersistenceUtils");
				List<Comment> comments = commentPeristenceUtils.findByLandmark(landmarkId);
				output = JSONUtil.serialize(comments);
			} catch (Exception e) {
            	output = "{\"error\":\"" + e.getMessage() + "\"}";
				logger.log(Level.SEVERE, e.getMessage(), e);
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
			String output = null;
			try {
            	CheckinPersistenceUtils checkinPeristenceUtils = (CheckinPersistenceUtils) ServiceLocator.getInstance().getService("bean/CheckinPersistenceUtils");
				List<Checkin> checkins = checkinPeristenceUtils.findByLandmark(landmarkId);
				output = JSONUtil.serialize(checkins);
			} catch (Exception e) {
            	output = "{\"error\":\"" + e.getMessage() + "\"}";
				logger.log(Level.SEVERE, e.getMessage(), e);
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
		try {
			LayerPersistenceUtils layerPeristenceUtils = (LayerPersistenceUtils) ServiceLocator.getInstance().getService("bean/LayerPersistenceUtils");
			List<Layer> layers = layerPeristenceUtils.findAll();
			output = JSONUtil.serialize(layers);
		} catch (Exception e) {
        	output = "{\"error\":\"" + e.getMessage() + "\"}";
			logger.log(Level.SEVERE, e.getMessage(), e);
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
		boolean confirm = (NumberUtils.getInt(getParameter("confirm"), 0)==1);
		
		if (StringUtils.isNotEmpty(id) && StringUtils.isNotEmpty(pwd)) {
			return login(id, pwd);
		} else if (StringUtils.isNotEmpty(id)) {
	    	return findByIdUser(id, confirm);
	    } else { 
	    	addActionError("Missing required user parameter!");
	    	return ERROR;
	    }
	}
	
	private String findByIdScreenshot(int id) {
		String output = null;
    	try {
    		ScreenshotPersistenceUtils screenshotPeristenceUtils = (ScreenshotPersistenceUtils) ServiceLocator.getInstance().getService("bean/ScreenshotPersistenceUtils");
    		Screenshot s = screenshotPeristenceUtils.findById(id);
    		if (s != null) {
    			output = JSONUtil.serialize(s);
    		} else {   	
    			logger.log(Level.INFO, "No screenshot found with id: " + id);
    			output = "{}";
    		}  		
    	} catch (Exception e) {
        	output = "{\"error\":\"" + e.getMessage() + "\"}";
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
    	request.setAttribute("output", output);
    	return SUCCESS;
	}
	
	private String removeScreenshot(int id) {
		String output = null;
    	try {
    		ScreenshotPersistenceUtils screenshotPeristenceUtils = (ScreenshotPersistenceUtils) ServiceLocator.getInstance().getService("bean/ScreenshotPersistenceUtils");
    		boolean deleted = screenshotPeristenceUtils.delete(id);
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
		}
    	request.setAttribute("output", output);
    	return SUCCESS;
	}
	
	private String findOlder(int ndays) {
		String output = null;
    	try {
    		ScreenshotPersistenceUtils screenshotPeristenceUtils = (ScreenshotPersistenceUtils) ServiceLocator.getInstance().getService("bean/ScreenshotPersistenceUtils");
    	    List<Screenshot> s = screenshotPeristenceUtils.findOlder(ndays, 100);
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
		}
    	request.setAttribute("output", output);
    	return SUCCESS;	
	}
	
	private String findByIdUser(String id, boolean confirm) {
		String output = null;
    	try {
    		UserPersistenceUtils userPeristenceUtils = (UserPersistenceUtils) ServiceLocator.getInstance().getService("bean/UserPersistenceUtils");
    		User u = userPeristenceUtils.findById(id);
    		if (u != null) {
    			userPeristenceUtils.setLastLogonDate(id);
    			if (confirm) {
    				userPeristenceUtils.setConfirmation(id);
    			}
    			output = JSONUtil.serialize(u);
    		} else {   	
    			logger.log(Level.INFO, "No user found wih id: " + id);
    			output = "{}";
    		}  		
    	} catch (Exception e) {
        	output = "{\"error\":\"" + e.getMessage() + "\"}";
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
    	request.setAttribute("output", output);
    	return SUCCESS;
	}
	
	private String login(String login, String password) {
		String output = null;
    	try {
    		UserPersistenceUtils userPeristenceUtils = (UserPersistenceUtils) ServiceLocator.getInstance().getService("bean/UserPersistenceUtils");
    		boolean auth = userPeristenceUtils.login(login, password);
    		output = "{\"auth\": " + auth + "}";
    	} catch (Exception e) {
        	output = "{\"error\":\"" + e.getMessage() + "\"}";
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
    	request.setAttribute("output", output);
    	return SUCCESS;
	}
}
