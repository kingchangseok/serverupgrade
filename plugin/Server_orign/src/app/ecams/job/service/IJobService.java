package app.ecams.job.service;



import java.util.HashMap;
import java.util.List;

import app.core.proto.ProtoEcams.EcamsMessage;
import app.core.proto.ProtoEcams.ReturnMsg;
import app.ecams.job.model.Job;

public interface IJobService {
	List<Job> getJobInfo (HashMap params) ;
	List<Job> getJobCheck (HashMap params) ;
	String getJobCd(HashMap params);
	ReturnMsg getMyJobCheck (EcamsMessage ecamsmsg);
}
