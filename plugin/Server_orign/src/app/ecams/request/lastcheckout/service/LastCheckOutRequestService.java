package app.ecams.request.lastcheckout.service;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.core.proto.ProtoEcams.EcamsMessage;
import app.core.proto.ProtoEcams.FileData;
import app.core.proto.ProtoEcams.FileDataList;
import app.core.proto.ProtoEcams.ReturnMsg;
import app.ecams.request.lastcheckout.dao.ILastCheckOutDAO;
import app.ecams.request.lastcheckout.model.LastCheckOutInfo;

@Service
public class LastCheckOutRequestService implements ILastCheckOutRequestService {
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Autowired private ILastCheckOutDAO lastCheckOutDAO;
	
	public ReturnMsg select_lastver(EcamsMessage ecamsmsg) {
		ReturnMsg.Builder returnmsg_builder = ReturnMsg.newBuilder();
		EcamsMessage.Builder ecamsmsg_builder = ecamsmsg.toBuilder();
		int returnval = 0;
		String returnStr = "";
		try{ 
			FileDataList.Builder lastverlist_builder = FileDataList.newBuilder();
			List<LastCheckOutInfo> lastversionlist = (List<LastCheckOutInfo>) lastCheckOutDAO.select_lastver(ecamsmsg.getFiledata().getItemid());

			for(int i=0; i<lastversionlist.size(); i++){
				FileData.Builder lstver_builder = FileData.newBuilder();
				
				lstver_builder.setLstdate(lastversionlist.get(i).getCrPrcdate());
				lstver_builder.setFilename(lastversionlist.get(i).getCrRsrcname()); 
				lstver_builder.setEditor(lastversionlist.get(i).getCmUsername());
				lstver_builder.setBasever(lastversionlist.get(i).getBaseVer());
				lstver_builder.setAcptno(lastversionlist.get(i).getAcptno());	// 20201230 서버에서도 신청번호 가져오게 변경
				if(lastversionlist.get(i).getEditCon() != null && !lastversionlist.get(i).getEditCon().equals("")){
					lstver_builder.setMsguse(lastversionlist.get(i).getEditCon());
				}else{
					lstver_builder.setMsguse("-");
				}
				
				lstver_builder.setViewver(lastversionlist.get(i).getBaseVer());
				
				lastverlist_builder.addFiledatas(lstver_builder.build());
			}
			
			if (lastversionlist.size() > 0){
				returnval = 0; 
				returnStr = "정상";
				ecamsmsg_builder.setFiledatalist(lastverlist_builder.build());
				returnmsg_builder.setEcamsmsg(ecamsmsg_builder.build());				
			}
			else{
				returnval = 1;
				returnStr = "오류";			
			}
			
			returnmsg_builder.setReturnval(returnval);
			returnmsg_builder.setReturnStr(returnStr);
			
			
		}catch(Exception e){
			logger.error("LastCheckOut Service: ",e);
		    returnmsg_builder.setReturnval(1);
		    returnmsg_builder.setReturnStr("message:"+e.getMessage());
		}
		return returnmsg_builder.build();
	}
}
