package app.ecams.request.lastcheckout.model;

public class LastCheckOutInfo {

	private String crPrcdate;
	private int crVer;
	private String crRsrcname;
	private String cmUsername;
	private String BaseVer;
	private String Vergbn;
	private String editCon;
	private String acptno;
	
	public String getCrPrcdate() {
		return crPrcdate;
	}
	public void setCrPrcdate(String crPrcdate) {
		this.crPrcdate = crPrcdate;
	}
	public int getCrVer() {
		return crVer;
	}
	public void setCrVer(int crVer) {
		this.crVer = crVer;
	}
	public String getCrRsrcname() {
		return crRsrcname;
	}
	public void setCrRsrcname(String crRsrcname) {
		this.crRsrcname = crRsrcname;
	}
	public String getCmUsername() {
		return cmUsername;
	}
	public void setCmUsername(String cmUsername) {
		this.cmUsername = cmUsername;
	}
	public void setBaseVer(String baseVer) {
		BaseVer = baseVer;
	}
	public String getBaseVer() {
		return BaseVer;
	}
	public void setVergbn(String vergbn) {
		Vergbn = vergbn;
	}
	public String getVergbn() {
		return Vergbn;
	}
	public String getEditCon() {
		return editCon;
	}
	public void setEditCon(String editCon) {
		this.editCon = editCon;
	}
	public String getAcptno() {
		return acptno;
	}
	public void setAcptno(String acptno) {
		this.acptno = acptno;
	}
	
}
