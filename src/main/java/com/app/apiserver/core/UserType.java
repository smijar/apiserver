package com.app.apiserver.core;

import com.google.common.base.Optional;

/**
 * An enum that contains the list of user types
 * 
 * @author epowell
 *
 */
public enum UserType {
    END_USER("END_USER", "An End User with a virtual device"),
    SHARED_DEVICE_USER("SHARED_DEVICE_USER", "An End User that shares a physical device"),
    ;

    private String name;
    private String description;

    private UserType(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * checks if the UserType passed in is valid or not
     * 
     * @param policyType
     * @return
     */
	public boolean isUserTypeValid(String policyType) {
		return getUserTypeForName(policyType).isPresent();
	}

    /**
     * returns the policyType given a name from the list above 
     * 
     * @param name
     * @return
     */
    public Optional<UserType> getUserTypeForName(String name) {
    	Optional<UserType> p = Optional.absent();

    	for (UserType s : values()) {
            if(s.getName().equals(name)) {
            	p = Optional.of(s);
            	break;
            }
        }

        return p;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
    	return name + ":" + description;
    }
}
