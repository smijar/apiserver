package com.app.apiserver.services;

import com.app.apiserver.core.MgmtServerConfig;
import com.app.apiserver.core.UserInfo;

public interface MgmtApiService {

	String getServerId(String mgmtServerName);

	//String getPrimaryDomainId(int mgmtServerIndex);

	//String getPrimaryLdapConfigId(int mgmtServerIndex);

	//String getPrimaryCloudConfigId(int mgmtServerIndex);

	//ApkFile[] getApkListFromRepo(int mgmtServerIndex, String cloudConfigId);

	//String getPrimaryAggregate(int mgmtServerIndex);

	//User[] getUserList(int mgmtServerIndex, String cloudConfigId);

	//boolean doesUserExist(int mgmtServerIndex, String cloudConfigId, UserInfo userInfo);

	//Image[] getImageListForCloudConfig(int mgmtServerIndex, String cloudConfigId);

	//ExternalUserAccount getExternalUserAccountForUser(int mgmtServerIndex, UserInfo userInfo) throws NoSuchAlgorithmException;

	//String createTemplateForUser(int mgmtServerIndex, String cloudConfigId, CreateTemplateRequest createTemplateRequest);

	//VDTemplate[] getTemplateListForCloudConfig(int mgmtServerIndex, String cloudConfigId);

	//void createImageForUser(int mgmtServerIndex, String cloudConfigId, String imageHash, CreateTemplateRequest createTemplateRequest);

	UserInfo addNewUserToSystemAndAllocate(UserInfo userInfo);

	String saveUser(UserInfo userInfo);

	public long getNumUsersAtDC(String mgmtServerName);
	
	public String getUserIDFromDC( MgmtServerConfig mgmtServerConfig, String email );
	
	public void deleteUserFromDC(String serverConfigName, String email);
}
