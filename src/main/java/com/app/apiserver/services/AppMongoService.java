package com.app.apiserver.services;

import java.net.UnknownHostException;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import com.app.apiserver.core.AppMongoConfig;
import com.mongodb.Mongo;

public interface AppMongoService  {
	public void connect() throws UnknownHostException;
	public void close();
    public AppMongoConfig getMongoConfig();
	public void setMongoConfig(AppMongoConfig mongoConfig);
	public Mongo getMongo();
	public void setMongo(Mongo mongo);
	public Morphia getMorphia();
	public void setMorphia(Morphia morphia);
	public Datastore getDS();
	public String getDBName();
}
