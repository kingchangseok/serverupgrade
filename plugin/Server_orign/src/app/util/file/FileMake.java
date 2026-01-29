package app.util.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.core.exception.RollBackException;
import app.core.proto.ProtoEcams.EcamsMessage;
import app.core.proto.ProtoEcams.FileData;
import app.core.proto.ProtoEcams.ReturnMsg;
import app.ecams.system.service.ISystemService;

@Service
public class FileMake {
	private Logger logger = Logger.getLogger(this.getClass());
	@Autowired private ISystemService systemService;
    public ReturnMsg fileMake(EcamsMessage ecamsmsg) throws IOException{
    	ReturnMsg.Builder returnmsg_builder = ReturnMsg.newBuilder();
		try{			
			String tmpPath = ecamsmsg.getFiledata().getPathinfo().getRelativitePath();
			HashMap sysinfoMap = systemService.sysdirinfo(tmpPath);
			
			if(sysinfoMap==null){
				throw new IOException("디렉토리정보 오류입니다. [Temp]");
			}
			File nfolder = new File((String)sysinfoMap.get("cm_path"));
			//File nfolder = new File("C:\\eCAMS_Log");			
			if (!nfolder.exists()){
				nfolder.mkdirs();
			}
			File filez = new File(nfolder + "/" + ecamsmsg.getFiledata().getItemid());
			
			if (!filez.exists()){
				filez.createNewFile();
			}
			else{
				//filez.delete();
				filez.createNewFile();
			}
			
			FileOutputStream fw = new FileOutputStream(filez);
			fw.write(ecamsmsg.getFiledata().getFilebytes().toByteArray());
			if(fw.getChannel().size() <1){
				throw new IOException("FILE SIZE ERROR");
			}
			
			
			fw.flush();
			fw.close();
			returnmsg_builder.setReturnStr("정상");
			returnmsg_builder.setReturnval(0);
	    }
		catch (IOException e) {		
			logger.error("file Write Error=",e);
			returnmsg_builder.setReturnStr("오류");
			returnmsg_builder.setReturnval(1);
			// TODO Auto-generated catch block
		}
		
		return returnmsg_builder.build();
    }
    
    

}
