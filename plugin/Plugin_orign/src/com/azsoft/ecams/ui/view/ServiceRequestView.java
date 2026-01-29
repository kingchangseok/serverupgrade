package com.azsoft.ecams.ui.view;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import com.azsoft.ecams.core.EcamsProviderPlugin;
import com.azsoft.ecams.core.jobs.SyncJob;
import com.azsoft.ecams.core.resource.EcamsRepositoryProvider;
import com.azsoft.ecams.image.ImageUtil;
import com.azsoft.ecams.properties.IProperty;
import com.azsoft.ecams.proto.ProtoEcams.EcamsMessage;
import com.azsoft.ecams.proto.ProtoEcams.FileData;
import com.azsoft.ecams.proto.ProtoEcams.ReturnMsg;
import com.azsoft.ecams.proto.ProtoEcams.SRInfo;
import com.azsoft.ecams.proto.ProtoEcams.UserInfo;
import com.azsoft.ecams.socket.EcamsClient;

public class ServiceRequestView extends ViewPart{
	private Logger logger = Logger.getLogger(this.getClass());
	private Composite container;
	private Table svrlist;
	
	private String ip,port,id,passwd;
	private TableViewer viewer;
	ReturnMsg returnMsg;
	public Action refreshAction;
//	private Listener refresh_Listener;

	//final Display display = Display.getCurrent();
	//Color blue = display.getSystemColor(SWT.COLOR_BLUE);
	//Color black = display.getSystemColor(SWT.COLOR_BLACK);
	
    public ServiceRequestView() {
		ip = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.IP, "", null);
		port = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PORT, "", null);
		id = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.ID, "", null);
		passwd = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PASSWD, "", null);
		
		if(ip == null || port == null || id == null || passwd == null){
			return;
		}
    }
	
    @Override
    public void createPartControl(Composite parent) {
 //   	setupCallbacks();
    	
		container = new Composite(parent, SWT.NULL);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		container.setLayout(tableColumnLayout);

		viewer = new TableViewer(container, SWT.BORDER | SWT.FULL_SELECTION);
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				// TODO Auto-generated method stub
				
				IWorkbenchPage aPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

				if (aPage != null){
					try {
						TableItem[] children = viewer.getTable().getItems();
						int sIndex = viewer.getTable().getSelectionIndex();
						
						if(children[sIndex] != null){														
							EcamsMessage.Builder builder_msg = EcamsMessage.newBuilder();
							builder_msg.setMsgtype("GETRESOURCES");

							UserInfo.Builder userinfo_builder = UserInfo.newBuilder();
							userinfo_builder.setId(id);
							userinfo_builder.setPasswd(passwd);
							builder_msg.setUserinfo(userinfo_builder.build());
							
							SRInfo.Builder srinfo_builder = SRInfo.newBuilder();
							srinfo_builder.setCcEditor(children[sIndex].getText(7).toString());
							srinfo_builder.setCcSRId(children[sIndex].getText(1).toString());
							builder_msg.setSrinfo(srinfo_builder.build());
							
							EcamsClient syncClient = new EcamsClient(ip,port);
							ReturnMsg returnMsg = syncClient.sendMsg(builder_msg.build());
							
							if (returnMsg.getReturnStr().startsWith("SOCKERR")){
								return;
							}
							
							if(returnMsg.getReturnval() == 1){
								MessageBox messageBox = new MessageBox(new Shell());
								messageBox.setMessage(returnMsg.getReturnStr());
								messageBox.setText("["+children[sIndex].getText(1).toString()+"]"+children[sIndex].getText(4).toString()+" ERROR");
								messageBox.open();
								return;
							}else{
								if(returnMsg.getReturnStr().equals("END")){
									aPage.showView("com.azsoft.ecams.ui.view.resourceview",null,IWorkbenchPage.VIEW_ACTIVATE);
									IViewReference viewReference = aPage.findViewReference("com.azsoft.ecams.ui.view.resourceview");
									
									if (viewReference != null) {
										IViewPart view = viewReference.getView(true);
										ResourceView showview = (ResourceView) view;
										showview.setFocus();
									
										showview.setResourceView(null, "", "");
									}
								}else{
									List vieweList = new ArrayList();
									vieweList.addAll(returnMsg.getEcamsmsg().getFiledatalist().getFiledatasList());
		
	
									List<IResource> addList = new ArrayList<IResource>();
									
					    			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
									for(int i=0; i<vieweList.size(); i++){
										FileData filedata = (FileData) vieweList.get(i);
										
										String projectName = filedata.getSysinfo().getPrjname();
										String filepath = root.getLocation().toString()+"/"+projectName+filedata.getPathinfo().getRelativitePath();
						    			
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
						    			
						    			if( null != (IResource)root.getFileForLocation(new Path(filename)) ) {
						    				addList.add((IResource)root.getFileForLocation(new Path(filename)));
						    			}
									}

									if(addList.size()>0){
										EcamsProviderPlugin.getPlugin().getJobManager().addJob(new SyncJob("eCAMS Sync",(IResource[])addList.toArray(new IResource[addList.size()]),"","NONE"));
									}
									
									aPage.showView("com.azsoft.ecams.ui.view.resourceview",null,IWorkbenchPage.VIEW_ACTIVATE);
									IViewReference viewReference = aPage.findViewReference("com.azsoft.ecams.ui.view.resourceview");
									
									if (viewReference != null) {
										IViewPart view = viewReference.getView(true);
										ResourceView showview = (ResourceView) view;
										showview.setFocus();
										
										//IResource[] resources, String srId, String srTitle
										showview.setResourceView((IResource[])addList.toArray(new IResource[addList.size()]), 
												 				  children[sIndex].getText(1).toString(), 
//												 				  children[sIndex].getText(4).toString());
																  children[sIndex].getText(2).toString());
									}
								}
							}
						}
					} catch (PartInitException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		//viewer.getControl().getDisplay().addFilter(SWT.KeyDown, refresh_Listener);
		
		svrlist = viewer.getTable();
		svrlist.setHeaderVisible(true);
		svrlist.setLinesVisible(true);


		TableViewerColumn viewercolumn1 = new TableViewerColumn(viewer, SWT.NONE);
		TableColumn column1 = viewercolumn1.getColumn();
		tableColumnLayout.setColumnData(column1, new ColumnPixelData(35, true, true));
		column1.setText("No.");
        column1.setAlignment(SWT.RIGHT);

        TableViewerColumn viewercolumn2 = new TableViewerColumn(viewer, SWT.NONE);
		final TableColumn column2 = viewercolumn2.getColumn();
		tableColumnLayout.setColumnData(column2, new ColumnPixelData(200, true, true));
		column2.setText("SRID");
        column2.setAlignment(SWT.CENTER);

        TableViewerColumn viewercolumn11 = new TableViewerColumn(viewer, SWT.NONE);
		TableColumn column11 = viewercolumn11.getColumn();
		tableColumnLayout.setColumnData(column11, new ColumnPixelData(450, true, true));
		column11.setText("\uc81c\ubaa9"); 
        column11.setAlignment(SWT.LEFT);

        TableViewerColumn viewercolumn3 = new TableViewerColumn(viewer, SWT.NONE);
		TableColumn column3 = viewercolumn3.getColumn();
		tableColumnLayout.setColumnData(column3, new ColumnPixelData(120, true, true));
		column3.setText("\uc694\uccad\uc778"); 
        column3.setAlignment(SWT.CENTER);
        
        TableViewerColumn viewercolumn4 = new TableViewerColumn(viewer, SWT.NONE);
		TableColumn column4 = viewercolumn4.getColumn();
		tableColumnLayout.setColumnData(column4, new ColumnPixelData(120, true, true));
		column4.setText("\uc0c1\ud0dc");
		column4.setAlignment(SWT.LEFT);
        
        //TableViewerColumn viewercolumn4 = new TableViewerColumn(viewer, SWT.NONE);
		//TableColumn column4 = viewercolumn4.getColumn();
		//tableColumnLayout.setColumnData(column4, new ColumnPixelData(120, true, true));
		//column4.setText("\ubd84\ub958\uc720\ud615");
		//column4.setAlignment(SWT.LEFT);
        
        //TableViewerColumn viewercolumn8 = new TableViewerColumn(viewer, SWT.NONE);
		//TableColumn column8 = viewercolumn8.getColumn();
		//tableColumnLayout.setColumnData(column8, new ColumnPixelData(120, true, true));
		//column8.setText("\ubcc0\uacbd\uc885\ub958");
		//column8.setAlignment(SWT.LEFT);
        
		//TableViewerColumn viewercolumn5 = new TableViewerColumn(viewer, SWT.NONE);
		//TableColumn column5 = viewercolumn5.getColumn();
		//tableColumnLayout.setColumnData(column5, new ColumnPixelData(120, true, true));
		//column5.setText("\uc791\uc5c5\uc21c\uc704");
		//column5.setAlignment(SWT.CENTER);

		TableViewerColumn viewercolumn6 = new TableViewerColumn(viewer, SWT.NONE);
		TableColumn column6 = viewercolumn6.getColumn();
		column6.setResizable(false);
		tableColumnLayout.setColumnData(column6, new ColumnPixelData(0, false, false));
		
		getSRList();
		createActions();
		initializeToolBar();
    }
    
//    public void setupCallbacks() {
//		refresh_Listener = new Listener() {
//			
//			@Override
//			public void handleEvent(Event event) {
//				// TODO Auto-generated method stub
//				if( viewer.getControl().isFocusControl() && event.keyCode == SWT.F5 ) {
//					//System.out.println("serviceRequest viewer refresh!");
//					refreshAction.run();
//				}
//			}
//		};
//	}
    
	public void getSRList() {
		try{
			ip = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.IP, "", null);
			port = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PORT, "", null);
			id = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.ID, "", null);
			passwd = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PASSWD, "", null);
			if(ip == null || port == null || id == null || passwd == null){
				return;
			}
			
			String tool = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.TOOL, "", null);
			
			//tool = null;
			if(null == tool || "".equals(tool)){
				MessageDialog.openError(new Shell(),"Tool\uad6c\ubd84 \ud655\uc778","Preferences\uc5d0\uc11c \uac1c\ubc1c \ud234\uc744 \uc120\ud0dd\ud558\uc2ed\uc2dc\uc624.");
				return;
			}
			
			svrlist.removeAll();
			
			String isAdmin = "N";
			
			EcamsMessage.Builder builder_msg = EcamsMessage.newBuilder();
			builder_msg.setMsgtype("ADMIN");

			UserInfo.Builder userinfo_builder = UserInfo.newBuilder();
			userinfo_builder.setId(id);
			userinfo_builder.setPasswd(passwd);
			
			builder_msg.setUserinfo(userinfo_builder.build());

			EcamsClient syncClient = new EcamsClient(ip,port);
			ReturnMsg returnMsg = syncClient.sendMsg(builder_msg.build());
			
			if(0 == returnMsg.getReturnval()){
				if("1".equals(returnMsg.getReturnStr())) {
					isAdmin = "Y";
				}
			}else if (returnMsg.getReturnStr().startsWith("SOCKERR")){
				return;
			}

			builder_msg = EcamsMessage.newBuilder();
			builder_msg.setMsgtype("SRLIST_GET");

			builder_msg.setUserinfo(userinfo_builder.build());			
			
			SRInfo.Builder srinfo_builder = SRInfo.newBuilder();
			srinfo_builder.setCcEditor(id);
			srinfo_builder.setIsAdmin(isAdmin);
			
			builder_msg.setTooltype(tool);
			builder_msg.setSrinfo(srinfo_builder.build());
			
			syncClient = new EcamsClient(ip,port);
			
			returnMsg = syncClient.sendMsg(builder_msg.build());

			svrlist.removeAll();
			
			if(returnMsg.getReturnval() == 0){
				for(int i=0;i<returnMsg.getEcamsmsg().getSrinfolist().getSrinfoCount();i++){
					TableItem item = new TableItem(svrlist, SWT.NONE);
					int c = 0;
					item.setText(c++, Integer.toString(i+1));
					item.setText(c++, returnMsg.getEcamsmsg().getSrinfolist().getSrinfo(i).getCcSRId());
					item.setText(c++, returnMsg.getEcamsmsg().getSrinfolist().getSrinfo(i).getCcTitle());
					item.setText(c++, returnMsg.getEcamsmsg().getSrinfolist().getSrinfo(i).getCcEditorName());
					item.setText(c++, returnMsg.getEcamsmsg().getSrinfolist().getSrinfo(i).getCcCattype());
					item.setText(c++, returnMsg.getEcamsmsg().getSrinfolist().getSrinfo(i).getCcChgtype());
					item.setText(c++, returnMsg.getEcamsmsg().getSrinfolist().getSrinfo(i).getCcWorkrank());
					item.setText(c++, returnMsg.getEcamsmsg().getSrinfolist().getSrinfo(i).getCcEditor());
					item.setText(c++, returnMsg.getEcamsmsg().getSrinfolist().getSrinfo(i).getCcSyscd());
				}
			}
			
			svrlist.redraw();
		}catch(Exception e){
			e.printStackTrace();
		}		
	}
		
	public void createActions(){
		refreshAction = new Action("Reload ServiceRequest List") {
			public void run() {
				getSRList();
				viewer.getTable().redraw();
				//viewer.refresh();
			}
		};
	 	CreatePopupMenu();
	}    
	
    private void initializeToolBar() {
        IToolBarManager toolbarManager=getViewSite().getActionBars().getToolBarManager();
        
        toolbarManager.add(refreshAction);
        
        //refreshAction.setImageDescriptor(EcamsImages.getImageDescriptor("Refresh"));
        refreshAction.setImageDescriptor(ImageUtil.getImageRegistry().getDescriptor("Refresh"));
        refreshAction.setToolTipText("Reload ServiceRequest List");
        
        refreshAction.setEnabled(true);
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
						refreshAction.run();
					}
				};
				action.setEnabled(true);
				action.setImageDescriptor(ImageUtil.getImageRegistry().getDescriptor("Refresh"));
				action.setText("&Reload SR List");
				manager.add(action);
			}
		});
		
		
 		Menu menu = menuMgr.createContextMenu(viewer.getControl());
 		viewer.getControl().setMenu(menu);
 		getSite().registerContextMenu(menuMgr, viewer);
    }
    
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}
}