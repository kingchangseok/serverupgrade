package com.azsoft.ecams.ui.editor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.model.IWorkbenchAdapter;


public class LockFileEditorInput implements IWorkbenchAdapter,
		IStorageEditorInput, IPathEditorInput {
	private IFile lockFile;
	protected IStorage lockStorage;
	
	
	public LockFileEditorInput(IFile lockFile){
		this.lockFile = lockFile;
		lockStorage = new LockFileStorage(lockFile);
	}
	
	public boolean exists() {
		// TODO Auto-generated method stub
		return true;
	}

	public ImageDescriptor getImageDescriptor() {
		IWorkbenchAdapter fileAdapter = (IWorkbenchAdapter)lockFile.getAdapter(IWorkbenchAdapter.class);
		return fileAdapter == null ? null : fileAdapter.getImageDescriptor(lockFile);
	}

	public String getName() {
		// TODO Auto-generated method stub
		return lockFile.getName();
	}

	public IPersistableElement getPersistable() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getToolTipText() {
		// TODO Auto-generated method stub
		return getName();
	}

	public Object getAdapter(Class adapter) {
		// TODO Auto-generated method stub
		return null;
	}

	public IPath getPath() {
		// TODO Auto-generated method stub
		return lockFile.getFullPath();
	}

	public IStorage getStorage() throws CoreException {
		// TODO Auto-generated method stub
		if (lockStorage == null) {
			lockStorage = new LockFileStorage(lockFile);
		}
		return lockStorage;
	}

	public Object[] getChildren(Object o) {
		// TODO Auto-generated method stub
		return null;
	}

	public ImageDescriptor getImageDescriptor(Object object) {
		IWorkbenchAdapter fileAdapter = (IWorkbenchAdapter)lockFile.getAdapter(IWorkbenchAdapter.class);
		return fileAdapter == null ? null : fileAdapter.getImageDescriptor(lockFile);
	}

	public String getLabel(Object o) {
		// TODO Auto-generated method stub
		return getName();
	}

	public Object getParent(Object o) {
		// TODO Auto-generated method stub
		return null;
	}

}
