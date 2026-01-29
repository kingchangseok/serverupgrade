package app.ecams.srjob.dao;

import java.util.HashMap;
import java.util.List;
import app.ecams.srjob.model.SRJobInfo;

public interface ISRJobDAO {
	public List<SRJobInfo>  getSRInfo(HashMap param);
	public List<SRJobInfo> getSResource(HashMap param);
	public HashMap getCmc0100(String id);
	public HashMap getCmc0110(HashMap param);
	public int chkMySR(HashMap param);
	public List<SRJobInfo> getSResource2(HashMap param);
}
