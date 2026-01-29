package app.ecams.lang.service;


import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


import app.core.exception.RollBackException;
import app.core.proto.ProtoEcams.CodeInfo;
import app.core.proto.ProtoEcams.CodeInfoList;
import app.core.proto.ProtoEcams.EcamsMessage;
import app.core.proto.ProtoEcams.LangInfoList;
import app.core.proto.ProtoEcams.RsrcInfoList;
import app.core.proto.ProtoEcams.JobInfoList;
import app.core.proto.ProtoEcams.ReturnMsg;
import app.core.proto.ProtoEcams.SysInfo;

import app.ecams.lang.dao.ILangDAO;
import app.ecams.lang.model.Lang;

@Service
public class LangService implements ILangService {
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Autowired private ILangDAO langDAO;
	
	@Transactional (readOnly = false, propagation = Propagation.REQUIRED, rollbackForClassName={"app.core.exception.RollBackException"})
	public ReturnMsg getLang (EcamsMessage ecamsmsg){
		
		ReturnMsg.Builder returnmsg_builder = ReturnMsg.newBuilder();
		EcamsMessage.Builder builder_msg = ecamsmsg.toBuilder();
		try{
			String cmSyscd = null;
			String cmRsrccd = null;
			String retMsg;
			int langsize=0;
			
			cmSyscd = ecamsmsg.getSysinfo().getSyscd();
			cmRsrccd = ecamsmsg.getSysinfo().getRsrcinfolist().getRsrcinfo(0).getRsrccd();
			
			HashMap params = new HashMap();
			params.put("CM_SYSCD", cmSyscd);
			params.put("CM_RSRCCD", cmRsrccd);
			
			List<Lang> langList = langDAO.getLangInfo(params);
			langsize = langList.size();
			if (langsize < 1){
				retMsg="등록된 언어가 없습니다.";
				throw new Exception(retMsg);
			}
			else{
				LangInfoList.Builder langinfolist_builder = LangInfoList.newBuilder();
				
				for(int i=0;i<langsize;i++){
					langinfolist_builder.addLanginfo(langList.get(i).toLangInfo());
				}
				builder_msg.setLanginfolist(langinfolist_builder.build());
			}
			
			
			returnmsg_builder.setReturnval(0);
			returnmsg_builder.setReturnStr("정상");
			returnmsg_builder.setEcamsmsg(builder_msg.build());
		}
		catch (RollBackException e){
			logger.error("RequestCheckIn.request: ",e);
			returnmsg_builder.setReturnval(1);
			returnmsg_builder.setReturnStr("message:"+e.getError());
		}
		catch (Exception e){
			logger.error("RequestCheckIn.request: ",e);
			returnmsg_builder.setReturnval(1);
			returnmsg_builder.setReturnStr("message:"+e.getMessage());
		}
		return returnmsg_builder.build();
	
	}
	
	public List<Lang> getLangInfo (HashMap param){
		String cmSyscd = null;
		String cmRsrccd = null;
		
		cmSyscd = (String) param.get("CM_SYSCD");
		cmRsrccd = (String) param.get("CM_RSRCCD");
		
		HashMap params = new HashMap();
		params.put("CM_SYSCD", cmSyscd);
		params.put("CM_RSRCCD", cmRsrccd);
		
		return langDAO.getLangInfo(params);
	}
	
	public String getRsrcCD(String syscd){
		return (String)langDAO.getRsrcCD(syscd);
	}
}
