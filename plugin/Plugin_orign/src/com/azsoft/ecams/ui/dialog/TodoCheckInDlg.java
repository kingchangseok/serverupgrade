package com.azsoft.ecams.ui.dialog;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

import com.azsoft.ecams.core.EcamsProviderPlugin;
import com.azsoft.ecams.core.resource.EcamsRepositoryProvider;
import com.azsoft.ecams.properties.IProperty;
import com.azsoft.ecams.proto.ProtoEcams.FileData;
import com.azsoft.ecams.util.checksum.CheckSum;

public class TodoCheckInDlg extends Dialog {
	private IResource[] inputResource;
	private Table svrlist;
	private TableColumn column1;
	private TableColumn column2;
	private TableColumn column3;
	private TableColumn column4;
	private TableColumn column5;
	private TableColumn column6;
	private List realList = new ArrayList();
	
	private String id = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.ID, "", null);
	
	final Display display = Display.getCurrent();
	Color gray = display.getSystemColor(SWT.COLOR_GRAY);
	Color white = display.getSystemColor(SWT.COLOR_WHITE);
	
	public TodoCheckInDlg(Shell parentShell) {
		super(parentShell);
		// TODO Auto-generated constructor stub
	}
	/**
	 * @wbp.parser.constructor
	 */
	//public TodoCheckInDlg(Shell parentShell, IResource[] inputResoruce, List syncLists) {
	public TodoCheckInDlg(Shell parentShell, List syncLists) {
		super(parentShell);
		this.realList = syncLists;
		//setInputResource(inputResoruce);
		// TODO Auto-generated constructor stub
	}	
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("\ubcc0\uacbd\uc911\ubaa9\ub85d");
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		//svrlist.clearAll();
		createButton(parent, IDialogConstants.OK_ID,IDialogConstants.OK_LABEL, false);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		//createTableViewer(parent);
		Composite container = new Composite(parent, SWT.NULL);
        
		svrlist = new Table(container, SWT.BORDER | SWT.FULL_SELECTION);
		svrlist.setBounds(0, 0, 990, 500);
		svrlist.setHeaderVisible(true);
		svrlist.setLinesVisible(true);
		
		column4 = new TableColumn(svrlist, SWT.NONE);
		column4.setText("No"); 
		column4.setWidth(70);
		column4.setResizable(true);
		column4.setMoveable(true);
		
		column1 = new TableColumn(svrlist, SWT.NONE);
        column1.setText("\uc5c5\ubb34"); 
        column1.setWidth(130);
        column1.setResizable(true);
        column1.setMoveable(true);
        
        column5 = new TableColumn(svrlist, SWT.NONE);
        column5.setText("\ud30c\uc77c\uacbd\ub85c"); 
        column5.setWidth(165);
        column5.setResizable(true);
        column5.setMoveable(true);
        
        column6 = new TableColumn(svrlist, SWT.NONE);
        column6.setText("\uc0ac\uc6a9\uc790\uba85"); 
        column6.setWidth(115);
        column6.setResizable(true);
        column6.setMoveable(true);
        
        column2 = new TableColumn(svrlist, SWT.NONE);
        column2.setText("\ud30c\uc77c\uba85"); 
        column2.setWidth(300);
        column2.setResizable(true);
        column2.setMoveable(true);
        
        column3 = new TableColumn(svrlist, SWT.NONE);
        column3.setText("\uc0c1\ud0dc"); 
        column3.setWidth(200);
        column3.setResizable(true);
        column3.setMoveable(true);
        
        Listener sortListener = new Listener(){
        	public void handleEvent(Event e){
        		TableItem[] items = svrlist.getItems();
        		Collator collator = Collator.getInstance(Locale.getDefault());
        		TableColumn sortColumn = svrlist.getSortColumn();
        		TableColumn column = (TableColumn)e.widget;
        		int dir = svrlist.getSortDirection();
        		if(sortColumn == column){
        			dir = dir == SWT.UP?SWT.DOWN:SWT.UP;
        		}else{
        			dir = SWT.UP;
        		}
        		int index = 100;
        		if(column.equals(column4)){
        			index = 0;
        		}else if(column.equals(column6)){
        			index = 3;
        		}else if(column.equals(column2)){
        			index = 4;
        		}else if(column.equals(column3)){
        			index = 5;
        		}
        		if(index == 0 || index == 3 || index == 4 || index == 5){
	        		for(int i=1; i<items.length; i++){
	        			String value1 = items[i].getText(index);
	        			for(int j=0; j<i; j++){
	        				String value2 = items[j].getText(index);
	        				int compare = 0;
	        				if(index == 0){
	        					compare = Integer.parseInt(items[i].getText(0)) - Integer.parseInt(items[j].getText(0));
	        				}else{
	        					compare = collator.compare(value1,  value2);
	        				}
	        				if(dir == SWT.UP){
		        				if(compare<0){
		        					String[] values = {items[i].getText(0), items[i].getText(1), items[i].getText(2), items[i].getText(3), items[i].getText(4), items[i].getText(5)};
		        					TableItem newitem = new TableItem(svrlist, SWT.NONE, j);
		        					newitem.setText(values);
		        					String usr = items[i].getText(3).split(":")[1];
		        					if(!usr.equals(id)){
		        						newitem.setBackground(gray);
		        					}else{
		        						newitem.setBackground(white);
		        					}
		        					items[i].dispose();
		        					items = svrlist.getItems();
		        					break;
		        				}
	        				}else if(dir == SWT.DOWN){
	        					if(compare>0){
	        						String[] values = {items[i].getText(0), items[i].getText(1), items[i].getText(2), items[i].getText(3), items[i].getText(4), items[i].getText(5)};
		        					TableItem newitem = new TableItem(svrlist, SWT.NONE, j);
		        					newitem.setText(values);
		        					String usr = items[i].getText(3).split(":")[1];
		        					if(!usr.equals(id)){
		        						newitem.setBackground(gray);
		        					}else{
		        						newitem.setBackground(white);
		        					}
		        					items[i].dispose();
		        					items = svrlist.getItems();
		        					break;
	        					}
	        				}
	        			}
	        		}
	        		svrlist.setSortColumn(column);
	                svrlist.setSortDirection(dir);
        		}
        	}
        };
        column4.addListener(SWT.Selection, sortListener);
        column6.addListener(SWT.Selection, sortListener);
        column2.addListener(SWT.Selection, sortListener);
        column3.addListener(SWT.Selection, sortListener);
        svrlist.setSortColumn(column4);
        
        setTodoList();
        
        /*shell.setSize(shell.computeSize(SWT.DEFAULT, SWT.DEFAULT).x, 600);
        shell.open();
        while(!shell.isDisposed()){
        	if(!display.readAndDispatch()) display.sleep();
        }*/
        //display.dispose();
        
		return container;
	}
	
	public class NameSorter extends ViewerSorter{
		public int compare(Viewer viewer, Object e1, Object e2){
			return ((String)e1).compareTo(((String)e2));
		}
	}
	
	public void setTodoList(){
		try{
			int i,j = 0;
			IProject myproject = null;
			
    		for(i=0;i<realList.size();i++){
    			FileData filedata = (FileData) realList.get(i);
    			//myproject = EcamsRepositoryProvider.findProject(filedata.getSysinfo().getSyscd(),"");
    			//myproject = EcamsRepositoryProvider.findProject(filedata.getSysinfo().getSyscd(),filedata.getJobinfo().getJobcd());
    			myproject = EcamsRepositoryProvider.findProject(filedata.getSysinfo().getSyscd());
    			
    			if (myproject == null){
    				continue;
    			}
    			String projectPath = myproject.getLocation().toOSString();
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
				
				if (filez.exists()){
					TableItem item = new TableItem(svrlist, SWT.NONE);
					int c = 0;
					j++;
					item.setText(c++, Integer.toString(j));
					item.setText(c++, filedata.getJobinfo().getJobname()+"("+filedata.getJobinfo().getJobcd()+")");
					item.setText(c++, filedata.getPathinfo().getRelativitePath());
					item.setText(c++, filedata.getEditor().split(":")[1]+":"+filedata.getEditor().split(":")[0]);
					item.setText(c++, filedata.getFilename());
					
					if(filedata.getStatus().split(":")[1].equals("0") || filedata.getStatus().split(":")[1].equals("3")){ //운영중, 신규
						item.setText(c++, filedata.getStatus().split(":")[0]);
					}else{ //체크아웃
						if(filedata.getMd5Sum() != null && !filedata.getMd5Sum().equals("")){
							if(!CheckSum.MD5SumVal(filename).equals(filedata.getMd5Sum())){
								item.setText(c++, "\uccb4\ud06c\uc544\uc6c3 \ud6c4 \ubcc0\uacbd\ub0b4\uc6a9\uc788\uc74c");
							} else {
								item.setText(c++, filedata.getStatus().split(":")[0]);
							}
						}else{
							item.setText(c++, "\uccb4\ud06c\uc778\uac00\ub2a5(\uc2e0\uaddc)");
						}
					}
					
					if(!filedata.getEditor().split(":")[0].equals(id)){
						item.setBackground(gray);
					}else{
						item.setBackground(white);
					}
				}
    		}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public void setInputResource(IResource[] inputResource){
		this.inputResource = inputResource;
	}
}
