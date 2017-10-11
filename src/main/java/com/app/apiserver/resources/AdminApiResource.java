package com.app.apiserver.resources;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.app.apiserver.core.AppConfiguration;
import com.app.apiserver.core.AppMessage;
import com.app.apiserver.core.UserInfo;
import com.app.apiserver.core.MgmtServerConfig;
import com.app.apiserver.messaging.AppMessageService;
import com.app.apiserver.services.UserInfoService;
import com.app.apiserver.services.LocationService;
import com.app.apiserver.services.MgmtApiService;
import com.app.apiserver.services.ProspectsAppService;
import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Provider;

import io.dropwizard.jersey.caching.CacheControl;

/**
 * this class provides access to admin needed queries
 * 
 * @author smijar
 */
@Path("/v1/admin")
@Produces(MediaType.APPLICATION_JSON)
@SuppressWarnings("unused")
public class AdminApiResource  {
	private static Logger logger = LoggerFactory.getLogger(AdminApiResource.class);
	private Provider<MgmtApiService> mgmtApiService;
	private Provider<AppConfiguration> appConfig;
	private Provider<LocationService> locationService;
	private Provider<ProspectsAppService> prospectsAppService;
	private Provider<UserInfoService> userInfoService;
	private Provider<AppMessageService> appMessageService;
	
	private static final int HTTP_ERROR_INPUT_VALIDATION = 510;


	@Inject
	public AdminApiResource(Provider<AppConfiguration> appConfig, 
			Provider<MgmtApiService> mgmtApiService, 
			Provider<LocationService> locationService,
			Provider<ProspectsAppService> prospectsAppService,
			Provider<UserInfoService> userInfoService,
			Provider<AppMessageService> appMessageService) {
		this.appConfig = appConfig;
		this.mgmtApiService = mgmtApiService;
		this.locationService = locationService;
		this.prospectsAppService = prospectsAppService;
		this.userInfoService = userInfoService;
		this.appMessageService = appMessageService;
	}

	/**
	 * simple call to identify the resource
	 * @return
	 */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String get() {
        return "AdminApiResource";
    }

	/**
	 * a protected resource that returns the list of userInfos
	 * 
	 * @param userInfo
	 * @return
	 */
	@Timed
	@GET
	@Path("/userinfos")
	@CacheControl(noCache = true)
    @Produces(value = MediaType.APPLICATION_JSON)
	public List<UserInfo> getUserInfoList() {
		boolean verified = false;
		List<UserInfo> userInfoList = Lists.newArrayList();

		try {
			userInfoList = userInfoService.get().find(userInfoService.get().createQuery()).asList();
		} catch(WebApplicationException e) {
			throw e;
		} catch(Exception e) {
			logger.error("Error during reload of zipcodes into DB", e);
			throw new WebApplicationException(e.getMessage(), e);
		}

		return userInfoList;
	}

	/**
	 * a protected resource that returns the list of userInfos
	 * 
	 * @param userInfo
	 * @return
	 */
	@Timed
	@GET
	@Path("/appmessages")
	@CacheControl(noCache = true)
    @Produces(value = MediaType.APPLICATION_JSON)
	public List<AppMessage> getAppMessageList() {
		boolean verified = false;
		List<AppMessage> appMessageList = Lists.newArrayList();

		try {
			appMessageList = appMessageService.get().find(appMessageService.get().createQuery()).asList();
		} catch(WebApplicationException e) {
			throw e;
		} catch(Exception e) {
			logger.error("Error during reload of zipcodes into DB", e);
			throw new WebApplicationException(e.getMessage(), e);
		}

		return appMessageList;
	}
	
	// ADMIN rest API to get the ID of a user in Management server given an Email address
	@Timed
	@GET
	@Path("/getfromdc/{userEmail}")
	@CacheControl(noCache = true)
    @Produces(value = MediaType.APPLICATION_JSON)
	public Map<String,String> getUserIDFromDC(@PathParam("userEmail") String userEmail) {
		boolean verified = false;
		Map<String,String> result = Maps.newHashMap();
		
		try {
			if( StringUtils.isBlank(userEmail) ) {
				throw new WebApplicationException("Email is invalid. Please enter a valid email address.", HTTP_ERROR_INPUT_VALIDATION);
			}
			
			userEmail = userEmail.toLowerCase(Locale.ENGLISH);
			userEmail = userEmail.trim();

			// Look for the user in all DCs
			List<MgmtServerConfig> configs = appConfig.get().getMgmtServersConfig().getMgmtServerConfigs();
			if( configs != null ) {
				for( int i = 0; i < configs.size(); i++ ) {
					
					String sID = mgmtApiService.get().getUserIDFromDC(configs.get(i), userEmail);
					
					if( !StringUtils.isBlank(sID) ) {
						result.put(sID, userEmail + " in " + configs.get(i).getName() );
					}
				}
			}
			
		} catch(WebApplicationException e) {
			throw e;
		} catch(Exception e) {
			logger.error("Error during reload of zipcodes into DB", e);
			throw new WebApplicationException(e.getMessage(), e);
		}

		return result;
	}
	
	// ADMIN rest API reset a user given a EMAIL address
	@Timed
	@POST
	@Path("/resetuser/{userEmail}")
	@CacheControl(noCache = true)
    @Produces(value = MediaType.APPLICATION_JSON)
	public UserInfo resetuser(@PathParam("userEmail") String userEmail) {

		UserInfo userInfoDB = null;
		try {
			// Do input validation

			if( StringUtils.isBlank(userEmail) ) {
				throw new WebApplicationException("Email is invalid. Please enter a valid email address.", HTTP_ERROR_INPUT_VALIDATION);
			}

			userEmail = userEmail.toLowerCase(Locale.ENGLISH);
			userEmail = userEmail.trim();
			
			userInfoDB = userInfoService.get().getByEmail(userEmail);
			if( userInfoDB != null ) {
				// update user state to NEW
				userInfoService.get().updateUserStatusField( userInfoDB.getId(), UserInfo.NEW);
				//Delete the user from management console if present
				mgmtApiService.get().deleteUserFromDC(userInfoDB.getMgmtServerName(), userEmail);
				
			} else {
				
				// Look for the user in all DCs
				List<MgmtServerConfig> configs = appConfig.get().getMgmtServersConfig().getMgmtServerConfigs();
				if( configs != null ) {
					for( int i = 0; i < configs.size(); i++ ) {
						mgmtApiService.get().deleteUserFromDC(configs.get(i).getName(), userEmail);
					}
				}
				
			}
			
			
		} catch(WebApplicationException e) {
			throw e;
		} catch(Exception e) {
			logger.error("Error during reload of zipcodes into DB", e);
			throw new WebApplicationException(e.getMessage(), e);
		}

		return userInfoDB;
	}
}
