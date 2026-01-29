package app.ecams.anal.service;

import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import app.ecams.anal.dao.IAnalDAO;
import app.core.exception.RollBackException;
import app.core.proto.ProtoEcams.EcamsMessage;
import app.core.proto.ProtoEcams.FileDataList;
import app.core.proto.ProtoEcams.ReturnMsg;
import app.core.proto.ProtoEcams.AnalData;
import app.core.proto.ProtoEcams.AnalDataList;
import app.ecams.anal.model.AnalInfo;

@Service
public class AnalService implements IAnalService {
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Autowired private IAnalDAO analDAO;
	@Transactional (readOnly = false, propagation = Propagation.REQUIRED, rollbackForClassName={"app.core.exception.RollBackException"})
	public ReturnMsg getAnalList_detail(EcamsMessage ecamsmsg) {
		ReturnMsg.Builder returnmsg_builder = ReturnMsg.newBuilder();
		EcamsMessage.Builder ecamsmsg_builder = ecamsmsg.toBuilder();
		int returnval = 0;
		String returnStr = "";
		
		try{
			
			AnalDataList.Builder anallist_builder = AnalDataList.newBuilder();
			List<AnalInfo> anallist = null;
			int i,j=0;
			HashMap param = new HashMap();
			for (i=0 ; i<ecamsmsg.getAnaldatalist().getAnaldatasCount() ; i++){
				param = new HashMap();
				param.put("itemid",ecamsmsg.getFiledata().getItemid());
				param.put("methid",ecamsmsg.getAnaldatalist().getAnaldatas(i).getMethid());
				param.put("Depth","2");
				List<AnalInfo> tmpAnallist = (List<AnalInfo>) analDAO.getAnalList_detail(param);
//				if(tmpAnallist==null){
//					throw new Exception("getAnalList_detail Error.");
//				}
				for (j=0 ; j<tmpAnallist.size() ; j++){
					tmpAnallist.get(j).setItemid(ecamsmsg.getFiledata().getItemid());
				}
				if(tmpAnallist.size()>0){
					anallist.add((AnalInfo) tmpAnallist);
				}
			}
			if(anallist != null){
				for(i=0; i<anallist.size(); i++){
					
					AnalData.Builder anal_builder = AnalData.newBuilder();
					
					anal_builder.setMethid(anallist.get(i).getMethid());
					anal_builder.setMethseqn(anallist.get(i).getMethseqn());
					anal_builder.setClassnm(anallist.get(i).getClassnm());
					anal_builder.setItemid(anallist.get(i).getItemid());
					anal_builder.setCalllevel(anallist.get(i).getCalllevel());
					anal_builder.setResrckey(anallist.get(i).getResrckey());
					anal_builder.setLinepos(anallist.get(i).getLinepos());
					anal_builder.setFilename(anallist.get(i).getFilename());
					anal_builder.setToresrckey(anallist.get(i).getToresrckey());
					anal_builder.setTomethseqn(anallist.get(i).getTomethseqn());
					anal_builder.setTomethid(anallist.get(i).getTomethid());
					
					anallist_builder.addAnaldatas(anal_builder.build());
				}
			
				returnval = 0;
				returnStr = "정상";
				ecamsmsg_builder.setAnaldatalist(anallist_builder.build());
				returnmsg_builder.setEcamsmsg(ecamsmsg_builder.build());				
			}
			else{
				returnval = 2;
				returnStr = "데이터없음";			
			}
			
			returnmsg_builder.setReturnval(returnval);
			returnmsg_builder.setReturnStr(returnStr);
			
			
		}catch(Exception e){
			logger.error("AnalService: ",e);
		    returnmsg_builder.setReturnval(1);
		    returnmsg_builder.setReturnStr("message:"+e.getMessage());
		}finally{			
			return returnmsg_builder.build();
		}
	}
	
	public ReturnMsg getMethod(EcamsMessage ecamsmsg) {
		
		ReturnMsg.Builder returnmsg_builder = ReturnMsg.newBuilder();
		FileDataList.Builder fileDataList_builder = FileDataList.newBuilder();
		AnalDataList.Builder anallist_builder = AnalDataList.newBuilder();
		int i,j,datalength;
		List<AnalInfo> anallist = null;
		
		try{
			FileDataList fileDataList = ecamsmsg.getFiledatalist();
			datalength = fileDataList.getFiledatasCount();

			if(anallist != null){
				for(i=0; i<anallist.size(); i++){
					
					AnalData.Builder anal_builder = AnalData.newBuilder();
					
					anal_builder.setMethid(anallist.get(i).getMethid());
					anal_builder.setMethseqn(anallist.get(i).getMethseqn());
					anal_builder.setClassnm(anallist.get(i).getClassnm());
					anal_builder.setItemid(anallist.get(i).getItemid());
					
					anallist_builder.addAnaldatas(anal_builder.build());
				}
				returnmsg_builder.setReturnval(0);
				returnmsg_builder.setReturnStr("정상");
			}
			else{
				returnmsg_builder.setReturnval(2);
				returnmsg_builder.setReturnStr("데이터없음");			
			}
		}
		
		catch (Exception e){
			logger.error("RequestCheckIn.request: ",e);
			returnmsg_builder.setReturnval(1);
			returnmsg_builder.setReturnStr("message:"+e.getMessage());
			//txManager.rollback(status);
		}
		finally{
			return returnmsg_builder.build();
		}
	}
}
