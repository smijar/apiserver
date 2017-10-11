package com.app.apiserver.services;

import org.bson.types.ObjectId;
import org.mongodb.morphia.dao.DAO;

import com.app.apiserver.core.ZipLatLong;

public interface LocationService extends DAO<ZipLatLong, ObjectId> {
	public double distance(double lat1, double lon1, double lat2, double lon2, String unit);

	void loadZipCodesIntoDB(boolean skipIfExists);

	ZipLatLong getLatLongForZipCode(String zipCode);

	double distance(String zipCode, String zipCode2);
}