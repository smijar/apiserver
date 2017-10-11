package com.app.apiserver.core;

import javax.validation.constraints.NotNull;

import com.google.common.base.MoreObjects;

/**
 * config for the mgmt server cluster
 * 
 * @author smijar
 */
public class MgmtServerConfig {
	private String name;
	private String host;
	private int port;
	
	@NotNull
	private String domainName; // domain under which to create users on management server
	@NotNull
	private String cloudConfigName; // cloudconfig under which to create virtual devices on management server
	@NotNull
	private String ldapConfigName; // ldapconfig to use when create users on management server
	@NotNull
	private String aggregateName;
	@NotNull
	private String baseVDImage;
	@NotNull
	private String flavor;
	@NotNull
	private String zipCode;
	@NotNull
	private int capacity;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	
	public String getDomainName() {
		return domainName;
	}
	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}
	public String getCloudConfigName() {
		return cloudConfigName;
	}
	public void setCloudConfigName(String cloudConfigName) {
		this.cloudConfigName = cloudConfigName;
	}
	public String getLdapConfigName() {
		return ldapConfigName;
	}
	public void setLdapConfigName(String ldapConfigName) {
		this.ldapConfigName = ldapConfigName;
	}
	public String getAggregateName() {
		return aggregateName;
	}
	public void setAggregateName(String aggregateName) {
		this.aggregateName = aggregateName;
	}
	/*
	public String getLdapAccountPrefix() {
		return ldapAccountPrefix;
	}
	public void setLdapAccountPrefix(String ldapAccountPrefix) {
		this.ldapAccountPrefix = ldapAccountPrefix;
	}
	public String getLdapAccountSuffix() {
		return ldapAccountSuffix;
	}
	public void setLdapAccountSuffix(String ldapAccountSuffix) {
		this.ldapAccountSuffix = ldapAccountSuffix;
	}*/
	public String getBaseVDImage() {
		return baseVDImage;
	}
	public void setBaseVDImage(String baseVDImage) {
		this.baseVDImage = baseVDImage;
	}
	public String getFlavor() {
		return flavor;
	}
	public void setFlavor(String flavor) {
		this.flavor = flavor;
	}
	public String getZipCode() {
		return zipCode;
	}
	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}
	public int getCapacity() {
		return capacity;
	}
	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}
	public String toString() {
		return MoreObjects.toStringHelper(this.getClass())
				.add("name", name)
				.add("host", host)
				.add("port", port)
				.add("cloudConfigName", cloudConfigName)
				.add("domainName", domainName)
				.add("ldapConfigName", ldapConfigName)
				.add("aggregateName", aggregateName)
				//.add("ldapAccountPrefix", ldapAccountPrefix)
				//.add("ldapAccountSuffix", ldapAccountSuffix)
				.add("flavor", flavor)
				.add("baseVDImage", baseVDImage)
				.add("zipCode", zipCode)
				.add("capacity", capacity)
				.toString();
	}
}
