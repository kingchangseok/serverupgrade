package app.ecams.commoncode.dao;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;


import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import app.ecams.commoncode.model.CommonCode;
import app.ecams.file.model.FileInfo;

@Repository
public class CommonCodeDAO implements ICommonCodeDAO {
	private Logger logger = Logger.getLogger(this.getClass());
	
    @Autowired private SqlSession sqlSession;
    //@Autowired private SqlSession sqlSession2;
	

	public List<CommonCode> getCode(HashMap param) {
		// TODO Auto-generated method stub
		try{
			return (List<CommonCode>) sqlSession.selectList("commoncode.getCode", param);
		}catch (Exception exception) {
			return null;
		}
	}
	
}
