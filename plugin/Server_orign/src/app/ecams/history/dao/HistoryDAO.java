package app.ecams.history.dao;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import app.ecams.history.model.HistoryInfo;

@Repository
public class HistoryDAO implements IHistoryDAO {
	@Autowired private SqlSession sqlSession;
	
	public List<HistoryInfo> getHistoryList_detail(String itemid){
		try{
			return sqlSession.selectList("historyinfo.getHistoryList_detail", itemid);
		}catch (Exception exception) {
			return null;
		}
	}
}
