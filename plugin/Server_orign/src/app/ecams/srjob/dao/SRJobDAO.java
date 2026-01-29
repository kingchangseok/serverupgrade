package app.ecams.srjob.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import app.ecams.srjob.model.SRJobInfo;

@Repository
public class SRJobDAO implements ISRJobDAO {
	@Autowired private SqlSession sqlSession; 
	
	public List<SRJobInfo> getSRInfo(HashMap param) {
		// TODO Auto-generated method stub
		try{
			return sqlSession.selectList("srmapper.getSRInfo",param);
		}catch (Exception exception) {
			exception.printStackTrace();
			System.out.println(exception.getMessage());
			return null;
		}
	}
	
	public List<SRJobInfo> getSResource(HashMap param){
		// TODO Auto-generated method stub
		try{
			return sqlSession.selectList("srmapper.getSResource",param);
		}catch (Exception exception) {
			return null;
		}
	}

	public HashMap getCmc0100(String id){
		try{
			return (HashMap) sqlSession.selectOne("srmapper.getCmc0100",id);
		}catch (Exception exception) {
			return null;
		}
	}
	
	public HashMap getCmc0110(HashMap param){
		try{
			return (HashMap) sqlSession.selectOne("srmapper.getCmc0110",param);
		}catch (Exception exception) {
			return null;
		}
	}
	
	public int chkMySR(HashMap param){
		try{
			return (Integer)sqlSession.selectOne("srmapper.chkMySR",param);
		}catch (Exception exception) {
			return 0;
		}
	}
	
	public List<SRJobInfo> getSResource2(HashMap param){ // 20210106 SR사용안함 옵션일때도 리소스목록 새로고침 되도록
		// TODO Auto-generated method stub
		try{
			return sqlSession.selectList("srmapper.getSResource2",param);
		}catch (Exception exception) {
			return null;
		}
	}
}
