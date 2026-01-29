package com.azsoft.ecams.ui.show;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.PlatformUI;

import com.azsoft.ecams.ui.compare.CompareItem;

public class FileCompareView {
	
	public FileCompareView (String befName, final String befPath, String aftName, final String aftPath){
		
		CompareConfiguration cc = new CompareConfiguration();
	    cc.setLeftEditable(false);
	    cc.setRightEditable(false);
	    cc.setLeftLabel(befName);
	    cc.setRightLabel(aftName);
	    cc.setProperty(CompareConfiguration.IGNORE_WHITESPACE, Boolean.FALSE);

	    CompareEditorInput editorInput = new CompareEditorInput(cc) {
	    	   CompareItem left = new CompareItem(befPath);   
	    	   CompareItem right = new CompareItem(aftPath);   
	    	   
	    	   
	    	   @Override  
	    	   protected Object prepareInput(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {   
	    	       return new DiffNode(null, Differencer.CHANGE, null, left, right);   
	    	   }  
	    	   
	    	   @Override  
	    	   public void saveChanges(IProgressMonitor pm) throws CoreException {   
	    	       super.saveChanges(pm);   
	    	  
	    	       left.writeFile();   
	    	       right.writeFile();   
	    	   }   
	    	};   
	    	
	        CompareUI.openCompareEditorOnPage(editorInput, PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage());
	}
}
