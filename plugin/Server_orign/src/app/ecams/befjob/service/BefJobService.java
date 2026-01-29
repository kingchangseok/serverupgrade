package app.ecams.befjob.service;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import app.ecams.befjob.dao.IBefJobDAO;
import app.core.exception.RollBackException;
import app.core.proto.ProtoEcams.EcamsMessage;
import app.core.proto.ProtoEcams.ReturnMsg;
import app.core.proto.ProtoEcams.BefJobData;
import app.core.proto.ProtoEcams.BefJobList;
import app.ecams.befjob.model.BefJobInfo;

@Service
public class BefJobService implements IBefJobService {
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Autowired private IBefJobDAO befjobDAO;
	@Transactional (readOnly = false, propagation = Propagation.REQUIRED, rollbackForClassName={"app.core.exception.RollBackException"})
	public ReturnMsg getBefJobList_detail(EcamsMessage ecamsmsg) {
		ReturnMsg.Builder returnmsg_builder = ReturnMsg.newBuilder();
		EcamsMessage.Builder ecamsmsg_builder = ecamsmsg.toBuilder();
		int returnval = 0;
		String returnStr = "";
		try{
			BefJobList.Builder befjoblist_builder = BefJobList.newBuilder();
			List<BefJobInfo> befjoblist = (List<BefJobInfo>) befjobDAO.getBefJobList_detail();
			if(befjoblist==null){
				throw new Exception("getBefJobList_detail Error.");
			}
			for(int i=0; i<befjoblist.size(); i++){
				BefJobData.Builder befjob_builder = BefJobData.newBuilder();
				
				befjob_builder.setCrAcptno(befjoblist.get(i).getCrAcptno());
				befjob_builder.setCrAcptdate(befjoblist.get(i).getCrAcptdate());
				befjob_builder.setCrSysmsg(befjoblist.get(i).getCrSysmsg());
				befjob_builder.setCrUsername(befjoblist.get(i).getCrUsername());
				befjob_builder.setCrStatus(befjoblist.get(i).getCrStatus());
				befjob_builder.setCrSayu(befjoblist.get(i).getCrSayu());
				befjoblist_builder.addBefjobdata(befjob_builder.build());
			}
			
			if (befjoblist.size() > 0){
				returnval = 0;
				returnStr = "정상";
				ecamsmsg_builder.setBefjoblist(befjoblist_builder.build());
				returnmsg_builder.setEcamsmsg(ecamsmsg_builder.build());				
			}
			else{
				returnval = 1;
				returnStr = "오류";			
			}
			
			returnmsg_builder.setReturnval(returnval);
			returnmsg_builder.setReturnStr(returnStr);
			
			
		}catch(Exception e){
			logger.error("BefJobService: ",e);
		    returnmsg_builder.setReturnval(1);
		    returnmsg_builder.setReturnStr("message:"+e.getMessage());
		}finally{			
			return returnmsg_builder.build();
		}
	}
}
