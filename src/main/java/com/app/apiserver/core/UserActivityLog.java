package com.app.apiserver.core;

import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.mongodb.morphia.annotations.CappedAt;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Transient;

/**
 * the message status object keeping track of user activity i.e. session start/stop/reqeust of auth token...
 * 
 * Capped collection since this could become a huge table very quickly
 * 
 * @author smijar
 */
@Entity(cap = @CappedAt(count=100000))
public class UserActivityLog extends AppBaseEntity {
	
	public static String ACTIVITY_AUTHTOKEN = "AUTHTOKEN"; 
	public static String ACTIVITY_NOTIFICATIONTOKEN = "NOTIFICATIONTOKEN"; 
	public static String ACTIVITY_USERACTIVE = "ACTIVE";
	public static String ACTIVITY_USERINACTIVE = "INACTIVE"; 
	public static String ACTIVITY_USERTERMINATED = "TERMINATED"; 
	public static String ACTIVITY_APPUSAGE = "APPUSAGE"; 

	@NotNull
	private String userId;

	@NotNull
	private String domainId;
	
    private String clientCertId = null; // The ID of the record in DCCertificate table
	
	@NotNull
	private String activity;

	@Nullable
	private String description; // any string

	@Nullable
	private Map<String, String> attributes = null; // key value attribtues of the session. Set for activity of type  AUTHTOKEN and NOTIFICATION

	@Nullable
	private ClientSessionData sessionData = null; // data for the established session. Set only for activity of type "INACTIVE"
	
	
	@Transient
	private ExternalUserAccount userAccount;


	public UserActivityLog() { }
	
	public UserActivityLog(String userId, String domainId, String activity, String description) {
		this.userId = userId;
		this.activity = activity;
		this.description = description;
		this.domainId = domainId;
	}

	public UserActivityLog(String userId, String domainId, String activity, String description, Map<String, String> attributes) {
		this.userId = userId;
		this.activity = activity;
		this.description = description;
		this.attributes = attributes;
		this.domainId = domainId;
	}

	public UserActivityLog(String userId, String domainId, String activity, String description, ClientSessionData sessionData) {
		this.userId = userId;
		this.activity = activity;
		this.description = description;
		this.sessionData = sessionData;
		this.domainId = domainId;
	}

	public String getUserId() {
		return userId;
	}


	public void setUserId(String userId) {
		this.userId = userId;
	}


	public String getActivity() {
		return activity;
	}


	public void setActivity(String activity) {
		this.activity = activity;
	}


	public String getDescription() {
		return description;
	}


	public void setDescription(String description) {
		this.description = description;
	}


	public Map<String, String> getAttributes() {
		return attributes;
	}


	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}


	public ExternalUserAccount getUserAccount() {
		return userAccount;
	}


	public void setUserAccount(ExternalUserAccount userAccount) {
		this.userAccount = userAccount;
	}


	public String getDomainId() {
		return domainId;
	}


	public void setDomainId(String domainId) {
		this.domainId = domainId;
	}


	public ClientSessionData getSessionData() {
		return sessionData;
	}


	public void setSessionData(ClientSessionData sessionData) {
		this.sessionData = sessionData;
	}


	public String getClientCertId() {
		return clientCertId;
	}


	public void setClientCertId(String clientCertId) {
		this.clientCertId = clientCertId;
	}

}
