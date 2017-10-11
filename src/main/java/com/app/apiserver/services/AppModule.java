package com.app.apiserver.services;

import java.lang.reflect.Field;

import org.slf4j.Logger;

import com.app.apiserver.core.AppConfiguration;
import com.app.apiserver.core.AppMongoConfig;
import com.app.apiserver.messaging.AppMessageService;
import com.app.apiserver.messaging.AppMessageServiceImpl;
import com.app.apiserver.messaging.ExampleScheduledService;
import com.app.apiserver.messaging.ExampleScheduledTaskImpl;
import com.app.apiserver.messaging.QCheckScheduledService;
import com.app.apiserver.messaging.QCheckScheduledServiceImpl;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

public class AppModule extends AbstractModule {
	private AppConfiguration appConfig;
	private Injector injector;
	
	public AppModule(AppConfiguration appConfig) {
		this.appConfig = appConfig;
	}

	public AppConfiguration getAppConfig() {
		return appConfig;
	}

	public void setAppConfig(AppConfiguration appConfig) {
		this.appConfig = appConfig;
	}

	/**
	 * sets up the logger to be injectable
	 */
	private void setupInjectableLogger() {
		// first, register the logger and the Global Event Bus
		// LOGGER
		binder().bindListener(Matchers.any(), new TypeListener() {
			public <I> void hear(TypeLiteral<I> aTypeLiteral, TypeEncounter<I> aTypeEncounter) {

				for (Field field : aTypeLiteral.getRawType().getDeclaredFields()) {
					if (field.getType() == Logger.class && field.isAnnotationPresent(AppInjectLogger.class)) {
						aTypeEncounter.register(new Slf4jMembersInjector<I>(field));
					}
				}
			}
		});
	}

	@Override
	public void configure() {
		/*
		 * This tells Guice that whenever it sees a dependency on a
		 * TransactionLog, it should satisfy the dependency using a
		 * DatabaseTransactionLog.
		 */
		setupInjectableLogger();

		// setup injection for the configurations first
		bind(AppConfiguration.class).toInstance(appConfig);
		bind(AppMongoConfig.class).toInstance(appConfig.getMongoConfig());

		// now set up bindings for service interfaces
		bind(AppMongoService.class).to(AppMongoServiceImpl.class).in(Scopes.SINGLETON);
		bind(AppMapperService.class).to(AppMapperServiceImpl.class).in(Scopes.SINGLETON);
		bind(UserInfoService.class).to(UserInfoServiceImpl.class).in(Scopes.SINGLETON);
		bind(ExampleScheduledService.class).to(ExampleScheduledTaskImpl.class).in(Scopes.SINGLETON);
		bind(QCheckScheduledService.class).to(QCheckScheduledServiceImpl.class).in(Scopes.SINGLETON);
		bind(AppMessageService.class).to(AppMessageServiceImpl.class).in(Scopes.SINGLETON);
		bind(UserInfoMsgHandler.class).to(UserInfoMsgHandlerImpl.class).in(Scopes.SINGLETON);
		bind(MgmtApiService.class).to(MgmtApiServiceImpl.class).in(Scopes.SINGLETON);
		bind(LocationService.class).to(LocationServiceImpl.class).in(Scopes.SINGLETON);
		bind(ProspectsAppService.class).to(ProspectsAppServiceImpl.class).in(Scopes.SINGLETON);
		bind(ProspectsAppUpdateService.class).to(ProspectUpdateServiceImpl.class);
		bind(EventLogService.class).to(EventLogServiceImpl.class);
	}
}
