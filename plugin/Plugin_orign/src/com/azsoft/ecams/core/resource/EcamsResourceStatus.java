package com.azsoft.ecams.core.resource;

import java.io.File;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.CoreException;

import com.azsoft.ecams.core.EcamsProviderPlugin;
import com.azsoft.ecams.core.IEcamsStatus;
import org.eclipse.core.runtime.QualifiedName;
import com.azsoft.ecams.properties.IProperty;
import com.azsoft.ecams.proto.ProtoEcams.FileData;
import com.azsoft.ecams.proto.ProtoEcams.JobInfo;
import com.azsoft.ecams.proto.ProtoEcams.PathInfo;
import com.azsoft.ecams.proto.ProtoEcams.RsrcInfo;
import com.azsoft.ecams.proto.ProtoEcams.SysInfo;

public class EcamsResourceStatus implements IEcamsStatus, Serializable {


	static final long serialVersionUID = 1L;
	
	protected File file;
	protected long lastChangedDate;
	protected String lastUser;
	protected String editor;
	protected String itemid;
	protected String md5sum;
	
	protected String rsrccd;
	protected String rsrccodename;
	protected String rsrcinfo;
	
	protected String jobcd;
	protected String jobname;
	protected String syscd;
	protected String sysmsg;

	protected int lastVer;
	protected int tstVer;
	protected String filestatus;
	protected boolean lock;
	protected boolean authority;
	protected boolean changed;
	protected String relativitePath;
	
	protected String tstmd5sum;
	protected String srid;
	protected String viewver;




	public EcamsResourceStatus(){
		super();
		
		this.file = null;
		this.lastChangedDate = 0;
		
		this.lastUser = "";
		this.editor = "";
		
		this.itemid = "";
		this.md5sum = "";
		
		this.lastVer = 0;
		this.tstVer = 0;
		this.filestatus = "";
		this.lock = true;
		this.authority = false;
		this.changed = false;
		
		this.tstmd5sum = "";
		this.srid = "";
		this.viewver = "";
	}
	
	public EcamsResourceStatus(IEcamsStatus status){
		super();
		
		this.file = status.getFile();
		this.lastChangedDate = ((Date)status.getLastChangedDate()).getTime();
		
		this.lastUser = status.getLastUser();
		this.editor = status.getEditor();
		
		this.itemid = status.getItemid();
		this.md5sum = status.getMd5sum();
		this.tstmd5sum = status.getTstmd5sum();
		
		this.lastVer = status.getLastVer();
		this.tstVer = status.getTstVer();
		this.viewver = status.getViewver();
		this.filestatus = status.getFileStatus();
		this.lock = status.isLocked();
		this.authority = status.isAuthority();
	}
	
		
	public String getEditor() {
		// TODO Auto-generated method stub
		return editor;
	}

	public File getFile() {
		// TODO Auto-generated method stub
		return file;
	}

	public String getItemid() {
		// TODO Auto-generated method stub
		return itemid;
	}

	public Date getLastChangedDate() {
		if (lastChangedDate == -1) {
			return null;
		} else {
			return new Date(lastChangedDate);
		}
	}

	public String getLastUser() {
		// TODO Auto-generated method stub
		return lastUser;
	}

	public int getLastVer() {
		// TODO Auto-generated method stub
		return lastVer;
	}
	
	public int getTstVer() {
		// TODO Auto-generated method stub
		return tstVer;
	}

	public String getPath() {
		return file.getParent();
	}
	
	public IPath getIPath() {
		return new Path(file.getAbsolutePath());
	}

	public boolean isLocked() {
		return lock;
	}
	
	public String getFileStatus() {
		return filestatus;
	}

	public void setFilestatus(String filestatus) {
		this.filestatus = filestatus;
	}


	public void setLock(boolean lock) {
		this.lock = lock;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public void setLastChangedDate(long lastChangedDate) {
		this.lastChangedDate = lastChangedDate;
	}

	public void setLastUser(String lastUser) {
		this.lastUser = lastUser;
	}

	public void setEditor(String editor) {
		this.editor = editor;
	}

	public void setItemid(String itemid) {
		this.itemid = itemid;
	}

	public void setLastVer(int lastVer) {
		this.lastVer = lastVer;
	}
	
	public void setTstVer(int tstVer) {
		this.tstVer = tstVer;
	}
	
	
	public String getName(){
		return this.file.getName();
	}

	public String getMd5sum() {
		return md5sum;
	}

	public void setMd5sum(String md5sum) {
		this.md5sum = md5sum;
	}
	
	public boolean isAuthority() {
		// TODO Auto-generated method stub
		return authority;
	}

	public void setAuthority(boolean authority) {
		this.authority = authority;
	}
	
	public boolean isChanged() {
		return changed;
	}

	public void setChanged(boolean changed) {
		this.changed = changed;
	}
	
	public String getRsrccd() {
		return rsrccd;
	}

	public void setRsrccd(String rsrccd) {
		this.rsrccd = rsrccd;
	}

	public String getRsrccodename() {
		return rsrccodename;
	}

	public void setRsrccodename(String rsrccodename) {
		this.rsrccodename = rsrccodename;
	}

	public String getRsrcinfo(){
		return rsrcinfo;
	}
	
	public void setRsrcinfo(String rsrcinfo){
		this.rsrcinfo = rsrcinfo;
	}
	public String getJobcd() {
		return jobcd;
	}

	public void setJobcd(String jobcd) {
		this.jobcd = jobcd;
	}

	public String getJobname() {
		return jobname;
	}

	public void setJobname(String jobname) {
		this.jobname = jobname;
	}
	
	public String getRelativitePath() {
		return relativitePath;
	}

	public void setRelativitePath(String relativitePath) {
		this.relativitePath = relativitePath;
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
	
	public String getTstmd5sum() {
		return tstmd5sum;
	}

	public void setTstmd5sum(String tstmd5sum) {
		this.tstmd5sum = tstmd5sum;
	}

	public String getSRId() {
		return srid;
	}

	public void setSRId(String srid) {
		this.srid = srid;
	}
	
	public String getViewver() {
		return viewver;
	}

	public void setViewver(String viewver) {
		this.viewver = viewver;
	}

	public FileData toFileData(){
		FileData.Builder fileData_builder = FileData.newBuilder();
		fileData_builder.setFilename(this.getName());
		fileData_builder.setMd5Sum(this.getMd5sum());
		fileData_builder.setTstmd5Sum(this.getTstmd5sum());
		fileData_builder.setVersion(this.getLastVer());
		fileData_builder.setTstver(this.getTstVer());
		fileData_builder.setViewver(this.getViewver());
		fileData_builder.setEditor(this.getEditor());
		fileData_builder.setLstUser(this.getLastUser());
		PathInfo.Builder pathinfo_builder = PathInfo.newBuilder();
		pathinfo_builder.setRelativitePath(this.getRelativitePath());
		fileData_builder.setPathinfo(pathinfo_builder.build());
		RsrcInfo.Builder rsrcinfo_builder = RsrcInfo.newBuilder();
		rsrcinfo_builder.setRsrccd(this.getRsrccd());
		rsrcinfo_builder.setRsrcmsg(this.getRsrccodename());
		rsrcinfo_builder.setCminfo(this.getRsrcinfo());
		fileData_builder.setRsrcinfo(rsrcinfo_builder.build());
		JobInfo.Builder jobinfo_builder = JobInfo.newBuilder();
		jobinfo_builder.setJobcd(this.getJobcd());
		jobinfo_builder.setJobname(this.getJobname());
		fileData_builder.setJobinfo(jobinfo_builder);
		fileData_builder.setStatus(this.getFileStatus());
		fileData_builder.setLstdate(Long.toString(((Date)this.getLastChangedDate()).getTime()));
		if (this.getItemid() != null){
			fileData_builder.setItemid(this.getItemid());
		}
		SysInfo.Builder sysinfo_builder = SysInfo.newBuilder();
		sysinfo_builder.setSyscd(this.getSyscd());
		sysinfo_builder.setSysmsg(this.getSysmsg());
		fileData_builder.setSysinfo(sysinfo_builder.build());
		return fileData_builder.build();
	}


	public void setStatus(FileData filedata ,IProject project){
		try {
			String projectPath = project.getLocation().toOSString();
			//String filepath = (projectPath+"/"+filedata.getRsrcinfo().getRsrcmsg()+"/"+filedata.getPathinfo().getRelativitePath());
			String filepath = (projectPath+"/"+filedata.getPathinfo().getRelativitePath());
			
			while(filepath.indexOf("/") >=0){
				filepath = filepath.replace("/","\\");
			}
			
			while(filepath.indexOf("\\\\") >=0){
				filepath = filepath.replace("\\\\", "\\");
			}
			
			while(filepath.indexOf("\\") >=0){
				filepath = filepath.replace("\\", "/");
			}		    			
			
			String filename = filepath+"/"+filedata.getFilename();
			
			this.setRelativitePath(filedata.getPathinfo().getRelativitePath());
			this.setFile(new File(filename));
			this.setEditor(filedata.getEditor());
			this.setLastUser(filedata.getLstUser());
			this.setFilestatus(filedata.getStatus());
			this.setItemid(filedata.getItemid());
			this.setLastVer(filedata.getVersion());
			this.setTstVer(filedata.getTstver());
			this.setViewver(filedata.getViewver());
			this.setSRId(filedata.getSrinfo().getCcSRId());
	
			SimpleDateFormat formater = new SimpleDateFormat("yyyy/MM/dd/HH/mm/ss");
			Date lstDate;
			
			lstDate = formater.parse(filedata.getLstdate());
	
			this.setLastChangedDate(lstDate.getTime());
			/*
			String ip = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.IP, "", null);
			String port = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PORT, "", null);
			String id = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.ID, "", null);
			String passwd = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PASSWD, "", null);
			
			EcamsMessage.Builder builder_msg = EcamsMessage.newBuilder();
			builder_msg.setMsgtype("JOBLIST_GET");
			
			SysInfo.Builder sysinfo_builder = SysInfo.newBuilder();
			sysinfo_builder.setSyscd(filedata.getSysinfo().getSyscd());
			sysinfo_builder.setSysmsg(filedata.getSysinfo().getSysmsg());
			builder_msg.setSysinfo(sysinfo_builder.build());
			
			UserInfo.Builder userinfo_builder = UserInfo.newBuilder();
			userinfo_builder.setId(id);
			userinfo_builder.setPasswd(passwd);
			builder_msg.setUserinfo(userinfo_builder.build());				
			
			EcamsClient syncClient = new EcamsClient(ip,port);
			ReturnMsg returnMsg = syncClient.sendMsg(builder_msg.build());
			
			int jobCnt = returnMsg.getEcamsmsg().getJobinfolist().getJobinfoCount();
			
			for(int i=0;i<jobCnt;i++){
				if( returnMsg.getEcamsmsg().getJobinfolist().getJobinfo(i).getJobcd().equals(filedata.getJobinfo().getJobcd()) ) {
					tf = true;
					break;
				}
			}*/
			boolean tf = false;
			try {
				String[] jobary;
				jobary = project.getPersistentProperty(new QualifiedName("Properties","setjobcd")).split("/");
				for (int j=0;j<jobary.length;j++){
					if(filedata.getJobinfo().getJobcd().equals(jobary[j].split(":")[0])){
						tf=true;
						break;
					}
				}
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			/*
			String jobcd = "";
			try {
				jobcd = project.getPersistentProperty(new QualifiedName("Properties","setjobcd")).split(":")[0];
				if(filedata.getJobinfo().getJobcd().equals(jobcd)) tf = true;
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			*/
			if(tf){
				this.setAuthority(true);
			}else{
				this.setAuthority(false);
			}
			
			if (filedata.getStatus().split(":")[1].equals("5")){
				if (Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.ID, "", null).equals(filedata.getEditor().split(":")[1])){
					this.setLock(false);
				}
				else{
					this.setLock(true);
				}
			}
			else if(filedata.getStatus().split(":")[1].equals("3")){
				this.setLock(false);
			}
			else{
				this.setLock(true);
			}
			/*
			if (filedata.getStatus().split(":")[1].equals("5")){
				this.setLock(true);
			}else {
				this.setLock(false);
			}
			*/
			this.setMd5sum(filedata.getMd5Sum());
			this.setTstmd5sum(filedata.getTstmd5Sum());
			this.setChanged(false);
			
			this.setRsrccd(filedata.getRsrcinfo().getRsrccd());
			this.setRsrccodename(filedata.getRsrcinfo().getRsrcmsg());
			this.setRsrcinfo(filedata.getRsrcinfo().getCminfo());
			this.setJobcd(filedata.getJobinfo().getJobcd());
			this.setJobname(filedata.getJobinfo().getJobname());
			this.setSyscd(filedata.getSysinfo().getSyscd());
			this.setSysmsg(filedata.getSysinfo().getSysmsg());
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}
