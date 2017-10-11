package com.app.apiserver.core;

import jersey.repackaged.com.google.common.base.MoreObjects;

/**
 * contains a zip code's latitude and longitude
 * 
 * @author smijar
 *
 */
public class ZipLatLong extends AppBaseEntity {
	private String zipCode;
	private Loc loc;
	
	public ZipLatLong() {
		
	}
	
	public ZipLatLong(String zipCode, double latitude, double longitude) {
		this.zipCode = zipCode;
		this.loc = new Loc(latitude, longitude);
	}

	public String getZipCode() {
		return zipCode;
	}
	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}
	public double getLatitude() {
		return loc.getX();
	}
	public double getLongitude() {
		return loc.getY();
	}
	public Loc getLoc() {
		return loc;
	}
	public String toString() {
		return MoreObjects.toStringHelper(this.getClass())
				.add("zipCode", zipCode)
				.add("loc", loc)
				.toString();
	}
}
