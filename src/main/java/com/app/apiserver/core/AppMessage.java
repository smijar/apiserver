package com.app.apiserver.core;

import javax.validation.constraints.NotNull;

import com.google.common.base.MoreObjects;

/**
 * the message class used for the in-app messaging
 * @author smijar
 *
 */
public class AppMessage extends AppBaseEntity {
	// MESSAGE STATES
	public static final String NEW = "NEW";
	public static final String RUNNING = "RUNNING";
	public static final String SUCCESS = "SUCCESS";
	public static final String ERROR = "ERROR";
	public static final String PENDING = "PENDING";
	
	// MESSAGE ACTIONS
	public static final String ALLOCATE_VD = "ALLOCATE_VD";
	
	// MESSAGE TYPES
	public static final String USER_INFO_TYPE = UserInfo.class.getSimpleName();
	public static final String UPDATE_PARDOT = "UPDATE_PARDOT";

	@NotNull
	//private String action = ALLOCATE_VD; // the default action to take for the message of type UserInfo
	private String type; // message type - normally the class of the entity
	private String entityId; // message pertaining to userInfo or some other entity
	private String optionalEntityDetail; // more detail about the entity for debugging purposes
	private String status = "NEW"; // status
	private String errorDetail = ""; // used for storing error detail

	
	public AppMessage() {
	}
	
	public AppMessage(String type, String entityId) {
		this.type = type;
		this.entityId = entityId;
	}

	public AppMessage(String type, String entityId, String optionalEntityDetail) {
		this.type = type;
		this.entityId = entityId;
		this.optionalEntityDetail = optionalEntityDetail;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	public String getOptionalEntityDetail() {
		return optionalEntityDetail;
	}

	public void setOptionalEntityDetail(String optionalEntityDetail) {
		this.optionalEntityDetail = optionalEntityDetail;
	}

	public String getDetail() {
		return errorDetail;
	}

	public void setDetail(String detail) {
		this.errorDetail = detail;
	}

	public String toString() {
		return MoreObjects.toStringHelper(AppMessage.class)
				.add("_id", getId())
				.add("entityId", entityId)
				.add("entityDetail", optionalEntityDetail)
				.add("type", type)
				.add("status", status)
				.toString();
	}
}
