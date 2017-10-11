package com.app.apiserver.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.app.apiserver.core.AppMessage;
import com.app.apiserver.core.UserInfo;
import com.app.apiserver.messaging.AppMessageService;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

/**
 * handles UserInfo message in the Q 
 * 
 * @author smijar
 */
@Singleton
public class UserInfoMsgHandlerImpl implements UserInfoMsgHandler {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private Provider<UserInfoService> userInfoService;
	private Provider<MgmtApiService> mgmtApiService;
	private Provider<AppMessageService> appMesageService;


	@Inject
	public UserInfoMsgHandlerImpl(Provider<UserInfoService> userInfoService,
									Provider<MgmtApiService> mgmtApiService,
									Provider<AppMessageService> appMessageService) {
		this.userInfoService = userInfoService;
		this.mgmtApiService = mgmtApiService;
		this.appMesageService = appMessageService;
	}

	@Override
	public void onMessage(AppMessage message) {
		logger.info("UserInfoMsgHandlerImpl: start-processing msg:{}", message);
		//CryptUtils.sleepForSeconds(CryptUtils.getRandomNumberInRange(5, 10));

		UserInfo userInfo = userInfoService.get().getById(message.getEntityId());
		logger.info("UserInfoMsgHandlerImpl: processing message:{} for userInfo", message, userInfo);
		if(userInfo == null) {
			throw new RuntimeException("userInfo with id:"+message.getEntityId()+" retrieved for message:"+message+" was null");
		}

		logger.info("UserInfoMsgHandlerImpl: finish-processing msg:{}", message);
	}
}