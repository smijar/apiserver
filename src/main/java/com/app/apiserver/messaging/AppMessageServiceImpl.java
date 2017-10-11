package com.app.apiserver.messaging;

import java.util.Date;
import java.util.Map;

import org.bson.types.ObjectId;
import org.mongodb.morphia.dao.BasicDAO;
import org.mongodb.morphia.query.UpdateOperations;
import org.mongodb.morphia.query.UpdateResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.app.apiserver.core.AppMessage;
import com.app.apiserver.services.AppMongoService;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

/**
 * saves messages into the DB table
 * @author smijar
 *
 */
@Singleton
public class AppMessageServiceImpl extends BasicDAO<AppMessage, ObjectId> implements AppMessageService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private Provider<AppMongoService> mongoService;

	@Inject
	public AppMessageServiceImpl(Provider<AppMongoService> mongoService) {
		super(mongoService.get().getMongo(), mongoService.get().getMorphia(), mongoService.get().getDBName());
		this.mongoService = mongoService;
	}
	
	/**
	 * saves the message
	 * @param message
	 * @return
	 */
	@Override
	public String saveObj(AppMessage message) {
		return super.save(message).getId().toString();
	}
	

	/**
	 * updates specific fields of the MessageStatus object
	 * 
	 * @param vdId
	 * @param updateFields
	 */
	private boolean updateFieldsById(String messageId, Map<String,Object> updateFields) {
		boolean updated = false;
		//logger.info("updateFieldsById - MQMessage id:{} with fields:{}", messageId, updateFields);

		try {
			UpdateOperations<AppMessage> ops = getDatastore().createUpdateOperations(AppMessage.class);
			for(Map.Entry<String, Object> entry:updateFields.entrySet()) {
				String field = entry.getKey();
				Object value = entry.getValue();
				ops.set(field, value);
			}
	
			if(updateFields.size()>0) {
				UpdateResults<AppMessage> ur = getDatastore().update(getDatastore().createQuery(AppMessage.class).disableValidation().field("_id").equal(new ObjectId(messageId)), ops);
				//logger.info("update id:{} and results.updatedCount:{}, results.hadError:{}", messageId, ur.getUpdatedCount(), ur.getHadError());
				updated = ur.getHadError();
				if(ur.getHadError()) {
					throw new Exception("There was an error while trying to update the messageStatus:"+messageId+" with fields:"+updateFields);
				}
			}
		}
		catch(Exception e) {
			// IMPORTANT - SWALLOWING EXCEPTION BECAUSE WE DO NOT WANT CALLER TO FAIL
			logger.error("error while trying to update messageStatusId:"+messageId+"with updateFields:"+updateFields+" exception: "+e.getClass().getName()+": "+e.getMessage());
		}

		return updated;
	}

	/* 
	 * marks the status of the message
	 * (non-Javadoc)
	 * @see com.dc.mgmt.services.messaging.MessagingService#markTaskInProgress(java.lang.String)
	 */
	@Override
	public void markMessageInProgress(String messageId) {
		Map<String, Object> updateOps = Maps.newHashMap();
		updateOps.put("status", AppMessage.RUNNING);
		updateOps.put("lastModified", new Date()); // update the last modified date
		this.updateFieldsById(messageId, updateOps);	
	}
	
	/* 
	 * marks the status of the message
	 * (non-Javadoc)
	 * @see com.dc.mgmt.services.messaging.MessagingService#markTaskInProgress(java.lang.String)
	 */
	@Override
	public void markMessageSuccess(String messageId) {
		Map<String, Object> updateOps = Maps.newHashMap();
		updateOps.put("status", AppMessage.SUCCESS);
		updateOps.put("lastModified", new Date()); // update the last modified date
		this.updateFieldsById(messageId, updateOps);	
	}
	
	/* 
     * marks the status of the message
     * (non-Javadoc)
     * @see com.dc.mgmt.services.messaging.MessagingService#markTaskInProgress(java.lang.String)
     */
    @Override
    public void markMessageNew(String messageId) {
        Map<String, Object> updateOps = Maps.newHashMap();
        updateOps.put("status", AppMessage.NEW);
        updateOps.put("lastModified", new Date()); // update the last modified date
        this.updateFieldsById(messageId, updateOps);    
    }

	/* 
	 * marks the status of the message
	 * (non-Javadoc)
	 * @see com.dc.mgmt.services.messaging.MessagingService#markTaskInProgress(java.lang.String)
	 */
	@Override
	public void markMessagPending(String messageId) {
		Map<String, Object> updateOps = Maps.newHashMap();
		updateOps.put("status", AppMessage.PENDING);
		updateOps.put("lastModified", new Date()); // update the last modified date
		this.updateFieldsById(messageId, updateOps);	
	}

	/* 
	 * marks the status of the message
	 * (non-Javadoc)
	 * @see com.dc.mgmt.services.messaging.MessagingService#markTaskInProgress(java.lang.String)
	 */
	@Override
	public void markMessageError(String messageId, String detail) {
		Map<String, Object> updateOps = Maps.newHashMap();
		updateOps.put("status", AppMessage.ERROR);
		updateOps.put("lastModified", new Date()); // update the last modified date
		updateOps.put("errorDetail", detail);
		this.updateFieldsById(messageId, updateOps);	
	}
}
