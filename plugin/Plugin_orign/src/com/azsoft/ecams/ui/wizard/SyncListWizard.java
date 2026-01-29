package com.azsoft.ecams.ui.wizard;


import javax.swing.JOptionPane;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import com.azsoft.ecams.core.EcamsProviderPlugin;
import com.azsoft.ecams.core.jobs.CompleteSRJob;
import com.azsoft.ecams.core.jobs.SyncJob;
import com.azsoft.ecams.core.jobs.SyncListJob;
import com.azsoft.ecams.core.jobs.UpdateStatusJob;
import com.azsoft.ecams.proto.ProtoEcams.FileDataList;
import com.azsoft.ecams.ui.dialog.SyncListDlg;
import com.azsoft.ecams.ui.wizard.page.SyncSelectResourcePage;
import com.azsoft.ecams.ui.dialog.ConfirmMergeMessageDlg;
import com.azsoft.ecams.ui.dialog.ConfirmMessage5Dlg;


public class SyncListWizard extends Wizard implements INewWizard{
	private IResource[] resources, selectResource;
	private IProject project;
	String result = "N";
	private IStructuredSelection selection;
	private SyncListDlg parentDialog;
	

	public SyncListWizard(IResource[] resources, IProject project) {
		this.resources = resources;
		this.project = project;
		setWindowTitle("\ubcc0\uacbd\uc0ac\ud56d\uc774 \uc788\ub294 \ud504\ub85c\uadf8\ub7a8 \ubaa9\ub85d");
	}
	
	public void addPages() {
		this.addPage(new SyncSelectResourcePage(resources));
	}

	public boolean performFinish() {
		if (parentDialog.getCurrentPage() instanceof SyncSelectResourcePage){
			SyncSelectResourcePage syncRequestPage = (SyncSelectResourcePage) parentDialog.getCurrentPage();
			selectResource = syncRequestPage.getResourceSelectionTree().getSelectedResources();
			
			if(null != selectResource && selectResource.length>0){
//				final int option = JOptionPane.showConfirmDialog(null, "\uc120\ud0dd\ud558\uc2e0 \ud504\ub85c\uadf8\ub7a8\uc740 \ucd5c\uc885\ubc84\uc804\uc73c\ub85c \ub36e\uc5b4\uc4f0\uae30 \ub429\ub2c8\ub2e4.\n\uacc4\uc18d\uc9c4\ud589 \ud558\uc2dc\uaca0\uc2b5\ub2c8\uae4c?", "\ud655\uc778", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
//				if(option == JOptionPane.YES_OPTION){
//					//EcamsProviderPlugin.getPlugin().getJobManager().addJob(new SyncJob("eCAMS Sync",selectResource,"","DOWN"));
//					EcamsProviderPlugin.getPlugin().getJobManager().addJob(new SyncListJob("eCAMS Sync",selectResource,project));
//					return true;
//				}else{
//					return false;
//				}
			
				Display.getDefault().syncExec(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						ConfirmMessage5Dlg confirmMergeMessageDlg = new ConfirmMessage5Dlg(new Shell());
						confirmMergeMessageDlg.open();
						
						if (confirmMergeMessageDlg.getReturnCode() != ConfirmMergeMessageDlg.OK) {
							result = "N";
						}else{
							if ("1".equals(confirmMergeMessageDlg.getChkAns())) {
								result = "Y";
							}else{
								result = "N";
							}
						}
					}
				});
				
				if("Y".equals(result)) {
					EcamsProviderPlugin.getPlugin().getJobManager().addJob(new SyncListJob("eCAMS Sync",selectResource,project));
					return true;
				}else{
					return false;
				}
				
			}else{
				return true;
			}
		}
		return false;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// TODO Auto-generated method stub
		this.selection = selection;
	}
	
	public boolean canFinish() {
		// TODO Auto-generated method stub
		if (parentDialog.getCurrentPage() instanceof SyncSelectResourcePage){
			return true;
		}
		return super.canFinish();
	}

	public SyncListDlg getParentDialog() {
		return parentDialog;
	}

	public void setParentDialog(SyncListDlg parentDialog) {
		this.parentDialog = parentDialog;
	}
	
	@Override
	public boolean performCancel() {
		// TODO Auto-generated method stub
		if (parentDialog.getCurrentPage() instanceof SyncSelectResourcePage){
			SyncSelectResourcePage syncRequestPage = (SyncSelectResourcePage) parentDialog.getCurrentPage();
			selectResource = syncRequestPage.getResourceSelectionTree().getSelectedResources();
			
			if(null != selectResource && selectResource.length>0){
				try {
					Thread.sleep(1000);
					EcamsProviderPlugin.getPlugin().getJobManager().addJob(new UpdateStatusJob("Resource Status Updating..",selectResource,""));
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return super.performCancel();
	}	

}
