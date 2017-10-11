package com.app.apiserver.services;

import java.util.Date;
import java.util.Map;

import org.bson.types.ObjectId;
import org.mongodb.morphia.dao.BasicDAO;
import org.mongodb.morphia.query.UpdateOperations;
import org.mongodb.morphia.query.UpdateResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.app.apiserver.core.AppMessage;
import com.app.apiserver.core.EventLog;
import com.app.apiserver.core.UserInfo;
import com.app.apiserver.core.ProspectsAppUpdate;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class ProspectUpdateServiceImpl extends BasicDAO<ProspectsAppUpdate, ObjectId> implements ProspectsAppUpdateService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private Provider<AppMongoService> mongoService;

	@Inject
	public ProspectUpdateServiceImpl(Provider<AppMongoService> mongoService) {
		super(mongoService.get().getMongo(), mongoService.get().getMorphia(), mongoService.get().getDBName());
		this.mongoService = mongoService;
	}
	
	/**
	 * saves the message
	 * @param message
	 * @return
	 */
	@Override
	public String saveObj(ProspectsAppUpdate update) {
		return super.save(update).getId().toString();
	}


	/**
	 * updates specific fields of the MessageStatus object
	 * 
	 * @param vdId
	 * @param updateFields
	 */
	private boolean updateFieldsById(String userId, Map<String,Object> updateFields) {
		boolean updated = false;
		//logger.info("updateFieldsById - MQMessage id:{} with fields:{}", messageId, updateFields);

		try {
			UpdateOperations<ProspectsAppUpdate> ops = getDatastore().createUpdateOperations(ProspectsAppUpdate.class);
			for(Map.Entry<String, Object> entry:updateFields.entrySet()) {
				String field = entry.getKey();
				Object value = entry.getValue();
				ops.set(field, value);
			}

			if(updateFields.size()>0) {
				UpdateResults<ProspectsAppUpdate> ur = getDatastore().update(getDatastore().createQuery(ProspectsAppUpdate.class).disableValidation().field("_id").equal(new ObjectId(userId)), ops);
				//logger.info("update id:{} and results.updatedCount:{}, results.hadError:{}", messageId, ur.getUpdatedCount(), ur.getHadError());
				updated = ur.getHadError();
				if(ur.getHadError()) {
					throw new Exception("There was an error while trying to update the messageStatus:"+userId+" with fields:"+updateFields);
				}
			}
		}
		catch(Exception e) {
			// IMPORTANT - SWALLOWING EXCEPTION BECAUSE WE DO NOT WANT CALLER TO FAIL
			logger.error("error while trying to update messageStatusId:"+userId+"with updateFields:"+updateFields+" exception: "+e.getClass().getName()+": "+e.getMessage());
		}

		return updated;
	}

	/* 
	 * update the management server index to which the user is pinned
	 * 
	 * (non-Javadoc)
	 * @see com.dc.mgmt.services.messaging.MessagingService#markTaskInProgress(java.lang.String)
	 */
	@Override
	public void updateFields(String id, String status, String appMessageId) {
		Map<String, Object> updateOps = Maps.newHashMap();

		updateOps.put("status", status);
		updateOps.put("messageId", appMessageId); // update the last modified date
		updateOps.put("lastModified", new Date()); // update the last modified date

		this.updateFieldsById(id, updateOps);	
	}
}
