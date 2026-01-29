package app.ecams.request.dao;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class RequestDAO implements IRequestDAO {
	@Autowired private SqlSession sqlSession;
	
    
	public int dupleCheck(String acptno) {
		// TODO Auto-generated method stub
		try{
			return ((Integer)sqlSession.selectOne("request.request_duple_check",acptno)).intValue();
		}catch (Exception exception) {
			return -1;
		}
	}
	
	public int insertCmr1000(HashMap param){
		try{
			return sqlSession.insert("request.insertCmr1000",param);
		}catch (Exception exception) {
			return -1;
		}
	}
	
	public int updateCmr1010_confno(HashMap param){
		try{
			return sqlSession.update("request.updateCmr1010_confno",param);
		}catch (Exception exception) {
			return -1;
		}
	}
	public int insertCmr1010_default(HashMap param){
		try{
			return sqlSession.insert("request.insertCmr1010_default",param);
		}catch (Exception exception) {
			return -1;
		}
	}
	
	public int updateCmr1010_setcncl(HashMap param){
		try{
			return sqlSession.update("request.updateCmr1010_setcncl",param);
		}catch (Exception exception) {
			return -1;
		}
	}
	
	public List updateConfCmr1010(HashMap param) {
		// TODO Auto-generated method stub
		try{
			return sqlSession.selectList("request.updateConfCmr1010",param);
		}catch (Exception exception) {
			return null;
		}
	}

	public String getAcptno(HashMap param) {
		// TODO Auto-generated method stub
		try{
			return (String) sqlSession.selectOne("request.getAcptno", param);
		}catch (Exception exception) {
			return null;
		}
	}
	
}
