package app.ecams.anal.dao;

import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import app.ecams.anal.model.AnalInfo;
import app.ecams.file.model.FileInfo;

@Repository
public class AnalDAO implements IAnalDAO {
	//@Autowired private SqlSession sqlSession3;
	
	public List<AnalInfo> getAnalList_detail(HashMap param){
		try{
			//return sqlSession3.selectList("analinfo.getAnalList_detail",param);
		}catch (Exception exception) {
			System.out.println(exception.getMessage());
			//return null;
		}
		return null;
	}
	
	public List<AnalInfo> getAnalList_Method(String itemid){
		try{
			//return sqlSession3.selectList("analinfo.getAnalList_Method",itemid.toString());
		}catch (Exception exception) {
			System.out.println(exception.getMessage());
			//return null;
		}
		return null;
	}	
}
