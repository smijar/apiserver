package com.app.apiserver.messaging;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.mongodb.morphia.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.app.apiserver.core.AppConfiguration;
import com.app.apiserver.core.AppMessage;
import com.app.apiserver.core.EventLog;
import com.app.apiserver.core.UserInfo;
import com.app.apiserver.core.MgmtServerDownException;
import com.app.apiserver.core.ProspectsAppProspectListMembership;
import com.app.apiserver.core.ProspectsAppUpdate;
import com.app.apiserver.services.DurationTracker;
import com.app.apiserver.services.EventLogService;
import com.app.apiserver.services.UserInfoService;
import com.app.apiserver.services.ProspectsAppUpdateMsgHandler;
import com.app.apiserver.services.ProspectsAppUpdateService;
import com.app.apiserver.services.UserInfoMsgHandler;
import com.google.common.base.Throwables;
import com.google.common.util.concurrent.AbstractScheduledService;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;


/**
 * scheduled task to check for messages in the DB table AppMessage
 * 
 * @author smijar
 */
@Singleton
public class QCheckScheduledServiceImpl extends AbstractScheduledService implements QCheckScheduledService {
    private final Logger logger = LoggerFactory.getLogger(QCheckScheduledServiceImpl.class);
    private Provider<AppConfiguration> appConfig;
	private Provider<AppMessageService> messageService;
	private Provider<UserInfoService> userInfoService;
	private Provider<UserInfoMsgHandler> userInfoMsgHandler;
	private Provider<ProspectsAppUpdateMsgHandler> prospectsAppUpdateMsgHandler;
	private Provider<EventLogService> eventLogService;
	private Provider<ProspectsAppUpdateService> prospectsAppUpdateService;
	private int limit = 10;
	//private ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10));
	
	private ListeningExecutorService msgDispatcher;
	private ThreadPoolExecutor underlyingTP;
	private DurationTracker durationTracker = new DurationTracker();

	@Inject
	public QCheckScheduledServiceImpl(Provider<AppConfiguration> appConfig, 
										Provider<AppMessageService> messageService, 
										Provider<UserInfoMsgHandler> userInfoMsgHandler,
										Provider<ProspectsAppUpdateMsgHandler> prospectsAppUpdateMsgHandler,
										Provider<UserInfoService> userInfoService,
										Provider<EventLogService> eventLogService,
										Provider<ProspectsAppUpdateService> prospectsAppUpdateService) {
		this.appConfig = appConfig;
		this.messageService = messageService;
		this.userInfoMsgHandler = userInfoMsgHandler;
		this.prospectsAppUpdateMsgHandler = prospectsAppUpdateMsgHandler;
		this.userInfoService = userInfoService;
		this.prospectsAppUpdateService = prospectsAppUpdateService;

		ThreadFactory tf = new ThreadFactoryBuilder()
				 							.setNameFormat("UserInfo-%d")
				 							.setDaemon(true)
				 							.build();

		limit = appConfig.get().getGeneralConfig().getNumMsgThreadPoolSize();

		this.msgDispatcher = MoreExecutors.listeningDecorator(newFixedThreadPoolWithQueueSize(tf, limit, limit));
	}

	/**
	 * creates a new thread pool with a limited blocking pool
	 * @param threadFactory
	 * @param nThreads
	 * @param queueSize
	 * @return
	 */
	private ExecutorService newFixedThreadPoolWithQueueSize(ThreadFactory threadFactory, int nThreads, int queueSize) {
	    this.underlyingTP = new ThreadPoolExecutor(nThreads, nThreads,
		                                  60L, TimeUnit.SECONDS,
		                                  new ArrayBlockingQueue<Runnable>(queueSize, true),
		                                  threadFactory,
		                                  new ThreadPoolExecutor.CallerRunsPolicy());
	    return this.underlyingTP;
	}

	/**
	 * look at the top of the queue for messages 
	 * 
	 * @param numMessages
	 * @return
	 */
	private List<AppMessage> peek(int numMessages) {
		Query<AppMessage> q = messageService.get().createQuery().field("status").equal(AppMessage.NEW);
		// q.or((q.criteria("status").equal(AppMessage.NEW)), 
		//		q.criteria("status").equal(AppMessage.PENDING));
		return q.asList();
	}

	/**
	 * checks the DB for list of "NEW" messages.  we check to see if the pool is full.  if it is, we skip dispatch. if it is not full,
	 * we dispatch and process the message.
	 * 
	 * on completion-onSuccess() of task, we mark message as SUCCESS, and if not, we mark it as ERROR onFailure()
	 * 
	 * We use Guava's ListenableFuture<AppMessage> to listen to the success or failure (completion) of the task
	 */
    @Override
    protected void runOneIteration() throws Exception {
    	boolean canLogCheckForMessagesInDBNow = durationTracker.isLongerThanSeconds(25);
    	if(canLogCheckForMessagesInDBNow) durationTracker.reset();

        try {
        	int numOpenSlots = limit;
        	// check if the pool has active slots to process new messages and skip if not
        	logger.debug("qchecker: checking if pool has slots to process messages - pool active count:{}, poolIsFull:{}", underlyingTP.getActiveCount(), (underlyingTP.getActiveCount() >= limit));

        	if(underlyingTP.getActiveCount() > 0) {
        		numOpenSlots = numOpenSlots - underlyingTP.getActiveCount();
        		if(numOpenSlots < 0)
        			numOpenSlots = 0;
        	}

        	if(numOpenSlots == 0)
        		logger.info("qchecker: skipping peek of messages because the dispatcher pool is full...");

        	// if pool has no idle threads, then skip this run 
        	if(numOpenSlots > 0) {
        		
        		// peek
                //logger.info("qchecker: peeking at {} messages...", messagesForOpenSlots);
                List<AppMessage> messages = peek(numOpenSlots);
                // logs this message only once in 25 or 40 seconds
                if(canLogCheckForMessagesInDBNow)
                	logger.info("qchecker: numOpenSlots:{} consuming {} new message(s)", numOpenSlots, messages.size());

	        	// dispatch
		        for(final AppMessage message:messages) {
    				messageService.get().markMessageInProgress(message.getId());

	        		// dispatch the message
	        		ListenableFuture<AppMessage> msgFuture = msgDispatcher.submit(new Callable<AppMessage>() {
	        			public AppMessage call() {
	        				
	        				logger.info("qchecker: {}-start-onCall()-for message:{}", Thread.currentThread().getName(), message);
	        				if(message.getType().equals(UserInfo.class.getSimpleName()))
	        					userInfoMsgHandler.get().onMessage(message);
	        				else if(message.getType().equals(ProspectsAppUpdate.class.getSimpleName()))
	        					prospectsAppUpdateMsgHandler.get().onMessage(message);
	        				logger.info("qchecker: {}-finish-onCall()-for message:{}", Thread.currentThread().getName(), message);

							return message;
	        			}
	        		});

	        		// add callbacks to the promise
	        		Futures.addCallback(msgFuture, new FutureCallback<AppMessage>(){

	        			public void onSuccess(AppMessage message) {
	        				logger.info("qchecker: {}-onSuccess() for message:{}", Thread.currentThread().getName(), message);
        					messageService.get().markMessageSuccess(message.getId());
        					if(message.getType().equals(UserInfo.class.getSimpleName())) {
        						userInfoService.get().updateUserStatusAndMessageIdFields(message.getEntityId(), UserInfo.SUCCESS_SUBMITTED_FOR_ALLOCATION, message.getId());
        					} else if(message.getType().equals(ProspectsAppUpdate.class.getSimpleName())){
        						prospectsAppUpdateService.get().updateFields(message.getEntityId(), ProspectsAppUpdate.SUCCESSFULLY_UPDATED, message.getId());
        					}
	        			}

	        			/**
	        			 * called if there was an error/exception in the handling of the messgae.
	        			 * we mark the message in ERROR and log the exception for examination later
	        			 * 
	        			 * we also mark the associated entity(userInfo) as having failed and the associated messageId so 
	        			 * its easier for monitoring allocation for the user
	        			 * 
	        			 * we also log the failure in the event log. 
	        			 */
	        			public void onFailure(Throwable thrown) {

	        				// VERY VERY IMPORTANT retry section
	        				// if the management server was down, then we don't change status of the message, we just let it stay as NEW
	        				// so that it can be retried until we can reach the management server
	        				if(thrown instanceof MgmtServerDownException) {
	        			        logger.error("Server down.. will retry later");
	        			        messageService.get().markMessageNew(message.getId());
	                            return;
	        			    }

	        				// it was some other failure, hence we want to mark the message as error and why, and mark the entity status as well, 
	        				//	while referencing the messageId
	        				try {
		        			    logger.error("qchecker: {}-onFailure() for message:{} error:{}", Thread.currentThread().getName(), message, thrown);
	
		        			    // update the message with Error and the Exception details
		        				messageService.get().markMessageError(message.getId(), Throwables.getStackTraceAsString(thrown));
	        				} finally {
	        					// and now update the specific entity status
	        					if(message.getType().equals(UserInfo.class.getSimpleName())) {
	        						// update UserInfo
	    	        			    UserInfo userInfo = userInfoService.get().getById(message.getEntityId());
	    	        				// update the userinfo object as well and add the message id
	        						userInfoService.get().updateUserStatusAndMessageIdFields(message.getEntityId(), UserInfo.ERROR_SEE_APP_MESSAGE, message.getId());
	        						// update the event log
	    	        				eventLogService.get().save(new EventLog(userInfo.getEmail(), EventLog.USER_ERROR_ALLOCATE_VPHONE_VD_START));
	        					} else if(message.getType().equals(ProspectsAppProspectListMembership.class.getSimpleName())){
	        						// update UserInfo
	        						prospectsAppUpdateService.get().updateFields(message.getEntityId(), ProspectsAppUpdate.ERROR_ON_UPDATE, message.getId());
	        					}
	        				}
	        			}

	        		});
	        	}
        	}
        } catch(Exception e) {
        	logger.error("qchecker: Error while trying to check for new messages, but continuing...", e);
        }
    }

    /**
     * scheduled to run every 5 seconds
     */
    @Override
    protected AbstractScheduledService.Scheduler scheduler() {
		if(appConfig.get().getGeneralConfig().canRunBackgroundTasks()) {
			return AbstractScheduledService.Scheduler.newFixedRateSchedule(0, 5, TimeUnit.SECONDS);
		} else {
			return null;
		}
    }

    /**
     * starts the scheduled task
     */
    @Override
    public void start() {
    	super.startAsync();
    }

    /**
     * stops the scheduled task
     */
    @Override
    public void stop() {
    	super.stopAsync();
    }
}