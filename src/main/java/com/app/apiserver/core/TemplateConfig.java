package com.app.apiserver.core;

import java.util.Map;

import com.google.common.base.MoreObjects;

/**
 * a TemplateConfig that is used to load a template based on its key
 *  
 * @author smijar
 */
public class TemplateConfig {
	private Map<String, String> templates;

	public Map<String, String> getTemplates() {
		return templates;
	}

	public void setTemplates(Map<String, String> templates) {
		this.templates = templates;
	}

	public String toString() {
		return MoreObjects.toStringHelper(TemplateConfig.class)
				.add("templates", templates)
				.toString();
	}
}
