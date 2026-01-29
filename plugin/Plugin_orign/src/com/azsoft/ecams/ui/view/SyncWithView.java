package com.azsoft.ecams.ui.view;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

import com.azsoft.ecams.core.EcamsProviderPlugin;
import com.azsoft.ecams.core.IEcamsStatus;
import com.azsoft.ecams.core.jobs.SyncJob;
import com.azsoft.ecams.image.ImageUtil;
import com.azsoft.ecams.popmenu.ShowHistoryPageSource;
import com.azsoft.ecams.properties.IProperty;
import com.azsoft.ecams.proto.ProtoEcams.EcamsMessage;
import com.azsoft.ecams.proto.ProtoEcams.FileData;
import com.azsoft.ecams.proto.ProtoEcams.FileDataList;
import com.azsoft.ecams.proto.ProtoEcams.PathInfo;
import com.azsoft.ecams.proto.ProtoEcams.ReturnMsg;
import com.azsoft.ecams.proto.ProtoEcams.SysInfo;
import com.azsoft.ecams.proto.ProtoEcams.UserInfo;
import com.azsoft.ecams.socket.EcamsClient;
import com.azsoft.ecams.ui.dialog.ShowHistoryPage;
import com.azsoft.ecams.ui.dialog.SourceDiffDlg;
import com.azsoft.ecams.ui.show.FileCompareView;
import com.azsoft.ecams.ui.show.FileEditorView;
import com.azsoft.ecams.ui.show.MakeFileManager;
import com.azsoft.ecams.ui.widgets.SyncWithResourceTree;
import com.azsoft.ecams.util.checksum.CheckSum;
import com.azsoft.ecams.util.file.EFileToByteArray;

import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.history.IHistoryPage;
import org.eclipse.team.ui.history.IHistoryView;

public class SyncWithView extends ViewPart{
	private Action AllExpand, AllCollapse, IncomingMode, OutgoingMode, BothMode, ConflictsMode, SyncAction, versionAction, diffAction;
	
	public SyncWithResourceTree viewer;
	public Text text;
	
	private IResource[] changeResources = null;
	private IProject project = null;
	
	private String ip, port, id, passwd;
	private List addLists = new ArrayList();
	
	private IDoubleClickListener iDoubleClick_Listener;
	private IMenuListener SyncListener, VersionListener, DiffListener;
    private IResource[] selectResources;
    /**
     * @wbp.parser.constructor
     */
    
	
	public SyncWithView() {
    }
	
    @Override
    public void createPartControl(Composite parent) {  

		setupCallbacks();
		
    	parent.setLayout(new GridLayout(1, false));
    	
    	text = new Text(parent, SWT.NONE);
    	text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    	text.setEditable(false);
    	
    	viewer = new SyncWithResourceTree(parent, SWT.NONE, changeResources, "T");
    	viewer.getTreeViewer().addDoubleClickListener(iDoubleClick_Listener);
    	viewer.setDragDetect(true);
    	addDragSupport(viewer.getParent());
    	
        createActions();
        initializeToolBar();
    }

    private void addDragSupport(Control control) {
		DragSourceListener listener = new DragSourceListener() {
			
			@Override
			public void dragStart(DragSourceEvent event) {
				// TODO Auto-generated method stub
				event.doit = true;
			}
			
			@Override
			public void dragSetData(DragSourceEvent event) {
				// TODO Auto-generated method stub
				IStructuredSelection sel = (IStructuredSelection) viewer.getTreeViewer().getSelection();
				
				List filenameList = new LinkedList();
				for (Iterator it =((IStructuredSelection) sel).iterator();it.hasNext();){
					Object select_obj = it.next();
					if (select_obj != null && select_obj instanceof IResource){
						if (((IResource)select_obj).getType()==IResource.FILE){
							File file = new File(((IResource)select_obj).getLocation().toString());
							if(file.exists()){
								filenameList.add(file.getAbsoluteFile().toString());
							}
							file = null;
						}
					}
				}
				event.data = (String[])filenameList.toArray(new String[filenameList.size()]);
			}
			
			@Override
			public void dragFinished(DragSourceEvent event) {
				// TODO Auto-generated method stub
			}
		};

    	Transfer[] transferTypes = new Transfer[] {FileTransfer.getInstance()};
    	//LocalSelectionTransfer.getTransfer(), ResourceTransfer.getInstance(), FileTransfer.getInstance(), PluginTransfer.getInstance()

    	viewer.getTreeViewer().addDragSupport(DND.DROP_COPY | DND.DROP_MOVE, transferTypes, listener);
    }
    
    private void setupCallbacks(){
    	iDoubleClick_Listener = new IDoubleClickListener() {
			
			@Override
			public void doubleClick(DoubleClickEvent event) {
				// TODO Auto-generated method stub
				IStructuredSelection sel = (IStructuredSelection)viewer.getTreeViewer().getSelection();
				if((IResource)sel.getFirstElement() instanceof IContainer){
					if(viewer.getTreeViewer().getExpandedState(sel.getFirstElement())){
						viewer.getTreeViewer().collapseToLevel(sel.getFirstElement(), AbstractTreeViewer.ALL_LEVELS);
					}else{
						viewer.getTreeViewer().expandToLevel(sel.getFirstElement(), AbstractTreeViewer.ALL_LEVELS);
					}
				}else{
					if(((IResource)sel.getFirstElement()).getType() == IResource.FILE){

				    	ip = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.IP, "", null);
						port = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PORT, "", null);
						id = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.ID, "", null);
						passwd = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PASSWD, "", null);
						
						if((null == ip || "".equals(ip)) || (null == port || "".equals(port))
								|| (null == id || "".equals(id)) || (null == passwd || "".equals(passwd))){
							return;
						}
						
						IResource selResources = (IResource)sel.getFirstElement();
						
						boolean newFlag = false;
						final IEcamsStatus tmpStatus = EcamsProviderPlugin.getPlugin().getXmlStatusMgr().getStatus(selResources);
						if(null == tmpStatus){
							newFlag = true;
						}else{
							if("3".equals(tmpStatus)){
								newFlag = true;
							}
						}
						
						if(newFlag){
							try{
								File nfile = new File(selResources.getLocation().toString());
								if(!nfile.exists()){
									String itemid = "";
									String version = "0";
									String relativePath = "";
									
									for(int fcnt=0; fcnt<addLists.size(); fcnt++){
										FileData fileData = (FileData) addLists.get(fcnt);
										
										relativePath = selResources.getParent().getLocation().toString().replace(project.getLocation().toString(), "");
										
										if(fileData.getFilename().equals(selResources.getName())
												&& fileData.getPathinfo().getRelativitePath().equals(relativePath)){
											itemid = fileData.getItemid();
											version = Integer.toString(fileData.getVersion());
											break;
										}
										fileData = null;
									}
									
									if(null != itemid && !"".equals(itemid)){
										MakeFileManager makeFileManager = new MakeFileManager();
										
										//System.out.println(project+","+itemid+","+selResources[0].getName()+","+relativePath+","+version);
										String retStr = makeFileManager.execFileMake(project, itemid, selResources.getName(), relativePath, version);
										makeFileManager = null;
										
										if("OK".equals(retStr.substring(0,2))){
											//System.out.println(Path.fromOSString(retStr.substring(2)));
											new FileEditorView(project, selResources.getName(), Path.fromOSString(retStr.substring(2)), "LO");
										}
									}
								}else{
									//System.out.println(new Path(selResources[0].getLocation().toString()));
									new FileEditorView(project, selResources.getName(), new Path(selResources.getLocation().toString()), "RE");
								}
								nfile = null;
							}catch(Exception E){
								E.printStackTrace();
							}
						}else{
							String relativePath = selResources.getLocation().toOSString();
							while(relativePath.indexOf("/") >=0){
								relativePath = relativePath.replace("/","\\");
							}
							while(relativePath.indexOf("\\\\") >=0){
								relativePath = relativePath.replace("\\\\", "\\");
							}
							while(relativePath.indexOf("\\") >=0){
								relativePath = relativePath.replace("\\", "/");
							}

							MakeFileManager makeFileManager = new MakeFileManager();
							String retStr = makeFileManager.execFileMake(project, tmpStatus.getItemid(), tmpStatus.getName(), relativePath, "LO");
							
							if("OK".equals(retStr.substring(0, 2))){
								relativePath = selResources.getParent().getLocation().toString().replace(project.getLocation().toString(), "");
								retStr = makeFileManager.execFileMake(project, tmpStatus.getItemid(), tmpStatus.getName(), relativePath, Integer.toString(tmpStatus.getLastVer()));
							}
							makeFileManager = null;
							
							if("OK".equals(retStr.substring(0, 2))){
								new FileCompareView(tmpStatus.getName()+" <Local>", 
										            "C:/history/tmp/."+tmpStatus.getName()+".LO",
										            tmpStatus.getName()+" <Remote Version:"+tmpStatus.getLastVer()+">", 
										            "C:/history/tmp/."+tmpStatus.getName()+"."+tmpStatus.getLastVer());
							}
						}
						selResources = null;
					}
				}
			}
		};
    }
    
    public void createActions() {
        // Create the actions
    	ip = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.IP, "", null);
		port = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PORT, "", null);
		id = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.ID, "", null);
		passwd = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PASSWD, "", null);
		
		if((null == ip || "".equals(ip)) || (null == port || "".equals(port))
				|| (null == id || "".equals(id)) || (null == passwd || "".equals(passwd))){
			return;
		}
		
		SyncAction = new Action("eCAMS Synchronizing") {
			public void run() {
				IStructuredSelection sel = (IStructuredSelection)viewer.getTreeViewer().getSelection();
				
    			List selectedList = new ArrayList();
				for (Iterator it =((IStructuredSelection) sel).iterator();it.hasNext();){
					Object select_obj = it.next();
					if (select_obj != null && select_obj instanceof IResource){
						if (((IResource)select_obj).getType()==IResource.FILE){
							selectedList.add((IResource)select_obj);
						}else if (((IResource)select_obj).getType()==IResource.FOLDER){
							selectResources = viewer.getSelectedResources();
							for(int i=0; i<selectResources.length; i++){
								if(selectResources[i].getParent().toString().indexOf(((IResource)select_obj).toString())>-1){
									selectedList.add((IResource)selectResources[i]);
								}
							}
						}else if (((IResource)select_obj).getType()==IResource.PROJECT){
							selectResources = viewer.getSelectedResources();
							for(int i=0; i<selectResources.length; i++){
								if(selectResources[i].getProject().equals(((IResource)select_obj).getProject())){
									selectedList.add((IResource)selectResources[i]);
								}
							}
							break;
						}
					}
				}
				
				if(selectedList.size()>0){
					selectResources = (IResource[])selectedList.toArray(new IResource[selectedList.size()]);
				}
				selectedList = null;
				
				boolean enabledflg = true;
				for (int x=1; x<selectResources.length; x++){
					if(!selectResources[x].getProject().equals(selectResources[0].getProject())){
						enabledflg = false;
						break;
					}
				}
				if(enabledflg){
					EcamsProviderPlugin.getPlugin().getJobManager().addJob(new SyncJob("eCAMS Sync",selectResources,"","NONE"));
				}else{
					MessageBox messageBox = new MessageBox(getSite().getShell());
    				messageBox.setMessage("\uac19\uc740 \ud504\ub85c\uc81d\ud2b8\uc758 \ud504\ub85c\uadf8\ub7a8\ub9cc \ub3d9\uae30\ud654 \uac00\ub2a5\ud569\ub2c8\ub2e4.");
    				messageBox.setText("\ub2e4\ub978 \ud504\ub85c\uc81d\ud2b8\uac00 \uc120\ud0dd\ub418\uc5c8\uc2b5\ub2c8\ub2e4.");
    				messageBox.open();
    				return;
				}
			}
		};
		
		versionAction = new Action("Show History") {
			@SuppressWarnings("restriction")
			public void run() {
    			IHistoryView view = TeamUI.showHistoryFor(TeamUIPlugin.getActivePage(), selectResources, ShowHistoryPageSource.getInstance());
				IHistoryPage page = view.getHistoryPage();
				if (page instanceof ShowHistoryPage){
					ShowHistoryPage historyPage = (ShowHistoryPage) page;
					historyPage.setFocus();
				}
    		}
		};
		
		diffAction = new Action("Compare with Each Other") {
			public void run() {
				IEcamsStatus resourceStatus = EcamsProviderPlugin.getPlugin().getXmlStatusMgr().getStatus(selectResources[0]);
				
				if(resourceStatus != null){
					String filename = resourceStatus.getName();
	    			String itemid = resourceStatus.getItemid();
	    			
	    			try {
	    				FileChannel inChannel = new FileInputStream(resourceStatus.getFile()).getChannel();
	    				int size = (int)inChannel.size();
	    				if(size<1){
	    					throw new IOException("Error");
	    				}
	    				//byte[] testbyte = EFileToByteArray.FileToByteArray(resourceStatus.getFile());
	    			} catch (IOException e) {
	    				MessageBox messageBox = new MessageBox(getSite().getShell());
	    				messageBox.setMessage(filename+" \ud30c\uc77c \uc77d\uae30 \uc2e4\ud328(Size:0).");
	    				messageBox.setText("Source Diff");
	    				messageBox.open();
	    				return;
	    			}
	    			
	    			SourceDiffDlg sourcediffdlg = new SourceDiffDlg(getSite().getShell(), filename, itemid, resourceStatus.getRsrcinfo(), resourceStatus.getPath(), selectResources[0].getProject());
	    			sourcediffdlg.open();
	    			
	    			resourceStatus = null;
				}
			}
		};
		
		AllExpand = new Action("ALL Expand") {
			public void run() {
				viewer.getTreeViewer().expandAll();
			}
		};
	 	   
		AllCollapse = new Action("ALL Collapse") {
			public void run() {
				viewer.getTreeViewer().collapseAll();
			}
		};
		
		/*
		 * gbn
		 * C (\ub85c\uceec\uc218\uc815\uc0ac\ud56d\uc788\uc74c I)
		 * L (\ub85c\uceec\uc2e0\uaddc I)
		 * 
		 * V (\uc218\uc815\uc0ac\ud56d\uc5c6\uc774 \ubc84\uc804\ubcc0\uacbd\ub428 O)
		 * S (\uc11c\ubc84\uc2e0\uaddc O)
		 * D (\uc11c\ubc84\ud3d0\uae30 O)
		 * X ()
		 */
		
		
		IncomingMode = new Action("Incoming Mode") {
			public void run() {
				IncomingMode.setChecked(true);
				OutgoingMode.setChecked(false);
				BothMode.setChecked(false);
				ConflictsMode.setChecked(false);

				String[] objStr = {"C","L"};
				treeRefresh(objStr);
			}
		};
		   
		OutgoingMode = new Action("Outgoing Mode") {
			public void run() {
				OutgoingMode.setChecked(true);
				IncomingMode.setChecked(false);
				BothMode.setChecked(false);
				ConflictsMode.setChecked(false);

				String[] objStr = {"V","S","D"};
				treeRefresh(objStr);
			}
		};
		   
		BothMode = new Action("Outgoing Mode") {
			public void run() {
				BothMode.setChecked(true);
				IncomingMode.setChecked(false);
				OutgoingMode.setChecked(false);
				ConflictsMode.setChecked(false);

				String[] objStr = {};
				treeRefresh(objStr);
			}
		};
		
		ConflictsMode = new Action("Conflicts Mode") {
			public void run() {
				ConflictsMode.setChecked(true);
				BothMode.setChecked(false);
				IncomingMode.setChecked(false);
				OutgoingMode.setChecked(false);

				String[] objStr = {"X"};
				treeRefresh(objStr);
			}
		};
		
		CreatePopupMenu();
	}
    
    
    void treeRefresh(String[] gbn){
		List treeList = new ArrayList();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

		List tmpList = new ArrayList();
		for(int z=0; z<addLists.size(); z++){
			FileData filedata = (FileData) addLists.get(z);
			
			boolean addflg = false;
			
			if(gbn.length>0){
				for(int gbnCnt=0; gbnCnt<gbn.length; gbnCnt++){
					if(gbn[gbnCnt].equals(filedata.getStatus())){
						addflg = true;
						break;
					}
				}
			}else{
				addflg = true;
			}
			
			if(addflg){
    			String filepath = project.getLocation()+filedata.getPathinfo().getRelativitePath();
    			
    			while(filepath.indexOf("/") >=0){
    				filepath = filepath.replace("/","\\");
    			}
    			
    			while(filepath.indexOf("\\\\") >=0){
    				filepath = filepath.replace("\\\\", "\\");
    			}

    			while(filepath.indexOf("\\") >=0){
    				filepath = filepath.replace("\\", "/");
    			}	
    			
    			String filename = filepath+"/"+filedata.getFilename();
    			
				IPath tmpPath = new Path(filename);
				IResource tmpResource = root.getFileForLocation(tmpPath);
				tmpPath = null;
				
				treeList.add(tmpResource);

				tmpList.add(addLists.get(z));
				tmpResource = null;
			}
		}
		viewer.setAddLists(tmpList);
		tmpList = null;
		
		root = null;
		
		if(treeList.size()>0){
			viewer.setResources((IResource[])treeList.toArray(new IResource[treeList.size()]));
			treeList = null;
		}else{
			viewer.setDeleteViewer();
			viewer.getTreeViewer().remove(viewer.getTreeViewer().getInput());
		}
    	viewer.getTreeViewer().refresh();
    }
    
    private void initializeToolBar() {
        IToolBarManager toolbarManager=getViewSite().getActionBars().getToolBarManager();
        
        toolbarManager.add(AllExpand);
        toolbarManager.add(AllCollapse);
        toolbarManager.add(OutgoingMode);
        toolbarManager.add(IncomingMode);
        toolbarManager.add(BothMode);
        toolbarManager.add(ConflictsMode);
        
        AllExpand.setImageDescriptor(ImageUtil.getImageRegistry().getDescriptor("ExpandAll"));
        AllExpand.setToolTipText("ALL Expand");
        AllCollapse.setImageDescriptor(ImageUtil.getImageRegistry().getDescriptor("CollapseAll"));
        AllCollapse.setToolTipText("ALL Collapse");
        OutgoingMode.setImageDescriptor(ImageUtil.getImageRegistry().getDescriptor("Incoming"));
        OutgoingMode.setToolTipText("Outgoing Mode(\ub3d9\uae30\ud654 \ub300\uc0c1)");
        IncomingMode.setImageDescriptor(ImageUtil.getImageRegistry().getDescriptor("Outgoing"));
        IncomingMode.setToolTipText("Incoming Mode(\uccb4\ud06c\uc778 \ub300\uc0c1)");
        BothMode.setImageDescriptor(ImageUtil.getImageRegistry().getDescriptor("Both"));
        BothMode.setToolTipText("Incoming/Outgoing Mode(\uc804\uccb4 \ubd88\uc77c\uce58 \ub300\uc0c1)");
        ConflictsMode.setImageDescriptor(ImageUtil.getImageRegistry().getDescriptor("Conflicts"));
        ConflictsMode.setToolTipText("Conflicts Mode(\uccb4\ud06c\uc544\uc6c3 \ub300\uc0c1)");
        
        AllExpand.setEnabled(true);
        AllCollapse.setEnabled(true);
        OutgoingMode.setEnabled(true);
        OutgoingMode.setChecked(true);
        IncomingMode.setEnabled(true);
        IncomingMode.setChecked(true);
        BothMode.setEnabled(true);
        BothMode.setChecked(true);
        ConflictsMode.setEnabled(true);
        ConflictsMode.setChecked(true);
        OutgoingMode.setChecked(false);
        IncomingMode.setChecked(false);
        ConflictsMode.setChecked(false);
    }
    
    private void CreatePopupMenu(){
    	CreateListener();
    	
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(SyncListener);
		menuMgr.addMenuListener(VersionListener);
		menuMgr.addMenuListener(DiffListener);
		
 		Menu menu = menuMgr.createContextMenu(viewer.getTreeViewer().getTree());
		viewer.getTreeViewer().getTree().setMenu(menu);
 		menu = null;

 		getSite().registerContextMenu(menuMgr, viewer.getTreeViewer());
 		menuMgr = null;
    }
    
    private void CreateListener(){
    	SyncListener = new IMenuListener() {
			
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				// TODO Auto-generated method stub
				//!manager.isEmpty()

				Action action = new Action() {
					public void run() {
						super.run();
						SyncAction.run();
					}
				};
				
				IStructuredSelection sel = (IStructuredSelection)viewer.getTreeViewer().getSelection();
				
				boolean enabledflg = true;
				if(sel.size() > 0){
					int prjCnt = 0;
					for (Iterator it =((IStructuredSelection) sel).iterator();it.hasNext();){
						Object select_obj = it.next();
						if (select_obj != null && select_obj instanceof IResource){
							if(((IResource)select_obj).getType()==IResource.PROJECT){
								++prjCnt;
							}
						}
					}
					if(prjCnt>1) enabledflg = false;
				}else{
					enabledflg = false;
				}
				
				action.setEnabled(enabledflg);
				action.setText("Synchronize with eCAMS");
				manager.add(action);
			}
		};
		
		VersionListener = new IMenuListener() {
			
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				// TODO Auto-generated method stub

				Action action = new Action() {
					public void run() {
						super.run();
						versionAction.run();
					}
				};

				IStructuredSelection sel = (IStructuredSelection)viewer.getTreeViewer().getSelection();
				
				List selectedList = new ArrayList();
				for (Iterator it =((IStructuredSelection) sel).iterator();it.hasNext();){
					Object select_obj = it.next();
					if (select_obj != null && select_obj instanceof IResource){
						if (((IResource)select_obj).getType()==IResource.FILE){
							selectedList.add((IResource)select_obj);
						}
					}
				}
				boolean enabledflg = false;
				if(selectedList.size() == 1){
					enabledflg = true;
					selectResources = (IResource[])selectedList.toArray(new IResource[selectedList.size()]);
			    	IEcamsStatus resourceStatus = EcamsProviderPlugin.getPlugin().getXmlStatusMgr().getStatus(selectResources[0]);
					if((resourceStatus == null) || (resourceStatus.getLastVer() == 0 && resourceStatus.getTstVer() == 0)) enabledflg = false;
					
					resourceStatus = null;
				}
				selectedList = null;
								
				action.setEnabled(enabledflg);
				action.setText("Show History");
				action.setImageDescriptor(ImageUtil.getImageRegistry().getDescriptor("History"));
				manager.add(action);
			}
		};
		
		DiffListener = new IMenuListener() {
			
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				// TODO Auto-generated method stub

				Action action = new Action() {
					public void run() {
						super.run();
						diffAction.run();
					}
				};
				
				IStructuredSelection sel = (IStructuredSelection)viewer.getTreeViewer().getSelection();
				
				List selectedList = new ArrayList();
				for (Iterator it =((IStructuredSelection) sel).iterator();it.hasNext();){
					Object select_obj = it.next();
					if (select_obj != null && select_obj instanceof IResource){
						if (((IResource)select_obj).getType()==IResource.FILE){
							selectedList.add((IResource)select_obj);
						}
					}
				}
				boolean enabledflg = false;
				if(selectedList.size() == 1){
					enabledflg = true;
					selectResources = (IResource[])selectedList.toArray(new IResource[selectedList.size()]);
			    	IEcamsStatus resourceStatus = EcamsProviderPlugin.getPlugin().getXmlStatusMgr().getStatus(selectResources[0]);
					if((resourceStatus == null) || resourceStatus.getLastVer() == 0 
						|| (resourceStatus.getRsrcinfo().toString().substring(9,10).equals("1"))){
						enabledflg = false;
					}
					
					resourceStatus = null;
				}
				selectedList = null;
				
				action.setEnabled(enabledflg);
				action.setText("Compare with Each Other");
				manager.add(action);
			}
		};
    }
    
    @Override
    public void setFocus() {
        // set the focus
    	viewer.setFocus();
    }

    public SyncWithResourceTree getViewer(){
    	return viewer;
    }
    
    public void setResourceView(IProject project) {
    	this.project = project;    	
    	if(project == null){
    		text.setText("");
			viewer.setDeleteViewer();
			viewer.getTreeViewer().remove(viewer.getTreeViewer().getInput());
    	}else{
        	viewer.setDeleteViewer();
        	addLists.clear();        	
    		try {
    	    	ip = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.IP, "", null);
    			port = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PORT, "", null);
    			id = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.ID, "", null);
    			passwd = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PASSWD, "", null);
    			
    			if((null == ip || "".equals(ip)) || (null == port || "".equals(port)) || (null == id || "".equals(id)) || (null == passwd || "".equals(passwd))) return;
    			
            	String sysmsg = this.project.getPersistentProperty(new QualifiedName("Properties","syscd")).split(":")[1];
        		text.setText("eCAMS("+sysmsg+" /"+this.project.getName()+")");
        		
        		String classPathStr = project.getLocation()+"/.classpath";
        		String outputPath = "";
        		File classPathFile = null;
        		try {
	        		classPathFile = new File(classPathStr);
	        		if (classPathFile.exists()) {
	        			//입력 스트림 생성
	                    FileReader filereader = new FileReader(classPathFile);
	                    //입력 버퍼 생성
	                    BufferedReader bufReader = new BufferedReader(filereader);
	                    String lineStr = "";
	                    String findStr1 = "kind=\"output\"";
	                    String findStr2 = "path=\"";
	                    while((lineStr = bufReader.readLine()) != null){
	                    	outputPath = "";
	                        if (lineStr.indexOf(findStr1)>-1) {
	                    	    outputPath = lineStr.substring(lineStr.indexOf(findStr1));	                    	    
	                    	    if (outputPath.indexOf(findStr2)>-1) {
		                    	    outputPath = outputPath.substring(outputPath.indexOf(findStr2)+findStr2.length());	
		                    	    outputPath = outputPath.substring(0,outputPath.indexOf("\"")).trim();
		                    	    break;
	                    	    } else {
	                    	    	outputPath = "";
	                    	    }
	                        }
	                    }
	                    findStr2 = null;
	                    findStr1 = null;
	                    lineStr = null;
	                    bufReader = null;
	                    filereader = null;
	        		}
	        	} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					outputPath = "";
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					outputPath = "";
				} finally {
					classPathFile = null;
					classPathStr = null;
				}
    			changeResources = getChangedResoures(project.members(), outputPath);
				String projectpath = project.getLocation().toOSString();
    			
    			FileDataList.Builder fileDataList = FileDataList.newBuilder();
    			for(int i=0;i<changeResources.length;i++){
    				if(changeResources[i].getType() == IResource.FILE){
	    				String filename = changeResources[i].getParent().getLocation().toOSString();
		    			
		    			while(filename.indexOf("/") >=0){
		    				projectpath = projectpath.replace("/","\\");
		    				filename = filename.replace("/","\\");
		    			}
		    			
		    			while(filename.indexOf("\\\\") >=0){
		    				projectpath = projectpath.replace("\\\\", "\\");
		    				filename = filename.replace("\\\\", "\\");
		    			}
	
		    			while(filename.indexOf("\\") >=0){
		    				projectpath = projectpath.replace("\\", "/");
		    				filename = filename.replace("\\", "/");
		    			}
		    			
		    			File realfile = new File(filename+"/"+changeResources[i].getName());
		    			if (realfile.exists()){//\ud30c\uc77c\uc774 \uc788\uc744\ub54c
		    				
			    			FileData.Builder fileData = FileData.newBuilder();
			    			fileData.setFilename(((IResource)changeResources[i]).getName());
			    			
		    				IEcamsStatus ecmStatus = EcamsProviderPlugin.getPlugin().getXmlStatusMgr().getStatus((IResource)changeResources[i]);
		    	    		if(null != ecmStatus){
		    	    			//System.out.println(changeResources[i].getName()+":"+CheckSum.MD5SumVal(realfile)+"\n"+CheckSum.MD5SumVal(filename+"/"+changeResources[i].getName()));
			    	    		fileData.setMd5Sum(CheckSum.MD5SumVal(filename+"/"+changeResources[i].getName()));
			    	    		if(("5".equals(ecmStatus.getFileStatus().split(":")[1]) || "3".equals(ecmStatus.getFileStatus().split(":")[1])
			    	    				|| "8".equals(ecmStatus.getFileStatus().split(":")[1]) || "D".equals(ecmStatus.getFileStatus().split(":")[1]))
			    	    			&& ecmStatus.getEditor().split(":")[1].equals(id)){
			    	    			if(ecmStatus.isChanged()) fileData.setStatus("1");
			    	    			else fileData.setStatus("0");
			    	    		}else{
			    	    			//\uccb4\ud06c\uc544\uc6c3(5)\uc744 \ud558\uc9c0\uc54a\uace0 \uc218\uc815\ud588\uac70\ub098 \uc2e0\uaddc(3)\uac00 \uc544\ub2cc \uacbd\uc6b0
			    	    			if(ecmStatus.isChanged()) fileData.setStatus("9");
			    	    			else fileData.setStatus("0");
			    	    		}
			    	    		ecmStatus = null;
		    	    		}else{
		    	    			fileData.setMd5Sum("");
		    	    			fileData.setItemid("");
		    	    			fileData.setVersion(0);
		    	    			fileData.setStatus("L");
		    	    		}
		    	    		
		    				PathInfo.Builder pathInfo = PathInfo.newBuilder();
			    			pathInfo.setRelativitePath(filename.replace(projectpath, ""));
			    			fileData.setPathinfo(pathInfo.build());
			    			
			    			fileDataList.addFiledatas(fileData.build());
			    			
			    			pathInfo = null;
			    			fileData = null;
		    			}
		    			realfile = null;
    				}
    			}
    			
    			if(fileDataList.getFiledatasCount() > 0){
					EcamsMessage.Builder builder_msg = EcamsMessage.newBuilder();
			    	builder_msg.setMsgtype("LOCAL_VS_SERVER");
			    	
			    	UserInfo.Builder userinfo_builder = UserInfo.newBuilder();
					userinfo_builder.setId(id);
					userinfo_builder.setPasswd(passwd);
		    		builder_msg.setUserinfo(userinfo_builder.build());
	
					SysInfo.Builder sysinfo_builder = SysInfo.newBuilder();
		    		sysinfo_builder.setSyscd(project.getPersistentProperty(new QualifiedName("Properties","syscd")).split(":")[0]);
		    		sysinfo_builder.setSysmsg(project.getPersistentProperty(new QualifiedName("Properties","syscd")).split(":")[1]);
		    		builder_msg.setSysinfo(sysinfo_builder.build());

	    			//System.out.println("a:"+fileDataList.getFiledatasCount());
		    		builder_msg.setFiledatalist(fileDataList.build());
		    		fileDataList = null;
		    		
		    		EcamsClient syncClient = new EcamsClient(ip,port);
		    		ReturnMsg returnMsg = syncClient.sendMsg(builder_msg.build());
		    		builder_msg = null;
		    		syncClient = null;
		    		
		    		if (returnMsg.getReturnval() == 0) {
		    			//System.out.println("b:"+returnMsg.getEcamsmsg().getFiledatalist().getFiledatasCount());
		    			addLists.addAll(returnMsg.getEcamsmsg().getFiledatalist().getFiledatasList());
		    			viewer.setAddLists(addLists);
		    			
		    			List treeList = new ArrayList();

	    				IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
	    				
	    				boolean addFlag = false;
		    			for(int z=0; z<addLists.size(); z++){
		    				addFlag = false;
		    				
		    				FileData filedata = (FileData) addLists.get(z);
		    				if(IncomingMode.isChecked() && ("C".equals(filedata.getStatus()) || "L".equals(filedata.getStatus()))){
		    					addFlag = true;
		    				}else if(OutgoingMode.isChecked() && ("V".equals(filedata.getStatus()) || "S".equals(filedata.getStatus()) || "D".equals(filedata.getStatus()))){
		    					addFlag = true;
		    				}else if(ConflictsMode.isChecked() && "X".equals(filedata.getStatus())){
		    					addFlag = true;
		    				}else if(BothMode.isChecked()){
		    					addFlag = true;
		    				}
		    	        	
		    				if(addFlag){
			        			String filepath = (projectpath+filedata.getPathinfo().getRelativitePath());
			        			
			        			while(filepath.indexOf("/") >=0){
			        				filepath = filepath.replace("/","\\");
			        			}
			        			
			        			while(filepath.indexOf("\\\\") >=0){
			        				filepath = filepath.replace("\\\\", "\\");
			        			}
	
			        			while(filepath.indexOf("\\") >=0){
			        				filepath = filepath.replace("\\", "/");
			        			}	
			        			
			        			String filename = filepath+"/"+filedata.getFilename();
			        			
			    				IPath tmpPath = new Path(filename);
			    				IResource tmpResource = root.getFileForLocation(tmpPath);
			    				if ("L".equals(filedata.getStatus())) {
			    					//System.out.println("+++++[SyncWithView]ecm-meta delete+++++"+tmpResource);
			    					EcamsProviderPlugin.getPlugin().getXmlStatusMgr().deleteDeco(tmpResource);
			    				}
			    				tmpPath = null;
			    				
			    				treeList.add(tmpResource);
			    				tmpResource = null;
		    				}
		    			}
		    			root = null;
		    			
		    			if (treeList.size()>0) {
		    				viewer.setResources((IResource[])treeList.toArray(new IResource[treeList.size()]));
		    				treeList = null;
		    			} else {
			    			viewer.setDeleteViewer();
			    			viewer.getTreeViewer().remove(viewer.getTreeViewer().getInput());
		    			}
		    		} else {
		    			viewer.setDeleteViewer();
		    			viewer.getTreeViewer().remove(viewer.getTreeViewer().getInput());
		    			

						MessageBox messageBox = new MessageBox(getSite().getShell());
	    				messageBox.setMessage(returnMsg.getReturnStr());
	    				messageBox.setText("\uc624\ub958\ubc1c\uc0dd");
	    				messageBox.open();
	    				
		    		}
		    		returnMsg = null;
				}
    			
				
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	viewer.getTreeViewer().refresh();
    }
    
    private IResource[] getChangedResoures(IResource[] resources, String outputPath){
		List findResourceList = null;
		Set<IResource> addResources = new HashSet<IResource>();
		try {
			for (int i=0;i<resources.length;i++){
				if (resources[i].getType()==IResource.FILE){
	    			String filename = resources[i].getLocation().toOSString();
	    			
	    			while(filename.indexOf("/") >=0){
	    				filename = filename.replace("/","\\");
	    			}
	    			
	    			while(filename.indexOf("\\\\") >=0){
	    				filename = filename.replace("\\\\", "\\");
	    			}
	
	    			while(filename.indexOf("\\") >=0){
	    				filename = filename.replace("\\", "/");
	    			}	
	    			
					if (filename.indexOf(".deco")>-1 || filename.indexOf(".svn")>-1
							|| filename.indexOf(".settings")>-1 || filename.indexOf("history/tmp")>-1
							//|| resources[i].getName().lastIndexOf(".class")>0 
							|| (!"".equals(outputPath) && filename.indexOf(outputPath)>-1)
							|| resources[i].getName().lastIndexOf(".ecm-meta")>-1){
						continue;
					}
					addResources.add(resources[i]);
					
				} else if (resources[i].getType()==IResource.FOLDER) {
	    			//System.out.println("Folder: "+resources[i].getLocation());
					IResource[] childFindResources = getChangedResoures(((IContainer) resources[i]).members(), outputPath);
					if (childFindResources != null){
						for (int j=0;j<childFindResources.length;j++){
							addResources.add(childFindResources[j]);
						}
					}
				}
			}
			
			findResourceList = new ArrayList(addResources);
			
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(findResourceList.size()>0){
			return (IResource[])findResourceList.toArray(new IResource[findResourceList.size()]);
		}else{
			return null;
		}
    }
    
    public IProject getProject(){
    	return this.project;
    }
}