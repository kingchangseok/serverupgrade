package com.azsoft.ecams.ui.wizard.page;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.azsoft.ecams.core.EcamsProviderPlugin;
import com.azsoft.ecams.core.jobs.SyncJob;
import com.azsoft.ecams.properties.IProperty;
import com.azsoft.ecams.proto.ProtoEcams.EcamsMessage;
import com.azsoft.ecams.proto.ProtoEcams.FileData;
import com.azsoft.ecams.proto.ProtoEcams.JobInfoList;
import com.azsoft.ecams.proto.ProtoEcams.ReturnMsg;
import com.azsoft.ecams.proto.ProtoEcams.RsrcInfoList;
import com.azsoft.ecams.proto.ProtoEcams.SRInfo;
import com.azsoft.ecams.proto.ProtoEcams.SysInfo;
import com.azsoft.ecams.proto.ProtoEcams.UserInfo;
import com.azsoft.ecams.socket.EcamsClient;
import com.azsoft.ecams.ui.view.ResourceView;
import com.azsoft.ecams.ui.widgets.ResourceSelectionTree;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.layout.GridLayout;


public class RegistFileAllPage extends WizardPage {
//	private Logger logger = Logger.getLogger(this.getClass());
	/* \ub9ac\uc18c\uc2a4 */
	private IResource[]										resources; //\ub2e4\uc774\uc5bc\ub85c\uadf8\uc5d0\uc11c \ub118\uc5b4\uc628 \uc6d0\ub798 \ub9ac\uc18c
	private Object[]										selectedTreeResources; //\ud2b8\ub9ac\uc5d0\uc11c \uc120\ud0dd\ub41c \ub9ac\uc18c\uc2a4
	private Hashtable<Integer, Hashtable<String, Object> >	registFileAllResources; //\ucd5c\uc885\uc801\uc73c\ub85c \uc77c\uad04\ub4f1\ub85d\uc5d0 \ub300\ud55c \uc815\ubcf4\uac00 \ub2f4\uae38 \ub9ac\uc18c\uc2a4
	
	/* \ud654\uba74 */
	private ResourceSelectionTree	treeRegistFileAll; //\ud2b8\ub9ac(\ub9ac\uc18c\uc2a4\uc815\ubcf4 \ud3ec\ud568)
	private TableViewer 			tblViewerRegistFileAll; //\uc77c\uad04\ub4f1\ub85d \uc815\ubcf4\uac00 \ub2f4\uae38 \ud14c\uc774\ube14\ubdf0\uc5b4
	private Table 					tblRegistFileAll; //\uc77c\uacfc\ub4f1\ub85d \uc815\ubcf4\uac00 \ub2f4\uae38 \ud14c\uc774\ube14
	private Combo					cboProgKind, cboJob, cboSRID;
	private Text					textProgKind, textProgComment;
	private Button					chkAll;
	private Label					lblTreeTotalCnt, lblTreeSelectedCnt, lblTblTotalCnt, lblTblSelectedCnt;

	/* \ub9ac\uc2a4\ub108 */
	private MouseListener			btnListAddClickListener, btnListDelClickListener, chkAllClickListener;
	private ICheckStateListener 	treeCheckListner;
	private SelectionListener		cboProgKindSelectListner, tableSelectListner;
	
	final private int AUTO_NEW_ITEM = 26; //\uc790\ub3d9\uc2e0\uaddc\ud56d\ubaa9 cm_micode 26
	final private int NO_CHECK_EXE	= 40; //\ud655\uc7a5\uc790 \uccb4\ud06c\uc548\ud568 cm_micode 41
//	final private int AUTO_REG_EXE	= 43; //\ud655\uc7a5\uc790 \uc790\ub3d9\ub4f1\ub85d cm_micode 44
	
	/* \uc800\uc7a5\ub41c \uc0ac\uc6a9\uc790\uc815\ubcf4, \uc11c\ubc84\uc815\ubcf4 */
	private IDialogSettings		settings;
	private String 				ip, port, id, passwd;
	
	/* \uc0c1\ub2e8 \uce74\uc6b4\ud130 \uc138\ub294 \ub77c\ubca8 */
	final private int TREE_TOTAL_CNT		= 1; //\ud2b8\ub9ac \ucd1d \uac2f\uc218
	final private int TREE_SELECTED_CNT		= 2; //\ud2b8\ub9ac \uc120\ud0dd\ub41c \uac2f\uc218
	final private int TABLE_TOTAL_CNT		= 3; //\ud14c\uc774\ube14 \ucd1d \uac2f\uc218
	final private int TABLE_SELECTED_CNT	= 4; //\ud14c\uc774\ube14 \uc120\ud0dd\ub41c \uac2f\uc218
	
	private String gbnCd = "";
	private String tool = "";
	private String Sysinfo = "";
	private String Syscd = "";
	
	/**
	 * Create the wizard.
	 */
	public RegistFileAllPage(IResource[] resources, String gbnCd) {
		super("wizardPage");
		this.gbnCd = gbnCd;
		this.resources = resources;
		
		settings = EcamsProviderPlugin.getPlugin().getDialogSettings();
		registFileAllResources = new Hashtable<Integer, Hashtable<String, Object> >();
		
		setPageComplete(false);
		if( "ALL".equals(gbnCd) ) {
			setTitle("\ucd5c\ucd08\uc77c\uad04\ub4f1\ub85d");
			setDescription("\ucd5c\ucd08\uc77c\uad04\ub4f1\ub85d \ud560 \ud30c\uc77c\uc744 \uc120\ud0dd \ud6c4 \uc815\ubcf4\ub97c \uc785\ub825\ud558\uc5ec \ub4f1\ub85d \ud558\uc138\uc694.");
		} else if( "NEW".equals(gbnCd) ) {
			setTitle("\uc2e0\uaddc\ub4f1\ub85d");
			setDescription("\uc2e0\uaddc\ub4f1\ub85d \ud560 \ud30c\uc77c\uc744 \uc120\ud0dd \ud6c4 \uc815\ubcf4\ub97c \uc785\ub825\ud558\uc5ec \ub4f1\ub85d \ud558\uc138\uc694.");
		} else {}
		
		ip = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.IP, "", null);
		port = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PORT, "", null);
		id = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.ID, "", null);
		passwd = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PASSWD, "", null);
		
		if(ip == null || port == null || id == null || passwd == null){
			return;
		}	
	}

	/**
	 * Create contents of the wizard.
	 * @param parent
	 */
	public void createControl(Composite parent) {
		setListner(); //\ub9ac\uc2a4\ub108 \uc138\ud305 \ud638\ucd9c
		int tmpTreeTotalCnt = 0;
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new FormLayout()); //\uc804\uccb4\ud654\uba74 \ub808\uc774\uc544\uc6c3 \uc124\uc815
		
		/* \ud2b8\ub9ac \uc138\ud305 */
		treeRegistFileAll = new ResourceSelectionTree(container, SWT.NONE, resources, true);
		selectedTreeResources = treeRegistFileAll.getSelectedResources();
		tmpTreeTotalCnt = selectedTreeResources.length;
		
		FormData treeRegistFileAllFormData = new FormData();
		treeRegistFileAllFormData.bottom = new FormAttachment(71);
		treeRegistFileAllFormData.right = new FormAttachment(40);
		treeRegistFileAllFormData.top = new FormAttachment(7);
		treeRegistFileAllFormData.left = new FormAttachment(0);
		treeRegistFileAll.setLayoutData(treeRegistFileAllFormData);
		
		
		/* \uc77c\uad04\ub4f1\ub85d \ud14c\uc774\ube14 \ubdf0\uc5b4 \ub808\uc774\uc544\uc6c3 \uc138\ud305*/
		Composite tblViewerRegistFileAllContainer = new Composite(container, SWT.NONE); //\uc77c\uad04\ub4f1\ub85d \uc815\ubcf4\uc5d0 \ub300\ud55c ui\uad00\ub9ac \uac1d\uccb4
		//regInfoListContainer.setBounds(286, 0, 64, 64); //\uc77c\uad04\ub4f1\ub85d\uc815\ubcf4 \ud14c\uc774\ube14\uc758 \uc0ac\uc774\uc988 \ub4f1 \uc124\uc815
		TableColumnLayout tblColLayout = new TableColumnLayout();
		
		FormData tblViewerRegistFileAllFormData = new FormData();
		tblViewerRegistFileAllFormData.bottom = new FormAttachment(70); 
		//regInfoListFormData.right = new FormAttachment(0, 582);
		tblViewerRegistFileAllFormData.right = new FormAttachment(100);
		tblViewerRegistFileAllFormData.top = new FormAttachment(7); 
		tblViewerRegistFileAllFormData.left = new FormAttachment(40); //282
		tblViewerRegistFileAllContainer.setLayoutData(tblViewerRegistFileAllFormData);
		
		tblViewerRegistFileAllContainer.setLayout(tblColLayout);
		
		tblViewerRegistFileAll = new TableViewer(tblViewerRegistFileAllContainer, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		
		tblRegistFileAll = tblViewerRegistFileAll.getTable();
		tblRegistFileAll.setHeaderVisible(true);
		tblRegistFileAll.setLinesVisible(true);


		/* \ud14c\uc774\ube14 \uceec\ub7fc \uc138\ud305 */
		TableViewerColumn tblViewerCol1 = new TableViewerColumn(tblViewerRegistFileAll, SWT.NONE);
		TableColumn tblColNo = tblViewerCol1.getColumn();
		tblColNo.setResizable(false);
		tblColLayout.setColumnData(tblColNo, new ColumnPixelData(35, true, true));
		tblColNo.setText("No.");
		tblColNo.setAlignment(SWT.CENTER);

        TableViewerColumn tblViewerCol2 = new TableViewerColumn(tblViewerRegistFileAll, SWT.NONE);
		TableColumn tblColProgName = tblViewerCol2.getColumn();
		tblColLayout.setColumnData(tblColProgName, new ColumnWeightData(20, true));
		tblColProgName.setText("\ud504\ub85c\uadf8\ub7a8\uba85");
        tblColProgName.setAlignment(SWT.CENTER);

        TableViewerColumn tblViewerCol3 = new TableViewerColumn(tblViewerRegistFileAll, SWT.NONE);
		TableColumn tblColDirPath = tblViewerCol3.getColumn();
		tblColLayout.setColumnData(tblColDirPath, new ColumnWeightData(30, true));
		tblColDirPath.setText("\ub514\ub809\ud1a0\ub9ac");
		tblColDirPath.setAlignment(SWT.CENTER);

        TableViewerColumn tblViewerCol4 = new TableViewerColumn(tblViewerRegistFileAll, SWT.NONE);
		TableColumn tblColProgComment = tblViewerCol4.getColumn();
		tblColLayout.setColumnData(tblColProgComment, new ColumnWeightData(20, true));
		tblColProgComment.setText("\ud504\ub85c\uadf8\ub7a8\uc124\uba85"); 
        tblColProgComment.setAlignment(SWT.CENTER);
        
        TableViewerColumn tblViewerCol5 = new TableViewerColumn(tblViewerRegistFileAll, SWT.NONE);
		TableColumn tblColProgKind = tblViewerCol5.getColumn();
		tblColLayout.setColumnData(tblColProgKind, new ColumnWeightData(15, true));
		tblColProgKind.setText("\ud504\ub85c\uadf8\ub7a8\uc885\ub958");
		tblColProgKind.setAlignment(SWT.CENTER);
        
        TableViewerColumn tblViewerCol6 = new TableViewerColumn(tblViewerRegistFileAll, SWT.NONE);
		TableColumn tblColJob = tblViewerCol6.getColumn();
		tblColLayout.setColumnData(tblColJob, new ColumnWeightData(10, true));
		tblColJob.setText("\uc5c5\ubb34");
		tblColJob.setAlignment(SWT.CENTER);
		
        TableViewerColumn tblViewerCol7 = new TableViewerColumn(tblViewerRegistFileAll, SWT.NONE);
		TableColumn tblColSRID = tblViewerCol7.getColumn();
		tblColLayout.setColumnData(tblColSRID, new ColumnWeightData(10, true));
		tblColSRID.setText("SRID");
		tblColSRID.setAlignment(SWT.CENTER);

		
		/* \ud558\ub2e8 \uc77c\uad04\ub4f1\ub85d \uc815\ubcf4 \uc138\ud305 */
		Composite registFileAllInfoContainer = new Composite(container, SWT.NONE);
		//regAllInfoContainer.setLayoutData(new FormData());
		
		FormData registFileAllInfoFormData = new FormData();
		registFileAllInfoFormData.right = new FormAttachment(tblViewerRegistFileAllContainer, 0, SWT.RIGHT);
		registFileAllInfoFormData.top = new FormAttachment(treeRegistFileAll, 2);
		registFileAllInfoFormData.bottom = new FormAttachment(100);
		registFileAllInfoFormData.left = new FormAttachment(0);
		registFileAllInfoContainer.setLayoutData(registFileAllInfoFormData);
		
		registFileAllInfoContainer.setLayout(new GridLayout(6, false));
		
		
		/* \uc804\uccb4\uc120\ud0dd */
		chkAll = new Button(registFileAllInfoContainer, SWT.CHECK);
		chkAll.setText("\uc804\uccb4\uc120\ud0dd");
		
		new Label(registFileAllInfoContainer, SWT.NONE);
		new Label(registFileAllInfoContainer, SWT.NONE);
		new Label(registFileAllInfoContainer, SWT.NONE);
		new Label(registFileAllInfoContainer, SWT.NONE);
		new Label(registFileAllInfoContainer, SWT.NONE);
		
		
		/* \ud504\ub85c\uadf8\ub7a8 \uc885\ub958 */
		Label lblProgKind = new Label(registFileAllInfoContainer, SWT.NONE);
		lblProgKind.setText("*\ud504\ub85c\uadf8\ub7a8\uc885\ub958");
		
		cboProgKind = new Combo(registFileAllInfoContainer, SWT.READ_ONLY);
		cboProgKind.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		cboProgKind.addSelectionListener(cboProgKindSelectListner);
		
		textProgKind = new Text(registFileAllInfoContainer, SWT.BORDER | SWT.READ_ONLY);
		textProgKind.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));


		/* \uc5c5\ubb34 */
		Label lblJob = new Label(registFileAllInfoContainer, SWT.NONE);
		lblJob.setText("*\uc5c5\ubb34");
		
		cboJob = new Combo(registFileAllInfoContainer, SWT.READ_ONLY);
		cboJob.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		//cboJob.setEnabled(false);	// 20201222 
		
		/* \ud504\ub85c\uadf8\ub7a8 \uc124\uba85 */
		Label lblProgComment = new Label(registFileAllInfoContainer, SWT.NONE);
		lblProgComment.setText("*\ud504\ub85c\uadf8\ub7a8\uc124\uba85");
		
		textProgComment = new Text(registFileAllInfoContainer, SWT.BORDER);
		textProgComment.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1));
		
		
		/* SRID */
		Label lblSRList = new Label(registFileAllInfoContainer, SWT.NONE);
		lblSRList.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblSRList.setText("*SR-ID");
		
		cboSRID = new Combo(registFileAllInfoContainer, SWT.READ_ONLY);
		cboSRID.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		
		
		/* \ubaa9\ub85d\ucd94\uac00 */
		Button btnListAdd = new Button(registFileAllInfoContainer, SWT.NONE);
		btnListAdd.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnListAdd.setText("\ubaa9\ub85d \ucd94\uac00");
		
		
		/* \ubaa9\ub85d\uc81c\uac70 */
		Button btnDel = new Button(registFileAllInfoContainer, SWT.CENTER);
		btnDel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnDel.setText("\ubaa9\ub85d \uc81c\uac70");


		/* \uc0c1\ub2e8 \uac2f\uc218 \ud45c\uc2dc \ub77c\ubca8 \ubd80\ubd84 */
		new Label(registFileAllInfoContainer, SWT.NONE);
		new Label(registFileAllInfoContainer, SWT.NONE);
		new Label(registFileAllInfoContainer, SWT.NONE);
		new Label(registFileAllInfoContainer, SWT.NONE);
		new Label(registFileAllInfoContainer, SWT.NONE);
		new Label(registFileAllInfoContainer, SWT.NONE);
		
		Composite leftLabelContainer = new Composite(container, SWT.NONE);
		leftLabelContainer.setLayout(new GridLayout(3, false));
		FormData leftLabelFormData = new FormData();
		leftLabelFormData.right = new FormAttachment(40);
		leftLabelFormData.top = new FormAttachment(0);
		leftLabelFormData.bottom = new FormAttachment(treeRegistFileAll, 0);
		leftLabelFormData.left = new FormAttachment(0);
		leftLabelContainer.setLayoutData(leftLabelFormData);
		
		lblTreeTotalCnt = new Label(leftLabelContainer, SWT.NONE);
		lblTreeTotalCnt.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, true, 2, 1));
		lblTreeTotalCnt.setText("\ucd1d 0\uac1c");
		
		lblTreeSelectedCnt = new Label(leftLabelContainer, SWT.NONE);
		lblTreeSelectedCnt.setAlignment(SWT.RIGHT);
		lblTreeSelectedCnt.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, true, 1, 1));
		lblTreeSelectedCnt.setText("0\uac1c \uc120\ud0dd\ub428.");
		
		Composite rightLabelContainer = new Composite(container, SWT.NONE);
		rightLabelContainer.setLayout(new GridLayout(3, false));
		FormData rightLabelFormData = new FormData();
		rightLabelFormData.right = new FormAttachment(100);
		rightLabelFormData.top = new FormAttachment(0);
		rightLabelFormData.bottom = new FormAttachment(treeRegistFileAll,0);
		rightLabelFormData.left = new FormAttachment(40);
		rightLabelContainer.setLayoutData(rightLabelFormData);
		
		lblTblTotalCnt = new Label(rightLabelContainer, SWT.NONE);
		GridData gd_lblTblTotalCnt = new GridData(SWT.FILL, SWT.BOTTOM, true, true, 2, 1);
		gd_lblTblTotalCnt.horizontalIndent = 10;
		lblTblTotalCnt.setLayoutData(gd_lblTblTotalCnt);
		lblTblTotalCnt.setText("\ucd1d 0\uac1c");
		
		lblTblSelectedCnt = new Label(rightLabelContainer, SWT.NONE);
		lblTblSelectedCnt.setAlignment(SWT.RIGHT);
		lblTblSelectedCnt.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, true, true, 1, 1));
		lblTblSelectedCnt.setText("0\uac1c \uc120\ud0dd\ub428.");		
		
		
		/* \ub9ac\uc2a4\ub108 \uc5f0\uacb0 */
		chkAll.addMouseListener(chkAllClickListener);
		((CheckboxTreeViewer)treeRegistFileAll.getTreeViewer()).addCheckStateListener(treeCheckListner);
		btnListAdd.addMouseListener(btnListAddClickListener);
		btnDel.addMouseListener(btnListDelClickListener);
		tblRegistFileAll.addSelectionListener(tableSelectListner);

		
		getSRList();
		getJobList();
		
		/* \ud2b8\ub9ac\ubdf0\uc5b4 \ucd08\uae30\uc5d0 \ubaa8\ub450 \uccb4\ud06c \ud574\uc81c */
		chkAllClick(false);
		setLblCnt(TREE_TOTAL_CNT, tmpTreeTotalCnt);
		
		setControl(container);
		setPageComplete(true);
	}
	
	
	public void setLblCnt(int lblGbnCd, int cnt) {
		switch(lblGbnCd) {
			case TREE_TOTAL_CNT:
									lblTreeTotalCnt.setText("\ucd1d "+cnt+"\uac1c");
									break;
			case TREE_SELECTED_CNT:
									lblTreeSelectedCnt.setText(cnt+"\uac1c \uc120\ud0dd\ub428.");
									break;
			case TABLE_TOTAL_CNT:
									lblTblTotalCnt.setText("\ucd1d "+cnt+"\uac1c");
									if( "NEW".equals(gbnCd) ) {
										if( 0 < cnt ) {
											cboSRID.setEnabled(false);
										} else {
											cboSRID.setEnabled(true);
										}
									}
									break;
			case TABLE_SELECTED_CNT:
									lblTblSelectedCnt.setText(cnt+"\uac1c \uc120\ud0dd\ub428.");
									break;
			default:				break;
		}
		
	}
	public void setListner(){
		/* \ud2b8\ub9ac \uc804\uccb4\uc120\ud0dd \ub9ac\uc2a4\ub108 \uc138\ud305 */
		chkAllClickListener = new MouseListener(){
			public void mouseDoubleClick(MouseEvent e) {}
			public void mouseDown(MouseEvent e) {}
			public void mouseUp(MouseEvent e) {
				chkAllClick( chkAll.getSelection() );
			}
		};
		
		/* \ud2b8\ub9ac \uccb4\ud06c\ubc15\uc2a4 \ub9ac\uc2a4\ub108 \uc138\ud305 */
		treeCheckListner = new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				
				/* \ud2b8\ub9ac\uc5d0\uc11c \ud558\uc704 \uc694\uc18c\uc758 \uccb4\ud06c \uc5ec\ubd80\uc5d0 \ub530\ub77c \ubd80\ubaa8\uc758 \uccb4\ud06c\uc5ec\ubd80 \ubcc0\uacbd */
				TreeItem[] parentElement = treeRegistFileAll.getTreeViewer().getTree().getItems(); //\ubd80\ubaa8\uc694\uc18c(\ub514\ub809\ud1a0\ub9ac)
				int parentElementCnt = parentElement.length;
				int checkedParentElementCnt = 0;
				
				for(int i=0; i<parentElementCnt; i++) {
					TreeItem[] childElement = parentElement[i].getItems(); //\uc790\uc2dd\uc694\uc18c(\ud504\ub85c\uadf8\ub7a8)
					int childElementCnt = childElement.length;
					int checkedChildElementCnt = 0; //\uccb4\ud06c\ubc15\uc2a4\uc5d0 \uccb4\ud06c\ub41c \uc790\uc2dd\uc694\uc18c \uc218
					
					for( int j=0; j<childElementCnt; j++ ) {
						if( childElement[j].getChecked() ) { //\uc790\uc2dd\uc694\uc18c \uc911 \uccb4\ud06c\ubc15\uc2a4\uc5d0 \uccb4\ud06c\ub41c \uac74\uc774 \uc788\uc73c\uba74 \uccb4\ud06c\uce74\uc6b4\ud2b8 \uc99d\uac00
							checkedChildElementCnt++;
						}
					}
					
					if( childElementCnt == checkedChildElementCnt ) { //\uc790\uc2dd\uc694\uc18c\uc758 \uac2f\uc218\uc640 \uccb4\ud06c\ub41c \uc790\uc2dd\uc694\uc18c \uac2f\uc218\uac00 \uac19\uc73c\uba74(\uc790\uc2dd\uc694\uc18c \ubaa8\ub450 \uc120\ud0dd)
						if( checkedChildElementCnt > 0 ) { //\uc790\uc2dd\uc694\uc18c\uac00 1\uac1c \uc774\uc0c1\uc774\uba74
							parentElement[i].setChecked(true);
							checkedParentElementCnt++;
						} else { //\uc790\uc2dd\uc694\uc18c\uac00 \uc5c6\uc73c\uba74 \uccb4\ud06c\ud574\uc81c(\ubaa9\ub85d\uc5d0\uc11c \uc6d0\ub798 \ud45c\uc2dc \uc548\ub418\ub098, \uba85\uc2dc\uc801\uc73c\ub85c \uc791\uc131)
							parentElement[i].setChecked(false);
						}
					} else { //\uc790\uc2dd\uc694\uc18c\uc758 \uac2f\uc218\uc640 \uccb4\ud06c\ub41c \uc790\uc2dd\uc694\uc18c \uac2f\uc218\uac00 \ub2e4\ub974\uba74 \ubd80\ubaa8\uc694\uc18c\uc758 \uccb4\ud06c \ud574\uc81c
						parentElement[i].setChecked(false);
					}
				}
				
				if( parentElementCnt == checkedParentElementCnt ) { //\ubd80\ubaa8\uc694\uc18c\uc758 \uac2f\uc218\uc640 \uccb4\ud06c\ub41c \ubd80\ubaa8\uc694\uc218\uc758 \uac2f\uc218\uac00 \uac19\uc73c\uba74(\ubd80\ubaa8\uc694\uc18c \ubaa8\ub450\uc120\ud0dd)
					chkAll.setSelection(true);
				} else {
					chkAll.setSelection(false);
				}
				
				selectedTreeResources = treeRegistFileAll.getSelectedResources();
				setLblCnt(TREE_SELECTED_CNT, selectedTreeResources.length);
			}
		};
		
		/* \ud504\ub85c\uadf8\ub7a8\uc885\ub958 \uc120\ud0dd \ub9ac\uc2a4\ub108 \uc138\ud305 */
		cboProgKindSelectListner = new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				textProgKind.setText( ((Hashtable)cboProgKind.getData("cm_rsrcexe")).get(cboProgKind.getSelectionIndex()).toString() );
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
		};
		
		/* \ubaa9\ub85d\ucd94\uac00 \ubc84\ud2bc \ub9ac\uc2a4\ub108 \uc138\ud305 */
		btnListAddClickListener = new MouseListener() {
			@Override
			public void mouseUp(MouseEvent e) {
				String error = "";
				if( selectedTreeResources.length < 1 ) {
					showErrMsg("\ud2b8\ub9ac\uc5d0\uc11c \ud504\ub85c\uadf8\ub7a8\uc744 \uc120\ud0dd\ud574 \uc8fc\uc2ed\uc2dc\uc624.");
					return;
				}
				
				/*	//20201222
				if( cboProgKind.getSelectionIndex() < 1 ) {
					showErrMsg("\ud504\ub85c\uadf8\ub7a8\uc885\ub958\ub97c \uc120\ud0dd\ud574\uc8fc \uc2ed\uc2dc\uc624.");
					return;
				}
				
				if( cboJob.getSelectionIndex() < 1 ) {
					showErrMsg("\uc5c5\ubb34\ub97c \uc120\ud0dd\ud574\uc8fc \uc2ed\uc2dc\uc624.");
					return;
				}
				*/
				
				if( textProgComment.getText().trim().length() < 1 ) {
					showErrMsg("\uc124\uba85\uc744 \uc785\ub825\ud574 \uc8fc\uc2ed\uc2dc\uc624.");
					textProgComment.setText("");
					textProgComment.setFocus();
					return;
				}
				
				/* \ud2b8\ub9ac\uc5d0\uc11c \uc120\ud0dd\ud55c \ub9ac\uc18c\uc2a4 \ubaa9\ub85d\uacfc \ub300\uc751\ud558\ub294 \uc815\ubcf4\ub4e4\uc744 \ud574\uc26c\ud14c\uc774\ube14\uc5d0 \ub2f4\uc74c. */
				int initCnt = tblRegistFileAll.getItemCount(); //loop\uc2dc \ucd08\uae30 \uac12 = \ud604\uc7ac \ud14c\uc774\ube14\uc5d0 \uc544\uc774\ud15c \uc218
				int loopCnt = initCnt + selectedTreeResources.length; //loop\uac12 = initCnt + \ud2b8\ub9ac\uc5d0\uc11c \uc120\ud0dd\ud55c \ub9ac\uc18c\uc2a4\uc758 \uc218
				
				String cm_rsrcinfo = ((Hashtable)cboProgKind.getData("cm_rsrcinfo")).get(cboProgKind.getSelectionIndex()).toString(); //\uc120\ud0dd\ud55c \uc5c5\ubb34\uc758 \uc18d\uc131
				boolean isCorrectExe = false; //\uc720\ud6a8\ud55c \ud655\uc7a5\uc790 \uc5ec\ubd80
				boolean hasNoCheckExe = false; //'\ud655\uc7a5\uc790\uccb4\ud06c\uc548\ud568'\uc18d\uc131\uc744 \uac16\uace0\uc788\ub294\uc9c0 \uc5ec\ubd80
				boolean hasAutoNewItem = false; //'\uc790\ub3d9\uc2e0\uaddc\ud56d\ubaa9'\uc18d\uc131\uc744 \uac16\uace0 \uc788\ub294\uc9c0 \uc5ec\ubd80
				String msgExeCheckErr = ""; //\ud655\uc7a5\uc790 \uccb4\ud06c \uc624\ub958\uba54\uc138\uc9c0 \ub2f4\uc744 \ubcc0\uc218
				
				/*	// 20201222 \uc8fc\uc11d\ucc98\ub9ac
				if( "1".equals(cm_rsrcinfo.substring(NO_CHECK_EXE, NO_CHECK_EXE+1)) ) { //'\ud655\uc7a5\uc790 \uccb4\ud06c\uc548\ud568'\uc774\uba74
					hasNoCheckExe = true;
				}
				
				if( "1".equals(cm_rsrcinfo.substring(AUTO_NEW_ITEM, AUTO_NEW_ITEM+1)) ) { //'\uc790\ub3d9\uc2e0\uaddc\ud56d\ubaa9'\uc774\uba74
					hasAutoNewItem = true;
				}
				*/
				
				if( cboProgKind.getSelectionIndex() > 0) {
					if( "1".equals(cm_rsrcinfo.substring(NO_CHECK_EXE, NO_CHECK_EXE+1)) ) { //'\ud655\uc7a5\uc790 \uccb4\ud06c\uc548\ud568'\uc774\uba74
						hasNoCheckExe = true;
					}
					
					if( "1".equals(cm_rsrcinfo.substring(AUTO_NEW_ITEM, AUTO_NEW_ITEM+1)) ) { //'\uc790\ub3d9\uc2e0\uaddc\ud56d\ubaa9'\uc774\uba74
						hasAutoNewItem = true;
					}
					if( cboJob.getSelectionIndex() < 1 ) {
						showErrMsg("\uc5c5\ubb34\ub97c \uc120\ud0dd\ud574\uc8fc \uc2ed\uc2dc\uc624.");
						return;
					}
				}

				/* \ud655\uc7a5\uc790 \uccb4\ud06c Loop */
				
				/*
				 * \ud604\uc7ac \ud504\ub85c\uc81d\ud2b8\uc758 \uc2dc\uc2a4\ud15c\uc5d0\uc11c \uc0ac\uc6a9\ud558\ub294 \ud504\ub85c\uadf8\ub7a8\uc885\ub958\uc758 \uc18d\uc131 \uc911 '\ud655\uc7a5\uc790\uccb4\ud06c\uc548\ud568'\uc774\ub77c\ub294 \uc18d\uc131\uc774 \uc5c6\uc73c\uba74 
				 * 1)\ud504\ub85c\uadf8\ub7a8\uba85\uc774 ,\ub85c \ub05d\ub098\ub294\uc9c0?
				 * 2)XXX.\ud655\uc7a5\uc790\uba85 \uc2dd\uc758 \uc720\ud6a8\ud55c \ud615\uc2dd\uc778\uc9c0?
				 * 3)\ud655\uc7a5\uc790 \ube44\uad50\ub97c \ud1b5\ud574 \ud655\uc7a5\uc790\uac00 \uc720\ud6a8\ud55c\uc9c0\ub97c \uccb4\ud06c
				 * 4)\ube44\uad50 \ud6c4, \uc720\ud6a8\ud558\uc9c0 \uc54a\uc740 \ud655\uc7a5\uc790\uac00 \uc788\uc73c\uba74 \uc624\ub958 \uba54\uc138\uc9c0\uc640 \ud568\uaed8 \uc885\ub8cc, \uc720\ud6a8\ud55c \ud655\uc7a5\uc790\ub77c\uba74 \ud14c\uc774\ube14\uc5d0 \ub9ac\uc2a4\ud2b8\uc5c5 \uc2dc\ud0b4
				 * 
				 */
				
				int iCoreectExeCnt = -1;
				ArrayList<IResource> arrayListTempRsrc = new ArrayList<IResource>();
				IProject project= resources[0].getProject();
				try {
					Syscd = project.getPersistentProperty(new QualifiedName("Properties","syscd")).split(":")[0];
				} catch (CoreException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				boolean dircheck = false;
				boolean JobSw = false;
				if("I".equals(tool) && Syscd.equals("00100")){			// istudio \uad6c\ubd84
					for(int i=initCnt; i<loopCnt; i++) {
						dircheck = false;
						String tmprsrccd = "";
						String tmpjobcd = "";
						String tmprsrcname = "";
						String tmpjobname = "";
						
						isCorrectExe = false;
						
						String strTempProgName = ((IResource)selectedTreeResources[i-initCnt]).getName(); //\ud504\ub85c\uadf8\ub7a8\uba85 \ub2f4\uc744 \uc784\uc2dc \ubcc0\uc218
						IPath strTempPath = ((IResource)selectedTreeResources[i-initCnt]).getFullPath(); //\ud504\ub85c\uadf8\ub7a8\uacbd\ub85c\ub97c \ub2f4\uc744 \uc784\uc2dc \ubcc0\uc218
						String TmpPath = strTempPath.toString(); 
						
						String strTempExe = textProgKind.getText();
						if( null == strTempExe ) {
							strTempExe = "";
						}
						if( !"".equals(strTempExe) && (strTempExe.substring(strTempExe.length() - 1).equals(",") )) {
							strTempExe = strTempExe.substring(0, strTempExe.length() - 1);
						}
						if( (strTempProgName.indexOf(",") >= 0) ) {
							isCorrectExe = false;
						}
						//\uc5c5\ubb34 \uc138\ud305 ------------------------------
						
						if( cboJob.getSelectionIndex() < 1 ) {
							if(TmpPath.indexOf("/tfskr/mfis/ba/") >= 0){
								tmpjobcd = "BA";
								tmpjobname = "\uc7ac\ubb34\ud68c\uacc4/\uc790\uae08(BA)";
							} else if(TmpPath.indexOf("/tfskr/mfis/bc/") >= 0){
								tmpjobcd = "BC";
								tmpjobname = "\uace0\uac1d\uc81c\ud734(BC)";
							} else if(TmpPath.indexOf("/tfskr/mfis/bp/") >= 0){
								tmpjobcd = "BP";
								tmpjobname = "\uc5c5\ubb34\ud328\ud0a4\uc9c0(BP)";
							} else if(TmpPath.indexOf("/tfskr/mfis/cc/") >= 0){
								tmpjobcd = "CC";
								tmpjobname = "\uacf5\ud1b5(CC)";
							} else if(TmpPath.indexOf("/tfskr/mfis/lc/") >= 0){
								tmpjobcd = "LC";
								tmpjobname = "\ub9ac\uc2a4/\ud560\ubd80(LC)";
							} else if(TmpPath.indexOf("/tfskr/mfis/pf/") >= 0){
								tmpjobcd = "PF";
								tmpjobname = "\uac1c\uc778/\uae30\uc5c5\uae08\uc735(PF)";
							} else if(TmpPath.indexOf("/tfskr/mfis/fi/") >= 0){
								tmpjobcd = "FI";
								tmpjobname = "\uc608\uc0b0(FI)";
							} else {
								tmpjobcd = "ADMIN";
								tmpjobname = "\uad00\ub9ac\uc790";
							}
							JobSw = true;
						}else{
							JobSw = false;
						}
						// -------------------------------------------------------------------------
						if( hasAutoNewItem ) {
							isCorrectExe = true;
							iCoreectExeCnt++;
							if( strTempProgName.indexOf(".") >= 0 ) { //XX.\ud655\uc7a5\uc790 \uc2dd\uc758 \ud615\uc2dd\uc778\uc9c0\ub97c \ud310\ud0c4
								isCorrectExe = false;
							}
						} else {
							//if(TmpPath.indexOf("/.istudiometa") >= 0){	// 20201223 \uba54\ud0c0 \uacbd\ub85c\uac00 \ubc14\ub01c
							if(TmpPath.indexOf("/meta") >= 0){
								dircheck = false;
								tmprsrccd = "02";
								tmprsrcname = "\uba54\ud0c0_iStudio";
								dircheck = true;		
								isCorrectExe = true;
								iCoreectExeCnt++;
							}else{ 
								error = "";
								String tmpExe = "";
								if(strTempProgName.lastIndexOf(".") > 0) {	// 20200105 \ud504\ub85c\uadf8\ub7a8\uc5d0 \ud655\uc7a5\uc790\uac00 \uc5c6\ub294 \uacbd\uc6b0\uc5d0\ub294 \uac80\uc0c9\ud560 \uc218\uac00 \uc5c6\uae30 \ub54c\ubb38\uc5d0 \uc870\uac74\ubb38 \ucd94\uac00\ud574\uc90c(istudio\uc5d0\ub9cc if\ubb38\uc774 \uc5c6\uc5c8\uc74c)
									tmpExe = strTempProgName.substring( strTempProgName.lastIndexOf(".") ); //\ud504\uadf8\ub7a8\uba85\uc5d0\uc11c \ud655\uc7a5\uc790 \ucd94\ucd9c
								}
								if( hasNoCheckExe ) {		//\uc120\ud0dd\uccb4\ud06c
									isCorrectExe = true;
									iCoreectExeCnt++;
								}else {
									dircheck = false;
									//if(TmpPath.indexOf("IFIS/src") >= 0){
									if(TmpPath.indexOf("/src-db") >= 0){
										if(tmpExe.equals(".sql")){
											tmprsrccd = "07";
											tmprsrcname = "PL/SQL";
											dircheck = true;
										}else{
											dircheck = false;
										}
									}else if(TmpPath.indexOf("/src-etc") >= 0){
										tmprsrccd = "24";
										tmprsrcname = "\ubc84\uc804\uad00\ub9ac";
										dircheck = true;
									}else if(tmpExe.equals(".java")){
										tmprsrccd = "32";
										tmprsrcname = "Java";
										dircheck = true;
									}else{
										dircheck = false;
											
										if(TmpPath.indexOf("TFSKR/WebContent") >= 0){
											if (TmpPath.indexOf("/WEB-INF") >= 0){
												if(tmpExe.equals(".jar")){
													tmprsrccd = "40";
													tmprsrcname = "\ubc30\ud3ec\ud30c\uc77c(WebContent)";
													dircheck = true;
												} else{
													dircheck = false;
												}
											}else if(TmpPath.indexOf("/resources/scripts") >= 0){
												tmprsrccd = "23";
												tmprsrcname = "Js(Scripts)";
												dircheck = true;
											}else if(tmpExe.equals(".xml") || tmpExe.equals(".xhtml") || tmpExe.equals(".reb") || tmpExe.equals(".jsp")
													 || tmpExe.equals(".jar") || tmpExe.equals(".conf") || tmpExe.equals(".wmf") || tmpExe.equals(".thmx") || tmpExe.equals(".emz")
													 || tmpExe.equals(".crf")){
												tmprsrccd = "40";
												tmprsrcname = "\ubc30\ud3ec\ud30c\uc77c(WebContent)";
												dircheck = true;
											}else if(tmpExe.equals(".js") || tmpExe.equals(".html") || tmpExe.equals(".htm") || tmpExe.equals(".exe")
													 || tmpExe.equals(".cab") || tmpExe.equals(".jpg") || tmpExe.equals(".ico") || tmpExe.equals(".png")
													 || tmpExe.equals(".gif") || tmpExe.equals(".css") || tmpExe.equals(".CAB") || tmpExe.equals(".JPG") || tmpExe.equals(".xsl")){
												tmprsrccd = "40";
												tmprsrcname = "\ubc30\ud3ec\ud30c\uc77c(WebContent)";
												dircheck = true;
											}
										}else if(TmpPath.indexOf("TFSKR/UserComponent") >= 0 || TmpPath.indexOf("TFSKR/batchconfig") >= 0 || TmpPath.indexOf("TFSKR/configuration") >= 0 
												|| TmpPath.indexOf("TFSKR/lib") >= 0 || TmpPath.indexOf("TFSKR/templates") >= 0 || TmpPath.indexOf("TFSKR/unittest") >= 0
												|| TmpPath.indexOf("TFSKR/.settings") >= 0 || TmpPath.indexOf("TFSKR/BizComponent") >= 0){
											tmprsrccd = "24";
											tmprsrcname = "\ubc84\uc804\uad00\ub9ac";
											dircheck = true;
										}else{
											dircheck = false;
										}
									}
									if(dircheck){
										isCorrectExe = true;
										iCoreectExeCnt++;
									}else{
										isCorrectExe = false;
									}
								}
							}
						}
						if(!dircheck){
							if( isCorrectExe ) {
								Hashtable<String, Object> tempHashTable = new Hashtable<String, Object>();
								IResource tempResources = (IResource)selectedTreeResources[i-initCnt];
								arrayListTempRsrc.add(tempResources);
								
								tempHashTable.put("resource",		tempResources );
								tempHashTable.put("rsrcname",		tempResources.getName() );
								tempHashTable.put("dirpath",		tempResources.getParent().getProjectRelativePath().toString() );
								tempHashTable.put("story",			textProgComment.getText().trim() );
								tempHashTable.put("rsrctype",		cboProgKind.getText());
								tempHashTable.put("jobname",		cboJob.getText() );
								tempHashTable.put("srid",			((Hashtable)cboSRID.getData("cc_srid")).get(cboSRID.getSelectionIndex()) );
								tempHashTable.put("srtitle",		((Hashtable)cboSRID.getData("cc_srtitle")).get(cboSRID.getSelectionIndex()) );
								tempHashTable.put("rsrccd",			((Hashtable)cboProgKind.getData("cm_rsrccd")).get(cboProgKind.getSelectionIndex()) );
								tempHashTable.put("jobcd",			((Hashtable)cboJob.getData("cm_jobcd")).get(cboJob.getSelectionIndex()) );
								
								registFileAllResources.put(initCnt+iCoreectExeCnt, tempHashTable);
							}
						}else{ 
							if(JobSw){
								if( isCorrectExe ) {
									Hashtable<String, Object> tempHashTable = new Hashtable<String, Object>();
									IResource tempResources = (IResource)selectedTreeResources[i-initCnt];
									arrayListTempRsrc.add(tempResources);
									
									tempHashTable.put("resource",		tempResources );
									tempHashTable.put("rsrcname",		tempResources.getName() );
									tempHashTable.put("dirpath",		tempResources.getParent().getProjectRelativePath().toString() );
									tempHashTable.put("story",			textProgComment.getText().trim() );
									tempHashTable.put("rsrctype",		tmprsrcname);
									tempHashTable.put("jobname",		tmpjobname);
									tempHashTable.put("srid",			((Hashtable)cboSRID.getData("cc_srid")).get(cboSRID.getSelectionIndex()) );
									tempHashTable.put("srtitle",		((Hashtable)cboSRID.getData("cc_srtitle")).get(cboSRID.getSelectionIndex()) );
									tempHashTable.put("rsrccd",			tmprsrccd);
									tempHashTable.put("jobcd",			tmpjobcd);
									
									registFileAllResources.put(initCnt+iCoreectExeCnt, tempHashTable);
								}
							}else{
								if( isCorrectExe ) {
									Hashtable<String, Object> tempHashTable = new Hashtable<String, Object>();
									IResource tempResources = (IResource)selectedTreeResources[i-initCnt];
									arrayListTempRsrc.add(tempResources);
									
									tempHashTable.put("resource",		tempResources );
									tempHashTable.put("rsrcname",		tempResources.getName() );
									tempHashTable.put("dirpath",		tempResources.getParent().getProjectRelativePath().toString() );
									tempHashTable.put("story",			textProgComment.getText().trim() );
									tempHashTable.put("rsrctype",		tmprsrcname);
									tempHashTable.put("jobname",		cboJob.getText() );
									tempHashTable.put("srid",			((Hashtable)cboSRID.getData("cc_srid")).get(cboSRID.getSelectionIndex()) );
									tempHashTable.put("srtitle",		((Hashtable)cboSRID.getData("cc_srtitle")).get(cboSRID.getSelectionIndex()) );
									tempHashTable.put("rsrccd",			tmprsrccd);
									tempHashTable.put("jobcd",			((Hashtable)cboJob.getData("cm_jobcd")).get(cboJob.getSelectionIndex()) );
									
									registFileAllResources.put(initCnt+iCoreectExeCnt, tempHashTable);
								}
							}
						}
					} 
			}else{
				if( cboProgKind.getSelectionIndex() < 1 ) {
					showErrMsg("\ud504\ub85c\uadf8\ub7a8\uc885\ub958\ub97c \uc120\ud0dd\ud574\uc8fc \uc2ed\uc2dc\uc624.");
					return;
				}
				if( cboJob.getSelectionIndex() < 1 ) {
					showErrMsg("\uc5c5\ubb34\ub97c \uc120\ud0dd\ud574\uc8fc \uc2ed\uc2dc\uc624.");
					return;
				}
				for(int i=initCnt; i<loopCnt; i++) {
					isCorrectExe = false;
					
					String strTempProgName = ((IResource)selectedTreeResources[i-initCnt]).getName(); //\ud504\ub85c\uadf8\ub7a8\uba85 \ub2f4\uc744 \uc784\uc2dc \ubcc0\uc218
					String strTempExe = textProgKind.getText();
					if( null == strTempExe ) {
						strTempExe = "";
					}
					
					
					if( !"".equals(strTempExe) && (strTempExe.substring(strTempExe.length() - 1) == ",") ) {
						strTempExe = strTempExe.substring(0, strTempExe.length() - 1);
					}
					
					if( (strTempProgName.indexOf(",") >= 0) ) {
						isCorrectExe = false;
//						msgExeCheckErr = "\ud504\ub85c\uadf8\ub7a8\uba85\uc5d0 \ucef4\ub9c8(,)\uac00 \ud3ec\ud568\ub418\uc5b4\uc788\uc2b5\ub2c8\ub2e4. \uc81c\uc678\ud558\uace0 \ub4f1\ub85d\ud558\uc5ec \uc8fc\uc2ed\uc2dc\uc624. [" + strTempExe + "] [" + strTempProgName + "]";
//						break;
					}
					
					if( hasAutoNewItem ) {
						isCorrectExe = true;
						iCoreectExeCnt++;
						
						if( strTempProgName.indexOf(".") >= 0 ) { //XX.\ud655\uc7a5\uc790 \uc2dd\uc758 \ud615\uc2dd\uc778\uc9c0\ub97c \ud310\ud0c4
							isCorrectExe = false;
//							msgExeCheckErr = "\uc790\ub3d9\uc2e0\uaddc\ud56d\ubaa9\uc740 \ud655\uc7a5\uc790\uc5c6\uc774 \ub4f1\ub85d\ud558\uc5ec\uc57c \ud569\ub2c8\ub2e4.\n"
//											+ "[" + strTempProgName + "]";
//							break;
						}
					} else {
						if( (strTempExe != null) && !strTempExe.equals("") ) {
							if( hasNoCheckExe ) {
								isCorrectExe = true;
								iCoreectExeCnt++;
							} else if( strTempProgName.indexOf(".") > 0 ) {
								String tmpExe = strTempProgName.substring( strTempProgName.lastIndexOf(".") ); //\ud504\uadf8\ub7a8\uba85\uc5d0\uc11c \ud655\uc7a5\uc790 \ucd94\ucd9c
								tmpExe = tmpExe + ","; //.toUpperCase() //\ub300\ubb38\uc790\ub85c \ubcc0\ud658\uc2dc\ud0b4\uacfc \ub3d9\uc2dc\uc5d0 \ud655\uc7a5\uc790 \ube44\uad50\ub97c \uc704\ud574 \ub9e8\ub4a4\uc5d0 ,\ub97c \ubd99\uc5ec\uc90c.
								String rsrcExe = textProgKind.getText(); //.toUpperCase() //\ud504\ub85c\uadf8\ub7a8\uc885\ub958\uc758 \ub300\uc0c1\ud655\uc7a5\uc790\ub97c \uac00\uc838\uc634.
								if( rsrcExe.indexOf(tmpExe) >= 0) { //\ub300\uc0c1\ud655\uc7a5\uc790 \uc548\uc5d0 \ucd94\ucd9c\ud55c \ud655\uc7a5\uc790\uac00 \uc788\uc73c\uba74
									isCorrectExe = true;
									iCoreectExeCnt++;
								} else {
									//showErrMsg("\ud655\uc7a5\uc790 \ud2c0\ub9bc");
									isCorrectExe = false;
//									msgExeCheckErr = "\ud655\uc7a5\uc790\ub97c \uc815\ud655\ud788 \uc785\ub825\ud558\uc5ec\uc8fc\uc2ed\uc2dc\uc624.\n"
//													+ "[" + strTempExe + "]\n"
//													+ "[" + strTempProgName + "]";
//									break;
								}
							} else {
								isCorrectExe = false;
//								msgExeCheckErr = "\ud504\ub85c\uadf8\ub7a8\uba85\uc774 \uc5c6\uc2b5\ub2c8\ub2e4.\n"
//												+ "[" + strTempProgName + "]";
//								break;
							}
						} else {
							if( hasNoCheckExe ) {
								isCorrectExe = true;
								iCoreectExeCnt++;
							}else {
								isCorrectExe = false;
							}
						}
					}
					
					if (Syscd.equals("00015")) {
						if (strTempProgName.indexOf(".clx.js") > 0) {
							isCorrectExe = false;
						}
					}
					
					if( isCorrectExe ) {
						Hashtable<String, Object> tempHashTable = new Hashtable<String, Object>();
						IResource tempResources = (IResource)selectedTreeResources[i-initCnt];
						arrayListTempRsrc.add(tempResources);
						
						tempHashTable.put("resource",		tempResources );
						tempHashTable.put("rsrcname",		tempResources.getName() );
						tempHashTable.put("dirpath",		tempResources.getParent().getProjectRelativePath().toString() );
						tempHashTable.put("story",			textProgComment.getText().trim() );
						tempHashTable.put("rsrctype",		cboProgKind.getText() );
						tempHashTable.put("jobname",		cboJob.getText() );
						tempHashTable.put("srid",			((Hashtable)cboSRID.getData("cc_srid")).get(cboSRID.getSelectionIndex()) );
						tempHashTable.put("srtitle",		((Hashtable)cboSRID.getData("cc_srtitle")).get(cboSRID.getSelectionIndex()) );
						tempHashTable.put("rsrccd",			((Hashtable)cboProgKind.getData("cm_rsrccd")).get(cboProgKind.getSelectionIndex()) );
						tempHashTable.put("jobcd",			((Hashtable)cboJob.getData("cm_jobcd")).get(cboJob.getSelectionIndex()) );
						
						registFileAllResources.put(initCnt+iCoreectExeCnt, tempHashTable);
					}
				}
			}
				selectedTreeResources = null;
//				if( isCorrectExe ) { //\uc720\ud6a8\ud55c \ud655\uc7a5\uc790\ub97c \uac00\uc9c0\uace0 \uc788\ub2e4\uba74
//					for(int i=initCnt; i<loopCnt; i++) {
//						Hashtable<String, Object> tempHashTable = new Hashtable<String, Object>();
//						IResource tempResources = (IResource)selectedTreeResources[i-initCnt];
//						
//						tempHashTable.put("resource",		tempResources );
//						tempHashTable.put("rsrcname",		tempResources.getName() );
//						tempHashTable.put("dirpath",		tempResources.getParent().getProjectRelativePath().toString() );
//						tempHashTable.put("story",			textProgComment.getText().trim() );
//						tempHashTable.put("rsrctype",		cboProgKind.getText() );
//						tempHashTable.put("jobname",		cboJob.getText() );
//						tempHashTable.put("srid",			((Hashtable)cboSRID.getData("cc_srid")).get(cboSRID.getSelectionIndex()) );
//						tempHashTable.put("srtitle",		((Hashtable)cboSRID.getData("cc_srtitle")).get(cboSRID.getSelectionIndex()) );
//						tempHashTable.put("rsrccd",			((Hashtable)cboProgKind.getData("cm_rsrccd")).get(cboProgKind.getSelectionIndex()) );
//						tempHashTable.put("jobcd",			((Hashtable)cboJob.getData("cm_jobcd")).get(cboJob.getSelectionIndex()) );
//						
//						registFileAllResources.put(i, tempHashTable);
//					}
//				} else {
//					showErrMsg(msgExeCheckErr);
//					return;
//				}
				
				
				if( error.equals("ER")) {
					showErrMsg("Nexacro \ud504\ub85c\uadf8\ub7a8\uc885\ub958, \ud655\uc7a5\uc790\ub97c \ud655\uc778\ud558\uc2ed\uc2dc\uc624.");
					return;
				}
				if( arrayListTempRsrc.size() <= 0) {
					chkAll.setSelection(false);
					chkAllClick( chkAll.getSelection() );
					showErrMsg("\ubaa9\ub85d\uc5d0 \ucd94\uac00\ud560 \ub300\uc0c1\uc774 \uc5c6\uc2b5\ub2c8\ub2e4.");
					return;
				}
				
				/* \ud14c\uc774\ube14\uc5d0 \ub370\uc774\ud130 \uc138\ud305 */
				int iTmpRsrcSize = registFileAllResources.size();
				
				for(int i=initCnt; i<iTmpRsrcSize; i++){
					if (i>0) {
						if (null == registFileAllResources.get(i)) continue;
						if (null == registFileAllResources.get(i-1)) continue;
						
						if (registFileAllResources.get(i).get("rsrcname").toString().equals(registFileAllResources.get(i-1).get("rsrcname").toString()) &&
								registFileAllResources.get(i).get("dirpath").toString().equals(registFileAllResources.get(i-1).get("dirpath").toString())) {
							registFileAllResources.remove(i--);
							continue;
						}
					}
					TableItem item = new TableItem(tblRegistFileAll, SWT.NONE);
					int c = 0;
					item.setText(c++, Integer.toString(i+1));
					item.setText(c++, registFileAllResources.get(i).get("rsrcname").toString());
					item.setText(c++, registFileAllResources.get(i).get("dirpath").toString());
					item.setText(c++, registFileAllResources.get(i).get("story").toString());
					item.setText(c++, registFileAllResources.get(i).get("rsrctype").toString());
					item.setText(c++, registFileAllResources.get(i).get("jobname").toString());
					item.setText(c++, registFileAllResources.get(i).get("srid").toString());
				}
				tblRegistFileAll.redraw();
				
				/* \ucd94\uac00 \ubc84\ud2bc \ud074\ub9ad\uc2dc \ud2b8\ub9ac\uc5d0\uc11c \ubaa9\ub85d \uc81c\uac70 */
				/*
				 * \uc804\uccb4\ud2b8\ub9ac\uc5d0\uc11c \uc77c\uad04\ub4f1\ub85d\uc815\ubcf4 \ud14c\uc774\ube14\ub85c \ub118\uae34 \ub370\uc774\ud130\ub97c \uc81c\uc678\ud55c \ub0a8\ub294 \ub370\uc774\ud130\ub4e4\uc744 \ucd94\ucd9c \ud574
				 * \ub2e4\uc2dc \ud2b8\ub9ac\ub97c \uad6c\uc131
				 * 
				 */
				ArrayList<IResource> tmpList = new ArrayList<IResource>();
				int iTmpTreeSize = treeRegistFileAll.getTreeViewer().getTree().getItemCount();
				
				for(int i =0; i<iTmpTreeSize; i++) { //\ud2b8\ub9ac \uc804\uccb4
					TreeItem tmpParentElement = treeRegistFileAll.getTreeViewer().getTree().getItem(i);
					int iParentElementCnt = tmpParentElement.getItemCount();
					
					for(int j =0; j<iParentElementCnt; j++) { //\ubd80\ubaa8 \uc5d8\ub9ac\uba3c\ud2b8
						boolean sameFlag = false; //\ud2b8\ub9ac\uc5d0\uc11c \uc120\ud0dd\ud55c \ub370\uc774\ud130\ub97c \ud2b8\ub9ac\uc804\uccb4\uc5d0\uc11c \ube44\uad50\ud558\uc5ec \ub2e4\ub978\ub9ac\uc18c\uc2a4\uc778\uc9c0 \uc5ec\ubd80 \ud310\ub2e8\ud558\ub294 \ud50c\ub798\uadf8
						int iTmpSeletectedTreeSize = arrayListTempRsrc.size();
						Object tmpChildElementData = tmpParentElement.getItem(j).getData();
						
						for(int k=0; k<iTmpSeletectedTreeSize; k++) {
							if( tmpChildElementData.equals(((IResource)arrayListTempRsrc.get(k))) ){ //\ud2b8\ub9ac \uc804\uccb4\ub9ac\uc2a4\ud2b8\uc5d0\uc11c \uac00\uc838\uc628 \ub370\uc774\ud130\uc640 \uc120\ud0dd\ub41c \ud2b8\ub9ac\ub9ac\uc18c\uc2a4\uc640 \ube44\uad50\ud574\uc11c \uac19\uc740\uc9c0 \ud310\ub2e8
								sameFlag = true;
							}
						}
						
						if( !sameFlag ){
							tmpList.add((IResource) tmpChildElementData);
						}
					}
				}
				chkAll.setSelection(false);
				
				treeRegistFileAll.setDeleteViewer();
				if(	(treeRegistFileAll.getTreeViewer().getInput() != null) ){
					treeRegistFileAll.getTreeViewer().remove(treeRegistFileAll.getTreeViewer().getInput());
					treeRegistFileAll.getTreeViewer().refresh();
				}
				
			   if(tmpList.size()>0){
				   selectedTreeResources = null;
				   selectedTreeResources = (IResource[])tmpList.toArray(new IResource[tmpList.size()]);
				   treeRegistFileAll.setResources((IResource[]) selectedTreeResources);
				   //resourceSelectionTree.redraw();
				   chkAllClick(chkAll.getSelection());
			   }
			   
			   setLblCnt(TREE_TOTAL_CNT, tmpList.size());
			   setLblCnt(TABLE_TOTAL_CNT, tblRegistFileAll.getItemCount());
			   setLblCnt(TABLE_SELECTED_CNT, tblRegistFileAll.getSelectionCount());
			}
			
			@Override
			public void mouseDown(MouseEvent e) {
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}
		};
		

		/* \ubaa9\ub85d\uc81c\uac70 \ubc84\ud2bc \ub9ac\uc2a4\ub108 \uc138\ud305 */
		btnListDelClickListener = new MouseListener() {
			@Override
			public void mouseUp(MouseEvent e) {
				int[] deleteList = tblRegistFileAll.getSelectionIndices(); //\uc77c\uad04\ub4f1\ub85d \ub300\uc0c1\ubaa9\ub85d \ud14c\uc774\ube14\uc5d0\uc11c \uc120\ud0dd\ud55c \uc544\uc774\ud15c\uc758 \uc778\ub371\uc2a4\ub4e4
				ArrayList<IResource> tmpList = new ArrayList<IResource>();
				int iTmpDelCnt = deleteList.length; 
				
				for(int i=0; i<iTmpDelCnt; i++) {
					int iTmpRegAllRsrcCnt = registFileAllResources.size();
					
					for(int j=0; j<iTmpRegAllRsrcCnt; j++) { //\ud14c\uc774\ube14 \ubaa9\ub85d \uc804\uccb4\uc758 \uc6d0\ubcf8\uc774 \ub418\ub294 \ub9ac\uc18c\uc2a4\uc640 \ube44\uad50
						if( deleteList[i] == j ) { //\ud14c\uc774\ube14\uc5d0\uc11c \uc120\ud0dd\ud55c \uc778\ub371\uc2a4\uc640 \ub9ac\uc18c\uc2a4\uc758 \uc778\ub371\uc2a4\uac00 \uc77c\uce58\ud558\uba74
							
							tmpList.add((IResource)registFileAllResources.get(j).get("resource")); //\ud2b8\ub9ac\uc5d0 \ub118\uae38 \uc544\uc774\ud15c \ubaa9\ub85d \uc791\uc131
							
							/* \ud14c\uc774\ube14\ubaa9\ub85d\uc5d0\uc11c \uc0ad\uc81c */
							for( int k=j; k<iTmpRegAllRsrcCnt; k++ ) {
								registFileAllResources.remove(k); //\ud604\uc7ac\uac74 \uc0ad\uc81c
								
								if( registFileAllResources.get(k+1) != null ) { //\ub2e4\uc74c\uac74\uc774 null\uc774 \uc544\ub2c8\uba74 \uc989, \ub9c8\uc9c0\ub9c9 \uac74\uc774 \uc544\ub2c8\uba74
									registFileAllResources.put(k, registFileAllResources.get(k+1)); //\ub2e4\uc74c\uac74\uc744 \uc55e\uc73c\ub85c \ud558\ub098\uc529 \uc55e\uc73c\ub85c \ub561\uae40
									registFileAllResources.remove(k+1); //\ub2e4\uc74c\uac74 \uc0ad\uc81c
								}
							}
							
							/* \uc0ad\uc81c\ub300\uc0c1 \ubaa9\ub85d \uac31\uc2e0 */
							for( int m=i+1; m<iTmpDelCnt; m++ ) { //\ud604\uc7ac \uc774\ud6c4 \uac74\uc5d0 \ub300\ud574\uc11c \ubd80\ud130 \uc120\ud0dd\ub41c \uc778\ub371\uc2a4 \uac12 \uac10\uc18c \uc2dc\ud0b4
								deleteList[m]--;
							}
							break;
						}
					}
				}
				
				/* \uc77c\uad04\ub4f1\ub85d \uc815\ubcf4 \ud14c\uc774\ube14 \uac31\uc2e0 */
				tblRegistFileAll.removeAll();
				
				int iTmpRegAllRsrcCnt = registFileAllResources.size();
				
				for(int i=0; i<iTmpRegAllRsrcCnt; i++){
					TableItem item = new TableItem(tblRegistFileAll, SWT.NONE);
					int c = 0;
					item.setText(c++, Integer.toString(i+1));
					item.setText(c++, registFileAllResources.get(i).get("rsrcname").toString());
					item.setText(c++, registFileAllResources.get(i).get("dirpath").toString());
					item.setText(c++, registFileAllResources.get(i).get("story").toString());
					item.setText(c++, registFileAllResources.get(i).get("rsrctype").toString());
					item.setText(c++, registFileAllResources.get(i).get("jobname").toString());
					item.setText(c++, registFileAllResources.get(i).get("srid").toString());
				}
				
				tblRegistFileAll.redraw();
				
				/* \ud2b8\ub9ac\ub85c \ub418\ub3cc\ub9b4 \uc544\uc774\ud15c\uc774 \uc788\ub294 \uacbd\uc6b0 */
				if( tmpList.size()>0 ){
					int iTreeRegAllCnt = treeRegistFileAll.getTreeViewer().getTree().getItemCount();

					/* \uc77c\uad04\ub4f1\ub85d \ud14c\uc774\ube14\ubaa9\ub85d + \uae30\uc874 \ud2b8\ub9ac\uc544\uc774\ud15c \uc815\ubcf4\ub97c \ud569\uce58\ub294 \uc791\uc5c5 */
					for(int i =0; i<iTreeRegAllCnt; i++) {
						TreeItem tmpParentElement = treeRegistFileAll.getTreeViewer().getTree().getItem(i);
						int iTmpParentElementCnt = tmpParentElement.getItemCount();
 
						for(int j =0; j<iTmpParentElementCnt; j++) {
							tmpList.add( (IResource)tmpParentElement.getItem(j).getData() );
						}
					}
					
					/* \ud2b8\ub9ac\uac31\uc2e0 */
					treeRegistFileAll.setDeleteViewer();
					if(	treeRegistFileAll.getTreeViewer().getInput()!= null	){
						treeRegistFileAll.getTreeViewer().remove(treeRegistFileAll.getTreeViewer().getInput());
						treeRegistFileAll.getTreeViewer().refresh();
					}
					selectedTreeResources = null;
					selectedTreeResources = (IResource[])tmpList.toArray(new IResource[tmpList.size()]);

					treeRegistFileAll.setResources((IResource[]) selectedTreeResources);
					//resourceSelectionTree.redraw();
					chkAll.setSelection(false);
					chkAllClick(chkAll.getSelection());
					
				   setLblCnt(TREE_TOTAL_CNT, tmpList.size() );
				   setLblCnt(TABLE_TOTAL_CNT, tblRegistFileAll.getItemCount());
				   setLblCnt(TABLE_SELECTED_CNT, tblRegistFileAll.getSelectionCount());
				}				
			}
			
			@Override
			public void mouseDown(MouseEvent e) {
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}
		};
		
		/* \ud14c\uc774\ube14 \uc120\ud0dd \ub9ac\uc2a4\ub108 \uc138\ud305 */
		tableSelectListner = new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setLblCnt(TABLE_SELECTED_CNT, tblRegistFileAll.getSelectionCount());
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		};
	}
	
	public void getJobList(){
		try {
			RsrcInfoList rsrcInfoList = null;
			JobInfoList jobInfoList = null;
			
			IProject project= resources[0].getProject();
			
			EcamsMessage.Builder builder_msg = EcamsMessage.newBuilder();
			builder_msg.setMsgtype("JOBLIST_GET");
			
			SysInfo.Builder sysinfo_builder = SysInfo.newBuilder();
			sysinfo_builder.setSyscd(project.getPersistentProperty(new QualifiedName("Properties","syscd")).split(":")[0]);
			sysinfo_builder.setSysmsg(project.getPersistentProperty(new QualifiedName("Properties","syscd")).split(":")[1]);
			
			builder_msg.setSysinfo(sysinfo_builder.build());
			
			UserInfo.Builder userinfo_builder = UserInfo.newBuilder();
			userinfo_builder.setId(id);
			userinfo_builder.setPasswd(passwd);
			
			builder_msg.setUserinfo(userinfo_builder.build());				
			
			EcamsClient syncClient = new EcamsClient(ip,port);
			ReturnMsg returnMsg = syncClient.sendMsg(builder_msg.build());
			
			if(returnMsg.getReturnval() == 0){
				jobInfoList = returnMsg.getEcamsmsg().getJobinfolist();
				rsrcInfoList = returnMsg.getEcamsmsg().getSysinfo().getRsrcinfolist();
				
				int jobListCnt = jobInfoList.getJobinfoCount();
				int jawonListCnt = rsrcInfoList.getRsrcinfoCount();
				
				Hashtable<Integer, String> hashTblTempCd = new Hashtable<Integer, String>();
				Hashtable<Integer, String> hashTblTempInfo = new Hashtable<Integer, String>();
				Hashtable<Integer, String> hashTblTempExe = new Hashtable<Integer, String>();
				
				/*	//20201222
				cboJob.removeAll();
				cboJob.add("\uc120\ud0dd\ud558\uc138\uc694.");				
				//cboJob.select(0);
				
				cboProgKind.removeAll();
				cboProgKind.add("\uc120\ud0dd\ud558\uc138\uc694.");				
				cboProgKind.select(0);
				*/
				
				if("I".equals(tool) && project.getPersistentProperty(new QualifiedName("Properties","syscd")).split(":")[0].equals("00100")){
					cboJob.removeAll();
					cboJob.add("\uc790\ub3d9\ub4f1\ub85d");				
					cboJob.select(0);
					
					cboProgKind.removeAll();
					cboProgKind.add("\uc790\ub3d9\ub4f1\ub85d");				
					cboProgKind.select(0);
				}else{
					cboJob.removeAll();
					cboJob.add("\uc120\ud0dd\ud558\uc138\uc694.");				
					cboJob.select(0);
					
					cboProgKind.removeAll();
					cboProgKind.add("\uc120\ud0dd\ud558\uc138\uc694.");				
					cboProgKind.select(0);
				}
				
				hashTblTempCd.put(0, "");
				hashTblTempInfo.put(0, "");
				
				for (int i=0;i<jobListCnt;i++){	// 20201222 \uc8fc\uc11d \uc81c\uac70
					cboJob.add(jobInfoList.getJobinfo(i).getJobname());
					hashTblTempCd.put(i+1, jobInfoList.getJobinfo(i).getJobcd());
				}
				cboJob.setData("cm_jobcd",hashTblTempCd);
				cboJob.select(1);
				
				/*	// 20201222 \uc8fc\uc11d \ucc98\ub9ac
				cboJob.add(project.getPersistentProperty(new QualifiedName("Properties","setjobcd")).split(":")[1]);
				hashTblTempCd.put(1, project.getPersistentProperty(new QualifiedName("Properties","setjobcd")).split(":")[0]);
				cboJob.setData("cm_jobcd",hashTblTempCd);
				cboJob.select(1);
				*/
				hashTblTempCd = new Hashtable<Integer, String>();
				hashTblTempInfo = new Hashtable<Integer, String>();
				hashTblTempExe = new Hashtable<Integer, String>();
				
				hashTblTempCd.put(0, "");
				hashTblTempInfo.put(0, "");
				hashTblTempExe.put(0, "");
				
				for (int i=0;i<jawonListCnt;i++){
					cboProgKind.add(rsrcInfoList.getRsrcinfo(i).getRsrcmsg());
					hashTblTempCd.put(i+1, rsrcInfoList.getRsrcinfo(i).getRsrccd());
					hashTblTempInfo.put(i+1, rsrcInfoList.getRsrcinfo(i).getCminfo());
					
					String strTempExeName = rsrcInfoList.getRsrcinfo(i).getExename();
					if( "".equals(strTempExeName) ) {
//						if( !"1".equals(rsrcInfoList.getRsrcinfo(i).getCminfo().substring(NO_CHECK_EXE, NO_CHECK_EXE+1)) ) {
						if( "1".equals(rsrcInfoList.getRsrcinfo(i).getCminfo().substring(NO_CHECK_EXE, NO_CHECK_EXE+1)) ) {
							strTempExeName = "\ud655\uc7a5\uc790\uccb4\ud06c\uc548\ud568";
//							strTempExeName = "[\uad00\ub9ac\uc790\ubb38\uc758]\ub4f1\ub85d\ub41c \ud655\uc7a5\uc790 \uc5c6\uc74c";
						}
						else {
							strTempExeName = "[\uad00\ub9ac\uc790\ubb38\uc758]\ub4f1\ub85d\ub41c \ud655\uc7a5\uc790 \uc5c6\uc74c";
						}
					}else {
						if( "1".equals(rsrcInfoList.getRsrcinfo(i).getCminfo().substring(NO_CHECK_EXE, NO_CHECK_EXE+1)) ) {
							strTempExeName = "\ud655\uc7a5\uc790\uccb4\ud06c\uc548\ud568";
						}
					}
					hashTblTempExe.put(i+1, strTempExeName);
				}
				cboProgKind.setData("cm_rsrccd",hashTblTempCd);
				cboProgKind.setData("cm_rsrcinfo",hashTblTempInfo);
				cboProgKind.setData("cm_rsrcexe",hashTblTempExe);
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	public Hashtable<Integer, Hashtable<String, Object> > getRegAllResources() {
		return registFileAllResources;
	}
	
	public void showErrMsg(String msg) {
		MessageBox messageBox = new MessageBox(new Shell());
		messageBox.setText("ERROR");
		messageBox.setMessage(msg);
		messageBox.open();
	}
	
	public void chkAllClick(boolean checked) {
		TreeItem[] parentElement = treeRegistFileAll.getTreeViewer().getTree().getItems();
		int parentElementCnt = parentElement.length;
		
		for(int i=0; i<parentElementCnt; i++) {
			parentElement[i].setChecked(checked);
			
			TreeItem[] childElement = parentElement[i].getItems();
			int childElementCnt = childElement.length;
			if( childElementCnt > 0 ) {
				for( int j=0; j<childElementCnt; j++ ) {
					childElement[j].setChecked(checked);
				}
			}
		}
		selectedTreeResources = treeRegistFileAll.getSelectedResources();
		setLblCnt(TREE_SELECTED_CNT, selectedTreeResources.length);
	}
	
	public void getSRList() {
		try{
			ip = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.IP, "", null);
			port = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PORT, "", null);
			id = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.ID, "", null);
			passwd = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PASSWD, "", null);
			if(ip == null || port == null || id == null || passwd == null){
				return;
			}
			
			tool = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.TOOL, "", null);
			
			//tool = null;
			if(null == tool || "".equals(tool)){
				MessageDialog.openError(new Shell(),"Tool\uad6c\ubd84 \ud655\uc778","Preferences\uc5d0\uc11c \uac1c\ubc1c \ud234\uc744 \uc120\ud0dd\ud558\uc2ed\uc2dc\uc624.");
				return;
			}
			
			tblRegistFileAll.removeAll();
			
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
/*
			builder_msg = EcamsMessage.newBuilder();
			builder_msg.setMsgtype("SRLIST_GET");

			builder_msg.setUserinfo(userinfo_builder.build());			
			
			SRInfo.Builder srinfo_builder = SRInfo.newBuilder();
			srinfo_builder.setCcEditor(id);
			srinfo_builder.setIsAdmin(isAdmin);
			
			builder_msg.setSrinfo(srinfo_builder.build());
			
			syncClient = new EcamsClient(ip,port);
			
			returnMsg = syncClient.sendMsg(builder_msg.build());

			tblRegistFileAll.removeAll();
			
			if(returnMsg.getReturnval() == 0){
				Hashtable<Integer, String> hashTblTempSRID = new Hashtable<Integer, String>();
				Hashtable<Integer, String> hashTblTempSRTitle = new Hashtable<Integer, String>();
				
				cboSRID.removeAll();
				cboSRID.add("SR\uc120\ud0dd \ub610\ub294 \uc5c6\uc74c");	
				cboSRID.setData("SR\uc120\ud0dd \ub610\ub294 \uc5c6\uc74c","");
				cboSRID.select(0);
				hashTblTempSRID.put(0, "");
				hashTblTempSRTitle.put(0, "");
				
				if( "NEW".equals(gbnCd) ){
					for(int i=0;i<returnMsg.getEcamsmsg().getSrinfolist().getSrinfoCount();i++){
						String srInfo = "["+returnMsg.getEcamsmsg().getSrinfolist().getSrinfo(i).getCcSRId()+"]"+returnMsg.getEcamsmsg().getSrinfolist().getSrinfo(i).getCcTitle();
						cboSRID.add(srInfo);
						
						hashTblTempSRID.put(i+1, returnMsg.getEcamsmsg().getSrinfolist().getSrinfo(i).getCcSRId());
						hashTblTempSRTitle.put(i+1, returnMsg.getEcamsmsg().getSrinfolist().getSrinfo(i).getCcTitle());
					}
				} else if( "ALL".equals(gbnCd) ){
					cboSRID.setEnabled(false);
				} else{}
				
				cboSRID.setData("cc_srid", hashTblTempSRID);
				cboSRID.setData("cc_srtitle", hashTblTempSRTitle);
			}
*/
			Hashtable<Integer, String> hashTblTempSRID = new Hashtable<Integer, String>();
			Hashtable<Integer, String> hashTblTempSRTitle = new Hashtable<Integer, String>();
			cboSRID.removeAll();
			cboSRID.add("SR\uc120\ud0dd \ub610\ub294 \uc5c6\uc74c");	
			cboSRID.setData("SR\uc120\ud0dd \ub610\ub294 \uc5c6\uc74c","");
			cboSRID.select(0);
			hashTblTempSRID.put(0, "");
			hashTblTempSRTitle.put(0, "");
			cboSRID.setEnabled(false);
			cboSRID.setData("cc_srid", hashTblTempSRID);
			cboSRID.setData("cc_srtitle", hashTblTempSRTitle);
			
			
			tblRegistFileAll.redraw();
		}catch(Exception e){
			e.printStackTrace();
		}		
	}	
}
