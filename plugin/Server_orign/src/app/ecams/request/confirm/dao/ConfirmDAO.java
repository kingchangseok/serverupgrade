package app.ecams.request.confirm.dao;

import java.util.HashMap;
import java.util.List;


import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ConfirmDAO implements IConfirmDAO {
	@Autowired private SqlSession sqlSession;
	

	public int insertCmr9900_default(HashMap param) {
		// TODO Auto-generated method stub
		try{
			return sqlSession.insert("confirm.insertCmr9900_default",param);
		}catch (Exception exception) {
			return -1;
		}
	}

	public int insertCmr9900_select(HashMap param) {
		// TODO Auto-generated method stub
		try{
			return sqlSession.insert("confirm.insertCmr9900_select",param);
		}catch (Exception exception) {
			return -1;
		}
	}

	public int updateCmr9900(HashMap param) {
		// TODO Auto-generated method stub
		try{
			return sqlSession.update("confirm.updateCmr9900",param);
		}catch (Exception exception) {
			return -1;
		}
	}

	public void cmr9900_str(HashMap param) {
		// TODO Auto-generated method stub
		sqlSession.selectList("confirm.cmr9900_str",param);
	}
	
	public void insertCmr9900_Insert(String acptno) {
		// TODO Auto-generated method stub
		sqlSession.insert("confirm.insertCmr9900_Insert",acptno);
		
	}
	
	public List confselect(HashMap param){
		return sqlSession.selectList("confirm.confselect",param);
	}

}
