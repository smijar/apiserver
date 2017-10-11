package com.app.apiserver.services;

import java.net.UnknownHostException;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.app.apiserver.core.AppMongoConfig;
import com.app.apiserver.core.UserInfo;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;

@Singleton
public class AppMongoServiceImpl implements AppMongoService {
	private Logger logger = LoggerFactory.getLogger(AppMongoServiceImpl.class);
	private AppMongoConfig mongoConfig;
	private Mongo mongo;
	private Morphia morphia;
	private Datastore ds;

	@Inject
	public AppMongoServiceImpl(AppMongoConfig mongoConfig) {
		this.mongoConfig = mongoConfig;
	}

	@Override
	public void connect() throws UnknownHostException {
		logger.info("Connecting to mongo:{}  and port:{}", mongoConfig.getHost(), mongoConfig.getPort());
		MongoClientOptions options = MongoClientOptions.builder().connectionsPerHost(500).build();

    	//this.mongo = new MongoClient(mongoConfig.getHost(), mongoConfig.getPort());
    	this.mongo = new MongoClient(mongoConfig.getHost(), options);
    	this.morphia = new Morphia();

    	// using .map() instead of mappackage()
    	morphia.map(UserInfo.class);

    	this.ds = morphia.createDatastore(this.mongo, mongoConfig.getDbName());

    	// sets up the indexes and capped collections
    	ds.ensureIndexes(); // creates all defined with @Indexed
    	ds.ensureCaps(); //creates all collections for @Entity(cap=@CappedAt(...))

    	// if authentication is needed, add the following:
    	// db.authenticate(configuration.mongouser, configuration.mongopwd.toCharArray());
    	// check connectivity to Mongo immediately
        mongo.getVersion(); // checking if mongo is connected
		logger.info("Finished connecting to mongo:"+mongoConfig.getHost()+" and port:"+mongoConfig.getPort());
	}

	@Override
	public void close() {
		this.mongo.close();
	}

    @Override
	public AppMongoConfig getMongoConfig() {
		return mongoConfig;
	}

	@Override
	public void setMongoConfig(AppMongoConfig mongoConfig) {
		this.mongoConfig = mongoConfig;
	}

	@Override
	public Mongo getMongo() {
		return mongo;
	}

	@Override
	public void setMongo(Mongo mongo) {
		this.mongo = mongo;
	}

	@Override
	public Morphia getMorphia() {
		return morphia;
	}

	@Override
	public void setMorphia(Morphia morphia) {
		this.morphia = morphia;
	}

	@Override
	public Datastore getDS() {
		return this.ds;
	}

	@Override
	public String getDBName() {
		return this.mongoConfig.getDbName();
	}
}
