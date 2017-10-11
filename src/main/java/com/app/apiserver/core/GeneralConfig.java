package com.app.apiserver.core;

import com.google.common.base.MoreObjects;

/**
 * contains general configuration constants
 * 
 * @author smijar
 */
public class GeneralConfig {
	private int imageCreationTimeoutSeconds = 120; // after which image is assumed to have failed
	private int imageCreationCheckWaitIntervalSeconds = 10; // interval to check for image creation status in loop
	private int imageRetrievalSeconds = 15; // images list cache is refreshed every n seconds
	private int numMsgThreadPoolSize = 2; // number of threads used by messaging
	private String zipLatLongFile = "conf/us_zip_lat_long.csv";
	private boolean runBackgroundTasks = true;
	
	private boolean useGrecaptcha = false;
	private String grecaptchaKey = "6LeJrCIUAAAAAIguC3_mUT574glhCYe4Jtbov96P"; // key to check grecaptcha
	private String grecaptchaServer = "https://www.google.com/recaptcha/api/siteverify";
	
	
		
	public String getZipLatLongFile() {
		return zipLatLongFile;
	}

	public void setZipLatLongFile(String zipLatLongFile) {
		this.zipLatLongFile = zipLatLongFile;
	}

	public int getImageCreationTimeoutSeconds() {
		return imageCreationTimeoutSeconds;
	}

	public void setImageCreationTimeoutSeconds(int imageCreationTimeoutSeconds) {
		this.imageCreationTimeoutSeconds = imageCreationTimeoutSeconds;
	}

	public int getImageRetrievalSeconds() {
		return imageRetrievalSeconds;
	}

	public void setImageRetrievalSeconds(int imageRetrievalSeconds) {
		this.imageRetrievalSeconds = imageRetrievalSeconds;
	}

	public int getImageCreationCheckWaitIntervalSeconds() {
		return imageCreationCheckWaitIntervalSeconds;
	}

	public void setImageCreationCheckWaitIntervalSeconds(int imageCreationCheckWaitIntervalSeconds) {
		this.imageCreationCheckWaitIntervalSeconds = imageCreationCheckWaitIntervalSeconds;
	}

	public int getNumMsgThreadPoolSize() {
		return numMsgThreadPoolSize;
	}

	public void setNumMsgThreadPoolSize(int numMsgThreadPoolSize) {
		this.numMsgThreadPoolSize = numMsgThreadPoolSize;
	}

	public boolean canRunBackgroundTasks() {
		return runBackgroundTasks;
	}

	public void setRunBackgroundTasks(boolean runBackgroundTasks) {
		this.runBackgroundTasks = runBackgroundTasks;
	}

    public boolean isUseGrecaptcha() {
		return useGrecaptcha;
	}

	public void setUseGrecaptcha(boolean useGrecaptcha) {
		this.useGrecaptcha = useGrecaptcha;
	}

	public String getGrecaptchaKey() {
		return grecaptchaKey;
	}

	public void setGrecaptchaKey(String grecaptchaKey) {
		this.grecaptchaKey = grecaptchaKey;
	}

	public String getGrecaptchaServer() {
		return grecaptchaServer;
	}

	public void setGrecaptchaServer(String grecaptchaServer) {
		this.grecaptchaServer = grecaptchaServer;
	}

	public String toString() {
		return MoreObjects.toStringHelper(this.getClass())
				.add("zipLatLongFile", zipLatLongFile)
				.add("imageCreationTimeoutSeconds", 120)
				.add("imageRetrievalSeconds", imageRetrievalSeconds)
				.add("imageCreationCheckWaitIntervalSeconds", imageCreationCheckWaitIntervalSeconds)
				.add("numMsgThreadPoolSize", numMsgThreadPoolSize)
				.add("runBackgroundTasks", runBackgroundTasks)
				.toString();
	}
}