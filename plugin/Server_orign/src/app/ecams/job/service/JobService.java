package app.ecams.job.service;


import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.protobuf.ByteString;


import app.core.proto.ProtoEcams.EcamsMessage;
import app.core.proto.ProtoEcams.FileData;
import app.core.proto.ProtoEcams.RsrcInfoList;
import app.core.proto.ProtoEcams.JobInfoList;
import app.core.proto.ProtoEcams.ReturnMsg;
import app.core.proto.ProtoEcams.SysInfo;

import app.ecams.file.model.FileInfo;
import app.ecams.job.dao.IJobDAO;
import app.ecams.job.model.Job;

@Service
public class JobService implements IJobService {
	private Logger logger = Logger.getLogger(this.getClass());
	@Autowired private IJobDAO JobDAO;	
	
	public List<Job> getJobInfo (HashMap param){
		int i;
		int joblistsize;
		int returnval=0;
		
			String cmSyscd = null;
			String cmUserid = null;
			
			cmSyscd = (String) param.get("CM_SYSCD");
			cmUserid = (String) param.get("CM_USERID");
			
			HashMap params = new HashMap();
			params.put("CM_SYSCD", cmSyscd);
			params.put("CM_USERID", cmUserid);
			
		//	List<ResourceType> jobList = JobDAO.getJobInfo(param);
			
			return JobDAO.getJobInfo(params);
		
	}
	
	public List<Job> getJobCheck (HashMap param){
		return JobDAO.getJobCheck(param);
	}
	
	public String getJobCd(HashMap params){
		return (String)JobDAO.getJobCd(params);
	}
	
	public ReturnMsg getMyJobCheck (EcamsMessage ecamsmsg){
		ReturnMsg.Builder returnmsg_builder = ReturnMsg.newBuilder();
		
		try{
			HashMap param = new HashMap();
			param.put("CM_SYSCD",ecamsmsg.getSysinfo().getSyscd());
			param.put("CM_JOBCD",ecamsmsg.getJobinfolist().getJobinfo(0).getJobcd());
			param.put("CM_USERID",ecamsmsg.getUserinfo().getId());
			
			//logger.error(ecamsmsg.getSysinfo().getSyscd()+" , "+ecamsmsg.getJobinfolist().getJobinfo(0).getJobcd()+" , "+ecamsmsg.getUserinfo().getId());
			
			String getFile = JobDAO.getMyJobCheck(param);
			
			//logger.error("jobcd : " + getFile);
			
			if (getFile == null || getFile.equals("")){
				returnmsg_builder.setReturnStr("권한이 없는 업무입니다.");
				returnmsg_builder.setReturnval(1);
				return returnmsg_builder.build();
			}

			returnmsg_builder.setReturnStr("정상");
			returnmsg_builder.setReturnval(0);
						
		}
		catch(Exception e){
			returnmsg_builder.setReturnStr(e.getMessage());
			returnmsg_builder.setReturnval(1);
		}
		return returnmsg_builder.build();
	}
}
