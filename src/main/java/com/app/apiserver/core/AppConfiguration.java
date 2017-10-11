package com.app.apiserver.core;

import com.app.apiserver.services.AppShiroConfiguration;
import com.google.common.base.MoreObjects;

import io.dropwizard.Configuration;

/**
 * App's Dropwizard configuration.
 */
public class AppConfiguration extends Configuration {

    public AppShiroConfiguration shiro;
    private AppMongoConfig mongoConfig;
    private MgmtServersConfig mgmtServersConfig;
    private GeneralConfig generalConfig;
    private ProspectsAppConfig prospectsAppConfig;
    private TemplateConfig templateConfig;

	public AppShiroConfiguration getShiro() {
		return shiro;
	}

	public void setShiro(AppShiroConfiguration shiro) {
		this.shiro = shiro;
	}

	public AppMongoConfig getMongoConfig() {
		return mongoConfig;
	}

	public void setMongoConfig(AppMongoConfig mongoConfig) {
		this.mongoConfig = mongoConfig;
	}
	
	
	public MgmtServersConfig getMgmtServersConfig() {
		return mgmtServersConfig;
	}

	public void setMgmtServerConfigs(MgmtServersConfig mgmtServersConfig) {
		this.mgmtServersConfig = mgmtServersConfig;
	}

	public GeneralConfig getGeneralConfig() {
		return generalConfig;
	}

	public void setGeneralConfig(GeneralConfig generalConfig) {
		this.generalConfig = generalConfig;
	}

	public ProspectsAppConfig getProspectsAppConfig() {
		return prospectsAppConfig;
	}

	public void setProspectsAppConfig(ProspectsAppConfig prospectsAppConfig) {
		this.prospectsAppConfig = prospectsAppConfig;
	}

	public TemplateConfig getTemplateConfig() {
		return templateConfig;
	}

	public void setTemplateConfig(TemplateConfig templateConfig) {
		this.templateConfig = templateConfig;
	}

	public String toString() {
		return MoreObjects.toStringHelper(this.getClass())
							.add("ShiroConfiguration", shiro)
							.add("MongoConfig", mongoConfig)
							.add("MgmtServersConfig", mgmtServersConfig)
							.add("GeneralConfig", generalConfig)
							.add("prospectsAppConfig", prospectsAppConfig)
							.add("templateConfig", templateConfig)
							.toString();
	}
}