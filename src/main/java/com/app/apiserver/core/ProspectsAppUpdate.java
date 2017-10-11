package com.app.apiserver.core;

import java.util.Map;

import org.mongodb.morphia.annotations.Entity;

import jersey.repackaged.com.google.common.base.MoreObjects;

/**
 * updates Pardot with the action taken 
 * 
 * @author smijar
 *
 */
@Entity
public class ProspectsAppUpdate extends AppBaseEntity {

	private String email;
	private String action;
	
	private String status;
	private String messageId;
	
	private Map<String,String> payload;

	public static final String REGISTER_USER_ON_LANDING_PAGE = "REGISTER_USER_ON_LANDING_PAGE";
	public static final String REGISTER_USER_ON_PICK_APPS_PAGE = "REGISTER_USER_ON_PICK_APPS_PAGE";
	public static final String REGISTER_USER_CONFIRMED = "REGISTER_USER_VPHONE_CONFIRMED";
	public static final String REGISTER_USER_OUT_OF_CAPACITY = "REGISTER_USER_OUT_OF_CAPACITY";
	
	public static final String SUCCESSFULLY_UPDATED = "SUCCESSFULLY_UPDATED";
	public static final String ERROR_ON_UPDATE = "ERROR_ON_UPDATE_SEE_MESSAGE";

	public ProspectsAppUpdate() {
	}
	public ProspectsAppUpdate(String email, String action) {
		this.email = email;
		this.action = action;
	}
	public ProspectsAppUpdate(String email, String action, Map<String,String> payload) {
		this.email = email;
		this.action = action;
		this.payload = payload;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getMessageId() {
		return messageId;
	}
	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}
	public Map<String, String> getPayload() {
		return payload;
	}
	public void setPayload(Map<String, String> payload) {
		this.payload = payload;
	}
	public String toString() {
		return MoreObjects.toStringHelper(this.getClass())
				.add("email", email)
				.add("action", action)
				.add("status", status)
				.add("messageId", messageId)
				.add("payload", payload)
				.toString();
	}
}