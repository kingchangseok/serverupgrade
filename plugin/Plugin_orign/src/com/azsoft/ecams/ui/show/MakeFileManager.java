package com.azsoft.ecams.ui.show;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

import com.azsoft.ecams.core.EcamsProviderPlugin;
import com.azsoft.ecams.properties.IProperty;
import com.azsoft.ecams.proto.ProtoEcams.EcamsMessage;
import com.azsoft.ecams.proto.ProtoEcams.FileData;
import com.azsoft.ecams.proto.ProtoEcams.ReturnMsg;
import com.azsoft.ecams.proto.ProtoEcams.UserInfo;
import com.azsoft.ecams.socket.EcamsClient;
import com.azsoft.ecams.ui.compare.Utilities;
import com.azsoft.ecams.util.file.EGzip;

public class MakeFileManager {
	
	public String execFileMake(IProject project, String itemid, String filename, String filepath, String version) {
		String localCharset = "UTF-8";
       
	   	try{
	   		String ip = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.IP, "", null);
	   		String port = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PORT, "", null);
	   		String id = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.ID, "", null);
	   		String passwd = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PASSWD, "", null);

			if((null == ip || "".equals(ip)) || (null == port || "".equals(port))
					|| (null == id || "".equals(id)) || (null == passwd || "".equals(passwd))){
				return "USER INFO IS NULL";
			}
			
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			
			String tmpPath = "";
			if ("LO".equals(version)) {
				tmpPath = filepath;
			}else{
				tmpPath = project.getLocation()+"/"+filepath+"/"+filename;
			}

			while(tmpPath.indexOf("/") >=0){
				tmpPath = tmpPath.replace("/","\\");
			}
			
			while(tmpPath.indexOf("\\\\") >=0){
				tmpPath = tmpPath.replace("\\\\", "\\");
			}

			while(tmpPath.indexOf("\\") >=0){
				tmpPath = tmpPath.replace("\\", "/");
			}	
			
			//System.out.println(tmpPath);
			
			IResource tmpResource = root.getFileForLocation(new Path(tmpPath));
			
			localCharset = Utilities.getCharset(tmpResource);
			tmpResource = null;
			
			String AbsolutePath = "";
			
			byte[] tmpByte = null;
			
			if ("LO".equals(version)) {
				try {
					File filez = new File(filepath);
					
					FileInputStream fs = new FileInputStream(filez);
					
					byte[] buffer = new byte[1024];
					int read = 0;
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					while( (read = fs.read(buffer)) > 0) {
						bos.write(buffer, 0, read);
					}
					
					tmpByte = bos.toString(localCharset).getBytes();
					
					filez = null;
					bos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
		   		EcamsMessage.Builder builder_msg = EcamsMessage.newBuilder();
		   		
		   		FileData.Builder lastfiledata_builder = FileData.newBuilder();
		   		lastfiledata_builder.setItemid(itemid);
		   		
		   		lastfiledata_builder.setBasever(version);
		   		lastfiledata_builder.setVergbn("R");
		   		lastfiledata_builder.setFilename(filename);
		   		builder_msg = EcamsMessage.newBuilder();
		   		builder_msg.setMsgtype("GETLASTFILE");
		   		builder_msg.setFiledata(lastfiledata_builder.build());
		   		
		   		UserInfo.Builder userinfo_builder = UserInfo.newBuilder();
		   		
		   		userinfo_builder.setId(id);
		   		userinfo_builder.setPasswd(passwd);
		   		builder_msg.setUserinfo(userinfo_builder.build());
		   		
		   		EcamsClient ecamsclient = new EcamsClient(ip,port);
		   		ReturnMsg returnMsg = ecamsclient.sendMsg(builder_msg.build());
		   		
		   		if(returnMsg.getReturnval() == 0){
		   			tmpByte = EGzip.getFileByte(returnMsg.getEcamsmsg().getFiledata().getFilebytes().toByteArray(),localCharset);
		   		}else{
		   			return "SERVER ERROR";
		   		}
			}
			
			if(null != tmpByte){
	   			File nfolder = new File("C:/history/tmp");
	   			
	   			if (!nfolder.exists()){
	   				nfolder.mkdirs();
	   			}
	   			nfolder = null;
	   			
	   			File filez = new File("C:/history/tmp/."+filename+"."+version);
	
	   			if (!filez.exists()){
	   				filez.createNewFile();
	   			}else{
	   				filez.delete();
	   				filez.createNewFile();
	   			}
	   			
	   			FileOutputStream fw = new FileOutputStream(filez);
	   			
	   			fw.write(tmpByte);
	   			tmpByte = null;
	   			
	   			if(fw.getChannel().size() <1){
		   			fw.close();
		   			fw = null;
	   				//throw new IOException("FILE SIZE ERROR");
		   			return "FILE SIZE ERROR";
	   			}
	   			fw.flush();
	   			fw.close();
	   			
	   			fw = null;
	   			
	   			AbsolutePath = filez.getAbsolutePath();
	   			filez = null;
			}
			
   		   	return "OK"+AbsolutePath;
   		   	
	   	}catch(Exception E){
	   		E.printStackTrace();
	   		return E.getMessage();
	   	}
	}
}
