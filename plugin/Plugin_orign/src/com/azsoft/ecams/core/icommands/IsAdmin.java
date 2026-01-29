package com.azsoft.ecams.core.icommands;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Platform;

import com.azsoft.ecams.core.EcamsProviderPlugin;
import com.azsoft.ecams.properties.IProperty;

public class IsAdmin extends AbstractHandler{
	private Logger logger = Logger.getLogger(this.getClass());

	@Override
	public Boolean execute(ExecutionEvent event) throws ExecutionException {
		// TODO Auto-generated method stub
		//Map<String, List<String>> map = event.getParameters();
		
		boolean adminFlg = false;
		
		String ip = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.IP, "", null);
		String port = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PORT, "", null);
		String id = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.ID, "", null);
		String passwd = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PASSWD, "", null);
		logger.error("IsAdmin    Start");
		if(ip != null && port != null && id != null && passwd != null){
			if("1".equals(java.lang.System.getProperty("isAdmin"))){
				adminFlg = true;
			}
		}
		logger.debug("IsAdmin adminFlg:"+adminFlg);
		System.out.println("IsAdmin adminFlg:"+adminFlg);
		logger.error("IsAdmin    End");
		return adminFlg;
	}

}
