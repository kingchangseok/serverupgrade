package app.ecams.syncwith.dao;

import java.util.List;
import app.ecams.syncwith.model.SyncWithInfo;

public interface ISyncWithDAO {
	public List<SyncWithInfo> getPgmInfo(String id);
}
