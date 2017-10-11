package com.app.apiserver.core;


import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.MoreObjects;

/**
 * Image for VMs
 */
public class Image {
	
	public static final String SYSTEM_IMAGE_SUFFIX = "_android";
	public static final String KERNEL_IMAGE_SUFFIX = "_kernel";
	public static final String USERDATA_IMAGE_SUFFIX = "_userdata";
	public static final String INITRD_IMAGE_SUFFIX = "_initrd";
	
    private String name;
    private String id;
    private String status;
    private long imageSizeBytes;
    private String metadata;
    private String kernelId;
    private String kernelCmdline;
    private String ramDiskId;
    private List<String> apps;
    private String sourceImage;
    private String createdByID;
    private String createdByName;
    private boolean fipsEnabled = false;
    
    private int launchIDsVersion;// the version of the method used to inject apks in imageName
    
    
    public Image() {

    }

    public List<String> getApps() {
		return apps;
	}

	public void setApps(List<String> apps) {
		this.apps = apps;
	}

	public Image(String id, String name, String status, long imageSizeBytes, String kernelId, String kernelCmdline, String ramDiskId, String sourceImg, String launchIDsVersion, String sFIPSEnabled, String createdByID, List<String> apps) {
        this.name = name;
        this.id = id;
        this.status = status;
        this.imageSizeBytes = imageSizeBytes;
        this.kernelId = kernelId;
        this.kernelCmdline = kernelCmdline;
        this.ramDiskId = ramDiskId;
        this.apps = apps;
        this.sourceImage = sourceImg;
        this.createdByID = createdByID;
        
        if( !StringUtils.isBlank(launchIDsVersion)) {
        	try {
        		this.launchIDsVersion = Integer.parseInt(launchIDsVersion);        		
        	} catch( Exception e ) {
        		
        	}
        }
        
        if( !StringUtils.isBlank(sFIPSEnabled) && sFIPSEnabled.compareToIgnoreCase("true") == 0 ) {
        	fipsEnabled = true;
        }
        
    }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public long getImageSizeBytes() {
		return imageSizeBytes;
	}

	public void setImageSizeBytes(long imageSizeBytes) {
		this.imageSizeBytes = imageSizeBytes;
	}

	public String getMetadata() {
		return metadata;
	}

	public void setMetadata(String metadata) {
		this.metadata = metadata;
	}

	public String getKernelId() {
		return kernelId;
	}

	public void setKernelId(String kernelId) {
		this.kernelId = kernelId;
	}

	public String getKernelCmdline() {
		return kernelCmdline;
	}

	public void setKernelCmdline(String kernelCmdline) {
		this.kernelCmdline = kernelCmdline;
	}

	public String getRamDiskId() {
		return ramDiskId;
	}

	public void setRamDiskId(String ramDiskId) {
		this.ramDiskId = ramDiskId;
	}

	public String getSourceImage() {
		return sourceImage;
	}

	public void setSourceImage(String sourceImage) {
		this.sourceImage = sourceImage;
	}

	public int getLaunchIDsVersion() {
		return launchIDsVersion;
	}

	public void setLaunchIDsVersion(int launchIDsVersion) {
		this.launchIDsVersion = launchIDsVersion;
	}

	public boolean isFipsEnabled() {
		return fipsEnabled;
	}

	public void setFipsEnabled(boolean fipsEnabled) {
		this.fipsEnabled = fipsEnabled;
	}

	public String getCreatedByID() {
		return createdByID;
	}

	public void setCreatedByID(String createdByID) {
		this.createdByID = createdByID;
	}

	public String getCreatedByName() {
		return createdByName;
	}

	public void setCreatedByName(String createdByName) {
		this.createdByName = createdByName;
	}

	public String toString() {
		return MoreObjects.toStringHelper(this.getClass())
		.add("name", name)
		.add("id", getId())
		.add("status", status)
		.add("apps.length", apps.size())
		.toString();
	}
}