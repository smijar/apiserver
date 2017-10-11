package com.app.apiserver.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/v1")
@Produces(MediaType.TEXT_PLAIN)
public class RootResource {
	public RootResource() {
		
	}

    @GET
    public String get() {
        return "Welcome to the ApiServer App!!";
    }
}