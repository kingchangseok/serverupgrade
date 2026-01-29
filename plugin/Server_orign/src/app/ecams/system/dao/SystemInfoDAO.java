package app.ecams.system.dao;


import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import app.ecams.system.model.SystemInfo;

@Repository
public class SystemInfoDAO implements ISystemInfoDAO {
	@Autowired private SqlSession sqlSession;
	

	public List<SystemInfo> getSysInfo(){

		try{
			return sqlSession.selectList("systeminfo.getSysInfo");
		}catch (Exception exception) {
			return null;
		}
	}

	public List<SystemInfo> getSysInfo_user(HashMap params){

		try{
			return sqlSession.selectList("systeminfo.getSysInfo_user", params);
		}catch (Exception exception) {
			return null;
		}
	}

	public HashMap getSysInfo_detail(String syscd){
		// TODO Auto-generated method stub
		try{
			return (HashMap) sqlSession.selectOne("systeminfo.getSysInfo_detail", syscd);
		
		}catch (Exception exception) {
			return null;
		}
	}


	public HashMap timecheck(String syscd){
		// TODO Auto-generated method stub
		return (HashMap) sqlSession.selectOne("systeminfo.timecheck", syscd);
	}
	
	public HashMap sysdirinfo(String pathcd){
		// TODO Auto-generated method stub
		return (HashMap) sqlSession.selectOne("systeminfo.sysdirinfo", pathcd);
	}
}
