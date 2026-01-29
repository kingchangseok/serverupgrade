package app.ecams.befjob.dao;

import java.util.List;

import app.ecams.befjob.model.BefJobInfo;

public interface IBefJobDAO {
	List<BefJobInfo> getBefJobList_detail();
}
