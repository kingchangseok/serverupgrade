package app.ecams.file.model;


public class FileInfo {
	private String itemid;
	private String rsrcname;
	private Integer lstver;
		
	private String lstUser;
	private String lstUserName;
	
	private String editor;
	private String editorName;

	private String lastdate;
	private String dirpath;

	private String md5sum; 
	private byte[] filebyte;
	
	private String status;
	private String statusName;
		
	private String resourceType;
	private String resourceTypeName;
	
	private String jobcd;
	private String jobName;
	
	private String cmSyscd;
	private String cmSysmsg;	
	
	private Integer tstver;
	private String tstmd5sum;
	
	private String cm_info;
	private String cmPrjname;
	
	private String cr_isrid;
	private String viewver;
	
	public Integer getLstver() {
		return lstver;
	}
	public void setLstver(Integer lstver) {
		this.lstver = lstver;
	}
	
	public void setFilebyte(byte[] filebyte) {
		this.filebyte = filebyte;
	}
	public byte[] getFilebyte() {
		return filebyte;
	}	
	
	public void setLstUser(String lstUser) {
		this.lstUser = lstUser;
	}
	public String getLstUser() {
		return lstUser;
	}

	public void setLstUserName(String lstUserName) {
		this.lstUserName = lstUserName;
	}
	public String getLstUserName() {
		return lstUserName;
	}
	
	public void setEditor(String editor) {
		this.editor = editor;
	}
	public String getEditor() {
		return editor;
	}

	public void setEditorName(String editorName) {
		this.editorName = editorName;
	}
	public String getEditorName() {
		return editorName;
	}
	public void setItemid(String itemid) {
		this.itemid = itemid;
	}
	public String getItemid() {
		return itemid;
	}
	public void setRsrcname(String rsrcname) {
		this.rsrcname = rsrcname;
	}
	public String getRsrcname() {
		return rsrcname;
	}
	public void setLastdate(String lastdate) {
		this.lastdate = lastdate;
	}
	public String getLastdate() {
		return lastdate;
	}
	public void setDirpath(String dirpath) {
		this.dirpath = dirpath;
	}
	public String getDirpath() {
		return dirpath;
	}
	public void setMd5sum(String md5sum) {
		this.md5sum = md5sum;
	}
	public String getMd5sum() {
		return md5sum;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	public String getStatus() {
		return status;
	}
	public void setStatusName(String statusName) {
		this.statusName = statusName;
	}
	public String getStatusName() {
		return statusName;
	}
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
	public String getJobcd() {
		return jobcd;
	}
	public void setJobcd(String jobcd) {
		this.jobcd = jobcd;
	}
	public String getJobName() {
		return jobName;
	}
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}
	public String getCmSyscd() {
		return cmSyscd;
	}
	public void setCmSyscd(String cmSyscd) {
		this.cmSyscd = cmSyscd;
	}
	public String getCmSysmsg() {
		return cmSysmsg;
	}
	public void setCmSysmsg(String cmSysmsg) {
		this.cmSysmsg = cmSysmsg;
	}
	
	public Integer getTstver() {
		return tstver;
	}
	public void setTstver(Integer tstver) {
		this.tstver =tstver;
	}
	
	public void setTstmd5sum(String tstmd5sum) {
		this.tstmd5sum = tstmd5sum;
	}
	public String getTstmd5sum() {
		return tstmd5sum;
	}
	public String getCm_info() {
		return cm_info;
	}
	public void setCm_info(String cmInfo) {
		cm_info = cmInfo;
	}
	public String getCmPrjname() {
		return cmPrjname;
	}
	public void setCmPrjname(String cmPrjname) {
		this.cmPrjname = cmPrjname;
	}
	public String getCr_isrid() {
		return cr_isrid;
	}
	public void setCr_isrid(String crIsrid) {
		cr_isrid = crIsrid;
	}
	public String getViewver() {
		return viewver;
	}
	public void setViewver(String viewver) {
		this.viewver = viewver;
	}

}
