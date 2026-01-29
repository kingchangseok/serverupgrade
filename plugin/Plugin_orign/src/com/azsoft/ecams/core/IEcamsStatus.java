package com.azsoft.ecams.core;

import java.io.File;
import java.util.Date;

import org.eclipse.core.resources.IProject;


import com.azsoft.ecams.proto.ProtoEcams.FileData;

public interface IEcamsStatus {
	
	//디렉토리
	public String getPath();
	
	//파일명(filename)
	public String getName();
			
	public File getFile();
	
	//마지막 체크인 날짜(lastdate)
	public Date getLastChangedDate();
	
	//마지막 체크인 사용자(lasteditor)
	public String getLastUser();
	
	//체크아웃 사용자(editor)
	public String getEditor();
	
	//md5sum
	public String getMd5sum();
		
	//파일 버전
	public int getLastVer();
		
	//isLocked
	public boolean isLocked();
	
	//파일ID itemid(key)
	public String getItemid();
	
	//tstmd5sum
	public String getTstmd5sum();
	
	//TEST 파일 버전
	public int getTstVer();
	
	public String getFileStatus();
	public String getRelativitePath();
	
	public String getSRId();
	public String getViewver();

	public boolean isAuthority();
	public boolean isChanged();
		
	public void setFilestatus(String filestatus);
	public void setLock(boolean lock);
	public void setFile(File file);
	public void setLastChangedDate(long lastChangedDate);
	public void setLastUser(String lastCheckinUser);
	public void setEditor(String checkOutUser);
	public void setItemid(String itemid);
	public void setLastVer(int lastVer);
	public void setAuthority(boolean authority);
	public void setMd5sum(String md5sum);
	public void setChanged(boolean changed);
	public void setRelativitePath(String relativitePath);
	public void setTstVer(int tstVer);
	public void setTstmd5sum(String tstmd5sum);
	public void setSRId(String srid);
	public void setViewver(String viewver);
	
	public String getRsrccd();

	public void setRsrccd(String rsrccd);

	public String getRsrccodename();

	public void setRsrccodename(String rsrccodename);

	public String getRsrcinfo();
	
	public void setRsrcinfo(String rsrcinfo);
	
	public String getJobcd();

	public void setJobcd(String jobcd);

	public String getJobname();

	public void setJobname(String jobname);

	public String getSyscd();

	public void setSyscd(String syscd);

	public String getSysmsg();

	public void setSysmsg(String sysmsg);
	
	public FileData toFileData();
	
	public void setStatus(FileData filedata ,IProject project);

}

