package app.ecams.request.lastcheckout.dao;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import app.ecams.request.lastcheckout.model.LastCheckOutInfo;

@Repository
public class LastCheckOutDAO implements ILastCheckOutDAO {
	@Autowired private SqlSession sqlSession;
	
	public List<LastCheckOutInfo> select_lastver(String itemid) {
		// TODO Auto-generated method stub
		return  sqlSession.selectList("lastcheckout.select_lastver",itemid);
	}
}
