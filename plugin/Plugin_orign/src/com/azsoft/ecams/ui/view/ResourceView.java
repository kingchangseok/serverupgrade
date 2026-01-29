package com.azsoft.ecams.ui.view;



import java.awt.Checkbox;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.azsoft.ecams.core.CommandExecuter;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.ViewPart;

import com.azsoft.ecams.core.EcamsLogManager;
import com.azsoft.ecams.core.EcamsProviderPlugin;
import com.azsoft.ecams.core.IEcamsStatus;
import com.azsoft.ecams.core.jobs.CheckInJob;
import com.azsoft.ecams.core.jobs.CheckOutCnlJob;
import com.azsoft.ecams.core.jobs.CheckOutJob;
import com.azsoft.ecams.core.jobs.SyncJob;
import com.azsoft.ecams.core.resource.EcamsRepositoryProvider;
import com.azsoft.ecams.image.ImageUtil;
import com.azsoft.ecams.popmenu.ShowHistoryPageSource;
import com.azsoft.ecams.properties.IProperty;
import com.azsoft.ecams.proto.ProtoEcams.EcamsMessage;
import com.azsoft.ecams.proto.ProtoEcams.FileData;
import com.azsoft.ecams.proto.ProtoEcams.ReturnMsg;
import com.azsoft.ecams.proto.ProtoEcams.SRInfo;
import com.azsoft.ecams.proto.ProtoEcams.UserInfo;
import com.azsoft.ecams.socket.EcamsClient;
import com.azsoft.ecams.ui.dialog.CheckInDlg;
import com.azsoft.ecams.ui.dialog.ConfirmMessageDlg;
import com.azsoft.ecams.ui.dialog.LastCheckOutDlg;
import com.azsoft.ecams.ui.dialog.ShowHistoryPage;
import com.azsoft.ecams.ui.dialog.SourceDiffDlg;
import com.azsoft.ecams.ui.widgets.ResourceTree;
import com.azsoft.ecams.ui.wizard.CheckInWizard;
import com.azsoft.ecams.ui.wizard.LastCheckOutWizard;
import com.azsoft.ecams.util.file.EFileToByteArray;

import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.history.IHistoryPage;
import org.eclipse.team.ui.history.IHistoryView;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;

public class ResourceView extends ViewPart{
	private static EcamsLogManager logger = new EcamsLogManager(ICommandService.class.getName());
	
	ResourceTree viewer;
	
	// deleteAllAction,
	public Action refreshAction, deleteItemAction, syncAction, inAction, outAction, lastoutAction, cnclAction, versionAction, diffAction, AllExpand, AllCollapse;
	private IResource[] resources,resources2;
	
	private IResource[] SelectedResource, selectResources, chkInResources, chkOutResources, cnClResources;
	private IEcamsStatus resourceStatus;
	
	private static String srId, srTitle, Syscd2;
	private String ip, port, id, passwd, tool;
	private String Sysinfo = "";
	private List selectedList = new ArrayList();
	
	private MouseListener chkModifyButtonCheck_Listener;
//	private Listener refresh_Listener;
	private static Text text;
	private static Text title_sr;
	private static Button chkModify;
    /**
     * @wbp.parser.constructor
     */
    
	
	public ResourceView() {
    }
	
    @Override
    public void createPartControl(Composite parent) {
    	setupCallbacks();
    	
    	parent.setLayout(new GridLayout(3, false));
    	
    	text = new Text(parent, SWT.NONE);
    	text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
    	text.setEditable(false);

    	chkModify = new Button(parent, SWT.CHECK);
		chkModify.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		chkModify.setText("SR\uc0ac\uc6a9\uc548\ud568");
		chkModify.addMouseListener(chkModifyButtonCheck_Listener);
//		chkModify.setVisible(false);
		chkModify.setSelection(true);
		
    	viewer = new ResourceTree(parent, SWT.NONE, SelectedResource, "F");
    	viewer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 2));
    	viewer.getTreeViewer().addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				// TODO Auto-generated method stub
				IStructuredSelection sel = (IStructuredSelection)viewer.getTreeViewer().getSelection();
				//System.out.println(sel.getFirstElement());
				if((IResource)sel.getFirstElement() instanceof IContainer){
					if(viewer.getTreeViewer().getExpandedState(sel.getFirstElement())){
						viewer.getTreeViewer().collapseToLevel(sel.getFirstElement(), AbstractTreeViewer.ALL_LEVELS);
					}else{
						viewer.getTreeViewer().expandToLevel(sel.getFirstElement(), AbstractTreeViewer.ALL_LEVELS);
					}
				}else{
					if(((IResource)sel.getFirstElement()).getType() == IResource.FILE){
						List findResourceList = new ArrayList();
						Set addResources = new HashSet();
						addResources.add((IResource)sel.getFirstElement());
						findResourceList = new ArrayList(addResources);
						IResource[] selResources = (IResource[])findResourceList.toArray(new IResource[findResourceList.size()]);
						
						Map<String, String> parameter = new HashMap<String, String>(); 
						parameter.put(selResources[0].getProject().getName(), selResources[0].getProject().getName()+"/"+selResources[0].getProjectRelativePath().toString());
						
						
						boolean metaFlg = false;

						tool = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.TOOL, "", null);
						
						if("I".equals(tool)){
							try {
								if((Boolean)CommandExecuter.executeCommand("command.team.ResourceItemDoubleClicked", parameter) != null){
									metaFlg = (Boolean)CommandExecuter.executeCommand("command.team.ResourceItemDoubleClicked", parameter);
								}else{
									MessageBox messageBox = new MessageBox(getSite().getShell());
				    				messageBox.setMessage("Tool\uad6c\ubd84\uc774 \ub9de\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4. \ud655\uc778\ud558\uc5ec\uc8fc\uc2ed\uc2dc\uc624.");
				    				messageBox.setText("Tool");
				    				messageBox.open();
				    				return;
								}
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								ConfirmMessageDlg confirmMessageDlg = new ConfirmMessageDlg(new Shell(),e.getCause().toString(),e.getMessage().toString());
								confirmMessageDlg.open();
								return;
							} finally {
								parameter = null;
							}
						}
						
						if(!metaFlg){	
							try{
								//IPath path = Path.fromOSString(selResources[0].getProjectRelativePath().toOSString());
								
								IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
								IPath ipath = new Path(selResources[0].getLocation().toString());
								
								IFile ifile = root.getFileForLocation(ipath);
								
								FileEditorInput input = new FileEditorInput(ifile);
								
								IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(ifile.getName());
								IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
								if(desc == null){
									page.openEditor(input,"org.eclipse.ui.DefaultTextEditor");
								}else{
									page.openEditor(input, desc.getId());
								}
							}catch(Exception E){
								E.printStackTrace();
							}
						}
					}
				}
			}
		});
//    	viewer.getDisplay().addFilter(SWT.KeyDown, refresh_Listener);
		addDropSupport(viewer.getParent());
		
        createActions();
    }
    
    public void setupCallbacks() {
		chkModifyButtonCheck_Listener = new MouseListener(){
			
			public void mouseDoubleClick(MouseEvent e) {
				
			}
			public void mouseDown(MouseEvent e) {
				
			}
			public void mouseUp(MouseEvent e) {
				if (chkModify.getSelection()) {
					srId = "";
					text.setText("");
					viewer.setDeleteViewer();
					if(null != viewer.getTreeViewer().getInput()){
						viewer.getTreeViewer().remove(viewer.getTreeViewer().getInput());
					}
					viewer.redraw();
				}
			}
		};
//		refresh_Listener = new Listener() {
//			
//			@Override
//			public void handleEvent(Event event) {
//				// TODO Auto-generated method stub
//				if( viewer.getTreeViewer().getControl().isFocusControl() ) {
//					switch( event.keyCode ) {
//						case SWT.F5:	refreshAction.run();
//										break;
//						case SWT.DEL:	deleteItemAction.run();
//										break;
//						default:		break;
//					}
//				}
////				if( viewer.isVisible() && event.keyCode == SWT.F5 ) {
////					//System.out.println("resource viewer refresh!");
////					refreshAction.run();
////				}
//			}
//		};
	}
    
    //public ResourceSelectionTree getViewer(){
    public ResourceTree getViewer(){
    	return viewer;
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
		
    	syncAction = new Action("eCAMS Synchronizing") {
    		public void run() {
    			IStructuredSelection sel = (IStructuredSelection)viewer.getTreeViewer().getSelection();
    			selectedList.clear();
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
							//selectedList.clear();
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
					selectResources = dupliChk(selectedList);
					//selectResources = (IResource[])selectedList.toArray(new IResource[selectedList.size()]);
				}
				
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
 	    inAction = new Action("Request Check-IN") {
    		public void run() {
				if(isMySR()){
					CheckInWizard wizard = new CheckInWizard(chkInResources, srId, srTitle, "A", chkModify.getSelection());
					CheckInDlg checkInDlg = new CheckInDlg(getSite().getShell(),wizard);
					wizard.setParentDialog(checkInDlg);
					checkInDlg.open();
					//EcamsProviderPlugin.getPlugin().getJobManager().addJob(new CheckInJob("CheckIn Request",chkInResources,"","07",srId, srTitle, "A", chkModify.getSelection()));
				}
    		}
 	    };
 	    outAction = new Action("Requst Check-OUT") {
    		public void run() {
				if(isMySR()){
					EcamsProviderPlugin.getPlugin().getJobManager().addJob(new CheckOutJob("CheckOut Request",chkOutResources,"","",srId, srTitle, chkModify.getSelection()));
				}
    		}
 	    };
 	    lastoutAction = new Action("Request Previous Version Check-OUT") {
    		public void run() {
				if(isMySR()){
	    			LastCheckOutWizard wizard = new LastCheckOutWizard(selectResources, selectResources.length, resourceStatus.getItemid(), resourceStatus.getName(), ip, port, id, passwd, srId, srTitle, chkModify.getSelection());
					LastCheckOutDlg lastCheckOutDlg = new LastCheckOutDlg(getSite().getShell(),wizard);
					wizard.setParentDialog(lastCheckOutDlg);
					lastCheckOutDlg.open();
				}
    		}
 	    };
 	    cnclAction = new Action("Request Cancel Check-OUT") {
	   		public void run() {
				//if(isMySR()){
					EcamsProviderPlugin.getPlugin().getJobManager().addJob(new CheckOutCnlJob("CheckOutCancel Request",cnClResources, srId, srTitle, chkModify.getSelection()));
				//}
			}
	    };
 	    versionAction= new Action("Show History") {
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
    		}
 	    };
 	    refreshAction = new Action("Reload ServiceRequest List") {
			public void run() {
				if(!chkModify.getSelection()){
					if(null == srId || "".equals(srId)) return;
					
					EcamsMessage.Builder builder_msg = EcamsMessage.newBuilder();
					builder_msg.setMsgtype("GETRESOURCES");
	
					UserInfo.Builder userinfo_builder = UserInfo.newBuilder();
					userinfo_builder.setId(id);
					userinfo_builder.setPasswd(passwd);
					builder_msg.setUserinfo(userinfo_builder.build());
					
					SRInfo.Builder srinfo_builder = SRInfo.newBuilder();
					srinfo_builder.setCcSRId(srId);
					builder_msg.setSrinfo(srinfo_builder.build());
					
					EcamsClient syncClient = new EcamsClient(ip,port);
					ReturnMsg returnMsg = syncClient.sendMsg(builder_msg.build());
					
					if(returnMsg.getReturnval() == 0){
						viewer.setDeleteViewer();
						if(null != viewer.getTreeViewer().getInput()){
							viewer.getTreeViewer().remove(viewer.getTreeViewer().getInput());
						}
						viewer.getTreeViewer().refresh();
						
						if(returnMsg.getReturnStr().equals("END")){
							srId = "";
							text.setText("");
						}else{
							List vieweList = new ArrayList();
							vieweList.addAll(returnMsg.getEcamsmsg().getFiledatalist().getFiledatasList());
							
							if(vieweList.size()>0){
								IResource tmpResource = null;
								List<IResource> addList = new ArrayList<IResource>();
								
								for(int i=0; i<vieweList.size(); i++){
									FileData filedata = (FileData) vieweList.get(i);
									
									IProject findProject = EcamsRepositoryProvider.findProject(filedata.getSysinfo().getSyscd());
									
									if(findProject == null) continue;
									if(!findProject.isAccessible()) continue;
									if (!EcamsProviderPlugin.getPlugin().isManagedByEcams(findProject)) continue;
									
									String projectPath = findProject.getLocation().toString();
									
									String filepath = (projectPath+"/"+filedata.getPathinfo().getRelativitePath());
					    			
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
					    			File filez = new File(filename);
					    			
					    			if( filez.exists() ) {
						    			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
										IPath tmpPath = new Path(filename);
										
										tmpResource = root.getFileForLocation(tmpPath);
										
										addList.add(tmpResource);
					    			}
					    			tmpResource = null;
					    			filez = null;
								}
		
								SelectedResource = null;
								SelectedResource = (IResource[])addList.toArray(new IResource[addList.size()]);
								
								if(addList.size()>0){
									EcamsProviderPlugin.getPlugin().getJobManager().addJob(new SyncJob("eCAMS Sync",SelectedResource,"","NONE"));
								}
								viewer.setResources(SelectedResource);
								viewer.getTreeViewer().refresh();
							}
						}
					}else{
						srId = "";
						text.setText("");
					}
					getText();
				}else{
//					srId = "";
//					text.setText("");
//					viewer.setDeleteViewer();
//					if(null != viewer.getTreeViewer().getInput()){
//						viewer.getTreeViewer().remove(viewer.getTreeViewer().getInput());
//					}
//					viewer.getTreeViewer().refresh(); // 20210106 NO SR 
					EcamsMessage.Builder builder_msg = EcamsMessage.newBuilder();
					builder_msg.setMsgtype("GETRESOURCES2");
	
					UserInfo.Builder userinfo_builder = UserInfo.newBuilder();
					userinfo_builder.setId(id);
					userinfo_builder.setPasswd(passwd);
					builder_msg.setUserinfo(userinfo_builder.build());
					
					SRInfo.Builder srinfo_builder = SRInfo.newBuilder();
					srinfo_builder.setCcSRId("");
					builder_msg.setSrinfo(srinfo_builder.build());
					
					EcamsClient syncClient = new EcamsClient(ip,port);
					ReturnMsg returnMsg = syncClient.sendMsg(builder_msg.build());
					
					if(returnMsg.getReturnval() == 0){
						viewer.setDeleteViewer();
						if(null != viewer.getTreeViewer().getInput()){
							viewer.getTreeViewer().remove(viewer.getTreeViewer().getInput());
						}
						viewer.getTreeViewer().refresh();
						
						if(returnMsg.getReturnStr().equals("END")){
							srId = "";
							title_sr.setText("");
						}else{
							List vieweList = new ArrayList();
							vieweList.addAll(returnMsg.getEcamsmsg().getFiledatalist().getFiledatasList());
							
							if(vieweList.size()>0){
								IResource tmpResource = null;
								List<IResource> addList = new ArrayList<IResource>();
								
								for(int i=0; i<vieweList.size(); i++){
									FileData filedata = (FileData) vieweList.get(i);
									IProject findProject = EcamsRepositoryProvider.findProject(filedata.getSysinfo().getSyscd());
									
									if(findProject == null) continue;
									if(!findProject.isAccessible()) continue;
									if (!EcamsProviderPlugin.getPlugin().isManagedByEcams(findProject)) continue;
									
									String projectPath = findProject.getLocation().toString();
									
									String filepath = (projectPath+"/"+filedata.getPathinfo().getRelativitePath());
					    			
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
					    			File filez = new File(filename);
					    			
					    			if( filez.exists() ) {
						    			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
										IPath tmpPath = new Path(filename);
										
										tmpResource = root.getFileForLocation(tmpPath);
										
										addList.add(tmpResource);
					    			}
					    			tmpResource = null;
					    			filez = null;
								}
		
								SelectedResource = null;
								SelectedResource = (IResource[])addList.toArray(new IResource[addList.size()]);
								if(addList.size()>0){
									EcamsProviderPlugin.getPlugin().getJobManager().addJob(new SyncJob("eCAMS Sync",SelectedResource,"","NONE"));
								}
								viewer.setResources(SelectedResource);
								viewer.getTreeViewer().refresh();
							}
						}
					}else{
						srId = "";
						title_sr.setText("");
					}
					getText();
				}
			}
		};
		
 	    deleteItemAction = new Action("Selected Delete") {
 		   public void run() {
 			   if(viewer.getTreeViewer().getInput()!=null){
 				  id = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.ID, "", null);
 				  
 				   List<IResource> tmpList = new ArrayList<IResource>();
 				   IStructuredSelection sel = (IStructuredSelection)viewer.getTreeViewer().getSelection();
 				   int iDeleteItemCnt = 0;
 				   int i=0;
 				   for(i=0;i<SelectedResource.length;i++){
					   boolean sameFlg = false;
					   for (Iterator it =((IStructuredSelection) sel).iterator();it.hasNext();){
						   Object select_obj = it.next();
						   if (select_obj != null && select_obj instanceof IResource){
							   if ( ((IResource)select_obj).getType()==IResource.FILE ){
								   if(((IResource)select_obj).equals(SelectedResource[i])){
									   IEcamsStatus resourceStatus = EcamsProviderPlugin.getPlugin().getXmlStatusMgr().getStatus(SelectedResource[i]);
									   if(resourceStatus != null){
										   if((!resourceStatus.getFileStatus().split(":")[1].equals("5") 
												&& !resourceStatus.getFileStatus().split(":")[1].equals("B")
												&& !resourceStatus.getFileStatus().split(":")[1].equals("G"))
												|| (!resourceStatus.getFileStatus().split(":")[1].equals("0") && !resourceStatus.getEditor().split(":")[1].equals(id))
												|| !resourceStatus.getSRId().equals(srId)
										   		)
										   
										   {
											   sameFlg = true;
											   break;
										   }
									   }else{
										   sameFlg = true;
										   break;
									   }
									   /*if(resourceStatus != null){
										   if(resourceStatus.getFileStatus().split(":")[1].equals("0")){
											   sameFlg = true;
											   break;
										   }
									   } else{
										   sameFlg = true;
										   break;
									   }*/
								   }
							   } else if ( ((IResource)select_obj).getType()==IResource.FOLDER ) {
								   if( SelectedResource[i].getParent().toString().indexOf(((IResource)select_obj).toString())>-1 ) {
									   IEcamsStatus resourceStatus = EcamsProviderPlugin.getPlugin().getXmlStatusMgr().getStatus(SelectedResource[i]);
									   if(resourceStatus != null){
										   if((!resourceStatus.getFileStatus().split(":")[1].equals("5") 
												&& !resourceStatus.getFileStatus().split(":")[1].equals("B")
												&& !resourceStatus.getFileStatus().split(":")[1].equals("G"))
												|| (!resourceStatus.getFileStatus().split(":")[1].equals("0") && !resourceStatus.getEditor().split(":")[1].equals(id))
												|| !resourceStatus.getSRId().equals(srId)
										   		){
											   sameFlg = true;
											   break;
										   }
									   }else{
										   sameFlg = true;
										   break;
									   }
								   }
							   } else if(((IResource)select_obj).getType()==IResource.PROJECT){
									if(((IResource)select_obj).getProject().equals(SelectedResource[i].getProject())){
										resourceStatus = EcamsProviderPlugin.getPlugin().getXmlStatusMgr().getStatus(SelectedResource[i]);
										if(resourceStatus != null){
											if((!resourceStatus.getFileStatus().split(":")[1].equals("5") 
											&& !resourceStatus.getFileStatus().split(":")[1].equals("B")
											&& !resourceStatus.getFileStatus().split(":")[1].equals("G"))
											|| (!resourceStatus.getFileStatus().split(":")[1].equals("0") && !resourceStatus.getEditor().split(":")[1].equals(id))
											|| !resourceStatus.getSRId().equals(srId)
												){
												sameFlg = true;
												break;
											}
										}else{
											sameFlg = true;
											break;
										}
									}
								}
						   }
						   
					   }
					   if(!sameFlg){
						   tmpList.add(SelectedResource[i]);
					   } else {
						   iDeleteItemCnt++;
					   }
 				   }
				   
 				   if( iDeleteItemCnt > 0 ) {
					   viewer.setDeleteViewer();
					   viewer.getTreeViewer().remove(viewer.getTreeViewer().getInput());
					   viewer.getTreeViewer().refresh();
					   if(tmpList.size()>0){
						   SelectedResource = null;
						   SelectedResource = (IResource[])tmpList.toArray(new IResource[tmpList.size()]);
						   viewer.setResources(SelectedResource);
						   viewer.redraw();
					   }
 				   }
 				  
 				   resources = null;
 				   resources2 = null;
 				   //deleteAllAction.setEnabled(viewer.getTreeViewer().getTree().getItemCount()>0);
 				   deleteItemAction.setEnabled(false);
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
 	   
 	   viewer.getTreeViewer().addSelectionChangedListener(new ISelectionChangedListener() {
 		   public void selectionChanged(SelectionChangedEvent event) {
 			  id = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.ID, "", null);
 			  
 			   	IStructuredSelection sel = (IStructuredSelection)viewer.getTreeViewer().getSelection();
 			   	boolean selflg = false;
				for (Iterator it =((IStructuredSelection) sel).iterator();it.hasNext();){
					Object select_obj = it.next();
					if (select_obj != null && select_obj instanceof IResource){
						if (((IResource)select_obj).getType()==IResource.FILE){
							IEcamsStatus resourceStatus = EcamsProviderPlugin.getPlugin().getXmlStatusMgr().getStatus((IResource)select_obj);
							if(resourceStatus != null){
								//!resourceStatus.getFileStatus().split(":")[1].equals("0")
								if( (resourceStatus.getFileStatus().split(":")[1].equals("5") || resourceStatus.getFileStatus().split(":")[1].equals("B")
										||	resourceStatus.getFileStatus().split(":")[1].equals("G")) && resourceStatus.getEditor().split(":")[1].equals(id)
										&&	resourceStatus.getSRId().equals(srId)
										){
									selflg = true;
									break;
								}
							}
						}
					}
				}
				if(sel.size()==0) selflg = true;
 			   	deleteItemAction.setEnabled(!selflg);
 		   }
 	   });

       initializeToolBar();
 	   CreatePopupMenu();
	}

    private void initializeToolBar() {
        IToolBarManager toolbarManager=getViewSite().getActionBars().getToolBarManager();
        
        toolbarManager.add(refreshAction);
        toolbarManager.add(deleteItemAction);
        toolbarManager.add(AllExpand);
        toolbarManager.add(AllCollapse);

        deleteItemAction.setImageDescriptor(ImageUtil.getImageRegistry().getDescriptor("SelDel"));
        deleteItemAction.setToolTipText("\uc2e0\uaddc \ub610\ub294 \uc6b4\uc601\uc911\uc778 \ub9ac\uc18c\uc2a4\ub97c \uac1c\ubc1c\ubaa9\ub85d\uc5d0\uc11c \uc81c\uac70");
        refreshAction.setImageDescriptor(ImageUtil.getImageRegistry().getDescriptor("Refresh"));
        refreshAction.setToolTipText("Reload ServiceRequest List");
        AllExpand.setImageDescriptor(ImageUtil.getImageRegistry().getDescriptor("ExpandAll"));
        AllExpand.setToolTipText("ALL Expand");
        AllCollapse.setImageDescriptor(ImageUtil.getImageRegistry().getDescriptor("CollapseAll"));
        AllCollapse.setToolTipText("ALL Collapse");
        
        deleteItemAction.setEnabled(false);
        refreshAction.setEnabled(true);
        AllExpand.setEnabled(true);
        AllCollapse.setEnabled(true);
    }
    
    private void CreatePopupMenu(){
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				Action action = new Action() {
					public void run() {
						super.run();
						//TODO do something
						syncAction.run();
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
					if(prjCnt>1){
						enabledflg = false;
					}
				}else{
					enabledflg = false;
				}
				action.setEnabled(enabledflg);
				action.setText("&Synchronize with eCAMS");
				manager.add(action);
			}
		});
		//\uccb4\ud06c\uc778 (null,3,5,8 -> 8)
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				Action action = new Action() {
					public void run() {
						super.run();
						// TODO do something
						
						if( !isOtherProject() ) {
							inAction.run();
						}
					}
				};

				boolean enabledflg = false;

				IStructuredSelection sel = (IStructuredSelection)viewer.getTreeViewer().getSelection();
				if(selectedList.size() > 0) selectedList.removeAll(selectedList);
				if(sel.size() > 0) enabledflg = true;
				
				if(enabledflg){
					for (Iterator it =((IStructuredSelection) sel).iterator();it.hasNext();){
						Object select_obj = it.next();
						if (select_obj != null && select_obj instanceof IResource){
							for(int i=0;i<SelectedResource.length;i++){
								if (((IResource)select_obj).getType()==IResource.FILE){
									if(((IResource)select_obj).equals(SelectedResource[i])){
										resourceStatus = EcamsProviderPlugin.getPlugin().getXmlStatusMgr().getStatus(SelectedResource[i]);
										if(resourceStatus != null) {
											if ((!resourceStatus.getFileStatus().split(":")[1].equals("3")
												&& !resourceStatus.getFileStatus().split(":")[1].equals("5")
												&& !resourceStatus.getFileStatus().split(":")[1].equals("B")
												&& !resourceStatus.getFileStatus().split(":")[1].equals("G")
												&& !resourceStatus.getFileStatus().split(":")[1].equals("E"))
												|| !resourceStatus.getEditor().split(":")[1].equals(Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.ID, "", null))
												|| ( !resourceStatus.getFileStatus().split(":")[1].equals("0") && ( (null != resourceStatus.getSRId()) 
																													&& !"".equals(resourceStatus.getSRId())) 
																													&& !srId.equals(resourceStatus.getSRId()) ) ){
												enabledflg = false;
												break;
											}
										}else{
											enabledflg = false;
											break;
										}
										selectedList.add(SelectedResource[i]);
									}
								}else if(((IResource)select_obj).getType()==IResource.FOLDER){
									if(SelectedResource[i].getParent().toString().indexOf(((IResource)select_obj).toString())>-1){
										resourceStatus = EcamsProviderPlugin.getPlugin().getXmlStatusMgr().getStatus(SelectedResource[i]);
										if(resourceStatus != null) {
											if ((!resourceStatus.getFileStatus().split(":")[1].equals("3")
												&& !resourceStatus.getFileStatus().split(":")[1].equals("5")
												&& !resourceStatus.getFileStatus().split(":")[1].equals("B")
												&& !resourceStatus.getFileStatus().split(":")[1].equals("G")
												&& !resourceStatus.getFileStatus().split(":")[1].equals("E"))
												|| !resourceStatus.getEditor().split(":")[1].equals(Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.ID, "", null))
												|| ( !resourceStatus.getFileStatus().split(":")[1].equals("0") && ( (null != resourceStatus.getSRId()) 
																													&& !"".equals(resourceStatus.getSRId())) 
																													&& !srId.equals(resourceStatus.getSRId())) ){
												enabledflg = false;
												break;
											}
										}else{
											enabledflg = false;
											break;
										}
										selectedList.add(SelectedResource[i]);
									}
								}else if(((IResource)select_obj).getType()==IResource.PROJECT){
									if(((IResource)select_obj).getProject().equals(SelectedResource[i].getProject())){
										resourceStatus = EcamsProviderPlugin.getPlugin().getXmlStatusMgr().getStatus(SelectedResource[i]);
										if(resourceStatus != null) {
											if ((!resourceStatus.getFileStatus().split(":")[1].equals("3")
												&& !resourceStatus.getFileStatus().split(":")[1].equals("5")
												&& !resourceStatus.getFileStatus().split(":")[1].equals("B")
												&& !resourceStatus.getFileStatus().split(":")[1].equals("G")
												&& !resourceStatus.getFileStatus().split(":")[1].equals("E"))
												|| !resourceStatus.getEditor().split(":")[1].equals(Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.ID, "", null))
												|| ( !resourceStatus.getFileStatus().split(":")[1].equals("0") && ( (null != resourceStatus.getSRId()) 
																													&& !"".equals(resourceStatus.getSRId())) 
																													&& !srId.equals(resourceStatus.getSRId())) ){
												enabledflg = false;
												break;
											}
										}else{
											enabledflg = false;
											break;
										}
										selectedList.add(SelectedResource[i]);
									}
								}
							}
						}
					}
					if(enabledflg){
						chkInResources = dupliChk(selectedList);
						//chkInResources = (IResource[])selectedList.toArray(new IResource[selectedList.size()]);
						
						for (int x=1; x<chkInResources.length; x++){
							if(!chkInResources[x].getProject().equals(chkInResources[0].getProject())){
								enabledflg = false;
								break;
							}
						}
					}
				}
				action.setEnabled(enabledflg);
				action.setText("Check-&In");
				manager.add(action);
			}
		});
		//\uccb4\ud06c\uc544\uc6c3(0,G -> 5)
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				Action action = new Action() {
					public void run() {
						super.run();
						// TODO do something
						if( !isOtherProject() ) {
							outAction.run();
						}
					}
				};

				boolean enabledflg = false;

				IStructuredSelection sel = (IStructuredSelection)viewer.getTreeViewer().getSelection();
				if(selectedList.size() > 0) selectedList.removeAll(selectedList);
				if(sel.size() > 0) enabledflg = true;
				
				if(enabledflg){
					for (Iterator it =((IStructuredSelection) sel).iterator();it.hasNext();){
						Object select_obj = it.next();
						if (select_obj != null && select_obj instanceof IResource){
							for(int i=0;i<SelectedResource.length;i++){
								if (((IResource)select_obj).getType()==IResource.FILE){
									if(((IResource)select_obj).equals(SelectedResource[i])){
										resourceStatus = EcamsProviderPlugin.getPlugin().getXmlStatusMgr().getStatus(SelectedResource[i]);
										if(resourceStatus != null) {
											if (!resourceStatus.isAuthority() || !resourceStatus.isLocked() 
													|| !resourceStatus.getFileStatus().split(":")[1].equals("0")){
												enabledflg = false;
												break;
											}
											selectedList.add(SelectedResource[i]);
										}else{
											enabledflg = false;
											break;
										}
									}
								}else if(((IResource)select_obj).getType()==IResource.FOLDER){
									if(SelectedResource[i].getParent().toString().indexOf(((IResource)select_obj).toString())>-1){
										resourceStatus = EcamsProviderPlugin.getPlugin().getXmlStatusMgr().getStatus(SelectedResource[i]);
										if(resourceStatus != null) {
											if (!resourceStatus.isAuthority() || !resourceStatus.isLocked() 
													|| !resourceStatus.getFileStatus().split(":")[1].equals("0")){
												enabledflg = false;
												break;
											}
											selectedList.add(SelectedResource[i]);
										}else{
											enabledflg = false;
											break;
										}
									}
								}else if(((IResource)select_obj).getType()==IResource.PROJECT){
									if(((IResource)select_obj).getProject().equals(SelectedResource[i].getProject())){
										resourceStatus = EcamsProviderPlugin.getPlugin().getXmlStatusMgr().getStatus(SelectedResource[i]);
										if(resourceStatus != null) {
											if (!resourceStatus.isAuthority() || !resourceStatus.isLocked() 
													|| !resourceStatus.getFileStatus().split(":")[1].equals("0") ){
												enabledflg = false;
												break;
											}
											selectedList.add(SelectedResource[i]);
										}else{
											enabledflg = false;
											break;
										}
									}
								}
							}
						}
					}
					if(enabledflg){
						chkOutResources = dupliChk(selectedList);
						//chkOutResources = (IResource[])selectedList.toArray(new IResource[selectedList.size()]);
						for (int x=1; x<chkOutResources.length; x++){
							if(!chkOutResources[x].getProject().equals(chkOutResources[0].getProject())){
								enabledflg = false;
								break;
							}
						}
					}
				}
				
				action.setEnabled(enabledflg);
				action.setText("Check-&Out");
				manager.add(action);
			}
		});
		//\uc774\uc804\ubc84\uc804 \uccb4\ud06c\uc544\uc6c3
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				Action action = new Action() {
					public void run() {
						super.run();
						// TODO do something
						if( !isOtherProject() ) {
							lastoutAction.run();
						}
					}
				};
				IStructuredSelection sel = (IStructuredSelection)viewer.getTreeViewer().getSelection();
				if(selectedList.size() > 0) selectedList.removeAll(selectedList);
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
			    	resourceStatus = EcamsProviderPlugin.getPlugin().getXmlStatusMgr().getStatus(selectResources[0]);
			    	if(resourceStatus == null) {
						enabledflg = false;
					}else{
						if (!resourceStatus.isAuthority() || !resourceStatus.isLocked() 
								|| !resourceStatus.getFileStatus().split(":")[1].equals("0")){
							enabledflg = false;
						}
					}
				}
				action.setEnabled(enabledflg);
				action.setText("&Previous Version Check-OUT");
				manager.add(action);
			}
		});
		//\uccb4\ud06c\uc544\uc6c3\ucde8\uc18c(5,8,G -> 0)
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				Action action = new Action() {
					public void run() {
						super.run();
						// TODO do something
						if( !isOtherProject() ) {
							cnclAction.run();
						}
					}
				};

				boolean enabledflg = false;

				IStructuredSelection sel = (IStructuredSelection)viewer.getTreeViewer().getSelection();
				if(selectedList.size() > 0) selectedList.removeAll(selectedList);
				if(sel.size() > 0) enabledflg = true;
				
				if(enabledflg){
					for (Iterator it =((IStructuredSelection) sel).iterator();it.hasNext();){
						Object select_obj = it.next();
						if (select_obj != null && select_obj instanceof IResource){
							for(int i=0;i<SelectedResource.length;i++){
								if (((IResource)select_obj).getType()==IResource.FILE){
									if(((IResource)select_obj).equals(SelectedResource[i])){
										resourceStatus = EcamsProviderPlugin.getPlugin().getXmlStatusMgr().getStatus(SelectedResource[i]);
										if(resourceStatus != null) {
											if ((!resourceStatus.getFileStatus().split(":")[1].equals("5")
													//&& !resourceStatus.getFileStatus().split(":")[1].equals("8")
													&& !resourceStatus.getFileStatus().split(":")[1].equals("B")
													&& !resourceStatus.getFileStatus().split(":")[1].equals("G")
													&& !resourceStatus.getFileStatus().split(":")[1].equals("E")
													)
												|| !resourceStatus.getEditor().split(":")[1].equals(Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.ID, "", null))){
												enabledflg = false;
												break;
											}
											selectedList.add(SelectedResource[i]);
										}else{
											enabledflg = false;
											break;
										}
									}
								}else if(((IResource)select_obj).getType()==IResource.FOLDER){
									if(SelectedResource[i].getParent().toString().indexOf(((IResource)select_obj).toString())>-1){
										resourceStatus = EcamsProviderPlugin.getPlugin().getXmlStatusMgr().getStatus(SelectedResource[i]);
										if(resourceStatus != null) {
											if ((!resourceStatus.getFileStatus().split(":")[1].equals("5")
													//&& !resourceStatus.getFileStatus().split(":")[1].equals("8")
													&& !resourceStatus.getFileStatus().split(":")[1].equals("B")
													&& !resourceStatus.getFileStatus().split(":")[1].equals("G")
													&& !resourceStatus.getFileStatus().split(":")[1].equals("E")
													)
												|| !resourceStatus.getEditor().split(":")[1].equals(Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.ID, "", null))){
												enabledflg = false;
												break;
											}
											selectedList.add(SelectedResource[i]);
										}else{
											enabledflg = false;
											break;
										}
									}
								}else if(((IResource)select_obj).getType()==IResource.PROJECT){
									if(((IResource)select_obj).getProject().equals(SelectedResource[i].getProject())){
										resourceStatus = EcamsProviderPlugin.getPlugin().getXmlStatusMgr().getStatus(SelectedResource[i]);
										if(resourceStatus != null) {
											if ((!resourceStatus.getFileStatus().split(":")[1].equals("5")
													//&& !resourceStatus.getFileStatus().split(":")[1].equals("8")
													&& !resourceStatus.getFileStatus().split(":")[1].equals("B")
													&& !resourceStatus.getFileStatus().split(":")[1].equals("G")
													&& !resourceStatus.getFileStatus().split(":")[1].equals("E")
													)
												|| !resourceStatus.getEditor().split(":")[1].equals(Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.ID, "", null))){
												enabledflg = false;
												break;
											}
											selectedList.add(SelectedResource[i]);
										}else{
											enabledflg = false;
											break;
										}
									}
								}
							}
						}
					}
					if(enabledflg){
						cnClResources = dupliChk(selectedList);
						//cnClResources = (IResource[])selectedList.toArray(new IResource[selectedList.size()]);
						for (int x=1; x<cnClResources.length; x++){
							if(!cnClResources[x].getProject().equals(cnClResources[0].getProject())){
								enabledflg = false;
								break;
							}
						}
					}
				}
				
				action.setEnabled(enabledflg);
				action.setText("&Cancel Check-OUT");
				manager.add(action);
			}
		});
		//\uc774\ub825\ubcf4\uae30
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				Action action = new Action() {
					public void run() {
						super.run();
						// TODO do something
						versionAction.run();
					}
				};
				IStructuredSelection sel = (IStructuredSelection)viewer.getTreeViewer().getSelection();
				if(selectedList.size() > 0) selectedList.removeAll(selectedList);
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
			    	resourceStatus = EcamsProviderPlugin.getPlugin().getXmlStatusMgr().getStatus(selectResources[0]);
					if((resourceStatus == null) || (resourceStatus.getLastVer() == 0 && resourceStatus.getTstVer() == 0)) enabledflg = false;
				}
				action.setEnabled(enabledflg);
				action.setText("Show &History");
				//action.setImageDescriptor(EcamsImages.getImageDescriptor("History"));
				action.setImageDescriptor(ImageUtil.getImageRegistry().getDescriptor("History"));
				manager.add(action);
			}
		});
		//\uc18c\uc2a4\ube44\uad50
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				Action action = new Action() {
					public void run() {
						super.run();
						// TODO do something
						diffAction.run();
					}
				};
				IStructuredSelection sel = (IStructuredSelection)viewer.getTreeViewer().getSelection();
				if(selectedList.size() > 0) selectedList.removeAll(selectedList);
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
			    	resourceStatus = EcamsProviderPlugin.getPlugin().getXmlStatusMgr().getStatus(selectResources[0]);
					if((resourceStatus == null) || resourceStatus.getLastVer() == 0 
						|| (resourceStatus.getRsrcinfo().toString().substring(9,10).equals("1"))){
						enabledflg = false;
					}
				}
				action.setEnabled(enabledflg);
				action.setText("Compare with &Each Other");
				manager.add(action);
			}
		});
		//\uc120\ud0dd\uac74\uc0ad\uc81c
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				Action action = new Action() {
					public void run() {
						super.run();
						// TODO do something
						deleteItemAction.run();
					}
				};
				
				id = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.ID, "", null);
				
				IStructuredSelection sel = (IStructuredSelection)viewer.getTreeViewer().getSelection();
 			   	boolean selflg = false;
				for (Iterator it =((IStructuredSelection) sel).iterator();it.hasNext();){
					Object select_obj = it.next();
					if (select_obj != null && select_obj instanceof IResource){
						if (((IResource)select_obj).getType()==IResource.FILE){
							IEcamsStatus resourceStatus = EcamsProviderPlugin.getPlugin().getXmlStatusMgr().getStatus((IResource)select_obj);
							if(resourceStatus != null){
								if((resourceStatus.getFileStatus().split(":")[1].equals("5") || resourceStatus.getFileStatus().split(":")[1].equals("B")
										|| resourceStatus.getFileStatus().split(":")[1].equals("G")) && resourceStatus.getEditor().split(":")[1].equals(id)
										&& resourceStatus.getSRId().equals(srId)
									){
									selflg = true;
									break;
								}
							}
						}else{
							selflg = false;
							break;
						}
					}
				}
				if(sel.size()==0) selflg = true;
				action.setEnabled(!selflg);
				//action.setEnabled(sel.size() > 0);
				action.setText("&Remove Selected List");
				//action.setImageDescriptor(EcamsImages.getImageDescriptor("SelDel"));
				action.setImageDescriptor(ImageUtil.getImageRegistry().getDescriptor("SelDel"));
				manager.add(action);
			}
		});
		
 		Menu menu = menuMgr.createContextMenu(viewer.getTreeViewer().getTree());
 		viewer.getTreeViewer().getTree().setMenu(menu);
 		getSite().registerContextMenu(menuMgr, viewer.getTreeViewer());
 		
    }
    
    public IResource[] dupliChk(List dupliList){
    	IResource[] tmpDuSrc = (IResource[])dupliList.toArray(new IResource[dupliList.size()]);
    	IResource[] tmpDuSrc2 = (IResource[])dupliList.toArray(new IResource[dupliList.size()]);
    	List<IResource> tmpDuList = new ArrayList<IResource>();
    	
    	
    	int j = 0;
    	int k = 0;
    	int l = 0;
    	for(j=0; j<tmpDuSrc.length; j++){
			IPath tmpLocation = tmpDuSrc[j].getLocation();
    		for(k=0; k<tmpDuSrc2.length; k++){
    			if(tmpDuSrc2[k].getLocation().equals(tmpLocation)){
    				if(tmpDuList.size()>0){
    					IResource[] tmpDusrc = (IResource[])tmpDuList.toArray(new IResource[tmpDuList.size()]);
    					boolean addFlg = false;
	    				for(l=0; l<tmpDusrc.length; l++){
	    					if(tmpDusrc[l].getLocation().equals(tmpDuSrc2[k].getLocation())){
	    						addFlg = true;
	    						break;
	    					}
	    				}
	    				if(!addFlg){
	    					tmpDuList.add(tmpDuSrc2[k]);
	    				}
    				}else{
    					tmpDuList.add(tmpDuSrc2[k]);
    				}
    			}
    		}
    	}
    	
    	return (IResource[])tmpDuList.toArray(new IResource[tmpDuList.size()]);
    }
    
    @Override
    public void setFocus() {
        // set the focus
    	viewer.setFocus();
    }
    
    public void addDropSupport(Control control) {
		int operations = DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_DEFAULT;
		DropTarget target = new DropTarget(control, operations);
		
		//final EditorInputTransfer editorInputTransfer = EditorInputTransfer.getInstance();
		//final TextTransfer textTransfer = TextTransfer.getInstance();
		final FileTransfer fileTransfer = FileTransfer.getInstance();
		Transfer[] transferTypes = new Transfer[] {fileTransfer};//editorInputTransfer, 
		target.setTransfer(transferTypes);
		
		target.addDropListener(new DropTargetListener() {
			public void dragEnter(DropTargetEvent event) 
			{
				if (event.detail == DND.DROP_DEFAULT) 
				{
					 if ((event.operations & DND.DROP_COPY) != 0)
					 	event.detail = DND.DROP_COPY;
					 else 
					 	event.detail = DND.DROP_NONE; 
				}
			}
			
			public void dragOver(DropTargetEvent event) {
			}
			
			public void dragOperationChanged(DropTargetEvent event) 
			{
				if (event.detail == DND.DROP_DEFAULT) 
				{
					 if ((event.operations & DND.DROP_COPY) != 0)
					 	event.detail = DND.DROP_COPY;
					 else 
					 	event.detail = DND.DROP_NONE; 
				}
			}
			
			public void dragLeave(DropTargetEvent event) {
			}
			
			public void dropAccept(DropTargetEvent event) {
			}
			
			public void drop(DropTargetEvent event) 
			{
				
				logger.error("ResourceView   Start");
				for(int data=0; data<event.dataTypes.length; data++){
					if (fileTransfer.isSupportedType(event.currentDataType)){
						String[] strDat = (String[])event.data;
						for(int k=0; k<strDat.length; k++){
							String filename = strDat[k];
							logger.info(">>>>>>>> drop event.data["+filename+"]");
							IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
							String filepath = "";
							if(filename.indexOf(".istudiometa")>-1){
								logger.info(">>>>>>>> drop filename1[full:"+filename+",split:"+filename.split("\\\\")[0]+"]");
								if (filename.split("\\\\")[0].indexOf(":")>-1) {//C:, D:
									filepath = filename;
								} else {
									IProject project = root.getProject(filename.split("\\\\")[0]);
									logger.info(">>>>>>>> drop project["+project+"]");
									filename = filename.substring(filename.split("\\\\")[0].length());//tmpR.getName();
									logger.info(">>>>>>>> drop filename2["+filename+"]");
									filepath = project.getLocation()+filename;
									logger.info(">>>>>>>> drop filepath["+filepath+"]");
									project = null;
								}
							}else{
								filepath = filename;
							}
							
							while(filepath.indexOf("/") >=0){
								filepath = filepath.replace("/","\\");
			    			}
			    			
			    			while(filepath.indexOf("\\\\") >=0){
			    				filepath = filepath.replace("\\\\", "\\");
			    			}
	
			    			while(filepath.indexOf("\\") >=0){
			    				filepath = filepath.replace("\\", "/");
			    			}
							if(filepath != null){
								IResource tmpResource = root.getFileForLocation(new Path(filepath));
								logger.info(">>>>>>>> drop tmpResource["+tmpResource+"]");
								IProject project = root.getProject(tmpResource.getProject().getName());
								try{
									//System.out.println(project.getPersistentProperty(new QualifiedName("Properties","syscd")).split(":")[0]);
									Sysinfo = project.getPersistentProperty(new QualifiedName("Properties","sysinfo")).split(":")[0];
									Syscd2 = project.getPersistentProperty(new QualifiedName("Properties","syscd")).split(":")[0];
									if(Sysinfo.substring(9,10).equals("1")){
										chkModify.setSelection(true);
										srId = "";
										title_sr.setText("");
//										viewer.setDeleteViewer();
//										if(null != viewer.getTreeViewer().getInput()){
//											viewer.getTreeViewer().remove(viewer.getTreeViewer().getInput());
//										}
//										viewer.redraw();
										break;
									}else{
										chkModify.setSelection(false);
										break;
									}
								}catch(Exception e){
									e.getStackTrace();
								}
							}
						}
					}
				}
				
				
				if( !chkModify.getSelection() && (null == srId || "".equals(srId) || null == text.getText() || "".equals(text.getText())) ){
					MessageBox messageBox = new MessageBox(getSite().getShell(), SWT.OK);
		 			messageBox.setMessage("\ub0b4 \uc791\uc5c5\ubaa9\ub85d \ud0ed\uc5d0\uc11c \uc9c4\ud589\ud560 SR\uc744 \ub354\ube14\ud074\ub9ad\ud558\uc5ec \uc120\ud0dd\ud574 \uc8fc\uc2ed\uc2dc\uc624.\n" +
					"\ub2e4\uc2dc \uc120\ud0dd\ud55c \ud6c4\uc5d0\ub3c4 \ub3d9\uc77c\ud55c \uba54\uc2dc\uc9c0\uac00 \ub098\uc628\ub2e4\uba74\n" +
					"\ud574\ub2f9 SR\uc740 \ud504\ub85c\uadf8\ub7a8\uc744 \ucd94\uac00\ud558\uc5ec \uc2e0\uccad\ud560 \uc218 \uc5c6\ub294 \uc0c1\ud0dc\uc785\ub2c8\ub2e4.");
					messageBox.setText("SR-ID is null");
		 			messageBox.open();
					return;
				}
				
				String ip = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.IP, "", null);
				String port = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PORT, "", null);
				String id = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.ID, "", null);
				String passwd = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PASSWD, "", null);
				
				if(id == null || passwd == null){
					MessageBox messageBox = new MessageBox(new Shell());
					messageBox.setMessage("Preferences\uc5d0\uc11c ID\ub97c \uc785\ub825\ud558\uc138\uc694.");
					messageBox.setText("To Do Login");
					messageBox.open();
					return;
				}else if(ip == null || port == null){
					MessageBox messageBox = new MessageBox(new Shell());
					messageBox.setMessage("Preferences\uc5d0\uc11c IP\ub97c \uc785\ub825\ud558\uc138\uc694.");
					messageBox.setText("IS NOT CONNECTED");
					messageBox.open();
					return;
				}
				
				EcamsMessage.Builder builder_msg = EcamsMessage.newBuilder();
				builder_msg.setMsgtype("SYSINFOLIST_USER_GET");

				UserInfo.Builder userinfo_builder = UserInfo.newBuilder();

				userinfo_builder.setId(id);
				userinfo_builder.setPasswd(passwd);
				
				builder_msg.setUserinfo(userinfo_builder.build());
			
				EcamsClient syncClient = new EcamsClient(ip,port);
				ReturnMsg returnMsg = syncClient.sendMsg(builder_msg.build());
				
				if (returnMsg.getReturnval() == 0){
					for(int i=0;i<returnMsg.getEcamsmsg().getSysinfolist().getSysinfoCount();i++){
						if(Syscd2.equals(returnMsg.getEcamsmsg().getSysinfolist().getSysinfo(i).getSyscd())){
							if(!Sysinfo.equals(returnMsg.getEcamsmsg().getSysinfolist().getSysinfo(i).getSysinfo())){
								MessageBox messageBox = new MessageBox(new Shell());
								messageBox.setMessage("["+returnMsg.getEcamsmsg().getSysinfolist().getSysinfo(i).getSysmsg()+"]" +
										              "\uc2dc\uc2a4\ud15c\uc815\ubcf4\uac00 \ubcc0\uacbd\ub418\uc5c8\uc2b5\ub2c8\ub2e4. \ud615\uc0c1\uad00\ub9ac \ud504\ub85c\uc81d\ud2b8\ub97c \uc7ac \uc5f0\uacb0 \ud558\uc2ed\uc2dc\uc624.\n" +
													  "Properties -> eCAMS Plugin -> \uc2dc\uc2a4\ud15c \uc7ac \uc5f0\uacb0");
								messageBox.setText("Re Connection");
								messageBox.open();
								return;
							}
						}
					}
				}
				
				if(isMySR()){
					for(int data=0; data<event.dataTypes.length; data++){
						if (fileTransfer.isSupportedType(event.currentDataType)){
							String[] strDat = (String[])event.data;
							
							if(strDat != null){
								for(int k=0; k<strDat.length; k++){
									List<IResource> tmplist = new ArrayList<IResource>();
									
									String filename = strDat[k];
									
									IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
									String filepath = "";
																		
									if(filename.indexOf(".istudiometa")>-1){
										logger.info(">>>>>>>> drop filename1[full:"+filename+",split:"+filename.split("\\\\")[0]+"]");
										if (filename.split("\\\\")[0].indexOf(":")>-1) {//C:, D:
											filepath = filename;
										} else {
											IProject project = root.getProject(filename.split("\\\\")[0]);
											
											filename = filename.substring(filename.split("\\\\")[0].length());//tmpR.getName();
											filepath = project.getLocation()+filename;
											project = null;
											
		//									logger.info("ResourceView  1 filename:" + filename);
		//									logger.info("ResourceView  2 filepath:" + filepath);
										}
									}else{
										filepath = filename;
									}
									
									while(filepath.indexOf("/") >=0){
										filepath = filepath.replace("/","\\");
					    			}
					    			
					    			while(filepath.indexOf("\\\\") >=0){
					    				filepath = filepath.replace("\\\\", "\\");
					    			}
			
					    			while(filepath.indexOf("\\") >=0){
					    				filepath = filepath.replace("\\", "/");
					    			}
					    			
									
									if(filepath != null){
										IResource tmpResource = root.getFileForLocation(new Path(filepath));
										
										IProject project = root.getProject(tmpResource.getProject().getName());
										if(!project.isAccessible()){
											MessageBox messageBox = new MessageBox(new Shell(), SWT.OK);
											messageBox.setMessage("Project is closed.");
											messageBox.open();
											continue;
										}
										if(!EcamsRepositoryProvider.isManagedByEcams(project)){
											MessageBox messageBox = new MessageBox(new Shell(), SWT.OK);
											messageBox.setMessage("Project - Properties\uc5d0\uc11c \uc2dc\uc2a4\ud15c\uacfc \uc5c5\ubb34 \uc5f0\uacb0\uc744 \ud558\uc2ed\uc2dc\uc624.");
											messageBox.open();
											continue;
										}
										try{
											if(!"true".equals(project.getPersistentProperty(new QualifiedName("Properties","useyn")))){
												MessageBox messageBox = new MessageBox(new Shell(), SWT.OK);
												messageBox.setMessage("properties\uc5d0\uc11c \uc2dc\uc2a4\ud15c\uacfc \uc5c5\ubb34\ub97c \uc5f0\uacb0\ud574\uc8fc\uc2ed\uc2dc\uc624.");
												messageBox.open();
												continue;
											}
											
											if( !chkModify.getSelection() && project.getPersistentProperty(new QualifiedName("Properties","sysinfo")).substring(9,10).equals("1") ){
												//SR\uc0ac\uc6a9 && \uc2dc\uc2a4\ud15c\uc18d\uc131 SR\uc0ac\uc6a9\uc548\ud568
												MessageBox messageBox = new MessageBox(new Shell(), SWT.OK);
												messageBox.setMessage("SR\uc744 \uc0ac\uc6a9\ud558\uc9c0\uc54a\ub294 \uc2dc\uc2a4\ud15c\uc785\ub2c8\ub2e4. \nSR\uc0ac\uc6a9\uc548\ud568\uc744 \uccb4\ud06c\ud558\uc2e0 \ud6c4 \ud504\ub85c\uadf8\ub7a8\uc744 \ub04c\uc5b4\ub193\uc73c\uc2ed\uc2dc\uc624.");
												messageBox.open();
												continue;
											}else if( chkModify.getSelection() && project.getPersistentProperty(new QualifiedName("Properties","sysinfo")).substring(9,10).equals("0") ){
												//SR\uc0ac\uc6a9\uc548\ud568 && \uc2dc\uc2a4\ud15c\uc18d\uc131 SR\uc0ac\uc6a9
												MessageBox messageBox = new MessageBox(new Shell(), SWT.OK);
												messageBox.setMessage("SR\uc744 \uc0ac\uc6a9\ud558\ub294 \uc2dc\uc2a4\ud15c\uc785\ub2c8\ub2e4. \nSR-ID\ub97c \ub0b4\uc791\uc5c5\ubaa9\ub85d\uc5d0\uc11c \ub354\ube14\ud074\ub9ad \ud6c4 \ud504\ub85c\uadf8\ub7a8\uc744 \ub04c\uc5b4\ub193\uc73c\uc2ed\uc2dc\uc624.");
												messageBox.open();
												continue;
											}
										}catch(Exception e){
											continue;
										}
										
										List<IResource> fileList = new ArrayList<IResource>();
						    			File tmpfile = new File(filepath);
						    			
						    			if(tmpfile.isDirectory()){
						    				File[] fileLst = tmpfile.listFiles();
						    				fileLst = getFile(fileLst);
						    				for(int i=0;i<fileLst.length;i++){
						    					if(fileLst[i].isFile()){
						    						tmpResource = root.getFileForLocation(new Path(fileLst[i].getPath()));
						    						fileList.add(tmpResource);
						    					}
						    				}
						    			}else{
						    				fileList.add(tmpResource);
						    			}
						    			tmpfile = null;
						    			
										resources = null;
										resources = (IResource[]) fileList.toArray(new IResource[fileList.size()]);
										fileList = null;
										
										resources2 = null;
										resources2 = viewer.getSelectedResources();
	
										if(null != resources2){
											for(int i=0;i<resources2.length; i++){
												tmplist.add(resources2[i]);
											}
											boolean duplFlg = false;
											for(int i=0;i<resources.length; i++){
												duplFlg = false;
												for(int j=0; j<resources2.length; j++){
													if(resources2[j].getLocation().equals(resources[i].getLocation())){
														duplFlg = true;
														break;
													}
												}
												if(!duplFlg){
													tmplist.add(resources[i]);
													
												}
											}
										}else{
											if(resources != null){
												for(int i=0;i<resources.length; i++){
													tmplist.add(resources[i]);
												}
											}
										}
										if(tmplist != null){
											SelectedResource = null;
											SelectedResource = (IResource[])tmplist.toArray(new IResource[tmplist.size()]);
											viewer.setDeleteViewer();
											if(null != viewer.getTreeViewer().getInput()){
												viewer.getTreeViewer().remove(viewer.getTreeViewer().getInput());
											}
											viewer.getTreeViewer().refresh();
											viewer.setResources(SelectedResource);
											viewer.redraw();
										}
										tmplist = null;
										resources = null;
										resources2 = null;
						    			tmpResource = null;
									}
								}
							}
						}
					}
					if(viewer.getSelectedResources().length>0){
						EcamsProviderPlugin.getPlugin().getJobManager().addJob(new SyncJob("eCAMS Sync",viewer.getSelectedResources(),"","NONE"));
					}
				}
			}
		});
		
    }
    
    private File[] getFile(File[] files){
    	List findResourceList= null;
		Set<File> addResources = new HashSet<File>();
    	for(int i=0;i<files.length;i++){
			if(files[i].isDirectory()){
				if(files[i].getPath().lastIndexOf(".deco")<0
						&& files[i].getPath().lastIndexOf(".svn")<0){
					
					File[] getfiles = files[i].listFiles();
			    	File[] retfile = null;
			    	retfile = getFile(getfiles);
			    	
					if (retfile != null){
						for (int j=0;j<retfile.length;j++){
							addResources.add(retfile[j]);
						}
					}
				}
			}else if(files[i].isFile()){
				if (files[i].getName().lastIndexOf(".deco")<0
						//&& files[i].getName().lastIndexOf(".class")<0
						&& files[i].getPath().lastIndexOf(".svn")<0){
					addResources.add(files[i]);
				}
			}
    	}
    	
    	findResourceList = new ArrayList(addResources);
    	
    	if(findResourceList != null){
			return (File[])findResourceList.toArray(new File[findResourceList.size()]);
		}else{
			return null;
		}
    }
	
    public boolean isOtherProject(){
		boolean otherPrjFlag = false;
		int selectedListCnt = selectedList.size();
		
		for(int i=0; i<selectedListCnt; i++) {
			for(int j=i+1; j<selectedListCnt; j++) {
				if(!((IResource)selectedList.get(i)).getProject().equals(((IResource)selectedList.get(j)).getProject())) {
					otherPrjFlag = true;
					break;
				}
			}
			
			if(otherPrjFlag) {
				break;
			}
		}
		
		if(otherPrjFlag) {
			MessageBox messageBox = new MessageBox(new Shell());
			messageBox.setMessage("\ud558\ub098\uc758 \ud504\ub85c\uc81d\ud2b8 \ub9ac\uc18c\uc2a4\ub9cc \uc120\ud0dd\ud558\uc138\uc694.");
			messageBox.open();
		}else if(!chkModify.getSelection() && (srId == null || srId == "") ){
			MessageBox messageBox = new MessageBox(new Shell());
			messageBox.setMessage("\ub0b4 \uc791\uc5c5\ubaa9\ub85d \ud0ed\uc5d0\uc11c \uc9c4\ud589\ud560 SR\uc744 \ub354\ube14\ud074\ub9ad\ud558\uc5ec \uc120\ud0dd\ud574 \uc8fc\uc2ed\uc2dc\uc624.\n" +
					"\ub2e4\uc2dc \uc120\ud0dd\ud55c \ud6c4\uc5d0\ub3c4 \ub3d9\uc77c\ud55c \uba54\uc2dc\uc9c0\uac00 \ub098\uc628\ub2e4\uba74\n" +
					"\ud574\ub2f9 SR\uc740 \ud504\ub85c\uadf8\ub7a8\uc744 \ucd94\uac00\ud558\uc5ec \uc2e0\uccad\ud560 \uc218 \uc5c6\ub294 \uc0c1\ud0dc\uc785\ub2c8\ub2e4.");
			messageBox.setText("SR-ID is null");
			messageBox.open();
			
			otherPrjFlag = true;
		}
		
    	return otherPrjFlag;
    }
    
    private Boolean isMySR(){
    	boolean ok = false;
		if(chkModify.getSelection()){
			ok = true;
		}else{
	    	ip = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.IP, "", null);
			port = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PORT, "", null);
			id = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.ID, "", null);
			passwd = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PASSWD, "", null);
			
	    	EcamsMessage.Builder builder_msg = EcamsMessage.newBuilder();
			builder_msg.setMsgtype("ISMYSR");
	
			UserInfo.Builder userinfo_builder = UserInfo.newBuilder();
			userinfo_builder.setId(id);
			userinfo_builder.setPasswd(passwd);
			builder_msg.setUserinfo(userinfo_builder.build());
			
			SRInfo.Builder srinfo_builder = SRInfo.newBuilder();
			srinfo_builder.setCcSRId(srId);
			builder_msg.setSrinfo(srinfo_builder.build());
			
			EcamsClient syncClient = new EcamsClient(ip,port);
			ReturnMsg returnMsg = syncClient.sendMsg(builder_msg.build());
			
			if (returnMsg.getReturnval() == 0) {
				ok = true;
			} else {
				MessageBox messageBox = new MessageBox(getSite().getShell(), SWT.OK);
				messageBox.setMessage("["+srId+"]" + srTitle + "\n" +
						"\uc120\ud0dd\ud55c SR\uc758 \uac1c\ubc1c\ub2f4\ub2f9\uc790\uac00 \uc544\ub2c8\uac70\ub098, \uac1c\ubc1c\uac00\ub2a5\ud55c \uc0c1\ud0dc\uac00 \uc544\ub2d9\ub2c8\ub2e4." +
						"\n(" + returnMsg.getReturnStr() + ")");
				messageBox.setText("Invalid Access");
				messageBox.open();
			}
		}

		return ok;
    }
    
    public void setResourceView(IResource[] resources, String srId, String srTitle) {
    	this.srId = srId;
    	this.srTitle = srTitle;
    	this.SelectedResource = resources;
    	
    	chkModify.setSelection(false);
    	
    	if(srId.length() == 0 || srTitle.length() == 0){
    		text.setText("");
    	}else{
    		text.setText("["+srId+"] "+srTitle);
    	}
    	if(SelectedResource == null){
			viewer.setDeleteViewer();
    	}else{
    		viewer.setResources(this.SelectedResource);
    	}
    	viewer.getTreeViewer().refresh();
    }
}