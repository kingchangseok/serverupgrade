package com.azsoft.ecams.image;

import org.eclipse.jface.resource.ImageRegistry;

public class ImageUtil {

	private static ImageRegistry imageRegistry;
	

	public static ImageRegistry getImageRegistry(){
		if(imageRegistry == null){
			try{
				imageRegistry = new ImageRegistry();
				imageRegistry.put("Lock", EcamsImages.getImageDescriptor("Lock"));
				imageRegistry.put("Modified", EcamsImages.getImageDescriptor("Modified"));
				imageRegistry.put("Authority", EcamsImages.getImageDescriptor("Authority"));
				imageRegistry.put("AllDel", EcamsImages.getImageDescriptor("AllDel"));
				imageRegistry.put("SelDel", EcamsImages.getImageDescriptor("SelDel"));
				imageRegistry.put("Add", EcamsImages.getImageDescriptor("Add"));
				imageRegistry.put("Refresh", EcamsImages.getImageDescriptor("Refresh"));
				imageRegistry.put("ExpandAll", EcamsImages.getImageDescriptor("ExpandAll"));
				imageRegistry.put("CollapseAll", EcamsImages.getImageDescriptor("CollapseAll"));
				imageRegistry.put("History", EcamsImages.getImageDescriptor("History"));
				imageRegistry.put("ChkImg", EcamsImages.getImageDescriptor("ChkImg"));
				imageRegistry.put("Both", EcamsImages.getImageDescriptor("Both"));
				imageRegistry.put("Incoming", EcamsImages.getImageDescriptor("Incoming"));
				imageRegistry.put("Outgoing", EcamsImages.getImageDescriptor("Outgoing"));
				imageRegistry.put("Conflicts", EcamsImages.getImageDescriptor("Conflicts"));
				imageRegistry.put("In", EcamsImages.getImageDescriptor("In"));
				imageRegistry.put("Inplus", EcamsImages.getImageDescriptor("Inplus"));
				imageRegistry.put("Out", EcamsImages.getImageDescriptor("Out"));
				imageRegistry.put("Outplus", EcamsImages.getImageDescriptor("Outplus"));
				imageRegistry.put("Outminus", EcamsImages.getImageDescriptor("Outminus"));
				imageRegistry.put("Different", EcamsImages.getImageDescriptor("Different"));
			}catch(Exception e){
				imageRegistry = new ImageRegistry();
				imageRegistry.put("Lock", EcamsImages.getImageDescriptor("Lock"));
				imageRegistry.put("Modified", EcamsImages.getImageDescriptor("Modified"));
				imageRegistry.put("Authority", EcamsImages.getImageDescriptor("Authority"));
				imageRegistry.put("AllDel", EcamsImages.getImageDescriptor("AllDel"));
				imageRegistry.put("SelDel", EcamsImages.getImageDescriptor("SelDel"));
				imageRegistry.put("Add", EcamsImages.getImageDescriptor("Add"));
				imageRegistry.put("Refresh", EcamsImages.getImageDescriptor("Refresh"));
				imageRegistry.put("ExpandAll", EcamsImages.getImageDescriptor("ExpandAll"));
				imageRegistry.put("CollapseAll", EcamsImages.getImageDescriptor("CollapseAll"));
				imageRegistry.put("History", EcamsImages.getImageDescriptor("History"));
				imageRegistry.put("ChkImg", EcamsImages.getImageDescriptor("ChkImg"));
				imageRegistry.put("Both", EcamsImages.getImageDescriptor("Both"));
				imageRegistry.put("Incoming", EcamsImages.getImageDescriptor("Incoming"));
				imageRegistry.put("Outgoing", EcamsImages.getImageDescriptor("Outgoing"));
				imageRegistry.put("Conflicts", EcamsImages.getImageDescriptor("Conflicts"));
				imageRegistry.put("In", EcamsImages.getImageDescriptor("In"));
				imageRegistry.put("Inplus", EcamsImages.getImageDescriptor("Inplus"));
				imageRegistry.put("Out", EcamsImages.getImageDescriptor("Out"));
				imageRegistry.put("Outplus", EcamsImages.getImageDescriptor("Outplus"));
				imageRegistry.put("Outminus", EcamsImages.getImageDescriptor("Outminus"));
				imageRegistry.put("Different", EcamsImages.getImageDescriptor("Different"));
			}
		}
		return imageRegistry;
	}

	public static void removeImageRegistry(){
		if(imageRegistry != null){
			imageRegistry.remove("Lock");
			imageRegistry.remove("Modified");
			imageRegistry.remove("Authority");
			imageRegistry.remove("AllDel");
			imageRegistry.remove("SelDel");
			imageRegistry.remove("Add");
			imageRegistry.remove("Refresh");
			imageRegistry.remove("ExpandAll");
			imageRegistry.remove("CollapseAll");
			imageRegistry.remove("History");
			imageRegistry.remove("ChkImg");
			imageRegistry.remove("Both");
			imageRegistry.remove("Incoming");
			imageRegistry.remove("Outgoing");
			imageRegistry.remove("Conflicts");
			imageRegistry.remove("In");
			imageRegistry.remove("Inplus");
			imageRegistry.remove("Out");
			imageRegistry.remove("Outplus");
			imageRegistry.remove("Outminus");
			imageRegistry.remove("Different");
			imageRegistry = null;
		}
	}
	
}
