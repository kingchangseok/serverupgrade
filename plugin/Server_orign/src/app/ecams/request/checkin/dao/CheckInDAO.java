package app.ecams.request.checkin.dao;

import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import app.ecams.commoncode.model.CommonCode;
import app.ecams.request.checkin.model.CheckInfo;

@Repository
public class CheckInDAO implements ICheckInDAO {
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
	
	public String getSysCd(String itemid){
		try{
			return (String) sqlSession.selectOne("checkin.getSysCd",itemid);
		}catch (Exception exception) {
			return null;
		}
	}
	/*
	public List<HashMap<String,String>> selectFileInspectList(String acptNo) {
		try{
			return (List<HashMap<String,String>>)sqlSession.selectList("checkin.selectFileInspectList",acptNo);
		}catch (Exception exception) {
			return null;
		}
	}

	public int update_cmr1010_sparrowRst(HashMap<String,String> param) {
		return sqlSession.update("checkin.update_cmr1010_sparrowRst",param);
	}
	*/
}