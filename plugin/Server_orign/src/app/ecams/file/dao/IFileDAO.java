package app.ecams.file.dao;


import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.session.RowBounds;

import app.ecams.file.model.FileInfo;

public interface IFileDAO {
	//List<FileInfo> getFileList(HashMap<String, String> param,RowBounds rowBounds);
	List<FileInfo> getFileList(HashMap<String, String> param);
	List<FileInfo> getNewFileList(HashMap<String, String> param);
	int getFileListCount(HashMap<String, String> param);
	List<FileInfo> getExecModList(String id);
	List<FileInfo> getAllFileList(String id);
	FileInfo getFileData(HashMap<String, String> param) ;
	FileInfo getLastFileData(HashMap<String, String> param) ;
	List<FileInfo> syncData() ;
	HashMap<String, String> getStatus(HashMap<String, String> param) ;
	HashMap<String, String> getFileInfo(String itemid);
	String getFileInfo_noitemid(HashMap<String, String> param);
	String getFind_Itemid(HashMap<String, String> param);
	int delete_cmr0025(HashMap param);
	int delete_cmr0021(HashMap param);
	int delete_cmr1010(String item);
	int delete_cmr0022(String item);
	int delete_cmr0020(String item);
	int updateStatus(HashMap<String, String> param);
	int insertFileData(HashMap<String, String> param);
	int insertCmr0020(HashMap<String, String> param);
	String getNextItemID();
	FileInfo getFileTstData(String itemid);
	FileInfo getPrevFileData(HashMap<String, String> param);
	String registCheckInFile (HashMap<String, String> newList);
	String getFileInfo_MasterUnique(HashMap<String, String> param);
	String getLastFileVer(String itemid);
}
