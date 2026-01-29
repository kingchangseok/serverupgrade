package app.ecams.lang.dao;


import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import app.ecams.lang.model.Lang;


@Repository
public class LangDAO implements ILangDAO {
	@Autowired private SqlSession sqlSession;
	
	public List<Lang> getLangInfo(HashMap params){
		//TODO Auto-generated method stub
		try{
			return sqlSession.selectList("lang.getLangInfo", params);
		}catch (Exception exception) {
			return null;
		}
	}
	
	public String getRsrcCD(String syscd){
		//TODO Auto-generated method stub
		try{
			return (String) sqlSession.selectOne("lang.getRsrcCD", syscd);
		}catch (Exception exception) {
			return null;
		}
	}
}
