package app.ecams.request.registfileall.dao;

import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import app.ecams.commoncode.model.CommonCode;
import app.ecams.request.checkin.model.CheckInfo;

@Repository
public class RegistFileAllDAO implements IRegistFileAllDAO {
	@Autowired private SqlSession sqlSession;
	
	

	public int CheckIn_InsertCmr1010(String acpt) {
		// TODO Auto-generated method stub
		return sqlSession.insert("checkin.CheckIn_InsertCmr1010",acpt);
	}
	
	public List updateCmr1010(HashMap param) {
		// TODO Auto-generated method stub
		try{
			return sqlSession.selectList("checkin.updateCmr1010",param);
		}catch (Exception exception) {
			return null;
		}
	}
	
	public List<CheckInfo> getSameRsrc(HashMap param){
		try{
			return (List<CheckInfo>)sqlSession.selectList("checkin.getSameRsrc",param);
		}catch (Exception exception) {
			return null;
		}
	}
	
	public int getCmd0010Cnt(HashMap param){
		try{
			return (Integer) sqlSession.selectOne("checkin.getCmd0010Cnt",param);
		}catch (Exception exception) {
			return -1;
		}
	}
	
	public String getNameChange(HashMap param){
		try{
			return (String) sqlSession.selectOne("checkin.getNameChange",param);
		}catch (Exception exception) {
			return null;
		}
	}
}