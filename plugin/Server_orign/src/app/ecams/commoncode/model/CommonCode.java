package app.ecams.commoncode.model;

import app.core.proto.ProtoEcams.SysInfo;
import app.core.proto.ProtoEcams.CodeInfo;

public class CommonCode {
	private String macode;
	private String micode;
	private String codename;
	
	
	public String getMacode() {
		return macode;
	}
	public void setMacode(String macode) {
		this.macode = macode;
	}
	public String getMicode() {
		return micode;
	}
	public void setMicode(String micode) {
		this.micode = micode;
	}
	public String getCodename() {
		return codename;
	}
	public void setCodename(String codename) {
		this.codename = codename;
	}
	
	public CodeInfo toCodeInfo(){
		CodeInfo.Builder  codeinfo_builder = CodeInfo.newBuilder();
		codeinfo_builder.setMacode(this.macode);
		codeinfo_builder.setMicode(this.micode);
		codeinfo_builder.setCodename(this.codename);
		return codeinfo_builder.build();		
	}
}
