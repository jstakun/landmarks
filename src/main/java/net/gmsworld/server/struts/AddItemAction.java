package net.gmsworld.server.struts;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import net.gmsworld.server.config.ConfigurationManager;
import net.gmsworld.server.layers.GeocodeHelperFactory;
import net.gmsworld.server.persistence.Checkin;
import net.gmsworld.server.persistence.Comment;
import net.gmsworld.server.persistence.Geocode;
import net.gmsworld.server.persistence.Landmark;
import net.gmsworld.server.persistence.Layer;
import net.gmsworld.server.persistence.Notification;
import net.gmsworld.server.persistence.Screenshot;
import net.gmsworld.server.persistence.User;
import net.gmsworld.server.utils.DateUtils;
import net.gmsworld.server.utils.GeocodeUtils;
import net.gmsworld.server.utils.TomcatThreadProvider;
import net.gmsworld.server.utils.NumberUtils;
import net.gmsworld.server.utils.ServiceLocator;
import net.gmsworld.server.utils.StringUtil;
import net.gmsworld.server.utils.UrlUtils;
import net.gmsworld.server.utils.memcache.JBossCacheProvider;
import net.gmsworld.server.utils.persistence.CheckinPersistenceUtils;
import net.gmsworld.server.utils.persistence.CommentPersistenceUtils;
import net.gmsworld.server.utils.persistence.EMF;
import net.gmsworld.server.utils.persistence.GeocodePersistenceUtils;
import net.gmsworld.server.utils.persistence.LandmarkPersistenceUtils;
import net.gmsworld.server.utils.persistence.LayerPersistenceUtils;
import net.gmsworld.server.utils.persistence.NotificationPersistenceUtils;
import net.gmsworld.server.utils.persistence.ScreenshotPersistenceUtils;
import net.gmsworld.server.utils.persistence.UserPersistenceUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.interceptor.ParameterAware;
import org.apache.struts2.interceptor.ServletRequestAware;

import com.opensymphony.xwork2.ActionSupport;

public class AddItemAction extends ActionSupport implements ParameterAware, ServletRequestAware {

	private static final Logger logger = Logger.getLogger(AddItemAction.class.getName());
	private static final long serialVersionUID = 1L;
	private Map<String, String[]> parameters;
	private HttpServletRequest request;
	private TomcatThreadProvider threadProvider = new TomcatThreadProvider();
	
	public AddItemAction() {
		super();
		GeocodeHelperFactory.setCacheProvider(new JBossCacheProvider());
	}
	
	@Override
	public void setParameters(Map<String, String[]> arg0) {
		this.parameters = arg0;		
	}
	
	private String getParameterValue(String key) {
		if (parameters.containsKey(key)) {
			return parameters.get(key)[0];
		} else {
			return null;
		}
	}
	
	private boolean isEmptyAny(String... params) {
		for (String param : params) {
			if (getParameterValue(param) == null) {
				return true;
			}
		}
		return false;
	}

	public String execute()
	{	
		String type = getParameterValue("type");
		if (StringUtils.equals(type, "landmark")) {
			return executeLandmark();
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
	
	private String executeLandmark() {
		if (isEmptyAny("latitude", "longitude", "name", "username")) {
            addActionError("Missing required landmark parameter!");
            return ERROR;
		} else {
			long start = System.currentTimeMillis();
            double latitude = GeocodeUtils.getLatitude(getParameterValue("latitude"));
            double longitude = GeocodeUtils.getLongitude(getParameterValue("longitude"));
            double altitude = NumberUtils.getDouble(getParameterValue("altitude"), 0.0);

            String name = getParameterValue("name");
            String username = getParameterValue("username");
                     
            String layer = getParameterValue("layer");
            if (StringUtils.isEmpty(layer)) {
            	layer = "Public";
            }
            
            Date validityDate = null;

            String validityStr = getParameterValue("validityDate");
            if (StringUtils.isNotEmpty(validityStr)) {
            	long validity = Long.parseLong(validityStr);
            	validityDate = new Date(validity);
            } else {
            	validityDate = DateUtils.afterOneHundredYearsFromNow();
            }

            String email = getParameterValue("email");
            
            String description = getParameterValue("description");
            String flex = getParameterValue("flex");
            
            Landmark landmark = new Landmark(latitude, longitude, altitude, name, description, username, validityDate, layer, email, flex);
            EntityManager em = EMF.getEntityManager();
            try {
            	LandmarkPersistenceUtils landmarkPersistenceUtils = (LandmarkPersistenceUtils) ServiceLocator.getInstance().getService("bean/LandmarkPersistenceUtils");
            	List<Landmark> newestLandmarks = landmarkPersistenceUtils.findNewestLandmarks(1, em);
            	int id = -1;
            	if (!newestLandmarks.isEmpty()) {
            		Landmark newest = newestLandmarks.get(0);
            	
            		logger.log(Level.INFO, "Comparing " + landmark.getName() + ": " + StringUtil.formatCoordE2(landmark.getLatitude()) + "," + StringUtil.formatCoordE2(landmark.getLongitude()) + 
            			               " with " + newest.getName() + ": " + StringUtil.formatCoordE2(newest.getLatitude()) + "," + StringUtil.formatCoordE2(newest.getLongitude()));
            		if (newest.compare(landmark)) {
            			id =  newest.getId(); 
            		}
            	}
            	
            	if (id > 0) {
            		request.setAttribute("output", "{\"status\":\"ok\",\"id\":" + id + ",\"error\":\"Landmark exists\"}");
            		logger.log(Level.INFO, "Landmark exists.");
            	} else {
            		logger.log(Level.INFO, "New landmark will be created...");
            		landmarkPersistenceUtils.save(landmark, em);	
				
            		//add bitly hash
            		String hash = UrlUtils.getHash(ConfigurationManager.SERVER_URL + "showLandmark/" + landmark.getId());
            		if (hash != null) {
            			//landmark.setHash(hash);
            			//landmarkPersistenceUtils.update(landmark);	
            			request.setAttribute("output", "{\"status\":\"ok\",\"id\":" + landmark.getId() + ",\"hash\":\"" + hash + "\"}");
            		} else {
            			request.setAttribute("output", "{\"status\":\"ok\",\"id\":" + landmark.getId() + "}");
            		}
            		threadProvider.newThread(new LandmarkExtender(landmark, hash)).start();
            		logger.log(Level.INFO, "Done in " + (System.currentTimeMillis()-start) + " millis.");
            	}  
            } catch (NamingException e) {
            	request.setAttribute("output", "{\"error\":\"" + e.getMessage() + "\"}");
				logger.log(Level.SEVERE, e.getMessage(), e);
			}	finally {
				em.close();
			}
            
            return SUCCESS;
		}    
	}
	
	private String executeGeocode() {
		if (isEmptyAny("latitude", "longitude", "address")) {
            addActionError("Missing required geocode parameter!");
            return ERROR;
		} else {
			double latitude = GeocodeUtils.getLatitude(getParameterValue("latitude"));
            double longitude = GeocodeUtils.getLongitude(getParameterValue("longitude"));
            int status = NumberUtils.getInt(getParameterValue("status"), 0);
            String location = getParameterValue("address");

            Geocode g = new Geocode(location, status, latitude, longitude);
            EntityManager em = EMF.getEntityManager();
            try {
            	GeocodePersistenceUtils geocodePeristenceUtils = (GeocodePersistenceUtils) ServiceLocator.getInstance().getService("bean/GeocodePersistenceUtils");
				geocodePeristenceUtils.save(g, em);	
				request.setAttribute("output", "{\"status\":\"ok\",\"id\":" + g.getId() + "}");
            } catch (NamingException e) {
            	request.setAttribute("output", "{\"error\":\"" + e.getMessage() + "\"}");
				logger.log(Level.SEVERE, e.getMessage(), e);
			} finally {
				em.close();
			}
			
			return SUCCESS;
		}
	}
	
	private String executeCheckin() {
		if (isEmptyAny("username", "landmarkId") && isEmptyAny("username", "venueId")) {
            addActionError("Missing required checkin parameter!");
            return ERROR;
		} else {
			int type = NumberUtils.getInt(getParameterValue("itemType"), 0);
			int landmarkId = NumberUtils.getInt(getParameterValue("landmarkId"), -1);
            String username = getParameterValue("username");
            String venueid = getParameterValue("venueId");

            EntityManager em = EMF.getEntityManager();
            try {
            	LandmarkPersistenceUtils landmarkPersistenceUtils = (LandmarkPersistenceUtils) ServiceLocator.getInstance().getService("bean/LandmarkPersistenceUtils");
            	
            	Landmark l = null;
            	if (landmarkId > -1) {
            		l = landmarkPersistenceUtils.selectLandmarkById(landmarkId, em);
            	}
            	
            	CheckinPersistenceUtils checkinPersistenceUtils = (CheckinPersistenceUtils) ServiceLocator.getInstance().getService("bean/CheckinPersistenceUtils");
            	int count = 0;
            	if (type == 2) { //social checkin only: check if last checkin before 8 hours
            		Calendar cal = Calendar.getInstance();
            		cal.add(Calendar.HOUR_OF_DAY, -8);
            		count = checkinPersistenceUtils.countNewer(cal.getTime(), username, venueid, em);
            	}
            	if (count == 0) {
            		Checkin c = new Checkin(username, l, venueid, type);
            		checkinPersistenceUtils.save(c, em);	
            		request.setAttribute("output", "{\"status\":\"ok\",\"id\":" + c.getId() + "}");
            	} else {
            		request.setAttribute("output", "{\"error\":\"last checkin in less than 8 hours\"}");
            	}
            } catch (NamingException e) {
            	request.setAttribute("output", "{\"error\":\"" + e.getMessage() + "\"}");
				logger.log(Level.SEVERE, e.getMessage(), e);
			} finally {
				em.close();
			}
			
			return SUCCESS;
		}
	}
	
	private String executeComment() {
		if (isEmptyAny("username", "landmarkId", "message")) {
            addActionError("Missing required comment parameter!");
            return ERROR;
		} else {
			int landmarkId = NumberUtils.getInt(getParameterValue("landmarkId"), -1);
            String username = getParameterValue("username");
            String message = getParameterValue("message");

            EntityManager em = EMF.getEntityManager();
            try {
            	LandmarkPersistenceUtils landmarkPersistenceUtils = (LandmarkPersistenceUtils) ServiceLocator.getInstance().getService("bean/LandmarkPersistenceUtils");
            	Landmark l = landmarkPersistenceUtils.selectLandmarkById(landmarkId, em);
            	if (l != null) {
            		Comment c = new Comment(username, l, message);
            		CommentPersistenceUtils commentPeristenceUtils = (CommentPersistenceUtils) ServiceLocator.getInstance().getService("bean/CommentPersistenceUtils");
					commentPeristenceUtils.save(c, em);	
					request.setAttribute("output", "{\"status\":\"ok\",id:" + c.getId() + "}");
            	} else {
            		addActionError("Csn't find landmark " + landmarkId + "!");
                    return ERROR;
            	}
            } catch (NamingException e) {
            	request.setAttribute("output", "{\"error\":\"" + e.getMessage() + "\"}");
				logger.log(Level.SEVERE, e.getMessage(), e);
			} finally {
				em.close();
			}
			
			return SUCCESS;
		}
	}
	
	private String executeLayer() {
		if (isEmptyAny("name", "desc", "formatted")) {
            addActionError("Missing required comment parameter!");
            return ERROR;
		} else {
			String name = request.getParameter("name");
			String desc = request.getParameter("desc");
			String formatted = request.getParameter("formatted");
			EntityManager em = EMF.getEntityManager();
            try {
				LayerPersistenceUtils layerPeristenceUtils = (LayerPersistenceUtils) ServiceLocator.getInstance().getService("bean/LayerPersistenceUtils");
				Layer layer = new Layer(name, desc, true, false, true, formatted);
				layerPeristenceUtils.save(layer, em);
				request.setAttribute("output", "{\"status\":\"ok\",\"name\":\"" + name + "\"}");
			} catch (Exception e) {
				request.setAttribute("output", "{\"error\":\"" + e.getMessage() + "\"}");
				logger.log(Level.SEVERE, e.getMessage(), e);
			} finally {
				em.close();
			}
			return SUCCESS;
		}
	}

	private String executeScreenshot() {
		if (isEmptyAny("latitude", "longitude", "filename")) {
            addActionError("Missing required screenshot parameter!");
            return ERROR;
		} else {
			String filename = getParameterValue("filename");
			double latitude = GeocodeUtils.getLatitude(getParameterValue("latitude"));
            double longitude = GeocodeUtils.getLongitude(getParameterValue("longitude"));
            String username = getParameterValue("username");
            int storageId = 1; //Google Cloud Storage
            EntityManager em = EMF.getEntityManager();
            try {
				ScreenshotPersistenceUtils screenshotPeristenceUtils = (ScreenshotPersistenceUtils) ServiceLocator.getInstance().getService("bean/ScreenshotPersistenceUtils");
				Screenshot s = new Screenshot(filename, latitude, longitude, username, storageId);
				screenshotPeristenceUtils.save(s, em);
				request.setAttribute("output", "{\"status\":\"ok\",\"id\":\"" + s.getId() + "\"}");
            } catch (Exception e) {
				request.setAttribute("output", "{\"error\":\"" + e.getMessage() + "\"}");
				logger.log(Level.SEVERE, e.getMessage(), e);
			} finally {
				em.close();
			}
            return SUCCESS;
		}
	}	
	
	private String executeUser() {
		if (isEmptyAny("login", "password", "email")) {
            addActionError("Missing required user parameter!");
            return ERROR;
		} else {
			String login = getParameterValue("login");
			String password = getParameterValue("password");
			String email = getParameterValue("email");
			String firstname = getParameterValue("firstname");
			String lastname = getParameterValue("lastname");
			EntityManager em = EMF.getEntityManager();
            try {
				UserPersistenceUtils userPeristenceUtils = (UserPersistenceUtils) ServiceLocator.getInstance().getService("bean/UserPersistenceUtils");
				User u = userPeristenceUtils.findById(login, em);
				if (u == null) {
					u = new User(login, password,  email, firstname, lastname);
					userPeristenceUtils.save(u, em);
					request.setAttribute("output", "{\"status\":\"ok\",\"login\":\"" + login + "\",\"secret\":\"" + u.getSecret() + "\"}");
				} else {
					request.setAttribute("output", "{\"error\":\"User exists!\"}");    	
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
	
	private String executeNotification() {
		if (isEmptyAny("id")) {
            addActionError("Missing required notification parameter!");
            return ERROR;
		} else {
			String id = request.getParameter("id");
			String statusStr = request.getParameter("status");
			Notification.Status status = Notification.Status.UNVERIFIED;
			if (StringUtils.equals(statusStr, "1") || StringUtils.equalsIgnoreCase(statusStr, "true")) {
				status = Notification.Status.VERIFIED;
			}
			EntityManager em = EMF.getEntityManager();
            try {
            	NotificationPersistenceUtils notificationPeristenceUtils = (NotificationPersistenceUtils) ServiceLocator.getInstance().getService("bean/NotificationPersistenceUtils");
            	Notification n = notificationPeristenceUtils.persist(id, status, em);
				request.setAttribute("output", "{\"status\":\"" +  n.getStatus().name() + "\",\"id\":\"" + id + "\",\"secret\":\"" +  n.getSecret() + "\"}");
			} catch (Exception e) {
				request.setAttribute("output", "{\"error\":\"" + e.getMessage() + "\"}");
				logger.log(Level.SEVERE, e.getMessage(), e);
			} finally {
				em.close();
			}
			return SUCCESS;
		}
	}
	
	@Override
	public void setServletRequest(HttpServletRequest request) {
		this.request = request;
	}
	
	private class LandmarkExtender implements Runnable {

		private Landmark landmark;
		private String hash;
		
		public LandmarkExtender(Landmark landmark, String hash) {
			this.landmark = landmark;
			this.hash = hash;
		}
		
		@Override
		public void run() {
			EntityManager em = EMF.getEntityManager();
            try {
				long start = System.currentTimeMillis();
				LandmarkPersistenceUtils landmarkPersistenceUtils = (LandmarkPersistenceUtils) ServiceLocator.getInstance().getService("bean/LandmarkPersistenceUtils");
        	    if (StringUtils.isNotEmpty(hash)) {
        	    	landmark.setHash(hash);
        	    }
        	    String desc = landmarkPersistenceUtils.setFlex(landmark);
        	    if (StringUtils.isEmpty(landmark.getDescription())) {
        	    	landmark.setDecription(desc);
        	    }
    			landmarkPersistenceUtils.update(landmark, em);	
    			logger.log(Level.INFO, "Landmark " + landmark.getId() + " has been updated in " + (System.currentTimeMillis()-start) + " millis.");
			} catch (Exception e) {
            	logger.log(Level.SEVERE, e.getMessage(), e);
			} finally {
				em.close();
			}
		}
	}
}
