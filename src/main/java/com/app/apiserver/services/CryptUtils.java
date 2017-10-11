package com.app.apiserver.services;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.shiro.codec.Base64;

public class CryptUtils {

	private static final int SALT_LENGTH = 4;

	public static String generateSSHA(byte[] password)
	        throws NoSuchAlgorithmException {
	    SecureRandom secureRandom = new SecureRandom();
	    byte[] salt = new byte[SALT_LENGTH];
	    secureRandom.nextBytes(salt);

	    MessageDigest crypt = MessageDigest.getInstance("SHA-1");
	    crypt.reset();
	    crypt.update(password);
	    crypt.update(salt);
	    byte[] hash = crypt.digest();

	    byte[] hashPlusSalt = new byte[hash.length + salt.length];
	    System.arraycopy(hash, 0, hashPlusSalt, 0, hash.length);
	    System.arraycopy(salt, 0, hashPlusSalt, hash.length, salt.length);

	    return new StringBuilder().append("{SSHA}")
	            .append(Base64.encode(hashPlusSalt))
	            .toString();
	}
	
	/**
	 * @param start start of range (inclusive)
	 * @param end end of range (exclusive)
	 * @param excludes numbers to exclude (= numbers you do not want)
	 * @return the random number within start-end but not one of excludes
	 */
	public static int getRandomNumberInRange(int min, int max) {

		if (min >= max) {
			throw new IllegalArgumentException("max must be greater than min");
		}

		Random r = new Random();
		return r.nextInt((max - min) + 1) + min;
	}

	public static void sleepForSeconds(int seconds) {
		try{TimeUnit.SECONDS.sleep(seconds);}catch(Exception e) {}
	}

}
