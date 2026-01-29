package app.ecams.request.checkinreal.dao;

import java.util.HashMap;
import java.util.List;

public interface ICheckInRealDAO {
	public int CheckInReal_InsertCmr1010(String acpt);
	public HashMap CheckInReal_SelectDBIO(String acpt);
	public List CheckInReal_item_List_check(String acpt);
	public int CheckInReal_item_delete(HashMap param);
	public List CheckInReal_list_check(String acpt);
	public List updateCmr1010(HashMap param);
	public int insertCmr1030(HashMap param);
}
