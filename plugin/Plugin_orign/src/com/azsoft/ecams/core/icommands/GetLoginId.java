package com.azsoft.ecams.core.icommands;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Platform;

import com.azsoft.ecams.core.EcamsProviderPlugin;
import com.azsoft.ecams.properties.IProperty;
import com.azsoft.ecams.proto.ProtoEcams.EcamsMessage;
import com.azsoft.ecams.proto.ProtoEcams.ReturnMsg;
import com.azsoft.ecams.proto.ProtoEcams.UserInfo;
import com.azsoft.ecams.socket.EcamsClient;

public class GetLoginId extends AbstractHandler{
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Override
	public String execute(ExecutionEvent event) throws ExecutionException {

		String ip = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.IP, "", null);
		String port = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PORT, "", null);
		String id = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.ID, "", null);
		String passwd = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PASSWD, "", null);
		logger.error("GetLofinId    Start");
		if (ip != null && port != null && id != null && passwd != null) {
			EcamsMessage.Builder builder_msg = EcamsMessage.newBuilder();
			builder_msg.setMsgtype("LOGIN_CHECK");
			UserInfo.Builder userInfo_builder = UserInfo.newBuilder();
			userInfo_builder.setId(id);
			userInfo_builder.setPasswd(passwd);
			builder_msg.setUserinfo(userInfo_builder.build());
			EcamsClient ecamsclient = new EcamsClient(ip, port);
			ReturnMsg returnMsg = ecamsclient.sendMsg(builder_msg.build());
			
			if (returnMsg.getReturnval() != 0){
				id = null;
			}
		} else {
			id = null;
		}
		logger.debug("GetLoginId id:"+id);
		logger.error("GetLofinId    End");
		return id;
	}
}
