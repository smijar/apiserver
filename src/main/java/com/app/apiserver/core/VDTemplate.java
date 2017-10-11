package com.app.apiserver.core;

import java.util.List;

import javax.annotation.Nullable;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Transient;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

/**
 * VD template
 * @author smijar
 *
 */
public class VDTemplate extends AppBaseEntity {
	
	@Transient
	public static final String TEMPLATE_TYPE_PUBLISHED = "PublishedApp";
	
	@Transient
	public static final String TEMPLATE_TYPE_DEFAULT = "FullVM";
	
	@NotNull	
	@Size(min = 1, max = 100)
	@Pattern(regexp = ALLOWED_CHARS)
	@ApiModelProperty(value = "Name of a ACE Device Template", required=true)
	private String name;
	
	@Size(max = 300)
	@Pattern(regexp = ALLOWED_CHARS)
	@ApiModelProperty(value = "Description of a ACE Device Template", required=false)
    private String description;
    
	@ApiModelProperty(value = "FlavorId of a ACE Device Template", hidden=true)
    private String flavorId;
    
	@NotNull	
	@Pattern(regexp = ALLOWED_CHARS)
	@ApiModelProperty(value = "Openstack Flavor for the ACE Device Template", required=true)
    private String flavorName;
    
	@ApiModelProperty(value = "ImageId of a ACE Device Template", hidden=true)
    private String imageId;
    
	@NotNull	
	@Pattern(regexp = ALLOWED_CHARS)
	@ApiModelProperty(value = "Android Image Name for the ACE Device Template", required=true)
    private String imageName;

	@NotNull	
	@Pattern(regexp = ALLOWED_CHARS)
	@ApiModelProperty(value = "User Data Image Name for the ACE Device Template", required=true)
    private String imageUserDataName;
	
	@ApiModelProperty(value = "imageUserDataId of a ACE Device Template", hidden=true)
    private String imageUserDataId;
    
	@ApiModelProperty(value = "snapshotId of a ACE Device Template", hidden=true)
    private String snapshotId;
    
	@ApiModelProperty(value = "snapshotName of a ACE Device Template", hidden=true)
    private String snapshotName;
    
	//@NotNull	
    //private String qemu;
		
	//@NotNull	
    //private String cert;
	@ApiModelProperty(value = "Enable ADB Debugging on the ACE Devices spawned from this template", required=true)
    private boolean enableDebugOnVd = false;
    
	@ApiModelProperty(value = "Enable Logcat Debugging on the ACE Devices spawned from this template", required=true)
    private boolean enableLogcatOnVd = false;
	
	@ApiModelProperty(value = "Make this template the default for any new users added", required=true)
    private boolean defaultConfig;
    
	@ApiModelProperty(value = "Enable or Disable sideloading of apps", required=true)
	private boolean enableSideloadApps = true;
	
	@NotNull
	@ApiModelProperty(value = "The ID of the cloud config to which this template belongs", required=true)
    private String cloudConfigId;

	@Min(1)
	@Max(16)
	@ApiModelProperty(value = "Size of user data in GB", required=true, allowableValues="{@code range[1, 16]}")
    private int userDataSizeGB;

	@Nullable
	@Transient
	@ApiModelProperty(value = "Number of users using an ACE Device Template", hidden=true)
	private long vdTemplateUsersCount = -1;  // count of how many users are using this template

	@Nullable
	@ApiModelProperty(value = "enableGpuAcceleration on this ACE Device Template", hidden=true)
	private boolean enableGpuAcceleration = false;
	
	@Nullable
	@ApiModelProperty(value = "Is template usable", hidden=true)
	private boolean usable = true; //is template currently usable
	
	@Nullable
	@ApiModelProperty(value = "If tempalte is not usable, the fault associated with the template", hidden=true)
	private String faultString = null;
	
	
	@Nullable
	private String virtualDeviceType = TEMPLATE_TYPE_DEFAULT;
	
	@Nullable
	@Transient
	private String overlayIconFileHash = null;
	
	@Nullable
	private String overlayIconFileName = null;
	
	// the version of the method used to inject apks in imageName
	// this is saved here so that we do not need to lookup the value by making calls to openstack
	// everytime we need to find this version.
	private int launchIDsVersion = 0;  

	
	List<String> publishedApps = Lists.newArrayList();  // list of launchIDs which are published

	public VDTemplate() {}

	public VDTemplate(String s) {}

    public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getFlavorId() {
		return flavorId;
	}
	public void setFlavorId(String flavorId) {
		this.flavorId = flavorId;
	}
	public String getImageId() {
		return imageId;
	}
	public void setImageId(String imageId) {
		this.imageId = imageId;
	}
	public String getSnapshotId() {
		return snapshotId;
	}
	public void setSnapshotId(String snapshotId) {
		this.snapshotId = snapshotId;
	}
/*	public String getQemu() {
		return qemu;
	}
	public void setQemu(String qemu) {
		this.qemu = qemu;
	}
	public String getCert() {
		return cert;
	}
	public void setCert(String cert) {
		this.cert = cert;
	}*/
	public String getFlavorName() {
		return flavorName;
	}
	public void setFlavorName(String flavorName) {
		this.flavorName = flavorName;
	}
	public String getImageName() {
		return imageName;
	}
	public void setImageName(String imageName) {
		this.imageName = imageName;
	}
	public String getSnapshotName() {
		return snapshotName;
	}
	public void setSnapshotName(String snapshotName) {
		this.snapshotName = snapshotName;
	}
	public boolean isDefaultConfig() {
		return defaultConfig;
	}
	public void setDefaultConfig(boolean defaultConfig) {
		this.defaultConfig = defaultConfig;
	}
	public String getImageUserDataName() {
		return imageUserDataName;
	}
	public void setImageUserDataName(String imageUserDataName) {
		this.imageUserDataName = imageUserDataName;
	}
	public String getImageUserDataId() {
		return imageUserDataId;
	}
	public void setImageUserDataId(String imageUserDataId) {
		this.imageUserDataId = imageUserDataId;
	}
	public String getCloudConfigId() {
		return cloudConfigId;
	}
	public void setCloudConfigId(String cloudConfigId) {
		this.cloudConfigId = cloudConfigId;
	}
	public int getUserDataSizeGB() {
		return this.userDataSizeGB;
	}
	public void setUserDataSizeGB(int userDataSizeGB) {
		this.userDataSizeGB = userDataSizeGB;
	}
	public long getVdTemplateUsersCount() {
		return vdTemplateUsersCount;
	}
	public void setVdTemplateUsersCount(long vdTemplateUsersCount) {
		this.vdTemplateUsersCount = vdTemplateUsersCount;
	}
	public boolean isEnableGpuAcceleration() {
		return enableGpuAcceleration;
	}
	public void setEnableGpuAcceleration(boolean enableGpuAcceleration) {
		this.enableGpuAcceleration = enableGpuAcceleration;
	}
	//public boolean isCanUseGpuAcceleration() {
	//	return canUseGpuAcceleration;
	//}
	//public void setCanUseGpuAcceleration(boolean canUseGpuAcceleration) {
	//	this.canUseGpuAcceleration = canUseGpuAcceleration;
	//}
	public boolean isUsable() {
		return usable;
	}
	public void setUsable(boolean usable) {
		this.usable = usable;
		if(usable)
			setFaultString(null);
	}
	public String getFaultString() {
		return faultString;
	}
	public void setFaultString(String faultString) {
		this.faultString = faultString;
	}
	public boolean isEnableDebugOnVd() {
		return enableDebugOnVd;
	}
	public void setEnableDebugOnVd(boolean enableDebugOnVd) {
		this.enableDebugOnVd = enableDebugOnVd;
	}
	public boolean isEnableLogcatOnVd() {
		return enableLogcatOnVd;
	}
	public void setEnableLogcatOnVd(boolean enableLogcatOnVd) {
		this.enableLogcatOnVd = enableLogcatOnVd;
	}
	public boolean isEnableSideloadApps() {
		return enableSideloadApps;
	}
	public void setEnableSideloadApps(boolean enableSideloadApps) {
		this.enableSideloadApps = enableSideloadApps;
	}
	public String getVirtualDeviceType() {
		return virtualDeviceType;
	}
	public void setVirtualDeviceType(String virtualDeviceType) {
		this.virtualDeviceType = virtualDeviceType;
	}
	public String getOverlayIconFileHash() {
		return overlayIconFileHash;
	}
	public void setOverlayIconFileHash(String overlayIconFileHash) {
		this.overlayIconFileHash = overlayIconFileHash;
	}
	public String getOverlayIconFileName() {
		return overlayIconFileName;
	}
	public void setOverlayIconFileName(String overlayIconFileName) {
		this.overlayIconFileName = overlayIconFileName;
	}
	public List<String> getPublishedApps() {
		return publishedApps;
	}
	public void setPublishedApps(List<String> publishedApps) {
		this.publishedApps = publishedApps;
	}

	public int getLaunchIDsVersion() {
		return launchIDsVersion;
	}

	public void setLaunchIDsVersion(int launchIDsVersion) {
		this.launchIDsVersion = launchIDsVersion;
	}

	public String toString() {
		return MoreObjects.toStringHelper(this.getClass())
				.add("id", getId())
				.add("cloudConfigId", cloudConfigId)
				.add("isDefault", isDefaultConfig())
				.add("virtualDeviceType", virtualDeviceType)
				.add("name", name)
				.add("description", description)
				.add("flavorName", flavorName)
				.add("imageName", imageName)
				.add("enableDebugOn", enableDebugOnVd)
				.add("enableLogcatOn", enableLogcatOnVd)
				.add("enableSideloadApps", enableSideloadApps)
				.add("imageUserDataName", imageUserDataName)
				.add("imageName", imageName)
				.toString();
	}
}
