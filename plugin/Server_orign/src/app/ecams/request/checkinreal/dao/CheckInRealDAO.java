package app.ecams.request.checkinreal.dao;

import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import app.ecams.commoncode.model.CommonCode;

@Repository
public class CheckInRealDAO implements ICheckInRealDAO {
	@Autowired private SqlSession sqlSession;
	

	public int CheckInReal_InsertCmr1010(String acpt) {
		// TODO Auto-generated method stub
		try{
			return sqlSession.insert("checkinreal.CheckInReal_InsertCmr1010",acpt);
		}catch (Exception exception) {
			return -1;
		}
	}
	
	public HashMap CheckInReal_SelectDBIO(String acpt) {
		// TODO Auto-generated method stub
		return (HashMap) sqlSession.selectList("checkinreal.CheckInReal_SelectDBIO",acpt);
	}
	
	public List CheckInReal_item_List_check(String acpt) {
		// TODO Auto-generated method stub
		try{
			return sqlSession.selectList("checkinreal.CheckInReal_item_List_check",acpt);
		}catch (Exception exception) {
			return null;
		}
	}
	
	public int CheckInReal_item_delete(HashMap param) {
		// TODO Auto-generated method stub
		try{
			return sqlSession.delete("checkinreal.CheckInReal_item_delete",param);
		}catch (Exception exception) {
			return -1;
		}
	}
	
	public List CheckInReal_list_check(String acpt) {
		// TODO Auto-generated method stub
		try{
			return sqlSession.selectList("checkinreal.CheckInReal_list_check",acpt);
		}catch (Exception exception) {
			return null;
		}
	}
	
	public List updateCmr1010(HashMap param) {
		// TODO Auto-generated method stub
		try{
			return sqlSession.selectList("checkinreal.updateCmr1010",param);
		}catch (Exception exception) {
			return null;
		}
	}
	
	public int insertCmr1030(HashMap param) {
		// TODO Auto-generated method stub
		try{
			return sqlSession.insert("checkinreal.insertCmr1030",param);
		}catch (Exception exception) {
			return -1;
		}
	}
}