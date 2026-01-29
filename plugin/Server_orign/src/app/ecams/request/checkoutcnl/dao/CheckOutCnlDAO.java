package app.ecams.request.checkoutcnl.dao;

import java.util.HashMap;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;


@Repository
public class CheckOutCnlDAO implements ICheckOutCnlDAO {
	
	@Autowired private SqlSession sqlSession;

	public int updateCmr1010_cnclyn(HashMap param) {
		// TODO Auto-generated method stub
		try{
			return sqlSession.update("checkoutcnl.updateCmr1010_cnclyn",param);
		}catch (Exception exception) {
			return -1;
		}
	}
	
	public String selectCmr1010_acptno(HashMap param) {
		// TODO Auto-generated method stub
		try{
			return (String) sqlSession.selectOne("checkoutcnl.selectCmr1010_acptno",param);
		}catch (Exception exception) {
			return null;
		}
	}
	
	public int updateCmr1010_confno(String acptno) {
		// TODO Auto-generated method stub
		try{
			return sqlSession.update("checkoutcnl.updateCmr1010_confno",acptno);
		}catch (Exception exception) {
			return -1;
		}
	}
	
	public String selectCnl_count(HashMap param){
		// TODO Auto-generated method stub
		try{
			return (String) sqlSession.selectOne("checkoutcnl.selectCnl_count",param);
		}catch (Exception exception) {
			return exception.getMessage()+"[ERROR]selectCnl_count";
		}
	}
}
