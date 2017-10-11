package com.app.apiserver.services;

import org.bson.types.ObjectId;
import org.mongodb.morphia.dao.DAO;

import com.app.apiserver.core.EventLog;

public interface EventLogService extends DAO<EventLog, ObjectId> {

}
