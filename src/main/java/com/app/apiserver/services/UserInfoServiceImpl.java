package com.app.apiserver.services;

import java.util.Date;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.mongodb.morphia.dao.BasicDAO;
import org.mongodb.morphia.query.UpdateOperations;
import org.mongodb.morphia.query.UpdateResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.app.apiserver.core.UserInfo;
import com.google.common.collect.Maps;
import com.google.inject.Provider;
import com.google.inject.Singleton;

/**
 * DAO for persistence and queries for the UserInfo object
 * 
 * @author smijar
 */
@Singleton
public class UserInfoServiceImpl extends BasicDAO<UserInfo, ObjectId> implements UserInfoService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private Provider<AppMongoService> mongoService;

	@Inject
	public UserInfoServiceImpl(Provider<AppMongoService> mongoService) {
		super(mongoService.get().getMongo(), mongoService.get().getMorphia(), mongoService.get().getDBName());
		this.mongoService = mongoService;
	}

	/**
	 * returns the user info for the id if found
	 * 
	 * @param userEmail
	 * @return
	 */
	@Override
	public UserInfo getById(String id) {
		UserInfo user = super.get(new ObjectId(id));
		return user;
	}

	/**
	 * returns the user info for the email if found
	 * 
	 * @param userEmail
	 * @return
	 */
	@Override
	public UserInfo getByEmail(String userEmail) {
		UserInfo user = super.findOne(super.createQuery().field("email").equal(userEmail));
		return user;
	}
	
	/**
	 * returns true if the user already exists
	 * 
	 * @param userEmail
	 * @return
	 */
	@Override
	public boolean userEmailAlreadyRegistered(String userEmail) {
		return super.exists(super.createQuery().field("email").equal(userEmail));
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
			UpdateOperations<UserInfo> ops = getDatastore().createUpdateOperations(UserInfo.class);
			for(Map.Entry<String, Object> entry:updateFields.entrySet()) {
				String field = entry.getKey();
				Object value = entry.getValue();
				ops.set(field, value);
			}
	
			if(updateFields.size()>0) {
				UpdateResults<UserInfo> ur = getDatastore().update(getDatastore().createQuery(UserInfo.class).disableValidation().field("_id").equal(new ObjectId(userId)), ops);
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
	public void updateLocationForUser(String userId, int mgmtServerIndex) {
		Map<String, Object> updateOps = Maps.newHashMap();

		updateOps.put("mgmtServerIndex", mgmtServerIndex);
		updateOps.put("lastModified", new Date()); // update the last modified date
		this.updateFieldsById(userId, updateOps);	
	}


	/**
	 * updates the template to be used by user field
	 */
	@Override
	public void updateUserStatusField(String userId, String status) {
		this.updateUserStatusAndMessageIdFields(userId, status, null);
	}

	/**
	 * updates the status for the user AFTER the message is processed to indicate
	 * whether the message was successful or not.  It also indicates which message
	 * was used to process the user.
	 */
	@Override
	public void updateUserStatusAndMessageIdFields(String userId, String status, String appMessageId) {
		Map<String, Object> updateOps = Maps.newHashMap();
		
		updateOps.put("status", status);
		if(!StringUtils.isBlank(appMessageId))
			updateOps.put("messageId", appMessageId);
		updateOps.put("lastModified", new Date()); // update the last modified date
		this.updateFieldsById(userId, updateOps);
	}

	/**
	 * updates just the messageId field to indicate which message will be used to process
	 * the user
	 */
	@Override
	public void updateUserMessageIdField(String userId, String appMessageId) {
		Map<String, Object> updateOps = Maps.newHashMap();

		updateOps.put("messageId", appMessageId);
		updateOps.put("lastModified", new Date()); // update the last modified date
		this.updateFieldsById(userId, updateOps);
	}

	/**
	 * gets the number of users at the datacenter
	 * 
	 * @param mgmtServerIndex
	 * @return
	 */
	@Override
	public long getNumUsersAtDC(String mgmtServerName) {
		return getDatastore().getCount(super.createQuery().field("mgmtServerName").equal(mgmtServerName));
	}
}