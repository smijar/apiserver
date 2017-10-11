package com.app.apiserver.core;

import java.util.Collection;
import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.mongodb.morphia.annotations.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;

// Following are the different GUID attributes used by various different ldap/AD servers.
// We are currently supporting ONLY ldap or Active Directory servers for authentication

/*
Provider	Default GUID Attribute Name
WebLogic Authentication provider	orclguid
Oracle Internet Directory Authentication provider	orclguid
Oracle Virtual Directory Authentication provider	orclguid
Active Directory Authentication provider	objectguid
iPlanet Authentication provider	nsuniqueid
Novell Authentication provider	guid
Open LDAP Authentication provider	entryuuid
*/

/**
 * A base class used to hold the base attributes of a user account
 * @author smijar
 */
public class ExternalUserAccount extends AppBaseEntity {
	
	//public static final String LDAP_GUID_ATTR = "entryuuid";
	//public static final String AD_GUID_ATTR = "objectguid";
	
	@NotNull
	private String dn; 	// the distinguished name of the user in LDAP
	
	@Nullable
	private String guid; 	//the GUID of the user in local ldap or active directory
		
	private Map<String, String> attributes = Maps.newTreeMap(); // contains other important fields from LDAP like mail, CN or something else

	@Nullable
	@Transient
	@JsonIgnore
	private Collection<com.unboundid.ldap.sdk.Attribute> allAttributes = null;
	
	
	// this is not stored in the DB since its in the parent User object - ONLY used for import/other api purposes
	@Nullable
	@Transient
	private String vdUserId = null;
	
	//Following are used for creating a new user in LDAP
	@Nullable
	@Transient
	private String ldapConfigId;	
	
	// this is not stored in the DB since its in the parent User object - ONLY used during login to keep trac
	@Nullable
	@Transient
	private boolean pwdResetRequired = false;
	
	
	public ExternalUserAccount() {
		
	}

	public ExternalUserAccount(String dn) {
		this.dn = dn;
	}

	public String toString() {
        return MoreObjects.toStringHelper(this)
        		.add("dn", dn)
        		.add("attributes", attributes)
                .toString();
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}

	public String getDn() {
		return dn;
	}

	public void setDn(String dn) {
		this.dn = dn;
	}

	public String getVdUserId() {
		return vdUserId;
	}

	public void setVdUserId(String vdUserId) {
		this.vdUserId = vdUserId;
	}

	public Collection<com.unboundid.ldap.sdk.Attribute> getAllAttributes() {
		return allAttributes;
	}

	public void setAllAttributes(
			Collection<com.unboundid.ldap.sdk.Attribute> allAttributes) {
		this.allAttributes = allAttributes;
	}

	public String getLdapConfigId() {
		return ldapConfigId;
	}

	public void setLdapConfigId(String ldapConfigId) {
		this.ldapConfigId = ldapConfigId;
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}
	
	@JsonIgnore
	private static String prefixZeros(int value) {
	    if (value <= 0xF) {
	        StringBuilder sb = new StringBuilder("0");
	        sb.append(Integer.toHexString(value));
	 
	        return sb.toString();
	 
	    } else {
	        return Integer.toHexString(value);
	    }
	}
	
	// as per AD documentation, this is how we should construct a string form the byte array
	// [3] [2] [1] [0] - [5] [4] - [7] [6] - [8] [9] - [10] [11] [12] [13] [14] [15]
	// This API is used to convert the objectGUID returned by AD into a string so that we can store it in mongoDB
	// and also put it in the user's client certificate
	@JsonIgnore
	public static String getGuidFromByteArray(byte[] objectGUID) {
	    StringBuilder displayStr = new StringBuilder();
	    
	    displayStr.append(prefixZeros((int) objectGUID[3] & 0xFF));  // 0,1
	    displayStr.append(prefixZeros((int) objectGUID[2] & 0xFF));  // 2,3
	    displayStr.append(prefixZeros((int) objectGUID[1] & 0xFF));  // 4,5
	    displayStr.append(prefixZeros((int) objectGUID[0] & 0xFF));  // 6,7
	    displayStr.append("-");                                      // 8
	    displayStr.append(prefixZeros((int) objectGUID[5] & 0xFF));  // 9,10
	    displayStr.append(prefixZeros((int) objectGUID[4] & 0xFF));  // 11,12
	    displayStr.append("-");                                      // 13
	    displayStr.append(prefixZeros((int) objectGUID[7] & 0xFF));  // 14, 15
	    displayStr.append(prefixZeros((int) objectGUID[6] & 0xFF));  // 16, 17
	    displayStr.append("-");                                      // 18
	    displayStr.append(prefixZeros((int) objectGUID[8] & 0xFF));  // 19,20
	    displayStr.append(prefixZeros((int) objectGUID[9] & 0xFF));  // 21,22
	    displayStr.append("-");                                      // 23
	    displayStr.append(prefixZeros((int) objectGUID[10] & 0xFF)); // 24, 25
	    displayStr.append(prefixZeros((int) objectGUID[11] & 0xFF)); // 26, 27 
	    displayStr.append(prefixZeros((int) objectGUID[12] & 0xFF)); // 28, 29
	    displayStr.append(prefixZeros((int) objectGUID[13] & 0xFF)); // 30, 31
	    displayStr.append(prefixZeros((int) objectGUID[14] & 0xFF)); // 32, 33
	    displayStr.append(prefixZeros((int) objectGUID[15] & 0xFF)); // 34, 35
	 
	    return displayStr.toString();
	}
	
	@JsonIgnore
    public static byte[] getByteArrayFromGuid(String str)
    {
		byte guidBytes[] = str.getBytes();
        byte[] data = new byte[16];
        data[0] = (byte) ((Character.digit(guidBytes[6], 16) << 4)+ Character.digit(guidBytes[7], 16));
        data[1] = (byte) ((Character.digit(guidBytes[4], 16) << 4)+ Character.digit(guidBytes[5], 16));
        data[2] = (byte) ((Character.digit(guidBytes[2], 16) << 4)+ Character.digit(guidBytes[3], 16));
        data[3] = (byte) ((Character.digit(guidBytes[0], 16) << 4)+ Character.digit(guidBytes[1], 16));

        data[4] = (byte) ((Character.digit(guidBytes[11], 16) << 4)+ Character.digit(guidBytes[12], 16));
        data[5] = (byte) ((Character.digit(guidBytes[9], 16) << 4)+ Character.digit(guidBytes[10], 16));

        data[6] = (byte) ((Character.digit(guidBytes[16], 16) << 4)+ Character.digit(guidBytes[17], 16));
        data[7] = (byte) ((Character.digit(guidBytes[14], 16) << 4)+ Character.digit(guidBytes[15], 16));

        data[8] = (byte) ((Character.digit(guidBytes[19], 16) << 4)+ Character.digit(guidBytes[20], 16));
        data[9] = (byte) ((Character.digit(guidBytes[21], 16) << 4)+ Character.digit(guidBytes[22], 16));

        data[10] = (byte) ((Character.digit(guidBytes[24], 16) << 4)+ Character.digit(guidBytes[25], 16));
        data[11] = (byte) ((Character.digit(guidBytes[26], 16) << 4)+ Character.digit(guidBytes[27], 16));
        data[12] = (byte) ((Character.digit(guidBytes[28], 16) << 4)+ Character.digit(guidBytes[29], 16));
        data[13] = (byte) ((Character.digit(guidBytes[30], 16) << 4)+ Character.digit(guidBytes[31], 16));
        data[14] = (byte) ((Character.digit(guidBytes[32], 16) << 4)+ Character.digit(guidBytes[33], 16));
        data[15] = (byte) ((Character.digit(guidBytes[34], 16) << 4)+ Character.digit(guidBytes[35], 16));
        
        //UUID uuid = UUID.fromString(str);
        //ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        //bb.putLong(uuid.getMostSignificantBits());
        //bb.putLong(uuid.getLeastSignificantBits());
        //System.out.println("GUID length= " + str.length());

        return data;
    }

	public boolean isPwdResetRequired() {
		return pwdResetRequired;
	}

	public void setPwdResetRequired(boolean pwdResetRequired) {
		this.pwdResetRequired = pwdResetRequired;
	}
}