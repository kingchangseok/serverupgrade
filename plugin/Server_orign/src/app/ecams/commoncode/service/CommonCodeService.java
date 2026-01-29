package app.ecams.commoncode.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.core.proto.ProtoEcams.EcamsMessage;
import app.core.proto.ProtoEcams.FileData;
import app.core.proto.ProtoEcams.ReturnMsg;
import app.core.proto.ProtoEcams.SysInfoList;
import app.core.proto.ProtoEcams.CodeInfoList;
import app.core.proto.ProtoEcams.PrjInfo;
import app.core.proto.ProtoEcams.RsrcInfo;
import app.core.proto.ProtoEcams.PathInfo;
import app.core.proto.ProtoEcams.JobInfo;
import app.core.proto.ProtoEcams.PrjInfoList;
import app.core.proto.ProtoEcams.FileDataList;
import app.ecams.commoncode.dao.ICommonCodeDAO;
import app.ecams.commoncode.model.CommonCode;
import app.ecams.file.model.FileInfo;
import app.ecams.system.model.SystemInfo;
import app.ecams.user.dao.UserInfoDAO;

@Service
public class CommonCodeService implements ICommonCodeService {
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Autowired private ICommonCodeDAO commonCodeDAO;
	@Autowired private UserInfoDAO userInfoDAO;
	
	public CommonCode getCode(String macode, String micode) {
		HashMap param = new HashMap();
		param.put("macode", macode);
		param.put("micode", micode);
		return ((List<CommonCode>)commonCodeDAO.getCode(param)).get(0);
	}

	public ReturnMsg getCodes(EcamsMessage ecamsmsg) {
		int i;
		int listsize;
		int returnval=0;
		String returnStr="";
		
		ReturnMsg.Builder returnmsg_builder = ReturnMsg.newBuilder();
		
		EcamsMessage.Builder ecamsmsg_builder = ecamsmsg.toBuilder();
		
		try{
			HashMap param = new HashMap();
			param.put("macode", ecamsmsg.getCodeinfo().getMacode());
			
			List<CommonCode> tmpList = (List<CommonCode>) commonCodeDAO.getCode(param);
			listsize = tmpList.size();
			
			CodeInfoList.Builder codeinfolist_builder = CodeInfoList.newBuilder();
			
			for (i=0;i<listsize;i++){
				codeinfolist_builder.addCodeinfo(tmpList.get(i).toCodeInfo());
			}
			
			if (listsize > 0){
				returnval = 0;
				returnStr = "정상";			
			}
			else{
				returnval = 1;
				returnStr = "처리에러";			
			}
			
			ecamsmsg_builder.setCodeinfolist(codeinfolist_builder.build());
			returnmsg_builder.setEcamsmsg(ecamsmsg_builder.build());
		} catch(Exception e){
			returnval = 1;
			returnStr = e.getMessage().toString();				
		} finally{
			returnmsg_builder.setReturnStr(returnStr);
			returnmsg_builder.setReturnval(returnval);
		}
		return returnmsg_builder.build();
	}
	
}
