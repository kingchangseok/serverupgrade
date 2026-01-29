package app.ecams.path.dao;

import java.util.HashMap;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class PathDAO implements IPathDAO {
	@Autowired private SqlSession sqlSession;
	
	
	public String getPath(HashMap param) {
		// TODO Auto-generated method stub
		return (String) sqlSession.selectOne("path.getPath",param);
	}

	public String getPathCD(HashMap param) {
		// TODO Auto-generated method stub
		try{
			return (String) sqlSession.selectOne("path.getPathCD",param);
		}catch (Exception exception) {
			return null;
		}
	}
	
	public String getBasePath(HashMap param) {
		// TODO Auto-generated method stub
		try{
			//return (String) sqlSession.selectOne("path.getBasePath",param);
			
			String basePath = (String) sqlSession.selectOne("path.getBasePath",param);
			
			String prjPath = getProjectPath(param);
			if (null != prjPath && !"".equals(prjPath)) {
				basePath = basePath+prjPath;
			}
			
			return basePath;
		}catch (Exception exception) {
			return null;
		}
	}
	
	public String getMaxDsnCD(String syscd){
		try{
			return (String) sqlSession.selectOne("path.getMaxDsnCD",syscd);
		}catch (Exception exception) {
			return null;
		}
	}

	public int insert_cmm0070(HashMap param) {
		// TODO Auto-generated method stub
		try{
			return sqlSession.insert("path.insert_cmm0070",param);
		}catch (Exception exception) {
			return -1;
		}
	}

	public int insert_cmm0072(HashMap param) {
		// TODO Auto-generated method stub
		try{
			return sqlSession.insert("path.insert_cmm0072",param);
		}catch (Exception exception) {
			return -1;
		}
	}

	public int insert_cmm0073(HashMap param) {
		// TODO Auto-generated method stub
		try{
			return sqlSession.insert("path.insert_cmm0073",param);
		}catch (Exception exception) {
			return -1;
		}
	}

	public String getHomePath(String syscd){
		try{
			return (String) sqlSession.selectOne("path.getHomePath",syscd);
		}catch (Exception exception) {
			return null;
		}
	}
	
	public String getTempPath(String cm_pathcd){
		try{
			return (String) sqlSession.selectOne("path.getTempPath",cm_pathcd);
		}catch (Exception exception) {
			return null;
		}
	}

	public String getProjectPath(HashMap param) {
		try{
			return (String) sqlSession.selectOne("path.getProjectPath",param);
		}catch (Exception exception) {
			return null;
		}
	}
}
