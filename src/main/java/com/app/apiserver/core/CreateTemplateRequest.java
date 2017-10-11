package com.app.apiserver.core;

import java.util.List;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import com.google.common.base.MoreObjects;

import jersey.repackaged.com.google.common.collect.Lists;


/**
 * Creates an image and a template containing the following apps, also contains requestedApps and answers to questions and answers
 * 
 * @author smijar
 */
public class CreateTemplateRequest {
	private transient String cloudConfigId = ""; // NOT persisted - used only for creating template request to mgmt server
	private transient String name = ""; // NOT persisted -used only for creating template request to mgmt server

	@Nullable
	private List<String> apkList = Lists.newArrayList();
	@Nullable
	private List<String> appNamesList = Lists.newArrayList();
	@Nullable
	private List<String> requestedAppsList = Lists.newArrayList();
	@Nullable
	private List<String> questionsAndAnswers = Lists.newArrayList();

	public String getCloudConfigId() {
		return cloudConfigId;
	}

	public void setCloudConfigId(String cloudConfigId) {
		this.cloudConfigId = cloudConfigId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getApkList() {
		return apkList;
	}

	public void setApkList(List<String> apkNames) {
		this.apkList = apkNames;
	}

	public List<String> getAppNamesList() {
		return appNamesList;
	}

	public void setAppNamesList(List<String> appNamesList) {
		this.appNamesList = appNamesList;
	}

	public List<String> getRequestedAppsList() {
		return requestedAppsList;
	}

	public void setRequestedAppsList(List<String> requestedApps) {
		this.requestedAppsList = requestedApps;
	}

	public List<String> getQuestionsAndAnswers() {
		return questionsAndAnswers;
	}

	public void setQuestionsAndAnswers(List<String> questionsAndAnswers) {
		this.questionsAndAnswers = questionsAndAnswers;
	}

	public String toString() {
		return MoreObjects.toStringHelper(this.getClass())
				.add("apkList.length", apkList.size())
				.add("appNamesList.length", appNamesList.size())
				.add("requestedApps", requestedAppsList)
				.add("questionsAndAnswers", questionsAndAnswers)
				.add(name, name)
				.toString();
	}
}
