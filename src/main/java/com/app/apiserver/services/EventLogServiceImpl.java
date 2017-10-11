package com.app.apiserver.services;

import javax.inject.Inject;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.dao.BasicDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.app.apiserver.core.EventLog;
import com.google.inject.Provider;
import com.google.inject.Singleton;

/**
 * used for simple logging of events like those for users so that its easier for see the actions that a user takes or
 * the system takes for a user
 * 
 * @author smijar
 *
 */
@Singleton
public class EventLogServiceImpl extends BasicDAO<EventLog, ObjectId> implements EventLogService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private Provider<AppMongoService> mongoService;

	@Inject
	public EventLogServiceImpl(Provider<AppMongoService> mongoService) {
		super(mongoService.get().getMongo(), mongoService.get().getMorphia(), mongoService.get().getDBName());
		this.mongoService = mongoService;
	}
	
    public Key<EventLog> save(final EventLog eventLog) {
    	logger.info(eventLog.toString());
        return super.save(eventLog);
    }
}