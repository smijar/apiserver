package com.app.apiserver.messaging;

/**
 * a scheduled task interface that starts and stops based on calls to the implementation
 * 
 * @author smijar
 *
 */
public interface AppScheduledService {
	void start();
	void stop();
}
