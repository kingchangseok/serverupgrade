package com.azsoft.ecams.ui.dialog;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationTargetException;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.input.BOMInputStream;
import org.eclipse.compare.BufferedContent;
import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.IEditableContent;
import org.eclipse.compare.IModificationDate;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.Differencer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Label;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

import java.util.List;

import com.azsoft.ecams.ui.compare.CompareItem;
import com.azsoft.ecams.ui.compare.Utilities;

import com.azsoft.ecams.core.CommandExecuter;
import com.azsoft.ecams.core.EcamsLogManager;
import com.azsoft.ecams.core.EcamsProviderPlugin;
import com.azsoft.ecams.properties.IProperty;
import com.azsoft.ecams.proto.ProtoEcams.EcamsMessage;
import com.azsoft.ecams.proto.ProtoEcams.FileData;
import com.azsoft.ecams.proto.ProtoEcams.ReturnMsg;
import com.azsoft.ecams.proto.ProtoEcams.UserInfo;
import com.azsoft.ecams.socket.EcamsClient;
import com.azsoft.ecams.util.file.EFileCopy;
import com.azsoft.ecams.util.file.EGzip;
import com.google.protobuf.DescriptorProtos.FileOptions;
import org.eclipse.wb.swt.SWTResourceManager;

public class SourceDiffDlg extends Dialog {
	private static EcamsLogManager logger = new EcamsLogManager(ICommandService.class.getName());
	//private IResource[] inputResource;
	private String filename,itemid,rsrcinfo,localpath;
	private Table svrlist;
	private Shell parentShell;
	
	private String ip,port,id,passwd;
	private CheckboxTableViewer ctv;
	private String filepath;
	private String checkItemVer;
	private IResource tmpResource;
	private String localCharset = "UTF-8";
	
	private IProject project;
	
	public SourceDiffDlg(Shell parentShell) {
		super(parentShell);
		// TODO Auto-generated constructor stub
	}
	/**
	 * @wbp.parser.constructor
	 */
	//public SourceDiffDlg(Shell parentShell, IResource[] inputResoruce, String file, String item, String rsrcinfo) {
	public SourceDiffDlg(Shell parentShell, String filename, String item, String rsrcinfo, String filepath, IProject project) {
		super(parentShell);
		this.parentShell = parentShell;
		//setInputResource(inputResoruce);
		this.filename = filename;
		this.localpath = filepath;
		this.itemid = item;
		this.rsrcinfo = rsrcinfo;
		this.project = project;
		
		// TODO Auto-generated constructor stub
	}	
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("["+filename+"]\uc758 \ud788\uc2a4\ud1a0\ub9ac");
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID,IDialogConstants.OK_LABEL, false);
		
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		//createTableViewer(parent);
		
		Composite container = new Composite(parent, SWT.NULL);
		

		Label comment = new Label(container, SWT.NONE);
		comment.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
		comment.setBounds(5, 10, 300, 20);
		comment.setText("\ube44\uad50\ud560 \ubc84\uc804 \ub450\uac1c\ub97c \uc120\ud0dd\ud558\uc2ed\uc2dc\uc624.");
		
		svrlist = new Table(container, SWT.BORDER | SWT.FULL_SELECTION | SWT.CHECK);
		svrlist.setLinesVisible(true);
		svrlist.setBounds(0, 35, 460, 384);
		svrlist.setHeaderVisible(true);
		
		svrlist.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				TableItem[] tabitem = svrlist.getItems();
				int chkCnt = 0;
				int selectedIndex = 0;
				for(int tabCnt=0; tabCnt<tabitem.length; tabCnt++){
					if(e.item == tabitem[tabCnt]){
						selectedIndex = tabCnt;
						break;
					}
				}
				
				for(int tabCnt=0; tabCnt<tabitem.length; tabCnt++){
					if(tabitem[tabCnt].getChecked()){
						chkCnt++;
						if(chkCnt>2){
							svrlist.getItem(selectedIndex).setChecked(false);
							svrlist.redraw();
						}
					}
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
			}
		});
		
		TableColumn column1 = new TableColumn(svrlist, SWT.NONE);
		column1.setWidth(80);
		column1.setResizable(true);
		column1.setMoveable(true);
		column1.setText("\uc2e0\uccad\uc790");
		column1.setAlignment(SWT.CENTER);
		
		TableColumn column2 = new TableColumn(svrlist, SWT.NONE);
		column2.setWidth(120);
		column2.setResizable(true);
		column2.setMoveable(true);
		column2.setText("\uc2e0\uccad\ubc88\ud638");
		column2.setAlignment(SWT.CENTER);

		TableColumn column3 = new TableColumn(svrlist, SWT.NONE);
		column3.setWidth(150);
		column3.setResizable(true);
		column3.setMoveable(true);
		column3.setText("\uc2e0\uccad\uc77c\uc2dc/\uc218\uc815\uc77c\uc2dc(Local)");
		column3.setAlignment(SWT.CENTER);
		
		TableColumn column4 = new TableColumn(svrlist, SWT.NONE);
		column4.setWidth(80);
		column4.setResizable(true);
		column4.setMoveable(true);
		column4.setText("\ubc84\uc804");
		column4.setAlignment(SWT.CENTER);
		
		TableColumn column5 = new TableColumn(svrlist, SWT.NONE);
		column5.setWidth(150);
		column5.setResizable(true);
		column5.setMoveable(true);
		column5.setText("\uc2e0\uccad\uc0ac\uc720");
		column5.setAlignment(SWT.CENTER);

		setHistoryList();
		
		return container;
	}
	
	public void setHistoryList(){
		ip = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.IP, "", null);
		port = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PORT, "", null);
		id = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.ID, "", null);
		passwd = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PASSWD, "", null);
		
		
		if(itemid.length() > 0){
			try{
				EcamsMessage.Builder builder_msg = EcamsMessage.newBuilder();
				builder_msg.setMsgtype("GET_LASTVERSION");
				FileData.Builder filedata_builder = FileData.newBuilder();
				
				filedata_builder.setItemid(itemid);
				filedata_builder.setFilename(filename);
				
				builder_msg.setFiledata(filedata_builder.build());
				
				UserInfo.Builder userinfo_builder = UserInfo.newBuilder();
		
				userinfo_builder.setId(id);
				userinfo_builder.setPasswd(passwd);
				
				builder_msg.setUserinfo(userinfo_builder.build());
			
				EcamsClient syncClient = new EcamsClient(ip,port);
				ReturnMsg returnMsg = syncClient.sendMsg(builder_msg.build());
				
				if(returnMsg.getReturnval() == 0){
					TableItem item = new TableItem(svrlist, SWT.NONE);
					int c = 0;
					
					while(localpath.indexOf("/") >=0){
						localpath = localpath.replace("/","\\");
					}
					while(localpath.indexOf("\\\\") >=0){
						localpath = localpath.replace("\\\\", "\\");
					}
					while(localpath.indexOf("\\") >=0){
						localpath = localpath.replace("\\", "/");
					}
					
					File filez = new File(localpath+"/"+filename);
					
					IPath tmpPath = new Path(localpath+"/"+filename);
					tmpResource = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(tmpPath);

					localCharset = Utilities.getCharset(tmpResource);
					
					//System.out.println(localCharset);
					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
					
					item.setText(c++, "Local");
					item.setText(c++, "");
					item.setText(c++, dateFormat.format(filez.lastModified()));
					item.setText(c++, "");
					item.setText(c++, "-");
					
					for(int i=0;i<returnMsg.getEcamsmsg().getFiledatalist().getFiledatasCount();i++){
						item = new TableItem(svrlist, SWT.NONE);
						c = 0;
						item.setText(c++, returnMsg.getEcamsmsg().getFiledatalist().getFiledatas(i).getEditor());
						item.setText(c++, returnMsg.getEcamsmsg().getFiledatalist().getFiledatas(i).getAcptno());	//20201230 \uc18c\uc2a4\ube44\uad50\uc2dc \uc2e0\uccad\ubc88\ud638\uac00 \uc548\ub4e4\uc5b4\uac00\ub294 \uc624\ub958 \uc218\uc815
						item.setText(c++, returnMsg.getEcamsmsg().getFiledatalist().getFiledatas(i).getLstdate());
						item.setText(c++, returnMsg.getEcamsmsg().getFiledatalist().getFiledatas(i).getBasever());
						//+"."+Integer.toString(returnMsg.getEcamsmsg().getFiledatalist().getFiledatas(i).getVersion())
						item.setText(c++, returnMsg.getEcamsmsg().getFiledatalist().getFiledatas(i).getMsguse());
						
					} 
					
					item = null;
				}else{
		    		if (returnMsg.getReturnStr().startsWith("SOCKERR")){
						return;
					}else{
						MessageBox messageBox = new MessageBox(this.getShell(), SWT.OK);
						messageBox.setMessage("\ud788\uc2a4\ud1a0\ub9ac\uac00 \uc5c6\uc2b5\ub2c8\ub2e4");
						if (messageBox.open() == SWT.OK){
							this.close();
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
	
	protected void okPressed() {
		//TableItem[] children = ctv.getTable().getItems();
		TableItem[] children = svrlist.getItems();
		checkItemVer = "";
		for(int i=0; i<children.length; i++){
			if(children[i] != null && children[i].getChecked()){
			//if(children[i] != null){
				if(i == 0){
					checkItemVer = "LO";
				}else{
					if (checkItemVer.length() == 0) checkItemVer = children[i].getText(3).toString();
					else checkItemVer = checkItemVer + "," + children[i].getText(3).toString();
				}
			}
		}
		if(checkItemVer.split(",").length==2){
			EcamsMessage.Builder builder_msg = EcamsMessage.newBuilder();
			
			FileData.Builder lastfiledata_builder = FileData.newBuilder();
			lastfiledata_builder.setItemid(itemid);
			lastfiledata_builder.setVergbn("R");
			lastfiledata_builder.setFilename(filename);
			builder_msg = EcamsMessage.newBuilder();
    		builder_msg.setMsgtype("GETLASTFILE");
    		UserInfo.Builder userinfo_builder = UserInfo.newBuilder();
			userinfo_builder.setId(id);
			userinfo_builder.setPasswd(passwd);

			
			//Map<String, byte[]> parameter = new HashMap<String, byte[]>();
			Map<String, byte[]> parameter = new HashMap<String, byte[]>();
			
			int i=0;
			for (i=0; i<checkItemVer.split(",").length; i++) {
				if(checkItemVer.split(",")[i].equals("LO")){
					try {
						while(localpath.indexOf("/") >=0){
							localpath = localpath.replace("/","\\");
						}
						while(localpath.indexOf("\\\\") >=0){
							localpath = localpath.replace("\\\\", "\\");
						}
						while(localpath.indexOf("\\") >=0){
							localpath = localpath.replace("\\", "/");
						}
						
						File filez = new File(localpath+"/"+filename);
						
			            FileInputStream fs2 = new FileInputStream(filez);
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
			            
						BOMInputStream inputStream = new BOMInputStream(fs2);//,  ByteOrderMark.UTF_16LE, ByteOrderMark.UTF_16BE);
						
						int ch;
						while ((ch = inputStream.read()) != -1) {
							bos.write((char)ch);
						}

						//parameter.put(filename+" "+checkItemVer.split(",")[i], bos.toString(localCharset).getBytes());	// 20210105 sourceDiff charset delete
						parameter.put(filename+" "+checkItemVer.split(",")[i], bos.toByteArray());
						//parameter.put(filename+" "+checkItemVer.split(",")[i], bos.toString().getBytes());
						
						
						fs2.close();
						bos.close();
						//os.close();
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
				}else{
					//lastfiledata_builder.setVersion(Integer.parseInt(checkItemVer.split(",")[i].split("\\.")[1]));
//					System.out.println("SourceDiffDlg 382 line basever:" + checkItemVer.split(",")[i].split("/")[0]);
//					System.out.println("SourceDiffDlg 383 line Acotno:" + checkItemVer.split(",")[i].split("/")[1]);
					
					lastfiledata_builder.setBasever(checkItemVer.split(",")[i].split("/")[0]);
					
		    		builder_msg.setFiledata(lastfiledata_builder.build());
		    		builder_msg.setUserinfo(userinfo_builder.build());
	
		    		EcamsClient ecamsclient = new EcamsClient(ip,port);
		    		ReturnMsg returnMsg1 = ecamsclient.sendMsg(builder_msg.build());
		    		
		    		//IProject project= inputResource[0].getProject();
		    		if(returnMsg1.getReturnval()==0){
		    			ByteArrayOutputStream bos = new ByteArrayOutputStream();

						try {
			    			byte[] inputBytes = EGzip.getDecompressedByte(returnMsg1.getEcamsmsg().getFiledata().getFilebytes().toByteArray());
			    			BOMInputStream inputStream = new BOMInputStream(new ByteArrayInputStream(inputBytes));
							
							int ch;
							while ((ch = inputStream.read()) != -1) {
								bos.write((char)ch);
							}
//							parameter.put(filename+" "+checkItemVer.split(",")[i], bos.toString(localCharset).getBytes()); // 20210105 sourceDiff charset delete
							parameter.put(filename+" "+checkItemVer.split(",")[i], bos.toByteArray());
							bos.close();							
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} 
						bos = null;
		    		}else{
		    			if (returnMsg1.getReturnStr().startsWith("SOCKERR")){
							return;
						}
		    		}
				}
	        }
			
			if(parameter.size()>0){
				boolean metaflg = false;
				String tool = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.TOOL, "", null);
//				if("I".equals(tool)){
//					try {
////						logger.info(">>>>>>>> parameter: "+ parameter);
//						if((Boolean)CommandExecuter.executeCommand("command.team.OpenResourceCompare", parameter) != null){
//							metaflg = (Boolean)CommandExecuter.executeCommand("command.team.OpenResourceCompare", parameter);
//						}else{
//							MessageBox messageBox = new MessageBox(getShell());
//		    				messageBox.setMessage("Tool\uad6c\ubd84\uc774 \ub9de\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4. \ud655\uc778\ud558\uc5ec\uc8fc\uc2ed\uc2dc\uc624.");
//		    				messageBox.setText("Tool");
//		    				messageBox.open();
//		    				return;
//						}
//					} catch (Exception e1) {
//						// TODO Auto-generated catch block
//						e1.printStackTrace();
//						parameter = null;
//						return;
//					}
//				}
				
				if(!metaflg){
				String fullname = localpath+"/"+filename;
				IPath path = new Path(fullname);

				IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
				IResource tmpResource = root.getFileForLocation(path);
				
				IProject project= tmpResource.getProject();
    			
    			filepath = project.getLocation().toString()+"/.ecm_tmp";
    			
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
				
				
				List<IFile> list = new ArrayList<IFile>();
				for(i=0; i<parameter.size(); i++){
					File filez = new File(filepath+"/."+filename+"."+checkItemVer.split(",")[i]);
					try{
						if (!filez.exists()){
							filez.createNewFile();
						}else{
							filez.delete();
							filez.createNewFile();
						}
						
						FileOutputStream fw = new FileOutputStream(filez);
						fw.write(parameter.get(filename+" "+checkItemVer.split(",")[i]));
						if(fw.getChannel().size() <1){
							throw new IOException("FILE SIZE ERROR");
						}
						fw.flush();
						fw.close();
						
						//IFile iFile = root.getFile(Path.fromOSString(filez.getAbsolutePath()));
						
						IPath path2 = new Path(filepath+"/."+filename+"."+checkItemVer.split(",")[i]);
						IResource tmpResource2 = root.getFileForLocation(path2);
						IPath ipath = new Path(tmpResource2.getLocation().toString());
						IFile iFile = root.getFileForLocation(ipath);
						
			        	list.add(iFile);
			        	
			        	((IResource) tmpResource2).refreshLocal(IResource.FILE, null);
			        	
			        	iFile = null;
					}catch(Exception e){
						e.getStackTrace();
						System.out.println(e);
					}
		        	filez = null;
				}
				if(list.size()>0){
					CompareConfiguration cc = new CompareConfiguration();
			        cc.setLeftEditable(false);
			        cc.setRightEditable(false);
			        cc.setLeftLabel(filename+" <"+checkItemVer.split(",")[0]+">");
			        cc.setRightLabel(filename+" <"+checkItemVer.split(",")[1]+">");
			        cc.setProperty(CompareConfiguration.IGNORE_WHITESPACE, Boolean.FALSE);			        

			        CompareEditorInput editorInput = new CompareEditorInput(cc) {
		        	   CompareItem left = new CompareItem(filepath+"/."+filename+"."+checkItemVer.split(",")[0]);   
		        	   CompareItem right = new CompareItem(filepath+"/."+filename+"."+checkItemVer.split(",")[1]);   
		        	   
		        	   
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
			        this.close();
					}else{
						MessageBox messageBox = new MessageBox(this.getShell(), SWT.OK);
						messageBox.setMessage("\ube44\uad50 \ubaa9\ub85d\uc744 \uac00\uc838\uc624\ub294\ub370 \uc2e4\ud328\ud588\uc2b5\ub2c8\ub2e4.");
						messageBox.setText("ERROR");
						messageBox.open();
					}
					list = null;
				}
				this.close();
			}else{
				MessageBox messageBox = new MessageBox(this.getShell(), SWT.OK);
				messageBox.setMessage("\ub450\uac1c\uc758 \ubc84\uc804 \uc815\ubcf4\ub97c \uac00\uc838\uc624\ub294\ub370 \uc2e4\ud328\ud588\uc2b5\ub2c8\ub2e4.");
				messageBox.setText("ERROR");
				messageBox.open();
			}
			parameter = null;
		} else {
			MessageBox messageBox = new MessageBox(this.getShell(), SWT.OK);
			messageBox.setMessage("\ube44\uad50\ud560 \ubc84\uc804 \ub450\uac1c\ub97c \uc120\ud0dd\ud558\uc138\uc694.");
			messageBox.setText("ERROR");
			messageBox.open();
		}
	}
}
