package com.azsoft.ecams.core;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;


public class EcamsException extends CoreException {

	private static final long serialVersionUID = 1L;
	
	public EcamsException(IStatus status) {
		super(status);
		// TODO Auto-generated constructor stub
	}
	
	public EcamsException(String message, Throwable e){
		super(new Status(IStatus.ERROR, EcamsProviderPlugin.ID,0,message,e));
	}
	
	public EcamsException(String message){
		this(message,null);
	}
	
	protected EcamsException(CoreException e){
		super(asStatus(e));
	}
	
	private static Status asStatus(CoreException e){
		IStatus status = e.getStatus();
		return new Status(status.getSeverity(),status.getPlugin(),status.getCode(),status.getMessage(),e);
	}
	
	public static EcamsException asEcamsException(InvocationTargetException e){
		Throwable target = e.getTargetException();
		if (target instanceof EcamsException){
			return (EcamsException)target;
		}
		return new EcamsException(new Status(IStatus.ERROR,EcamsProviderPlugin.ID,-1,target.getMessage() != null ? target.getMessage() : "",target));
	}

}
