package net.gmsworld.server.utils.persistence;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.commons.lang.StringUtils;
import org.hibernate.LockMode;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.StatelessSession;
import org.json.JSONObject;

import net.gmsworld.server.persistence.Landmark;
import net.gmsworld.server.utils.DateUtils;
import net.gmsworld.server.utils.StringUtil;

public class LandmarkPersistenceUtils {

    private final Logger logger = Logger.getLogger(LandmarkPersistenceUtils.class.getName());
	
	public void save(Landmark landmark, EntityManager entityManager) {
		EMF.save(landmark, entityManager);		
	}
	
	public void update(Landmark landmark, EntityManager entityManager) {
		EMF.update(landmark, entityManager);		
	}
	
	public boolean remove(int id, EntityManager entityManager) {
		Landmark landmark = selectLandmarkById(id, entityManager);
		if (landmark != null) {
			EMF.remove(landmark, entityManager);
			return true;
		} else {
			return false;
		}
	}
	
	public List<Landmark> findNewestLandmarks(int limit, EntityManager entityManager) {
        TypedQuery<Landmark> query = entityManager.createNamedQuery(Landmark.FIND_NEWEST, Landmark.class);
        query.setMaxResults(limit);
        return query.getResultList();
    }
	
	public Landmark selectLandmarkByHash(String hash, EntityManager entityManager) {
		TypedQuery<Landmark> query = entityManager.createNamedQuery(Landmark.FIND_BY_HASH, Landmark.class);
		query.setParameter("hash", hash);
		Landmark l = null;
		try {
			l = query.getSingleResult();
		} catch (NoResultException nre) {
			logger.log(Level.WARNING, "No landmark found with hash {0}", hash);
		}					
	    return l;
	}
	
	public Landmark selectLandmarkById(int id, EntityManager entityManager) {
		return entityManager.find(Landmark.class, id);
	}
	
	public List<Landmark> searchLandmarks(String queryStr, int limit, EntityManager entityManager) {
        String joinedTokens = buildQueryString(queryStr);
		if (joinedTokens != null) {
			Query query = entityManager.createNativeQuery("select * from landmark where to_tsvector(name || ' ' || description) @@ to_tsquery(:query) ORDER BY LANDMARK_ID DESC LIMIT :limit", Landmark.class).
		    	setParameter("query", joinedTokens).
		    	setParameter("limit", limit);
			query.setHint("org.hibernate.cacheable", Boolean.TRUE);
			return query.getResultList();
		} else {
			return new ArrayList<Landmark>();
		}
    }
	
	public List<Landmark> selectLandmarksByUserAndLayer(String username, String layer, int first, int limit, EntityManager entityManager) {
		TypedQuery<Landmark> query = null; 
		
		if (layer != null && username == null) {
			query = entityManager.createNamedQuery(Landmark.FIND_LAYER, Landmark.class);
			query.setParameter("layer", layer);
		} else if (layer == null && username != null) {
			query = entityManager.createNamedQuery(Landmark.FIND_USER, Landmark.class);
			query.setParameter("username", username);
		} else if (layer != null && username != null) {
			query = entityManager.createNamedQuery(Landmark.FIND_USER_LAYER, Landmark.class);
			query.setParameter("username", username);
			query.setParameter("layer", layer);
		}
		
		if (query != null) {
			query.setFirstResult(first);
			query.setMaxResults(limit);
			return query.getResultList();
		} else {
			return Collections.<Landmark>emptyList();
		}
	}
	
	public int countLandmarksByUserAndLayer(String username, String layer, EntityManager entityManager) {
		TypedQuery<Long> query = null;
		if (layer != null && username == null) {
			query = entityManager.createNamedQuery(Landmark.COUNT_LAYER, Long.class);
			query.setParameter("layer", layer);
		} else if (layer == null && username != null) {
			query = entityManager.createNamedQuery(Landmark.COUNT_USER, Long.class);
			query.setParameter("username", username);
		} else if (layer != null && username != null) {
		    query = entityManager.createNamedQuery(Landmark.COUNT_USER_LAYER, Long.class);
		    query.setParameter("username", username);
			query.setParameter("layer", layer);
		}
		if (query != null) {
			return query.getSingleResult().intValue();
		} else {
			return 0;
		}
	}
	
	public List<Landmark> selectLandmarksByCoordsAndLayer(String layer, double latitude, double longitude, int radius, int limit, EntityManager entityManager) {
    	logger.log(Level.INFO, "Searching for landmarks in " + latitude + "," + longitude + 
    			" with radius " + radius + " meters with limit " + limit + " records.");
    	//radius in meters
    	Query query = entityManager.createNativeQuery("SELECT * FROM landmark WHERE VALIDITY_DATE > now() and layer = :layer and earth_box(ll_to_earth(:latitude, :longitude), :radius) @> ll_to_earth(latitude, longitude) order by creation_date desc limit :limit", Landmark.class).
    			setParameter("layer", layer).
    			setParameter("latitude", latitude).
    			setParameter("longitude", longitude).
    			setParameter("radius", radius).
    			setParameter("limit", limit);
    	query.setHint("org.hibernate.cacheable", Boolean.TRUE);
    	return (List<Landmark>) query.getResultList();
    }
	
	public int countLandmarksByCoordsAndLayer(String layer, double latitude, double longitude, int radius, EntityManager entityManager) {
    	//radius in meters
		Query query = entityManager.createNativeQuery("SELECT count(*) FROM landmark WHERE VALIDITY_DATE > now() and layer = :layer and  earth_box(ll_to_earth(:latitude, :longitude), :radius) @> ll_to_earth(latitude, longitude)").
    			setParameter("layer", layer).
    			setParameter("latitude", latitude).
    			setParameter("longitude", longitude).
    			setParameter("radius", radius);
		//query.setHint("org.hibernate.cacheable", Boolean.TRUE);		
    	return ((BigInteger) query.getSingleResult()).intValue();
    }
	
	public List<Object[]> countLandmarksByCoords(double latitude, double longitude, int radius, EntityManager entityManager) {
    	//radius in meters
		Query query = entityManager.createNativeQuery("SELECT count(*), layer FROM landmark WHERE earth_box(ll_to_earth(:latitude, :longitude), :radius) @> ll_to_earth(latitude, longitude) group by layer").
    			setParameter("latitude", latitude).
    			setParameter("longitude", longitude).
    			setParameter("radius", radius);
		//query.setHint("org.hibernate.cacheable", Boolean.TRUE);		
    	return (List<Object[]>) query.getResultList();
    }
	
	public List<Landmark> selectLandmarksByMonth(String month, int first, int limit, EntityManager entityManager) {
		Calendar calendar = Calendar.getInstance();
		
		try {
		   calendar.setTime(DateUtils.parseDate("MM-yyyy", month));
		   TypedQuery<Landmark> query = entityManager.createNamedQuery(Landmark.FIND_YEAR_MONTH, Landmark.class);
		   query.setParameter("year", calendar.get(Calendar.YEAR));
		   query.setParameter("month", calendar.get(Calendar.MONTH)+1);
		   query.setFirstResult(first);
		   query.setMaxResults(limit);
		   return query.getResultList();
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			return null;
		}
	}
	
	public int countLandmarksByMonth(String month, EntityManager entityManager) {
		Calendar calendar = Calendar.getInstance();
		
		try {
		   calendar.setTime(DateUtils.parseDate("MM-yyyy", month));
		   TypedQuery<Long> query = entityManager.createNamedQuery(Landmark.COUNT_YEAR_MONTH, Long.class);
		   query.setParameter("year", calendar.get(Calendar.YEAR));
		   query.setParameter("month", calendar.get(Calendar.MONTH)+1);
		   return query.getSingleResult().intValue();
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			return 0;
		}
	}

	public Map<String, Integer> getHeatMap(int nDays, EntityManager entityManager) {
		Map<String, Integer> bucket = new HashMap<String, Integer>();
		Calendar calendar = Calendar.getInstance();
		if (nDays < 0) {
			calendar.add(Calendar.DAY_OF_YEAR, nDays);
		} else {
			calendar.add(Calendar.DAY_OF_YEAR, -nDays);
		}
		TypedQuery<Landmark> query = entityManager.createNamedQuery(Landmark.FIND_NEWER, Landmark.class);
		query.setParameter("date", calendar.getTime());
		
		List<Landmark> newer = query.getResultList();
		
		logger.log(Level.INFO, "Found " + newer.size() + " landmarks from last " + nDays + " days");
		
		for (Landmark l : newer) {
			String key = StringUtil.formatCoordE2(l.getLatitude()) + "_" + StringUtil.formatCoordE2(l.getLongitude());
            Integer currentValue = bucket.remove(key);
            if (currentValue != null) {
                currentValue++;
            } else {
                currentValue = 1;
            }
            bucket.put(key, currentValue);
		}
		
		entityManager.clear();
		
		return bucket;
	}
	
	public Map<String, Integer> getNativeHeatMap(int nDays, EntityManager entityManager) {
		Map<String, Integer> bucket = new HashMap<String, Integer>();
		
		Calendar calendar = Calendar.getInstance();
		if (nDays < 0) {
			calendar.add(Calendar.DAY_OF_YEAR, nDays);
		} else {
			calendar.add(Calendar.DAY_OF_YEAR, -nDays);
		}
		
		StatelessSession session = ((Session) entityManager.getDelegate()).getSessionFactory().openStatelessSession();

		org.hibernate.Query query =  session.createQuery("select l from Landmark l where l.creationDate > :date");
		query.setParameter("date", calendar.getTime());
		query.setFetchSize(Integer.valueOf(1000));
	    query.setReadOnly(true);
	    query.setLockMode("a", LockMode.NONE);
	    ScrollableResults results = query.scroll(ScrollMode.FORWARD_ONLY);
	    int counter = 0;
	    while (results.next()) {
	          Landmark l = (Landmark) results.get(0);
	          String key = StringUtil.formatCoordE2(l.getLatitude()) + "_" + StringUtil.formatCoordE2(l.getLongitude());
	          Integer currentValue = bucket.remove(key);
	          if (currentValue != null) {
	                currentValue++;
	          } else {
	                currentValue = 1;
	          }
	          bucket.put(key, currentValue);
	          counter++;
	    }
	    
	    logger.log(Level.INFO, "Found " + counter + " landmarks from last " + nDays + " days");
		
	    results.close();
	    session.close();
		
		return bucket;
	}
	
	public static String buildQueryString(String queryStr) {
		//joinedTokens should be a single tokens separated by the boolean operators & and, | or, and ! not		
		String joinedTokens = null;
		String[] tokens = StringUtils.split(queryStr, ' ');
		if (tokens.length == 1 && StringUtils.isNotBlank(queryStr)) {
			joinedTokens = StringUtils.replaceEach(queryStr, new String[]{"(",")","!","'",":","&"}, new String[]{"","","","","",""}); 	
		} else if (tokens.length > 1) {
			List<String> tokensArr = new ArrayList<String>();
			for (String token : tokens) {
                String normalized = StringUtils.replaceEach(token, new String[]{"(",")","!","'",":","&"}, new String[]{"","","","","",""}); 			
				if (StringUtils.isNotBlank(normalized) && !StringUtils.isWhitespace(normalized) ) {
					tokensArr.add(normalized);
				}
			}
			if (!tokensArr.isEmpty()) {
				joinedTokens = StringUtils.join(tokensArr, '&');
			}
		}
		return joinedTokens;
	}
	
	public void setFlex(Landmark landmark, String cc, String city) throws Exception {
    	String flexStr = landmark.getFlex();
    	JSONObject flex = null; 
    	if (flexStr != null) {
    		flex = new JSONObject(flexStr);
    	} else {
    		flex = new JSONObject();
    	}
    	if (StringUtils.isNotEmpty(cc) && !flex.has("cc")) {
    		flex.putOpt("cc", cc);
    	}
    	if (StringUtils.isNotEmpty(city) && !flex.has("city")) {
    		flex.putOpt("city", city);
    	}
    	landmark.setFlex(flex.toString());
    }
}
