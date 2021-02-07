package net.gmsworld.server.layers;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

import javax.naming.NamingException;
import javax.persistence.EntityManager;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.ConfigurationManager;
import net.gmsworld.server.persistence.Landmark;
import net.gmsworld.server.utils.JSONUtils;
import net.gmsworld.server.utils.MathUtils;
import net.gmsworld.server.utils.NumberUtils;
import net.gmsworld.server.utils.ServiceLocator;
import net.gmsworld.server.utils.memcache.CacheUtil;
import net.gmsworld.server.utils.persistence.EMF;
import net.gmsworld.server.utils.persistence.LandmarkPersistenceUtils;
import net.gmsworld.server.utils.xml.XMLUtils;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.jstakun.gms.android.landmarks.ExtendedLandmark;
import com.jstakun.gms.android.landmarks.LandmarkFactory;
import com.openlapi.AddressInfo;
import com.openlapi.QualifiedCoordinates;

/**
 *
 * @author jstakun
 */
public class GMSUtils extends LayerHelper {

    private static final String landingPage = "showLandmark.do?key=";
    private static final Date migrationDate; //2012-08-25
    private String layer;
    
    static {
        Calendar c = Calendar.getInstance();
        c.set(2012, 8, 25, 0, 0, 0);
        migrationDate = c.getTime();
    };

    @Override
    protected JSONObject processRequest(double latitude, double longitude, String query, int radius, int version, int limit, int stringLimit, String layer, String flexString2) throws JSONException, UnsupportedEncodingException, NamingException {
        this.layer = layer;
    	String key = getCacheKey(getClass(), "processRequest", latitude, longitude, query, radius, version, limit, stringLimit, layer, flexString2);
        JSONObject json = null;
        String output = CacheUtil.getString(key);
        if (output == null) {
            List<Landmark> landmarkList = null;    	
            LandmarkPersistenceUtils landmarkPersistenceUtils = (LandmarkPersistenceUtils) ServiceLocator.getInstance().getService("bean/LandmarkPersistenceUtils");
            EntityManager em = EMF.getEntityManager();
            
            if (StringUtils.isNotEmpty(query)) {
        		landmarkList = landmarkPersistenceUtils.searchLandmarks(query, limit, em);
        	} else {
        		landmarkList = landmarkPersistenceUtils.selectLandmarksByCoordsAndLayer(layer, latitude, longitude, radius, limit, em);
        	}
            
        	em.close();
            json = createCustomJSonLandmarkList(landmarkList, version, stringLimit);
            if (!landmarkList.isEmpty()) {
                CacheUtil.put(key, json.toString());
                logger.log(Level.INFO, "Adding GMS landmark list to cache with key {0}", key);
            }
        } else {
            json = new JSONObject(output);
            logger.log(Level.INFO, "Reading GMS landmark list from cache with key {0}", key);
        }

        return json;
    }

    protected String processRequest(double latitudeMin, double longitudeMin, double latitudeMax, double longitudeMax, int version, int limit, int stringLimit, String layer, String format) throws JSONException, UnsupportedEncodingException, NamingException {
    	this.layer = layer;
    	String key = getCacheKey(GMSUtils.class, "processRequest", (latitudeMin + latitudeMax)/2, (longitudeMin + longitudeMax)/2, null, 0, version, limit, stringLimit, layer, format);

        String output = CacheUtil.getString(key);
        if (output == null) {
        	
        	double latitude = (latitudeMin + latitudeMax) / 2;
            double longitude = (longitudeMin + longitudeMax) / 2;
            int radius = (int)(NumberUtils.distanceInKilometer(latitudeMin, latitudeMax, longitudeMin, longitudeMax) * 1000 / 2);            
            LandmarkPersistenceUtils landmarkPersistenceUtils = (LandmarkPersistenceUtils) ServiceLocator.getInstance().getService("bean/LandmarkPersistenceUtils");
            EntityManager em = EMF.getEntityManager();
            List<Landmark> landmarkList = landmarkPersistenceUtils.selectLandmarksByCoordsAndLayer(layer, latitude, longitude, radius, limit, em);
            em.close();        	
            if (format.equals("kml")) {
                output = XMLUtils.createKmlLandmarkList(landmarkList, landingPage);
            } else if (format.equals("json")) {
                output = createCustomJSonLandmarkList(landmarkList, version, stringLimit).toString();
            } else {
                output = XMLUtils.createCustomXmlLandmarkList(landmarkList, landingPage);
            }
            if (StringUtils.isNotEmpty(output) && !landmarkList.isEmpty()) {
                CacheUtil.put(key, output);
                logger.log(Level.INFO, "Adding GMS landmark list to cache with key {0}", key);
            }
        } else {
            logger.log(Level.INFO, "Reading GMS landmark list from cache with key {0}", key);
        }

        return output;
    }

    private static JSONObject createCustomJSonLandmarkList(List<Landmark> landmarkList, int version, int stringLimit) throws JSONException {
        List<Map<String, Object>> jsonArray = new ArrayList<Map<String, Object>>();

        for (Iterator<Landmark> iter = landmarkList.iterator(); iter.hasNext();) {
            Landmark landmark = iter.next();
            Map<String, Object> jsonObject = new HashMap<String, Object>();
            jsonObject.put("name", landmark.getName());
            jsonObject.put("lat", MathUtils.normalizeE6(landmark.getLatitude()));
            jsonObject.put("lng", MathUtils.normalizeE6(landmark.getLongitude()));

            String url = landingPage + landmark.getId();

            if (version >= 2) {
                if (version >= 4) { 
                    String hash = landmark.getHash();
                    if (landmark.getCreationDate().after(migrationDate) && !StringUtils.isEmpty(hash)) { //2012-08-25
                        url = hash;
                        jsonObject.put("urlType", 1);
                    } else {
                        jsonObject.put("urlType", 0);
                    }
                }
                jsonObject.put("url", url);
                Map<String, String> desc = new HashMap<String, String>();
                JSONUtils.putOptValue(desc, "description", landmark.getDescription(), stringLimit, false);
                if (version == 2) {
                    DateFormat df = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
                    String date = df.format(landmark.getCreationDate());
                    desc.put("creationDate", date);
                } else if (version >= 3) {
                    desc.put("creationDate", Long.toString(landmark.getCreationDate().getTime()));
                }
                jsonObject.put("desc", desc);
            } else {
                jsonObject.put("desc", url);
            }

            jsonArray.add(jsonObject);
        }

        JSONObject json = new JSONObject().put("ResultSet", jsonArray);
        return json;
    }
    
    @Override
    protected List<ExtendedLandmark> loadLandmarks(double latitude, double longitude, String query, int radius, int version, int limit, int stringLimit, String layer, String flexString2, Locale locale, boolean useCache) throws Exception {
    	this.layer = layer;
    	List<ExtendedLandmark> landmarks = new ArrayList<ExtendedLandmark>();
        List<Landmark> landmarkList = null;	
        LandmarkPersistenceUtils landmarkPersistenceUtils = (LandmarkPersistenceUtils) ServiceLocator.getInstance().getService("bean/LandmarkPersistenceUtils");
        EntityManager em = EMF.getEntityManager();
        		
        if (StringUtils.isNotEmpty(query)) {
        		landmarkList = landmarkPersistenceUtils.searchLandmarks(query, limit, em);
        } else {
        		landmarkList = landmarkPersistenceUtils.selectLandmarksByCoordsAndLayer(layer, latitude, longitude, radius, limit, em);
        }
      
        em.close();
        
        if (!landmarkList.isEmpty()) {
            	//Collection<Landmark> results = Collections2.filter(landmarkList, new QueryPredicate(query));      
            	landmarks.addAll(Collections2.transform(landmarkList, new LandmarkTransformFunction(layer, locale)));
        }

        logger.log(Level.INFO, "Returning " + landmarks.size() + " landmarks for layer " + layer + " ...");

        return landmarks;
	}
	
    public String getLayerName() {
		if (layer == null) {
			return Commons.LM_SERVER_LAYER;
		} else {
			return layer;
		}
    }
	
	private class LandmarkTransformFunction implements Function<Landmark, ExtendedLandmark> {
		private Locale locale = null;
		
		public LandmarkTransformFunction(String layer, Locale locale) {
			this.locale = locale;
		}
		
		@Override
		public ExtendedLandmark apply(Landmark source) {
			final QualifiedCoordinates qc = new QualifiedCoordinates(source.getLatitude(), source.getLongitude(), 0f, 0f, 0f);
	    	final String name = source.getName();
	    	final long creationDate = source.getCreationDate().getTime();	
	    	final String hash = source.getHash();
	    	String url = null;	      
	    	if (StringUtils.isNotEmpty(hash)) { 
	            url = "https://bit.ly/" + hash;
	        } else {
	        	url =  ConfigurationManager.SERVER_URL + "showLandmark/" + source.getId();
	        }
	    	Map<String, String> tokens = new HashMap<String, String>();
	    	tokens.put("description", source.getDescription());	    	
	    	ExtendedLandmark target = LandmarkFactory.getLandmark(name, null, qc, source.getLayer(), new AddressInfo(), creationDate, null);
	    	target.setUrl(url);
	    	target.setDescription(JSONUtils.buildLandmarkDesc(target, tokens, locale));
	    	return target;
		}
	}
}
