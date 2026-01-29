package com.azsoft.ecams.popmenu.provider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.internal.WorkbenchImages;

/**
* This class provides the labels for the file tree
*/

public class FileTreeLabelProvider implements ILabelProvider {
	// The listeners
	private List listeners;

	// Images for tree nodes
	private Image file;

	private Image dir;

	// Label provider state: preserve case of file names/directories
	boolean preserveCase;

	/**
	* Constructs a FileTreeLabelProvider
	*/
	public FileTreeLabelProvider() {
		// Create the list to hold the listeners
		listeners = new ArrayList();

		file = WorkbenchImages.getImage(ISharedImages.IMG_OBJ_FILE);
		dir = WorkbenchImages.getImage(ISharedImages.IMG_OBJ_FOLDER);
	}

	public Image getImage(Object arg0) {
		return ((File) arg0).isDirectory() ? dir : file;
	}

	public String getText(Object arg0) {
		String text = ((File) arg0).getName();

		if (((File) arg0).getName().length() == 0) {
			text = ((File) arg0).getPath();
		}
		return text;
	}

	public void addListener(ILabelProviderListener arg0) {
		listeners.add(arg0);
	}

	public void dispose() {
		//Dispose the images
		if (dir != null){
			dir.dispose();
		}
	    if (file != null){
	      file.dispose();
	    }
	}

	public boolean isLabelProperty(Object arg0, String arg1) {
		return false;
	}

	public void removeListener(ILabelProviderListener arg0) {
		listeners.remove(arg0);		
	}

}