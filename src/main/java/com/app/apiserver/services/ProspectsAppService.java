package com.app.apiserver.services;

import com.app.apiserver.core.ProspectsAppProspectListMembership;

public interface ProspectsAppService {

    
	int addUpdateUserToPardotList(String email, int listId);

	int getProspectId(String email, boolean bRetry);

	ProspectsAppProspectListMembership verifyUserInPardotList(String email, Integer prospectId, int listId, boolean bRetry);

	boolean updateFieldInPardot(String email, String fieldName, String fieldValue, boolean bRetry);
}
