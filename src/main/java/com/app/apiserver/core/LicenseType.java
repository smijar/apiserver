package com.app.apiserver.core;

import java.util.List;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

/**
 * An enum that contains the list of license types that are used by the system
 * 
 *
 */
public enum LicenseType {
    NAMED("NAMED", "License is assigned to a user all the time."),
    CONCURRENT("CONCURRENT", "License is assigned to the user when he connects."),
    ;

    private String name;
    private String description;

    /**
     * constructor
     * 
     * @param name
     * @param description
     */
    private LicenseType(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * checks if the VDSessionStatus passed in is valid or not
     * 
     * @param policyType
     * @return
     */
	public boolean isVDSessionStatusValid(String vdSessionStatus) {
		return getVDSessionStatusForName(vdSessionStatus).isPresent();
	}

    /**
     * returns the policyType given a name from the list above 
     * 
     * @param name
     * @return
     */
    public Optional<LicenseType> getVDSessionStatusForName(String name) {
    	Optional<LicenseType> p = Optional.absent();

    	for (LicenseType s : values()) {
            if(s.getName().equals(name)) {
            	p = Optional.of(s);
            	break;
            }
        }

        return p;
    }
    
    public List<String> getVDSessionStatuses() {
    	List<String> policyTypes = Lists.newArrayList();
    	for(LicenseType s:values())
    		policyTypes.add(s.getName());
    	return policyTypes;
    }
    
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String toString() {
    	return this.getName();
    }
}
