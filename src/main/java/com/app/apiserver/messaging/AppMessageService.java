package com.app.apiserver.messaging;

import org.bson.types.ObjectId;
import org.mongodb.morphia.dao.DAO;

import com.app.apiserver.core.AppMessage;

public interface AppMessageService extends DAO<AppMessage, ObjectId> {

	String saveObj(AppMessage message);

	void markMessageInProgress(String messageId);

	void markMessageSuccess(String messageId);

	void markMessageError(String messageId, String detail);

	void markMessagPending(String messageId);

    void markMessageNew(String messageId);

}
