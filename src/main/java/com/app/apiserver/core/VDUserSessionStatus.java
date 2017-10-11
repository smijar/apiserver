package com.app.apiserver.core;

import java.util.List;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

/**
 * An enum that contains the list of VDSession Statuses that are used by the system
 * ACTIVE - when a user's client connects to the spice server, the spice server tells the management server that the session started
 * INACTIVE - when the user's client session disconnected from the spice server, the spice server tells the management server that the session ended gracefully
 * TERMINATED - if a network error/VD crash occurs, the management server marks the session as TERMINATED
 *
 *
 * IMPORTANT - the management system will periodically check the health of the Virtual Device, and if the virtual device status is NOT ACTIVE, it will mark the session
 * 		as having been TERMINATED by the virtual device
 * 
 * @author smijar
 *
 */
public enum VDUserSessionStatus {
    ACTIVE("ACTIVE", "Client is connected to the virtual device"),
    INACTIVE("INACTIVE", "Client has gracefully disconnected from the virtual device"),
    TERMINATED("TERMINATED", "Client session was abnormally disconnected due to abnormal virtual device status")
    ;

    private String name;
    private String description;

    /**
     * constructor
     * 
     * @param name
     * @param description
     */
    private VDUserSessionStatus(String name, String description) {
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
    public Optional<VDUserSessionStatus> getVDSessionStatusForName(String name) {
    	Optional<VDUserSessionStatus> p = Optional.absent();

    	for (VDUserSessionStatus s : values()) {
            if(s.getName().equals(name)) {
            	p = Optional.of(s);
            	break;
            }
        }

        return p;
    }
    
    public List<String> getVDSessionStatuses() {
    	List<String> policyTypes = Lists.newArrayList();
    	for(VDUserSessionStatus s:values())
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
