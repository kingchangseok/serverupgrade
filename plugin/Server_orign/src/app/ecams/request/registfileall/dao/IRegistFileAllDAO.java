package app.ecams.request.registfileall.dao;

import java.util.HashMap;
import java.util.List;

import app.ecams.request.checkin.model.CheckInfo;

public interface IRegistFileAllDAO {
	public int CheckIn_InsertCmr1010(String acpt);
	public List updateCmr1010(HashMap param);
	public List<CheckInfo> getSameRsrc(HashMap param);
	public int getCmd0010Cnt(HashMap param);
	public String getNameChange(HashMap param);	
}
