package com.app.apiserver.services;

import org.bson.types.ObjectId;
import org.mongodb.morphia.dao.DAO;

import com.app.apiserver.core.ProspectsAppUpdate;

public interface ProspectsAppUpdateService extends DAO<ProspectsAppUpdate, ObjectId> {

	String saveObj(ProspectsAppUpdate update);

	void updateFields(String id, String status, String appMessageId);

}
