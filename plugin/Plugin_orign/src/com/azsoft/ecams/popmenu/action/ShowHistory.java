package com.azsoft.ecams.popmenu.action;



import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.PackageFragment;
import org.eclipse.jdt.internal.core.PackageFragmentRoot;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.history.IHistoryPage;
import org.eclipse.team.ui.history.IHistoryView;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.actions.ActionDelegate;
import org.eclipse.ui.internal.ViewPluginAction;

import com.azsoft.ecams.core.EcamsProviderPlugin;
import com.azsoft.ecams.core.IEcamsStatus;
import com.azsoft.ecams.popmenu.ShowHistoryPageSource;
import com.azsoft.ecams.ui.dialog.ShowHistoryPage;

public class ShowHistory extends ActionDelegate implements IViewActionDelegate {
	private Shell shell;
	private String ip,port,id,passwd;
	private TableViewer viewer;
	private String result = "";
	private Composite tableComposite;
	
	@Override
	public void init(IViewPart view) {
		// TODO Auto-generated method stub
		shell = view.getSite().getShell();
	}
	
	public void run(IAction action) {
		List selectedList = new ArrayList();
		
		ViewPluginAction myAction;
		ISelection mySelection;
		if (action instanceof ViewPluginAction){
			myAction = (ViewPluginAction) action;
		}
		else{
			return;
		}
		
		mySelection = myAction.getSelection();
		int cnt = 0;
		if (mySelection instanceof IStructuredSelection){
			for (Iterator it =((IStructuredSelection) mySelection).iterator();it.hasNext();){
				Object select_obj = it.next();

				if (select_obj != null && select_obj instanceof IResource){
					if(((IResource)select_obj).getType()==IResource.FILE){
						selectedList.add((IResource)select_obj);
					}
					if (((IResource)select_obj).getType()==IResource.FOLDER || ((IResource)select_obj).getType()==IResource.PROJECT){
						cnt++;
					}
				}
				else if (select_obj != null && select_obj instanceof JavaProject){
					cnt++;
				}
				else if (select_obj != null && select_obj instanceof PackageFragmentRoot){
					cnt++;
				}
				else if (select_obj != null && select_obj instanceof PackageFragment){
					cnt++;
				}
				else if (select_obj != null && select_obj instanceof CompilationUnit){
					selectedList.add((IResource)((CompilationUnit)select_obj).getResource());
				}
			}
		}
		
		if(cnt>0){
			MessageBox messageBox = new MessageBox(shell);
			messageBox.setMessage("\ud30c\uc77c\uc744 \uc120\ud0dd\ud558\uc138\uc694");
			messageBox.setText("\ud30c\uc77c\uc120\ud0dd");
			messageBox.open();
			return;
		}
		
		IResource[] selectResources = (IResource[])selectedList.toArray(new IResource[selectedList.size()]);
		
		IEcamsStatus resourceStatus = EcamsProviderPlugin.getPlugin().getXmlStatusMgr().getStatus(selectResources[0]);
		
		if (resourceStatus == null){
			MessageBox messageBox = new MessageBox(shell);
			messageBox.setMessage("\ud788\uc2a4\ud1a0\ub9ac\uac00\uc5c6\uc2b5\ub2c8\ub2e4.");
			messageBox.setText("Show History");
			messageBox.open();
			return;
		}
		
		if(resourceStatus.getLastVer() == 0){
			MessageBox messageBox = new MessageBox(shell);
			messageBox.setMessage("\uc2e0\uaddc\ud30c\uc77c\uc785\ub2c8\ub2e4.");
			messageBox.setText("Show History");
			messageBox.open();
			return;
		}
		
		if(selectedList.size() == 1){
			IHistoryView view = TeamUI.showHistoryFor(TeamUIPlugin.getActivePage(), selectResources, ShowHistoryPageSource.getInstance());
			IHistoryPage page = view.getHistoryPage();
			if (page instanceof ShowHistoryPage){
				ShowHistoryPage historyPage = (ShowHistoryPage) page;
			}
			
			//ShowHistoryDlg showhistorydlg = new ShowHistoryDlg(shell,selectResources, filename, itemid);
			//showhistorydlg.open();
		}
	}
	
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub
		
	}
}