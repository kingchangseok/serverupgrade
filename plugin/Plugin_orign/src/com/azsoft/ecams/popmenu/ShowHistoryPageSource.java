package com.azsoft.ecams.popmenu;

import com.azsoft.ecams.core.EcamsProviderPlugin;
import com.azsoft.ecams.core.IEcamsStatus;
import com.azsoft.ecams.popmenu.ResourceUtil;
import com.azsoft.ecams.ui.dialog.ShowHistoryPage;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.team.ui.history.IHistoryPageSource;
import org.eclipse.ui.part.Page;


public class ShowHistoryPageSource implements IHistoryPageSource {
	
	private static ShowHistoryPageSource instance;
	private String filename,itemid,rsrcinfo;
	
	public static IFile[] getFiles(Object[] objects) {
		return ResourceUtil.getFiles(objects);
//		return ResourceUtil.getResources(objects);
	}

	public boolean canShowHistoryFor(Object object) {
		System.out.println(object);
		return getFiles((Object[]) object) != null;
	}

	public Page createPage(Object object) {
		IResource[] resource = (IResource[])object;
		
		IEcamsStatus status = EcamsProviderPlugin.getPlugin().getXmlStatusMgr().getStatus(resource[0]);
		
		if(status != null){
			filename = status.getName();
			itemid = status.getItemid().toString();
			rsrcinfo = status.getRsrcinfo().toString();
		}
		status = null;

		ShowHistoryPage page = new ShowHistoryPage(resource, filename,itemid,rsrcinfo);
		return page;
	}
	
	public synchronized static IHistoryPageSource getInstance() {
		if (instance == null)
			instance = new ShowHistoryPageSource();
		return instance;
	}
}
