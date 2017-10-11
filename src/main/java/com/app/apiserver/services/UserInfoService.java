package com.app.apiserver.services;

import org.bson.types.ObjectId;
import org.mongodb.morphia.dao.DAO;

import com.app.apiserver.core.UserInfo;

public interface UserInfoService extends DAO<UserInfo, ObjectId> {
	void updateLocationForUser(String userId, int mgmtServerIndex);

	UserInfo getById(String id);

	UserInfo getByEmail(String userEmail);

	boolean userEmailAlreadyRegistered(String userEmail);

	public long getNumUsersAtDC(String mgmtServerName);

	void updateUserStatusAndMessageIdFields(String userId, String status, String appMessageId);
	void updateUserStatusField(String userId, String status);

	void updateUserMessageIdField(String userId, String appMessageId);
}