package com.azsoft.ecams.ui.view;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.ViewPart;

import com.azsoft.ecams.core.CommandExecuter;
import com.azsoft.ecams.core.EcamsProviderPlugin;
import com.azsoft.ecams.properties.IProperty;
import com.azsoft.ecams.proto.ProtoEcams.EcamsMessage;
import com.azsoft.ecams.proto.ProtoEcams.FileData;
import com.azsoft.ecams.proto.ProtoEcams.ReturnMsg;
import com.azsoft.ecams.proto.ProtoEcams.UserInfo;
import com.azsoft.ecams.socket.EcamsClient;
import com.azsoft.ecams.ui.compare.Utilities;
import com.azsoft.ecams.util.file.EGzip;

public class HistoryView extends ViewPart{
	private Logger logger = Logger.getLogger(this.getClass());
	private Composite container;
	private Table svrlist;
	
	private TableViewer viewer;
	private MouseListener svrlistDoubleClick_Listener;
	private ReturnMsg returnMsg;
	private String filename,itemid,rsrcinfo;
	private String ip,port,id,passwd;
	private IResource[] inputResource;

	@Override
	public void createPartControl(Composite parent) {
		// TODO Auto-generated method stub
		ip = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.IP, "", null);
		port = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PORT, "", null);
		id = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.ID, "", null);
		passwd = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PASSWD, "", null);
		
		if(ip == null || port == null || id == null || passwd == null){
			return;
		}
		
		setupCallbacks();
		
		container = new Composite(parent, SWT.NULL);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		container.setLayout(tableColumnLayout);

		viewer = new TableViewer(container, SWT.BORDER | SWT.FULL_SELECTION);
		viewer.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		svrlist = viewer.getTable();
		svrlist.setHeaderVisible(true);
		svrlist.setLinesVisible(true);
		svrlist.addMouseListener(svrlistDoubleClick_Listener);

		TableViewerColumn viewercolumn1 = new TableViewerColumn(viewer, SWT.NONE);
		TableColumn column1 = viewercolumn1.getColumn();
		tableColumnLayout.setColumnData(column1, new ColumnPixelData(100, true, true));
		column1.setText("\uc2e0\uccad\ubc88\ud638");
        column1.setAlignment(SWT.CENTER);

        TableViewerColumn viewercolumn2 = new TableViewerColumn(viewer, SWT.NONE);
		TableColumn column2 = viewercolumn2.getColumn();
		tableColumnLayout.setColumnData(column2, new ColumnPixelData(120, true, true));
		column2.setText("\uc2e0\uccad\uc77c\uc2dc"); 
        column2.setAlignment(SWT.CENTER);
        
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
        
		TableViewerColumn viewercolumn7 = new TableViewerColumn(viewer, SWT.NONE);
		TableColumn column7 = viewercolumn7.getColumn();
		tableColumnLayout.setColumnData(column7, new ColumnPixelData(180, true, true));
		column7.setText("\ubcc0\uacbd\uc0ac\uc720");
		column7.setAlignment(SWT.CENTER);
		
		setHistoryList();
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
						item.setText(c++, returnMsg.getEcamsmsg().getHistorylist().getHistorydata(i).getCrAcptdate());
						item.setText(c++, returnMsg.getEcamsmsg().getHistorylist().getHistorydata(i).getCmUsername());
						item.setText(c++, returnMsg.getEcamsmsg().getHistorylist().getHistorydata(i).getCmCodename());
						item.setText(c++, returnMsg.getEcamsmsg().getHistorylist().getHistorydata(i).getCrPrcdate());
						item.setText(c++, returnMsg.getEcamsmsg().getHistorylist().getHistorydata(i).getCrVersion());
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
				if (rsrcinfo.substring(9,10).equals("0")){ 
					if (svrlist.getItemCount()>0){ 
						try{
							EcamsMessage.Builder builder_msg = EcamsMessage.newBuilder();
							
							FileData.Builder lastfiledata_builder = FileData.newBuilder();
							lastfiledata_builder.setItemid(itemid);
							lastfiledata_builder.setVersion(Integer.parseInt(returnMsg.getEcamsmsg().getHistorylist().getHistorydata(svrlist.getSelectionIndex()).getCrVersion()));
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
				    		
				    		boolean metaFlg = false;
				    		
				    		String tool = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.TOOL, "", null);
				    		
							if("I".equals(tool)){
								try{
						    		Map<String, byte[]> parameter = new HashMap<String, byte[]>(); 
						    		parameter.put(filename+" "+returnMsg.getEcamsmsg().getHistorylist().getHistorydata(svrlist.getSelectionIndex()).getCrVersion(), EGzip.getDecompressedByte(returnMsg1.getEcamsmsg().getFiledata().getFilebytes().toByteArray()));
		
						    		metaFlg = (Boolean) CommandExecuter.executeCommand("command.team.OpenResourceHistory", parameter);
						    		parameter = null;
								}catch(Exception e1){
									e1.printStackTrace();
									return;
								}
							}
				    		
							if(!metaFlg){
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
							
			    			if (!filez.exists()){
			    				filez.createNewFile();
			    			}
			    			else{
			    				filez.delete();
			    				filez.createNewFile();
			    			}
							
							FileOutputStream fw = new FileOutputStream(filez);

							IPath tmpPath = new Path(inputResource[0].getLocation().toOSString());
							IResource tmpResource = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(tmpPath);
							String localCharset = Utilities.getCharset(tmpResource);
							
							
							//fw.write(EGzip.getDecompressedByte(returnMsg1.getEcamsmsg().getFiledata().getFilebytes().toByteArray()));
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
							}
						}catch(Exception E){
							E.printStackTrace();
						}
					} else {
						MessageBox messageBox = new MessageBox(new Shell(), SWT.OK);
						messageBox.setMessage("\ud574\ub2f9 \ub9ac\uc18c\uc2a4\uc758 \ubc84\uc804\uc774 \uc5c6\uc2b5\ub2c8\ub2e4.");
						messageBox.setText("\uc54c\ub9bc");
						messageBox.open();
					}
				} else {
					MessageBox messageBox = new MessageBox(new Shell(), SWT.OK);
					messageBox.setMessage("\ubc14\uc774\ub108\ub9ac\ud30c\uc77c\uc740 \uc18c\uc2a4\ubcf4\uae30\ub97c \ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4.");
					messageBox.setText("\uc54c\ub9bc");
					messageBox.open();
				}
			}
			
			public void mouseDown(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void mouseUp(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
		};
	}
	
	public void setInputData(IResource[] resource, String filename, String itemid, String rsrcinfo){
		this.inputResource = resource;
		this.filename = filename;
		this.itemid = itemid;
		this.rsrcinfo = rsrcinfo;
	}
	
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		container.setFocus();
	}
	
}
