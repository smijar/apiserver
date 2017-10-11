package com.app.apiserver.services;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.app.apiserver.core.AppConfiguration;
import com.app.apiserver.core.AppMessage;
import com.app.apiserver.core.CreateTemplateRequest;
import com.app.apiserver.core.EventLog;
import com.app.apiserver.core.ExternalUserAccount;
import com.app.apiserver.core.Domain;
import com.app.apiserver.core.LicenseType;
import com.app.apiserver.core.VDTemplate;
import com.app.apiserver.core.Image;
import com.app.apiserver.core.MgmtServerConfig;
import com.app.apiserver.core.MgmtServerDownException;
import com.app.apiserver.core.MgmtServersConfig;
import com.app.apiserver.core.User;
import com.app.apiserver.core.UserInfo;
import com.app.apiserver.core.UserType;
import com.app.apiserver.messaging.AppMessageService;
import com.app.apiserver.resources.MgmtApiResource;
import com.google.common.collect.Maps;
import com.google.inject.Provider;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class MgmtApiServiceImpl implements MgmtApiService {

	private static Logger logger = LoggerFactory.getLogger(MgmtApiResource.class);
	private static String LDAP_PERSON_SCHEMA = "inetOrgPerson";
	//private static String PREFIX = "uid=";
	//private static String SUFFIX = ",ou=Test,dc=droidcloud,dc=mobi";
	//private static int HTTP_ERROR_BASE_VDIMAGE_MISSING = 591;
	//private static String IMAGE_ACTIVE_STATUS = "ACTIVE";

	//private static int HTTP_ERROR_USER_ALREADY_IN_LDAP = 584; //for email already exists in ldap (basically any ldapException)
	//private static int HTTP_ERROR_INVALID_USER_OBJECT = 613; // Invalid User object (if fields are invalid or missing like if the licenseType or domainId is missing)

	private Provider<UserInfoService> userInfoService;
	private Provider<AppMessageService> messageService;
	private Provider<LocationService> locationService;

	private Provider<AppConfiguration> appConfig;
	private Provider<ProspectsAppService> prospectsAppService;
	private Provider<EventLogService> eventLogService;

	@Inject
	public MgmtApiServiceImpl(Provider<AppConfiguration> appConfig,
								Provider<UserInfoService> userInfoService,
								Provider<AppMessageService> messageService,
								Provider<LocationService> locationService,
								Provider<ProspectsAppService> prospectsAppService,
								Provider<EventLogService> eventLogService) {
		this.appConfig = appConfig;

		this.userInfoService = userInfoService;
		this.messageService = messageService;
		this.locationService = locationService;
		this.prospectsAppService = prospectsAppService;
		this.eventLogService = eventLogService;
	}


    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String get() {
        return "authenticateresource";
    }


	/**
	 * Resource: simple call to check connectivity to management server and get its unique id
	 */
	@Override
	public String getServerId(String mgmtServerName) {
		String uuid = "";
        try {
        	logger.info("getServerId: getting serverId");

        	MgmtServerConfig mgmtServerConfig = getMgmtServerConfigByName(mgmtServerName);

            //HttpResponse<JsonNode> response = Unirest.get("https://httpbin.org/get?show_env=1").asJson();
        	//+":"+mgmtServerConfig.getPort()
        	String url = "https://"+mgmtServerConfig.getHost()+"/api/v1/serverinfo/uuid";
            HttpResponse<String> response = Unirest.get(url).asString();
            if (response.getStatus() == HttpStatus.SC_OK) {
                uuid = response.getBody();
            } else {
                throw new WebApplicationException("getServerId: getting serverId returned code "+response.getStatus());
            }

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
	 * gets the primary domain id for creating new users
	 */
	private String getPrimaryDomainId(MgmtServerConfig mgmtServerConfig) {
		String domainId = null;
		Domain[] dcDomains = {};

        try {
        	//MgmtServerConfig mgmtServerConfig = appConfig.get().getMgmtServersConfig().getMgmtServerConfigs().get(mgmtServerIndex);
        	String domainName = mgmtServerConfig.getDomainName();

        	logger.info("getPrimaryDomainId: checking domains for domainName:{}", domainName);

            //HttpResponse<JsonNode> response = Unirest.get("https://httpbin.org/get?show_env=1").asJson();
        	//+":"+mgmtServerConfig.getPort()
        	String url = "https://"+mgmtServerConfig.getHost()+"/api/v1/domains";
            HttpResponse<Domain[]> response = Unirest.get(url).asObject(Domain[].class);
            dcDomains = response.getBody();

            logger.info("getPrimaryDomainId: got response status from server", response.getStatus());
            logger.info("getPrimaryDomainId: getting domainId for:{}", domainName);

            for(Domain dcDomain:dcDomains) {
            	if(dcDomain.getName().equalsIgnoreCase(mgmtServerConfig.getDomainName())) {
            		domainId = dcDomain.getId();
            		break;
            	}
            }
            logger.info("getPrimaryDomainId:{} for domain:{}", domainId, domainName);
		} catch(WebApplicationException e) {
			throw e;
        } catch (UnirestException e) {
            e.printStackTrace();
            throw new WebApplicationException(e.getMessage(), e);
		} catch(Exception e) {
			logger.error("getPrimaryDomainId:Error while trying to getPrimaryDomainId", e);
			throw new WebApplicationException(e.getMessage(), e);
		}

        return domainId;
	}

	/**
	 * gets the primary ldap config id for creating new users
	 */
	private String getPrimaryLdapConfigId(MgmtServerConfig mgmtServerConfig) {
		String ldapConfigId = null;

        try {
        	//MgmtServerConfig mgmtServerConfig = appConfig.get().getMgmtServersConfig().getMgmtServerConfigs().get(mgmtServerIndex);
        	String ldapConfigName = mgmtServerConfig.getLdapConfigName();

    		logger.info("getPrimaryLdapConfigId: retrieving ldapConfigs from server for:{}", ldapConfigName);

            //HttpResponse<JsonNode> response = Unirest.get("https://httpbin.org/get?show_env=1").asJson();
        	//+":"+mgmtServerConfig.getPort()
        	String url = "https://"+mgmtServerConfig.getHost()+"/api/v1/ldapconfigs";
        	
            HttpResponse<JsonNode> response = Unirest.get(url).asJson();

            logger.info("getPrimaryLdapConfigId: got response status from server", response.getStatus());

            JsonNode root = response.getBody();
            JSONArray ldapConfigsJsonArray = root.getArray();
            
            if(ldapConfigsJsonArray.length()==0)
            	throw new WebApplicationException("There were no LdapConfigs were not found on the server");
            
            JSONObject ldapConfigJson = ldapConfigsJsonArray.getJSONObject(0);
            String name = ldapConfigJson.getString("name");
            if(ldapConfigName.equalsIgnoreCase(name))
            	ldapConfigId = ldapConfigJson.getString("_id");
            else
            	throw new WebApplicationException("LdapConfig named:"+ldapConfigName+" was not found on mgmt server");

            logger.info("getPrimaryLdapConfigId: got ldapConfigId for:{} as:{}", ldapConfigName, ldapConfigId);
		} catch(WebApplicationException e) {
			throw e;
        } catch (UnirestException e) {
        	logger.error("getPrimaryLdapConfigId: while trying to getPrimaryLdapConfigId", e);
            throw new WebApplicationException(e.getMessage(), e);
		} catch(Exception e) {
			logger.error("getPrimaryLdapConfigId:Error while trying to getPrimaryLdapConfigId", e);
			throw new WebApplicationException(e.getMessage(), e);
		}

        return ldapConfigId;
	}
	
	/**
	 * gets the cloudconfig for the user to be allocated
	 */
	private String getPrimaryCloudConfigId(MgmtServerConfig mgmtServerConfig) {
		String cloudConfigId = null;

        try {
        	//MgmtServerConfig mgmtServerConfig = appConfig.get().getMgmtServersConfig().getMgmtServerConfigs().get(mgmtServerIndex);
        	String cloudConfigName = mgmtServerConfig.getCloudConfigName();

    		logger.info("getPrimaryCloudConfigId: retrieving cloudConfigs from server for:{}", cloudConfigName);

            //HttpResponse<JsonNode> response = Unirest.get("https://httpbin.org/get?show_env=1").asJson();
        	//+":"+mgmtServerConfig.getPort()
        	String url = "https://"+mgmtServerConfig.getHost()+"/api/v1/cloudconfigs";
        	
            HttpResponse<JsonNode> response = Unirest.get(url).asJson();

            logger.info("getPrimaryLdapConfigId: got response status from server:{}", response.getStatus());

            JsonNode root = response.getBody();
            JSONArray cloudConfigsJsonArray = root.getArray();
            
            if(cloudConfigsJsonArray.length()==0)
            	throw new WebApplicationException("There were no cloudConfigs not found on the server");
            
            JSONObject cloudConfigJson = cloudConfigsJsonArray.getJSONObject(0);
            String name = cloudConfigJson.getString("name");
            if(cloudConfigName.equalsIgnoreCase(name))
            	cloudConfigId = cloudConfigJson.getString("_id");
            else
            	throw new WebApplicationException("CloudConfig named:"+cloudConfigName+" was not found on mgmt server");

            logger.info("getPrimaryCloudConfigId: got cloudConfigId:{} for cloudConfig named:{}", cloudConfigId, cloudConfigName);
		} catch(WebApplicationException e) {
			throw e;
        } catch (UnirestException e) {
			logger.error("getPrimaryCloudConfigId while trying to getPrimaryCloudConfigId", e);
            throw new WebApplicationException(e.getMessage(), e);
		} catch(Exception e) {
			logger.error("getPrimaryCloudConfigId while trying to getPrimaryCloudConfigId", e);
			throw new WebApplicationException(e.getMessage(), e);
		}

        return cloudConfigId;
	}

	
	/**
	 * gets the aggregate for the user to be allocated
	 */
	//public String getPrimaryAggregate(MgmtServerConfig mgmtServerConfig) {
		//MgmtServerConfig mgmtServerConfig = appConfig.get().getMgmtServersConfig().getMgmtServerConfigs().get(mgmtServerIndex);
		//return mgmtServerConfig.getAggregateName();
	//}
	

	/**
	 * adds a new user to ldap
	 */
//	private ExternalUserAccount addUserToLdap(int mgmtServerIndex, ExternalUserAccount externalUserAccount) {
//
//		try{
//			logger.info("addUser: received user:{}, adding to ldap first", externalUserAccount);
//
//			MgmtServerConfig mgmtServerConfig = appConfig.get().getMgmtServersConfig().getMgmtServerConfigs().get(mgmtServerIndex);
//
//        	//HttpResponse<JsonNode> response = Unirest.get("https://httpbin.org/get?show_env=1").asJson();
//        	// Known errpr return codes
//        	// 584 - for email already existing in ldap (or any other ldap exception while adding a new user)
//        	String url = "https://"+mgmtServerConfig.getHost()+":"+Integer.toString(mgmtServerConfig.getPort())+"/api/v1/externaluseraccounts?randomizeCN=true";
//            HttpResponse<ExternalUserAccount> response = Unirest.post(url)
//					            										.header("Content-Type", "application/json")
//					            										.header("Accept", "application/json")
//					            										.header("X-Agent-Type", "DCManager")
//					            										.body(externalUserAccount)
//					            										.asObject(ExternalUserAccount.class);
//            logger.info("addUser: addToLdap response:{}", response.toString());
//
//            if(response.getStatus() == HttpStatus.SC_OK) {
//            	logger.info("addUser: add user to ldap succeeded, now adding user to mgmt and allocating VD");
//            } else {
//        		logger.error("addUser: Error status code:"+response.getStatus()+" and response status text:"+response.getStatusText()+" for user:"+externalUserAccount.toString());
//        		throw new WebApplicationException("addUser: Error status code:"+response.getStatus()+" and response status text:"+response.getStatusText()+" for user:"+externalUserAccount.toString());
//            }
//		} catch(WebApplicationException e) {
//			throw e;
//		} catch(UnirestException e) {
//			logger.error("addUserToLdap:Error while trying to add user to ldap", e);
//			throw new WebApplicationException(e.getMessage(), e);
//		} catch(Exception e) {
//			logger.error("addUserToLdap:Error while trying to add user to ldap", e);
//			throw new WebApplicationException(e.getMessage(), e);
//		}
//
//		return externalUserAccount;
//	}


	/**
	 * gets a list of existing users
	 * 
	 * @param cloudConfigId
	 * @return
	 */
	private User[] getUserList(MgmtServerConfig mgmtServerConfig, String cloudConfigId) {
		User[] users = null;

        try {
        	//MgmtServerConfig mgmtServerConfig = appConfig.get().getMgmtServersConfig().getMgmtServerConfigs().get(mgmtServerIndex);
        	String cloudConfigName = mgmtServerConfig.getCloudConfigName();

    		logger.info("getUserList: retrieving user list from server repo for:{}", cloudConfigName);

            //HttpResponse<JsonNode> response = Unirest.get("https://httpbin.org/get?show_env=1").asJson();
        	//+":"+mgmtServerConfig.getPort()
        	String url = "https://"+mgmtServerConfig.getHost()+"/api/v1/vdusers";

            HttpResponse<User[]> response = Unirest.get(url).asObject(User[].class);
            users = response.getBody();

            logger.info("getUserList: got response status from server:{}", response.getStatus());

            if(users.length==0)
            	throw new WebApplicationException("There were no users found on the server");

            logger.info("getImageListForCloudConfig: got images.length:{} for cloudConfig:{}", users.length, cloudConfigId);
		} catch(WebApplicationException e) {
			throw e;
        } catch (UnirestException e) {
			logger.error("getImageListForCloudConfig while trying to get images list", e);
            throw new WebApplicationException(e.getMessage(), e);
		} catch(Exception e) {
			logger.error("getImageListForCloudConfig while trying to get images list", e);
			throw new WebApplicationException(e.getMessage(), e);
		}

        return users;
	}
	
	/**
	 * retrieves the list of users from the server and checks to see if the user already exists on the server
	 * NOTE-this checks mgmt servers directly
	 * 
	 * @param cloudConfigId
	 * @param userInfo
	 * @return
	 */
	private boolean doesUserExist(MgmtServerConfig mgmtServerConfig, String cloudConfigId, UserInfo userInfo) {
		User[] users = getUserList(mgmtServerConfig, cloudConfigId);
		boolean exists = false;
		
		for(User user:users) {
			if(user.getExternalUserAccount().getAttributes().get("mail").equalsIgnoreCase(userInfo.getEmail())) {
				exists = true;
			}
		}
		
		return exists;
	}
	
	private DateTime imagesRetrievedTimestamp = new DateTime();
	private Image[] _images = {};

	/**
	 * gets the list of images for the cloud config
	 */
	private Image[] getImageListForCloudConfig(MgmtServerConfig mgmtServerConfig, String cloudConfigId) {

        try {
        	Duration dur = new Duration(imagesRetrievedTimestamp, new DateTime());
        	if( _images.length == 0 || dur.isLongerThan(Duration.standardSeconds(appConfig.get().getGeneralConfig().getImageRetrievalSeconds())) ) {

	        	//MgmtServerConfig mgmtServerConfig = appConfig.get().getMgmtServersConfig().getMgmtServerConfigs().get(mgmtServerIndex);
	        	String cloudConfigName = mgmtServerConfig.getCloudConfigName();
	
	    		logger.info("getImageListForCloudConfig: retrieving app list from server repo for:{}", cloudConfigName);
	
	            //HttpResponse<JsonNode> response = Unirest.get("https://httpbin.org/get?show_env=1").asJson();
	        	//+":"+mgmtServerConfig.getPort()
	        	String url = "https://"+mgmtServerConfig.getHost()+"/api/v1/cloudinfo/"+cloudConfigId+"/images";
	
	            HttpResponse<Image[]> response = Unirest.get(url).asObject(Image[].class);
	            imagesRetrievedTimestamp = new DateTime();
	            _images = response.getBody();
	
	            logger.info("getImageListForCloudConfig: got response status from server:{}", response.getStatus());
	
	            if(_images.length==0)
	            	throw new WebApplicationException("There were no images found on the server");
	
	            logger.info("getImageListForCloudConfig: got images.length:{} for cloudConfig:{}", _images.length, cloudConfigId);

        	} else {
        		return _images;
        	}
		} catch(WebApplicationException e) {
			throw e;
        } catch (UnirestException e) {
			logger.error("getImageListForCloudConfig while trying to get images list", e);
            throw new WebApplicationException(e.getMessage(), e);
		} catch(Exception e) {
			logger.error("getImageListForCloudConfig while trying to get images list", e);
			throw new WebApplicationException(e.getMessage(), e);
		}

        return _images;
	}
	
	private DateTime templatesRetrievedTimestamp = new DateTime();
	VDTemplate[] _templates = null;
	/**
	 * gets the list of images for the cloud config
	 */
	private VDTemplate[] getTemplateListForCloudConfig(MgmtServerConfig mgmtServerConfig, String cloudConfigId) {

        try {
        	Duration dur = new Duration(templatesRetrievedTimestamp, new DateTime());
        	if( _templates == null || _templates.length == 0 || dur.isLongerThan(Duration.standardSeconds(appConfig.get().getGeneralConfig().getImageRetrievalSeconds())) ) {

	        	String cloudConfigName = mgmtServerConfig.getCloudConfigName();
	
	    		logger.info("getTemplateListForCloudConfig: retrieving templates list from server repo for:{}", cloudConfigName);
	
	            //HttpResponse<JsonNode> response = Unirest.get("https://httpbin.org/get?show_env=1").asJson();
	        	//+":"+mgmtServerConfig.getPort()
	        	String url = "https://"+mgmtServerConfig.getHost()+"/api/v1/vdtemplates";
	
	        	HttpResponse<VDTemplate[]> response = Unirest.get(url).header("Content-Type", "application/json").asObject(VDTemplate[].class);
	            imagesRetrievedTimestamp = new DateTime();
	            _templates = response.getBody();
	
	            logger.info("getTemplateListForCloudConfig: got response status from server:{}", response.getStatus());
	
	            if(_templates.length==0)
	            	throw new WebApplicationException("There were no templates found on the server");
	
	            logger.info("getTemplateListForCloudConfig: got templates.length:{} for cloudConfig:{}", _templates.length, cloudConfigId);

        	}
		} catch(WebApplicationException e) {
			throw e;
        } catch (UnirestException e) {
			logger.error("getTemplateListForCloudConfig while trying to get images list", e);
            throw new WebApplicationException(e.getMessage(), e);
		} catch(Exception e) {
			logger.error("getTemplateListForCloudConfig while trying to get images list", e);
			throw new WebApplicationException(e.getMessage(), e);
		}

        return _templates;
	}


	/**
	 * checks to see if template exists or not
	 * 
	 * @param cloudConfigId
	 * @param templateName
	 * @return
	 */
//	private boolean doesTemplateExist(MgmtServerConfig mgmtServerConfig, String cloudConfigId, String templateName) {
//		VDTemplate[] templates = getTemplateListForCloudConfig(mgmtServerConfig, cloudConfigId);
//		boolean exists = false;
//
//		for(VDTemplate template:templates) {
//			if(template.getName().equals(templateName)) {
//				exists = true;
//				break;
//			}
//		}
//
//		return exists;
//	}


	/**
	 * checks to see if the image is on Openstack
	 */
	private boolean doesImageExist(MgmtServerConfig mgmtServerConfig, String cloudConfigId, String imageName) {
		/**
		 * This checks to see if all the components of the published image has been published to Openstack 
		 * and uploaded fully, because otherwise the image will not exist when we create the template for it,
		 * in downstream actions.
		 * 
		 * @return
		 */
		Image[] currentImages = getImageListForCloudConfig(mgmtServerConfig, cloudConfigId);
		boolean found = false;

		if( currentImages != null && currentImages.length > 0 ) {
			for( int i = 0; i < currentImages.length; i++ ) {    				
				if( currentImages[i].getName().compareToIgnoreCase( imageName+Image.SYSTEM_IMAGE_SUFFIX) == 0 ||
						currentImages[i].getName().compareToIgnoreCase( imageName+Image.KERNEL_IMAGE_SUFFIX) == 0 ||
						currentImages[i].getName().compareToIgnoreCase( imageName+Image.USERDATA_IMAGE_SUFFIX) == 0 ||
						currentImages[i].getName().compareToIgnoreCase( imageName+Image.INITRD_IMAGE_SUFFIX) == 0 ) {
					logger.info("published image with name " + imageName + " found");
					found = true;
					break;
				}
			}
		}

		return found;
	}
	
	/**
	 * gets the image object
	 * @param cloudConfigId
	 * @param imageName
	 * @return
	 */	
//	private Image getImageForName(MgmtServerConfig mgmtServerConfig, String cloudConfigId, String imageName) {
//		/**
//		 * This checks to see if all the components of the published image has been published to Openstack 
//		 * and uploaded fully, because otherwise the image will not exist when we create the template for it,
//		 * in downstream actions.
//		 * 
//		 * @return
//		 */
//		Image[] currentImages = getImageListForCloudConfig(mgmtServerConfig, cloudConfigId);
//		Image image = null;
//
//		if( currentImages != null && currentImages.length > 0 ) {
//			for( int i = 0; i < currentImages.length; i++ ) {    				
//				if( currentImages[i].getName().compareToIgnoreCase( imageName+Image.SYSTEM_IMAGE_SUFFIX) == 0 ||
//						currentImages[i].getName().compareToIgnoreCase( imageName+Image.KERNEL_IMAGE_SUFFIX) == 0 ||
//						currentImages[i].getName().compareToIgnoreCase( imageName+Image.USERDATA_IMAGE_SUFFIX) == 0 ||
//						currentImages[i].getName().compareToIgnoreCase( imageName+Image.INITRD_IMAGE_SUFFIX) == 0 ) {
//					logger.info("published image with name " + imageName + " found");
//					image = currentImages[i];
//					break;
//				}
//			}
//		}
//
//		return image;
//	}

	/**
	 * sleeps for n seconds
	 * @param seconds
	 */
	private void sleep(int seconds) {
    	try {TimeUnit.SECONDS.sleep(seconds); } catch(Exception e) {}
    }

    /**
     * wait for image to be published and block, timeout and throws exception if exceeded
     * 
     * @param cloudConfigId
     * @param imageName
     */
	private void waitForImageToBePublished(MgmtServerConfig mgmtServerConfig, String cloudConfigId, String imageName) {
    	DateTime startTime = new DateTime();
    	int timeOutInSeconds = appConfig.get().getGeneralConfig().getImageCreationTimeoutSeconds();
    	boolean exists = false;

    	Duration tDur = Duration.standardSeconds(timeOutInSeconds);
    	Duration endDur = new Duration(startTime, new DateTime());
    	while(endDur.isShorterThan(tDur)) {
    		if(doesImageExist(mgmtServerConfig, cloudConfigId, imageName)) {
    			logger.info("found image:{} in cloudConfig:{}", imageName, cloudConfigId);
    			exists = true;
    			break;
    		}

    		logger.info("waitForImageCreation: waiting 15 seconds for image:{} creation currently at:{} and timeout:{}", imageName, endDur.toStandardSeconds(), tDur.toStandardSeconds());
    		sleep(15);
    		endDur = new Duration(startTime, new DateTime());
    	}

    	if(!exists) {
    		logger.error("Timed out waiting for image:"+imageName+" in cloudConfig:"+cloudConfigId+" to be published.");
    		throw new WebApplicationException("Timed out while waiting for image to be published");
    	} else {
    		logger.info("Finished creating image:{} for cloudConfig:{}", imageName, cloudConfigId);
    	}
	}

	/**
	 * constucts the Dn for the user for the Ldap ExternalUserAccount
	 * 
	 * @param userInfo
	 * @return
	 */
	//private String getDnForUser(int mgmtServerIndex, UserInfo userInfo) {
	//	MgmtServerConfig mgmtServerConfig = appConfig.get().getMgmtServersConfig().getMgmtServerConfigs().get(mgmtServerIndex);
	//	return mgmtServerConfig.getLdapAccountPrefix()+userInfo.getName()+mgmtServerConfig.getLdapAccountSuffix();
	//}

	/**
	 * creates a new external user account for user
	 * 
	 * @param userInfo
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	private ExternalUserAccount getExternalUserAccountForUser(UserInfo userInfo) throws NoSuchAlgorithmException {
		ExternalUserAccount externalUserAccount = new ExternalUserAccount();
		
		//externalUserAccount.setDn(getDnForUser(mgmtServerIndex, userInfo));

		Map<String,String> attrs = Maps.newHashMap();
		attrs.put("mail", userInfo.getEmail());
		attrs.put("cn", userInfo.getName());
		attrs.put("displayName", userInfo.getName());
		attrs.put("sn", userInfo.getSN());
		attrs.put("objectclass", LDAP_PERSON_SCHEMA);
		attrs.put("userPassword", UUID.randomUUID().toString()); //CryptUtils.generateSSHA(userInfo.getPassword().getBytes()));

		externalUserAccount.setAttributes(attrs);

		return externalUserAccount;
	}


	// template creation methods
	/**
	 * creates a new template based on the list of Apps requested
	 */
	private void createImageForUser(MgmtServerConfig mgmtServerConfig, String cloudConfigId, String imageHash, CreateTemplateRequest createTemplateRequest) {
		
	}

	/**
	 * 
	 * creates a template for a user and either reusing an existing image or creating a new one
	 * based on the hash(baseVDImage+sorted-apkList)
	 * 
	 */
	private String createTemplateForUser(MgmtServersConfig mgmtServerConfig, String cloudConfigId, CreateTemplateRequest createTemplateRequest) {
		return "";
	}

	/**
	 * Resource: Adds a new user
	 * @throws MgmtServerDownException 
	 */
	@Override
	public UserInfo addNewUserToSystemAndAllocate(UserInfo userInfo) {

		try{
			logger.info("addUser: received user:{}, adding to ldap, mgmt and then allocating", userInfo);

			// get config for server
			MgmtServerConfig mgmtServerConfig = this.getMgmtServerConfigByName(userInfo.getMgmtServerName());
			if( mgmtServerConfig == null ) {
	            logger.error("Unable to find DC: " + userInfo.getMgmtServerName() + " for user.");
	            throw new WebApplicationException("Unable to find DC: " + userInfo.getMgmtServerName() + " for user.");
			}
			
			// get server id
			logger.info("addUser: checking if we can contact server named:{} first by getting just the serverid", mgmtServerConfig.getName());
			
			String serverId = null;
			try {
			    serverId = getServerId(mgmtServerConfig.getName());
			} catch (Exception e) {
			    logger.info("Cannot reach {} with exeption {}",mgmtServerConfig.getHost(),e);
			    throw new MgmtServerDownException((Throwable)e);
			}
			logger.info("got server id:{}", serverId);

			// get cloudConfigId
        	String cloudConfigId = getPrimaryCloudConfigId(mgmtServerConfig);
        	String templateId = null;

        	// check to see if present on server already
        	logger.info("addUser: checking to see if user is already existing on the server, aborting if so");
        	if(doesUserExist(mgmtServerConfig, cloudConfigId, userInfo)) {
        		logger.info("addUser: user:{} already exists on the cloudConfigId:{} on server:{}", userInfo, cloudConfigId, mgmtServerConfig.getName());
        		throw new WebApplicationException("Error: user with email:"+userInfo.getEmail()+" already exists on the server");
        	}

        	//---------------------------------------------------------------
        	// done with template id stuff
            // add user to ldap and mgmt and allocate VD for the user as well
			logger.info("addUser: converting userInfo:{} to user");

            User newUser = new User();
            ExternalUserAccount externalUserAccount = getExternalUserAccountForUser(userInfo);

            newUser.setDomainId(getPrimaryDomainId(mgmtServerConfig));

            newUser.setExternalUserAccount(externalUserAccount);
            newUser.setLdapConfigId(getPrimaryLdapConfigId(mgmtServerConfig));
            newUser.setUserType(UserType.END_USER.getName());
            newUser.setUserLicenseType(LicenseType.NAMED.getName());

            boolean allocateVD = true, disablePardot = true;
            if(appConfig.get().getProspectsAppConfig().isEnableProspectsApp()) {
                disablePardot = false;
            }

			logger.info("addUser: calling adduser to add user:{} and allocate", userInfo);

			eventLogService.get().save(new EventLog(userInfo.getEmail(), EventLog.USER_ALLOCATE_VPHONE_VD_START));
            // known return codes known 
            // 584 - for email already exists in ldap (basically any ldapException)
            // 613 - Invalid User object (if fields are invalid or missing like if the licenseType or domainId is missing)
        	String url = "https://"+mgmtServerConfig.getHost()+":"+Integer.toString(mgmtServerConfig.getPort())+"/api/v1/vdusers/addNewUser?allocateVD="+allocateVD+"&"+"emailOTP="+true + "&parentDN=apiserver";
            HttpResponse<User> response2 = Unirest.post(url)
														.header("Content-Type", "application/json")
														.header("Accept", "application/json")
														.header("X-Agent-Type", "DCManager")
														.body(newUser)
														.asObject(User.class);

            logger.info("addUser: addToMgmt response status:{} and statustext:{} for user:{}", response2.getStatus(), response2.getStatusText(), userInfo);

            if(response2.getStatus() == HttpStatus.SC_OK) {
            	logger.info("addUser: added user:{} to mgmt and allocated VD:{}", userInfo);
            	//waitForVDToBeAllocated(mgmtServerConfig, response2.getBody().getId());
            	logger.info("addUser: device for user:{} is ready", userInfo);
                   
            	//logger.info(""+disablePardot);
            	if(!disablePardot) {
                    String vdUserId = response2.getBody().getId();
             	    url = "https://"+mgmtServerConfig.getHost()+":"+Integer.toString(mgmtServerConfig.getPort())+"/api/v1/vdusers/"+vdUserId+"/getotp";
             	    HttpResponse<JsonNode> otpResponse = Unirest.get(url)
                                                                .header("Content-Type", "application/json")
                                                                .header("Accept", "application/json")
                                                                .header("X-Agent-Type", "DCManager")
                                                                .asJson();
                    
                    if(otpResponse.getStatus() != HttpStatus.SC_OK) {
                        logger.error("getOtp: Error status code:"+otpResponse.getStatus()+" and response status text:"+otpResponse.getStatusText()+" for user:"+externalUserAccount.toString());
                        throw new WebApplicationException("getOtp: Error status code:"+otpResponse.getStatus()+" and response status text:"+otpResponse.getStatusText()+" for user:"+externalUserAccount.toString());
                    }
                    //get OTP
            	    String otpValue = otpResponse.getBody().getObject().getString("linkWeb");
                    //update OTP in Pardot
            	    prospectsAppService.get().updateFieldInPardot(userInfo.getEmail(), "OTP", otpValue, true);
            	    
            	}
            } else {
        		logger.error("addUser: Error status code:"+response2.getStatus()+" and response status text:"+response2.getStatusText()+" for user:"+externalUserAccount.toString());
        		throw new WebApplicationException("addUser: Error status code:"+response2.getStatus()+" and response status text:"+response2.getStatusText()+" for user:"+externalUserAccount.toString());
            }
		} catch(WebApplicationException e) {
		    	throw e;
		} catch(UnirestException e) {
			logger.error("addUser: Error while trying to add user", e);
			throw new WebApplicationException(e.getMessage(), e);
		} catch(Exception e) {
			logger.error("addUser: Error while trying to add user", e);
			throw new WebApplicationException(e.getMessage(), e);
		}

		return userInfo;
	}


	/**
	 * gets the nearest DC for the user for DCPinning
	 * 
	 * @param userInfo
	 */
	private MgmtServerConfig getNearestDCForUser(String userZipCode) {

		List<MgmtServerConfig> mgmtServerConfigs = appConfig.get().getMgmtServersConfig().getMgmtServerConfigs();
		// if there's only 1 datacenter, then return that one
		if(mgmtServerConfigs.size() == 1) {
			return mgmtServerConfigs.get(0);
		}

		MgmtServerConfig mgmtServerConfigToReturn = null;

		// else, calculate the nearest datacenter
		double minDistance = 0;

		for(MgmtServerConfig mgmtServerConfig:mgmtServerConfigs) {
			String dcZipCode = mgmtServerConfig.getZipCode();

			double userDCDistance = locationService.get().distance(userZipCode, dcZipCode);
			
			// if we just entered the loop then use the first DC as where we will allocate
			if(minDistance == 0) {
				mgmtServerConfigToReturn = mgmtServerConfig;
				minDistance = userDCDistance;
			} else {
				
				// if we found a datacenter closer than what we already have then use that one
				if(minDistance > userDCDistance) {
					mgmtServerConfigToReturn = mgmtServerConfig;
					minDistance = userDCDistance;
				}
			}
		}
		
		// now we have the DC that is closest to the user
		return mgmtServerConfigToReturn;
	}
	
	
	private MgmtServerConfig getMgmtServerConfigByName( String name ) {
	
		List<MgmtServerConfig> configs = appConfig.get().getMgmtServersConfig().getMgmtServerConfigs();
		if( configs != null ) {
			for( int i = 0; i < configs.size(); i++ ) {
				if( configs.get(i).getName().compareToIgnoreCase(name) == 0 ) {
					return configs.get(i);
				}
			}
		}
		
		return null;
	}

	/**
	 * 
	 * @param request
	 * @param userInfo
	 * @return
	 */
	@Override
	public String saveUser(UserInfo userInfo) {
		// first, find out the zip code of the user and see which data center he is close to
		// then, assign him to that datacenter
		// determine in which datacenter to place the user based on his userInfo
		MgmtServerConfig mgmtServerToUse = getNearestDCForUser(userInfo.getPersonalZipCode());
		if( mgmtServerToUse == null ) {
			logger.warn("saveUser: unable to find any DC for this user");
			throw new WebApplicationException("Unable to find any data center for this user", 511);
		}
		
		userInfo.setMgmtServerName( mgmtServerToUse.getName() );

		logger.info("saveUser: starting save of userInfo:{} to DB pinned to DC name:{}", userInfo, mgmtServerToUse.getName());

		// save the user info with nearest DC for pinning
		//UserInfo u = userInfoService.get().getByEmail( userInfo.getEmail() );
		String savedId = userInfoService.get().save(userInfo).getId().toString();

		// now check capacity at DC before throwing an exception if there is not enough capacity
		long currentNumUsersInDC = getNumUsersAtDC(mgmtServerToUse.getName());
		int capacityInDC = mgmtServerToUse.getCapacity();
		if(currentNumUsersInDC >= capacityInDC) {
			// using 429 for this since its the closest HTTP code to being out of capacity
			int HTTP_CODE_TOO_MANY_REQUESTS = 429;

			// update the user status
			logger.warn("saveUser: Out of Capacity in DC:{} or user:{}, max:{}", mgmtServerToUse.getName(), userInfo, capacityInDC);
			userInfoService.get().updateUserStatusField(savedId, UserInfo.OUT_OF_CAPACITY);
			throw new WebApplicationException("We are out of capacity at this time.  Please wait for an email", HTTP_CODE_TOO_MANY_REQUESTS);
		}

		logger.info("saveUser: saved userInfo:{} to DB with id:{}, current capacity:{}, max capacity:{}", userInfo, userInfo.getId(), (currentNumUsersInDC+1), capacityInDC);
		return savedId;
	}
	
	/**
	 * gets current capacity at DC
	 * 
	 * @param mgmtServerIndex
	 * @return
	 */
	@Override
	public long getNumUsersAtDC(String mgmtServerName) {
		return userInfoService.get().getNumUsersAtDC(mgmtServerName);
	}
	
	@Override
	public String getUserIDFromDC( MgmtServerConfig mgmtServerConfig, String email ) {

		//first get the user by using his email
		String jsonQuery = "[{\"fieldName\": \"externalUserAccount.attributes.mail\", \"operator\": \"=\",\"valueToCompare\": \"" + email + "\"}]";
		
    	String url = "https://"+mgmtServerConfig.getHost()+":"+Integer.toString(mgmtServerConfig.getPort())+"/api/v1/vdusers/query";
    	try {
    		HttpResponse<User[]> response2 = Unirest.post(url)
													.header("Content-Type", "application/json")
													.header("Accept", "application/json")
													.header("X-Agent-Type", "DCManager")
													.body(jsonQuery)
													.asObject( User[].class );	
    		
            logger.info("getUserIDFromDC: response status:{} and statustext:{} for user:{}", response2.getStatus(), response2.getStatusText(), email);

            if(response2.getStatus() == HttpStatus.SC_OK) {
		        
            	User[] userList = response2.getBody();
            	if( userList != null && userList.length > 0 ) {
            		return userList[0].getId();
            	}
            }
    	} catch( Exception e) {
            logger.error("getUserIDFromDC failed with exception: " + e.getMessage());
    	}
		

        return null;
	}
	
	
	@Override
	public void deleteUserFromDC(String serverConfigName, String email) {
		
		MgmtServerConfig mgmtServerConfig = this.getMgmtServerConfigByName(serverConfigName);
		if( mgmtServerConfig == null ) {
			logger.info( "Unable to find DC: " + serverConfigName);
			return;
		}				
		String sGUID =  getUserIDFromDC( mgmtServerConfig, email);
		
		if( !StringUtils.isBlank( sGUID ) ) {
	    	String url = "https://"+mgmtServerConfig.getHost()+":"+Integer.toString(mgmtServerConfig.getPort())+"/api/v1/vdusers/"+ sGUID + "?wait=true&removeFromLDAP=true" ;
	    	HttpResponse<String> deleteResponse = null;
			try {
				deleteResponse = Unirest.delete(url).header("Content-Type", "application/json")
													.header("Accept", "application/json")
													.header("X-Agent-Type", "DCManager").asString();
				
				logger.info("deleteUserFromDC: addToMgmt response status:{} and statustext:{} for user:{}", deleteResponse.getStatus(), email );
	
				if(deleteResponse.getStatus() == HttpStatus.SC_OK) {
	        	
				}
	
			} catch (UnirestException e) {
				
				logger.error("deleteUserFromDC failed with exception: " + e.getMessage() );
			}
		}

	}
}
