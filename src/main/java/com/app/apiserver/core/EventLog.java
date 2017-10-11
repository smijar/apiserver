package com.app.apiserver.core;

import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.mongodb.morphia.annotations.CappedAt;
import org.mongodb.morphia.annotations.Entity;

import com.google.common.collect.Maps;

import jersey.repackaged.com.google.common.base.MoreObjects;

/**
 * the Event log for our side tracking of user activities and events
 * 
 * @author smijar
 */
@Entity(cap = @CappedAt(count=10000000))
public class EventLog extends AppBaseEntity {
	public static String USER_ON_LANDING_PAGE = "USER_ON_LANDING_PAGE";
	public static String USER_ON_PICK_APPS_PAGE = "USER_ON_PICK_APPS_PAGE";
	public static String USER_CONFIRMED = "USER_VPHONE_CONFIRMED";
	public static String USER_OUT_OF_CAPACITY = "USER_OUT_OF_CAPACITY";
	public static String USER_ALLOCATE_VPHONE_VD_START = "USER_ALLOCATE_VPHONE_VD_START";
	public static String USER_ERROR_ALLOCATE_VPHONE_VD_START = "USER_ERROR_ALLOCATE_VPHONE_VD_START";
	public static String USER_AUTHENTICATED = "USER_AUTHENTICATED";
	public static String USER_EMAIL_NOT_FOUND_DURING_VERIFY = "USER_EMAIL_NOT_FOUND_DURING_VERIFY";
	public static String ERROR_DURING_VERIFY = "ERROR_DURING_VERIFY";

	@Nullable
	private String email;

	@NotNull
	private String eventName;

	private Map<String,String> payload = Maps.newHashMap();
	public EventLog(String email, String eventName) {
		this.email = email;
		this.eventName = eventName;
	}

	public EventLog(String email, String eventName, Map<String,String> payload) {
		this.email = email;
		this.eventName = eventName;
		this.payload = payload;
	}

	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getEventName() {
		return eventName;
	}
	public void setEventName(String eventName) {
		this.eventName = eventName;
	}
	public Map<String, String> getPayload() {
		return payload;
	}
	public void setPayload(Map<String, String> payload) {
		this.payload = payload;
	}

	public String toString() {
		String str = "";

		if(email!=null)
			str = "User:"+email+", event:"+eventName;
		if(payload!=null && !payload.isEmpty())
			str = str + payload.toString();
		return str;

//		return MoreObjects.toStringHelper(this.getClass())
//				.add("email", email)
//				.add("eventName", eventName)
//				.add("payload", payload)
//				.toString();
	}
}
