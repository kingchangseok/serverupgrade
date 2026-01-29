package app.ecams.resourcetype.model;

import app.core.proto.ProtoEcams.RsrcInfo;
import app.core.proto.ProtoEcams.JobInfo;

public class ResourceType {
	private String resourceType;
	private String resourceTypeName;
	private String resourceExeName;
	private String resourceCmInfo;
	
	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}


	public String getResourceTypeName() {
		return resourceTypeName;
	}


	public void setResourceTypeName(String resourceTypeName) {
		this.resourceTypeName = resourceTypeName;
	}
	
	public String getResourceExeName() {
		return resourceExeName;
	}

	public void setResourceExeName(String resourceExeName) {
		this.resourceExeName = resourceExeName;
	}

	public RsrcInfo toRsrcInfo(){
		RsrcInfo.Builder Rsrcinfo_builder = RsrcInfo.newBuilder();
		Rsrcinfo_builder.setRsrccd(this.resourceType);
		Rsrcinfo_builder.setRsrcmsg(this.resourceTypeName);
		Rsrcinfo_builder.setCminfo(this.resourceCmInfo);
		if(this.resourceExeName != null && !this.resourceExeName.equals("")){
			Rsrcinfo_builder.setExename(this.resourceExeName);
		}
		return Rsrcinfo_builder.build();		
	}

	public void setResourceCmInfo(String resourceCmInfo) {
		this.resourceCmInfo = resourceCmInfo;
	}

	public String getResourceCmInfo() {
		return resourceCmInfo;
	}
	
}
