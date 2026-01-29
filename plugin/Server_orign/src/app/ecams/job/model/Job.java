package app.ecams.job.model;

import app.core.proto.ProtoEcams.JobInfo;

public class Job {
	private String jobcd;
	private String jobName;
	private String deptcd;
	
	public String getJobcd() {
		return jobcd;
	}
	public void setJobcd(String jobcd) {
		this.jobcd = jobcd;
	}
	public String getJobName() {
		return jobName;
	}
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}
	
	public String getDeptcd() {
		return deptcd;
	}
	public void setDeptcd(String deptcd) {
		this.deptcd = deptcd;
	}
	public JobInfo toJobInfo(){
		JobInfo.Builder Jobinfo_builder = JobInfo.newBuilder();
		Jobinfo_builder.setJobcd(this.jobcd);
		Jobinfo_builder.setJobname(this.jobName);
		Jobinfo_builder.setDeptcd(this.deptcd);
		return Jobinfo_builder.build();		
	}

}
