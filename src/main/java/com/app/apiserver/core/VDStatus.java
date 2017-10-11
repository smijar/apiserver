package com.app.apiserver.core;

import java.util.List;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

/**
 * An enum that contains the list of policy types that are used by the system
 * 
 * @author smijar
 *
 */
public enum VDStatus {
    UNALLOCATED("UNALLOCATED", "Unallocated Virtual Device"),
    SUBMITTED("SUBMITTED", "Submitted for Allocation"),
    PROCESSING("PROCESSING", "Being processed for Allocation"),
    ACTIVE("ACTIVE", "Active Virtual Device"),
    SHUTOFF("SHUTOFF", "Shutoff"),
    SUSPENDED("SUSPENDED", "Suspended Virtual Device"),
    PAUSED("PAUSED", "Paused Virtual Device"),
    BUILD("BUILD", "Virtual Device is being created"),
    ERROR("ERROR", "Virtual Device is in ERROR status within Openstack"),
    UNKNOWN("UNKNOWN", "Some unknown error in figuring out the status of the virtual device"),
    ;

    private String name;
    private String description;

    /**
     * constructor
     * 
     * @param name
     * @param description
     */
    private VDStatus(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * checks if the VDStatus passed in is valid or not
     * 
     * @param policyType
     * @return
     */
	public boolean isVDStatusValid(String policyType) {
		return getVDStatusForName(policyType).isPresent();
	}

    /**
     * returns the policyType given a name from the list above 
     * 
     * @param name
     * @return
     */
    public Optional<VDStatus> getVDStatusForName(String name) {
    	Optional<VDStatus> p = Optional.absent();

    	for (VDStatus s : values()) {
            if(s.getName().equals(name)) {
            	p = Optional.of(s);
            	break;
            }
        }

        return p;
    }
    
    public List<String> getVDStatuses() {
    	List<String> policyTypes = Lists.newArrayList();
    	for(VDStatus s:values()) {
			policyTypes.add(s.getName());
		}
    	return policyTypes;
    }
    
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override
	public String toString() {
    	return this.getName();
    }
}
