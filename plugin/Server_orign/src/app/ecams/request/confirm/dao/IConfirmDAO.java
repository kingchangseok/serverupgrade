package app.ecams.request.confirm.dao;

import java.util.HashMap;
import java.util.List;

public interface IConfirmDAO {
	public int insertCmr9900_default(HashMap param);
	public int insertCmr9900_select(HashMap param);
	public int updateCmr9900(HashMap param);
	public void cmr9900_str(HashMap param);
	List confselect(HashMap param);
	public void insertCmr9900_Insert(String acptno);
}
