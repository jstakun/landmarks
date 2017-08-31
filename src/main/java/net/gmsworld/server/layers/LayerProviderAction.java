package net.gmsworld.server.layers;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.persistence.Layer;
import net.gmsworld.server.struts.JSonDataAction;
import net.gmsworld.server.utils.BoundingBox;
import net.gmsworld.server.utils.GeocodeUtils;
import net.gmsworld.server.utils.JBossThreadProvider;
import net.gmsworld.server.utils.NumberUtils;
import net.gmsworld.server.utils.ServiceLocator;
import net.gmsworld.server.utils.StringUtil;
import net.gmsworld.server.utils.memcache.JBossCacheProvider;
import net.gmsworld.server.utils.persistence.LayerPersistenceUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.interceptor.ParameterAware;
import org.apache.struts2.interceptor.ServletRequestAware;

import com.jstakun.gms.android.landmarks.ExtendedLandmark;
import com.opensymphony.xwork2.ActionSupport;

public class LayerProviderAction extends ActionSupport implements ParameterAware, ServletRequestAware {

	private static final Logger logger = Logger.getLogger(LayerProviderAction.class.getName());
	private static final long serialVersionUID = 1L;
	private Map<String, String[]> parameters;
	private HttpServletRequest request;
	
	public LayerProviderAction() {
		super();
		LayerHelperFactory.getInstance().setCacheProvider(new JBossCacheProvider());
		LayerHelperFactory.getInstance().setThreadProvider(new JBossThreadProvider());
	}
	   
	@Override
	public void setServletRequest(HttpServletRequest request) {
		this.request = request;
	}

	@Override
	public void setParameters(Map<String, String[]> parameters) {
		this.parameters = parameters;	
	}
	
	public String executeNamedLayer() {
		String layer = null;
		String uri = request.getRequestURI();
		
        if (StringUtils.contains(uri, "facebookProvider")) {
        	layer = Commons.FACEBOOK_LAYER;
        } else if (StringUtils.contains(uri, "youTubeProvider")) {
        	layer = Commons.YOUTUBE_LAYER;
        } else if (StringUtils.contains(uri, "webcamProvider")) {
        	layer = Commons.WEBCAM_LAYER;
        } else if (StringUtils.contains(uri, "yelpProvider")) {
        	layer = Commons.YELP_LAYER;
        } else if (StringUtils.contains(uri, "twitterProvider")) {
        	layer = Commons.TWITTER_LAYER;
        } else if (StringUtils.contains(uri, "hotelsProvider")) {
        	layer = Commons.HOTELS_LAYER;
        } else if (StringUtils.contains(uri, "googlePlacesProvider")) {
        	layer = Commons.GOOGLE_PLACES_LAYER;
        } else if (StringUtils.contains(uri, "foursquareProvider")) {
        	layer = Commons.FOURSQUARE_LAYER;
        } else if (StringUtils.contains(uri, "foursquareMerchant")) {
        	layer = Commons.FOURSQUARE_MERCHANT_LAYER;
        } else if (StringUtils.contains(uri, "atmProvider")) {
        	layer = Commons.MC_ATM_LAYER;
        } else if (StringUtils.contains(uri, "lastfmProvider")) {
        	layer = Commons.LASTFM_LAYER;
        } else if (StringUtils.contains(uri, "meetupProvider")) {
        	layer = Commons.MEETUP_LAYER;
        } else if (StringUtils.contains(uri, "eventfulProvider")) {
        	layer = Commons.EVENTFUL_LAYER;
        } else if (StringUtils.contains(uri, "grouponProvider")) {
        	layer = Commons.GROUPON_LAYER;
        } else if (StringUtils.contains(uri, "expediaProvider")) {
        	layer = Commons.EXPEDIA_LAYER;
        } else if (StringUtils.contains(uri, "couponsProvider")) {
        	layer = Commons.COUPONS_LAYER;
        } else if (StringUtils.contains(uri, "flickrProvider")) {
        	layer = Commons.FLICKR_LAYER;
        } else if (StringUtils.contains(uri, "freebaseProvider")) {
        	layer = Commons.FREEBASE_LAYER;
        } else if (StringUtils.contains(uri, "instagramProvider")) {
        	layer = Commons.INSTAGRAM_LAYER;
        } else if (StringUtils.contains(uri, "picasaProvider")) {
        	layer = Commons.PICASA_LAYER;
        } else if (StringUtils.contains(uri, "panoramio2Provider")) {
        	layer = Commons.PANORAMIO_LAYER;
        } else if (StringUtils.contains(uri, "geonamesProvider")) {
        	layer = Commons.WIKIPEDIA_LAYER;
        } else if (StringUtils.contains(uri, "osmProvider")) {
        	String amenity = StringUtil.getStringParam(request.getParameter("amenity"), "atm");
        	if (StringUtils.equals(amenity, "taxi")) {
        		layer = Commons.OSM_TAXI_LAYER;
        	} else if (StringUtils.equals(amenity, "parking")) {
        		layer = Commons.OSM_PARKING_LAYER;
        	} else {
        		layer = Commons.OSM_ATM_LAYER;
        	}
        } else if (StringUtils.contains(uri, "search")) {
        	layer = "search";
        }
        
        if (layer != null) {
        	return execute(layer);
        } else { //Wrong or not implemented layer: Foursquare Merchants
        	addActionError("Wrong or unimplemented layer: " + uri);
    		logger.log(Level.WARNING, "Wrong or unimplemented layer: " + uri); 
        	return "error";
        }
	}
	
	public String execute() {
		String layer = getParameter("layer");
        return execute(layer);
	}
	
	public String executeLocalLayers() {
		String layer = getParameter("layer");
		if (layer != null) {
			int limit = NumberUtils.getInt(getParameter("limit"), 30);
			return executeLocalLayers(layer, limit);
		} else {
        	addActionError("Local layer name must be provided!");
    		logger.log(Level.WARNING, "Local layer name must be provided!"); 
        	return "error";
        }
	}
	
	private String execute(String layer)
	{
		 int limit = NumberUtils.getInt(getParameter("limit"), 30);
         int dealLimit = NumberUtils.getInt(getParameter("dealLimit"), 300);
          
         Locale l = request.getLocale();
         String locale = StringUtil.getLanguage(l.getLanguage() + "_" + l.getCountry(), "en_US", 5);
         String language;
         if (getParameter("lang") != null) {
             language = StringUtil.getLanguage(getParameter("lang"), "en", 2);
         } else if (getParameter("language") != null) {
             language = StringUtil.getLanguage(getParameter("language"), "en", 2);
         } else {
             language = StringUtil.getLanguage(l.getLanguage(), "en", 2);
         }
    
         if (layer == null) {
        	addActionError("No layer specified!");
        	logger.log(Level.WARNING, "No layer specified");
        	return "error"; 
         } else if (StringUtils.equals(layer, Commons.HOTELS_LAYER)) {
        	return executeLayer(Commons.HOTELS_LAYER, limit, language, null, 1000);
         } else if (StringUtils.equals(layer, Commons.FACEBOOK_LAYER)) {
        	 String token = null;
             if (StringUtils.isNotEmpty(getParameter("token"))) {
                 try {
                	 token = URLDecoder.decode(getParameter("token"), "UTF-8");
                 } catch (UnsupportedEncodingException e) {
                	 logger.log(Level.SEVERE, e.getMessage(), e);
                 }
             } 
             return executeLayer(Commons.FACEBOOK_LAYER, limit, token, null, 1000);	 
         } else if (StringUtils.equals(layer, Commons.FOURSQUARE_LAYER)) {
        	 return executeLayer(Commons.FOURSQUARE_LAYER, limit, "checkin", language, 1000);	
         } else if (StringUtils.equals(layer, Commons.YELP_LAYER)) {
        	 int deals = NumberUtils.getInt(request.getHeader("X-GMS-AppId"), 0);
             String hasDeals = "false";
             if (deals == 1) {
                 hasDeals = "true";
             }
             return executeLayer(Commons.YELP_LAYER, limit, hasDeals, language, 1000);	
         } else if (StringUtils.equals(layer, Commons.GOOGLE_PLACES_LAYER)) {
        	 return executeLayer(Commons.GOOGLE_PLACES_LAYER, limit, language, null, 1000);	
         } else if (StringUtils.equals(layer, Commons.TWITTER_LAYER)) {
        	 return executeLayer(Commons.TWITTER_LAYER, limit, language, null, 1000);	
         } else if (StringUtils.equals(layer, Commons.LASTFM_LAYER)) {
        	 return executeLayer(Commons.LASTFM_LAYER, limit, null, null, 1);	
         } else if (StringUtils.equals(layer, Commons.MEETUP_LAYER)) {
        	 return executeLayer(Commons.MEETUP_LAYER, limit, null, null, 1);	
         } else if (StringUtils.equals(layer, Commons.EVENTFUL_LAYER)) {
        	 return executeLayer(Commons.EVENTFUL_LAYER, limit, null, null, 1);	
         } else if (StringUtils.equals(layer, Commons.FOURSQUARE_MERCHANT_LAYER)) {
        	 String token = null;
             if (StringUtils.isNotEmpty(getParameter("token"))) {
            	 try {
            		 token = URLDecoder.decode(getParameter("token"), "UTF-8");
            	 } catch (UnsupportedEncodingException e) {
                	 logger.log(Level.SEVERE, e.getMessage(), e);
                 }	 
             }
             if (StringUtils.isEmpty(token)) {
                 token = Commons.getProperty(Property.FS_OAUTH_TOKEN);
             }
             String categoryid = getParameter("categoryid");
             return executeLayer(Commons.FOURSQUARE_MERCHANT_LAYER, limit, token, categoryid, 1000);	
         } else if (StringUtils.equals(layer,  Commons.COUPONS_LAYER)) {
        	 if (GeocodeUtils.isNorthAmericaLocation(getParameter("latitude"), getParameter("longitude"))) {
        		 String categoryid = request.getParameter("categoryid");
        		 return executeLayer(Commons.COUPONS_LAYER, dealLimit, categoryid, language, 1);
        	 } else {
        		 logger.log(Level.INFO, "Only North America region is supported for layer Coupons");
                 return chainResult(new ArrayList<ExtendedLandmark>());
        	 }
         } else if (StringUtils.equals(layer, Commons.GROUPON_LAYER)) {
        	 if (GeocodeUtils.isNorthAmericaLocation(getParameter("latitude"), getParameter("longitude"))) {
        		 String categoryid = request.getParameter("categoryid");
        		 return executeLayer(Commons.GROUPON_LAYER, dealLimit, categoryid, null, 1);
        	 } else {
        		 logger.log(Level.INFO, "Only North America region is supported for layer Groupon");
                 return chainResult(new ArrayList<ExtendedLandmark>());
        	 }
         } else if (StringUtils.equals(layer, Commons.EXPEDIA_LAYER)) {
        	 return executeLayer(Commons.EXPEDIA_LAYER, limit, locale, null, 1);	
         } else if (StringUtils.equals(layer, Commons.WIKIPEDIA_LAYER)) {
        	 return executeLayer(Commons.WIKIPEDIA_LAYER, limit, language, null, 1);	
         } else if (StringUtils.equals(layer, Commons.FREEBASE_LAYER)) {
        	 return executeLayer(Commons.FREEBASE_LAYER, limit, language, null, 1);	
         } else if (StringUtils.equals(layer, Commons.INSTAGRAM_LAYER)) {
        	 return executeLayer(Commons.INSTAGRAM_LAYER, limit, language, null, 1000);	
         } else if (StringUtils.equals(layer, Commons.FLICKR_LAYER)) {
        	 return executeLayer(Commons.FLICKR_LAYER, limit, null, null, 1000);	
         } else if (StringUtils.equals(layer, Commons.PICASA_LAYER)) {
        	 BoundingBox bb = getBoundingBox();
        	 String bbox = null;
        	 if (bb != null) {
        		bbox = StringUtil.formatCoordE6(bb.west) + "," + StringUtil.formatCoordE6(bb.south) + "," +
                        StringUtil.formatCoordE6(bb.east) + "," + StringUtil.formatCoordE6(bb.north); 
        	 }
        	 return executeLayer(Commons.PICASA_LAYER, limit, bbox, null, 1);	
         } else if (StringUtils.equals(layer, Commons.PANORAMIO_LAYER)) {
        	 BoundingBox bb = getBoundingBox();
        	 String bbox = null;
        	 if (bb != null) {
        		bbox = "minx=" + StringUtil.formatCoordE6(bb.west) + "&miny=" + StringUtil.formatCoordE6(bb.south) + 
        			   "&maxx=" + StringUtil.formatCoordE6(bb.east) + "&maxy=" + StringUtil.formatCoordE6(bb.north);
        	 }			
        	 return executeLayer(Commons.PANORAMIO_LAYER, limit, bbox, null, 1);	
         } else if (StringUtils.equals(layer, Commons.YOUTUBE_LAYER)) {
        	 return executeLayer(Commons.YOUTUBE_LAYER, limit, null, null, 1);	
         } else if (StringUtils.equals(layer, Commons.WEBCAM_LAYER)) {
        	 return executeLayer(Commons.WEBCAM_LAYER, limit, null, null, 1);	
         } else if (StringUtils.equals(layer, Commons.MC_ATM_LAYER)) {
        	 return executeLayer(Commons.MC_ATM_LAYER, limit, null, null, 1);	
         } else if (StringUtils.endsWithAny(layer, new String[]{Commons.OSM_ATM_LAYER, Commons.OSM_PARKING_LAYER, Commons.OSM_TAXI_LAYER})) {
        	 BoundingBox bb = getBoundingBox();
        	 String amenity = null;
        	 String helper = null;
        	 if (StringUtils.equals(layer, Commons.OSM_PARKING_LAYER)) {
        		 amenity = "atm";
            	 helper = Commons.OSM_ATM_LAYER;
        	 } else if (StringUtils.equals(layer, Commons.OSM_PARKING_LAYER)) {
        		amenity = "parking";
        		helper = Commons.OSM_PARKING_LAYER;
        	 } else if (StringUtils.equals(layer, Commons.OSM_TAXI_LAYER)) {
        		amenity = "taxi";
        		helper = Commons.OSM_TAXI_LAYER;
        	 }
         	 if (StringUtils.isNotEmpty(amenity) && StringUtils.isNotEmpty(helper)) {
         		 String bbox = StringUtil.formatCoordE6(bb.west) + "," + StringUtil.formatCoordE6(bb.south) + "," + 
         				StringUtil.formatCoordE6(bb.east) + "," + StringUtil.formatCoordE6(bb.north);         	
         		 return executeLayer(helper, limit, amenity, bbox, 1);       	 
         	 } else {
         		logger.log(Level.WARNING, "Wrong osm layer selected " +  layer + "!");
         		return chainResult(new ArrayList<ExtendedLandmark>());
         	 }
         } else if (StringUtils.equals(layer, "search")) {
        	 String token = null;
             if (StringUtils.isNotEmpty(getParameter("token"))) {
                 try {
                	 token = URLDecoder.decode(getParameter("token"), "UTF-8");
                 } catch (UnsupportedEncodingException e) {
                	 logger.log(Level.SEVERE, e.getMessage(), e); 
                 }
             } 
        	 String flexString = "0";
             if (StringUtils.isNotEmpty(getParameter("deals"))) {
                 flexString = "1";
             }
             flexString += "_" + NumberUtils.getInt(getParameter("geocode"), 0);
             flexString += "_" + dealLimit;
             return executeLayer(Commons.SEARCH_LAYER, limit, flexString, token, 1);
         } else {
        	return executeLocalLayers(layer, limit);
        }     
    }

	private String executeLocalLayers(String layer, int limit) {
		Layer gmsLayer = null; 
		try {
			LayerPersistenceUtils layerPeristenceUtils = (LayerPersistenceUtils) ServiceLocator.getInstance().getService(
					"java:global/ROOT/LayerPersistenceUtils!net.gmsworld.server.utils.persistence.LayerPersistenceUtils");
			gmsLayer = layerPeristenceUtils.findByName(layer);
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
		if (gmsLayer != null) {
			return executeLayer(Commons.LM_SERVER_LAYER, limit, layer, null, 1000);
		} else {
			addActionError("Wrong or not implemented layer: " + layer);
			logger.log(Level.WARNING, "Wrong or not implemented layer: " + layer);
		    return "error";
		}
	}

	private String getParameter(String key) {
		if (parameters.containsKey(key)) {
			return parameters.get(key)[0];
		} else if (request.getAttribute("layerForm") != null) {
			Map<String, String> layerForm = (Map<String, String>) request.getAttribute("layerForm");
			return layerForm.get(key);
		} else {
			return null;
		}
	}
	
	private String executeLayer(String layerName, int limit, String flex, String flex2, int radius_multiplier) {
		if (isEmptyAny("latitude", "longitude") && isEmptyAny("lat", "lng")) {
            addActionError("Missing required parameter latitude/longitude  or lat/lng");
            logger.log(Level.SEVERE, "Missing required parameter latitude/longitude or lat/lng");
            return "error";
        } else {
        	int version = NumberUtils.getVersion(getParameter("version"), 1);
   		 
   		    double latitude;
            if (getParameter("lat") != null) {
                latitude = GeocodeUtils.getLatitude(getParameter("lat"));
            } else if (getParameter("latitude") != null) {
                latitude = GeocodeUtils.getLatitude(getParameter("latitude"));
            } else {
            	latitude = GeocodeUtils.getLatitude(getParameter("latitudeMin"));
            }

            double longitude;
            if (getParameter("lng") != null) {
                longitude = GeocodeUtils.getLongitude(getParameter("lng"));
            } else if (getParameter("longitude") != null) {
                longitude = GeocodeUtils.getLongitude(getParameter("longitude"));
            } else {
            	longitude = GeocodeUtils.getLongitude(getParameter("longitudeMin"));
            }

            int radius = NumberUtils.getRadius(getParameter("radius"), 3, 100);
         	
            int stringLimit = StringUtil.getStringLengthLimit(getParameter("display"));
        	
       	 	try {
       	 		LayerHelper layer = LayerHelperFactory.getInstance().getByName(layerName);
       	 		if (layer != null) {
       	 			List<ExtendedLandmark> landmarks = layer.processBinaryRequest(latitude, longitude, null, radius * radius_multiplier, version, limit, stringLimit, flex, flex2, request.getLocale(), false);
       	 			return chainResult(landmarks);
       	 		} else {
       	 			String errorMessage = "No layer helper found for layer " + layerName;
       	 			logger.log(Level.SEVERE, errorMessage);
       	 			addActionError(errorMessage);
       	 			return "error";
       	 		}
       	 	} catch (Exception e) {
       		 	logger.log(Level.SEVERE, e.getMessage(), e);
       		 	addActionError(e.getMessage());
                return "error";
       	 	}
        }
	}
	
	private boolean isEmptyAny(String... params) {
        for (String p : params) {
            if (StringUtils.isEmpty(getParameter(p))) {
                logger.log(Level.SEVERE, "Missing required parameter {0}", p);
                return true;
            }
        }
        return false;
    }

	private BoundingBox getBoundingBox() {
		if (! isEmptyAny("latitude", "longitude")) {
			int radius = NumberUtils.getRadius(getParameter("radius"), 3, 100);
			return GeocodeUtils.getBoundingBox(GeocodeUtils.getLatitude(getParameter("latitude")), GeocodeUtils.getLongitude(getParameter("longitude")), radius);
		} else {
			return null;
		}
	}
	
	public String executeFSCheckins() {
		if (isEmptyAny("latitude", "longitude", "radius", "token")) {
            addActionError("Missing required parameter latitude, longitude, radius or token");
            logger.log(Level.WARNING, "Missing required parameter latitude, longitude, radius or token");
            return "error";
        } else {
        	int version = NumberUtils.getVersion(getParameter("version"), 1);
        	int limit = NumberUtils.getInt(getParameter("limit"), 30);
        	int stringLimit = StringUtil.getStringLengthLimit(getParameter("display"));
        	
        	double latitude;
        	if (getParameter("lat") != null) {
        		latitude = GeocodeUtils.getLatitude(getParameter("lat"));
        	} else if (getParameter("latitude") != null) {
        		latitude = GeocodeUtils.getLatitude(getParameter("latitude"));
        	} else {
        		latitude = GeocodeUtils.getLatitude(getParameter("latitudeMin"));
        	}

        	double longitude;
        	if (getParameter("lng") != null) {
        		longitude = GeocodeUtils.getLongitude(getParameter("lng"));
        	} else if (getParameter("longitude") != null) {
        		longitude = GeocodeUtils.getLongitude(getParameter("longitude"));
        	} else {
        		longitude = GeocodeUtils.getLongitude(getParameter("longitudeMin"));
        	}
        	
        	//String language;
            //if (getParameter("lang") != null) {
            //    language = StringUtil.getLanguage(getParameter("lang"), "en", 2);
            //} else if (getParameter("language") != null) {
            //    language = StringUtil.getLanguage(getParameter("language"), "en", 2);
            //} else {
            //    language = StringUtil.getLanguage(request.getLocale().getLanguage(), "en", 2);
            //}
        
        	try {
        		String token = URLDecoder.decode(request.getParameter("token"), "UTF-8");
        		List<ExtendedLandmark> landmarks = ((FoursquareUtils)LayerHelperFactory.getInstance().getByName(Commons.FOURSQUARE_LAYER)).getFriendsCheckinsToLandmarks(latitude, longitude, limit, stringLimit, version, token, request.getLocale(), false);
        		return chainResult(landmarks);
        	} catch (Exception e) {
        		logger.log(Level.SEVERE, e.getMessage(), e);
        		addActionError(e.getMessage());
        		return "error";
        	}
        }
	}
	
	public String executeFSRecommended() {
		if (isEmptyAny("latitude", "longitude", "radius","token")) {
            addActionError("Missing required parameter latitude, longitude, radius or token");
            logger.log(Level.WARNING, "Missing required parameter latitude, longitude, radius or token");
            return "error";
        } else {
        	int version = NumberUtils.getVersion(getParameter("version"), 1);
        	int limit = NumberUtils.getInt(getParameter("limit"), 30);
        	int radius = NumberUtils.getRadius(getParameter("radius"), 3, 100);
        	int stringLimit = StringUtil.getStringLengthLimit(getParameter("display"));
         	
        	double latitude;
        	if (getParameter("lat") != null) {
        		latitude = GeocodeUtils.getLatitude(getParameter("lat"));
        	} else if (getParameter("latitude") != null) {
        		latitude = GeocodeUtils.getLatitude(getParameter("latitude"));
        	} else {
        		latitude = GeocodeUtils.getLatitude(getParameter("latitudeMin"));
        	}

        	double longitude;
        	if (getParameter("lng") != null) {
        		longitude = GeocodeUtils.getLongitude(getParameter("lng"));
        	} else if (getParameter("longitude") != null) {
        		longitude = GeocodeUtils.getLongitude(getParameter("longitude"));
        	} else {
        		longitude = GeocodeUtils.getLongitude(getParameter("longitudeMin"));
        	}
        	
        	//String language;
            //if (getParameter("lang") != null) {
            //    language = StringUtil.getLanguage(getParameter("lang"), "en", 2);
            //} else if (getParameter("language") != null) {
            //    language = StringUtil.getLanguage(getParameter("language"), "en", 2);
            //} else {
            //    language = StringUtil.getLanguage(request.getLocale().getLanguage(), "en", 2);
            //}
        
        	try {
        		String token = URLDecoder.decode(request.getParameter("token"), "UTF-8");
        		List<ExtendedLandmark> landmarks = ((FoursquareUtils)LayerHelperFactory.getInstance().getByName(Commons.FOURSQUARE_LAYER)).exploreVenuesToLandmark(latitude, longitude, null, radius * 1000, limit, stringLimit, version, token, request.getLocale(), false);
        		return chainResult(landmarks);
        	} catch (Exception e) {
        		logger.log(Level.SEVERE, e.getMessage(), e);
        		addActionError(e.getMessage());
        		return "error";
        	}
        }
	}
	
	public String executeTWFriends() {
		if (isEmptyAny("token", "secret")) {
            addActionError("Missing required parameter token or secret");
            logger.log(Level.WARNING, "Missing required parameter token or secret");
            return "error";
        } else {
        	try {
        		String token = URLDecoder.decode(request.getParameter("token"), "UTF-8");
        		String secret = URLDecoder.decode(request.getParameter("secret"), "UTF-8");
        		List<ExtendedLandmark> landmarks = ((TwitterUtils)LayerHelperFactory.getInstance().getByName(Commons.TWITTER_LAYER)).getFriendsStatuses(token, secret, request.getLocale(), false);
        		return chainResult(landmarks);
        	} catch (Exception e) {
        		logger.log(Level.SEVERE, e.getMessage(), e);
        		addActionError(e.getMessage());
        		return "error";
        	}
        }
	}	
	
	public String executeFBPhotos() {
		if (isEmptyAny("latitude","longitude","token")) {
			addActionError("Missing required parameter latitude, longitude or token");
            logger.log(Level.WARNING, "Missing required parameter latitude, longitude or token");
            return "error";
		} else {
			int version = NumberUtils.getVersion(getParameter("version"), 1);
        	int limit = NumberUtils.getInt(getParameter("limit"), 30);
        	int stringLimit = StringUtil.getStringLengthLimit(getParameter("display"));
        	try {
        		String token = URLDecoder.decode(request.getParameter("token"), "UTF-8");
        		List<ExtendedLandmark> landmarks = ((FacebookUtils)LayerHelperFactory.getInstance().getByName(Commons.FACEBOOK_LAYER)).getMyPhotos(version, limit, stringLimit, token, request.getLocale(), false);
        		return chainResult(landmarks);
        	} catch (Exception e) {
        		logger.log(Level.SEVERE, e.getMessage(), e);
        		addActionError(e.getMessage());
        		return "error";
        	} 
		}
	}
	
	public String executeFBCheckins() {
		if (isEmptyAny("latitude","longitude","token")) {
			addActionError("Missing required parameter latitude, longitude or token");
            logger.log(Level.WARNING, "Missing required parameter latitude, longitude or token");
            return "error";
		} else {
			int version = NumberUtils.getVersion(getParameter("version"), 1);
        	int limit = NumberUtils.getInt(getParameter("limit"), 30);
        	int stringLimit = StringUtil.getStringLengthLimit(getParameter("display"));
        	try {
        		String token = URLDecoder.decode(request.getParameter("token"), "UTF-8");
        		List<ExtendedLandmark> landmarks = ((FacebookUtils)LayerHelperFactory.getInstance().getByName(Commons.FACEBOOK_LAYER)).getMyPlaces(version, limit, stringLimit, token, request.getLocale(), false);
        		return chainResult(landmarks);
        	} catch (Exception e) {
        		logger.log(Level.SEVERE, e.getMessage(), e);
        		addActionError(e.getMessage());
        		return "error";
        	} 
		}
	}
	
	public String executeFBTagged() {
		if (isEmptyAny("latitude","longitude","token")) {
			addActionError("Missing required parameter latitude, longitude or token");
            logger.log(Level.WARNING, "Missing required parameter latitude, longitude or token");
            return "error";
		} else {
			int version = NumberUtils.getVersion(getParameter("version"), 1);
        	int limit = NumberUtils.getInt(getParameter("limit"), 30);
        	int stringLimit = StringUtil.getStringLengthLimit(getParameter("display"));
        	try {
        		String token = URLDecoder.decode(request.getParameter("token"), "UTF-8");
        		List<ExtendedLandmark> landmarks = ((FacebookUtils)LayerHelperFactory.getInstance().getByName(Commons.FACEBOOK_LAYER)).getMyTaggedPlaces(version, limit, stringLimit, token, request.getLocale(), false);
        		return chainResult(landmarks);
        	} catch (Exception e) {
        		logger.log(Level.SEVERE, e.getMessage(), e);
        		addActionError(e.getMessage());
        		return "error";
        	} 
		}
	}
	
	
	private String chainResult(List<ExtendedLandmark> landmarks) {
		String format = StringUtil.getStringParam(getParameter("format"), "json");
    	if (format.equals("json")) {
			request.setAttribute(JSonDataAction.JSON_OUTPUT, landmarks);
			return "json";
		} else {
			request.setAttribute(DeflateDataAction.DEFLATE_OUTPUT, landmarks);
			return "deflate";
		}
	}
}
