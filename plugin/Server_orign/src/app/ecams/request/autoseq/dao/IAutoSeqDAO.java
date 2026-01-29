package app.ecams.request.autoseq.dao;

import java.util.HashMap;

public interface IAutoSeqDAO {
	public HashMap getSeqNo(String qrycd);
	public String getSeqNoElse(HashMap params);
	public int updateSeqNo(HashMap hashval);
	public int insertSeqNo(String qrycd);
}
