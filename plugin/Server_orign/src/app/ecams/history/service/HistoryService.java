package app.ecams.history.service;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import app.ecams.history.dao.IHistoryDAO;
import app.core.exception.RollBackException;
import app.core.proto.ProtoEcams.EcamsMessage;
import app.core.proto.ProtoEcams.FileData;
import app.core.proto.ProtoEcams.ReturnMsg;
import app.core.proto.ProtoEcams.HistoryData;
import app.core.proto.ProtoEcams.HistoryList;
import app.core.proto.ProtoEcams.SRInfo;
import app.ecams.history.model.HistoryInfo;

@Service
public class HistoryService implements IHistoryService {
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Autowired private IHistoryDAO historyDAO;
	public ReturnMsg getHistoryList_detail(EcamsMessage ecamsmsg) {
		ReturnMsg.Builder returnmsg_builder = ReturnMsg.newBuilder();
		EcamsMessage.Builder ecamsmsg_builder = ecamsmsg.toBuilder();
		int returnval = 0;
		String returnStr = "";
		try{
			//System.out.println("START getHistoryList_detail");
			HistoryList.Builder historylist_builder = HistoryList.newBuilder();
			List<HistoryInfo> historylist = (List<HistoryInfo>) historyDAO.getHistoryList_detail(ecamsmsg.getFiledata().getItemid());
			if(historylist==null){
				throw new Exception("getHistoryList_detail Error.");
			}
			//System.out.println("historylist.size():"+historylist.size());
			for(int i=0; i<historylist.size(); i++){
				HistoryData.Builder history_builder = HistoryData.newBuilder();

				FileData.Builder fileData = FileData.newBuilder();
				
				SRInfo.Builder srinfo_builder = SRInfo.newBuilder();
				if(null == historylist.get(i).getCcSrid()){
					srinfo_builder.setCcSRId("");
					srinfo_builder.setCcTitle("");
				}else{
					srinfo_builder.setCcSRId(historylist.get(i).getCcSrid());
					srinfo_builder.setCcTitle(historylist.get(i).getCcTitle());
				}
				
				fileData.setFilename(ecamsmsg.getFiledata().getFilename());
				fileData.setSrinfo(srinfo_builder.build());
				
				history_builder.setFiledata(fileData.build());
				
				if(historylist.get(i).getCrStatus().equals("3")){
					history_builder.setCmCodename(historylist.get(i).getCmCodename()+"[반려]");
				}else if(historylist.get(i).getCrStatus().equals("9")) {
					history_builder.setCmCodename(historylist.get(i).getCmCodename());
				}else {
					history_builder.setCmCodename(historylist.get(i).getCmCodename()+" 중");
				}
				history_builder.setCmUsername(historylist.get(i).getCmUsername());
				history_builder.setCrAcptdate(historylist.get(i).getCrAcptdate());
				history_builder.setCrAcptno(historylist.get(i).getCrAcptno());
				history_builder.setCrEditcon(historylist.get(i).getCrEditcon());
				
				if(null == historylist.get(i).getCrPrcdate()){
					history_builder.setCrPrcdate("");
				}else{
					history_builder.setCrPrcdate(historylist.get(i).getCrPrcdate());
				}
				history_builder.setCrQrycd(historylist.get(i).getCrQrycd());
				history_builder.setCrRsrccd(historylist.get(i).getCrRsrccd());
				history_builder.setCrStatus(historylist.get(i).getCrStatus());
				history_builder.setCrVersion(historylist.get(i).getCrVersion());
				history_builder.setCrDevenddt(historylist.get(i).getJoinver());
				
				historylist_builder.addHistorydata(history_builder.build());
			}
			
			if (historylist.size() > 0){
				returnval = 0;
				returnStr = "정상";
				ecamsmsg_builder.setHistorylist(historylist_builder.build());
				returnmsg_builder.setEcamsmsg(ecamsmsg_builder.build());				
			}
			else{
				returnval = 1;
				returnStr = "오류";			
			}

			//System.out.println("returnStr:"+returnStr);
			
			returnmsg_builder.setReturnval(returnval);
			returnmsg_builder.setReturnStr(returnStr);
			
			
		}catch(Exception e){
			logger.error("HistoryService: ",e);
		    returnmsg_builder.setReturnval(1);
		    returnmsg_builder.setReturnStr("message:"+e.getMessage());
		}
		return returnmsg_builder.build();
	}
}
