package app.ecams.user.model;

import java.math.BigDecimal;
import java.util.Date;

public class UserInfo {
	private String cm_userid;
	private String cm_username;
	private String cm_cpasswd;
	private Date cm_logindt;
	private BigDecimal cm_ercount;
	private String cm_admin;
	private String cm_status;
	private String cm_active;
	private String cm_manid;
	private String cm_deptseq;
	private String cm_project;
	private String teamname;
	private String cm_duty;
	private String cm_position;
	private String teamcd;
	
	public String getCm_userid() {
		return cm_userid;
	}
	public void setCm_userid(String cmUserid) {
		cm_userid = cmUserid;
	}
	public String getCm_username() {
		return cm_username;
	}
	public void setCm_username(String cmUsername) {
		cm_username = cmUsername;
	}
	public String getCm_cpasswd() {
		return cm_cpasswd;
	}
	public void setCm_cpasswd(String cmCpasswd) {
		cm_cpasswd = cmCpasswd;
	}
	public Date getCm_logindt() {
		return cm_logindt;
	}
	public void setCm_logindt(Date cmLogindt) {
		cm_logindt = cmLogindt;
	}
	public BigDecimal getCm_ercount() {
		return cm_ercount;
	}
	public void setCm_ercount(BigDecimal cmErcount) {
		cm_ercount = cmErcount;
	}
	public String getCm_admin() {
		return cm_admin;
	}
	public void setCm_admin(String cmAdmin) {
		cm_admin = cmAdmin;
	}
	public String getCm_status() {
		return cm_status;
	}
	public void setCm_status(String cmStatus) {
		cm_status = cmStatus;
	}
	public String getCm_active() {
		return cm_active;
	}
	public void setCm_active(String cmActive) {
		cm_active = cmActive;
	}
	public String getCm_manid() {
		return cm_manid;
	}
	public void setCm_manid(String cmManid) {
		cm_manid = cmManid;
	}
	public String getCm_deptseq() {
		return cm_deptseq;
	}
	public void setCm_deptseq(String cmDeptseq) {
		cm_deptseq = cmDeptseq;
	}
	public String getCm_project() {
		return cm_project;
	}
	public void setCm_project(String cmProject) {
		cm_project = cmProject;
	}
	public String getTeamname() {
		return teamname;
	}
	public void setTeamname(String teamname) {
		this.teamname = teamname;
	}
	public String getCm_duty() {
		return cm_duty;
	}
	public void setCm_duty(String cmDuty) {
		cm_duty = cmDuty;
	}
	public String getCm_position() {
		return cm_position;
	}
	public void setCm_position(String cmPosition) {
		cm_position = cmPosition;
	}
	public void setTeamcd(String teamcd) {
		this.teamcd = teamcd;
	}
	public String getTeamcd() {
		return teamcd;
	}	
}
