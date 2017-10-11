package com.app.apiserver.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.app.apiserver.core.AppMessage;
import com.app.apiserver.messaging.AppMessageHandler;
import com.google.inject.Provider;
import com.google.inject.Singleton;

/**
 * handles the user info messages
 * 
 * @author smijar
 */
public interface UserInfoMsgHandler extends AppMessageHandler {
}