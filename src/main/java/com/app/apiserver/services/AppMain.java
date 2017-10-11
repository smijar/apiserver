package com.app.apiserver.services;

import org.eclipse.jetty.server.session.SessionHandler;
import org.secnod.example.webapp.UserFactory;
import org.secnod.shiro.jaxrs.ShiroExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.app.apiserver.core.AppConfiguration;
import com.app.apiserver.messaging.AppScheduledServiceManager;
import com.app.apiserver.resources.AdminApiResource;
import com.app.apiserver.resources.MgmtApiResource;
import com.app.apiserver.resources.RootResource;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.inject.Guice;
import com.google.inject.Injector;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

/**
 * Main of the App
 */
public class AppMain extends Application<AppConfiguration> {
	private static Logger logger = LoggerFactory.getLogger(AppMain.class);
	public static Injector injector;

    private final AppShiroBundle<AppConfiguration> shiro = new AppShiroBundle<AppConfiguration>() {

        @Override
        protected AppShiroConfiguration narrow(AppConfiguration configuration) {
            return configuration.shiro;
        }
    };

    public AppMain() {}

    @Override
    public void initialize(Bootstrap<AppConfiguration> bootstrap) {
        //bootstrap.addBundle(shiro); 

		bootstrap.getObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		bootstrap.getObjectMapper().configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
		bootstrap.getObjectMapper().configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
		bootstrap.getObjectMapper().setSerializationInclusion(Include.NON_NULL);

		//bootstrap.addBundle(new AssetsBundle("/assets", "/"));
    }

    public void initServices(AppConfiguration configuration) {
    	logger.info("registering the guice module services");
    	AppModule module = new AppModule(configuration);
    	AppMain.injector = Guice.createInjector(module);
    }

    public void initManagedObjects(Environment environment) {
        environment.lifecycle().manage(injector.getInstance(AppMongoManaged.class));
        environment.lifecycle().manage(injector.getInstance(AppScheduledServiceManager.class));
    }

    @Override
    public void run(AppConfiguration appConfig, Environment environment) throws Exception {
    	logger.info("starting with configuration:{}", appConfig);
        environment.jersey().register(new UserFactory());
        environment.jersey().register(new ShiroExceptionMapper());

        environment.getApplicationContext().setSessionHandler(new SessionHandler());

        initServices(appConfig);

        initManagedObjects(environment);
        
        HttpClientSetup.initialize(appConfig);

//        for (Object resource : IntegrationTestApplication.createAllIntegrationTestResources()) {
//            environment.jersey().register(resource);
//        }

        environment.jersey().register(new RootResource());
        environment.jersey().register(injector.getInstance(MgmtApiResource.class));
        environment.jersey().register(injector.getInstance(AdminApiResource.class));

        //environment.jersey().setUrlPattern("/api/*");
    }

    @Override
    public String getName() {
        return "App";
    }

    public static void main(String[] args) throws Exception {
        new AppMain().run(args.length > 0 ? args : new String[] { "server", "conf/app.yml"});
    }
}