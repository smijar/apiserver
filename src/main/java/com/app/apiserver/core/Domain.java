package com.app.apiserver.core;

import java.util.List;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Transient;

import com.google.common.collect.Lists;
import com.wordnik.swagger.annotations.ApiModel;

public class Domain  extends AppBaseEntity {

	@NotNull
	private String name;
	
	private String description;

	@NotNull
	private String nodeId = "1";
	
	@Nullable
	@Transient
	private long domainUsersCount = -1; // count of how many users are in this domain

	private long domainUsersMaxCount = -1; // maximum users allowed within this domain, -1 means no limit

	// make this "1" for the root
	@NotNull
	private String parentEntityId = "";

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public long getDomainUsersCount() {
		return domainUsersCount;
	}

	public void setDomainUsersCount(long domainUsersCount) {
		this.domainUsersCount = domainUsersCount;
	}

	public long getDomainUsersMaxCount() {
		return domainUsersMaxCount;
	}

	public void setDomainUsersMaxCount(long domainUsersMaxCount) {
		this.domainUsersMaxCount = domainUsersMaxCount;
	}
}
