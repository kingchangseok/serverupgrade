package app.ecams.system.service;


import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import app.core.exception.RollBackException;
import app.core.proto.ProtoEcams.EcamsMessage;
import app.core.proto.ProtoEcams.RsrcInfoList;
import app.core.proto.ProtoEcams.SysInfo;
import app.core.proto.ProtoEcams.SysInfoList;
import app.core.proto.ProtoEcams.ReturnMsg;
import app.ecams.system.dao.ISystemInfoDAO;
import app.ecams.system.model.SystemInfo;
import app.ecams.user.service.IUserService;

@Service
public class SystemService implements ISystemService {
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Autowired private ISystemInfoDAO systemInfoDAO;
	@Autowired private IUserService userService;
	

	@Transactional (readOnly = false, propagation = Propagation.REQUIRED, rollbackForClassName={"app.core.exception.RollBackException"})
	public ReturnMsg getSysInfo (EcamsMessage ecamsmsg){
		int i;
		int listsize;
		int returnval=0;
		String returnStr="";
		HashMap param=null;
		
		ReturnMsg.Builder returnmsg_builder = ReturnMsg.newBuilder();
		
		
		
		
		try{
			EcamsMessage.Builder ecamsmsg_builder = ecamsmsg.toBuilder();
			//param = new HashMap();
			
			//param.put("CM_USERID", ecamsmsg.getUserinfo().getId());
			//param.put("CM_USERID", null);

			List<SystemInfo> tmpList = (List<SystemInfo>) systemInfoDAO.getSysInfo();
			
			listsize = tmpList.size();
			
			if (listsize > 0){
				returnval = 0;
				returnStr = "정상";			
			}
			else{
				throw new RollBackException("처리에러");			
			}
			
			SysInfoList.Builder sysinfolist_builder = SysInfoList.newBuilder();
			SysInfo.Builder sysinfo_builder = SysInfo.newBuilder();
			
			for (i=0;i<listsize;i++){
				sysinfo_builder = SysInfo.newBuilder();
				
				sysinfo_builder.setSyscd(tmpList.get(i).getCmSyscd());
				sysinfo_builder.setSysmsg(tmpList.get(i).getCmSysmsg());
				sysinfo_builder.setSysinfo(tmpList.get(i).getCmSysinfo());
				//if(tmpList.get(i).getCmSysinfo().substring(10, 11).equals("1")){
				if(tmpList.get(i).getCmSysinfo().substring(12, 13).equals("1")){
					sysinfo_builder.setAnalyn("Y");
					if(tmpList.get(i).getCmPrjname() == null || tmpList.get(i).getCmPrjname().equals("")){
						sysinfo_builder.setPrjname("프로젝트명규칙미입력");
					}else{
						sysinfo_builder.setPrjname(tmpList.get(i).getCmPrjname());
					}
				}else{
					sysinfo_builder.setAnalyn("N");
				}
				
				sysinfolist_builder.addSysinfo(sysinfo_builder.build());
			}
			
			ecamsmsg_builder.setSysinfolist(sysinfolist_builder.build());
			returnmsg_builder.setEcamsmsg(ecamsmsg_builder.build());
		}
		catch(RollBackException e){
			returnval = 1;
			returnStr = e.getMessage().toString();				
		}
		returnmsg_builder.setReturnStr(returnStr);
		returnmsg_builder.setReturnval(returnval);
		return returnmsg_builder.build();
	}
	
	public ReturnMsg getSysInfo_user (EcamsMessage ecamsmsg){
		int i;
		int listsize;
		int returnval=0;
		String returnStr="";
		HashMap params=null;
		
		ReturnMsg.Builder returnmsg_builder = ReturnMsg.newBuilder();
		
		
		
		
		try{
			EcamsMessage.Builder ecamsmsg_builder = ecamsmsg.toBuilder();
			params = new HashMap();
			
			params.put("CM_USERID", ecamsmsg.getUserinfo().getId());

			//List<SystemInfo> tmpList = (List<SystemInfo>) systemInfoDAO.getSysInfo();
			List<SystemInfo> tmpList = (List<SystemInfo>) systemInfoDAO.getSysInfo_user(params);
			
			listsize = tmpList.size();
			
			if (listsize > 0){
				returnval = 0;
				returnStr = "정상";			
			}
			else{
				throw new RollBackException("처리에러");			
			}
			
			SysInfoList.Builder sysinfolist_builder = SysInfoList.newBuilder();
			SysInfo.Builder sysinfo_builder = SysInfo.newBuilder();
			
			for (i=0;i<listsize;i++){
				sysinfo_builder = SysInfo.newBuilder();
				
				sysinfo_builder.setSyscd(tmpList.get(i).getCmSyscd());
				sysinfo_builder.setSysmsg(tmpList.get(i).getCmSysmsg());
				sysinfo_builder.setSysinfo(tmpList.get(i).getCmSysinfo());
				//if(tmpList.get(i).getCmSysinfo().substring(10, 11).equals("1")){
				if(tmpList.get(i).getCmSysinfo().substring(12, 13).equals("1")){
					sysinfo_builder.setAnalyn("Y");
					if(tmpList.get(i).getCmPrjname() == null || tmpList.get(i).getCmPrjname().equals("")){
						sysinfo_builder.setPrjname("프로젝트명규칙미입력");
					}else{
						sysinfo_builder.setPrjname(tmpList.get(i).getCmPrjname());
					}
				}else{
					sysinfo_builder.setAnalyn("N");
				}
				
				sysinfolist_builder.addSysinfo(sysinfo_builder.build());
			}
			
			ecamsmsg_builder.setSysinfolist(sysinfolist_builder.build());
			returnmsg_builder.setEcamsmsg(ecamsmsg_builder.build());
		}
		catch(RollBackException e){
			returnval = 1;
			returnStr = e.getMessage().toString();				
		}
		returnmsg_builder.setReturnStr(returnStr);
		returnmsg_builder.setReturnval(returnval);
		return returnmsg_builder.build();
	}	
	
	public HashMap getSysInfo_detail(String syscd){
		try {
			String tstInfo = "";
			int tstsev = 0;
			
			HashMap sysinfo_detail = new HashMap();
			HashMap sysinfo_tmp = systemInfoDAO.getSysInfo_detail(syscd);
			
//			if (sysinfo_detail == null){
//				return null;
//			}
			
			if (sysinfo_tmp == null){
				return null;
			}
			
			sysinfo_detail.put("cm_sysgb", sysinfo_tmp.get("cm_sysgb"));
			sysinfo_detail.put("cm_sysfc1", sysinfo_tmp.get("cm_sysfc1"));
			sysinfo_detail.put("cm_dirbase", sysinfo_tmp.get("cm_dirbase"));
			tstInfo = (String) sysinfo_tmp.get("cm_sysinfo");
			
			if ( ((String)sysinfo_tmp.get("cm_sysinfo")).substring(4,5).equals("1")){
				if ((Integer)sysinfo_tmp.get("diff1")<0 && (Integer)sysinfo_tmp.get("diff2")>0) {
					sysinfo_detail.put("cm_stopsw","1");
					tstInfo = tstInfo.substring(0,4) + "1" + tstInfo.substring(5);
				}
				else{
					sysinfo_detail.put("cm_stopsw","0");
					tstInfo = tstInfo.substring(0,4) + "0" + tstInfo.substring(5);
				}
			}
			else{
				sysinfo_detail.put("cm_stopsw","0");
			}
			
			if (((String)sysinfo_detail.get("cm_stopsw")).equals("0")){
				HashMap timecheck = systemInfoDAO.timecheck(syscd);
				boolean checkflag = false;
				
				if (timecheck != null){
					if (((String)timecheck.get("cm_termcd")).equals("1")){ //매일
						checkflag = true;
					}
					else if (((String)timecheck.get("cm_termcd")).equals("2")){ //매주
						if (((String)timecheck.get("weekday")).equals(((String)timecheck.get("cm_termsub")))) {
							checkflag = true;
						}
					}
					else if (((String)timecheck.get("cm_termcd")).equals("3")){ //매월
						if (((String)timecheck.get("monday")).equals(((String)timecheck.get("cm_termsub")))) {
							checkflag = true;
						}
					}
					
					if (checkflag){
						if ( ((BigDecimal)timecheck.get("diff1")).intValue() <0 && ((BigDecimal)timecheck.get("diff2")).intValue()>0) {
							sysinfo_detail.put("cm_stopsw","1");
							tstInfo = tstInfo.substring(0,4) + "1" + tstInfo.substring(5);
						}
					}
				}
				
			}
			sysinfo_detail.put("cm_sysinfo",tstInfo);
			sysinfo_detail.put("uploadsw","N");
			
//			tstsev=systemInfoDAO.getTstSys_conn(syscd);
//						
//			if(tstsev<=0){
//				sysinfo_detail.put("TstSw","0");
//			}else{
//				sysinfo_detail.put("TstSw","1");
//			}
			
			return sysinfo_detail;
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	public HashMap sysdirinfo(String pathcd){
		try {
			
			HashMap sysdir_info = new HashMap();
			HashMap sysdir_tmp = systemInfoDAO.sysdirinfo(pathcd);
			
//			if (sysinfo_detail == null){
//				return null;
//			}
			
			if (sysdir_tmp == null){
				return null;
			}
			
			sysdir_info.put("cm_pathcd", sysdir_tmp.get("cm_pathcd"));
			sysdir_info.put("cm_path", sysdir_tmp.get("cm_path"));
			
			return sysdir_info;
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
}

