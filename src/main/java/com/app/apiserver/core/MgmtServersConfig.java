package com.app.apiserver.core;

import java.util.List;

import jersey.repackaged.com.google.common.base.MoreObjects;

/**
 * config for the list of mgmt server clusters
 * 
 * @author smijar
 *
 */
public class MgmtServersConfig {
	private String apiCertificate;
	private String apiCertificatePassword;
	private String apiBasicAuthUsername;
	private String apiBasicAuthPassword;

	private List<MgmtServerConfig> mgmtServerConfigs;

	public String getApiCertificate() {
		return apiCertificate;
	}
	public void setApiCertificate(String apiCertificate) {
		this.apiCertificate = apiCertificate;
	}
	public String getApiCertificatePassword() {
		return apiCertificatePassword;
	}
	public void setApiCertificatePassword(String apiCertificatePassword) {
		this.apiCertificatePassword = apiCertificatePassword;
	}
	public String getApiBasicAuthUsername() {
		return apiBasicAuthUsername;
	}
	public void setApiBasicAuthUsername(String apiBasicAuthUsername) {
		this.apiBasicAuthUsername = apiBasicAuthUsername;
	}
	public String getApiBasicAuthPassword() {
		return apiBasicAuthPassword;
	}
	public void setApiBasicAuthPassword(String apiBasicAuthPassword) {
		this.apiBasicAuthPassword = apiBasicAuthPassword;
	}
	public List<MgmtServerConfig> getMgmtServerConfigs() {
		return mgmtServerConfigs;
	}
	public void setMgmtServerConfigs(List<MgmtServerConfig> mgmtServerConfigs) {
		this.mgmtServerConfigs = mgmtServerConfigs;
	}

	public String toString() {
		return MoreObjects.toStringHelper(this.getClass())
				.add("apiCertificate", apiCertificate)
				.add("mgmtServerConfigs", mgmtServerConfigs)
				.toString();
	}
}
