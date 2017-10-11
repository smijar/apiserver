package com.app.apiserver.core;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class AppMongoConfig  {
 
    @JsonProperty
    @NotEmpty
    private String host = "localhost";
 
    @JsonProperty
    @Min(1)
    @Max(65535)
    private int port = 27017;
 
    @JsonProperty
    @NotEmpty
    private String dbName = "";
    
    @JsonProperty
    private boolean reachable = false;

    @Inject
    public AppMongoConfig() {
    	// defaults
    	this.host = "localhost";
    	this.port = 27017;
    	this.dbName = "apiserver";
    	this.reachable = true;
    }

    public String toString() {
    	return MoreObjects.toStringHelper(this)
    				.add("host", this.host)
    				.add("port", this.port)
    				.add("dbName", this.dbName)
    				.toString();
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

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public boolean isReachable() {
		return reachable;
	}

	public void setReachable(boolean reachable) {
		this.reachable = reachable;
	}
}
