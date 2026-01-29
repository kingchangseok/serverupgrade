package app.ecams.job.dao;


import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import app.ecams.job.model.Job;


@Repository
public class JobDAO implements IJobDAO {
	@Autowired private SqlSession sqlSession;
	
	public List<Job> getJobInfo(HashMap params){
		//TODO Auto-generated method stub
		try{
			return sqlSession.selectList("job.getJobInfo", params);
		}catch (Exception exception) {
			return null;
		}
	}

	public List<Job> getJobListInfo(String id){
		//TODO Auto-generated method stub
		try{
			return sqlSession.selectList("job.getJobListInfo", id);
		}catch (Exception exception) {
			return null;
		}
	}
	
	public List<Job> getJobCheck(HashMap params){
		//TODO Auto-generated method stub
		try{
			return sqlSession.selectList("job.getJobCheck", params);
		}catch (Exception exception) {
			return null;
		}
	}

	public String getJobCd(HashMap params){
		try{
			return (String) sqlSession.selectOne("job.getJobCd",params);
		}catch (Exception exception) {
			return null;
		}
	}
	
	public String getMyJobCheck(HashMap params){
		try{
			return (String) sqlSession.selectOne("job.getMyJobCheck",params);
		}catch (Exception exception) {
			return null;
		}
	}
}
