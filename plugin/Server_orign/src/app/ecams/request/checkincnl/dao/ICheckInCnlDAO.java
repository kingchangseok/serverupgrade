package app.ecams.request.checkincnl.dao;

import java.util.HashMap;
import java.util.List;

public interface ICheckInCnlDAO {
	public List updateCmr1010_cnclyn(HashMap param);
	public String selectCmr1010_acptno(HashMap param);
	public int updateCmr1010_confno(HashMap param);
	public String selectCnl_count(HashMap param);
}
