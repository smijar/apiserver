package com.app.apiserver.services;

import java.net.URLEncoder;

import javax.ws.rs.WebApplicationException;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.app.apiserver.core.AppConfiguration;
import com.app.apiserver.core.EventLog;
import com.app.apiserver.core.ProspectsAppConfig;
import com.app.apiserver.core.ProspectsAppProspectListMembership;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.mashape.unirest.http.exceptions.UnirestException;

import okhttp3.CacheControl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;


/**
 * authenticates for Pardot API
 * 
 * @author smijar
 */
@Singleton
public class ProspectsAppServiceImpl implements ProspectsAppService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private Provider<AppConfiguration> appConfig;
	private String _apiKey;
	//private DurationTracker durationTracker = new DurationTracker();
    private ObjectMapper om;
	public static final MediaType JSON  = MediaType.parse("application/json; charset=utf-8");
	public static final MediaType FORM  = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");
	private Provider<EventLogService> eventLogService;


	@Inject
	private ProspectsAppServiceImpl(Provider<AppConfiguration> appConfig, Provider<EventLogService> eventLogService) {
		this.appConfig = appConfig;
		this.om = new ObjectMapper();
		this.eventLogService = eventLogService;
    }

	private String getApiKey() {
		return _apiKey;
	}

	/**
	 * 
	 * verifies if user is in list, and as a result is used for checking if the user is part of the invite list
	 * for authentication
	 * 
	 * @param userInfo
	 * @return -1 if not in list or the actual membership Id if present in list
	 */
	@Override
	public ProspectsAppProspectListMembership verifyUserInPardotList(String email, Integer prospectId, int listId, boolean bRetry) {
		int membershipId = -1;
		boolean verified = false;
		ResponseBody root = null;
		Response response = null;

		try {
			ProspectsAppConfig prospectsAppConfig = appConfig.get().getProspectsAppConfig();

			if(prospectId == null)
				prospectId = getProspectId(email, true);

			logger.info("verifyUserInPardotList: starting check if prospect:{} is in listId:{}", email, listId);
			String url = String.format(prospectsAppConfig.getApiUrlPrefix()+"/listMembership/version/3/do/read/list_id/%s/prospect_id/%s?user_key=%s&api_key=%s&output=simple&limit=10&format=json",
												listId,
												prospectId,
												prospectsAppConfig.getApiUserKey(),
												getApiKey() 
												);

            OkHttpClient okc = HttpClientSetup.getExternalHttpClient();

            //logger.info("reauthenticatePardot: re-authenticating with Pardot for apiUserEmail:{}", appConfig.get().getPardotConfig().getApiUserEmail());
            Request request = new Request.Builder()
                     .cacheControl(CacheControl.FORCE_NETWORK)
                     .url(url)
                     .build();

            response = okc.newCall(request).execute();

			// this API always returns a 200 (even on missing emails), so we have to check its contents to see if the return value was correct or not
            if(response.code() == HttpStatus.SC_OK) {
                root =  response.body();
                JsonNode rootNode = om.readValue(root.string(),JsonNode.class);

				// verify if it has "err" or "prospect" as a root element
				// output is either: {...., "err":{}} OR is: {...,"result": {}}
                // if err, then it was not found, don't throw an exception
				if(rootNode.has("err")) {
					
					// if we get an error and retry is true then try again after renewing authtoken but this time set retry to false
					if( bRetry == true ) {
						logger.info( "Error in verifyUserInPardotList for email: " + email + " apikey: " + getApiKey() + ". Retrying..."  );
						checkRenewApiAuthToken(true);
						return verifyUserInPardotList(email, prospectId, listId, false);
					}
					
					eventLogService.get().save(new EventLog(email, EventLog.USER_EMAIL_NOT_FOUND_DURING_VERIFY));
					String error = rootNode.get("err").asText();
					logger.warn("verifyUserInPardotList: Error response while trying to check if prospect:"+email+"/"+prospectId+" in list:"+listId+" - error returned:"+error);
					//throw new WebApplicationException("Error while trying to verify prospect:"+prospectsAppConfig.getApiUserEmail()+" - error returned:"+error);
					verified = false;
				} else if(rootNode.has("list_membership")){
					verified = true;

					membershipId = rootNode.get("list_membership").get("id").asInt();
					logger.info("verifyUserInPardotList: successfully checked prospect:{}/{} is part of list:{} with membershipId:{}", email, prospectId, listId, membershipId);
				}
			} else {
				// some other error
				eventLogService.get().save(new EventLog(email, EventLog.USER_EMAIL_NOT_FOUND_DURING_VERIFY));
				logger.warn("verifyUserInPardotList: Prospect not found:"+ email+" in list:"+listId+" responseCode:"+response.code());
				//throw new WebApplicationException("verifyUserInPardotList: Prospect not found:"+ email+" in list:"+listId);
			}
		} catch(WebApplicationException e) {
			throw e;
		} catch(Exception e) {
			// unknown error, log it
			eventLogService.get().save(new EventLog(email, EventLog.ERROR_DURING_VERIFY, Maps.newHashMap(ImmutableMap.of("error", e.getMessage()))));
			logger.error("verifyUserInPardotList: Error while trying to while trying to check prospect:"+appConfig.get().getProspectsAppConfig().getApiUserEmail()+" is in list:"+listId, e);
			throw new WebApplicationException(e.getMessage(), e);
		} finally {
			if(response!=null && response.body()!=null)
				response.body().close();
		}

		if(verified)
			return new ProspectsAppProspectListMembership(listId, prospectId, membershipId);
		else
			return null;
	}

	
	/**
	 * adds a user to the list id in Pardot by first getting his prospect Id, then verifying if he is already
	 * in list and then check either adding a new membership or updating the current one (or just not updating it).
	 */
	@Override
	public int addUpdateUserToPardotList(String email, int listId) {
		int prospectId = getProspectId(email, true);
		ProspectsAppProspectListMembership membership = verifyUserInPardotList(email, prospectId, listId, true);
		if(membership == null)
			return addUpdateUserToPardotList(email, prospectId, listId, true);
		else
			return membership.getListMembershipId();
	}

	/**
	 * adds a user to the list id in Pardot
	 * @param email
	 * @param listId
	 * @return membershipId
	 */
	private int addUpdateUserToPardotList(String email, int prospectId, int listId, boolean bRetry) {
		int membershipId = -1;
		ResponseBody root = null;
		Response response = null;

		try {
			ProspectsAppConfig prospectsAppConfig = appConfig.get().getProspectsAppConfig();

			logger.info("addUserToPardotList: starting add of prospect:{} in listId:{}", email, listId);
			String url = String.format(prospectsAppConfig.getApiUrlPrefix()+"/listMembership/version/3/do/create/list_id/%s/prospect_id/%s?user_key=%s&api_key=%s&output=simple&limit=10&format=json",
												listId,
												prospectId,
												prospectsAppConfig.getApiUserKey(),
												getApiKey() 
												);

            OkHttpClient okc = HttpClientSetup.getExternalHttpClient();
            String body = String.format("api_key=%s&user_key=%s", getApiKey(), prospectsAppConfig.getApiUserKey());
            RequestBody requestBody = RequestBody.create(FORM, body);
            Request request = new Request.Builder()
                     .cacheControl(CacheControl.FORCE_NETWORK)
                     .url(url)
                     .post(requestBody)
                     .build();

            response = okc.newCall(request).execute();

			// this API always returns a 200 (even on missing emails), so we have to check its contents to see if the return value was correct or not
            root =  response.body();
            JsonNode rootNode = om.readValue(root.string(),JsonNode.class);
            if(response.code() == HttpStatus.SC_OK) {
				// verify if it has "err" or "prospect" as a root element
				// output is either: {...., "err":{}} OR is: {...,"result": {}}
            	if(rootNode.has("list_membership")){
					logger.info("addUserToPardotList: successfully added prospect:{} to list:{}", email, listId);
				}
			} else {
				if(rootNode.has("err")) {
					
					// if we get an error and retry is true then try again after renewing authtoken but this time set retry to false
					if( bRetry == true ) {
						logger.info( "Error in addUpdateUserToPardotList for email: " + email + " apikey: " + getApiKey() + ". Retrying..."  );
						checkRenewApiAuthToken(true);
						return addUpdateUserToPardotList(email, prospectId, listId, false );
					}
					// if error string found, return that error message from server
					String error = rootNode.get("err").asText();
					logger.error("addUserToPardotList: Error while trying to add prospect:"+email+" to list:"+listId+" - error returned:"+error);
					throw new WebApplicationException("Error while trying to add prospect:"+email+" to list:"+listId+" - error returned:"+error);
				} 

				// if not, some other unknown error
				logger.error("addUserToPardotList: Error while trying to while trying to add prospect:"+ email+" to list:"+listId+" - might already be a member");
				throw new WebApplicationException("Error while trying to while trying to add prospect:"+ email+" to list:"+listId+" - might already be a member");
			}
		} catch(WebApplicationException e) {
			throw e;
		} catch(Exception e) {
			logger.error("addUserToPardotList: Error while trying to while trying to verify prospect:"+appConfig.get().getProspectsAppConfig().getApiUserEmail()+" - might already be a member", e);
			throw new WebApplicationException(e.getMessage(), e);
		} finally {
			if(response!=null && response.body()!=null)
				response.body().close();
		}

		return membershipId;
	}

	/**
	 * authenticates and gets a new API key if key expired in 1 hour
	 * 
	 * @param email
	 * @param password
	 * @param userKey
	 * @return
	 * 
	 * @throws UnirestException 
	 */
	private synchronized String checkRenewApiAuthToken(boolean forceRenewal) {
		//boolean renewToken = durationTracker.isLongerThanSeconds(3400);
		ResponseBody root = null;
		Response response = null;

//		if(renewToken)
//			durationTracker.reset();
		try {
			if(getApiKey() == null || forceRenewal) {

				ProspectsAppConfig prospectsAppConfig = appConfig.get().getProspectsAppConfig();

				logger.info("checkPardotRenewApiAuthToken: starting renewal of prospectsApp api token for apiuser:{}", prospectsAppConfig.getApiUserEmail());

				OkHttpClient okc = HttpClientSetup.getExternalHttpClient();
		
				//logger.info("reauthenticatePardot: re-authenticating with Pardot for apiUserEmail:{}", appConfig.get().getPardotConfig().getApiUserEmail());
				String url = prospectsAppConfig.getApiUrlPrefix()+"/login/version/3?format=json";
				String body = String.format("email=%s&password=%s&user_key=%s", prospectsAppConfig.getApiUserEmail(), prospectsAppConfig.getApiPassword(), prospectsAppConfig.getApiUserKey());
				RequestBody requestBody = RequestBody.create(FORM, body);
				Request request = new Request.Builder()
						.cacheControl(CacheControl.FORCE_NETWORK)
						.url(url)
						.post(requestBody)
						.build();
		
				response = okc.newCall(request).execute();
		
				// this API always returns a 200 (even on missing emails), so we have to check its contents to see if the return value was correct or not
				if(response.code() == HttpStatus.SC_OK) {
				    root =  response.body();
				    _apiKey = om.readValue(root.string(),JsonNode.class).get("api_key").asText();
				    if(this.getApiKey() == null) {
						logger.error("checkPardotRenewApiAuthToken: Error while trying to renew Pardot Auth Token for api user:"+prospectsAppConfig.getApiUserEmail()+" - token returned was null, response body was:"+root.string());
						throw new WebApplicationException("Error while trying to renew Pardot Auth Token for api user:"+prospectsAppConfig.getApiUserEmail()+" - token returned was null, response body was:"+root.string());
					}
					logger.info("checkPardotRenewApiAuthToken: checkPardotRenewApiAuthToken: successfully renewed prospectsApp api token for apiuser:{}", prospectsAppConfig.getApiUserEmail());
				} else {
					logger.error("checkPardotRenewApiAuthToken: checkPardotRenewApiAuthToken: Error while trying to renew Pardot Auth Token for api user:"+prospectsAppConfig.getApiUserEmail());
					throw new WebApplicationException("Error while trying to renew Pardot Auth Token for api user:"+prospectsAppConfig.getApiUserEmail());
				}
			}
		} catch(WebApplicationException e) {
			throw e;
		}  catch(Exception e) {
			logger.error("checkPardotRenewApiAuthToken: Error while trying to reauthenticate with Pardot for apiUser:"+appConfig.get().getProspectsAppConfig().getApiUserEmail(), e);
			throw new WebApplicationException(e.getMessage(), e);
		} finally {
			if(response!=null && response.body()!=null)
				response.body().close();
		}
		return _apiKey;
	}


	/**
     * updating a Pardot field - will be used by OTP handling code
     * 
     * @param userInfo
     */
    @Override
    public boolean updateFieldInPardot(String email, String fieldName, String fieldValue, boolean bRetry) {
        boolean verified = false;
        ResponseBody root = null;
        Response response = null;

        getProspectId(email, true);

        try {
            if(StringUtils.isBlank(fieldName)) {
                logger.error("updateFieldInPardot: Empty/blank fieldName specified during update field of  prospect:"+email);
                verified = false;
            }
            
            ProspectsAppConfig prospectsAppConfig = appConfig.get().getProspectsAppConfig();

            logger.info("updateFieldInPardot: starting updating of prospect:{}", email);
            
            OkHttpClient okc = HttpClientSetup.getExternalHttpClient();
            
            String url = String.format(prospectsAppConfig.getApiUrlPrefix()+"/prospect/version/3/do/update/email/%s?%s=%s&user_key=%s&api_key=%s&output=simple&format=json",
            		email,
                    fieldName, fieldValue,
                    prospectsAppConfig.getApiUserKey(),
                    getApiKey() 
                    );
            fieldValue =StringUtils.trimToEmpty(fieldValue);

            fieldName = StringUtils.trimToEmpty(fieldName);
            String body = String.format("%s=%s&api_key=%s&user_key=%s", fieldName, URLEncoder.encode(fieldValue), getApiKey(), prospectsAppConfig.getApiUserKey());
            //logger.info("updateFieldInPardot: OTP url is:{}", body);
            RequestBody requestBody = RequestBody.create(FORM, body);
            Request request = new Request.Builder()
                     .cacheControl(CacheControl.FORCE_NETWORK)
                     .url(url).post(requestBody)
                     .build();
            
            response = okc.newCall(request).execute();
            

            // this API always returns a 200 (even on missing emails), so we have to check its contents to see if the return value was correct or not
            if(response.code() == HttpStatus.SC_OK) {
                root =  response.body();
                JsonNode rootNode = om.readValue(root.string(),JsonNode.class);
                
                // verify if it has "err" or "prospect" as a root element
                // output is either: {...., "err":{}} OR is: {...,"result": {}}
                if(rootNode.has("err")) {
                	
					// if we get an error and retry is true then try again after renewing authtoken but this time set retry to false
					if( bRetry == true ) {
						logger.info( "Error in updateFieldInPardot for email: " + email + " apikey: " + getApiKey() + ". Retrying..."  );
						checkRenewApiAuthToken(true);
						return updateFieldInPardot(email, fieldName, URLEncoder.encode(fieldValue), false);
					}
                	
                    String error = rootNode.get("err").asText();
                    logger.error("updateFieldInPardot: Error while trying to update "+fieldName+" in prospect:"+email+" - error returned:"+error);
                    //throw new WebApplicationException("Error while trying to verify prospect:"+prospectsAppConfig.getApiUserEmail()+" - error returned:"+error);
                    verified = false;
                } else if(rootNode.has("prospect")){
                    verified = true;
                    logger.info("updateFieldInPardot: successfully updated prospect:{}", email);
                }
            } else {
                logger.error("updateFieldInPardot: Error while trying to while trying to update "+fieldName+" in prospect:"+email);
                throw new WebApplicationException("Error while trying to while trying to update "+fieldName+" in prospect:"+email);
            }
        } catch(WebApplicationException e) {
            throw e;
        } catch(Exception e) {
            logger.error("updateFieldInPardot: Error while trying to while trying to update "+fieldName+" in prospect:"+email);
            throw new WebApplicationException(e.getMessage(), e);
		} finally {
			if(response!=null && response.body()!=null)
				response.body().close();
		}

        return verified;
    }


    /**
     * queries prospectsApp to get the prospect Id by email
     * 
     * @param email
     * @return
     */
    @Override
    public int getProspectId(String email, boolean bRetry) {
		int prospectId = -1;
		ResponseBody root = null;
		Response response = null;

		try {
			ProspectsAppConfig prospectsAppConfig = appConfig.get().getProspectsAppConfig();

			logger.info("getProspectId: starting verification of prospect:{}", email);
			String url = String.format(prospectsAppConfig.getApiUrlPrefix()+"/prospect/version/3/do/read/email/%s?user_key=%s&api_key=%s&output=simple&limit=10&format=json",
												email,
												prospectsAppConfig.getApiUserKey(),
												getApiKey()
												);

            OkHttpClient okc = HttpClientSetup.getExternalHttpClient();
            
            //logger.info("reauthenticatePardot: re-authenticating with Pardot for apiUserEmail:{}", appConfig.get().getPardotConfig().getApiUserEmail());
            Request request = new Request.Builder()
                     .cacheControl(CacheControl.FORCE_NETWORK)
                     .url(url)
                     .build();
            
            response = okc.newCall(request).execute();

			// this API always returns a 200 (even on missing emails), so we have to check its contents to see if the return value was correct or not
            if(response.code() == HttpStatus.SC_OK) {
                root =  response.body();
                JsonNode rootNode = om.readValue(root.string(),JsonNode.class);

				// verify if it has "err" or "prospect" as a root element
				// output is either: {...., "err":{}} OR is: {...,"result": {}}
				if(rootNode.has("err")) {
					
					logger.info("Error while getting prospect ID for email: " + email + " apikey: " + getApiKey() );
					String error = rootNode.get("err").asText();
					if(error.equalsIgnoreCase("Invalid API key or user key") && ( bRetry == true ) ) {
						// invalid API token
						this._apiKey = null;
						logger.error("getProspectId: Error while trying to get prospect:"+email+" - error returned:"+error+" will trying renewing token and try again");
						logger.warn("getProspectId: renewing API token for prospectsApp while getting prospect: "+email);

						checkRenewApiAuthToken(true);
						return getProspectId(email, false);
						
					} else if(error.equalsIgnoreCase("Invalid prospect email address")) {
						// if invalid prospect email address, then he is not on the invite list and has to be returned a 401 specifically
						logger.error("getProspectId: Error while trying to get prospect:"+email+" - error returned:"+error);
						throw new WebApplicationException(error, HttpStatus.SC_UNAUTHORIZED);
					} else {
						
						if( bRetry == true ) {
							logger.info( "Error in getProspectId for email: " + email + " apikey: " + getApiKey() + ". Retrying..."  );
							checkRenewApiAuthToken(true);
							return getProspectId(email, false);
						}
						logger.error("getProspectId: Error while trying to get prospect:"+email+" - error returned:"+error);
						throw new WebApplicationException("Error while trying to verify prospect:"+email+" - error returned:"+error);
					}
				} else if(rootNode.has("prospect")){
					prospectId = rootNode.get("prospect").get("id").asInt();
					logger.info("getProspectId: successfully retrieved user:{} with prospectId:{}", email, prospectId);
				}
			} else {
				logger.error("getProspectId: Error while trying to retrieve prospect:"+email);
				throw new WebApplicationException("getProspectId: Error while trying retrieve prospect:"+email);
			}
		} catch(WebApplicationException e) {
			throw e;
		} catch(Exception e) {
			logger.error("getProspectId: Error while trying to while trying to verify prospect:"+email, e);
			throw new WebApplicationException(e.getMessage(), e);
		} finally {
			if(response!=null && response.body()!=null)
				response.body().close();
		}

		return prospectId;
	}
}
