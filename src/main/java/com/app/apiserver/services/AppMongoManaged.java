package com.app.apiserver.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.dropwizard.lifecycle.Managed;

@Singleton
public class AppMongoManaged implements Managed {
	Logger logger = LoggerFactory.getLogger(AppMongoManaged.class);
    private AppMongoService mongoService;
 
    @Inject
    public AppMongoManaged(AppMongoService mongoService) {
        this.mongoService = mongoService;
    }
 
	@Override
    public void start() throws Exception {
		logger.info("Opening connection to MongoDB");
		mongoService.connect();
		
        LocationService locationService = AppMain.injector.getInstance(LocationService.class);
        if( locationService == null ) {
        	logger.info( "Unable to find location service");
        } else {
    		// When the service starts, load all zip codes in DB.
    		// We need them in DB to validate and make sure user entered the correct zip code in DB.
            locationService.loadZipCodesIntoDB(true);
        }
		
		
    }
 
    @Override
    public void stop() throws Exception {
    	logger.info("Closing connection to MongoDB");
    	mongoService.close();
    }
}
