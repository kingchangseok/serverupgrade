package app.ecams.request.autoseq.service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import app.ecams.request.autoseq.dao.IAutoSeqDAO;

@Service
public class AutoSeqService implements IAutoSeqService {
	@Autowired private IAutoSeqDAO autoSeqDAO;
	
	public String getAcptNo(String qrycd, int wkC) {
		String acptno = null;
		Integer SV_DBseq;
		int retval;
		
		// TODO Auto-generated method stub
		if (qrycd == null){
			return null;
		}
		
		if (qrycd.length() == 0){
			return null;
		}
		/*if (qrycd.equals("03")|| qrycd.equals("04") || qrycd.equals("01") || qrycd.equals("11") || qrycd.equals("12") || qrycd.equals("02")){
			HashMap params = new HashMap();
			params.put("qrycd", qrycd);
			params.put("wkC", wkC);
			
			acptno = autoSeqDAO.getSeqNoElse(params);
			
		}else{*/
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy",Locale.KOREA);
			String nowDt = formatter.format(new Date());
			formatter = null;		
			
			HashMap qryVal = autoSeqDAO.getSeqNo(qrycd);
			
			if (qryVal == null){
				retval = autoSeqDAO.insertSeqNo(qrycd);
	
				SV_DBseq = 0;
			}
			else{
				retval = 0;
				
				SV_DBseq = ((BigDecimal)qryVal.get("cr_no")).intValue();
				
	        	if (Integer.parseInt((String)qryVal.get("lstdt")) < Integer.parseInt(nowDt)){
	        		SV_DBseq = 0;
	        	}
	        	
	        	HashMap<String,String> parmVal = new HashMap<String,String>();
	        	
	        	parmVal.put("cr_no", Integer.toString(SV_DBseq+1));
	        	parmVal.put("qrycd", qrycd);
	        	
	        	retval= autoSeqDAO.updateSeqNo(parmVal);
	        	
			}
			
			acptno = nowDt + qrycd.substring(qrycd.length()-2, qrycd.length());
	        
			acptno = acptno+ String.format("%06d", SV_DBseq+1);
		//}
		return acptno;		
	}
}
