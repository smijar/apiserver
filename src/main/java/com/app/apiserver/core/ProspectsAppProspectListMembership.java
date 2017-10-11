package com.app.apiserver.core;

import jersey.repackaged.com.google.common.base.MoreObjects;

/**
 * structure to hold all the parts of the Pardot List membership
 * @author smijar
 *
 */
public class ProspectsAppProspectListMembership {
	private int listId;
	private int listMembershipId;
	private int prospectId;
	
	public ProspectsAppProspectListMembership(int listId, int prospectId, int listMembershipId) {
		this.listId = listId;
		this.prospectId = prospectId;
		this.listMembershipId = listMembershipId;
	}

	public int getListId() {
		return listId;
	}
	public void setListId(int listId) {
		this.listId = listId;
	}
	public int getListMembershipId() {
		return listMembershipId;
	}
	public void setListMembershipId(int listMembershipId) {
		this.listMembershipId = listMembershipId;
	}
	public int getProspectId() {
		return prospectId;
	}
	public void setProspectId(int prospectId) {
		this.prospectId = prospectId;
	}

	public String toString() {
		return MoreObjects.toStringHelper(this.getClass())
							.add("listId", listId)
							.add("listMembershipId", listMembershipId)
							.add("prospectId", prospectId)
							.toString();
	}
}