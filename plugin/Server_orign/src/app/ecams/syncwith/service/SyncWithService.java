package app.ecams.syncwith.service;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.core.proto.ProtoEcams.EcamsMessage;
import app.core.proto.ProtoEcams.FileData;
import app.core.proto.ProtoEcams.FileDataList;
import app.core.proto.ProtoEcams.PathInfo;
import app.core.proto.ProtoEcams.ReturnMsg;
import app.ecams.syncwith.dao.ISyncWithDAO;
import app.ecams.syncwith.model.SyncWithInfo;

@Service
public class SyncWithService implements ISyncWithService {
private Logger logger = Logger.getLogger(this.getClass());
	
	@Autowired private ISyncWithDAO serviceSyncWithDAO;

	public ReturnMsg diffSvr(EcamsMessage ecamsmsg) {
		ReturnMsg.Builder returnmsg_builder = ReturnMsg.newBuilder();
		EcamsMessage.Builder ecamsmsg_builder = ecamsmsg.toBuilder();
		
		int returnval = 0;
		String returnStr = "";
		
		try{
			List<SyncWithInfo> retHash = serviceSyncWithDAO.getPgmInfo(ecamsmsg.getSysinfo().getSyscd());
			
			if(retHash != null && retHash.size()>0){
				/*
				 * gbn
				 * C (로컬수정사항있음 I)
				 * L (로컬신규 I)
				 *
				 * V (수정사항없이 버전변경됨 O)
				 * S (서버신규 O)
				 * D (서버폐기 O)
				 * X (체크아웃없이 수정함 O)
				 */
				
				
				String gbn = "";
				int setCnt = 0;
				boolean newFlg = false;
				
				FileDataList.Builder fileDataList = FileDataList.newBuilder();
				for(int x=0; x<retHash.size(); x++){
					gbn = "";
					newFlg = false;
					
					for(int y=0; y<ecamsmsg.getFiledatalist().getFiledatasCount(); y++){
						/*
						if ("L".equals(ecamsmsg.getFiledatalist().getFiledatas(y).getStatus())) {
							fileDataList.addFiledatas(setCnt++, ecamsmsg.getFiledatalist().getFiledatas(y).toBuilder().build());
							gbn = "L";
							continue;
						}
						*/
						if(retHash.get(x).getRsrcname().equals(ecamsmsg.getFiledatalist().getFiledatas(y).getFilename())
								&& retHash.get(x).getDirpath().equals(ecamsmsg.getFiledatalist().getFiledatas(y).getPathinfo().getRelativitePath())){
							
							if(retHash.get(x).getStatus().equals("9")){ //서버폐기
								if(!"".equals(ecamsmsg.getFiledatalist().getFiledatas(y).getMd5Sum())){
									gbn = "D";
								}
							}else{
								if(!"".equals(ecamsmsg.getFiledatalist().getFiledatas(y).getMd5Sum())){
									if("9".equals(ecamsmsg.getFiledatalist().getFiledatas(y).getStatus())){
										//체크아웃 하지않고 수정한 경우
										gbn = "X";
									}else if("1".equals(ecamsmsg.getFiledatalist().getFiledatas(y).getStatus())){
										//로컬수정됨
										gbn = "C";
									}else{
										if(!retHash.get(x).getMd5sum().equals(ecamsmsg.getFiledatalist().getFiledatas(y).getMd5Sum())){
											//로컬수정내용없이 버전이 변경되어 md5sum이 일치하지않음
											gbn = "V";
										}
									}
								}
							}
							if(!"".equals(gbn)){
								FileData.Builder fileData = FileData.newBuilder(); 
								fileData.setFilename(ecamsmsg.getFiledatalist().getFiledatas(y).getFilename());
								fileData.setItemid(retHash.get(x).getItemid());
								fileData.setVersion(Integer.parseInt(retHash.get(x).getVersion()));
								//fileData.setMd5Sum(retHash.get(x).getMd5sum());
								fileData.setStatus(gbn);
								PathInfo.Builder pathInfo = PathInfo.newBuilder();
								pathInfo.setRelativitePath(ecamsmsg.getFiledatalist().getFiledatas(y).getPathinfo().getRelativitePath());
								fileData.setPathinfo(pathInfo.build());
								fileDataList.addFiledatas(setCnt++, fileData.build());
								fileData = null;
								pathInfo = null;
							}
							newFlg = true;
							break;
						}
					}
					if(!newFlg && "".equals(gbn) && !retHash.get(x).getStatus().equals("9")){
						//서버 신규
						gbn = "S";
						FileData.Builder fileData = FileData.newBuilder(); 
						fileData.setFilename(retHash.get(x).getRsrcname());
						fileData.setItemid(retHash.get(x).getItemid());
						fileData.setVersion(Integer.parseInt(retHash.get(x).getVersion()));
						//fileData.setMd5Sum(retHash.get(x).getMd5sum());
						fileData.setStatus(gbn);
						PathInfo.Builder pathInfo = PathInfo.newBuilder();
						if(null == retHash.get(x).getDirpath() || "".equals(retHash.get(x).getDirpath())){
							pathInfo.setRelativitePath("/");
						}else{
							pathInfo.setRelativitePath(retHash.get(x).getDirpath());
						}
						fileData.setPathinfo(pathInfo.build());
						fileDataList.addFiledatas(setCnt++, fileData.build());
						fileData = null;
						pathInfo = null;
					}
				}
				for(int y=0; y<ecamsmsg.getFiledatalist().getFiledatasCount(); y++){
					//System.out.println(ecamsmsg.getFiledatalist().getFiledatas(y).getFilename()+","+ecamsmsg.getFiledatalist().getFiledatas(y).hasMd5Sum()+","+ecamsmsg.getFiledatalist().getFiledatas(y).getMd5Sum());
					//if(!ecamsmsg.getFiledatalist().getFiledatas(y).hasMd5Sum()){
					if("".equals(ecamsmsg.getFiledatalist().getFiledatas(y).getMd5Sum())){
						FileData.Builder fileData = ecamsmsg.getFiledatalist().getFiledatas(y).toBuilder(); 
						fileDataList.addFiledatas(setCnt++, fileData.build());
						fileData = null;
					}
				}
				if(fileDataList.getFiledatasCount()>0){
					returnval = 0; 
					returnStr = "정상";
					ecamsmsg_builder.setFiledatalist(fileDataList.build());
					returnmsg_builder.setEcamsmsg(ecamsmsg_builder.build());
				}else{
					returnval = 1; 
					returnStr = "모두일치";
				}
				fileDataList = null;
			}else{
				//returnval = 1; 
				//returnStr = "서버대상없음";

				FileDataList.Builder fileDataList = FileDataList.newBuilder();
				for(int y=0; y<ecamsmsg.getFiledatalist().getFiledatasCount(); y++){
					FileData.Builder fileData = ecamsmsg.getFiledatalist().getFiledatas(y).toBuilder(); 
	    			fileData.setMd5Sum("");
	    			fileData.setItemid("");
	    			fileData.setVersion(0);
	    			fileData.setStatus("L");
					fileDataList.addFiledatas(fileData.build());
					fileData = null;
				}
				
				if(fileDataList.getFiledatasCount()>0){
					returnval = 0; 
					returnStr = "정상";
					ecamsmsg_builder.setFiledatalist(fileDataList.build());
					returnmsg_builder.setEcamsmsg(ecamsmsg_builder.build());
				}else{
					returnval = 1; 
					returnStr = "서버대상없음";
				}
				fileDataList = null;
			}
			
			returnmsg_builder.setReturnval(returnval);
			returnmsg_builder.setReturnStr(returnStr);
				
		}catch(Exception e){
			logger.error("ServiceRequest Service: ",e);
		    returnmsg_builder.setReturnval(1);
		    returnmsg_builder.setReturnStr("message:"+e.getMessage());
		}
		return returnmsg_builder.build();
	}
}
