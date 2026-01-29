package app.ecams.request.confirm.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.core.proto.ProtoEcams.ReturnMsg;
import app.ecams.request.confirm.dao.IConfirmDAO;

@Service
public class ConfirmService implements IConfirmService {
	private Logger logger = Logger.getLogger(this.getClass());
	@Autowired private IConfirmDAO confirmDAO;
	
	public boolean request_confirm(String acptno, String syscd, String qrycd,
			String userid, List<HashMap> confData) {
		int i,confDatalength,confcnt, errcnt;
		HashMap param;
		String conGbn="1,2,3,4,5,6,";
		errcnt=0;
//		if (confData != null){
//			confDatalength = confData.size();
//			confcnt = 0;
//			for (i=0;i<confDatalength;i++){
//				if (conGbn.indexOf((String)confData.get(i).get("CM_CONGBN")+",")>=0){
//					param = new HashMap();
//					param.put("CR_ACPTNO", acptno);
//					param.put("CR_LOCAT", ++confcnt);
//					param.put("CR_CONFNAME", (String)confData.get(i).get("CM_NAME"));
//					param.put("CR_TEAM", (String)((ArrayList<HashMap>)confData.get(i).get("arysv")).get(0).get("SvUser"));
//					
//					if (((String)confData.get(i).get("CM_GUBUN")).equals("C")){
//	        	    	param.put("CR_TEAMCD", "3");
//	        	    }
//	        	    else if (((String)confData.get(i).get("CM_GUBUN")).equals("R")){
//	        	    	param.put("CR_TEAMCD", "8");
//	        	    }
//	        	    else{
//	        	    	param.put("CR_TEAMCD", (String)confData.get(i).get("CM_GUBUN"));
//	        	    }
//	        	    
//	        	    param.put("CR_STATUS", "0");
//	        	    param.put("CR_CONGBN", (String)confData.get(i).get("CM_CONGBN"));
//	        	    param.put("CR_COMMON", (String)confData.get(i).get("CM_COMMON"));
//	        	    param.put("CR_BLANK", (String)confData.get(i).get("CM_BLANK"));
//	        	    param.put("CR_EMGER", (String)confData.get(i).get("CM_EMG"));
//	        	    param.put("CR_HOLI", (String)confData.get(i).get("CM_HOLI"));
//	        	    param.put("CR_SGNGBN", (String)confData.get(i).get("CM_DUTY"));
//	        	    param.put("CR_ORGSTEP", (String)confData.get(i).get("CM_ORGSTEP"));
//	        	    param.put("CR_BASEUSR", (String)confData.get(i).get("CM_BASEUSER"));
//	        	    param.put("CR_PRCSW", (String)confData.get(i).get("CM_PRCSW"));
//	        	    
//	        	    if (confirmDAO.insertCmr9900_default(param) <1){
//	        	    	errcnt++;
//	        	    }
//	        	    param = null;
//				}
//			}
//		}
//		else{
//			param = new HashMap();
//			param.put("CM_ACPTNO", acptno);
//			param.put("CM_SYSCD", syscd);
//			param.put("CM_QRYCD", qrycd);
//			param.put("CM_USERID", userid);
//			if (qrycd.equals("16")){
//				param.put("CM_GUBUN", "1");
//			}
//
//			if (confirmDAO.insertCmr9900_select(param) <1){
//    	    	errcnt++;
//    	    }
//			
//    	    param = null;			
//		}
		
		//logger.error("insertCmr9900_Insert start");
		
		confirmDAO.insertCmr9900_Insert(acptno);
		
		//logger.error("insertCmr9900_Insert end");
		
//		param = new HashMap();
//		param.put("CR_ACPTNO", acptno);
//		param.put("CR_TEAMCD", "4");
//		
//		if (confirmDAO.updateCmr9900(param) <1){
//	    	//errcnt++;
//	    }
//		
//	    param = null;
//		
//		param = new HashMap();
//		param.put("CR_ACPTNO", acptno);
//		param.put("CR_TEAMCD", "2");
//		param.put("CR_TEAM", userid);		
//		param.put("CR_BASEUSR", userid);
//		
//		if (confirmDAO.updateCmr9900(param) <1){
//	    	//errcnt++;
//	    }
//		
//	    param = null;
//	    
//		param = new HashMap();
//		param.put("CR_ACPTNO", acptno);
//		param.put("CR_LOCAT", "00");
//		param.put("CR_STATUS", "0");		
//		param.put("CR_CONFUSR", "9999");
//		
//	    if (confirmDAO.insertCmr9900_default(param) <1){
//	    	errcnt++;
//	    }
//	    param = null;
//	    
	    param = new HashMap();
		param.put("CR_ACPTNO", acptno);
		param.put("CR_USERID", "000000");
		param.put("CR_SGNMSG", "");		
		param.put("CR_SGNCD", "9");
		param.put("CR_QRYCD", qrycd);		
		param.put("CR_CNCLSW", "1");
		
		if(!cmr9900_str(param)){
			errcnt = 1;
		}
		param = null;
		
		return errcnt>0?false:true;
	}
	
	public boolean cmr9900_str(HashMap param){
		boolean returnFlag=true;
		try{
			//logger.error("cmr9900_str start");
			
			confirmDAO.cmr9900_str(param);
			
			//logger.error("cmr9900_str end");
		}
		catch (Exception e){
			returnFlag=false;
		}
		return returnFlag;
	}
	
	public String confselect(HashMap params) {
		int i;
		int listsize;
		String retMsg = "N";
		String RsrcCd = null;
		int cnt = 0;
		ArrayList<String> RsrccdList=null;
		
		
		try {
//			RsrccdList=(ArrayList)params.get("rsrcList");
//			
//			for(i=0;i<RsrccdList.size();i++){
//				if(RsrcCd==null){
//					RsrcCd=RsrccdList.get(i);
//				}else{
//					RsrcCd=RsrcCd+","+RsrccdList.get(i);
//				}
//			}
			
			HashMap param = new HashMap();
			param.put("CM_SYSCD",params.get("CM_SYSCD"));
			param.put("CM_REQCD",params.get("CM_REQCD"));
			param.put("CM_USERID",params.get("CM_USERID"));
			
			ArrayList<HashMap<String,String>> tmpList = (ArrayList)confirmDAO.confselect(param);
			
			listsize = tmpList.size();
			
			for(i=0;i<listsize;i++){
				++cnt;
				if (!tmpList.get(i).get("CM_GUBUN").equals("1") && !tmpList.get(i).get("CM_GUBUN").equals("2")) {
					retMsg = "N";
//            		if (tmpList.get(i).get("CM_GUBUN").equals("C")) retMsg = "N";
//            		else {
//	            		if (tmpList.get(i).get("CM_RSRCCD") != null) {
//	            			String strRsrc[] = RsrcCd.split(",");
//	            			
//	            			for (int j = 0;strRsrc.length > j; j++) {
//	            				if (tmpList.get(i).get("CM_RSRCCD").indexOf(strRsrc[j]) >= 0) {
//	            					retMsg = "Y";
//	            					break;
//	            				}
//	            			}            			
//	            		} else {
//	            			retMsg = "Y";
//	            			break;
//	            		}  
//            		}
            	}else{
            		retMsg = "Y";
            	}
			}
//			if (QryCd.equals("09")) {
//	        	retMsg = "N";
//	        }
	        if (cnt == 0) retMsg = "0";
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return retMsg;
	}

}
