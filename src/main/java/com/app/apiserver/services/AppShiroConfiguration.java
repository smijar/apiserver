package com.app.apiserver.services;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import jersey.repackaged.com.google.common.base.MoreObjects;

public class AppShiroConfiguration {

    @NotNull
    @JsonProperty("iniConfigs")
    private String[] iniConfigs;

    @JsonProperty("filterUrlPattern")
    private String filterUrlPattern = "/*";

    public String[] iniConfigs() {
        return iniConfigs;
    }

    public String filterUrlPattern() {
        return filterUrlPattern;
    }
    
    public String toString() {
    	return MoreObjects.toStringHelper(this.getClass())
    				.add("iniConfigs", iniConfigs)
    				.toString();
    }
}
