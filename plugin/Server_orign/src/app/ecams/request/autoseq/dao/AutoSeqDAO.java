package app.ecams.request.autoseq.dao;

import java.util.HashMap;
import java.util.List;


import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import app.ecams.commoncode.model.CommonCode;

@Repository
public class AutoSeqDAO implements IAutoSeqDAO {
	@Autowired private SqlSession sqlSession;

    public HashMap getSeqNo(String qrycd) {
		try{
			return (HashMap) sqlSession.selectOne("autoseq.getSeqNo", qrycd);
		}catch (Exception exception) {
			return null;
		}
	}
    
    public String getSeqNoElse(HashMap params) {
		try{
			return (String) sqlSession.selectOne("autoseq.getSeqNoElse", params);
		}catch (Exception exception) {
			return null;
		}
	}
	
	public int updateSeqNo(HashMap hashval) {
		try{
			return sqlSession.update("autoseq.updateSeqNo", hashval);
		}catch (Exception exception) {
			return -1;
		}
	}

	public int insertSeqNo(String qrycd) {
		try{
			return sqlSession.insert("autoseq.insertSeqNo", qrycd);
		}catch (Exception exception) {
			return -1;
		}
	}
}
