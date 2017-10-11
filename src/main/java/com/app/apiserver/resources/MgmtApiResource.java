package com.app.apiserver.resources;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.app.apiserver.core.AppConfiguration;
import com.app.apiserver.core.AppMessage;
import com.app.apiserver.core.CreateTemplateRequest;
import com.app.apiserver.core.EventLog;
import com.app.apiserver.core.MgmtServerConfig;
import com.app.apiserver.core.ProspectsAppConfig;
import com.app.apiserver.core.ProspectsAppProspectListMembership;
import com.app.apiserver.core.ProspectsAppUpdate;
import com.app.apiserver.core.UserInfo;
import com.app.apiserver.core.ZipLatLong;
import com.app.apiserver.messaging.AppMessageService;
import com.app.apiserver.services.EventLogService;
import com.app.apiserver.services.LocationService;
import com.app.apiserver.services.MgmtApiService;
import com.app.apiserver.services.ProspectsAppUpdateService;
import com.app.apiserver.services.ProspectsAppService;
import com.app.apiserver.services.UserInfoService;
import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.inject.Provider;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import io.dropwizard.jersey.caching.CacheControl;


/**
 * This proxies the management server API with simple calls to add a new user, allocate a VD for him, and create new templates
 * The post will be coming from a UI on a web page.  The post will return one of 3 things:
 * - Http status of 200 OK if the submitting the form went ok
 * - Http status of 401 denied if the user could not be authenticated against Pardot
 * - Http status of 500 if there was an internal server of some sort
 * 
 *  No, the post will not wait for the VD to be allocated.  It gets saved into the DB and the steps of user getting added into the system, creating the template for the user, creating the image for the user, and the VD for the user get run asynchronously.
 * 
 * TODOs 
 * - 401 error code sends html page as body, need to alter this
 *  
 * @author smijar
 */
@Path("/v1/apiserver")
@Produces(MediaType.APPLICATION_JSON)
@SuppressWarnings("unused")
public class MgmtApiResource  {
	private static Logger logger = LoggerFactory.getLogger(MgmtApiResource.class);
	private Provider<MgmtApiService> mgmtApiService;
	private Provider<AppConfiguration> appConfig;
	private Provider<LocationService> locationService;
	private Provider<ProspectsAppService> prospectsAppService;
	private Provider<ProspectsAppUpdateService> prospectsAppUpdateService;
	private Provider<UserInfoService> userInfoService;
	private Provider<EventLogService> eventLogService;
	private Provider<AppMessageService> appMessageService;
	
	private static final int HTTP_ERROR_INPUT_VALIDATION = 510;

	private static final int MAX_INPUT_ALLOWED = 248;
	
	private static String INVALID_CHARS = "[~#@*+%{}<>\\[\\]|\"\\\\/^,=`\r\n\t]";
	private static String INVALID_CHARS_SHOW = "~ # @ * + % { } < > [ ] | \" \\ / ^ , = `";
	private static Pattern INVALID_CHARS_PATTERN = Pattern.compile(INVALID_CHARS);

	
	private Map<String,String> checkedDCs = Maps.newHashMap();
	
	@Inject
	public MgmtApiResource(Provider<AppConfiguration> appConfig, 
							Provider<MgmtApiService> mgmtApiService, 
							Provider<LocationService> locationService,
							Provider<ProspectsAppService> prospectsAppService,
							Provider<ProspectsAppUpdateService> prospectsAppUpdateService,
							Provider<UserInfoService> userInfoService,
							Provider<EventLogService> eventLogService,
							Provider<AppMessageService> appMessageService) {
		this.appConfig = appConfig;
		this.mgmtApiService = mgmtApiService;
		this.locationService = locationService;
		this.prospectsAppService = prospectsAppService;
		this.prospectsAppUpdateService = prospectsAppUpdateService;
		this.userInfoService = userInfoService;
		this.eventLogService = eventLogService;
		this.appMessageService = appMessageService;
	}

	/**
	 * simple call to identify the resource
	 * @return
	 */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String get() {
        return "MgmtApiResource";
    }

	/**
	 * Resource: simple call to check connectivity to management server and get its particular unique id
	 */
	@Timed
	@GET
	@Path("/serverid")
    @Produces(value = MediaType.APPLICATION_JSON)
	public String getServerId(@Context HttpServletRequest request) {
		String uuid = "";
        try {
        	logger.info("getServerId: getting serverId");

        	MgmtServerConfig mgmtServerConfig = appConfig.get().getMgmtServersConfig().getMgmtServerConfigs().get(0);

            //HttpResponse<JsonNode> response = Unirest.get("https://httpbin.org/get?show_env=1").asJson();
        	//+":"+mgmtServerConfig.getPort()
        	String url = "https://"+mgmtServerConfig.getHost()+"/api/v1/serverinfo/uuid";
            HttpResponse<String> response = Unirest.get(url).asString();
            uuid = response.getBody();

            logger.info("getServerId:{}", response.getBody());
		} catch(WebApplicationException e) {
			throw e;
        } catch (UnirestException e) {
			logger.error("getServerId: while trying to getServerId", e);
            throw new WebApplicationException(e.getMessage(), e);
		} catch(Exception e) {
			logger.error("getServerId: while trying to getServerId", e);
			throw new WebApplicationException(e.getMessage(), e);
		}

        return uuid;
	}


	/**
	 * saves the user. returns HTTP 200 if saved, and HTTP 409(conflict) if user email is already registered.
	 * 
	 * @param request
	 * @param userInfo
	 * @return
	 */
	@Timed
	@POST
	@Path("/saveuserinfo")
    @Produces(value = MediaType.APPLICATION_JSON)
	@CacheControl(noCache = true)
	public Response saveUserInfo(@Context HttpServletRequest request, UserInfo userInfo) {
		String savedId = null;
		Response response = Response.ok().build();

		try {			

			// if we are checking for grecaptcha then make sure it exists and is valid
            if( appConfig.get().getGeneralConfig().isUseGrecaptcha() ){
                String grecaptchaResponse = request.getHeader("grecaptcha");
                logger.debug( "grecaptchaResponse: " + grecaptchaResponse);

                if( StringUtils.isBlank(grecaptchaResponse) ) {
                	logger.error( "No grecaptcha found in request headers");
                    throw new WebApplicationException("Please verify that you are not a robot.", HTTP_ERROR_INPUT_VALIDATION);
                } else {
                	boolean b = verifyGrecaptchaResponse( grecaptchaResponse );
                	if( b == false ) {
                    logger.error( "grecaptcha verification failed.");
                    throw new WebApplicationException("Please verify that you are not a robot.", HTTP_ERROR_INPUT_VALIDATION);
                	}
                }
            }	

			// Do input validation
			if( userInfo == null ) {
				throw new WebApplicationException("Please specify a valid user.", HTTP_ERROR_INPUT_VALIDATION);
			}

			if( StringUtils.isBlank(userInfo.getEmail()) ) {
				throw new WebApplicationException("Email is invalid. Please enter a valid email address.", HTTP_ERROR_INPUT_VALIDATION);
			}
			if( userInfo.getEmail().length() > MAX_INPUT_ALLOWED ) {
				throw new WebApplicationException("Email contains too many characters. Please enter a valid email address.", HTTP_ERROR_INPUT_VALIDATION);
			}
			
			verifyString(userInfo.getName(), "Name");
			verifyString(userInfo.getCompany(), "Company");
			verifyString(userInfo.getJobTitle(), "JobTitle");
			verifyString(userInfo.getPersonalZipCode(), "ZipCode");

			// verify zip code entered by user is valid by doing a lookup in DB.
			ZipLatLong zip = locationService.get().getLatLongForZipCode(userInfo.getPersonalZipCode());
			if( zip == null ) {
				throw new WebApplicationException("ZipCode \"" + userInfo.getPersonalZipCode() + "\" is invalid. Please enter a valid ZipCode.", HTTP_ERROR_INPUT_VALIDATION);
			}
			
			//convert to lower case before saving in DB.
			userInfo.setEmail( userInfo.getEmail().toLowerCase(Locale.ENGLISH) );
			
			//trim white space if any from all input params
			userInfo.setEmail( userInfo.getEmail().trim() );
			userInfo.setPersonalZipCode( userInfo.getPersonalZipCode().trim() );
			userInfo.setName( userInfo.getName().trim() );
			if( userInfo.getJobTitle() != null ) {
				userInfo.setJobTitle( userInfo.getJobTitle().trim() );
			}
			if( userInfo.getCompany() != null ) {
				userInfo.setCompany( userInfo.getCompany().trim() );
			}

			// IMPORTANT:
			// we check to see if userInfo is already in DB and if the status is NEW, then we just update it
			//	 - this can happen only if the user went to second page and just wants to fix something in the form
			UserInfo user = userInfoService.get().getByEmail(userInfo.getEmail());
			if( user != null ) {
				// if we find a user in the DB, then make sure the user's state is NEW
				if( user.getStatus() != null && user.getStatus().compareToIgnoreCase(UserInfo.NEW) == 0 )  {

					userInfo.setId( user.getId() );

					// this should update existing user with new info that was just entered.
					// This can happen if user went to next page and then hit the back button.
					
					savedId = mgmtApiService.get().saveUser(userInfo);
					logger.info( "User with email: " + userInfo.getEmail() + " already exist in DB but state is NEW so allowing user to continue.");
				} else {
					//response = Response.status(HttpStatus.CONFLICT_409).build();
					throw new WebApplicationException("Email:"+userInfo.getEmail()+" is already registered. Please use another email.", HttpStatus.CONFLICT_409);
				}
			}
			else {
				// its a NEW user that has never been authenticated - verify the userInfo against PARDOT if enabled
				if(this.appConfig.get().getProspectsAppConfig().isEnableProspectsApp()) {
				    ProspectsAppProspectListMembership membership = this.prospectsAppService.get().verifyUserInPardotList(userInfo.getEmail(), null, appConfig.get().getProspectsAppConfig().getInviteListId(), true);
				    if(membership == null) {
						//eventLogService.get().save(new EventLog(userInfo.getEmail(), EventLog.USER_EMAIL_NOT_FOUND_DURING_VERIFY));
				        throw new WebApplicationException("Email address is not recognized. Please contact Sales or reenter valid email.", HttpStatus.FORBIDDEN_403);
				    } else {
				    	eventLogService.get().save(new EventLog(userInfo.getEmail(), EventLog.USER_AUTHENTICATED));
				    }
				}

				// save the user including calculating capacity at DC
				savedId = mgmtApiService.get().saveUser(userInfo);
			}
		} catch(WebApplicationException e) {
			throw e;
		} catch(Exception e) {
			eventLogService.get().save(new EventLog(userInfo.getEmail(), EventLog.ERROR_DURING_VERIFY, Maps.newHashMap(ImmutableMap.of("error", e.getMessage()))));

			logger.error("Error during save of userInfo:"+userInfo+" to DB", e);
			throw new WebApplicationException(e.getMessage(), e);
		}

		return Response.ok().build();
	}

	/**
	 * saves apps
	 * 
	 * @param request
	 * @param userInfo
	 * @return
	 */
	@Timed
	@POST
	@Path("/configureusertemplate/{email}")
    @Produces(value = MediaType.APPLICATION_JSON)
	@CacheControl(noCache = true)
	public Response configureUserTemplate(@Context HttpServletRequest request, @PathParam("email") String userEmail) {
		String savedId = null;
		Response response = Response.ok().build();

		try {

			if( StringUtils.isBlank(userEmail) ) {
				throw new WebApplicationException("Email is invalid. Please enter a valid email address.", HTTP_ERROR_INPUT_VALIDATION);
			}
			
			// convert to all lower case first.
			userEmail = userEmail.toLowerCase(Locale.ENGLISH);
			userEmail = userEmail.trim();
			
			UserInfo userInfo = userInfoService.get().getByEmail(userEmail);
			if(userInfo == null) {
				//response = Response.status(HttpStatus.CONFLICT_409).build();
				throw new WebApplicationException("User with email:"+userEmail+" is not registered. Please registered the user.", HttpStatus.FORBIDDEN_403);
			}
			
			// Check and make sure target server is UP before we schedule a job on it.
			// We do this only once just to make sure everything is setup correctly before
			// we start scheduling jobs.
			if( checkedDCs.containsKey(userInfo.getMgmtServerName()) == false ) {
				String serverId = null;
				try {
			    	serverId = mgmtApiService.get().getServerId( userInfo.getMgmtServerName() );
			    	checkedDCs.put( userInfo.getMgmtServerName(), "CHECKED");
				} catch (Exception e) {
			    	logger.info("Cannot reach {}. Exception: {}",userInfo.getMgmtServerName(), e );
			    	throw new WebApplicationException("Unable to contact server: " + userInfo.getMgmtServerName(), HTTP_ERROR_INPUT_VALIDATION );
				}			
			}
			
		} catch(WebApplicationException e) {
			throw e;
		} catch(Exception e) {
			logger.error("Error during reload of zipcodes into DB", e);
			throw new WebApplicationException(e.getMessage(), e);
		}

		return Response.ok().build();
	}

	private void verifyString(String toExamine, String propertyName ) {
		
		if( toExamine == null ) {
			throw new WebApplicationException( propertyName + " is invalid. Please enter a valid " + propertyName +".", HTTP_ERROR_INPUT_VALIDATION);
		}
		
		if( toExamine.length() > MAX_INPUT_ALLOWED ) {
			throw new WebApplicationException( propertyName + " contains too many characters. Please enter a valid value.", HTTP_ERROR_INPUT_VALIDATION);
		}

	    Matcher matcher = INVALID_CHARS_PATTERN.matcher(toExamine);
	    boolean illegal = matcher.find();
	    
	    if( illegal == true ) {
			throw new WebApplicationException( propertyName + " contains invalid characters. Please enter a valid value which does not contain the characters \"" + INVALID_CHARS_SHOW + "\".", HTTP_ERROR_INPUT_VALIDATION);
	    }
	    
	    
	}
	/**
	 * saves the list of apps that are used to create a new image/template for this user.  Internally, if the list of apps
	 * is the same as an existing one, we reuse the template by naming the image with the hash of list of apps.
	 * 
	 * @param request
	 * @param userInfo
	 * @return
	 */
	@Timed
	@POST
	@Path("/saveuserapps/{email}")
    @Produces(value = MediaType.APPLICATION_JSON)
	@CacheControl(noCache = true)
	public Response saveUserApps(@Context HttpServletRequest request, @PathParam("email") String userEmail, CreateTemplateRequest createTemplateRequest) {
		String savedId = null;
		Response response = Response.ok().build();

		try {
			
			if( StringUtils.isBlank(userEmail) ) {
				throw new WebApplicationException("Email is invalid. Please enter a valid email address.", HTTP_ERROR_INPUT_VALIDATION);
			}
			
			// convert to all lower case first.
			userEmail = userEmail.toLowerCase(Locale.ENGLISH);
			userEmail = userEmail.trim();
			
			boolean userEmailExists = userInfoService.get().userEmailAlreadyRegistered(userEmail);
		} catch(WebApplicationException e) {
			throw e;
		} catch(Exception e) {
			logger.error("Error during save of app configuration template into DB", e);
			throw new WebApplicationException(e.getMessage(), e);
		}

		return Response.ok().build();
	}

	/**
	 * reloads the zip codes into the DB
	 * 
	 * @param request
	 * @param userInfo
	 */
/*	@Timed
	@POST
	@Path("/reloadlocationdata")
    @Produces(value = MediaType.APPLICATION_JSON)
	@CacheControl(noCache = true)
	public Response reloadLocationData() {
		try {
			locationService.get().loadZipCodesIntoDB();
		} catch(WebApplicationException e) {
			throw e;
		} catch(Exception e) {
			logger.error("Error during reload of zipcodes into DB", e);
			throw new WebApplicationException(e.getMessage(), e);
		}
		
		return Response.ok().build();
	}
*/

	/**
	 * verifies that the user exists in Pardot
	 * 
	 * TODO - 401 returns an HTML page-alter it so that it returns maybe JSON or empty body
	 * 
	 * 
	 * @param userInfo
	 * @return
	 */
	@Timed
	@POST
	@Path("/verifyuser")
	@CacheControl(noCache = true)
    @Produces(value = MediaType.APPLICATION_JSON)
	public Response verifyUser(UserInfo userInfo) {
		boolean verified = false;

		try {
			// Do input validation
			if( userInfo == null ) {
				throw new WebApplicationException("Please specify a valid user.", HTTP_ERROR_INPUT_VALIDATION);
			}

			if( StringUtils.isBlank(userInfo.getEmail()) ) {
				throw new WebApplicationException("Email is invalid. Please enter a valid email address.", HTTP_ERROR_INPUT_VALIDATION);
			}
			
			//We always convert email to lowercase before saving it in DB.
			userInfo.setEmail( userInfo.getEmail().toLowerCase(Locale.ENGLISH) );
			userInfo.setEmail( userInfo.getEmail().trim() );
			
			ProspectsAppConfig prospectsAppConfig = appConfig.get().getProspectsAppConfig();
			ProspectsAppProspectListMembership membership = prospectsAppService.get().verifyUserInPardotList(userInfo.getEmail(), null, prospectsAppConfig.getInviteListId(), true);
			if(membership != null)
				verified = true;
		} catch(WebApplicationException e) {
			//throw e;
			// don't throw because any errors in verifying user in Pardot, we want to send back a 401 Unauthorized and not a 500 (not our internal server)
			verified = false;
		} catch(Exception e) {
			logger.error("Error during verification of user in prospectsApp invite list", e);
			throw new WebApplicationException(e.getMessage(), e);
		}

		// checks to see if verified and if not return 401 unauthorized
		if(verified)
			return Response.ok().build();
		else
			throw new WebApplicationException(HttpStatus.UNAUTHORIZED_401);
	}


	/**
	 * 
	 * @param userEmail
	 */
	@Timed
	@GET
	@Path("/register/userlandingpage/{email}")
	@CacheControl(noCache = true)
    @Produces(value = MediaType.APPLICATION_JSON)
	public Response registerUserReachedLandingPage(@PathParam("email") String email) {
		try {
			logger.info("user with:"+email+" reached landing page");
			if(!StringUtils.isBlank(email)) {
				
				//We always convert email to lowercase before saving it in DB.
				email = email.toLowerCase( Locale.ENGLISH);
				email = email.trim();
				
				eventLogService.get().save(new EventLog(email, EventLog.USER_ON_LANDING_PAGE));
				if(appConfig.get().getProspectsAppConfig().isEnableProspectsApp()) {
					String entityId = prospectsAppUpdateService.get().saveObj(new ProspectsAppUpdate(email, ProspectsAppUpdate.REGISTER_USER_ON_LANDING_PAGE));
					appMessageService.get().save(new AppMessage(ProspectsAppUpdate.class.getSimpleName(), entityId));
					//prospectsAppService.get().addUpdateUserToPardotList(email, appConfig.get().getPardotConfig().getLandingPageListId());
				}
			}
		} catch(Exception e) {
			// we have to swallow the exception because we do not want to interrupt normal processing
			//logger.error("");
		}
		
		return Response.ok().build();
	}

	/**
	 * 
	 * @param userEmail
	 */
	@Timed
	@GET
	@Path("/register/userpickapps/{email}/{personalzipcode}")
	@CacheControl(noCache = true)
    @Produces(value = MediaType.APPLICATION_JSON)
	public Response registerUserReachedPickAppsPage(@PathParam("email") String email, @PathParam("personalzipcode") String personalZipCode) {
		try {
			logger.info("user with:"+email+"/"+personalZipCode+" reached pick apps/template page.");

			if(!StringUtils.isBlank(email)) {

				//We always convert email to lowercase before saving it in DB.
				email = email.toLowerCase( Locale.ENGLISH);
				email = email.trim();
				personalZipCode = personalZipCode.trim();

				eventLogService.get().save(new EventLog(email, 
												EventLog.USER_ON_PICK_APPS_PAGE,
												Maps.newHashMap(ImmutableMap.of("personalZipCode", personalZipCode))));

				if(appConfig.get().getProspectsAppConfig().isEnableProspectsApp()) {
					String entityId = prospectsAppUpdateService.get().saveObj(new ProspectsAppUpdate(email, ProspectsAppUpdate.REGISTER_USER_ON_PICK_APPS_PAGE, Maps.newHashMap(ImmutableMap.of("personalZipCode", personalZipCode))));
					appMessageService.get().save(new AppMessage(ProspectsAppUpdate.class.getSimpleName(), entityId));
					//prospectsAppService.get().addUpdateUserToPardotList(email, appConfig.get().getPardotConfig().getLandingPageListId());
				}
			}
		} catch(Exception e) {
			// we have to swallow the exception because we do not want to interrupt normal processing
			//logger.error("");
		}
		
		return Response.ok().build();
	}

	/**
	 * add user to out of capacity list
	 * @param email
	 */
	@Timed
	@GET
	@Path("/register/useroutofcapacity/{email}")
	@CacheControl(noCache = true)
    @Produces(value = MediaType.TEXT_PLAIN)
	public Response registerUserOutOfCapacity(@PathParam("email") String email) {
		try {
			logger.info("user with:"+email+" reached out of capacity page");
			if(!StringUtils.isBlank(email)) {
				
				//We always convert email to lowercase before saving it in DB.
				email = email.toLowerCase( Locale.ENGLISH);
				email = email.trim();
				
				eventLogService.get().save(new EventLog(email, EventLog.USER_OUT_OF_CAPACITY));
				if(appConfig.get().getProspectsAppConfig().isEnableProspectsApp()) {
					String entityId = prospectsAppUpdateService.get().saveObj(new ProspectsAppUpdate(email, ProspectsAppUpdate.REGISTER_USER_OUT_OF_CAPACITY));
					appMessageService.get().save(new AppMessage(ProspectsAppUpdate.class.getSimpleName(), entityId));
					//prospectsAppService.get().addUpdateUserToPardotList(email, appConfig.get().getPardotConfig().getOutOfCapacityListId());
				}
			}
		} catch(Exception e) {
			// we have to swallow the exception because we do not want to interrupt normal processing
			//logger.error("");
		}
		
		return Response.ok().build();
	}


	/**
	 * add user to confirmed list
	 * @param email
	 */
	@Timed
	@GET
	@Path("/register/userconfirmed/{email}")
	@CacheControl(noCache = true)
    @Produces(value = MediaType.TEXT_PLAIN)
	public Response registerUserConfirmed(@PathParam("email") String email) {
		try {
			logger.info("user with:"+email+" reached Confirmation page");
			if(!StringUtils.isBlank(email)) {
				
				//We always convert email to lowercase before saving it in DB.
				email = email.toLowerCase( Locale.ENGLISH);
				email = email.trim();

				eventLogService.get().save(new EventLog(email, EventLog.USER_CONFIRMED));
				if(appConfig.get().getProspectsAppConfig().isEnableProspectsApp()) {
					String entityId = prospectsAppUpdateService.get().saveObj(new ProspectsAppUpdate(email, ProspectsAppUpdate.REGISTER_USER_CONFIRMED));
					appMessageService.get().save(new AppMessage(ProspectsAppUpdate.class.getSimpleName(), entityId));
					//prospectsAppService.get().addUpdateUserToPardotList(email, appConfig.get().getPardotConfig().getConfirmedListId());
				}
			}
		} catch(Exception e) {
			// we have to swallow the exception because we do not want to interrupt normal processing
			//logger.error("");
		}
		
		return Response.ok().build();
	}


	/**
	 * verifies Google Recaptcha
	 * 
	 * @param grecaptchaResponse
	 * @return
	 */
	private boolean verifyGrecaptchaResponse( String grecaptchaResponse ) {
		try {
			okhttp3.OkHttpClient httpClient = new okhttp3.OkHttpClient();
			okhttp3.RequestBody formBody = new okhttp3.FormBody.Builder()
		        .add("secret", appConfig.get().getGeneralConfig().getGrecaptchaKey())  // our secret key
		        .add("response", grecaptchaResponse)
		        .build();
			okhttp3.Request request = new okhttp3.Request.Builder()
		        .url(appConfig.get().getGeneralConfig().getGrecaptchaServer())
		        .post(formBody)
		        .build();
			
			okhttp3.Response response = httpClient.newCall(request).execute();
			
			if( !response.isSuccessful() ) {
				logger.error( "recaptcha siteverify failed");
				return false;
			}
			String responseStr = response.body().string();
			logger.info( "recaptcha siteverify returned: " + responseStr);
			
			JSONObject obj = new JSONObject(responseStr);
			Object returnCode = obj.get("success");
			if( returnCode != null && returnCode instanceof Boolean ) {
				
				return (Boolean)returnCode;
			}
		} catch( Exception e ) {
			logger.error( "Unable to verify grecaptchaResponse. Error: " + e.getMessage());
			return false;
		}
		return false;
	}	
}
