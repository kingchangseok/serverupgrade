package app.ecams.system.model;

import app.core.proto.ProtoEcams.SysInfo;

public class SystemInfo {
	private String cmSyscd;
	private String cmSysmsg;
	private String cmSysinfo;
	private String cmPrjname;
	
	public void setCmSyscd(String cmSyscd) {
		this.cmSyscd = cmSyscd;
	}
	public String getCmSyscd() {
		return cmSyscd;
	}
	public void setCmSysmsg(String cmSysmsg) {
		this.cmSysmsg = cmSysmsg;
	}
	public String getCmSysmsg() {
		return cmSysmsg;
	}
	public void setCmSysinfo(String cmSysinfo) {
		this.cmSysinfo = cmSysinfo;
	}
	public String getCmSysinfo() {
		return cmSysinfo;
	}
	public String getCmPrjname() {
		return cmPrjname;
	}
	public void setCmPrjname(String cmPrjname) {
		this.cmPrjname = cmPrjname;
	}
	
	public SysInfo toSysInfo(){
		SysInfo.Builder  sysinfo_builder = SysInfo.newBuilder();
		sysinfo_builder.setSyscd(this.cmSyscd);
		sysinfo_builder.setSysmsg(this.cmSysmsg);
		sysinfo_builder.setSysinfo(this.cmSysinfo);
		sysinfo_builder.setPrjname(this.cmPrjname);
		return sysinfo_builder.build();		
	}
	
}
