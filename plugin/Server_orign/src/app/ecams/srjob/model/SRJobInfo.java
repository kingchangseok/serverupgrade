package app.ecams.srjob.model;

public class SRJobInfo {
	
	private String ccID; //SR ID
	private String ccTitle; //SR 제목
	private String ccComment; //SR내용
	private String ccStatusCD; //SR 상태코드
	private String ccStatus; // SR 상태
	private String ccEditor; //작성자
	private String ccEditorName; //작성자 이름
	private String ccGbn;//SR구분코드 
	private String ccGbnName;//SR구분코드명
	
	private String ccCatType;
	private String ccChgType;
	private String ccWorkRank;
	
	public String getCcCatType() {
		return ccCatType;
	}
	public void setCcCatType(String ccCatType) {
		this.ccCatType = ccCatType;
	}
	public String getCcChgType() {
		return ccChgType;
	}
	public void setCcChgType(String ccChgType) {
		this.ccChgType = ccChgType;
	}
	public String getCcWorkRank() {
		return ccWorkRank;
	}
	public void setCcWorkRank(String ccWorkRank) {
		this.ccWorkRank = ccWorkRank;
	}
	private String itemid;
	private String rsrcname;
	private String syscd;
	private String sysmsg;
	private String prjname;
	private String jobcd;
	private String dsncd;
	private String status;
	private String dirpath;
	private String rsrccd;
	
	private String lstver;
	private String tstver;
	private String jobname;
	private String lstusr;
	private String editorname;
	private String lstusrname;
	private String lastdate;
	private String sta;
	private String jawon;
	private String md5sum;
	private String tstmd5;
	private String cm_info;
	private String basepath;	// 20210106 SR사용안함일때 리소스목록 새로고침 추가
	
	public String getLstver() {
		return lstver;
	}
	public void setLstver(String lstver) {
		this.lstver = lstver;
	}
	public String getJobname() {
		return jobname;
	}
	public void setJobname(String jobname) {
		this.jobname = jobname;
	}
	public String getLstusr() {
		return lstusr;
	}
	public void setLstusr(String lstusr) {
		this.lstusr = lstusr;
	}
	public String getEditorname() {
		return editorname;
	}
	public void setEditorname(String editorname) {
		this.editorname = editorname;
	}
	public String getLstusrname() {
		return lstusrname;
	}
	public void setLstusrname(String lstusrname) {
		this.lstusrname = lstusrname;
	}
	public String getLastdate() {
		return lastdate;
	}
	public void setLastdate(String lastdate) {
		this.lastdate = lastdate;
	}
	public String getSta() {
		return sta;
	}
	public void setSta(String sta) {
		this.sta = sta;
	}
	public String getJawon() {
		return jawon;
	}
	public void setJawon(String jawon) {
		this.jawon = jawon;
	}
	public String getMd5sum() {
		return md5sum;
	}
	public void setMd5sum(String md5sum) {
		this.md5sum = md5sum;
	}
	public String getCm_info() {
		return cm_info;
	}
	public void setCm_info(String cmInfo) {
		cm_info = cmInfo;
	}
	public String getCcID() {
		return ccID;
	}
	public String getCcComment() {
		return ccComment;
	}
	public void setCcComment(String ccComment) {
		this.ccComment = ccComment;
	}
	public void setCcID(String ccID) {
		this.ccID = ccID;
	}
	
	public String getCcTitle() {
		return ccTitle;
	}
	public void setCcTitle(String ccTitle) {
		this.ccTitle = ccTitle;
	}
	
	public String getCcStatusCD() {
		return ccStatusCD;
	}
	public void setCcStatusCD(String ccStatusCD) {
		this.ccStatusCD = ccStatusCD;
	}
	
	public String getCcStatus() {
		return ccStatus;
	}
	public void setCcStatus(String ccStatus) {
		this.ccStatus = ccStatus;
	}
	
	public String getCcEditor() {
		return ccEditor;
	}
	public void setCcEditor(String ccEditor) {
		this.ccEditor = ccEditor;
	}
	
	public String getCcEditorName() {
		return ccEditorName;
	}
	public void setCcEditorName(String ccEditorName) {
		this.ccEditorName = ccEditorName;
	}
	public String getCcGbn() {
		return ccGbn;
	}
	public void setCcGbn(String ccGbn) {
		this.ccGbn = ccGbn;
	}
	public String getCcGbnName() {
		return ccGbnName;
	}
	public void setCcGbnName(String ccGbnName) {
		this.ccGbnName = ccGbnName;
	}
	public String getItemid() {
		return itemid;
	}
	public void setItemid(String itemid) {
		this.itemid = itemid;
	}
	public String getRsrcname() {
		return rsrcname;
	}
	public void setRsrcname(String rsrcname) {
		this.rsrcname = rsrcname;
	}
	public String getSyscd() {
		return syscd;
	}
	public void setSyscd(String syscd) {
		this.syscd = syscd;
	}
	public String getSysmsg() {
		return sysmsg;
	}
	public void setSysmsg(String sysmsg) {
		this.sysmsg = sysmsg;
	}
	public String getPrjname() {
		return prjname;
	}
	public void setPrjname(String prjname) {
		this.prjname = prjname;
	}	
	public String getJobcd() {
		return jobcd;
	}
	public void setJobcd(String jobcd) {
		this.jobcd = jobcd;
	}
	public String getDsncd() {
		return dsncd;
	}
	public void setDsncd(String dsncd) {
		this.dsncd = dsncd;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getDirpath() {
		return dirpath;
	}
	public void setDirpath(String dirpath) {
		this.dirpath = dirpath;
	}
	public String getRsrccd() {
		return rsrccd;
	}
	public void setRsrccd(String rsrccd) {
		this.rsrccd = rsrccd;
	}
	public String getTstver() {
		return tstver;
	}
	public void setTstver(String tstver) {
		this.tstver = tstver;
	}
	public String getTstmd5() {
		return tstmd5;
	}
	public void setTstmd5(String tstmd5) {
		this.tstmd5 = tstmd5;
	}
	public String getBasepath() {
		return basepath;
	}
	public void setBasepath(String basepath) {
		this.basepath = basepath;
	}
}
