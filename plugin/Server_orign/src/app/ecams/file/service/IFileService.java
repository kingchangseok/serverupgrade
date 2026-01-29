package app.ecams.file.service;


import java.util.HashMap;

import app.core.proto.ProtoEcams.EcamsMessage;
import app.core.proto.ProtoEcams.ReturnMsg;

public interface IFileService {
	ReturnMsg getFileList (EcamsMessage ecamsmsg) ;
	ReturnMsg getFileList_Data(EcamsMessage ecamsmsg) ;
	ReturnMsg getNewFileList (EcamsMessage ecamsmsg);
	ReturnMsg getFileListCount (EcamsMessage ecamsmsg);
	ReturnMsg getFileData (EcamsMessage ecamsmsg) ;
	ReturnMsg getLastFileData (EcamsMessage ecamsmsg) ;
	HashMap<String, String> getFileStatus (String itemid, String qrycd) ;
	HashMap<String, String> getFileInfo(String itemid);
	int updateStatus (HashMap<String, String> param);
	int insertFileData(HashMap<String, String> param);
	ReturnMsg registFile (EcamsMessage ecamsmsg);
	ReturnMsg getFileTstData(EcamsMessage message);
	ReturnMsg setDeleteStatus(EcamsMessage ecamsmsg);
	String registCheckInFile (HashMap<String, String> newList);
	String registAllCheckInFile (HashMap<String, String> newList);
	ReturnMsg getMergeFileData (EcamsMessage ecamsmsg) ;
}
