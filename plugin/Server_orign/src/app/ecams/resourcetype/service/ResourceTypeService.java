package app.ecams.resourcetype.service;


import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import app.core.proto.ProtoEcams.EcamsMessage;
import app.core.proto.ProtoEcams.RsrcInfoList;
import app.core.proto.ProtoEcams.JobInfoList;
import app.core.proto.ProtoEcams.JobInfo;
import app.core.proto.ProtoEcams.RsrcInfo;
import app.core.proto.ProtoEcams.ReturnMsg;
import app.core.proto.ProtoEcams.SysInfo;

import app.ecams.resourcetype.dao.IResourceTypeDAO;
import app.ecams.job.dao.IJobDAO;
import app.ecams.resourcetype.model.ResourceType;
import app.ecams.job.model.Job;

@Service
public class ResourceTypeService implements IResourceTypeService {
	@Autowired private IResourceTypeDAO resourceTypeDAO;
	@Autowired private IJobDAO jobDAO;
	
	public ReturnMsg getRsrcInfo (EcamsMessage ecamsmsg){
		int i;
		int listsize;
		int joblistsize;
		int returnval=0;
		String returnStr="";		
		
		ReturnMsg.Builder returnmsg_builder = ReturnMsg.newBuilder();
		
		
		EcamsMessage.Builder ecamsmsg_builder = ecamsmsg.toBuilder();
		SysInfo.Builder sysinfo_builder = ecamsmsg.getSysinfo().toBuilder();
		
		
		try{
			
			String cmSyscd = null;
			String cmUserid = null;
			
			cmSyscd = ecamsmsg.getSysinfo().getSyscd();
			cmUserid = ecamsmsg.getUserinfo().getId();
			
			
			List<ResourceType> tmpList = (List<ResourceType>) resourceTypeDAO.getRsrcInfo(cmSyscd);
			listsize = tmpList.size();
			
			if (listsize > 0){
				returnval = 0;
				returnStr = "정상";			
			}
			else{
				returnval = 1;
				returnStr = "처리에러";			
			}
			
			RsrcInfoList.Builder rsrcinfolist_builder = RsrcInfoList.newBuilder();
			JobInfoList.Builder jobinfolist_builder = JobInfoList.newBuilder();
			
			for (i=0;i<listsize;i++){
				rsrcinfolist_builder.addRsrcinfo(tmpList.get(i).toRsrcInfo());
			}
			
			HashMap param = new HashMap();
			param.put("CM_SYSCD", cmSyscd);
			param.put("CM_USERID", cmUserid);
			
			List<Job> jobList = jobDAO.getJobInfo(param);
			joblistsize = jobList.size();
			
			for (i=0;i<joblistsize;i++){
				jobinfolist_builder.addJobinfo(jobList.get(i).toJobInfo());
			}
			
			sysinfo_builder.setRsrcinfolist(rsrcinfolist_builder.build());
			ecamsmsg_builder.setSysinfo(sysinfo_builder);
			ecamsmsg_builder.setJobinfolist(jobinfolist_builder);
			returnmsg_builder.setEcamsmsg(ecamsmsg_builder.build());
		}
		catch(Exception e){
			returnval = 1;
			returnStr = e.getMessage().toString();		
		}
		returnmsg_builder.setReturnStr(returnStr);
		returnmsg_builder.setReturnval(returnval);
		return returnmsg_builder.build();		
	}

	public HashMap getRsrcInfo_detail(String syscd, String rsrccd) {
		// TODO Auto-generated method stub
		HashMap param = new HashMap();
		param.put("CM_SYSCD", syscd);
		param.put("CM_RSRCCD", rsrccd);		
		return resourceTypeDAO.getRsrcInfo_detail(param);
	}

}
