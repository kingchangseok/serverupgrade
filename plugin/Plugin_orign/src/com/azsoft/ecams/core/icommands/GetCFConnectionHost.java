package com.azsoft.ecams.core.icommands;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Platform;

import com.azsoft.ecams.core.EcamsProviderPlugin;
import com.azsoft.ecams.properties.IProperty;

public class GetCFConnectionHost extends AbstractHandler{

	private Logger logger = Logger.getLogger(this.getClass());
	
	@Override
	public String execute(ExecutionEvent event) throws ExecutionException {
		// TODO Auto-generated method stub
		//Map<String, List<String>> map = event.getParameters();
		logger.error("GetCFConnectionHost    Start");
		String ip = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.IP, "", null);
		/*
		if(null == ip || "".equals(ip)){
			MessageBox messageBox = new MessageBox(new Shell());
			messageBox.setMessage("IP is null"+"\n"+"[Window>Pregerence>eCAMS Plugin] Please Insert eCAMS IP");
			messageBox.setText("Commands ERROR");
			messageBox.open();
		}
		*/
		logger.debug("GetCFConnectionHost ip:"+ip);
		
		logger.error("GetCFConnectionHost    End");
		return ip;
	}

}
