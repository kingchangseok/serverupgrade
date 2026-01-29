package com.azsoft.ecams.ui.show;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

public class FileEditorView {
	public FileEditorView (IProject project, String filename, IPath path, String Gbn){
		
		try{
			IFile ifile = null;
			
			if("LO".equals(Gbn)){
				ifile = project.getFile(path.lastSegment());
			}else{
				IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
				ifile = root.getFileForLocation(path);
			}
			
			if(null != ifile){
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				
				FileEditorInput input = new FileEditorInput(ifile);
				
				if("LO".equals(Gbn)){
					if (ifile.exists()){
						ifile.delete(true, null);
						
						final IEditorPart editor = page.findEditor(input);
						if (editor != null){
							editor.getEditorSite().getPage().closeEditor(editor, false);
						}
					}
					
					ifile.createLink(path, IResource.NONE, null);
					ifile = null;
				}
				
				IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(filename);
				if(desc == null){
					page.openEditor(input,"org.eclipse.ui.DefaultTextEditor");
				}else{
					page.openEditor(input, desc.getId());
					desc = null;
				}
				page = null;
				input = null;
			}
			
		}catch(Exception E){
			E.printStackTrace();
		}
	}
}
