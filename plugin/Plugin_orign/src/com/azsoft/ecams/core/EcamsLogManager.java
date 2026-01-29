package com.azsoft.ecams.core;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class EcamsLogManager {
	private ILog logger;
	private String className;
	
	public EcamsLogManager(String className){
		logger = EcamsProviderPlugin.getDefault().getLog();
		this.className = className;
	}
	public void info(String msg){
		logger.log(new Status(IStatus.INFO, className, msg));
	}
	public void info(String msg, Throwable e){
		logger.log(new Status(IStatus.INFO, className, msg, e));
	}
	public void info(Throwable e){
		logger.log(new Status(IStatus.INFO, className, e.getMessage(),e));
	}
	
	public void error(String msg){
		logger.log(new Status(IStatus.ERROR, className, msg));
	}
	public void error(String msg, Throwable e){
		logger.log(new Status(IStatus.ERROR, className, msg, e));
	}
	public void error(Throwable e){
		logger.log(new Status(IStatus.ERROR, className, e.getMessage(),e));
	}
	
	public void warn(String msg){
		logger.log(new Status(IStatus.WARNING, className, msg));
	}
	public void warn(String msg, Throwable e){
		logger.log(new Status(IStatus.WARNING, className, msg, e));
	}
	public void warn(Throwable e){
		logger.log(new Status(IStatus.WARNING, className, e.getMessage(),e));
	}
	
}
