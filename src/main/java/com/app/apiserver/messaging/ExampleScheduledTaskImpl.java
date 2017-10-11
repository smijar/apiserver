package com.app.apiserver.messaging;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.AbstractScheduledService;

public class ExampleScheduledTaskImpl extends AbstractScheduledService implements ExampleScheduledService {

    private final Logger logger = LoggerFactory.getLogger(ExampleScheduledTaskImpl.class);

    @Override
    protected void runOneIteration() throws Exception {
        logger.info("runOneIteration");
    }

    @Override
    protected AbstractScheduledService.Scheduler scheduler() {
        return AbstractScheduledService.Scheduler.newFixedRateSchedule(0, 5, TimeUnit.SECONDS);
    }
    
    @Override
    public void start() {
    	super.startAsync();
    }

    @Override
    public void stop() {
    	super.stopAsync();
    }
}