package com.app.apiserver.services;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.List;

import javax.ws.rs.WebApplicationException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.bson.types.ObjectId;
import org.eclipse.jetty.http.HttpStatus;
import org.mongodb.morphia.dao.BasicDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.app.apiserver.core.AppConfiguration;
import com.app.apiserver.core.ZipLatLong;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.mongodb.WriteConcern;

/**
 * 
 * This implements the location service that loads the CSV zipcodes/LatLong into the DB
 * and can compute the distance between 2 zipcodes
 * 
 * @author smijar
 */
@Singleton
class LocationServiceImpl extends BasicDAO<ZipLatLong, ObjectId> implements LocationService
{
	private final Logger logger  = LoggerFactory.getLogger(LocationServiceImpl.class);

	//private Provider<AppMongoService> mongoService;
	private Provider<AppConfiguration> appConfig;

	@Inject
	public LocationServiceImpl(Provider<AppMongoService> mongoService, Provider<AppConfiguration> appConfig) {
		super(mongoService.get().getMongo(), mongoService.get().getMorphia(), mongoService.get().getDBName());
		this.appConfig = appConfig;
		//this.mongoService = mongoService;		
	}

	/**
	 * calculates distance between 2 lat/long pairs in a unit specified:
	 * 
	 * Default is in miles
	 * K for Kilometers,
	 * N for Nautical Miles
	 */
	@Override
	public double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
		double theta = lon1 - lon2;
		double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515;
		if (unit == "K") {
			dist = dist * 1.609344;
		} else if (unit == "N") {
			dist = dist * 0.8684;
		}

		return (dist);
	}

	/**
	 * calculates distance between 2 lat/long pairs in a unit specified:
	 * 
	 * Default is in miles
	 * K for Kilometers,
	 * N for Nautical Miles
	 */
	@Override
	public double distance(String zipCode1, String zipCode2) {
		if(zipCode1.equalsIgnoreCase(zipCode2))
			return 0;

		ZipLatLong loc1 = getLatLongForZipCode(zipCode1);
		if(loc1 == null)
			throw new WebApplicationException("Invalid Zip code:"+zipCode1+", could not be found", HttpStatus.UNPROCESSABLE_ENTITY_422);

		ZipLatLong loc2 = getLatLongForZipCode(zipCode2);
		if(loc2 == null)
			throw new WebApplicationException("Invalid Zip code:"+zipCode2+", could not be found", HttpStatus.UNPROCESSABLE_ENTITY_422);

		return distance(loc1.getLoc().getX(), loc1.getLoc().getY(), loc2.getLoc().getX(), loc2.getLoc().getY(), "M");
	}

	private double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}

	private double rad2deg(double rad) {
		return (rad * 180 / Math.PI);
	}

	/**
	 * loads the zip code lat/long pairs from a file specified in configuration
	 * @param zipLatLongFile
	 * @return
	 */
	private List<ZipLatLong> loadCSVZipCodesFile(String zipLatLongFile) {
		Reader in = null;
		Iterable<CSVRecord> records = null;
		List<ZipLatLong> zips = Lists.newArrayList();

		try {
			logger.info("loadCSVZipCodesFile: looking for zip codes file");
			File f = new File(zipLatLongFile);
			if(!f.exists())
				throw new RuntimeException("zip code latitude and longitude records file not found");

			logger.info("loadCSVZipCodesFile: found zip codes file");
			in = new FileReader("conf/us_zip_lat_long.csv");
			//Iterable<CSVRecord> records = CSVFormat.EXCEL.parse(in);
			records = CSVFormat.RFC4180.withHeader("ZIP", "LAT", "LNG").parse(in);


			logger.info("loadCSVZipCodesFile: loading zip codes from file");
			int ctr = 0;
			for(CSVRecord record:records) {
				// skip the heading
				if(ctr == 0) {
					ctr++;
					continue;
				}
				String zipCode = record.get("ZIP");
				double latitude = new Double(record.get("LAT"));
				double longitude = new Double(record.get("LNG"));
				zips.add(new ZipLatLong(zipCode, latitude, longitude));
				ctr++;
			}
			logger.info("finished loading:{} zip codes from file", ctr);
		} catch(Exception e) {
			throw new RuntimeException(e);
		} finally {
			try { in.close(); } catch(Exception e) {}
		}
		
		return zips;
	}
	
	/**
	 * returns the location data for zip code
	 * @param zipCode
	 */
	@Override
	public ZipLatLong getLatLongForZipCode(String zipCode) {
		ZipLatLong zipLatLong = super.findOne(super.createQuery().field("zipCode").equal(zipCode));
		return zipLatLong;
	}

	/**
	 * loads the zipcodes from file into DB
	 */
	@Override
	public void loadZipCodesIntoDB(boolean skipIfExists) {
		
		ZipLatLong zip = getLatLongForZipCode("78665");
		if( zip != null && skipIfExists == true ) {
			logger.info("loadZipCodesIntoDB: Table exists. Skipping loading of zipcodes in DB.");
			return;
		}
		List<ZipLatLong> zipCodes = loadCSVZipCodesFile(appConfig.get().getGeneralConfig().getZipLatLongFile());
		logger.info("loadZipCodesIntoDB: removing old zip codes");
		super.deleteByQuery(super.createQuery());
		WriteConcern wc = new WriteConcern();
		logger.info("loadZipCodesIntoDB: saving {} zip codes from file into DB", zipCodes.size());
		super.getDatastore().save(zipCodes, wc);
		logger.info("loadZipCodesIntoDB: saved {} zip codes from file into DB", zipCodes.size());
	}


//	public static void main (String[] args) throws java.lang.Exception
//	{
//		System.out.println(distance(32.9697, -96.80322, 29.46786, -98.53506, "M") + " Miles\n");
//		System.out.println(distance(32.9697, -96.80322, 29.46786, -98.53506, "K") + " Kilometers\n");
//		System.out.println(distance(32.9697, -96.80322, 29.46786, -98.53506, "N") + " Nautical Miles\n");
//
//		System.out.println("reading CSV file for zipcode, lat, long");
//		Iterable<CSVRecord> records = readCSVZipCodesFile("conf/us_zip_lat_long.csv");
//
//		int ctr = 0;
//		for(CSVRecord record: records) {
//			if(ctr == 0)
//				continue;
//			String zip = record.get("ZIP");
//			String lat = record.get("LAT");
//			String lng = record.get("LNG");
//
//			System.out.println(zip+","+lat+","+lng);
//		}
//	}

	
}
