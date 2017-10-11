package com.app.apiserver.core;

import java.util.Date;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.PrePersist;
import org.mongodb.morphia.annotations.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wordnik.swagger.annotations.ApiModelProperty;

public class AppBaseEntity {
    /**
     * The id of the object
     */
	@JsonIgnore // see getId() below
    private @Id ObjectId _id;

    private String createdBy = null;  // ID of user who created this entity
    
    private String updatedBy = null;  // ID of user who updated this entity

    @Transient 
    private String createdByName = null; // Name of user who created this entity
    
    @Transient 
    private String updatedByName = null; // Name of user who updated this entity
    
    @JsonIgnore // see getCreated() below
    private Date created = new Date();

    @JsonIgnore // see getCreated() below
    private Date lastModified = new Date();

    @PrePersist 
    @JsonIgnore
    void prePersist() { lastModified = new Date(); if(created==null) created = new Date();}

    @Transient 
    @JsonIgnore
    public static final String ALLOWED_CHARS = "^[a-zA-Z0-9 _.:/()\\-&\n{}\"\'=,*]*$";

    @Transient 
    @JsonIgnore
    public static final String ALLOWED_CHARS_FLAVOR = "^[a-zA-Z0-9 _.-]*$"; // this is a restriction by openstack
    
    @Transient 
    @JsonIgnore
    public static final Pattern ALLOWED_CHARS_PATTERN = Pattern.compile(ALLOWED_CHARS);
    
    public AppBaseEntity()  {}
	/**
	 * IMPORTANT - I have put the @JsonProperty here instead of in field above, to customize the serialization of the
	 * objectId to a string representation
	 * @return
	 */
	@JsonProperty("_id")
	@ApiModelProperty(value = "The id of the object in the Application database", required=true)
	public String getId() {
		if(_id == null)
			return null;
		else
			return _id.toString();
	}

	public void setId(String id) {
		if( !StringUtils.isBlank(id)) {
			this._id = new ObjectId(id);
		}
	}

	@JsonProperty
	public String getCreated() {
		return ISODateTimeFormat.dateTime().print(new DateTime(this.created));
	}

	@JsonIgnore
	public Date getCreatedAsDate() {
		return this.created;
	}
	
	
    public void setCreated(Date created) {
        this.created = created;
    }

    
	@JsonProperty
	public String getLastModified() {
		return ISODateTimeFormat.dateTime().print(new DateTime(this.lastModified));
	}

	@JsonIgnore
	public Date getLastModifiedAsDate() {
		return this.lastModified;
	}	
	
    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	public String getUpdatedBy() {
		return updatedBy;
	}
	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}
	public String getCreatedByName() {
		return createdByName;
	}
	public void setCreatedByName(String createdByName) {
		this.createdByName = createdByName;
	}
	public String getUpdatedByName() {
		return updatedByName;
	}
	public void setUpdatedByName(String updatedByName) {
		this.updatedByName = updatedByName;
	}
}
