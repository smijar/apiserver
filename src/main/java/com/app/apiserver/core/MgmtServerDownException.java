package com.app.apiserver.core;

import javax.ws.rs.WebApplicationException;

public class MgmtServerDownException extends WebApplicationException {

    public MgmtServerDownException(Throwable e) {
        super(e);
    }

    /**
     * 
     */
    private static final long serialVersionUID = -5491601638011016390L;

}
