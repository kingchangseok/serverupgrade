package app.ecams.resourcetype.dao;


import java.util.HashMap;
import java.util.List;



import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import app.ecams.resourcetype.model.ResourceType;


@Repository
public class ResourceTypeDAO implements IResourceTypeDAO {
	@Autowired private SqlSession sqlSession;
	

          
	public List<ResourceType> getRsrcInfo(String cmSyscd){
		//TODO Auto-generated method stub
		try{
			return sqlSession.selectList("resourcetype.getRsrcInfo", cmSyscd);
		}catch (Exception exception) {
			return null;
		}
	}
	
	public HashMap getRsrcInfo_detail(HashMap param) {
		// TODO Auto-generated method stub
		try{
			return (HashMap) sqlSession.selectOne("resourcetype.getRsrcInfo_detail", param);
		}catch (Exception exception) {
			return null;
		}
	}
}
