package app.ecams.request.checkoutcnl.dao;

import java.util.HashMap;


public interface ICheckOutCnlDAO {
	
	public int updateCmr1010_cnclyn(HashMap param);
	public String selectCmr1010_acptno(HashMap param);
	public int updateCmr1010_confno(String acptno);
	public String selectCnl_count(HashMap param);
}
