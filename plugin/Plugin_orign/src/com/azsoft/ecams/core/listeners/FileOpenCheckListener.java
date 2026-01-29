package com.azsoft.ecams.core.listeners;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;

import com.azsoft.ecams.core.EcamsProviderPlugin;
import com.azsoft.ecams.core.IEcamsStatus;


public class FileOpenCheckListener implements IPartListener2 {

	public void partActivated(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub
	}

	public void partBroughtToTop(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub

	}

	public void partClosed(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub
	}

	public void partDeactivated(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub

	}

	public void partOpened(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub
		if (partRef instanceof IEditorReference){
			IEditorReference editRef = (IEditorReference)partRef;
			if (editRef == null){
				return;
			}

			IEditorPart editor = editRef.getEditor(true);
			if (editor == null) {
				return;
			}

			IEditorInput input = editor.getEditorInput();

			IFile editorFile = (IFile) input.getAdapter(IFile.class);

			IEcamsStatus filestatus = EcamsProviderPlugin.getPlugin().getXmlStatusMgr().getStatus(editorFile);
			if (filestatus == null){
				return;
			}
//			if(filestatus.getFileStatus().split(":")[1].equals("0")){ //\uc6b4\uc601\uc911\uc77c\ub54c \uba54\uc138\uc9c0
//				MessageBox messageBox = new MessageBox(partRef.getPage().getActivePart().getSite().getShell(), SWT.OK);
//				messageBox.setMessage("\uccb4\ud06c\uc544\uc6c3 \ub418\uc9c0\uc54a\uc740 \ubaa8\ub378\uc744 \uc218\uc815\ud558\uba74 \uccb4\ud06c\uc778 \ub418\uc9c0\uc54a\uc2b5\ub2c8\ub2e4.");
//				messageBox.open();
//			}
//			if (filestatus.isLocked()){
//				MessageBox messageBox = new MessageBox(partRef.getPage().getActivePart().getSite().getShell(), SWT.OK);
//				messageBox.setMessage("\uccb4\ud06c\uc544\uc6c3\ud6c4 \uc218\uc815\ud574\uc8fc\uc138\uc694");
//				messageBox.open();
//				try {
//					IEditorInput newinput = new LockFileEditorInput(editorFile); 
//					editor.init((IEditorSite) editor.getSite(),newinput );
//				} catch (PartInitException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				editor.getEditorSite().getPage().closeEditor(editor, false);
//			}
			/*
			if (!filestatus.isAuthority()){
				MessageBox messageBox = new MessageBox(partRef.getPage().getActivePart().getSite().getShell(), SWT.OK);
				messageBox.setMessage("\uad8c\ud55c\uc774\uc5c6\ub294 \ud30c\uc77c\uc785\ub2c8\ub2e4.");
				messageBox.open();
			}
			*/
			
			return;
		}
	}

	public void partHidden(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub

	}

	public void partVisible(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub

	}

	public void partInputChanged(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub
	}

}
