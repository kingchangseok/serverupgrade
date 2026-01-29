package com.azsoft.ecams.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;


public class CommandExecuter {
	private static EcamsLogManager logger = new EcamsLogManager(ICommandService.class.getName());
	//private static Logger logger = Logger.getLogger(ICommandService.class);
	
	public static Object executeCommand(String commandId, Map<?,?> parameter) throws Exception {
//		logger.info(">>>>>>>> executeCommand commandId Start");
		//ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getService(ICommandService.class);
		ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		Command command = commandService.getCommand(commandId);
//		logger.info(">>>>>>>> executeCommand command:"+command + "  isDefined,isEnabled,isHandled" +command.isDefined() + command.isEnabled() + command.isHandled());

		if ( command.isDefined() && command.isEnabled() && command.isHandled() ){
			ExecutionEvent event = new ExecutionEvent(null, parameter, null, null);
//			logger.info(">>>>>>>> executeCommand command:"+event);
			return command.executeWithChecks(event);
		}
		
//		logger.info(">>>>>>>> executeCommand commandId End");
		
		return null;
	}
	
	/**
	 * 파라미터가 없는 명령 호출
	 * 
	 * @param commandId
	 * @return
	 */
	public static Object executeCommand(String commandId) throws Exception {
//		logger.info(">>>>>>>> executeCommand commandId:" + commandId);
		return executeCommand(commandId, new HashMap<String, String>());
	}

}
