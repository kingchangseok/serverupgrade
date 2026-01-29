package app.ecams.job.dao;


import java.util.HashMap;
import java.util.List;

import app.ecams.job.model.Job;

public interface IJobDAO {
	List<Job> getJobInfo(HashMap params);
	List<Job> getJobListInfo(String id);
	List<Job> getJobCheck(HashMap params);
	String getJobCd(HashMap params);
	String getMyJobCheck(HashMap params);
}
