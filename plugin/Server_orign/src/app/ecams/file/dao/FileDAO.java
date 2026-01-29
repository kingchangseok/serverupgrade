package app.ecams.file.dao;

import java.util.HashMap;
import java.util.List;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import app.ecams.file.model.FileInfo;

@Repository
public class FileDAO implements IFileDAO {
	private Logger logger = Logger.getLogger(this.getClass());
	@Autowired private SqlSession sqlSession;
	

	public List<FileInfo> getFileList(HashMap<String, String> param){
		//TODO Auto-generated method stub
		try{
			return (List<FileInfo>)sqlSession.selectList("fileinfo.getFileList", param);
		}catch (Exception exception) {
			return null;
		}
	}
	
	public List<FileInfo> getNewFileList(HashMap<String, String> param){
		//TODO Auto-generated method stub
		try{
			return (List<FileInfo>)sqlSession.selectList("fileinfo.getNewFileList", param);
		}catch (Exception exception) {
			return null;
		}
	}
	
	public int getFileListCount(HashMap<String, String> param) {
		// TODO Auto-generated method stub
		try{
			int ret = ((Integer)sqlSession.selectOne("fileinfo.getFileListCount", param)).intValue();
			
			return ret;
		}catch (Exception exception) {
			exception.printStackTrace();
			
			return -1;
		}
	}
	
	public List<FileInfo> getExecModList(String id){
		try{
			return (List<FileInfo>)sqlSession.selectList("fileinfo.getExecModList", id);
		}catch (Exception exception) {
			return null;
		}
	}
	
	public List<FileInfo> getAllFileList(String id){
		try{
			return (List<FileInfo>)sqlSession.selectList("fileinfo.getAllFileList", id);
		}catch (Exception exception) {
			return null;
		}
	}
	
	public String getLastFileVer(String itemid){
		try{
			return (String) sqlSession.selectOne("fileinfo.getLastFileVer",itemid);
		} catch (Exception exception) {
			exception.printStackTrace();
			//logger.error("exception");
			return null;
		}
	}
	
	public FileInfo getFileData(HashMap<String, String> param){
		//TODO Auto-generated method stub
		try{
			return (FileInfo)sqlSession.selectOne("fileinfo.getFileData", param);
		}catch (Exception exception) {
			return null;
		}
	}
	
	public FileInfo getFileTstData(String param){
		//TODO Auto-generated method stub
		try{
			return (FileInfo)sqlSession.selectOne("fileinfo.getFileTstData", param);
		}catch (Exception exception) {
			return null;
		}
	}
	 
	public FileInfo getLastFileData(HashMap<String, String> param){
		try{
			return (FileInfo)sqlSession.selectOne("fileinfo.getLastFileData", param);
		}catch (Exception exception) {
			return null;
		}
	}

	public FileInfo getPrevFileData(HashMap<String, String> param){
		try{
			return (FileInfo)sqlSession.selectOne("fileinfo.getPrevFileData", param);
		}catch (Exception exception) {
			return null;
		}
	}
	
	public List<FileInfo> syncData(){
		// TODO Auto-generated method stub
		return sqlSession.selectList("fileinfo.getSyncTest");
	}

	public HashMap<String, String> getStatus(HashMap<String, String> param){
		// TODO Auto-generated method stub
		try{
			return (HashMap<String, String>) sqlSession.selectOne("fileinfo.getStatus",param);
		}catch (Exception exception){
			return null;
		}
	}

	public HashMap<String, String> getFileInfo(String itemid) {
		// TODO Auto-generated method stub
		try{
			return (HashMap<String, String>) sqlSession.selectOne("fileinfo.getFileInfo",itemid);
		}catch (Exception exception) {
			return null;
		}
	}

	public int updateStatus(HashMap<String, String> param) {
		// TODO Auto-generated method stub
		try{
			return sqlSession.update("fileinfo.updateStatus",param);
		}catch (Exception exception) {
			logger.error(exception.getMessage());
			return -1;
		}
	}

	public int insertFileData(HashMap<String, String> param) {
		// TODO Auto-generated method stub
		try{
			return sqlSession.insert("fileinfo.insertFileData",param);
		}catch (Exception exception) {
			return -1;
		}
		
	}

	public String getFileInfo_noitemid(HashMap<String, String> param) {
		// TODO Auto-generated method stub
		try{
			return (String) sqlSession.selectOne("fileinfo.getFileInfo_noitemid",param);
		}catch (Exception exception) {
			return null;
		}
	}
	
	public String getFind_Itemid(HashMap<String, String> param) {
		// TODO Auto-generated method stub
		try{
			return (String) sqlSession.selectOne("fileinfo.getFind_Itemid",param);
		}catch (Exception exception) {
			return null;
		}
	}
	
	public int delete_cmr0025(HashMap param){
		try{
			int ret = sqlSession.delete("fileinfo.delete_cmr0025",param);
			
			return ret;
		} catch (Exception exception) {
			exception.printStackTrace();
			//logger.error("exception");
			
			return -1;
		}
	}
	
	public int delete_cmr0021(HashMap param){
		try{
			int ret = sqlSession.delete("fileinfo.delete_cmr0021",param);
			
			return ret;
		} catch (Exception exception) {
			exception.printStackTrace();
			//logger.error("exception");
			
			return -1;
		}
	}
	
	public int delete_cmr1010(String item){
		try{
			int ret = sqlSession.delete("fileinfo.delete_cmr1010",item);
			
			return ret;
		} catch (Exception exception) {
			exception.printStackTrace();
			//logger.error("exception");
			
			return -1;
		}
	}
	
	public int delete_cmr0022(String item){
		try{
			int ret = sqlSession.delete("fileinfo.delete_cmr0022",item);
			
			return ret;
		} catch (Exception exception) {
			exception.printStackTrace();
			//logger.error("exception");
			
			return -1;
		}
	}
	
	public int delete_cmr0020(String item){
		try{
			int ret = sqlSession.delete("fileinfo.delete_cmr0020",item);
			
			return ret;
		} catch (Exception exception) {
			exception.printStackTrace();
			//logger.error("exception");
			
			return -1;
		}
	}
	
	public String getNextItemID(){
		try{
			return (String) sqlSession.selectOne("fileinfo.getNextItemID");
		}catch (Exception exception) {
			return null;
		}
	}
	
	public int insertCmr0020(HashMap<String, String> param) {
		try{
			int ret = sqlSession.insert("fileinfo.insertCmr0020",param);
			
			return ret;
		} catch (Exception exception) {
			exception.printStackTrace();
			//logger.error("exception");
			
			return -1;
		}
	}
	
	public HashMap<String, String> getSvrSavefile(String itemid){
		// TODO Auto-generated method stub
		try{
			return (HashMap<String, String>) sqlSession.selectOne("fileinfo.getSvrSavefile",itemid);
		}catch (Exception exception) {
			return null;
		}
	}
	
	public String registCheckInFile (HashMap<String, String> newList){
		try{
			return (String)sqlSession.selectOne("fileinfo.registCheckInFile",newList);
		}catch (Exception exception) {
			return null;
		}
	}

	public String getFileInfo_MasterUnique(HashMap<String, String> param) {
		try{
			return (String)sqlSession.selectOne("fileinfo.getFileInfo_MasterUnique",param);
		}catch (Exception exception) {
			return null;
		}
	}

}
