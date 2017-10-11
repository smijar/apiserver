package com.app.apiserver.messaging;


import com.google.inject.Inject;
import com.google.inject.Provider;

import io.dropwizard.lifecycle.Managed;

/**
 * Managed object that manages the scheduled services lifecycle on startup and shutdown of the server
 * @author smijar
 *
 */
public class AppScheduledServiceManager implements Managed {
	private Provider<ExampleScheduledService> exampleScheduledService;
	private Provider<QCheckScheduledService> qCheckScheduledService;
	
	@Inject
	public AppScheduledServiceManager(Provider<ExampleScheduledService> exampleScheduledTask, Provider<QCheckScheduledService> qCheckScheduledTask) {
		this.exampleScheduledService = exampleScheduledTask;
		this.qCheckScheduledService = qCheckScheduledTask;
	}

	@Override
	public void start() throws Exception {
		//this.exampleScheduledTask.get().stop();
		this.qCheckScheduledService.get().start();
	}

	@Override
	public void stop() throws Exception {
		//this.exampleScheduledTask.get().stop();
		this.qCheckScheduledService.get().stop();
	}
}
