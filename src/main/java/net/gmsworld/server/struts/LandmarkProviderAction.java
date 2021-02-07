package net.gmsworld.server.struts;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import net.gmsworld.server.persistence.Geocode;
import net.gmsworld.server.persistence.Landmark;
import net.gmsworld.server.utils.DateUtils;
import net.gmsworld.server.utils.GeocodeUtils;
import net.gmsworld.server.utils.NumberUtils;
import net.gmsworld.server.utils.ServiceLocator;
import net.gmsworld.server.utils.memcache.CacheUtil;
import net.gmsworld.server.utils.persistence.EMF;
import net.gmsworld.server.utils.persistence.GeocodePersistenceUtils;
import net.gmsworld.server.utils.persistence.LandmarkPersistenceUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.interceptor.ParameterAware;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.json.JSONUtil;
import org.json.JSONObject;

import com.opensymphony.xwork2.ActionSupport;

public class LandmarkProviderAction extends ActionSupport implements ParameterAware, ServletRequestAware {

	private static final Logger logger = Logger.getLogger(LandmarkProviderAction.class.getName());
	private static final long serialVersionUID = 1L;
	private Map<String, String[]> parameters;
	private HttpServletRequest request;
	protected static final int MAX_LANDMARKS = 100;
	protected static final String NEWEST_LANDMARKS = "NewestLandmarks";
	
	private LandmarkPersistenceUtils getLandmarkPersistenceUtils() throws Exception {
		 return (LandmarkPersistenceUtils) ServiceLocator.getInstance().getService("bean/LandmarkPersistenceUtils");
	}
	
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
	    	final int limit = NumberUtils.getInt(getParameter("limit"), 10);
	    	return findNewestLandmarks(limit);
	    } else if (StringUtils.equalsIgnoreCase(getParameter("action"), "update") && getParameter("id") != null) {
	    	return executeUpdate();
	    } else { 
	      	addActionError("Missing required parameter!");
            return ERROR;	
	    }
	}

	private String findNewestLandmarks(int limit) {
		final int l = NumberUtils.normalizeNumber(limit, 1, MAX_LANDMARKS);
		final String key = NEWEST_LANDMARKS + l;
		final long start = System.currentTimeMillis();
		String output = null;
		EntityManager em = null;
		List<Landmark> newestLandmarks = CacheUtil.getList(Landmark.class, key);
		try {
			if (newestLandmarks == null) {
				em = EMF.getEntityManager();
				newestLandmarks = getLandmarkPersistenceUtils().findNewestLandmarks(l, em);
				if (newestLandmarks != null && !newestLandmarks.isEmpty()) {
					CacheUtil.put(key, newestLandmarks);
				}
			}
			output = JSONUtil.serialize(newestLandmarks, null, null, true, true);
		} catch (Throwable e) {
			output = "{\"error\":\"" + e.getMessage() + "\"}";
			logger.log(Level.SEVERE, e.getMessage(), e);
			ServletActionContext.getResponse().setStatus(500);
		} finally {
			if (em != null) {
				em.close();
			}
		}
		logger.log(Level.INFO, "findNewestLandmarks(): found " + (newestLandmarks == null ? 0 : newestLandmarks.size()) + " landmarks in " + (System.currentTimeMillis() - start)  + " millis!");
		request.setAttribute("output", output);
		return SUCCESS;
	}
	
	private String findById() {
		int id = NumberUtils.getInt(getParameter("id"), -1);
		String output = null;
		EntityManager em = EMF.getEntityManager();
		try {
			Landmark l = getLandmarkPersistenceUtils().selectLandmarkById(id, em);
			if (l != null) {
				output = JSONUtil.serialize(l, null, null, true, true);
			} else {
				output = "{\"error\":\"No landmark found with id " + id + "\"}";
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
	
	private String findByHash() {
		String hash = getParameter("hash");
		String output = null;
		EntityManager em = EMF.getEntityManager();
		try {
			Landmark l = getLandmarkPersistenceUtils().selectLandmarkByHash(hash, em);	
			if (l != null) {
				output = JSONUtil.serialize(l, null, null, true, true);
			} else {
				output = "{\"error\":\"No landmark found with hash " + hash + "\"}";
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
	
	private String searchLandmarks() {
		long start = System.currentTimeMillis();
		String query = getParameter("query");
		String output = null;
		List<Landmark> landmarks = null;
		int limit = NumberUtils.getInt(getParameter("limit"), 10);
		EntityManager em = EMF.getEntityManager();
		
		try {
			logger.log(Level.INFO, "Querying for {0}", query);
			landmarks = getLandmarkPersistenceUtils().searchLandmarks(query, limit, em);	
			output = JSONUtil.serialize(landmarks, null, null, true, true);
		} catch (Exception e) {
			output = "{\"error\":\"" + e.getMessage() + "\"}";
			logger.log(Level.SEVERE, e.getMessage(), e);
		} finally {
			em.close();
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
		EntityManager em = EMF.getEntityManager();
		try {
			landmarks = getLandmarkPersistenceUtils().selectLandmarksByUserAndLayer(username, layer, first, limit, em);	
			output = JSONUtil.serialize(landmarks, null, null, true, true);
		} catch (Exception e) {
			output = "{\"error\":\"" + e.getMessage() + "\"}";
			logger.log(Level.SEVERE, e.getMessage(), e);
		} finally {
			em.close();
		}
		logger.log(Level.INFO, "selectLandmarksByUserAndLayer(): found " + (landmarks == null ? 0 : landmarks.size()) + " landmarks in " + (System.currentTimeMillis() - start)  + " millis!");
		request.setAttribute("output", output);
		return SUCCESS;
	}
	
	private String countLandmarksByUserAndLayer() {
		String username = getParameter("username");
		String layer = getParameter("layer");
		String output = null;
		EntityManager em = EMF.getEntityManager();
		try {
			int count = getLandmarkPersistenceUtils().countLandmarksByUserAndLayer(username, layer, em);	
			output = "{\"count\":" + count + "}"; 
		} catch (Exception e) {
			output = "{\"error\":\"" + e.getMessage() + "\"}";
			logger.log(Level.SEVERE, e.getMessage(), e);
		} finally {
			em.close();
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
		EntityManager em = EMF.getEntityManager();
		try {
			landmarks = getLandmarkPersistenceUtils().selectLandmarksByCoordsAndLayer(layer, latitude, longitude, radius * 1000, limit, em);	
			output = JSONUtil.serialize(landmarks, null, null, true, true);
		} catch (Exception e) {
			output = "{\"error\":\"" + e.getMessage() + "\"}";
			logger.log(Level.SEVERE, e.getMessage(), e);
		} finally {
			em.close();
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
		EntityManager em = EMF.getEntityManager();
		try {
			int count = getLandmarkPersistenceUtils().countLandmarksByCoordsAndLayer(layer, latitude, longitude, radius * 1000, em);	
			output = "{\"count\":" + count + "}"; 
		} catch (Exception e) {
			output = "{\"error\":\"" + e.getMessage() + "\"}";
			logger.log(Level.SEVERE, e.getMessage(), e);
		} finally {
			em.close();
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
		EntityManager em = EMF.getEntityManager();
		try {
			landmarks = getLandmarkPersistenceUtils().selectLandmarksByMonth(month, first, limit, em);
			output = JSONUtil.serialize(landmarks, null, null, true, true);
		} catch (Exception e) {
			output = "{\"error\":\"" + e.getMessage() + "\"}";
			logger.log(Level.SEVERE, e.getMessage(), e);
		} finally {
			em.close();
		}
		logger.log(Level.INFO, "selectLandmarksByMonth(): found " + (landmarks == null ? 0 : landmarks.size()) + " landmarks in " + (System.currentTimeMillis() - start)  + " millis!");
		request.setAttribute("output", output);
		return SUCCESS;
	}
	
	private String countLandmarksByMonth() {
		String month = getParameter("month");
		String output = null;
		EntityManager em = EMF.getEntityManager();
		try {
			int count = getLandmarkPersistenceUtils().countLandmarksByMonth(month, em);
			output = "{\"count\":" + count + "}"; 
		} catch (Exception e) {
			output = "{\"error\":\"" + e.getMessage() + "\"}";
			logger.log(Level.SEVERE, e.getMessage(), e);
		} finally {
			em.close();
		}
		request.setAttribute("output", output);
		return SUCCESS;
	}
	
	private String getHeatMap() {
		int days = NumberUtils.getInt(getParameter("days"), 365);
		String output = null;
		EntityManager em = EMF.getEntityManager();
		try {
			Map<String, Integer> bucket = getLandmarkPersistenceUtils().getNativeHeatMap(days, em); //.getHeatMap(days, em);
			output = JSONUtil.serialize(bucket, null, null, true, true);
		} catch (Exception e) {
			output = "{\"error\":\"" + e.getMessage() + "\"}";
			logger.log(Level.SEVERE, e.getMessage(), e);
		} finally {
			em.close();
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
		EntityManager em = EMF.getEntityManager();
		try {
			List<Object[]> count = getLandmarkPersistenceUtils().countLandmarksByCoords(latitude, longitude, radius * 1000, em);	
			output = JSONUtil.serialize(count, null, null, true, true);
		} catch (Exception e) {
			output = "{\"error\":\"" + e.getMessage() + "\"}";
			logger.log(Level.SEVERE, e.getMessage(), e);
		} finally {
			em.close();
		}
		request.setAttribute("output", output);
		return SUCCESS;
	}
	
	private String executeUpdate() {
		final int id = NumberUtils.getInt(getParameter("id"), -1);
		if (id > 0) {
			final double latitude = GeocodeUtils.getLatitude(getParameter("latitude"));
			final double longitude = GeocodeUtils.getLongitude(getParameter("longitude"));
			final double altitude = NumberUtils.getDouble(getParameter("altitude"), 0.0);

			Date validityDate = null;
			final String validityStr = getParameter("validityDate");
			if (StringUtils.isNotEmpty(validityStr)) {
				long validity = Long.parseLong(validityStr);
				validityDate = new Date(validity);
			} else {
				validityDate = DateUtils.afterOneHundredYearsFromNow();
			}

			String description = getParameter("description");
            
			String flex = getParameter("flex");
			JSONObject landmarkFlex = null;
			//add city and country    
			String cc = null, city = null;
			if (StringUtils.startsWith(flex, "{")) {
				landmarkFlex = new JSONObject(flex);
				if (landmarkFlex.has("cc")) {
					cc = landmarkFlex.getString("cc");
				}
				if (landmarkFlex.has("city")) {
					city = landmarkFlex.getString("city");
				} else if (landmarkFlex.has("county")) {
					city = landmarkFlex.getString("county");
				} else if (landmarkFlex.has("state")) {
					city = landmarkFlex.getString("state");
				}
			} else {
				landmarkFlex = new JSONObject();
			}
    		
			EntityManager em = EMF.getEntityManager();
        
			if (cc == null || city == null) {
				try {
					GeocodePersistenceUtils geocodePeristenceUtils = (GeocodePersistenceUtils) ServiceLocator.getInstance().getService("bean/GeocodePersistenceUtils");
					Geocode g = geocodePeristenceUtils.findByCoords(latitude, longitude, 0.001d, em);
					if (g != null) {
						if (StringUtils.startsWith(g.getFlex(), "{")) {
							JSONObject geocodeFlex = new JSONObject(g.getFlex());
							if (geocodeFlex.has("cc")) {
								cc = geocodeFlex.getString("cc");
								landmarkFlex.put("cc", cc);	
							}
							if (geocodeFlex.has("city")) {
								city = geocodeFlex.getString("city");
								landmarkFlex.put("city", city);
							} else if (geocodeFlex.has("county")) {
								city = geocodeFlex.getString("county");
								landmarkFlex.put("city", city);
							} else if (geocodeFlex.has("state")) {
								city = geocodeFlex.getString("state");
								landmarkFlex.put("city", city);
							}
							if (landmarkFlex.has("cc") || landmarkFlex.has("city")) {
								flex = landmarkFlex.toString();
							}
						}
						if (StringUtils.isEmpty(description)) {
							description = g.getLocation();
						}
					}
				} catch (NamingException e) {
					logger.log(Level.SEVERE, e.getMessage(), e);
				}
			}
            
			try {
				LandmarkPersistenceUtils landmarkPersistenceUtils = (LandmarkPersistenceUtils) ServiceLocator.getInstance().getService("bean/LandmarkPersistenceUtils");
				Landmark landmark = landmarkPersistenceUtils.selectLandmarkById(id, em);
				if (landmark != null) {	
					landmark.setAltitude(altitude);
					if (latitude != 180) {
						landmark.setLatitude(latitude);
					}
					if (longitude != 180) {
						landmark.setLongitude(longitude);
					}
					if (StringUtils.isNotEmpty(description)) {
						landmark.setDescription(description);
					}
					if (StringUtils.isNotEmpty(flex)) {
						landmark.setFlex(flex);
					}
					landmark.setCreationDate(new Date());
					landmark.setValidityDate(validityDate);
            	
					logger.log(Level.INFO, "Landmark " + id + " will be updated...");
					landmarkPersistenceUtils.update(landmark, em);	
					//invalidate NewestLandmarks
					CacheUtil.removeAll(LandmarkProviderAction.NEWEST_LANDMARKS, 1, LandmarkProviderAction.MAX_LANDMARKS);
            	
					JSONObject output = new JSONObject();
            			output.put("status", "ok")
            			.put("cc", cc)
            			.put("city", city)
            			.put("id", landmark.getId());
            		if (StringUtils.isNotEmpty(landmark.getHash())) {
            			output.put("hash", landmark.getHash());
            		}	 
            		request.setAttribute("output", output.toString());
				} else {
					request.setAttribute("output", "{\"error\":\"Landmark " + id + " not found\"}");
				}
			} catch (NamingException e) {
				request.setAttribute("output", "{\"error\":\"" + e.getMessage() + "\"}");
				logger.log(Level.SEVERE, e.getMessage(), e);
			}	finally {
				em.close();
			}	
		}	else {
    		request.setAttribute("output", "{\"error\":\"Landmark " + id + " not found\"}");
    	}
            
        return SUCCESS;
	}
}
