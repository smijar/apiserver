package com.app.apiserver.services;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.app.apiserver.core.AppConfiguration;
import com.app.apiserver.core.AppMessage;
import com.app.apiserver.core.ProspectsAppUpdate;
import com.app.apiserver.messaging.AppMessageService;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

/**
 * handles PardotUpdate message in the Q 
 * 
 * @author smijar
 */
@Singleton
public class ProspectsAppUpdateMsgHandler implements UserInfoMsgHandler {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private Provider<ProspectsAppUpdateService> prospectsAppUpdateService;
	private Provider<ProspectsAppService> prospectsAppService;
	private Provider<AppConfiguration> appConfig;
	//private Provider<UserInfoService> userInfoService;

	@Inject
	public ProspectsAppUpdateMsgHandler(Provider<AppConfiguration> appConfig,
									Provider<ProspectsAppUpdateService> prospectsAppUpdateService,
									Provider<ProspectsAppService> prospectsAppService,
									Provider<AppMessageService> appMessageService ) {
		this.prospectsAppUpdateService = prospectsAppUpdateService;
		this.prospectsAppService = prospectsAppService;

		this.appConfig = appConfig;
	}

	@Override
	public void onMessage(AppMessage message) {
		logger.info("PardotUpdateMsgHandler: start-processing msg:{}", message);
		//CryptUtils.sleepForSeconds(CryptUtils.getRandomNumberInRange(5, 10));

		ProspectsAppUpdate prospectsAppUpdate = prospectsAppUpdateService.get().get(new ObjectId(message.getEntityId()));
		logger.info("PardotUpdateMsgHandler: processing msg:{} for prospectsAppUpdate:{}", message, prospectsAppUpdate);
		if(prospectsAppUpdate == null) {
			throw new RuntimeException("prospectsAppUpdate with id:"+message.getEntityId()+" retrieved for message:"+message+" was null");
		}

		// add new user and allocate
		if(prospectsAppUpdate.getEmail()==null) {
			logger.warn("PardotUpdateMsgHandler: Email for prospectsAppUpdate:{} is empty, hence not updating prospectsApp for this message:{}", prospectsAppUpdate, message);
			throw new RuntimeException("prospectsAppUpdate:"+prospectsAppUpdate+" does not contain an email, hence cannot update to a list!");
		}

		int listId = -1;
		if(prospectsAppUpdate.getAction().equals(ProspectsAppUpdate.REGISTER_USER_ON_LANDING_PAGE)) {
			listId = appConfig.get().getProspectsAppConfig().getLandingPageListId();
		} else if(prospectsAppUpdate.getAction().equals(ProspectsAppUpdate.REGISTER_USER_OUT_OF_CAPACITY)) {
			listId = appConfig.get().getProspectsAppConfig().getOutOfCapacityListId();
		} else if(prospectsAppUpdate.getAction().equals(ProspectsAppUpdate.REGISTER_USER_CONFIRMED)) {
			listId = appConfig.get().getProspectsAppConfig().getvPhoneConfirmedListId();
		} else if(prospectsAppUpdate.getAction().equals(ProspectsAppUpdate.REGISTER_USER_ON_PICK_APPS_PAGE)) {
			listId = appConfig.get().getProspectsAppConfig().getPickAppsListId();
		} else {
			logger.warn("PardotUpdateMsgHandler: list id:{} for prospectsAppUpdate:{} was not found, hence not updating prospectsApp for this message:{}", prospectsAppUpdate, message);
			throw new RuntimeException("PardotUpdateMsgHandler: list id for prospectsAppUpdate:"+prospectsAppUpdate+" does not contain a valid listId, hence cannot update to a list!");
		}

		prospectsAppService.get().addUpdateUserToPardotList(prospectsAppUpdate.getEmail(), listId);
		if(prospectsAppUpdate.getAction().equals(ProspectsAppUpdate.REGISTER_USER_ON_PICK_APPS_PAGE)) {
			// update the zip code from the forms page
			if(prospectsAppUpdate.getPayload()!=null) {
				String personalZipCode = prospectsAppUpdate.getPayload().get("personalZipCode");
				if(personalZipCode != null) {
					// update the "Local Zipcode" field in prospectsApp
					prospectsAppService.get().updateFieldInPardot(prospectsAppUpdate.getEmail(), "Local Zipcode", personalZipCode, true);
				}
			}
		}

		logger.info("PardotUpdateMsgHandler: finish-processing msg:{} for prospectsAppUpdate:{}", message, prospectsAppUpdate);
	}
}