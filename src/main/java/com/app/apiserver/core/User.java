package com.app.apiserver.core;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

import jersey.repackaged.com.google.common.base.MoreObjects;

/**
 * This class encapsulates a virtual device user.  This user will have attributes that are imported from a directory server
 * and are stored in the attributesMap.
 *
 * @author smijar
 *
 */
@Entity
@ApiModel(value="User", description="Model for the User object.")
public class User extends AppBaseEntity {

	private List<OneTimeToken> oneTimeTokens;

	public User() { }
	/**
	 * from LDAP
	 */
	@NotNull
	private ExternalUserAccount externalUserAccount = new ExternalUserAccount();

	/**
	 * whether the user is enabled or not
	 */
	private boolean enabled = true;

	private String disabledReason; // if user is disabled ( enabled flag above is set to true ), the reason why user is diabled.

	/**
	 * the domainId to which the user belongs
	 */
	@NotNull
	private String domainId;

	/**
	 * The LDAP Config Id using which we retrieved the external user account
	 * this in combination with the queryObj below provides the full namespace and query used to retrieve this user
	 */
	@NotNull
	private String ldapConfigId;

	/**
	 * the ldap query object that was used to import the user(s) from the ldap directory server
	 */
	private String ldapQueryObjId;


	@NotNull
	private Set<String> roles ;


	/**
	 * the type of the user
	 */
	@NotNull
	@Pattern(regexp = ALLOWED_CHARS)
	private String userType = UserType.END_USER.getName();


	@Nullable
	@Transient
	private String userSessionStatus = VDUserSessionStatus.INACTIVE.getName(); // the vd user session status

    @JsonIgnore
    @Nullable
	@Transient
    private Date userSessionStatusUpdateTimeStamp;

	@Transient
	@Nullable
	@ApiModelProperty(value = "Name of user property which needs to be updated. Required if updating a user.", required=false)
	private String updateField = null; // the field which needs to be updated for this user. Used only on Updates.


	@Nullable
	@ApiModelProperty(value = "The type of license i.e. named or concurrent that is assigned to the user.", required=true)
	private String userLicenseType = null; // the license type for a user.


	public ExternalUserAccount getExternalUserAccount() {
		return externalUserAccount;
	}
	public void setExternalUserAccount(ExternalUserAccount externalUserAccount) {
		this.externalUserAccount = externalUserAccount;
	}
	public String getDomainId() {
		return domainId;
	}
	public void setDomainId(String domainId) {
		this.domainId = domainId;
	}
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public String getLdapConfigId() {
		return ldapConfigId;
	}
	public void setLdapConfigId(String ldapConfigId) {
		this.ldapConfigId = ldapConfigId;
	}
	public String getLdapQueryObjId() {
		return ldapQueryObjId;
	}
	public void setLdapQueryObjId(String ldapQueryObjId) {
		this.ldapQueryObjId = ldapQueryObjId;
	}
	public String getUserType() {
		return userType;
	}
	public void setUserType(String userType) {
		this.userType = userType;
	}
    public Set<String> getRoles() {
        return roles;
    }
    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
   
	@JsonIgnore
	public String getName() {

		if( getExternalUserAccount() != null) {
			return getExternalUserAccount().getDn();
		}
		return "";
	}

	public String getUserSessionStatus() {
		return userSessionStatus;
	}
	public void setUserSessionStatus(String userSessionStatus) {
		this.userSessionStatus = userSessionStatus;
	}

	@JsonProperty
	@ApiModelProperty(value = "Time when user session status was last updated", required=false)
	public String getUserSessionStatusUpdateTimeStamp() {
		if( userSessionStatusUpdateTimeStamp != null ) {
			return ISODateTimeFormat.dateTime().print(new DateTime(this.userSessionStatusUpdateTimeStamp));
		}
		return "";
	}

	@JsonIgnore
	public Date getUserSessionStatusUpdateTimeStampAsDate() {
		return userSessionStatusUpdateTimeStamp;
	}

	public void setUserSessionStatusUpdateTimeStamp(Date userSessionStatusUpdateTimeStamp) {
		this.userSessionStatusUpdateTimeStamp = userSessionStatusUpdateTimeStamp;
	}

	public String getUpdateField() {
		return updateField;
	}
	public void setUpdateField(String updateField) {
		this.updateField = updateField;
	}
	public String getUserLicenseType() {
		return userLicenseType;
	}
	public void setUserLicenseType(String userLicenseType) {
		this.userLicenseType = userLicenseType;
	}

	public List<OneTimeToken> getOneTimeTokens() {
		return oneTimeTokens;
	}
	public void setOneTimeTokens(List<OneTimeToken> oneTimeTokens) {
		this.oneTimeTokens = oneTimeTokens;
	}
	public String getDisabledReason() {
		return disabledReason;
	}
	public void setDisabledReason(String disabledReason) {
		this.disabledReason = disabledReason;
	}
	public String toString() {
		return MoreObjects.toStringHelper(User.class)
				.add("id", getId())
				.add("dn", externalUserAccount.getDn())
				.add("ldapConfigId", ldapConfigId)
				.add("domainId", domainId)
				.toString();
	}
}
