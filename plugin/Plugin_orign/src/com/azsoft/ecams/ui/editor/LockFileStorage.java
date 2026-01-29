package com.azsoft.ecams.ui.editor;

import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.PlatformObject;


public class LockFileStorage extends PlatformObject implements IStorage {
	IFile lockFile;
	
	public LockFileStorage(IFile lockFile){
		this.lockFile = lockFile;
	}
	
	public InputStream getContents(){
		try {
			return lockFile.getContents();
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public IPath getFullPath() {
		// TODO Auto-generated method stub
		return lockFile.getFullPath();
	}

	public String getName() {
		// TODO Auto-generated method stub
		return lockFile.getName();
	}

	public boolean isReadOnly() {
		// TODO Auto-generated method stub
		return true;
	}

}
