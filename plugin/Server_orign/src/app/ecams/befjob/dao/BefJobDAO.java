package app.ecams.befjob.dao;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import app.ecams.befjob.model.BefJobInfo;

@Repository
public class BefJobDAO implements IBefJobDAO {
	@Autowired private SqlSession sqlSession;
	
	public List<BefJobInfo> getBefJobList_detail(){
		try{
			return sqlSession.selectList("befjobinfo.getBefJobList_detail");
		}catch (Exception exception) {
			System.out.println(exception.getMessage());
			return null;
		}
	}
}
