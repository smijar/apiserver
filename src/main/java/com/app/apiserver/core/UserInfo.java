package com.app.apiserver.core;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.MoreObjects;

/**
 * holds user info provided by user onboarding screen
 * 
 * @author smijar
 */
public class UserInfo extends AppBaseEntity {
	public static String NEW = "NEW"; // when first created
	public static String SUCCESS_SUBMITTED_FOR_ALLOCATION = "SUCCESSFULLY_SUBMITTED_FOR_ALLOCATION"; // when submitted for allocation suces
	public static String OUT_OF_CAPACITY = "OUT_OF_CAPACITY"; // DC was out of capacity when being allocated
	public static String ERROR_SEE_APP_MESSAGE = "ERROR_SEE_APP_MESSAGE"; // Error during app message processing, see it for details
	public static String ERROR = "ERROR"; // Error before submission to AppMessage Q, see here for details

	@NotNull
	private String name;

	@NotNull
	private String email;

	@NotNull
	private String company;

	@NotNull
	private String jobTitle;

	@NotNull
	private String personalZipCode;

	private String mgmtServerName;  // name in MgmtServerConfig

	// see statuses above
	@Nullable
	private String status = "NEW";
	
	@Nullable
	private String errorDetail = "";
	

	// corresponding appMessageId, that can hold more details of the status above
	@Nullable
	private String messageId = "";

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@JsonIgnore
	public String getSN() {
		return name;
	}
	
	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getJobTitle() {
		return jobTitle;
	}

	public void setJobTitle(String jobTitle) {
		this.jobTitle = jobTitle;
	}

	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPersonalZipCode() {
		return personalZipCode;
	}
	public void setPersonalZipCode(String personalZipCode) {
		this.personalZipCode = personalZipCode;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStatusDetail() {
		return errorDetail;
	}

	public void setErrorDetail(String errorDetail) {
		this.errorDetail = errorDetail;
	}

	public String getAppMessageId() {
		return messageId;
	}

	public void setAppMessageId(String appMessageId) {
		this.messageId = appMessageId;
	}

	public String getMgmtServerName() {
		return mgmtServerName;
	}

	public void setMgmtServerName(String mgmtServerName) {
		this.mgmtServerName = mgmtServerName;
	}

	public String toString() {
		return MoreObjects.toStringHelper(this.getClass())
				.add("name", name)
				.add("email", email)
				.add("mgmtServerName", mgmtServerName)
				.add("company", company)
				.add("title", jobTitle)
				.add("zipCode", personalZipCode)
				.add("status", status)
				.add("errorDetail", errorDetail)
				.add("appMessageId", messageId)
				.toString();
	}
}