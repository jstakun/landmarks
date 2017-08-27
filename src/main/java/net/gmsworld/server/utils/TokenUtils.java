package net.gmsworld.server.utils;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;


/**
 *
 * @author jstakun
 */
public class TokenUtils {

    public static String generateToken() throws NoSuchAlgorithmException {
    	 SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");	
    	 //MessageDigest md = MessageDigest.getInstance("SHA-1");
    	 //md.digest(byte[]);
    	 return Long.toHexString(sr.nextLong()) + Long.toHexString(System.currentTimeMillis());
    }
}
