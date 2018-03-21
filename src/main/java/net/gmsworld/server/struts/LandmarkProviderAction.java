package net.gmsworld.server.struts;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import net.gmsworld.server.persistence.Landmark;
import net.gmsworld.server.utils.GeocodeUtils;
import net.gmsworld.server.utils.NumberUtils;
import net.gmsworld.server.utils.ServiceLocator;
import net.gmsworld.server.utils.memcache.CacheUtil;
import net.gmsworld.server.utils.persistence.LandmarkPersistenceUtils;

import org.apache.struts2.interceptor.ParameterAware;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.json.JSONUtil;

import com.opensymphony.xwork2.ActionSupport;

public class LandmarkProviderAction extends ActionSupport implements ParameterAware, ServletRequestAware {

	private static final Logger logger = Logger.getLogger(LandmarkProviderAction.class.getName());
	private static final long serialVersionUID = 1L;
	private Map<String, String[]> parameters;
	private LandmarkPersistenceUtils landmarkPersistenceUtils;
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
		if (getParameter("layer") != null && (getParameter("latitude") != null || getParameter("lat") != null) &&
				(getParameter("longitude") != null || getParameter("lng") != null) && 
				getParameter("radius") != null && getParameter("count") != null) {
		    return countLandmarksByCoordsAndLayer();
		} else if (getParameter("layer") != null && (getParameter("latitude") != null || getParameter("lat") != null) &&
				(getParameter("longitude") != null || getParameter("lng") != null) && 
				getParameter("radius") != null) {
		    return selectLandmarksByCoordsAndLayer();
		} else if ((getParameter("latitude") != null || getParameter("lat") != null) &&
				(getParameter("longitude") != null || getParameter("lng") != null) && 
				getParameter("radius") != null && getParameter("count") != null) {
			return countLandmarksByCoords();
		} else if ((getParameter("username") != null || getParameter("layer") != null) && 
				getParameter("count") != null) {
		    return countLandmarksByUserAndLayer();
		} else if (getParameter("username") != null || getParameter("layer") != null) {
		    return selectLandmarksByUserAndLayer();
		} else if (getParameter("hash") != null) {
	    	return findByHash();
	    } else if (getParameter("id") != null) {
	    	return findById();
	    } else if (getParameter("query") != null) {
	    	return searchLandmarks();
	    } else if (getParameter("month") != null && getParameter("count") != null) {
	    	return countLandmarksByMonth();
	    } else if (getParameter("month") != null) {
	    	return selectLandmarksByMonth();
	    } else if (getParameter("days") != null && getParameter("heatMap") != null) {
	    	return getHeatMap();
	    } else if (getParameter("limit") != null) {
	    	return findNewestLandmarks();
	    } else { 
	      	addActionError("Missing required parameter!");
            return ERROR;	
	    }
	}

	private String findNewestLandmarks() {
		final String key = "NewestLandmarks";
		long start = System.currentTimeMillis();
		int limit = NumberUtils.getInt(getParameter("limit"), 10);
		String output = null;
		List<Landmark> newestLandmarks = CacheUtil.getList(Landmark.class, key);
		try {
			if (newestLandmarks == null) {
				landmarkPersistenceUtils = (LandmarkPersistenceUtils) ServiceLocator.getInstance().getService("java:comp/env/ bean/LandmarkPersistenceUtils");
				newestLandmarks = landmarkPersistenceUtils.findNewestLandmarks(limit);
				if (newestLandmarks != null && !newestLandmarks.isEmpty()) {
					CacheUtil.putShort(key, newestLandmarks);
				}
			}
			output = JSONUtil.serialize(newestLandmarks, null, null, true, true);
		} catch (Exception e) {
			output = "{\"error\":\"" + e.getMessage() + "\"}";
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
		logger.log(Level.INFO, "findNewestLandmarks(): found " + (newestLandmarks == null ? 0 : newestLandmarks.size()) + " landmarks in " + (System.currentTimeMillis() - start)  + " millis!");
		request.setAttribute("output", output);
		return SUCCESS;
	}
	
	private String findById() {
		int id = NumberUtils.getInt(getParameter("id"), -1);
		String output = null;
		try {
			landmarkPersistenceUtils = (LandmarkPersistenceUtils) ServiceLocator.getInstance().getService("java:comp/env/ bean/LandmarkPersistenceUtils");
			Landmark l = landmarkPersistenceUtils.selectLandmarkById(id);
			if (l != null) {
				output = JSONUtil.serialize(l, null, null, true, true);
			} else {
				output = "{\"error\":\"No landmark found with id " + id + "\"}";
			}
		} catch (Exception e) {
			output = "{\"error\":\"" + e.getMessage() + "\"}";
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
		request.setAttribute("output", output);
		return SUCCESS;
	}
	
	private String findByHash() {
		String hash = getParameter("hash");
		String output = null;
		try {
			landmarkPersistenceUtils = (LandmarkPersistenceUtils) ServiceLocator.getInstance().getService("java:comp/env/ bean/LandmarkPersistenceUtils");
			Landmark l = landmarkPersistenceUtils.selectLandmarkByHash(hash);	
			if (l != null) {
				output = JSONUtil.serialize(l, null, null, true, true);
			} else {
				output = "{\"error\":\"No landmark found with hash " + hash + "\"}";
			}
		} catch (Exception e) {
			output = "{\"error\":\"" + e.getMessage() + "\"}";
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
		request.setAttribute("output", output);
		return SUCCESS;
	}
	
	private String searchLandmarks() {
		long start = System.currentTimeMillis();
		String query = getParameter("query");
		String output = null;
		List<Landmark> landmarks = null;
		int limit = NumberUtils.getInt(getParameter("limit"), 10);
		
		
		try {
			logger.log(Level.INFO, "Querying for {0}", query);
			landmarkPersistenceUtils = (LandmarkPersistenceUtils) ServiceLocator.getInstance().getService("java:comp/env/ bean/LandmarkPersistenceUtils");
			landmarks = landmarkPersistenceUtils.searchLandmarks(query, limit);	
			output = JSONUtil.serialize(landmarks, null, null, true, true);
		} catch (Exception e) {
			output = "{\"error\":\"" + e.getMessage() + "\"}";
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
		logger.log(Level.INFO, "searchLandmarks(): found " + (landmarks == null ? 0 : landmarks.size()) + " landmarks in " + (System.currentTimeMillis() - start)  + " millis!");
		request.setAttribute("output", output);
		return SUCCESS;
	}
	
	private String selectLandmarksByUserAndLayer() {
		long start = System.currentTimeMillis();
		String username = getParameter("username");
		String layer = getParameter("layer");
		int limit = NumberUtils.getInt(getParameter("limit"), 10);
		int first = NumberUtils.getInt(getParameter("first"), 0);
		String output = null;
		List<Landmark> landmarks = null;
		try {
			landmarkPersistenceUtils = (LandmarkPersistenceUtils) ServiceLocator.getInstance().getService("java:comp/env/ bean/LandmarkPersistenceUtils");
			landmarks = landmarkPersistenceUtils.selectLandmarksByUserAndLayer(username, layer, first, limit);	
			output = JSONUtil.serialize(landmarks, null, null, true, true);
		} catch (Exception e) {
			output = "{\"error\":\"" + e.getMessage() + "\"}";
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
		logger.log(Level.INFO, "selectLandmarksByUserAndLayer(): found " + (landmarks == null ? 0 : landmarks.size()) + " landmarks in " + (System.currentTimeMillis() - start)  + " millis!");
		request.setAttribute("output", output);
		return SUCCESS;
	}
	
	private String countLandmarksByUserAndLayer() {
		String username = getParameter("username");
		String layer = getParameter("layer");
		String output = null;
		try {
			landmarkPersistenceUtils = (LandmarkPersistenceUtils) ServiceLocator.getInstance().getService("java:comp/env/ bean/LandmarkPersistenceUtils");
			int count = landmarkPersistenceUtils.countLandmarksByUserAndLayer(username, layer);	
			output = "{\"count\":" + count + "}"; 
		} catch (Exception e) {
			output = "{\"error\":\"" + e.getMessage() + "\"}";
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
		request.setAttribute("output", output);
		return SUCCESS;
	}
	
	private String selectLandmarksByCoordsAndLayer() {
		long start = System.currentTimeMillis();
		double latitude;
        if (getParameter("lat") != null) {
            latitude = GeocodeUtils.getLatitude(getParameter("lat"));
        } else {
            latitude = GeocodeUtils.getLatitude(getParameter("latitude"));
        }

        double longitude;
        if (getParameter("lng") != null) {
            longitude = GeocodeUtils.getLongitude(getParameter("lng"));
        } else {
            longitude = GeocodeUtils.getLongitude(getParameter("longitude"));
        }
		int radius = NumberUtils.getRadius(getParameter("radius"), 3, 100);
        int limit = NumberUtils.getInt(getParameter("limit"), 30);
        String layer = getParameter("layer");
		String output = null;
		List<Landmark> landmarks = null;
		try {
			landmarkPersistenceUtils = (LandmarkPersistenceUtils) ServiceLocator.getInstance().getService("java:comp/env/ bean/LandmarkPersistenceUtils");
			landmarks = landmarkPersistenceUtils.selectLandmarksByCoordsAndLayer(layer, latitude, longitude, radius * 1000, limit);	
			output = JSONUtil.serialize(landmarks, null, null, true, true);
		} catch (Exception e) {
			output = "{\"error\":\"" + e.getMessage() + "\"}";
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
		logger.log(Level.INFO, "selectLandmarksByCoordsAndLayer(): found " + (landmarks == null ? 0 : landmarks.size()) + " landmarks in " + (System.currentTimeMillis() - start)  + " millis!");
		request.setAttribute("output", output);
		return SUCCESS;
	}
	
	private String countLandmarksByCoordsAndLayer() {
		double latitude;
        if (getParameter("lat") != null) {
            latitude = GeocodeUtils.getLatitude(getParameter("lat"));
        } else {
            latitude = GeocodeUtils.getLatitude(getParameter("latitude"));
        }

        double longitude;
        if (getParameter("lng") != null) {
            longitude = GeocodeUtils.getLongitude(getParameter("lng"));
        } else {
            longitude = GeocodeUtils.getLongitude(getParameter("longitude"));
        }
		int radius = NumberUtils.getRadius(getParameter("radius"), 3, 100);
		String layer = getParameter("layer");
		String output = null;
		try {
			landmarkPersistenceUtils = (LandmarkPersistenceUtils) ServiceLocator.getInstance().getService("java:comp/env/ bean/LandmarkPersistenceUtils");
			int count = landmarkPersistenceUtils.countLandmarksByCoordsAndLayer(layer, latitude, longitude, radius * 1000);	
			output = "{\"count\":" + count + "}"; 
		} catch (Exception e) {
			output = "{\"error\":\"" + e.getMessage() + "\"}";
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
		request.setAttribute("output", output);
		return SUCCESS;
	}
	
	private String selectLandmarksByMonth() {
		long start = System.currentTimeMillis();
		String month = getParameter("month");
		int limit = NumberUtils.getInt(getParameter("limit"), 10);
		int first = NumberUtils.getInt(getParameter("first"), 0);
		String output = null;
		List<Landmark> landmarks = null;
		try {
			landmarkPersistenceUtils = (LandmarkPersistenceUtils) ServiceLocator.getInstance().getService("java:comp/env/ bean/LandmarkPersistenceUtils");
			landmarks = landmarkPersistenceUtils.selectLandmarksByMonth(month, first, limit);
			output = JSONUtil.serialize(landmarks, null, null, true, true);
		} catch (Exception e) {
			output = "{\"error\":\"" + e.getMessage() + "\"}";
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
		logger.log(Level.INFO, "selectLandmarksByMonth(): found " + (landmarks == null ? 0 : landmarks.size()) + " landmarks in " + (System.currentTimeMillis() - start)  + " millis!");
		request.setAttribute("output", output);
		return SUCCESS;
	}
	
	private String countLandmarksByMonth() {
		String month = getParameter("month");
		String output = null;
		try {
			landmarkPersistenceUtils = (LandmarkPersistenceUtils) ServiceLocator.getInstance().getService("java:comp/env/ bean/LandmarkPersistenceUtils");
			int count = landmarkPersistenceUtils.countLandmarksByMonth(month);
			output = "{\"count\":" + count + "}"; 
		} catch (Exception e) {
			output = "{\"error\":\"" + e.getMessage() + "\"}";
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
		request.setAttribute("output", output);
		return SUCCESS;
	}
	
	private String getHeatMap() {
		int days = NumberUtils.getInt(getParameter("days"), 365);
		String output = null;
		try {
			landmarkPersistenceUtils = (LandmarkPersistenceUtils) ServiceLocator.getInstance().getService("java:comp/env/ bean/LandmarkPersistenceUtils");
			Map<String, Integer> bucket = landmarkPersistenceUtils.getNativeHeatMap(days); //.getHeatMap(days);
			output = JSONUtil.serialize(bucket, null, null, true, true);
		} catch (Exception e) {
			output = "{\"error\":\"" + e.getMessage() + "\"}";
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
		request.setAttribute("output", output);
		return SUCCESS;
	}
	
	private String countLandmarksByCoords() {
		double latitude;
        if (getParameter("lat") != null) {
            latitude = GeocodeUtils.getLatitude(getParameter("lat"));
        } else {
            latitude = GeocodeUtils.getLatitude(getParameter("latitude"));
        }

        double longitude;
        if (getParameter("lng") != null) {
            longitude = GeocodeUtils.getLongitude(getParameter("lng"));
        } else {
            longitude = GeocodeUtils.getLongitude(getParameter("longitude"));
        }
		int radius = NumberUtils.getRadius(getParameter("radius"), 3, 100);
		String output = null;
		try {
			landmarkPersistenceUtils = (LandmarkPersistenceUtils) ServiceLocator.getInstance().getService("java:comp/env/ bean/LandmarkPersistenceUtils");
			List<Object[]> count = landmarkPersistenceUtils.countLandmarksByCoords(latitude, longitude, radius * 1000);	
			output = JSONUtil.serialize(count, null, null, true, true);
		} catch (Exception e) {
			output = "{\"error\":\"" + e.getMessage() + "\"}";
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
		request.setAttribute("output", output);
		return SUCCESS;
	}
}
