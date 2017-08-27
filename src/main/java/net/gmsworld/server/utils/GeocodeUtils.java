package net.gmsworld.server.utils;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

public class GeocodeUtils {
	
	private static final Logger logger = Logger.getLogger(GeocodeUtils.class.getName());

	public static double getLatitude(String latitudeString) {
        double latitude = 90.0;
        if (StringUtils.isNotEmpty(latitudeString)) {
            try {
                double l = Double.parseDouble(latitudeString);
                Validate.isTrue(!(l > 90.0 || l < -90.0), "Latitude must be in [-90, 90] but was ", l);
                latitude = MathUtils.normalizeE6(l);
            } finally {
            }
        }
        return latitude;
    }

    public static double getLongitude(String longitudeString) {
        double longitude = 180.0;
        if (StringUtils.isNotEmpty(longitudeString)) {
            try {
                double l = Double.parseDouble(longitudeString);
                Validate.isTrue(!(l > 180.0 || l < -180.0), "Longitude must be in [-180, 180] but was ", l);
                longitude = MathUtils.normalizeE6(l);
            } finally {
            }
        }
        return longitude;
    }
    
    public static boolean isNorthAmericaLocation(String latitude, String longitude) {
        boolean isNA = false;

        try {
            if (StringUtils.isNotEmpty(latitude) && StringUtils.isNotEmpty(longitude)) {
                double lat = Double.parseDouble(latitude);
                double lng = Double.parseDouble(longitude);

                //N 83.162102, E -52.233040
                //S 5.499550, W -167.276413

                Validate.isTrue(!(lat > 85.0 || lat < 5.0), "Latitude must be in [5, 85] but was ", lat);
                Validate.isTrue(!(lng < -170.0 || lng > -50.0), "Longitude must be in [-170, -50] but was ", lng);

                isNA = true;
            }
        } catch (Exception ex) {
            logger.log(Level.WARNING, ex.getMessage(), ex);
        }

        return isNA;
    }
    
    public static BoundingBox getBoundingBox(double lat, double lng, int radius) {
    	final double R = 6371; // earth radius in km 26.447521
    	BoundingBox bbox = new BoundingBox();
    	
    	bbox.west =   (lng - Math.toDegrees(radius / R / Math.cos(Math.toRadians(lat))));
    	bbox.east =    (lng + Math.toDegrees(radius / R / Math.cos(Math.toRadians(lat))));
    	bbox.north =   (lat + Math.toDegrees(radius / R));
    	bbox.south =   (lat - Math.toDegrees(radius / R));
    	
    	return bbox;
    }
}
