package com.app.apiserver.core;

import javax.validation.constraints.NotNull;

import com.google.common.base.MoreObjects;

/**
 * config for Pardot API
 * 
 * @author smijar
 */
public class ProspectsAppConfig {
	@NotNull
	private String apiUrlPrefix = "https://pi.prospectsApp.com/api/";
	
	@NotNull
	private boolean enablePardot = false;
	
	@NotNull
	private String apiUserEmail;
	
	@NotNull
	private String apiPassword;
	
	@NotNull
	private String apiUserKey;

	// following are the various lists for Pardot
	@NotNull
	private int inviteListId;

	@NotNull
	private int landingPageListId;

	@NotNull
	private int outOfCapacityListId;

	@NotNull
	private int vPhoneConfirmedListId;
	
	@NotNull
	private int pickAppsListId;
	
	public ProspectsAppConfig() {
	}

	public String getApiUrlPrefix() {
		return apiUrlPrefix;
	}

	public void setApiUrlPrefix(String apiUrlPrefix) {
		this.apiUrlPrefix = apiUrlPrefix;
	}

	public String getApiUserEmail() {
		return apiUserEmail;
	}

	public void setApiUserEmail(String apiUserEmail) {
		this.apiUserEmail = apiUserEmail;
	}

	public String getApiPassword() {
		return apiPassword;
	}

	public void setApiPassword(String apiPassword) {
		this.apiPassword = apiPassword;
	}

	public String getApiUserKey() {
		return apiUserKey;
	}

	public void setApiUserKey(String apiUserKey) {
		this.apiUserKey = apiUserKey;
	}

	public boolean isEnableProspectsApp() {
		return enablePardot;
	}

	public void setEnablePardot(boolean enablePardot) {
		this.enablePardot = enablePardot;
	}

	public int getInviteListId() {
		return inviteListId;
	}

	public void setInviteListId(int inviteListId) {
		this.inviteListId = inviteListId;
	}

	public int getLandingPageListId() {
		return landingPageListId;
	}

	public void setLandingPageListId(int landingPageListId) {
		this.landingPageListId = landingPageListId;
	}

	public int getOutOfCapacityListId() {
		return outOfCapacityListId;
	}

	public void setOutOfCapacityListId(int outOfCapacityListId) {
		this.outOfCapacityListId = outOfCapacityListId;
	}

	public int getvPhoneConfirmedListId() {
		return vPhoneConfirmedListId;
	}

	public void setvPhoneConfirmedListId(int vPhoneConfirmedListId) {
		this.vPhoneConfirmedListId = vPhoneConfirmedListId;
	}

	public int getPickAppsListId() {
		return pickAppsListId;
	}

	public void setPickAppsListId(int pickAppsListId) {
		this.pickAppsListId = pickAppsListId;
	}

	public String toString() {
		return MoreObjects.toStringHelper(this.getClass())
				.add("enablePardot", enablePardot)
				.add("prospectsAppApiEmail", apiUserEmail)
				.add("apiUrlPrefix", apiUrlPrefix)
				.add("inviteListId", inviteListId)
				.add("pickedAppsListId", pickAppsListId)
				.add("landingPageListId", landingPageListId)
				.add("outOfCapacityListId", outOfCapacityListId)
				.add("vPhoneConfirmedListId", vPhoneConfirmedListId)
				.toString();
	}
}
