package app.ecams.lang.model;

import app.core.proto.ProtoEcams.LangInfo;


public class Lang {
	private String micode;
	private String codename;
	
	
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
	
	public LangInfo toLangInfo(){
		LangInfo.Builder langInfo_builder = LangInfo.newBuilder();
		langInfo_builder.setLangcd(this.micode);
		langInfo_builder.setLangname(this.codename);
		return langInfo_builder.build();		
	}

}
