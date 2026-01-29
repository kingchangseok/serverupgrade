package com.azsoft.ecams.ui.dialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.internal.runtime.IRuntimeConstants;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.SWT;
import org.eclipse.team.internal.ui.IHelpContextIds;
import org.eclipse.team.internal.ui.history.DialogHistoryPageSite;
import org.eclipse.team.ui.history.HistoryPage;
import org.eclipse.team.ui.history.IHistoryCompareAdapter;
import org.eclipse.team.ui.history.IHistoryPageSite;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.core.runtime.IPath;

import com.azsoft.ecams.core.CommandExecuter;
import com.azsoft.ecams.core.EcamsProviderPlugin;
import com.azsoft.ecams.popmenu.ShowHistoryPageSource;
import com.azsoft.ecams.properties.IProperty;
import com.azsoft.ecams.proto.ProtoEcams.EcamsMessage;
import com.azsoft.ecams.proto.ProtoEcams.FileData;
import com.azsoft.ecams.proto.ProtoEcams.ReturnMsg;
import com.azsoft.ecams.proto.ProtoEcams.UserInfo;
import com.azsoft.ecams.socket.EcamsClient;
import com.azsoft.ecams.ui.compare.Utilities;
import com.azsoft.ecams.util.file.EGzip;
import org.eclipse.swt.custom.StyledText;


public class ShowHistoryPage extends HistoryPage {
	private Logger logger = Logger.getLogger(this.getClass());
	private String filename,itemid,rsrcinfo;
	private IResource[] inputResource;
	private Composite container;
	private Table svrlist;
	
	private String ip,port,id,passwd;
	private TableViewer viewer;
	private SashForm sashForm;
	private StyledText styledText;
	
	private MouseListener svrlistDoubleClick_Listener;
	ReturnMsg returnMsg;
	
	public ShowHistoryPage(IResource[] resource, String file, String item, String rsrcinfo) {
		setInputResource(resource);
		this.filename = file;
		this.itemid = item;
		this.rsrcinfo = rsrcinfo;
		// TODO Auto-generated constructor stub
	}
	
	
	@SuppressWarnings("restriction")
	@Override
	public void createControl(Composite parent) {
		// TODO Auto-generated method stub
		ip = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.IP, "", null);
		port = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PORT, "", null);
		id = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.ID, "", null);
		passwd = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PASSWD, "", null);
		
		
		setupCallbacks();
		
		//createTableViewer(parent);
		sashForm = new SashForm(parent, SWT.VERTICAL);
		container = new Composite(sashForm, SWT.NULL);
		//container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		container.setLayout(tableColumnLayout);
		
		viewer = new TableViewer(container, SWT.BORDER | SWT.FULL_SELECTION);
		
		svrlist = viewer.getTable();
		svrlist.setHeaderVisible(true);
		svrlist.setLinesVisible(true);
		svrlist.addMouseListener(svrlistDoubleClick_Listener);
		
		
		TableViewerColumn viewercolumn1 = new TableViewerColumn(viewer, SWT.NONE);
		TableColumn column1 = viewercolumn1.getColumn();
		tableColumnLayout.setColumnData(column1, new ColumnPixelData(100, true, true));
		column1.setText("\uc2e0\uccad\ubc88\ud638");
		column1.setAlignment(SWT.CENTER);
		
		TableViewerColumn viewercolumn9 = new TableViewerColumn(viewer, SWT.NONE);
		TableColumn column9 = viewercolumn9.getColumn();
		tableColumnLayout.setColumnData(column9, new ColumnPixelData(110, true, true));
		column9.setText("SR-ID");
		column9.setAlignment(SWT.CENTER);
		
		TableViewerColumn viewercolumn10 = new TableViewerColumn(viewer, SWT.NONE);
		TableColumn column10 = viewercolumn10.getColumn();
		tableColumnLayout.setColumnData(column10, new ColumnPixelData(110, true, true));
		column10.setText("SR\uc81c\ubaa9");
		column10.setAlignment(SWT.CENTER);
		
        TableViewerColumn viewercolumn3 = new TableViewerColumn(viewer, SWT.NONE);
        TableColumn column3 = viewercolumn3.getColumn();
        tableColumnLayout.setColumnData(column3, new ColumnPixelData(80, true, true));
        column3.setText("\uc2e0\uccad\uc790");
		column3.setAlignment(SWT.CENTER);
						        
		TableViewerColumn viewercolumn4 = new TableViewerColumn(viewer, SWT.NONE);
		TableColumn column4 = viewercolumn4.getColumn();
		tableColumnLayout.setColumnData(column4, new ColumnPixelData(120, true, true));
		column4.setText("\uc2e0\uccad\uad6c\ubd84");
		column4.setAlignment(SWT.CENTER);
		
		TableViewerColumn viewercolumn5 = new TableViewerColumn(viewer, SWT.NONE);
		TableColumn column5 = viewercolumn5.getColumn();
		tableColumnLayout.setColumnData(column5, new ColumnPixelData(150, true, true));
		column5.setText("\uc644\ub8cc\uc77c\uc2dc");
		column5.setAlignment(SWT.CENTER);
		
		TableViewerColumn viewercolumn6 = new TableViewerColumn(viewer, SWT.NONE);
		TableColumn column6 = viewercolumn6.getColumn();
		tableColumnLayout.setColumnData(column6, new ColumnPixelData(60, true, true));
		column6.setText("\ubc84\uc804");
		column6.setAlignment(SWT.CENTER);
		
		TableViewerColumn viewercolumn2 = new TableViewerColumn(viewer, SWT.NONE);
		TableColumn column2 = viewercolumn2.getColumn();
		tableColumnLayout.setColumnData(column2, new ColumnPixelData(130, true, true));
		column2.setText("\uc2e0\uccad\uc77c\uc2dc"); 
		column2.setAlignment(SWT.CENTER);
		
		TableViewerColumn viewercolumn7 = new TableViewerColumn(viewer, SWT.NONE);
		TableColumn column7 = viewercolumn7.getColumn();
		tableColumnLayout.setColumnData(column7, new ColumnPixelData(180, true, true));
		column7.setText("\ubcc0\uacbd\uc0ac\uc720");
		column7.setAlignment(SWT.CENTER);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(sashForm, IHelpContextIds.LOCAL_HISTORY_PAGE);
		
		Composite detail_container = new Composite(sashForm, SWT.NONE);
		detail_container.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		styledText = new StyledText(detail_container, SWT.BORDER | SWT.READ_ONLY | SWT.V_SCROLL);
		styledText.setEditable(false);
		sashForm.setWeights(new int[] {392, 74});
        
		setHistoryList();
		
		IHistoryPageSite parentSite = getHistoryPageSite();
		if (parentSite != null && parentSite instanceof DialogHistoryPageSite && viewer != null){
			parentSite.setSelectionProvider(viewer);
		}
	}
	
	
	
	public void setHistoryList(){
		if(itemid.length() > 0){
			try{
				EcamsMessage.Builder builder_msg = EcamsMessage.newBuilder();
				builder_msg.setMsgtype("HISTORYLIST_GET");
				
				FileData.Builder filedata_builder = FileData.newBuilder();
				
				filedata_builder.setItemid(itemid);
				filedata_builder.setFilename(filename);
				
				builder_msg.setFiledata(filedata_builder.build());
				
				UserInfo.Builder userinfo_builder = UserInfo.newBuilder();
		
				userinfo_builder.setId(id);
				userinfo_builder.setPasswd(passwd);
				
				builder_msg.setUserinfo(userinfo_builder.build());
			
				EcamsClient syncClient = new EcamsClient(ip,port);
				returnMsg = syncClient.sendMsg(builder_msg.build());
				
				String acptno = "";
				if(returnMsg.getReturnval() == 0){
					for(int i=0;i<returnMsg.getEcamsmsg().getHistorylist().getHistorydataCount();i++){
						TableItem item = new TableItem(svrlist, SWT.NONE);
						int c = 0;
						acptno = returnMsg.getEcamsmsg().getHistorylist().getHistorydata(i).getCrAcptno();
						item.setText(c++, acptno.substring(0,4)+"-"+acptno.substring(4,6)+"-"+acptno.substring(6));
						item.setText(c++, returnMsg.getEcamsmsg().getHistorylist().getHistorydata(i).getFiledata().getSrinfo().getCcSRId());
						item.setText(c++, returnMsg.getEcamsmsg().getHistorylist().getHistorydata(i).getFiledata().getSrinfo().getCcTitle());
						item.setText(c++, returnMsg.getEcamsmsg().getHistorylist().getHistorydata(i).getCmUsername());
						item.setText(c++, returnMsg.getEcamsmsg().getHistorylist().getHistorydata(i).getCmCodename());
						item.setText(c++, returnMsg.getEcamsmsg().getHistorylist().getHistorydata(i).getCrPrcdate());
						item.setText(c++, returnMsg.getEcamsmsg().getHistorylist().getHistorydata(i).getCrVersion());
						item.setText(c++, returnMsg.getEcamsmsg().getHistorylist().getHistorydata(i).getCrAcptdate());
						if(returnMsg.getEcamsmsg().getHistorylist().getHistorydata(i).getCrEditcon().equals("-")){
							item.setText(c++, "");
						}else{
							item.setText(c++, returnMsg.getEcamsmsg().getHistorylist().getHistorydata(i).getCrEditcon());
						}
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}else{
			return;
		}
	}
	
	public void setupCallbacks(){
		svrlistDoubleClick_Listener = new MouseListener(){

			public void mouseDoubleClick(MouseEvent e) {
				// TODO Auto-generated method stub
				if (rsrcinfo.substring(9,10).equals("0")){ //\ubc14\uc774\ub108\ub9ac\uc544\ub2cc\uac83
					if (svrlist.getItemCount()>0){ //\ubc84\uc804\uc788\uace0 
						try{
							EcamsMessage.Builder builder_msg = EcamsMessage.newBuilder();
							
							FileData.Builder lastfiledata_builder = FileData.newBuilder();
							lastfiledata_builder.setItemid(itemid);
							
							//String version = returnMsg.getEcamsmsg().getHistorylist().getHistorydata(svrlist.getSelectionIndex()).getCrVersion().toString();
							String version = returnMsg.getEcamsmsg().getHistorylist().getHistorydata(svrlist.getSelectionIndex()).getCrDevenddt().toString();

							System.out.println("ShowHistoryPage version:"+version);
							
							lastfiledata_builder.setBasever(version);
							lastfiledata_builder.setVergbn("R");
							lastfiledata_builder.setFilename(filename);
			    			builder_msg = EcamsMessage.newBuilder();
				    		builder_msg.setMsgtype("GETLASTFILE");
				    		builder_msg.setFiledata(lastfiledata_builder.build());
				    		
				    		UserInfo.Builder userinfo_builder = UserInfo.newBuilder();
							
							userinfo_builder.setId(id);
							userinfo_builder.setPasswd(passwd);
				    		builder_msg.setUserinfo(userinfo_builder.build());
				    		
				    		EcamsClient ecamsclient = new EcamsClient(ip,port);
				    		ReturnMsg returnMsg1 = ecamsclient.sendMsg(builder_msg.build());
				    		
				    		if (returnMsg1.getReturnStr().startsWith("SOCKERR")){
								return;
							}else if(returnMsg1.getReturnval() > 0){
								MessageBox messageBox = new MessageBox(new Shell(), SWT.OK);
								messageBox.setMessage(returnMsg1.getReturnStr());
								messageBox.open();
								return;
							}
				    		
				    		boolean metaFlg = false;
							
				    		String tool = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.TOOL, "", null);
				    		
							if("I".equals(tool)){
					    		try{
									Map<String, byte[]> parameter = new HashMap<String, byte[]>(); 
						    		parameter.put(filename+" "+returnMsg.getEcamsmsg().getHistorylist().getHistorydata(svrlist.getSelectionIndex()).getCrVersion(), EGzip.getDecompressedByte(returnMsg1.getEcamsmsg().getFiledata().getFilebytes().toByteArray()));
									//parameter.put(filename+" "+returnMsg.getEcamsmsg().getHistorylist().getHistorydata(svrlist.getSelectionIndex()).getCrVersion(), EGzip.getFileByte(returnMsg1.getEcamsmsg().getFiledata().getFilebytes().toByteArray()));
						    		if((Boolean)CommandExecuter.executeCommand("command.team.OpenResourceHistory", parameter) != null){
						    			metaFlg = (Boolean)CommandExecuter.executeCommand("command.team.OpenResourceHistory", parameter);
						    		}else{
										MessageBox messageBox = new MessageBox(getSite().getShell());
					    				messageBox.setMessage("Tool\uad6c\ubd84\uc774 \ub9de\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4. \ud655\uc778\ud558\uc5ec\uc8fc\uc2ed\uc2dc\uc624.");
					    				messageBox.setText("Tool");
					    				messageBox.open();
					    				return;
									}
					    			parameter = null;
					    		}catch (Exception e1){
					    			e1.printStackTrace();
					    			return;
					    		}finally{
					    		}
							}
				    		
				    		if(!metaFlg){
				    			/*
					    		IProject project= inputResource[0].getProject();
					    		String filepath = "C://history/tmp";
					    		
					    		while(filepath.indexOf("/") >=0){
				    				filepath = filepath.replace("/","\\");
				    			}
				    			while(filepath.indexOf("\\\\") >=0){
				    				filepath = filepath.replace("\\\\", "\\");
				    			}
				    			while(filepath.indexOf("\\") >=0){
				    				filepath = filepath.replace("\\", "/");
				    			}	
				    			File nfolder = new File(filepath);
								
								if (!nfolder.exists()){
									nfolder.mkdirs();
								}
								
								File filez = new File(filepath+"/."+filename+"."+returnMsg.getEcamsmsg().getHistorylist().getHistorydata(svrlist.getSelectionIndex()).getCrVersion());

								IPath tmpPath = new Path(inputResource[0].getLocation().toOSString());
								IResource tmpResource = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(tmpPath);
								
								//System.out.println(tmpResource);
								String localCharset = Utilities.getCharset(tmpResource);
								
				    			if (!filez.exists()){
				    				filez.createNewFile();
				    			}else{
				    				filez.delete();
				    				filez.createNewFile();
				    			}
								
								FileOutputStream fw = new FileOutputStream(filez);
								//fw.write(EGzip.getDecompressedByte(returnMsg1.getEcamsmsg().getFiledata().getFilebytes().toByteArray()));
								//System.out.println(localCharset);
								fw.write(EGzip.getFileByte(returnMsg1.getEcamsmsg().getFiledata().getFilebytes().toByteArray(),localCharset));
								
								if(fw.getChannel().size() <1){
									throw new IOException("FILE SIZE ERROR");
								}
								fw.flush();
								fw.close();
								
								IWorkbenchPage page = null;
								IEditorInput input = null;
								IPath path = Path.fromOSString(filez.getAbsolutePath());	
								IFile ifile = project.getFile(path.lastSegment());
								
								page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
								input = new FileEditorInput(ifile);
								if (ifile.exists()){
									ifile.delete(true, null);
									
									final IEditorPart editor = page.findEditor(input);
									if (editor != null){
										editor.getEditorSite().getPage().closeEditor(editor, false);
									}
								}
								
								ifile.createLink(path, IResource.NONE, null);
								IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(filename);
								if(desc == null){
									page.openEditor(input,"org.eclipse.ui.DefaultTextEditor");
								}else{
									page.openEditor(input, desc.getId());
								}
								*/
								
				    			
				    			IProject project= inputResource[0].getProject();
				    			
				    			String filepath = project.getLocation().toString()+"/.ecm_tmp";
				    			
				    			while(filepath.indexOf("/") >=0){
				    				filepath = filepath.replace("/","\\");
				    			}
				    			
				    			while(filepath.indexOf("\\\\") >=0){
				    				filepath = filepath.replace("\\\\", "\\");
				    			}

				    			while(filepath.indexOf("\\") >=0){
				    				filepath = filepath.replace("\\", "/");
				    			}	

				    			File nfolder = new File(filepath);
								
								if (!nfolder.exists()){
									nfolder.mkdirs();
								}
								
				    			String fullname = filepath+"/."+filename+"."+returnMsg.getEcamsmsg().getHistorylist().getHistorydata(svrlist.getSelectionIndex()).getCrVersion();
				    			
								File filez = new File(fullname);
								
				    			filez.createNewFile();
				    			
				    			FileOutputStream fw = new FileOutputStream(filez);
								fw = new FileOutputStream(filez);
								fw.write(EGzip.getDecompressedByte(returnMsg1.getEcamsmsg().getFiledata().getFilebytes().toByteArray()));
								fw.flush();
								fw.close();						    
								fw = null;
								
								IPath path = new Path(fullname);

								IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
								IResource tmpResource = root.getFileForLocation(path);
								

								((IResource) tmpResource).refreshLocal(IResource.FILE, null);
								
								
								IPath ipath = new Path(tmpResource.getLocation().toString());
								
								IFile ifile = root.getFileForLocation(ipath);
								
								FileEditorInput input = new FileEditorInput(ifile);
								
								IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(ifile.getName());
								IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
								if(desc == null){
									page.openEditor(input,"org.eclipse.ui.DefaultTextEditor");
								}else{
									page.openEditor(input, desc.getId());
								}
								
				    		}
						}catch(Exception E){
							E.printStackTrace();
						}
					}
				} else {
					MessageBox messageBox = new MessageBox(new Shell(), SWT.OK);
					messageBox.setMessage("\ubc14\uc774\ub108\ub9ac\ud30c\uc77c\uc740 \uc18c\uc2a4\ubcf4\uae30\ub97c \ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4.");
					messageBox.open();
				}
			}
			
			public void mouseDown(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void mouseUp(MouseEvent e) {
				// TODO Auto-generated method stub
				styledText.setText( svrlist.getSelection()[0].getText(8) );
				//System.out.println(svrlist.getSelection()[svrlist.getSelectionIndex()]);
			}
		};
	}


	private static void throwPartInitException(String message) throws PartInitException {    
		throwPartInitException(message, IStatus.OK);  
	}  
	private static void throwPartInitException(String message, int code) throws PartInitException {    
		IStatus status= new Status(IStatus.ERROR, IRuntimeConstants.PI_RUNTIME, code, message, null);    
		throw new PartInitException(status);  
	}
	
	 

	public void setInputResource(IResource[] inputResource){
		this.inputResource = inputResource;
	}
	protected IFile[] getFiles() {
		return ShowHistoryPageSource.getFiles((Object[]) getInput());
	}
	
	public boolean isValidInput(Object object) {
		// TODO Auto-generated method stub
		return (object instanceof IFile);
	}
	public void refresh() {
		// TODO Auto-generated method stub
		
	}
	public String getName() {
		// TODO Auto-generated method stub
		IFile[] files = getFiles();
		return files[0].getName();
	}
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}
	public Object getAdapter(Class adapter) {
		// TODO Auto-generated method stub
		if(adapter == IHistoryCompareAdapter.class) {
			return this;
		}
		return null;
	}
	public boolean inputSet() {
		// TODO Auto-generated method stub
		return false;
	}
	
	public Control getControl() {
		// TODO Auto-generated method stub
		return sashForm;
	}
	public void setFocus() {
		// TODO Auto-generated method stub
		sashForm.setFocus();
	}
}
