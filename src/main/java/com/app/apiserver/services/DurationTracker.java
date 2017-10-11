package com.app.apiserver.services;

import org.joda.time.DateTime;
import org.joda.time.Duration;

public class DurationTracker {
	private DateTime createdAt;

	public DurationTracker() {
		reset();
	}

	public boolean isLongerThanSeconds(int seconds) {
		DateTime now = new DateTime();
		Duration sinceLastAlertPosted = new Duration(createdAt, now);
		boolean isLonger = sinceLastAlertPosted.isLongerThan(Duration.standardSeconds(seconds));
		createdAt = isLonger ? now: createdAt; // update if posting alert this time 
		return isLonger;
	}
	
	public long getDurationMillis() {
		DateTime now = new DateTime();
		Duration result = new Duration(createdAt, now);
		return result.getMillis();
	}

	public void reset() {
		createdAt = new DateTime();
	}
}
