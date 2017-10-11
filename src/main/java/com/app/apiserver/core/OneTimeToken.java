package com.app.apiserver.core;

/**
 * Aggregate for openstack hosts that groups together a list of hosts
 * that comprise a satellite location.
 * 
 * - has metadata that contains the "zone" and "latitude"/"longitude" for the satellite
 * 
 * @author smijar
 *
 */
public class OneTimeToken extends AppBaseEntity {
	
	public static final String REQUEST_TYPE_SHORTCUT_HTML ="shortcut_html";
	public static final String REQUEST_TYPE_MANIFEST_HTML ="manifest_html";
	public static final String REQUEST_TYPE_USER_CERTIFICATE ="user_certificate";
	public static final String REQUEST_TYPE_DEVICE_CERTIFICATE ="device_certificate";
	public static final String REQUEST_TYPE_RESET_USER_PASSWORD ="reset_password";
	
	String accountName;
	String launchId;
	
	String token;
	String baseToken;
	
	String userid;	
	String requestType;
	//List<String> manifestData = Lists.newArrayList();
	
	public OneTimeToken() { } // default constructor needed by json conversion code


	public String getAccountName() {
		return accountName;
	}


	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}


	public String getToken() {
		return token;
	}


	public void setToken(String token) {
		this.token = token;
	}


	public String getUserid() {
		return userid;
	}


	public void setUserid(String userid) {
		this.userid = userid;
	}

	public String getLaunchId() {
		return launchId;
	}


	public void setLaunchId(String launchid) {
		this.launchId = launchid;
	}


	public String getRequestType() {
		return requestType;
	}


	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}

/*
	public List<String> getManifestData() {
		return manifestData;
	}


	public void setManifestData(List<String> manifestData) {
		this.manifestData = manifestData;
	}

*/
	public String getBaseToken() {
		return baseToken;
	}


	public void setBaseToken(String baseToken) {
		this.baseToken = baseToken;
	}
}
