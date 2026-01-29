package app.ecams.request.checkincnl.dao;

import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class CheckInCnlDAO implements ICheckInCnlDAO {
	@Autowired private SqlSession sqlSession;

	public List updateCmr1010_cnclyn(HashMap param) {
		// TODO Auto-generated method stub
		try{
			return sqlSession.selectList("checkincnl.updateCmr1010_cnclyn",param);
		}catch (Exception exception) {
			return null;
		}
	}
	
	public String selectCmr1010_acptno(HashMap param) {
		// TODO Auto-generated method stub
		try{
			return (String) sqlSession.selectOne("checkincnl.selectCmr1010_acptno",param);
		}catch (Exception exception) {
			return null;
		}
	}
	
	public int updateCmr1010_confno(HashMap param) {
		// TODO Auto-generated method stub
		try{
			return sqlSession.update("checkincnl.updateCmr1010_confno",param);
		}catch (Exception exception) {
			return -1;
		}
	}
	
	public String selectCnl_count(HashMap param){
		// TODO Auto-generated method stub
		try{
			return (String) sqlSession.selectOne("checkincnl.selectCnl_count",param);
		}catch (Exception exception) {
			return exception.getMessage()+"[ERROR]";
		}
	}
}
