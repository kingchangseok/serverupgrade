package app.ecams.syncwith.dao;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import app.ecams.syncwith.model.SyncWithInfo;

@Repository
public class SyncWithDAO implements ISyncWithDAO {
	@Autowired private SqlSession sqlSession; 
	
	public List<SyncWithInfo> getPgmInfo(String id){
		// TODO Auto-generated method stub
		try{
			//System.out.println("+++++++++++++"+id);
			return sqlSession.selectList("syncmapper.getPgmInfo",id);
		}catch (Exception exception) {
			exception.printStackTrace();
			System.out.println(exception.getMessage());
			return null;
		}
	}
}
