package app.ecams.request.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface IRequestDAO {
	public int dupleCheck(String acptno);
	public int insertCmr1000(HashMap param);
	public int insertCmr1010_default(HashMap param);
	public int updateCmr1010_confno(HashMap param);
	public int updateCmr1010_setcncl(HashMap param);
	public String getAcptno(HashMap param);
	public List updateConfCmr1010(HashMap param);
}
