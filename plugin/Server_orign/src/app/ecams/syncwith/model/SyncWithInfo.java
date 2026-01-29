package app.ecams.syncwith.model;


public class SyncWithInfo {
	
	private String syscd;
	private String rsrcname;
	private String dirpath;
	private String md5sum;
	private String status;
	private String itemid;
	private String version;
	
	public String getSyscd() {
		return syscd;
	}
	public void setSyscd(String syscd) {
		this.syscd = syscd;
	}
	public String getRsrcname() {
		return rsrcname;
	}
	public void setRsrcname(String rsrcname) {
		this.rsrcname = rsrcname;
	}
	public String getDirpath() {
		return dirpath;
	}
	public void setDirpath(String dirpath) {
		this.dirpath = dirpath;
	}
	public String getMd5sum() {
		return md5sum;
	}
	public void setMd5sum(String md5sum) {
		this.md5sum = md5sum;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getItemid() {
		return itemid;
	}
	public void setItemid(String itemid) {
		this.itemid = itemid;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	
}
